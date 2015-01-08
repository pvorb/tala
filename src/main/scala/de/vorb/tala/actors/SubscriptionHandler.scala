package de.vorb.tala.actors

import java.net.URLEncoder
import java.util.Date
import scala.compat.Platform
import akka.actor.Actor
import org.mashupbots.socko.events.HttpResponseStatus
import de.vorb.tala.Utils
import de.vorb.tala.actors.Messages.Subscribe
import de.vorb.tala.actors.Messages.Unsubscribe
import scala.util.Try
import de.vorb.tala.db.DBPool
import scala.util.Success
import scala.util.Failure
import java.sql.Connection

class SubscriptionHandler extends Actor {
    val secret: String = """aePsBpM Pyj{~Lc$)otl:]W5v|L'wO,\"""
    val expirationOffset: Int = 7 * 24 * 60 * 60 * 1000

    def receive = {
        case Subscribe(http, uri, email) =>
            val dbConn = DBPool.getConnection
            // get the corresponding thread id
            getThreadID(dbConn, uri) match {
                case Success(threadId) =>
                    createSubscription(dbConn, threadId, email) match {
                        case Success(_) =>
                            http.response.write(HttpResponseStatus.CREATED)
                        case Failure(t) =>
                            Utils.writeThrowable(http.response, t)
                    }
                case Failure(t) =>
                    Utils.writeThrowable(http.response, t)
            }
            context.stop(self)
        case Unsubscribe(http, uri, email, expiration, signature) =>
            Utils.parseUrlDate(expiration) match {
                case Some(expirationDate) =>
                    if (expirationDate.after(new Date()) && signature ==
                        Utils.sha1(s"$email/$expiration/$secret")) {
                        http.response.write("unsubscribed")
                    } else {
                        val u = URLEncoder.encode(uri, "UTF-8")
                        val e1 = URLEncoder.encode(email, "UTF-8")
                        val e2 = Utils.dateToUriFormat(
                            new Date(Platform.currentTime + expirationOffset))
                        val s = Utils.sha1(s"$email/$e2/$secret")
                        http.response.write(s"""http://localhost:8888/api/unsubscribe?uri=$u&email=$e1&exp=$e2&sig=$s""")
                    }
                case None =>
                    http.response.write(HttpResponseStatus.BAD_REQUEST)
            }

            context.stop(self)
    }

    def getThreadID(dbConn: Connection, uri: String): Try[Long] = {
        val stmt = dbConn.prepareStatement(
            """|SELECT id
               |FROM threads
               |WHERE uri = ?;""".stripMargin)
        stmt.setString(1, uri)
        val getThreadIDResults = stmt.executeQuery()

        if (getThreadIDResults.next()) {
            Success(getThreadIDResults.getLong("id"))
        } else {
            Failure(new Exception("unknown thread"))
        }
    }

    def createSubscription(dbConn: Connection, threadId: Long,
                           email: String): Try[Unit] = try {
        val stmt = dbConn.prepareStatement(
            """|INSERT INTO subscriptions
               |  (tid, email)
               |VALUES
               |  (?, ?);""".stripMargin)
        stmt.setLong(1, threadId)
        stmt.setString(2, email)

        stmt.executeUpdate()

        Success()
    } catch {
        case t: Throwable => Failure(t)
    }
}
