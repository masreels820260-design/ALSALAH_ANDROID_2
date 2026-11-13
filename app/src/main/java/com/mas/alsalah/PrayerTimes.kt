package com.mas.alsalah

import java.time.LocalDate
import kotlin.math.*

data class PrayerSet(val fajr: Double, val sunrise: Double, val dhuhr: Double, val asr: Double, val maghrib: Double, val isha: Double)

object PrayerTimes {
    // Egyptian General Authority of Survey convention: Fajr 19.5°, Isha 17.5°.
    // Calculations are astronomical approximations; local authority adjustments may differ.
    fun calculate(date: LocalDate, latitude: Double, longitude: Double, timezone: Double): PrayerSet {
        val jd = julian(date) - longitude / (15.0 * 24.0)
        val d = jd - 2451545.0
        val g = norm(357.529 + 0.98560028 * d)
        val q = 280.459 + 0.98564736 * d
        val l = norm(q + 1.915 * sin(rad(g)) + 0.020 * sin(2 * rad(g)))
        val e = 23.439 - 0.00000036 * d
        val ra = norm(deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l))))) / 15.0
        val eqt = q / 15.0 - ra
        val decl = asin(sin(rad(e)) * sin(rad(l)))
        val noon = 12.0 + timezone - longitude / 15.0 - eqt
        // Solar altitude is negative before/after sunrise and positive for Asr.
        fun hourAngle(altitudeDegrees: Double): Double {
            val x = (sin(rad(altitudeDegrees)) - sin(rad(latitude)) * sin(decl)) /
                    (cos(rad(latitude)) * cos(decl))
            return if (x !in -1.0..1.0) Double.NaN else deg(acos(x)) / 15.0
        }
        val fajr = noon - hourAngle(-19.5)
        val sunrise = noon - hourAngle(-0.833)
        val sunset = noon + hourAngle(-0.833)
        val asrAltitude = deg(atan(1.0 / (1.0 + tan(abs(rad(latitude) - decl)))))
        val asr = noon + hourAngle(asrAltitude)
        val isha = noon + hourAngle(-17.5)
        return PrayerSet(fajr, sunrise, noon, asr, sunset, isha)
    }
    private fun julian(date: LocalDate): Double {
        var y = date.year; var m = date.monthValue
        if (m <= 2) { y--; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + date.dayOfMonth + b - 1524.5
    }
    private fun rad(v: Double) = Math.toRadians(v)
    private fun deg(v: Double) = Math.toDegrees(v)
    private fun norm(v: Double): Double { var x = v % 360.0; if (x < 0) x += 360.0; return x }
    fun format(hours: Double, use24: Boolean = true): String {
        if (hours.isNaN()) return "--:--"
        val total = ((hours * 60).roundToInt() + 1440) % 1440
        val h = total / 60; val m = total % 60
        return if (use24) "%02d:%02d".format(h,m) else "%d:%02d %s".format(if (h%12==0) 12 else h%12,m,if(h<12) "ص" else "م")
    }
    fun minutes(hours: Double): Int = if(hours.isNaN()) -1 else ((hours*60).roundToInt()+1440)%1440
}
