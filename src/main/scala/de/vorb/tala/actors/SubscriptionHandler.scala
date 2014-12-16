package de.vorb.tala.actors

import java.net.URLEncoder
import java.util.Date

import scala.compat.Platform

import akka.actor.Actor

import org.mashupbots.socko.events.HttpResponseStatus

import de.vorb.tala.Utils
import de.vorb.tala.actors.Messages.Subscribe
import de.vorb.tala.actors.Messages.Unsubscribe

class SubscriptionHandler extends Actor {
    val secret: String = """aePsBpM Pyj{~Lc$)otl:]W5v|L'wO,\"""

    def receive = {
        case Subscribe(http, uri, email) =>
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
                        val e2 = Utils.dateToUriFormat(new Date(Platform.currentTime + 7 * 24 * 60 * 60 * 1000))
                        val s = Utils.sha1(s"$email/$e2/$secret")
                        http.response.write(s"""http://localhost:8888/api/unsubscribe?uri=$u&email=$e1&exp=$e2&sig=$s""")
                    }
                case None =>
                    http.response.write(HttpResponseStatus.BAD_REQUEST)
            }

            context.stop(self)
    }
}
