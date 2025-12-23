package com.example.akllkampssalkvegvenlikbildirimuygulamas.utils

import android.content.Context

data class CampusBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    fun contains(lat: Double, lon: Double): Boolean {
        return lat in minLat..maxLat && lon in minLon..maxLon
    }
}

object SettingsManager {
    private const val PREF = "settings"

    private const val KEY_ENABLE_BOUNDS = "enableCampusBoundsValidation"
    private const val KEY_MIN_LAT = "campus_min_lat"
    private const val KEY_MAX_LAT = "campus_max_lat"
    private const val KEY_MIN_LON = "campus_min_lon"
    private const val KEY_MAX_LON = "campus_max_lon"

    // Default: example campus bbox around sample seed coordinates (Ankara-ish)
    private const val DEF_ENABLE = true
    private const val DEF_MIN_LAT = 39.915
    private const val DEF_MAX_LAT = 39.926
    private const val DEF_MIN_LON = 32.848
    private const val DEF_MAX_LON = 32.860

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun isCampusBoundsValidationEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ENABLE_BOUNDS, DEF_ENABLE)
    }

    fun setCampusBoundsValidationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLE_BOUNDS, enabled).apply()
    }

    fun getCampusBounds(context: Context): CampusBounds {
        val p = prefs(context)
        val minLat = p.getFloat(KEY_MIN_LAT, DEF_MIN_LAT.toFloat()).toDouble()
        val maxLat = p.getFloat(KEY_MAX_LAT, DEF_MAX_LAT.toFloat()).toDouble()
        val minLon = p.getFloat(KEY_MIN_LON, DEF_MIN_LON.toFloat()).toDouble()
        val maxLon = p.getFloat(KEY_MAX_LON, DEF_MAX_LON.toFloat()).toDouble()
        return CampusBounds(minLat, maxLat, minLon, maxLon)
    }

    fun setCampusBounds(context: Context, bounds: CampusBounds) {
        prefs(context).edit()
            .putFloat(KEY_MIN_LAT, bounds.minLat.toFloat())
            .putFloat(KEY_MAX_LAT, bounds.maxLat.toFloat())
            .putFloat(KEY_MIN_LON, bounds.minLon.toFloat())
            .putFloat(KEY_MAX_LON, bounds.maxLon.toFloat())
            .apply()
    }
}
