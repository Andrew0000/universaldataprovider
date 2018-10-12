package crocodile8008.universaldataprovider

import crocodile8008.libuniversaldataprovider.DataProvider
import crocodile8008.libuniversaldataprovider.TimeMemoryCache

object AppDataProviders {

    val provider = DataProvider(TimeMemoryCache<String, DomainModel>(
            lifeTimeMillis = 5000L,
            maxSize = 10
    )) { key ->
        MockNetworkService.load(key).firstOrError()
    }
}