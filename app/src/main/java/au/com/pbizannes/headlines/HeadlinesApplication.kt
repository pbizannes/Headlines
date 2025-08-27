package au.com.pbizannes.headlines

import android.app.Application
import android.webkit.WebView
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HeadlinesApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.BUILD_TYPE == "debug")
            WebView.setWebContentsDebuggingEnabled(true)
    }
}