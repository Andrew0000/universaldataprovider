package crocodile8008.libuniversaldataprovider

import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class DataProvider<K, V>(
    private val cache: Cache<K, V>,
    private val fetch: (key: K) -> Single<V>
) {

    private val emptyConsumerV : Consumer<V> = Consumer { }
    private val emptyConsumerT : Consumer<Throwable> = Consumer { }

    private val ongoingFetch = SharedRequest<K, V> { key ->
        fetch.invoke(key).doOnSuccess { cache[key] = it }
    }

    fun fetchAndStoreToCache(key: K): Single<V> {
        return ongoingFetch.performOrAttachToOngoing(key)
                .subscribeOn(Schedulers.io())
    }

    fun getFromCache(key: K): Single<V> = Single.defer {
        Single.defer { Single.just(cache[key]) }
                .subscribeOn(Schedulers.io())
    }

    fun getFromCacheElseFetch(key: K): Single<V> = Single.defer {
        getFromCache(key)
        .onErrorResumeNext { t: Throwable ->
            fetchAndStoreToCache(key)
        }
    }

    fun getFromCacheFetchAnyway(key: K): Single<V> = Single.defer {
        getFromCache(key)
        .doOnSuccess { _ ->
            fetchAndStoreToCache(key).subscribe(emptyConsumerV, emptyConsumerT)
        }
        .onErrorResumeNext { _ ->
            fetchAndStoreToCache(key)
        }
    }
}