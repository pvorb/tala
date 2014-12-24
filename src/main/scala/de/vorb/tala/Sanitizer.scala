package de.vorb.tala

import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import scala.util.Failure
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import org.json.simple.JSONObject
import org.owasp.encoder.Encode
import org.owasp.html.HtmlPolicyBuilder
import de.vorb.tala.model.CommentResult
import de.vorb.tala.model.CommentRequest

object Sanitizer {
    val textPolicy = new HtmlPolicyBuilder().allowElements("br", "p", "pre",
        "code", "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6",
        "blockquote", "hr", "a", "abbr", "em", "strong")
        .allowAttributes("title").globally()
        .allowAttributes("href").onElements("a")
        .allowAttributes("src", "alt").onElements("img")
        .allowAttributes("lang").globally()
        .allowStandardUrlProtocols()
        .toFactory()

    def sanitizeComment(obj: JSONObject): Try[CommentRequest] = try {
        val parent =
            obj.get("parent").asInstanceOf[Long]

        // parse date
        Utils.parseISO8601(obj.get("created").asInstanceOf[String]) match {
            case Success(created) =>
                // sanitize text with text policy
                val text =
                    textPolicy.sanitize(obj.get("text").asInstanceOf[String])
                val author =
                    Encode.forHtmlContent(obj.get("author").asInstanceOf[String])
                val email =
                    obj.get("email").asInstanceOf[String]
                val website = try {
                    new URL(obj.get("website").asInstanceOf[String]).toString()
                } catch {
                    case _: MalformedURLException => null
                    case _: URISyntaxException => null
                }
                val remoteAddr = obj.get("remoteAddress").asInstanceOf[String]

                Success(CommentRequest(parent, created, text, author, email,
                    website, remoteAddr))
            case Failure(throwable) => Failure(throwable)
        }
    } catch {
        case throwable: Throwable => Failure(throwable)
    }
}
