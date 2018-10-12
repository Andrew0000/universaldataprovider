package crocodile8008.libuniversaldataprovider

class TwoLvlsCache<K, V>(
    private val firstCache: Cache<K, V>,
    private val secondCache: Cache<K, V>
) : Cache<K, V> {

    override fun get(key: K): V? = firstCache[key] ?: secondCache[key]

    override fun set(key: K, value: V) {
        firstCache[key] = value
        secondCache[key] = value
    }
}