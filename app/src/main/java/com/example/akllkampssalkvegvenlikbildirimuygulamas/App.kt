package com.example.akllkampssalkvegvenlikbildirimuygulamas

import android.app.Application
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.NotificationHelper
import org.osmdroid.config.Configuration
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // osmdroid config
        val prefs = getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().setUserAgentValue(packageName)

        // Force cache to app cache dir -> no external storage permission needed
        val base = File(cacheDir, "osmdroid")
        if (!base.exists()) base.mkdirs()
        val tiles = File(base, "tiles")
        if (!tiles.exists()) tiles.mkdirs()
        Configuration.getInstance().setOsmdroidBasePath(base)
        Configuration.getInstance().setOsmdroidTileCache(tiles)

        NotificationHelper.ensureChannel(this)
    }
}