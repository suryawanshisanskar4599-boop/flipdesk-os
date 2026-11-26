package com.flipdesk.core.widgets

import kotlinx.serialization.Serializable

enum class WidgetCategory(val label: String) {
    SYSTEM("System"),
    PRODUCTIVITY("Productivity"),
    ENTERTAINMENT("Entertainment"),
    INFORMATION("Information"),
    ANDROID_APP_WIDGET("App Widgets")
}

enum class WidgetSize(val cols: Int, val rows: Int, val label: String) {
    SMALL_1X1(1, 1, "1×1"),
    WIDE_2X1(2, 1, "2×1"),
    TALL_1X2(1, 2, "1×2"),
    LARGE_2X2(2, 2, "2×2");

    companion object {
        fun fromSpan(cols: Int, rows: Int): WidgetSize =
            entries.firstOrNull { it.cols == cols && it.rows == rows } ?: SMALL_1X1
    }
}

@Serializable
enum class BuiltInWidgetType(
    val id: String,
    val title: String,
    val category: WidgetCategory,
    val defaultSize: WidgetSize,
    val description: String
) {
    CLOCK("clock", "Clock", WidgetCategory.SYSTEM, WidgetSize.WIDE_2X1, "Digital cover clock"),
    DATE("date", "Date", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Day & date"),
    BATTERY("battery", "Battery", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Charge level & status"),
    STORAGE("storage", "Storage", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Internal storage usage"),
    RAM("ram", "Memory", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Available RAM"),
    NETWORK("network", "Network", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Wi-Fi & cellular link"),
    WIFI("wifi", "Wi-Fi", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Wi-Fi status & panel"),
    BLUETOOTH("bluetooth", "Bluetooth", WidgetCategory.SYSTEM, WidgetSize.SMALL_1X1, "Bluetooth status"),

    CALENDAR("calendar", "Agenda", WidgetCategory.PRODUCTIVITY, WidgetSize.WIDE_2X1, "Upcoming events"),
    TASKS("tasks", "Reminders", WidgetCategory.PRODUCTIVITY, WidgetSize.WIDE_2X1, "Quick tasks & reminders"),
    NOTES("notes", "Quick Note", WidgetCategory.PRODUCTIVITY, WidgetSize.WIDE_2X1, "Pinned cover note"),
    STOPWATCH("stopwatch", "Stopwatch", WidgetCategory.PRODUCTIVITY, WidgetSize.SMALL_1X1, "Live lap stopwatch"),
    TIMER("timer", "Timer", WidgetCategory.PRODUCTIVITY, WidgetSize.SMALL_1X1, "Countdown timer"),
    CALCULATOR("calculator", "Calculator", WidgetCategory.PRODUCTIVITY, WidgetSize.LARGE_2X2, "Compact cover calculator"),

    MUSIC_PLAYER("music_player", "Now Playing", WidgetCategory.ENTERTAINMENT, WidgetSize.WIDE_2X1, "Media controls & art"),
    VOLUME("volume", "Volume", WidgetCategory.ENTERTAINMENT, WidgetSize.SMALL_1X1, "Media & ringer volume"),

    WEATHER("weather", "Weather", WidgetCategory.INFORMATION, WidgetSize.WIDE_2X1, "Current conditions & hi/lo"),
    NEWS("news", "Headlines", WidgetCategory.INFORMATION, WidgetSize.WIDE_2X1, "Tech & world brief"),
    CURRENCY("currency", "FX Rates", WidgetCategory.INFORMATION, WidgetSize.SMALL_1X1, "USD / EUR / INR"),
    STOCKS("stocks", "Watchlist", WidgetCategory.INFORMATION, WidgetSize.WIDE_2X1, "Market tickers");

    companion object {
        fun fromId(id: String): BuiltInWidgetType? = entries.firstOrNull { it.id == id }
    }
}
