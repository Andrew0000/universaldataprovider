package crocodile8008.libuniversaldataprovider

interface Cache<K, V> {

    operator fun get(key: K) : V?

    operator fun set(key: K, value: V)
}