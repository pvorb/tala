package de.vorb.tala

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

import de.vorb.tala.actors._
import de.vorb.tala.actors.Messages._

/**
 * Tala comment server.
 *
 * @author Paul Vorbach
 */
object Tala extends Logger {
    val actorSystem = ActorSystem("Tala")

    object UriParam extends QueryStringField("uri")
    object IdParam extends QueryStringField("id")
    object KeyParam extends QueryStringField("key")
    object EmailParam extends QueryStringField("email")
    object ExpirationParam extends QueryStringField("exp")
    object SignatureParam extends QueryStringField("sig")

    val routes = Routes({
        case HttpRequest(http) => http match {
            case GET(Path("/favicon.ico")) =>
                // no favicon available
                http.response.write(HttpResponseStatus.NOT_FOUND)

            case GET(Path("/api/comment") & UriParam(uri)) =>
                // get comments for the given document
                actorSystem.actorOf(Props[CommentHandler]) !
                    GetComments(http, uri)

            case GET(Path("/api/comment")) =>
                // list all comments (with a limit)
                actorSystem.actorOf(Props[CommentHandler]) !
                    ListComments(http)

            case POST(Path("/api/comment")) & UriParam(uri) =>
                actorSystem.actorOf(Props[CommentHandler]) !
                    PostComment(http, uri)

            case PUT(PathSegments("api" :: "comment" :: id :: Nil))
                & KeyParam(key) =>
                actorSystem.actorOf(Props[CommentHandler]) !
                    ReplaceComment(http, id.toLong, key)

            case GET(Path("/api/comment-count")) & UriParam(uri) =>
                actorSystem.actorOf(Props[CommentCountHandler]) !
                    GetCommentCount(http, Some(uri))

            case GET(Path("/api/subscribe") & UriParam(uri) &
                EmailParam(email)) =>
                actorSystem.actorOf(Props[SubscriptionHandler]) !
                    Subscribe(http, uri, email)

            case GET(Path("/api/unsubscribe") & UriParam(uri) &
                EmailParam(email) & ExpirationParam(expiration) &
                SignatureParam(signature)) =>
                actorSystem.actorOf(Props[SubscriptionHandler]) !
                    Unsubscribe(http, uri, email, expiration, signature)

            case GET(Path(path)) if path startsWith "/res/" =>
                actorSystem.actorOf(Props[FileHandler]) ! GetFile(http, path)

            case _ =>
                http.response.status = HttpResponseStatus.BAD_REQUEST
                Utils.writeThrowable(http.response, new IllegalRequestException)
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
