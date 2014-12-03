package de.vorb.util

import scala.concurrent.duration.DurationInt
import org.scalatest.FlatSpec

class CacheSpec extends FlatSpec {
    // key value function
    def kv(k: String): Int = k.hashCode

    "A cache" should "deliver its items" in {
        val cache = new Cache(kv, capacity = 2, revalidateAfter = 2.minutes)

        assert(cache.get("foo") == kv("foo"))
        assert(cache.get("bar") == kv("bar"))
        assert(cache.get("baz") == kv("baz"))
    }

    "A cache" should "respect its capacity" in {
        val cache = new Cache(kv, capacity = 2, revalidateAfter = 2.minutes)

        cache.get("foo")
        cache.get("bar")
        cache.get("baz")

        assert(cache.size == 2)
    }

    "A cache" should "cache its last values" in {
        val cache = new Cache(kv, capacity = 2, revalidateAfter = 2.minutes)

        cache.get("foo")
        cache.get("foo")
        cache.get("foo")
        cache.get("bar")
        cache.get("bar")
        cache.get("baz")

        assert(cache.cache.keySet == Set("bar", "baz"))
    }
}
