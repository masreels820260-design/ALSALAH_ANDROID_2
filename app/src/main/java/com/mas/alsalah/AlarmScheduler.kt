package com.mas.alsalah

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

object AlarmScheduler {
    fun schedule(context: Context, lat: Double, lon: Double, leadMinutes: Int, enabled: BooleanArray) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val offset = zone.rules.getOffset(java.time.Instant.ofEpochMilli(now)).totalSeconds / 3600.0
        val prefs = context.getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)
        val days = (0..2).map { LocalDate.now().plusDays(it.toLong()) }
        val names = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        days.forEach { date ->
            val p = PrayerTimes.calculate(date, lat, lon, offset)
            val values = listOf(p.fajr, p.dhuhr, p.asr, p.maghrib, p.isha)
            values.forEachIndexed { i, h ->
                val mins = PrayerTimes.minutes(h)
                if (mins >= 0 && enabled.getOrElse(i) { true }) {
                    val at = date.atStartOfDay(zone).toInstant().toEpochMilli() + mins * 60_000L
                    if (at > now) {
                        scheduleOne(context, am, at - leadMinutes * 60_000L, names[i], true, i*100 + date.dayOfMonth)
                        scheduleOne(context, am, at, names[i], false, 1000 + i*100 + date.dayOfMonth)
                    }
                }
            }
        }
    }
    private fun scheduleOne(context: Context, am: AlarmManager, at: Long, name: String, before: Boolean, id: Int) {
        if (at <= System.currentTimeMillis()) return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("prayer", name); putExtra("before", before)
        }
        val pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
