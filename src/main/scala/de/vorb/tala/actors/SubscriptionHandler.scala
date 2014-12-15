package de.vorb.tala.actors

import akka.actor.Actor

import de.vorb.tala.actors.Messages.Subscribe
import de.vorb.tala.actors.Messages.Unsubscribe

class SubscriptionHandler extends Actor {
    def receive = {
        case Subscribe(http, uri, email) =>
            context.stop(self)
        case Unsubscribe(http, uri, email, expiration, signature) =>
            context.stop(self)
    }
}
