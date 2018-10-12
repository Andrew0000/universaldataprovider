package crocodile8008.libuniversaldataprovider

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DataProvider<K, V>(
    private val cache: Cache<K, V>,
    private val fetch: (key: K) -> Single<V>
) {

    private val ongoingFetching = HashMap<K, Observable<V>>()

    fun getFromCacheElseFetch(key: K): Single<V> {
        return createSharedFetch(key, fromCacheElseFetch(key))
    }

    fun getFromCache(key: K): Single<V> {
        return createSharedFetch(key, fromCacheOnly(key))
    }

    fun getFromFetch(key: K): Single<V> {
        return createSharedFetch(key, fetchOnly(key))
    }

    private fun createSharedFetch(key: K, getStrategy: Single<WrappedValue<V>>): Single<V> = Observable
                .defer {
                    synchronized(ongoingFetching) {
                        ongoingFetching[key]?.let { cachedShared ->
                            return@defer cachedShared
                        }

                        val newShared =
                                getStrategy
                                .subscribeOn(Schedulers.io())
                                .doOnSuccess { wrapped ->
                                    if (!wrapped.isFromCache) {
                                        synchronized(cache) { cache[key] = wrapped.value }
                                    }
                                }
                                .doFinally {
                                    synchronized(ongoingFetching) { ongoingFetching.remove(key) }
                                }
                                .map { wrapped -> wrapped.value }
                                .toObservable()
                                .replay(1)
                                .refCount()

                        ongoingFetching[key] = newShared
                        return@defer newShared
                    }
                }
                .firstOrError()

    private fun fromCacheElseFetch(key: K): Single<WrappedValue<V>> =
            fromCacheOnly(key)
            .onErrorResumeNext { fetchOnly(key) }

    private fun fromCacheOnly(key: K): Single<WrappedValue<V>> = Single
            .fromCallable {
                synchronized(cache) {
                    val cached = cache[key] ?: throw NullPointerException()
                    return@fromCallable WrappedValue(value = cached, isFromCache = true)
                }
            }

    private fun fetchOnly(key: K): Single<WrappedValue<V>> = fetch.invoke(key)
            .map { WrappedValue(value = it, isFromCache = false) }

    private data class WrappedValue<V>(val value: V, val isFromCache: Boolean)
}