package de.vorb.tala.model

import java.security.MessageDigest
import java.util.Date
import java.util.LinkedHashMap
import org.json.simple.JSONValue
import de.vorb.tala.Utils
import java.nio.charset.StandardCharsets
import java.math.BigInteger

case class Comment(id: Long, parent: Long, created: Date, modified: Date,
                   text: String, author: String, email: String,
                   website: String) {
    override def toString: String = {
        val obj = new LinkedHashMap[String, Any]
        obj.put("id", id)
        obj.put("parent", parent)
        obj.put("created", Utils.dateToISO8601(created))
        if (created.before(modified))
            obj.put("modified", Utils.dateToISO8601(modified))
        obj.put("text", text)
        if (author != null)
            obj.put("author", author)
        if (email != null)
            obj.put("mailhash", Utils.md5(email))
        if (website != null)
            obj.put("website", website)
        JSONValue.toJSONString(obj)
    }
}
