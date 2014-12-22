package de.vorb.tala.model

import java.util.Date
import java.util.LinkedHashMap

import org.json.simple.JSONValue

import de.vorb.tala.Utils

case class CommentResult(id: Long, parent: Long, created: Date, modified: Date,
                         text: String, author: Option[String],
                         email: Option[String], website: Option[String]) {

    override def toString: String = {
        val obj = new LinkedHashMap[String, Any]
        obj.put("id", id)
        obj.put("parent", parent)
        obj.put("created", Utils.dateToISO8601(created))
        if (created.before(modified))
            obj.put("modified", Utils.dateToISO8601(modified))
        obj.put("text", text)
        if (author.isDefined)
            obj.put("author", author.get)
        if (email.isDefined)
            obj.put("mailhash", Utils.md5(email.get))
        if (website.isDefined)
            obj.put("website", website.get)
        JSONValue.toJSONString(obj)
    }
}
