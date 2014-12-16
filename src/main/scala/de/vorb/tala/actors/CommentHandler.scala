package de.vorb.tala.actors

import java.util.ArrayList

import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import akka.actor.Actor

import org.json.simple.JSONValue
import org.mashupbots.socko.events.HttpResponseMessage
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
        case GetComments(http, uri) => respond(http.response, uri)
        case ListComments(http) => respond(http.response, "")
    }

    def respond(resp: HttpResponseMessage, uri: String): Unit = {
        try {
            resp.contentType = "application/json"
            resp.write(CommentHandler.cache.get(uri))
        } catch {
            case _: ExecutionException =>
                resp.write(HttpResponseStatus.BAD_REQUEST)
        } finally {
            context.stop(self)
        }
    }
}

object CommentHandler {
    val cache: LoadingCache[String, String] = CacheBuilder
        .newBuilder()
        .maximumSize(32)
        .expireAfterAccess(10, TimeUnit.DAYS)
        .build(new CacheLoader[String, String] {
            override def load(uri: String): String = {
                val conn = DBPool.getConnection
                val stmt =
                    if (uri.length > 0) {
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

                stmt.setQueryTimeout(30)
                val results = stmt.executeQuery()

                val comments = new ArrayList[Comment]()
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
                    comments.add(Comment(id, parent, created, modified, text,
                        author, email, website))
                }

                results.close()

                wrapListInObject("comments", comments)
            }
        })

    def wrapListInObject(name: String, list: List[Comment]): String = {
        val obj = new HashMap[String, AnyRef](1)
        obj.put(name, list)
        JSONValue.toJSONString(obj)
    }
}
