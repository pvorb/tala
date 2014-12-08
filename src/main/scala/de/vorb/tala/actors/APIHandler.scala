package de.vorb.tala.actors

import akka.actor.Actor
import de.vorb.tala.actors.Messages.GetCommentCount
import de.vorb.tala.actors.Messages.GetComments
import org.mashupbots.socko.events.HttpResponseStatus
import de.vorb.tala.db.DBPool
import de.vorb.tala.cache.Caches
import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

class APIHandler extends Actor {
    def receive = {
        case GetComments(resp, Some(uri), _) =>
            resp.write(s"comments for $uri.")
            context.stop(self)

        case GetComments(resp, _, Some(quantity)) =>
            resp.write(s"$quantity newest comments.")
            context.stop(self)

        case GetCommentCount(resp, Some(uri)) =>
            Caches.commentCountsCache.get(uri) match {
                case Some(count) =>
                    resp.write(s"{count:$count}", "application/json")
                case None =>
                    resp.status = HttpResponseStatus.INTERNAL_SERVER_ERROR
                    resp.write("{error:\"db problem\"}", "application/json")
            }

            println(Caches.commentCountsCache)

            context.stop(self)

        case GetCommentCount(resp, None) =>
            resp.write(s"There are x comments.")
            context.stop(self)
    }
}
