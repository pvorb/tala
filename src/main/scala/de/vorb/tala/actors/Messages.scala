package de.vorb.tala.actors

import java.util.Date

import org.mashupbots.socko.events.HttpRequestEvent

object Messages {
    sealed trait TalaMessage
    sealed trait TalaRequest extends TalaMessage
    sealed trait APIRequest extends TalaRequest

    case class GetFile(http: HttpRequestEvent,
                       file: String) extends TalaRequest

    case class GetComments(http: HttpRequestEvent,
                           uri: String) extends APIRequest

    case class ListComments(http: HttpRequestEvent) extends APIRequest

    case class GetCommentCount(http: HttpRequestEvent,
                               uri: Option[String]) extends APIRequest

    case class PostComment(http: HttpRequestEvent,
                           uri: String) extends APIRequest

    case class ReplaceComment(http: HttpRequestEvent,
                              id: Long,
                              key: String) extends APIRequest

    case class DeleteComment(http: HttpRequestEvent,
                             uri: String,
                             id: Long,
                             key: String) extends APIRequest

    case class GetMostCommentedDocuments(http: HttpRequestEvent,
                                         since: Option[Date],
                                         quantity: Option[Int]) extends APIRequest

    case class Subscribe(http: HttpRequestEvent,
                         uri: String,
                         email: String) extends APIRequest

    case class Unsubscribe(http: HttpRequestEvent,
                           uri: String,
                           email: String,
                           expiration: String,
                           signature: String) extends APIRequest
}
