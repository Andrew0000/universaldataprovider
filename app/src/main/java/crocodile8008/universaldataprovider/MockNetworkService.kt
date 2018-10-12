package crocodile8008.universaldataprovider

import android.os.SystemClock
import io.reactivex.Observable
import java.util.*

object MockNetworkService {

    fun load(params: String) : Observable<DomainModel> = Observable
            .defer {
                SystemClock.sleep(3000)
                Observable.just(DomainModel("Ready", Random().nextInt()))
            }
}