package crocodile8008.libuniversaldataprovider

import android.os.SystemClock
import android.util.LruCache

class TimeMemoryCache<K, V>(private val lifeTimeMillis: Long, maxSize: Int) : Cache<K, V> {

    private val lru = LruCache<K, ValueAndTime<V>>(maxSize)

    override fun set(key: K, value: V) {
        lru.put(key, ValueAndTime(value))
    }

    override fun get(key: K): V? {
        val cached = lru[key] ?: return null
        val timeGoneMillis = SystemClock.elapsedRealtime() - cached.creationTimeMillis
        if (timeGoneMillis > lifeTimeMillis) {
            lru.remove(key)
            return null
        }
        return cached.value
    }

    fun clear() {
        lru.evictAll()
    }

    private data class ValueAndTime<V>(val value: V, val creationTimeMillis: Long = SystemClock.elapsedRealtime())
}