package de.vorb.tala.actors

import java.util.Date

import org.mashupbots.socko.events.HttpResponseMessage

object Messages {
    sealed trait TalaMessage
    sealed trait TalaRequest extends TalaMessage
    sealed trait APIRequest extends TalaRequest

    case class GetFile(response: HttpResponseMessage,
                       file: String) extends TalaRequest

    case class GetComments(response: HttpResponseMessage,
                           uri: Option[String],
                           quantity: Option[Int]) extends APIRequest

    case class GetCommentCount(response: HttpResponseMessage,
                               uri: Option[String]) extends APIRequest

    case class PostComment(response: HttpResponseMessage,
                           uri: String,
                           documentTitle: Option[String],
                           text: String,
                           author: Option[String],
                           email: Option[String],
                           website: Option[String],
                           subscription: Boolean) extends APIRequest

    case class ReplaceComment(response: HttpResponseMessage,
                              uri: String,
                              id: Long,
                              key: String) extends APIRequest

    case class DeleteComment(response: HttpResponseMessage,
                             uri: String,
                             id: Long,
                             key: String) extends APIRequest

    case class GetMostCommentedDocuments(response: HttpResponseMessage,
                                         since: Option[Date],
                                         quantity: Option[Int]) extends APIRequest
}
