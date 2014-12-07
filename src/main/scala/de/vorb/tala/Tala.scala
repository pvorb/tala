package de.vorb.tala

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

import spray.json.DefaultJsonProtocol._
import spray.json.pimpAny

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.routes.GET
import org.mashupbots.socko.routes.HttpRequest
import org.mashupbots.socko.routes.Path
import org.mashupbots.socko.routes.Routes
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

import de.vorb.tala.actors.APIHandler
import de.vorb.tala.actors.FileHandler
import de.vorb.tala.actors.Messages.GetCommentCount
import de.vorb.tala.actors.Messages.GetComments

/**
 * Tala comment server.
 *
 * @author Paul Vorbach
 */
object Tala extends Logger {
    val actorSystem = ActorSystem("TalaActorSystem")

    val routes = Routes({
        case HttpRequest(http) => http match {
            case GET(Path("/favicon.ico")) =>
                http.response.write(HttpResponseStatus.NOT_FOUND)

            case GET(Path("/api/comment")) =>
                val doc = http.request.endPoint.getQueryString("doc")

                if (doc.isDefined) {
                    // list comments for the given document
                    actorSystem.actorOf(Props[APIHandler]) !
                        GetComments(http.response, document = doc,
                            quantity = None)
                } else {
                    val qs = http.request.endPoint.getQueryString("quantity")

                    try {
                        // try to parse the GET param "quantity"
                        val qty = qs.getOrElse("5").toInt

                        // get the most recent comments of the whole site
                        actorSystem.actorOf(Props[APIHandler]) !
                            GetComments(http.response, document = None,
                                quantity = Some(qty))
                    } catch {
                        case e: NumberFormatException =>
                            // response with error message
                            http.response.write(HttpResponseStatus.BAD_REQUEST,
                                Map("message" -> s"Bad number format for query parameter 'quantity': ${qs.get}").toJson.compactPrint)
                    }
                }

            case GET(Path("/api/comment-count")) =>
                val doc = http.request.endPoint.getQueryString("doc")
                actorSystem.actorOf(Props[APIHandler]) !
                    GetCommentCount(http.response, doc)

            case Path(path) if path startsWith "/res/" =>
                actorSystem.actorOf(Props[FileHandler]) ! http
        }
    })

    def main(args: Array[String]): Unit = {
        val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
        webServer.start()

        Runtime.getRuntime.addShutdownHook(new Thread {
            override def run(): Unit = {
                webServer.stop()
            }
        })
    }
}
