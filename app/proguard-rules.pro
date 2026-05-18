# ─── Expense Pro – ProGuard Rules ───────────────────────────────────────────
# File: app/proguard-rules.pro
#
# Production minification rules.
# Keep these when you add Retrofit (Phase 2) or other reflection-heavy libs.

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembernames class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class androidx.room.** { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }

# ── Domain Models (keep for Room reflection) ─────────────────────────────────
-keep class com.yusuf.expensepro.domain.model.** { *; }
-keep class com.yusuf.expensepro.data.local.entity.** { *; }

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Google Sign-In ────────────────────────────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── Future: Retrofit + Gson (uncomment when adding Phase 2) ──────────────────
# -keep class com.squareup.retrofit2.** { *; }
# -keep class com.google.gson.** { *; }
# -keepclassmembers class com.yusuf.expensepro.data.remote.dto.** { *; }
# -keepattributes Signature
# -keepattributes *Annotation*
