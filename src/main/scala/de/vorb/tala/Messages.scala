package de.vorb.tala

import org.mashupbots.socko.events.HttpResponseMessage
import java.util.Date

sealed trait TalaMessage
sealed trait TalaRequest extends TalaMessage
sealed trait APIRequest extends TalaRequest

case class GetFile(response: HttpResponseMessage,
                   file: String) extends TalaRequest

case class GetComments(response: HttpResponseMessage,
                       document: Option[String],
                       quantity: Option[Int]) extends APIRequest

case class GetCommentCount(response: HttpResponseMessage,
                           document: Option[String]) extends APIRequest

case class PostComment(response: HttpResponseMessage,
                       document: String,
                       documentTitle: Option[String],
                       text: String,
                       author: Option[String],
                       email: Option[String],
                       website: Option[String],
                       subscription: Boolean) extends APIRequest

case class ReplaceComment(response: HttpResponseMessage,
                          document: String,
                          id: Long,
                          key: String) extends APIRequest

case class DeleteComment(response: HttpResponseMessage,
                         document: String,
                         id: Long,
                         key: String) extends APIRequest

case class GetMostCommentedDocuments(response: HttpResponseMessage,
                                     since: Option[Date],
                                     quantity: Option[Int]) extends APIRequest
