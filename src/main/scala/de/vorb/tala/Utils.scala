package de.vorb.tala

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.TimeZone

import scala.util.Try

import org.json.simple.JSONValue
import org.mashupbots.socko.events.HttpResponseMessage

object Utils {
    val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val isoDateFormat: DateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val uriDateFormat: DateFormat =
        new SimpleDateFormat("yyyyMMdd")

    def dateToISO8601(date: Date): String = isoDateFormat.format(date)
    def parseISO8601(date: String): Try[Date] = Try(isoDateFormat.parse(date))

    def dateToUriFormat(date: Date): String = uriDateFormat.format(date)
    def parseUrlDate(date: String): Option[Date] = try {
        Some(uriDateFormat.parse(date))
    } catch {
        case _: Throwable => None
    }

    private val md5inst = MessageDigest.getInstance("MD5")
    def md5(msg: String): String = {
        val hashBytes = md5inst.digest(msg.getBytes(StandardCharsets.UTF_8))
        val hashInt = new BigInteger(1, hashBytes)
        String.format("%0"+(hashBytes.length << 1)+"x", hashInt)
    }

    private val sha1inst = MessageDigest.getInstance("SHA-1")
    def sha1(msg: String): String = {
        val hashBytes = sha1inst.digest(msg.getBytes(StandardCharsets.UTF_8))
        val hashInt = new BigInteger(1, hashBytes)
        String.format("%0"+(hashBytes.length << 1)+"X", hashInt)
    }

    def floatToDate(date: Double): Date = new Date(date.toLong * 1000L)
    def dateToFloat(date: Date): Double = date.getTime / 1000d
    def dateToFloat(date: Long): Double = date / 1000d

    def writeThrowable(resp: HttpResponseMessage, t: Throwable): Unit = {
        val wrapper = new HashMap[String, HashMap[String, Any]](1)

        val err = new HashMap[String, Any]
        err.put("code", resp.status.code)
        err.put("type", t.getClass.getName)
        err.put("message", t.getMessage)

        wrapper.put("error", err)

        resp.contentType = "application/json"
        resp.write(JSONValue.toJSONString(wrapper))
    }
}
