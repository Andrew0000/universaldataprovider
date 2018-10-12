package crocodile8008.universaldataprovider

import android.os.Looper
import android.os.SystemClock
import io.reactivex.Observable
import java.util.*

object MockNetworkService {

    fun load(params: String) : Observable<DomainModel> = Observable
            .defer {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    throw IllegalStateException("Network in main thread")
                }
                SystemClock.sleep(4000)
                Observable.just(DomainModel("Ok: $params", Random().nextInt()))
            }
}