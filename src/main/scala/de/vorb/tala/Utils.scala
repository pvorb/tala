package de.vorb.tala

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import java.math.BigInteger
import java.text.ParseException

object Utils {
    val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val f: DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    def dateToISO8601(date: Date): String = f.format(date)
    def parseISO8601(date: String): Option[Date] = try {
        Some(f.parse(date))
    } catch {
        case e: ParseException =>
            None
    }

    private val md5inst = MessageDigest.getInstance("MD5")
    def md5(msg: String): String = {
        val hashBytes = md5inst.digest(msg.getBytes(StandardCharsets.UTF_8))
        val hashInt = new BigInteger(1, hashBytes)
        String.format("%0"+(hashBytes.length << 1)+"x", hashInt)
    }

    private val sha256inst = MessageDigest.getInstance("SHA-256")
    def sha256(msg: String): String = {
        val hashBytes = sha256inst.digest(msg.getBytes(StandardCharsets.UTF_8))
        val hashInt = new BigInteger(1, hashBytes)
        String.format("%0"+(hashBytes.length << 1)+"X", hashInt)
    }
}
