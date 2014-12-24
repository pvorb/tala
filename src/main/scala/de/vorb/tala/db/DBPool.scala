package de.vorb.tala.db

import java.sql.Connection

import com.mchange.v2.c3p0.ComboPooledDataSource

object DBPool {
    private val pool = new ComboPooledDataSource()
    pool.setDriverClass("org.sqlite.JDBC")
    pool.setJdbcUrl("jdbc:sqlite:/Users/Paul/Desktop/comments.db")

    pool.setMinPoolSize(2)
    pool.setAcquireIncrement(2)
    pool.setMaxPoolSize(10)
    pool.setMaxIdleTime(60)

    def getConnection: Connection = pool.getConnection

    def close(): Unit = pool.close()
}
