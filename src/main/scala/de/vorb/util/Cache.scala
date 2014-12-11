package de.vorb.util

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

import de.vorb.util.impl.LinkedHashMapCache

/**
 * A cache.
 *
 * @constructor create a new cache with a capacity and a re-validation timeout.
 *
 * @param map map function from key to value
 * @param capacity maximum capacity of the cache
 * @param revalidationTimeout maximum duration a key
 */
trait Cache[K, V] {
    /**
     * Get a (possibly cached) value.
     *
     * @param key key for the requested value
     */
    def get(key: K): V

    /**
     * Number of cached values.
     */
    def size: Int

    /**
     * Maximum capacity of the cache.
     */
    def capacity: Int

    /**
     * Clear this cache so it will be empty.
     */
    def clear(): Unit

    /**
     * Get a string representation of this cache's keys.
     */
    override def toString(): String
}

object Cache {
    def apply[K, V](map: K => V, capacity: Int = 1024,
                    revalidationTimeout: Duration = 10.minutes): Cache[K, V] = {
        new LinkedHashMapCache[K, V](map, capacity, revalidationTimeout)
    }
}
