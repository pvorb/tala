package de.vorb.util

import java.util.Date
import scala.collection.immutable.HashSet
import scala.collection.mutable.Map
import scala.concurrent.duration.Duration

class Cache[K, V](slowFunction: K => V, val capacity: Int,
                  revalidateAfter: Duration) {

    private[util] val timeout: Long = revalidateAfter.toMillis
    private[util] val cache: Map[K, V] = Map.empty
    private[util] val cacheTime: Map[K, Date] = Map.empty
    private[util] var order: HashSet[K] = HashSet.empty

    def get(key: K): V = {
        cacheTime.get(key) match {
            case Some(past) =>
                val now = new Date()
                if (now.getTime - past.getTime > timeout) {
                    updateAndGet(key, now)
                } else {
                    cache.get(key).get
                }

            case None =>
                updateAndGet(key, new Date())
        }
    }

    def size: Int = {
        cache.size
    }

    def clear(): Unit = {
        cache.clear()
        cacheTime.clear()
        order = HashSet.empty
    }

    override def toString(): String = {
        cache.keys.mkString("Cache(", ", ", ")")
    }

    private def updateAndGet(key: K, date: Date): V = {
        manageCapacity(key)
        val value = slowFunction(key)

        // update cache
        cache(key) = value
        // update cacheTime
        cacheTime(key) = date
        // enqueue key
        order = order + key

        value
    }

    private def manageCapacity(key: K): Unit = {
        if (cache.size == capacity && order.nonEmpty) {
            // if the cache size equals the maximum capacity and the order is
            // non-empty, get the oldest stored key
            val oldestKey = order.head

            if (oldestKey != key && cache.isDefinedAt(oldestKey)) {
                cache -= oldestKey
                cacheTime -= oldestKey
                order = order.tail
            }
        }
    }
}
