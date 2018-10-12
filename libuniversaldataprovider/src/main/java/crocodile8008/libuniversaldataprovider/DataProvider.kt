package crocodile8008.libuniversaldataprovider

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DataProvider<K, V>(
    private val cache: Cache<K, V>,
    private val fetch: (key: K) -> Single<V>
) {

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

    fun getFromCacheAndFetchAnyway(key: K, updateFetchDisposable: CompositeDisposable): Single<V> = Single.defer {
        getFromCache(key)
        .doOnSuccess { _ ->
            updateFetchDisposable.add(
                fetchAndStoreToCache(key).subscribe({}, {})
            )
        }
        .onErrorResumeNext { _ ->
            fetchAndStoreToCache(key)
        }
    }
}