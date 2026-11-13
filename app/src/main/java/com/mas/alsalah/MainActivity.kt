package com.mas.alsalah

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Ink = Color(0xFF111111)
private val Green = Color(0xFF777777)
private val Paper = Color(0xFFFFFFFF)
private val Glass = Color(0xCCFFFFFF)
private val Night = Color(0xFF000000)
private val NightGlass = Color(0xFF171717)
private val Silver = Color(0xFFE9E9E9)

class MainActivity : ComponentActivity() {
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { getLocation() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createChannels()
        setContent { AlSalahApp(this) }
    }
    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.createNotificationChannel(android.app.NotificationChannel("prayer_before", "تنبيهات قبل الصلاة", android.app.NotificationManager.IMPORTANCE_HIGH))
            nm.createNotificationChannel(android.app.NotificationChannel("prayer_now", "دخول وقت الصلاة", android.app.NotificationManager.IMPORTANCE_HIGH))
        }
    }
    fun askLocation() {
        locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        try {
            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    getSharedPreferences("prayer_settings", MODE_PRIVATE).edit()
                        .putFloat("lat", loc.latitude.toFloat()).putFloat("lon", loc.longitude.toFloat()).apply()
                    scheduleNow()
                }
            }
        } catch (_: SecurityException) {}
    }
    fun scheduleNow() {
        val p = getSharedPreferences("prayer_settings", MODE_PRIVATE)
        val lat = p.getFloat("lat", 30.0444f).toDouble()
        val lon = p.getFloat("lon", 31.2357f).toDouble()
        AlarmScheduler.schedule(this, lat, lon, p.getInt("lead", 15), BooleanArray(5) { p.getBoolean("enabled_$it", true) })
    }
}

@Composable
fun AlSalahApp(activity: MainActivity) {
    val prefs = remember { activity.getSharedPreferences("prayer_settings", Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf("الرئيسية") }
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
    var use24 by remember { mutableStateOf(prefs.getBoolean("24h", true)) }
    var lead by remember { mutableIntStateOf(prefs.getInt("lead", 15)) }
    var locationName by remember { mutableStateOf(prefs.getString("place", "القاهرة") ?: "القاهرة") }
    var lat by remember { mutableDoubleStateOf(prefs.getFloat("lat", 30.0444f).toDouble()) }
    var lon by remember { mutableDoubleStateOf(prefs.getFloat("lon", 31.2357f).toDouble()) }
    var selectedPrayer by remember { mutableIntStateOf(0) }
    var tasbeeh by remember { mutableIntStateOf(prefs.getInt("tasbeeh", 0)) }
    var azkarCount by remember { mutableIntStateOf(prefs.getInt("azkar_count", 0)) }
    var enabled by remember { mutableStateOf(List(5) { prefs.getBoolean("enabled_$it", true) }) }
    LaunchedEffect(Unit) { activity.scheduleNow(); while(true) { tick = System.currentTimeMillis(); delay(1000) } }
    val zone = java.time.ZoneId.systemDefault()
    val offset = zone.rules.getOffset(java.time.Instant.ofEpochMilli(tick)).totalSeconds / 3600.0
    val today = LocalDate.now()
    val times = remember(today, lat, lon, offset) { PrayerTimes.calculate(today, lat, lon, offset) }
    val prayerList = listOf("الفجر" to times.fajr, "الظهر" to times.dhuhr, "العصر" to times.asr, "المغرب" to times.maghrib, "العشاء" to times.isha)
    val nowCal = java.time.LocalTime.now()
    val nowMin = nowCal.hour * 60 + nowCal.minute
    val next = prayerList.indexOfFirst { PrayerTimes.minutes(it.second) > nowMin }.let { if(it < 0) 0 else it }
    val nextName = prayerList[next].first
    val nextMinutes = PrayerTimes.minutes(prayerList[next].second)
    val remaining = ((nextMinutes - nowMin + 1440) % 1440) * 60 - nowCal.second
    val bg = if(dark) Night else Paper
    val fg = if(dark) Color(0xFFF5F5F5) else Ink
    val card = if(dark) NightGlass else Glass
    val muted = if(dark) Color(0xFFAAAAAA) else Color(0xFF777777)
    MaterialTheme(colorScheme = if(dark) darkColorScheme(primary=Color.White, background=bg, surface=NightGlass, onSurface=fg) else lightColorScheme(primary=Ink, background=bg, surface=Glass, onSurface=fg)) {
        Scaffold(containerColor=bg, bottomBar = {
            NavigationBar(containerColor=if(dark) Color(0xF0141414) else Color(0xF7FFFFFF)) {
                listOf("الرئيسية","القبلة","الأذكار","المسبحة","الإعدادات").forEachIndexed { i, s ->
                    val icons = listOf(Icons.Default.Home, Icons.Default.Explore, Icons.Default.MenuBook, Icons.Default.Favorite, Icons.Default.Settings)
                    NavigationBarItem(selected=page==s, onClick={page=s}, icon={Icon(icons[i], null)}, label={Text(s, fontSize=10.sp)})
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp), verticalArrangement=Arrangement.spacedBy(14.dp), contentPadding=PaddingValues(top=18.dp,bottom=24.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                        Column {
                            Text("السلام عليكم", color=fg, fontSize=24.sp, fontWeight=FontWeight.Bold)
                            Text("$locationName • ${today.format(DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar")))}", color=fg.copy(alpha=.65f))
                        }
                        Surface(shape=RoundedCornerShape(18.dp), color=if(dark) Color.White.copy(alpha=.1f) else Color.White) {
                            IconButton(onClick={page="الإعدادات"}) { Icon(Icons.Default.Settings, null, tint=fg) }
                        }
                    }
                }
                when(page) {
                    "الرئيسية" -> {
                        item {
                            Surface(shape=RoundedCornerShape(30.dp), color=if(dark) Color(0xFF171717) else Color(0xFFF7F7F7), modifier=Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(22.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text("الصلاة القادمة", color=muted)
                                    Text(nextName, color=fg, fontSize=34.sp, fontWeight=FontWeight.Bold)
                                    Text("%02d:%02d:%02d".format(remaining/3600,(remaining%3600)/60,remaining%60), color=fg, fontSize=36.sp, fontWeight=FontWeight.Light)
                                    Text("متبقي على الصلاة", color=muted)
                                    Spacer(Modifier.height(10.dp))
                                    Text("التاريخ الهجري: ${runCatching { HijrahDate.now().toString() }.getOrDefault("—")}", color=muted)
                                }
                            }
                        }
                        item {
                            Text("مواقيت اليوم", color=fg, fontSize=20.sp, fontWeight=FontWeight.Bold)
                        }
                        items(6) { index ->
                            val pair = if(index < 5) prayerList[index] else "الشروق" to times.sunrise
                            PrayerRow(pair.first, PrayerTimes.format(pair.second,use24), index==next && index<5, fg, dark)
                        }
                        item {
                            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick={ activity.askLocation() }, modifier=Modifier.weight(1f)) { Icon(Icons.Default.MyLocation,null); Spacer(Modifier.width(6.dp)); Text("تحديد موقعي") }
                                OutlinedButton(onClick={page="الإعدادات"}, modifier=Modifier.weight(1f)) { Icon(Icons.Default.Notifications,null); Spacer(Modifier.width(6.dp)); Text("التنبيهات") }
                            }
                        }
                    }
                    "الإعدادات" -> {
                        item { Text("الإعدادات", color=fg, fontSize=24.sp, fontWeight=FontWeight.Bold) }
                        item { SettingCard("المظهر الداكن", fg, dark) { dark=it; prefs.edit().putBoolean("dark",it).apply() } }
                        item { SettingCard("تنسيق 24 ساعة", fg, use24) { use24=it; prefs.edit().putBoolean("24h",it).apply() } }
                        item {
                            Surface(color=card, shape=RoundedCornerShape(22.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("التنبيه قبل الصلاة", color=fg, fontWeight=FontWeight.Bold)
                                    Text("$lead دقيقة", color=Green)
                                    Slider(value=lead.toFloat(), onValueChange={lead=it.toInt()}, valueRange=0f..60f, steps=11, onValueChangeFinished={prefs.edit().putInt("lead",lead).apply(); activity.scheduleNow()})
                                    Text("يمكن اختيار مدة من 0 إلى 60 دقيقة", color=fg.copy(alpha=.6f), fontSize=12.sp)
                                }
                            }
                        }
                        item { Text("تفعيل تنبيهات كل صلاة", color=fg, fontWeight=FontWeight.Bold) }
                        items(5) { i ->
                            Surface(color=card, shape=RoundedCornerShape(18.dp)) {
                                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
                                    Text(prayerList[i].first, color=fg, modifier=Modifier.weight(1f))
                                    Switch(checked=enabled[i], onCheckedChange={v-> enabled=enabled.toMutableList().also{it[i]=v}; prefs.edit().putBoolean("enabled_$i",v).apply(); activity.scheduleNow()})
                                }
                            }
                        }
                        item {
                            Button(onClick={
                                if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(activity,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                                    activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),44)
                                if(Build.VERSION.SDK_INT>=31) activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                activity.scheduleNow()
                            }, modifier=Modifier.fillMaxWidth()) { Text("تفعيل صلاحيات التنبيهات") }
                        }
                        item {
                            OutlinedButton(onClick={activity.scheduleNow()}, modifier=Modifier.fillMaxWidth()) { Text("إعادة جدولة التنبيهات") }
                        }
                        item {
                            Text("الموقع: $locationName", color=fg)
                            OutlinedButton(onClick={ activity.askLocation() }, modifier=Modifier.fillMaxWidth()) { Text("استخدام GPS") }
                        }
                    }
                    "القبلة" -> {
                        item { Text("اتجاه القبلة", color=fg, fontSize=24.sp, fontWeight=FontWeight.Bold) }
                        item {
                            Surface(color=card, shape=RoundedCornerShape(28.dp), modifier=Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Explore, null, tint=Green, modifier=Modifier.size(100.dp))
                                    Text("ميزة البوصلة", color=fg, fontSize=22.sp, fontWeight=FontWeight.Bold)
                                    Text("سيتم استخدام إحداثيات موقعك لتحديد اتجاه القبلة. يلزم مستشعر البوصلة لمؤشر الاتجاه الحي.", color=fg.copy(alpha=.7f))
                                    Text("الموقع الحالي: ${"%.4f".format(lat)}, ${"%.4f".format(lon)}", color=Green)
                                    Text("اتجاه القبلة التقريبي: ${"%.1f".format(qiblaBearing(lat,lon))}°", color=fg, fontSize=20.sp)
                                    Button(onClick={activity.askLocation()}) { Text("تحديث الموقع") }
                                }
                            }
                        }
                    }
                    "الأذكار" -> {
                        item { Text("الأذكار", color=fg, fontSize=24.sp, fontWeight=FontWeight.Bold) }
                        items(listOf("أذكار الصباح","أذكار المساء","أذكار بعد الصلاة","أذكار النوم")) { title ->
                            Surface(color=card, shape=RoundedCornerShape(20.dp), modifier=Modifier.fillMaxWidth().clickable { azkarCount=0 }) {
                                Column(Modifier.padding(18.dp)) {
                                    Text(title, color=fg, fontWeight=FontWeight.Bold, fontSize=18.sp)
                                    Text("سبحان الله وبحمده، سبحان الله العظيم", color=fg.copy(alpha=.75f))
                                    Text("اضغط لبدء الذكر • $azkarCount", color=Green)
                                }
                            }
                        }
                        item { Button(onClick={azkarCount++; prefs.edit().putInt("azkar_count", azkarCount).apply()}, modifier=Modifier.fillMaxWidth()) { Text("تم الذكر  •  $azkarCount") } }
                    }
                    "المسبحة" -> {
                        item { Text("المسبحة الإلكترونية", color=fg, fontSize=24.sp, fontWeight=FontWeight.Bold) }
                        item {
                            Surface(color=card, shape=RoundedCornerShape(28.dp), modifier=Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(26.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text("سبحان الله", color=fg, fontSize=24.sp)
                                    Text("$tasbeeh", color=Green, fontSize=64.sp, fontWeight=FontWeight.Bold)
                                    Button(onClick={tasbeeh++; prefs.edit().putInt("tasbeeh", tasbeeh).apply()}, modifier=Modifier.fillMaxWidth().height(64.dp)) { Text("تسبيح", fontSize=20.sp) }
                                    TextButton(onClick={tasbeeh=0; prefs.edit().putInt("tasbeeh", 0).apply()}) { Text("إعادة العداد") }
                                }
                            }
                        }
                    }
                    else -> { item { Text(page, color=fg, fontSize=24.sp) } }
                }
                item { Text("الصلاة • MAS", color=fg.copy(alpha=.4f), modifier=Modifier.fillMaxWidth(), fontSize=12.sp) }
            }
        }
    }
}

@Composable
private fun PrayerRow(name: String, time: String, active: Boolean, fg: Color, dark: Boolean) {
    Surface(color=if(active) { if(dark) Color(0xFF303030) else Color(0xFFEEEEEE) } else if(dark) NightGlass else Glass, shape=RoundedCornerShape(18.dp), modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Text(name, color=fg, fontWeight=if(active) FontWeight.Bold else FontWeight.Medium)
            Text(time, color=fg, fontWeight=FontWeight.Bold, fontSize=18.sp)
        }
    }
}
@Composable
private fun SettingCard(title: String, fg: Color, value: Boolean, onChange: (Boolean)->Unit) {
    Surface(color=if (fg == Color.White || fg == Color(0xFFF5F5F5)) NightGlass else Glass, shape=RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
            Text(title, color=fg, modifier=Modifier.weight(1f))
            Switch(checked=value, onCheckedChange=onChange)
        }
    }
}
private fun qiblaBearing(lat: Double, lon: Double): Double {
    val kaabaLat = Math.toRadians(21.4225); val kaabaLon = Math.toRadians(39.8262)
    val p = Math.toRadians(lat); val dl = kaabaLon - Math.toRadians(lon)
    val y = kotlin.math.sin(dl)
    val x = kotlin.math.cos(p)*kotlin.math.tan(kaabaLat)-kotlin.math.sin(p)*kotlin.math.cos(dl)
    return (Math.toDegrees(kotlin.math.atan2(y,x))+360)%360
}
