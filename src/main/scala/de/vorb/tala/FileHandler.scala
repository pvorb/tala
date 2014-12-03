package de.vorb.tala

import java.util.Date

import org.mashupbots.socko.events.HttpRequestEvent

import akka.actor.Actor

class FileHandler extends Actor {
    def receive = {
        case event: HttpRequestEvent =>
            event.response.write("I serve files")
            context.stop(self)
    }
}
