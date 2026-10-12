package com.radiostic.app.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("radiostic_prefs", Context.MODE_PRIVATE)

    var currentPlayingId: Long
        get() = prefs.getLong(KEY_CURRENT_PLAYING_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_PLAYING_ID, value).apply()

    var selectedFont: String
        get() = prefs.getString(KEY_SELECTED_FONT, "System Font") ?: "System Font"
        set(value) = prefs.edit().putString(KEY_SELECTED_FONT, value).apply()

    var customFontFilePath: String?
        get() = prefs.getString(KEY_CUSTOM_FONT_FILE_PATH, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_FONT_FILE_PATH, value).apply()

    var customFontFileName: String?
        get() = prefs.getString(KEY_CUSTOM_FONT_FILE_NAME, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_FONT_FILE_NAME, value).apply()

    var customFontListJson: String
        get() = prefs.getString(KEY_CUSTOM_FONT_LIST_JSON, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_FONT_LIST_JSON, value).apply()

    var useDynamicColor: Boolean
        get() = prefs.getBoolean(KEY_USE_DYNAMIC_COLOR, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_DYNAMIC_COLOR, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "Dark") ?: "Dark"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var animationStyle: String
        get() = prefs.getString(KEY_ANIMATION_STYLE, "Expressive") ?: "Expressive"
        set(value) = prefs.edit().putString(KEY_ANIMATION_STYLE, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var cornerRoundness: String
        get() = prefs.getString(KEY_CORNER_ROUNDNESS, "Default") ?: "Default"
        set(value) = prefs.edit().putString(KEY_CORNER_ROUNDNESS, value).apply()

    var density: String
        get() = prefs.getString(KEY_DENSITY, "Comfortable") ?: "Comfortable"
        set(value) = prefs.edit().putString(KEY_DENSITY, value).apply()

    var textSize: String
        get() = prefs.getString(KEY_TEXT_SIZE, "Default") ?: "Default"
        set(value) = prefs.edit().putString(KEY_TEXT_SIZE, value).apply()

    var boldText: Boolean
        get() = prefs.getBoolean(KEY_BOLD_TEXT, false)
        set(value) = prefs.edit().putBoolean(KEY_BOLD_TEXT, value).apply()

    var visualizerMode: String
        get() = prefs.getString(KEY_VISUALIZER_MODE, "Bar") ?: "Bar"
        set(value) = prefs.edit().putString(KEY_VISUALIZER_MODE, value).apply()

    companion object {
        private const val KEY_CURRENT_PLAYING_ID = "key_current_playing_id"
        private const val KEY_SELECTED_FONT = "key_selected_font"
        private const val KEY_CUSTOM_FONT_FILE_PATH = "key_custom_font_file_path"
        private const val KEY_CUSTOM_FONT_FILE_NAME = "key_custom_font_file_name"
        private const val KEY_CUSTOM_FONT_LIST_JSON = "key_custom_font_list_json"
        private const val KEY_USE_DYNAMIC_COLOR = "key_use_dynamic_color"
        private const val KEY_THEME_MODE = "key_theme_mode"

        private const val KEY_ANIMATION_STYLE = "key_animation_style"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_CORNER_ROUNDNESS = "key_corner_roundness"
        private const val KEY_DENSITY = "key_density"
        private const val KEY_TEXT_SIZE = "key_text_size"
        private const val KEY_BOLD_TEXT = "key_bold_text"
        private const val KEY_VISUALIZER_MODE = "key_visualizer_mode"
    }
}
