package com.nyora.hasan72341.shared.extension

import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListedMangaCacheTest {

    @Test
    fun returnsWhatWasStoredForAUrl() {
        val cache = ListedMangaCache<String>(maxSize = 10)
        cache.put("/comics/a", "entry-a")

        assertEquals("entry-a", cache.get("/comics/a"))
    }

    @Test
    fun returnsNothingForAnUnseenUrl() {
        val cache = ListedMangaCache<String>(maxSize = 10)

        assertNull(cache.get("/comics/never-listed"))
    }

    @Test
    fun theLatestListingOfAUrlWins() {
        val cache = ListedMangaCache<String>(maxSize = 10)
        cache.put("/comics/a", "old")
        cache.put("/comics/a", "new")

        assertEquals("new", cache.get("/comics/a"))
    }

    @Test
    fun dropsTheLeastRecentlyUsedEntryPastMaxSize() {
        val cache = ListedMangaCache<String>(maxSize = 2)
        cache.put("/a", "a")
        cache.put("/b", "b")
        cache.put("/c", "c")

        assertNull(cache.get("/a"))
        assertEquals("b", cache.get("/b"))
        assertEquals("c", cache.get("/c"))
    }

    @Test
    fun readingAnEntryKeepsItFromBeingEvicted() {
        val cache = ListedMangaCache<String>(maxSize = 2)
        cache.put("/a", "a")
        cache.put("/b", "b")
        cache.get("/a")          // /a is now the most recently used
        cache.put("/c", "c")     // evicts /b, not /a

        assertEquals("a", cache.get("/a"))
        assertNull(cache.get("/b"))
    }

    @Test
    fun survivesConcurrentListingsFromSeveralRequests() {
        val cache = ListedMangaCache<Int>(maxSize = 50)
        val workers = (0 until 8).map { w ->
            thread {
                repeat(500) { i -> cache.put("/m/${w * 1000 + i}", i); cache.get("/m/${i}") }
            }
        }
        workers.forEach { it.join() }

        // No exception above is the point; the size bound must also hold.
        val present = (0 until 8).sumOf { w -> (0 until 500).count { i -> cache.get("/m/${w * 1000 + i}") != null } }
        assert(present <= 50) { "cache grew past maxSize: $present" }
    }
}
