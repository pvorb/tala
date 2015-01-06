package de.vorb.tala

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import akka.actor.ActorSystem

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TalaSpec extends FlatSpec {
    implicit val system = ActorSystem("tala-spec")

    override def withFixture(test: NoArgTest) = {
        val pool = Executors.newSingleThreadExecutor()
        pool.execute(new Runnable {
            def run = Tala.main(Array[String]())
        })

        try super.withFixture(test)
        finally pool.shutdown()
    }

    "/test.html" should "have more than 0 comments" in {
        val client = HttpClients.createDefault()
        val req = new HttpGet("http://localhost:8888/api/comment?uri=/test.html")

        val resp = client.execute(req)

        val comments = new JSONParser().parse(new BufferedReader(
            new InputStreamReader(resp.getEntity.getContent,
                StandardCharsets.UTF_8)))

        assert(comments.asInstanceOf[JSONObject].get("comments")
            .asInstanceOf[JSONArray].size > 0)
    }

    it should "accept a new valid comment" in {
        val client = HttpClients.createDefault()
        val req = new HttpPost("http://localhost:8888/api/comment?uri=/test.html")
        req.setEntity(new StringEntity(
            """|{
               |  "parent": -1,
               |  "text": "<p>Hello, World!</p>",
               |  "author": "John Doe",
               |  "email": "jd@example.com",
               |  "website": "http://example.com/",
               |  "threadTitle": "Foo"
               |}""".stripMargin,
            ContentType.create("application/json", StandardCharsets.UTF_8)))
        try {
            val resp = client.execute(req)
            assert(resp.getStatusLine.getStatusCode == 201, "response status code")
        } catch {
            case t: Throwable => t.printStackTrace()
        }
    }

    //
    //    it should "refuse an invalid comment" in {
    //        // create an invalid test request with socko
    //    }
}
