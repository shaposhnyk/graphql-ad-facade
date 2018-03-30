package com.shaposhnyk.graphql.adfacade

import java.util.concurrent.ConcurrentHashMap

class SimpleCache<in K, out V>(val supplier: (K) -> V,
                               private val cache: ConcurrentHashMap<K, V> = ConcurrentHashMap<K, V>()) {
    fun get(key: K) = cache.computeIfAbsent(key, supplier)

    fun isEmpty() = cache.isEmpty()

    fun clear() = cache.clear()
}
