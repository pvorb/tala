package de.vorb.tala

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import akka.actor.ActorSystem

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

import org.scalatest.FlatSpec

class TalaSpec extends FlatSpec {
    implicit val system = ActorSystem("tala-spec")

    override def withFixture(test: NoArgTest) = {
        val single = Executors.newSingleThreadExecutor()
        single.submit(new Runnable {
            def run = Tala.main(Array[String]())
        })

        try test.apply()
        finally single.shutdown()
    }

    "Tala" should "should accept a new valid comment" in { () =>
        val client = HttpClients.createDefault()
        val req = new HttpPost("http://localhost:8888/api/comments")
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
        val resp = client.execute(req)
        println(resp)
        assert(resp.getStatusLine.getStatusCode == 200, "response status code")
    }

    it should "should refuse an invalid comment" in { () =>
        // create an invalid test request with socko
    }
}
