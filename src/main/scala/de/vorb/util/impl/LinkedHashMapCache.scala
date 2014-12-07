package de.vorb.util.impl

import java.util.Date
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.Map
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import de.vorb.util.Cache

/**
 * An implementation of `Cache` using a `LinkedHashMap`.
 *
 * @constructor create a new cache with a capacity and a re-validation timeout.
 *
 * @param map map function from key to value
 * @param capacity maximum capacity of the cache
 * @param revalidationTimeout maximum duration a key
 */
private[util] class LinkedHashMapCache[K, V](map: K => V,
                                             override val capacity: Int,
                                             revalitationTimeout: Duration)
        extends Cache[K, V] {
    require(capacity > 0, "capacity <= 0")
    require(revalitationTimeout.isFinite(), "non-finite revalidation time")

    private val timeout: Long = revalitationTimeout.toMillis
    private val cache: LinkedHashMap[K, V] = LinkedHashMap.empty
    private val cacheTime: Map[K, Date] = Map.empty

    /**
     * Get a (possibly cached) value.
     *
     * @param key key for the requested value
     */
    def get(key: K): V = {
        if (cache.isDefinedAt(key)) {
            // if the value is defined already

            val now = new Date()
            if (now.getTime - cacheTime(key).getTime > timeout) {
                // if the value timed out, re-calculate it
                cache -= key
                val value = map(key)
                cache(key) = value
                cacheTime(key) = now

                value
            } else {
                // otherwise simply add it to the end of the cache again
                val value = cache(key)
                cache -= key
                cache(key) = value

                value
            }
        } else if (cache.size < capacity) {
            // if the cache still has remaining capacity, calculate the value
            val value = map(key)
            // and store it
            cache(key) = value
            cacheTime(key) = new Date()
            value
        } else {
            // if the cache size equals the maximum capacity and the order is
            // non-empty, get the oldest stored key
            val (k, v) = cache.head

            cache -= k // remove k from cache
            val value = map(key)

            // update cache
            cache(key) = value
            // update cacheTime
            cacheTime(key) = new Date()

            value
        }
    }

    /**
     * Number of cached values.
     */
    def size: Int = {
        cache.size
    }

    /**
     * Clear this cache so it will be empty.
     */
    def clear(): Unit = {
        cache.clear()
        cacheTime.clear()
    }

    /**
     * Get a string representation of this cache's keys.
     */
    override def toString(): String = {
        cache.mkString("Cache(", ", ", ")")
    }
}
