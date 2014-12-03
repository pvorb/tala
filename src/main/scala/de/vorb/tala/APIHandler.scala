package de.vorb.tala

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes.HttpRequest

import akka.actor.Actor

class APIHandler extends Actor {
    def receive = {
        case GetComments(resp, Some(document), _) =>
            resp.write(s"comments for $document.")
            context.stop(self)

        case GetComments(resp, _, Some(quantity)) =>
            resp.write(s"$quantity newest comments.")
            context.stop(self)

        case GetCommentCount(resp, Some(document)) =>
            resp.write(s"$document has x comments.")
            context.stop(self)

        case GetCommentCount(resp, None) =>
            resp.write(s"There are x comments.")
            context.stop(self)
    }
}
