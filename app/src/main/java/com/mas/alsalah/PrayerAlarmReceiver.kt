package com.mas.alsalah

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.speech.tts.TextToSpeech
import java.util.Locale

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: "الصلاة"
        val before = intent.getBooleanExtra("before", false)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (before) "prayer_before" else "prayer_now"
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, if(before) "تنبيهات قبل الصلاة" else "دخول وقت الصلاة",
                if(before) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_HIGH)
            channel.description = "تنبيهات تطبيق الصلاة"
            nm.createNotificationChannel(channel)
        }
        val title = if (before) "اقتربت صلاة $prayer" else "صلاة $prayer الآن"
        val body = if (before) "استعد للصلاة • تطبيق الصلاة" else "الله أكبر، الله أكبر"
        val n = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true).build()
        nm.notify((prayer + before).hashCode(), n)

        if (!before) {
            // Device Arabic TTS speaks the requested takbeer; volume/voice are controlled by Android.
            lateinit var engine: TextToSpeech
            engine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val languageResult = engine.setLanguage(Locale("ar"))
                    if (languageResult >= TextToSpeech.LANG_AVAILABLE) {
                        engine.speak("الله أكبر الله أكبر", TextToSpeech.QUEUE_FLUSH, null, "takbeer_$prayer")
                    } else {
                        engine.shutdown()
                    }
                } else {
                    engine.shutdown()
                }
            }
        }
        // Keep the rolling schedule populated beyond the first three days.
        val prefs = context.getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 30.0444f).toDouble()
        val lon = prefs.getFloat("lon", 31.2357f).toDouble()
        val lead = prefs.getInt("lead", 15)
        val enabled = BooleanArray(5) { prefs.getBoolean("enabled_$it", true) }
        AlarmScheduler.schedule(context, lat, lon, lead, enabled)
    }
}
