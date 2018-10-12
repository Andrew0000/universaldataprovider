package crocodile8008.libuniversaldataprovider

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SharedRequest<K, V>(private val load: (key: K) -> Single<V>) {

    private val requests = HashMap<K, Observable<V>>()

    fun performOrAttachToOngoing(key: K): Single<V> = Observable
            .defer {
                synchronized(requests) {
                    requests[key]?.let { cachedShared ->
                        return@defer cachedShared
                    }

                    val newShared = load(key)
                            .subscribeOn(Schedulers.io())
                            .doFinally {
                                synchronized(requests) { requests.remove(key) }
                            }
                            .toObservable()
                            .replay(1)
                            .refCount()

                    requests[key] = newShared
                    return@defer newShared
                }
            }
            .firstOrError()
}