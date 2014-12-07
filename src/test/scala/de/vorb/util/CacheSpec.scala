package de.vorb.util

import scala.concurrent.duration.DurationInt

import org.scalatest.FlatSpec

class CacheSpec extends FlatSpec {
    // key value function
    def map(k: String): Int = k.hashCode

    "A cache" should "deliver the same values as its given map function" in {
        val cache = Cache(map, capacity = 2)

        assert(cache.get("foo") == map("foo"))
        assert(cache.get("bar") == map("bar"))
        assert(cache.get("baz") == map("baz"))
    }

    it should "respect its capacity" in {
        val cache = Cache(map, capacity = 2)
        assert(cache.size == 0)

        cache.get("foo")
        assert(cache.size == 1)

        cache.get("bar")
        cache.get("baz")
        assert(cache.size == 2)

        cache.get("baz")
        assert(cache.size == 2)
    }

    it should "get its values from cache if possible" in {
        var counter = 0
        def sideEffectedMap(k: String): Int = {
            counter += 1
            k.hashCode
        }

        val cache = Cache(sideEffectedMap, capacity = 2)

        cache.get("foo")
        assert(counter == 1)
        cache.get("foo")
        assert(counter == 1)
        cache.get("foo")
        assert(counter == 1)
        cache.get("bar")
        assert(counter == 2)
        cache.get("bar")
        assert(counter == 2)
        cache.get("baz")
        assert(counter == 3)
        cache.get("baz")
        assert(counter == 3)
        cache.get("bar")
        assert(counter == 3)
        cache.get("foo")
        assert(counter == 4)
    }

    it should "re-validate its values after timeout" in {
        var counter = 0
        def sideEffectedMap(k: String): Int = {
            counter += 1
            k.hashCode
        }

        val cache = Cache(sideEffectedMap, capacity = 2, 50.milliseconds)

        cache.get("foo")
        assert(counter == 1)
        cache.get("foo")
        assert(counter == 1)
        Thread.sleep(50)
        cache.get("foo")
        assert(counter == 2)
    }
}
