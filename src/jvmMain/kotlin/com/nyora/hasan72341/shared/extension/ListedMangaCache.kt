package com.nyora.hasan72341.shared.extension

/**
 * The entries a source returned from its catalogue (popular / latest / search), keyed by
 * manga url, so `/manga/details?url=` can hand the parser the entry the user actually tapped.
 *
 * kotatsu's `getDetails(manga)` expects that entry and returns `manga.copy(...)`, so a parser
 * only overwrites the fields its detail page adds — the title and cover come from the list.
 * Nyora's REST surface is addressed by url alone, so without this the parser is seeded with an
 * empty manga, the cover comes back blank, and the OpenGraph fallback fills in an `og:image`
 * share card that does not match the cover shown in the list.
 *
 * Least-recently-used, bounded, and safe to share between concurrent requests.
 */
internal class ListedMangaCache<V : Any>(private val maxSize: Int = DEFAULT_MAX_SIZE) {

    private val entries = object : LinkedHashMap<String, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, V>): Boolean =
            size > maxSize
    }

    @Synchronized
    fun put(url: String, entry: V) {
        entries[url] = entry
    }

    @Synchronized
    fun get(url: String): V? = entries[url]

    private companion object {
        const val DEFAULT_MAX_SIZE = 2000
    }
}
