package crocodile8008.universaldataprovider

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disposable.add(
                AppDataProviders.provider.getFromCacheElseFetch("params1")
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        textView1.text = "Loading..."
                    }
                    .subscribe(
                            { textView1.text = "$it" },
                            { textView1.text = "Error: ${it.localizedMessage}" }
                    )
        )

        disposable.add(
                AppDataProviders.provider.getFromCacheElseFetch("params2")
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            textView2.text = "Loading..."
                        }
                        .subscribe(
                                { textView2.text = "$it" },
                                { textView2.text = "Error: ${it.localizedMessage}" }
                        )
        )
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }
}
