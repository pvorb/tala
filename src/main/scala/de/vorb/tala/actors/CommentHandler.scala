package de.vorb.tala.actors

import java.util.Date
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import akka.actor.Actor

import org.mashupbots.socko.events.HttpResponseStatus

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

import de.vorb.tala.actors.Messages.GetComments
import de.vorb.tala.actors.Messages.ListComments
import de.vorb.tala.db.DBPool
import de.vorb.tala.model.Comment

class CommentHandler extends Actor {
    def receive = {
        case GetComments(http, uri) =>
            try {
                val comments = CommentHandler.cache.get(uri)
                http.response.contentType = "application/json"
                http.response.write(comments.mkString("[", ",", "]"))
            } catch {
                case e: ExecutionException =>
                    http.response.write(HttpResponseStatus.BAD_REQUEST)
            } finally {
                context.stop(self)
            }

        case ListComments(http) =>
            try {
                val comments = CommentHandler.cache.get("")
                http.response.contentType = "application/json"
                http.response.write(comments.mkString("[", ",", "]"))
            } catch {
                case _: NumberFormatException =>
                    http.response.write(HttpResponseStatus.BAD_REQUEST)
            }

            context.stop(self)
    }
}

object CommentHandler {
    val cache: LoadingCache[String, List[Comment]] = CacheBuilder
        .newBuilder()
        .maximumSize(32)
        .expireAfterAccess(10, TimeUnit.DAYS)
        .build(new CacheLoader[String, List[Comment]] {
            override def load(uri: String): List[Comment] = {
                val conn = DBPool.getConnection
                val stmt =
                    if (uri.nonEmpty) {
                        val stmt = conn.prepareStatement(
                            """|SELECT
                               |  comments.id, parent, created, modified, text,
                               |  author, email, website
                               |FROM threads, comments
                               |WHERE
                               |  threads.uri = ? AND
                               |  threads.id = comments.tid
                               |ORDER BY
                               |  comments.created ASC;
                               |""".stripMargin)
                        stmt.setString(1, uri)
                        stmt
                    } else {
                        val stmt = conn.prepareStatement(
                            """|SELECT
                               |  id, parent, created, modified, text, author,
                               |  email, website
                               |FROM comments
                               |ORDER BY
                               |  comments.created DESC
                               |LIMIT 10;
                               |""".stripMargin)
                        stmt
                    }

                stmt.setQueryTimeout(30) // TODO configurable timeout?
                val results = stmt.executeQuery()

                val comments = List.newBuilder[Comment]
                while (results.next()) {
                    val id = results.getLong("id")
                    val parent = results.getLong("parent")
                    val created =
                        new Date(results.getDouble("created").toLong * 1000L)
                    val modified =
                        new Date(results.getDouble("modified").toLong * 1000L)
                    val text = results.getString("text")
                    val author = results.getString("author")
                    val email = results.getString("email")
                    val website = results.getString("website")
                    comments += Comment(id, parent, created, modified, text,
                        author, email, website)
                }

                results.close()

                comments.result()
            }
        })
}
