package de.vorb.tala.cache

import de.vorb.util.Cache
import de.vorb.tala.db.DBPool

object Caches {
    val commentCountsCache: Cache[String, Long] =
        Cache((uri: String) => {
            val conn = DBPool.getConnection
            val stmt = conn.prepareStatement("""|SELECT COUNT(*) as count
                                                |FROM threads, comments
                                                |WHERE
                                                |  threads.uri = ? AND
                                                |  threads.id = comments.tid;
                                                |""".stripMargin)
            stmt.setQueryTimeout(30) // TODO configurable timeout?
            stmt.setString(1, uri)
            val results = stmt.executeQuery()

            // get the count from the result
            val count =
                if (results.next()) results.getLong(1)
                else -1L // invalid value

            results.close()

            count
        }, capacity = 128)
}
