package de.vorb.tala.actors

import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import scala.compat.Platform

import akka.actor.Actor

import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.json.simple.parser.JSONParser
import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.events.HttpResponseMessage
import org.mashupbots.socko.events.HttpResponseStatus

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

import de.vorb.tala.Utils
import de.vorb.tala.actors.Messages.GetComments
import de.vorb.tala.actors.Messages.ListComments
import de.vorb.tala.actors.Messages.PostComment
import de.vorb.tala.db.DBPool
import de.vorb.tala.model.Comment

class CommentHandler extends Actor {
    def receive = {
        case GetComments(http, uri) => getComments(http.response, uri)
        case ListComments(http) => getComments(http.response, "")
        case PostComment(http, uri) => postComment(http, uri)
    }

    def getComments(resp: HttpResponseMessage, uri: String): Unit = {
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

    def postComment(http: HttpRequestEvent, uri: String): Unit = {
        try {
            // parse the comment
            val jsonStr = http.request.content.toString(StandardCharsets.UTF_8)
            val jsonObj = new JSONParser().parse(jsonStr)
                .asInstanceOf[JSONObject]

            // add the corresponding thread
            val conn = DBPool.getConnection
            val createThread = conn.prepareStatement(
                """|INSERT IGNORE INTO threads
                   |  (uri, title)
                   |VALUES
                   |  (?, ?);""".stripMargin)
            createThread.setString(1, uri)
            createThread.setString(2,
                jsonObj.get("threadTitle").asInstanceOf[String])
            createThread.execute()

            // get the id of the inserted thread
            val tid = {
                val keys = createThread.getGeneratedKeys
                if (keys.next()) {
                    // if a new entry was made, use the new id
                    keys.getLong("id")
                } else {
                    // otherwise do a SELECT to get the id of the thread
                    val getThreadId = conn.prepareStatement(
                        "SELECT id FROM threads WHERE uri = ?;")
                    getThreadId.setString(1, uri)
                    val result = getThreadId.executeQuery()
                    if (!result.next()) {
                        throw new Exception("race condition")
                    }
                    result.getLong("id")
                }
            }

            // create the new comment
            val createComment = conn.prepareStatement(
                """|INSERT INTO comments
                   |  (tid, parent, created, modified, mode, remote_addr,
                   |    text, author, email, website)
                   |VALUES
                   |  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""".stripMargin)

            createComment.setLong(1, tid)
            createComment.setLong(2, jsonObj.get("parent").asInstanceOf[Long])
            createComment.setDouble(3, Utils.dateToFloat(Platform.currentTime))
            createComment.setDouble(4, 0d)
            createComment.setInt(5, 1)
            createComment.setString(6,
                jsonObj.get("remoteAddress").asInstanceOf[String])
            createComment.setString(7,
                jsonObj.get("text").asInstanceOf[String])
            createComment.setString(8,
                jsonObj.get("author").asInstanceOf[String])
            createComment.setString(9,
                jsonObj.get("email").asInstanceOf[String])
            createComment.setString(10,
                jsonObj.get("website").asInstanceOf[String])

            createComment.execute()
        } catch {
            case _: Throwable =>
                http.response.write(HttpResponseStatus.BAD_REQUEST)
        } finally {
            context.stop(self)
        }
    }

    def sanitizeComment(obj: JSONObject): Boolean = {
        val text = obj.get("text").asInstanceOf[String]

        val author = obj.get("author").asInstanceOf[String]
        val email = obj.get("email").asInstanceOf[String]

        true
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
                               |  threads.id = comments.tid AND
                               |  comments.mode = 1
                               |ORDER BY
                               |  comments.created ASC;""".stripMargin)
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
                               |LIMIT 10;""".stripMargin)
                        stmt
                    }

                stmt.setQueryTimeout(30)
                val results = stmt.executeQuery()

                val comments = new ArrayList[Comment]()
                while (results.next()) {
                    val id = results.getLong("id")
                    val parent = results.getLong("parent")
                    val created =
                        Utils.floatToDate(results.getDouble("created"))
                    val modified =
                        Utils.floatToDate(results.getDouble("modified"))
                    val text = results.getString("text")
                    val author = results.getString("author")
                    val email = results.getString("email")
                    val website = results.getString("website")
                    comments.add(Comment(Some(id), parent, created, modified,
                        text, author, email, website))
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
