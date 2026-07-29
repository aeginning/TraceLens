package pl.tracelens.app

import android.app.Application
import pl.tracelens.app.data.AppRepository

class TraceLensApplication : Application() {
    val repository by lazy { AppRepository(this) }
}
