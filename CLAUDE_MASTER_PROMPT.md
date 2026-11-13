# AL-SALAH — MAS | Master Development Brief

You are a senior Android engineer, Kotlin/Compose architect, UI/UX designer, performance engineer, and QA engineer. You are receiving an EXISTING Android project in this ZIP. Your job is to inspect, improve, complete, test, and return this same project. Do not merely describe a plan. Do not create a separate demo. Preserve useful working code and explain any necessary architectural changes.

## Delivery objective
Deliver a complete, polished Android prayer application named **الصلاة (AL-SALAH)**, branded **MAS**, with reliable prayer calculations, real Android notifications, takbeer-only audio, GPS/manual location, Qibla, Adhkar, Tasbeeh, Hijri calendar, daily Dua, settings, widgets, offline-first storage, and a GitHub Actions APK build. The final output must be a complete source ZIP and a successfully built APK artifact when the environment permits.

Do not claim the app is production-ready or a feature is tested unless it has actually been implemented and verified. If a device-only test cannot be performed, state that clearly. Never replace a real feature with a fake button, static mock, placeholder, or fabricated data.

## 1. Platform and engineering constraints
- Native Android; Kotlin; Jetpack Compose; Material 3 with a custom design system.
- Minimum SDK 26; choose a currently supported target SDK and compatible dependencies.
- JDK 17 and a reproducible Gradle build suitable for GitHub Actions.
- Arabic-first, full RTL support, proper accessibility semantics and scalable text.
- No mandatory login, no mandatory backend, local-first/offline-first operation.
- Use MVVM/repository separation where useful, Coroutines, StateFlow, lifecycle-aware collection, Room for structured data, DataStore for preferences.
- Keep MainActivity small. Do not put the entire app in one file. Avoid overengineering and unnecessary dependencies.
- No developer-specific paths, local.properties dependency, API secrets, or private credentials in the repository.

## 2. Complete visual redesign — premium Apple-inspired Liquid Glass
The current UI is not acceptable as the final design. Redesign every screen and reusable component, not only the home page. Aim for the refinement of Apple's Liquid Glass while retaining Android-native usability; do not copy Apple's proprietary assets or exact UI.

**Light theme:** pure white (#FFFFFF) background, near-black text (#111111), translucent white glass surfaces, subtle silver borders, very soft gray shadows, carefully restrained blur, generous whitespace, readable contrast.

**Dark theme:** deep black/near-black background, white text, dark translucent glass surfaces, subtle silver borders/highlights, restrained glow only where useful. No excessive gradients or saturated purple/blue backgrounds.

Support Light, Dark, and System Default. Persist selection. All surfaces, dialogs, sheets, icons, dividers, navigation, and widgets must follow the theme. Build reusable GlassCard, GlassButton, GlassNavigation, GlassSheet, GlassDialog, GlassSettingsRow, GlassSegmentedControl, and countdown components. Prefer lightweight translucent fills and borders over expensive blur; automatically fall back to a lightweight surface on weaker devices. Ensure text contrast and accessibility.

## 3. Performance — top priority
The app must feel fast on low- and mid-range Android devices. Avoid heavy or decorative animation, infinite animations, large blur layers, excessive recompositions, unnecessary allocations, main-thread calculations, continuous GPS, polling, and needless network calls. Use stable UI state, `remember`, `derivedStateOf`, keys, and lifecycle-aware state collection appropriately. Keep one-second countdown updates scoped to the countdown only. Calculate prayer times off the main thread when appropriate. Use LazyColumn/LazyRow correctly, cache results, minimize startup work, and avoid battery-draining background work. Profile where possible and report measured results rather than promises.

## 4. Navigation and complete screen map
Main navigation (RTL):
1. الرئيسية
2. القبلة
3. الأذكار
4. المسبحة
5. المزيد

More contains التقويم الهجري، دعاء اليوم، الإعدادات، حول التطبيق. Implement real navigation, correct Android back behavior, state restoration where appropriate, empty/loading/error states, and consistent design. No inert controls.

## 5. Home dashboard
Build a premium dashboard with app identity, selected Egyptian governorate/city, Gregorian and Hijri dates, and settings shortcut. Main prayer card shows the next prayer name, local time, live HH:MM:SS countdown, and a restrained progress indicator. Update the next prayer correctly after each prayer and across midnight. Show all five prayers (الفجر، الظهر، العصر، المغرب، العشاء) plus الشروق, with times, notification state, and a clear next-prayer highlight. Add functional shortcuts to Qibla, Adhkar, Tasbeeh, daily Dua, and Hijri calendar.

## 6. Prayer times for Egypt
Support all 27 Egyptian governorates and a searchable hierarchical selector: Governorate → available city/administrative center → locality/village where reliable coordinates exist. Include GPS automatic location, manual selection, saved location, and manual fallback when permission is denied. Do not invent geographic records or claim village-level precision without verified coordinates. Use GPS coordinates as universal fallback.

Implement a tested astronomical calculation engine, not hardcoded schedules. Provide the Egyptian General Authority of Survey convention with commonly used Fajr 19.5° and Isha 17.5° as a selectable/verified method; verify the current authoritative convention before claiming official accuracy. Include Julian date, solar declination, equation of time, solar noon, sunrise/sunset, Fajr, Dhuhr, Asr, Maghrib, Isha, timezone, DST, date transitions, and edge cases. Make Asr juristic method and manual minute adjustments configurable. Validate against trusted Egyptian references for multiple cities and dates; document tolerances and discrepancies. Never silently label estimates as official.

## 7. Real notifications and takbeer
Notifications must be scheduled by Android and work when the app is not open, subject to Android/device restrictions. Implement AlarmManager with exact-alarm permission handling where applicable, notification channels, BroadcastReceivers, boot rescheduling, date/time/time-zone change handling, app-update rescheduling, and rolling future scheduling. Respect Android background execution and foreground-service rules; use a foreground service only if genuinely required and with the correct service type/notification. Do not request unnecessary full-screen intent privileges.

Before-prayer notification title example: **اقتربت صلاة العصر**. Allow per-prayer lead time: disabled, 5, 10, 15, 20, 25, 30 minutes, or custom.

At prayer time title example: **صلاة العصر الآن**. Default spoken/audio content must be **takbeer only**: “الله أكبر، الله أكبر”. Do not play the full Adhan by default. Prefer a properly licensed bundled Arabic takbeer recording if its license is documented; otherwise use Android Arabic TextToSpeech with availability/language checks and a graceful fallback. Never include unlicensed audio.

Provide independent controls per prayer for pre-alert, prayer-time alert, takbeer/silent, notification tone, vibration, sound enabled, and preview/test. Explain accurately that an app cannot guarantee overriding Android volume, silent mode, Do Not Disturb, battery restrictions, or OEM policies. Handle Android 13+ notification permission and exact alarm access correctly. Add a real “اختبار الإشعار والصوت” action. Do not report success unless the platform confirms scheduling/action.

## 8. Location and time settings
Request location only when the user chooses automatic location. Use one-shot/fresh-enough location, save last valid coordinates, do not continuously track. Support manual governorate/city/coordinates. Handle denied/revoked permission gracefully. Use system timezone by default; correctly respect DST automatically. If offering a manual summer/winter override, make it explicit and prevent double-applying DST. Support 12/24-hour display and persist preference.

## 9. Qibla compass
Calculate bearing from user coordinates to Kaaba (21.4225, 39.8262). Use Android rotation-vector/compass sensors with proper sensor lifecycle, smoothing that does not add lag, calibration guidance, degree display, and missing-sensor fallback. Do not display a fake compass. Provide a manual bearing/direction fallback and clearly communicate sensor accuracy limitations.

## 10. Adhkar library
Provide offline sections: أذكار الصباح، أذكار المساء، أذكار بعد الصلاة، أذكار النوم، أذكار الاستيقاظ، أذكار متنوعة. Every item must include verified Arabic text, repetition count, and source/reference where available. Include remaining count, tap-to-count, completion, reset, previous/next, favorites, copy/share, and local progress persistence. Do not fabricate Quranic verses, Hadith, virtues, or references. Use trustworthy sources and clearly mark any content requiring review; do not present unverified text as authentic.

## 11. Tasbeeh
Provide a responsive large counter with tap-to-increment, optional haptic feedback, undo, reset, selectable and custom Dhikr, daily target, progress, saved sessions, and daily/total counts. Persist locally. No heavy animation.

## 12. Hijri calendar
Show current Hijri and Gregorian dates, monthly grid, previous/next month, and Islamic occasions. Allow user adjustment from -2 to +2 days and persist it. Use Android/ICU or a vetted calendar implementation and clearly label dates/occasions that may vary by moon sighting; do not claim uncertain dates are guaranteed.

## 13. Daily Dua
Provide an offline verified collection with daily selection/rotation, Arabic text, source, favorites, copy/share, and previous/next. Do not invent religious citations or present generated text as sourced scripture.

## 14. Settings control center
Organize settings into Appearance, Time, Location, Prayer Calculation, Notifications, and General. Include theme, 12/24-hour format, timezone/DST behavior, GPS/manual location, calculation method, Asr method, per-prayer minute adjustments, per-prayer notification controls, lead time, takbeer/silent, vibration, sound preview/test, language, privacy/about/version, and reset preferences. All controls must actually work and persist after restart.

## 15. Android home-screen widgets
Implement actual Android widgets (Glance or RemoteViews, chosen compatibly) in small, medium, and large sizes. Small: next prayer and countdown. Medium: next prayer/countdown/location. Large: all five prayer times plus sunrise and next-prayer highlight. Respect light/dark theme, update efficiently, refresh on schedule/location changes, and open the correct app screen on tap. Do not create static widgets that never update. Respect Android widget update limits and avoid per-second background polling; use practical refresh strategy and explain countdown limitations if necessary.

## 16. Offline-first data
Prayer calculation from saved coordinates, saved location, notifications, Adhkar, Tasbeeh, Hijri calendar, Dua, and settings must work offline. Use Room for structured user data and DataStore for preferences. Bundle verified core content locally. Internet is optional only for data/catalog updates if implemented. Do not add Firebase/Supabase unless justified.

## 17. Privacy and permissions
No account required, no hidden tracking, no unnecessary analytics, no API secrets. Request location and notifications only in context. Explain why each permission is needed. Keep personal settings local. Follow Android permission policy and current SDK requirements.

## 18. Testing and acceptance checklist
Before declaring completion:
- Clean Gradle build from a fresh checkout.
- Resolve all compilation/dependency errors.
- Run unit tests and report results.
- Prayer calculations validated against trusted references across multiple Egyptian locations/dates.
- Countdown transitions and midnight verified.
- DST/timezone behavior verified.
- Alarm scheduling, rescheduling, boot receiver, notification permission, exact-alarm denial, and app updates tested.
- Arabic RTL, accessibility, light/dark/system themes, and persistence tested.
- Qibla bearing math and sensor absence handled.
- Adhkar/Tasbeeh/Dua persistence and offline use tested.
- Widget refresh strategy tested.
- Check crashes/logs and low-memory behavior.

Clearly separate CI/build tests, emulator tests, and physical-device tests. Never claim physical device verification if none occurred.

## 19. GitHub Actions and final delivery
Ensure `.github/workflows/build-apk.yml` exists and triggers on push to `main` and `workflow_dispatch`. Use JDK 17, compatible Gradle/Android SDK setup, build `:app:assembleDebug`, and upload `app/build/outputs/apk/debug/app-debug.apk` as artifact `alsalah-final-apk`. Do not depend on local.properties or machine-specific files. Verify workflow syntax and dependency versions.

Return:
1. Complete modified Android source project.
2. Complete redesign and implemented features.
3. README with exact GitHub and local build steps.
4. A ZIP containing the project root and hidden `.github` workflow.
5. APK artifact if build execution is available.
6. A concise implementation/test report listing completed, partially completed, and platform-limited items.

## 20. Required execution behavior
First inspect every file in the supplied project and identify its actual state. Then modify the existing code in coherent phases, but continue through all phases without stopping at a plan or home-screen mockup. Build and test iteratively. If the project has a defect, fix it rather than hiding it. If a feature cannot be reliably completed within the environment, do not fake it: implement the best safe fallback and document precisely what remains.

**Product:** الصلاة — AL-SALAH  
**Brand:** MAS  
**Design:** premium Apple-inspired Liquid Glass  
**Light:** pure white background, dark text  
**Dark:** deep black background, white text  
**Performance:** lightweight, extremely responsive, no heavy animations  
**Goal:** complete, honest, tested Android application and reproducible APK build.
