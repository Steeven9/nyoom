# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-keepclasseswithmembers class androidx.compose.** {
    public <methods>;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
