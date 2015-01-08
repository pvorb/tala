package de.vorb.tala.db

import scala.util.Try
import scala.util.Failure
import scala.util.Success

object DBUtils {
    def createTables(): Try[Unit] = try {
        val conn = DBPool.getConnection

        val threads = conn.prepareStatement(
            """|CREATE TABLE IF NOT EXISTS threads
               |  (id INTEGER PRIMARY KEY, uri STRING NOT NULL, title STRING);
               |""".stripMargin)
        threads.execute()

        val comments = conn.prepareStatement(
            """|CREATE TABLE IF NOT EXISTS comments
               |  (id INTEGER PRIMARY KEY, tid REFERENCES threads(id),
               |    parent INTEGER, created FLOAT NOT NULL,
               |    modified FLOAT NOT NULL, public INTEGER NOT NULL,
               |    text VARCHAR NOT NULL, author VARCHAR, email_hash VARCHAR,
               |    website VARCHAR);""".stripMargin)
        comments.execute()

        val subscriptions = conn.prepareStatement(
            """|CREATE TABLE IF NOT EXISTS subscriptions
               |  (tid REFERENCES threads(id), email STRING NOT NULL);
               |""".stripMargin)
        subscriptions.execute()

        Success()
    } catch {
        case t: Throwable => Failure(t)
    }
}
