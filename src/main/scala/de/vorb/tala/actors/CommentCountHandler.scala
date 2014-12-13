package de.vorb.tala.actors

import akka.actor.Actor
import de.vorb.tala.actors.Messages.GetCommentCount
import de.vorb.tala.cache.Caches
import org.mashupbots.socko.events.HttpResponseStatus
import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import java.util.concurrent.TimeUnit
import de.vorb.tala.db.DBPool
import com.google.common.cache.CacheLoader

import java.lang.Long
import java.util.Date
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.DurationInt

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

import de.vorb.tala.db.DBPool
import de.vorb.tala.model.Comment

class CommentCountHandler extends Actor {
    def receive = {
        case GetCommentCount(http, Some(uri)) =>
            CommentCountHandler.cache.get(uri) match {
                case count if count >= 0L =>
                    http.response.write(s"{count:$count}", "application/json")
                case count if count < 0L =>
                    http.response.status = HttpResponseStatus.INTERNAL_SERVER_ERROR
                    http.response.write("{error:\"db problem\"}", "application/json")
            }
            context.stop(self)

        case GetCommentCount(http, None) =>
            http.response.write(s"There are x comments.")
            context.stop(self)
    }
}

object CommentCountHandler {
    val cache: LoadingCache[String, Long] = CacheBuilder
        .newBuilder()
        .maximumSize(128)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader[String, Long] {
            override def load(uri: String): Long = {
                val conn = DBPool.getConnection
                val stmt = conn.prepareStatement(
                    """|SELECT COUNT(*)
                           |FROM threads, comments
                           |WHERE
                           |  threads.uri = ? AND
                           |  threads.id = comments.tid;
                           |""".stripMargin)
                stmt.setQueryTimeout(30) // TODO configurable timeout?
                stmt.setString(1, uri)
                val results = stmt.executeQuery()

                // get the count from the result
                val count =
                    if (results.next())
                        results.getLong(1)
                    else
                        -1L // invalid value

                results.close()

                count
            }
        })
}
