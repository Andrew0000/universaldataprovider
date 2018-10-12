package crocodile8008.libuniversaldataprovider

class MemoryCache<K, V> : Cache<K, V> {

    private val values = HashMap<K, V>()

    override fun get(key: K): V? = values[key]

    override fun set(key: K, value: V) {
        values[key] = value
    }
}