package de.vorb.tala.actors

import akka.actor.Actor

import org.mashupbots.socko.events.HttpRequestEvent

class FileHandler extends Actor {
    def receive = {
        case event: HttpRequestEvent =>
            event.response.write("I serve files")
            context.stop(self)
    }
}
