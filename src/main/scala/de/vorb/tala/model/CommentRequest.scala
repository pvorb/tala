package de.vorb.tala.model

import java.util.Date

case class CommentRequest(parent: Long, text: String, author: String,
                          email: String, website: String, remoteAddress: String)
