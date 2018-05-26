/*
 * Copyright 2018 Mr Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.duy.ide.editor.editor.R;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.StringUtils;
import com.jecelyin.common.utils.SysUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_FONT_SIZE = "pref_font_size";
    public static final String KEY_CURSOR_WIDTH = "pref_cursor_width";
    public static final String KEY_TOUCH_TO_ADJUST_TEXT_SIZE = "pref_touch_to_adjust_text_size";
    public static final String KEY_WORD_WRAP = "pref_word_wrap";
    public static final String KEY_SHOW_LINE_NUMBER = "pref_show_linenumber";
    public static final String KEY_SHOW_WHITESPACE = "pref_show_whitespace";
    public static final String KEY_AUTO_INDENT = "pref_auto_indent";
    public static final String KEY_INSERT_SPACE_FOR_TAB = "pref_insert_space_for_tab";
    public static final String KEY_TAB_SIZE = "pref_tab_size";
    public static final String KEY_SYMBOL = "pref_symbol";
    public static final String KEY_AUTO_CAPITALIZE = "pref_auto_capitalize";
    public static final String KEY_HIGHLIGHT_FILE_SIZE_LIMIT = "pref_highlight_file_size_limit";
    public static final String KEY_REMEMBER_LAST_OPENED_FILES = "pref_remember_last_opened_files";
    public static final String KEY_SCREEN_ORIENTATION = "pref_screen_orientation";
    public static final String KEY_KEEP_SCREEN_ON = "pref_keep_screen_on";
    public static final String KEY_TOOLBAR_ICONS = "pref_toolbar_icons";
    public static final String KEY_PREF_AUTO_CHECK_UPDATES = "pref_auto_check_updates";
    public static final String KEY_LAST_OPEN_PATH = "last_open_path";
    public static final String KEY_READ_ONLY = "readonly_mode";
    public static final String KEY_SHOW_HIDDEN_FILES = "show_hidden_files";
    public static final String KEY_FILE_SORT_TYPE = "show_file_sort";
    public static final String KEY_FULL_SCREEN = "fullscreen_mode";
    public static final String KEY_LAST_TAB = "last_tab";

    public static final int DEF_MIN_FONT_SIZE = 9;
    public static final int DEF_MAX_FONT_SIZE = 32;

    public static final int SCREEN_ORIENTATION_AUTO = 0;
    public static final int SCREEN_ORIENTATION_LANDSCAPE = 1;
    public static final int SCREEN_ORIENTATION_PORTRAIT = 2;
    public static final String VALUE_SYMBOL = TextUtils.join("\n", new String[]{"{", "}", "<", ">"
            , ",", ";", "'", "\"", "(", ")", "/", "\\", "%", "[", "]", "|", "#", "=", "$", ":"
            , "&", "?", "!", "@", "^", "+", "*", "-", "_", "`", "\\t", "\\n"});

    public static final int[] THEMES = new int[]{
            R.style.LightTheme,
            R.style.DarkTheme
    };

    private static final Object mContent = new Object();
    private static Preferences instance;

    static {
    }

    private final SharedPreferences preferences;

    private final Map<String, Object> map;
    private final Context context;
    private final WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    private Set<String> toolbarIcons;

    public Preferences(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        map = new HashMap<>();
        map.put(KEY_FONT_SIZE, 13);
        map.put(KEY_CURSOR_WIDTH, 2);
        map.put(KEY_TOUCH_TO_ADJUST_TEXT_SIZE, false);
        map.put(KEY_WORD_WRAP, true);
        map.put(KEY_SHOW_LINE_NUMBER, true);
        map.put(KEY_SHOW_WHITESPACE, true);
        map.put(KEY_AUTO_INDENT, true);
        map.put(KEY_INSERT_SPACE_FOR_TAB, true);
        map.put(KEY_TAB_SIZE, 4);
        map.put(KEY_SYMBOL, VALUE_SYMBOL);
        map.put(KEY_AUTO_CAPITALIZE, false);
        map.put(KEY_HIGHLIGHT_FILE_SIZE_LIMIT, 500);
        map.put(KEY_REMEMBER_LAST_OPENED_FILES, true);
        map.put(KEY_SCREEN_ORIENTATION, "auto");
        map.put(KEY_KEEP_SCREEN_ON, false);
        map.put(KEY_PREF_AUTO_CHECK_UPDATES, true);

        toolbarIcons = preferences.getStringSet(KEY_TOOLBAR_ICONS, null);
        map.put(KEY_LAST_OPEN_PATH, Environment.getExternalStorageDirectory().getPath());
        map.put(KEY_READ_ONLY, false);
        map.put(KEY_SHOW_HIDDEN_FILES, false);
        map.put(KEY_FILE_SORT_TYPE, 0);
        map.put(KEY_FULL_SCREEN, false);
        map.put(KEY_LAST_TAB, 0);

        Map<String, ?> values = preferences.getAll();
        for (String key : map.keySet()) {
            updateValue(key, values);
        }
    }

    public static Preferences getInstance(Context context) {
        if (instance == null) {
            instance = new Preferences(context.getApplicationContext());
        }
        return instance;
    }

    public static String getGoogleDriveKey() {
        return null; //drive_key
    }

    public static String getGoogleDriveSecret() {
        return null; //drive_key
    }

    public static String getBoxAPIKey() {
        return null; // TODO: 16/1/2
    }

    public static String getBoxApiSecret() {
        return null;
    }

    public int getMaxEditor() {
        return 3;
    }

    private void updateValue(String key, Map<String, ?> values) {
        Object value = map.get(key);
        if (value == null)
            return;
        Class cls = value.getClass();

        try {
            if (cls == int.class || cls == Integer.class) {
                Object in = values.get(key);
                if (in != null)
                    value = in instanceof Integer ? (int) in : StringUtils.toInt(String.valueOf(in));
            } else if (cls == boolean.class || cls == Boolean.class) {
                Boolean b = (Boolean) values.get(key);
                value = b == null ? (boolean) value : b;
            } else {
                String str = (String) values.get(key);
                value = str == null ? (String) value : str;
            }
        } catch (Exception e) {
            DLog.e("key = " + key, e);
            return;
        }
        map.put(key, value);
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.put(listener, mContent);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.remove(listener);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateValue(key, sharedPreferences.getAll());
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = mListeners.keySet();
        for (SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
            if (listener != null) {
                listener.onSharedPreferenceChanged(sharedPreferences, key);
            }
        }
    }

    public boolean isShowLineNumber() {
        return (boolean) map.get(KEY_SHOW_LINE_NUMBER);
    }

    public boolean isShowWhiteSpace() {
        return (boolean) map.get(KEY_SHOW_WHITESPACE);
    }

    public int getTheme() {
        return getInt(context.getString(R.string.pref_current_theme), 0);
    }

    /**
     * theme index of {@link #THEMES}
     */
    public void setTheme(int theme) {
        preferences.edit().putInt(context.getString(R.string.pref_current_theme), theme).apply();
    }

    public int getHighlightSizeLimit() {
        return 1024 * (int) map.get(KEY_HIGHLIGHT_FILE_SIZE_LIMIT);
    }

    //auto save is default
    public boolean isAutoSave() {
        return getBoolean(context.getString(R.string.pref_auto_save), true);
    }

    private boolean getBoolean(String key, boolean def) {
        try {
            return preferences.getBoolean(key, def);
        } catch (ClassCastException e) {
            return Boolean.parseBoolean(preferences.getString(key, String.valueOf(def)));
        }
    }

    private int getInt(String key, int def) {
        try {
            return preferences.getInt(key, def);
        } catch (ClassCastException e) {
            return Integer.parseInt(preferences.getString(key, String.valueOf(def)));
        }
    }

    public boolean isKeepScreenOn() {
        return (boolean) map.get(KEY_KEEP_SCREEN_ON);
    }

    public Integer[] getToolbarIcons() {
        if (toolbarIcons == null)
            return null;
        Integer[] list = new Integer[toolbarIcons.size()];
        int i = 0;
        for (String id : toolbarIcons) {
            list[i++] = Integer.valueOf(id);
        }
        return list;
    }

    public void setToolbarIcons(Integer[] toolbarIcons) {
        this.toolbarIcons = new HashSet<>();
        for (Integer id : toolbarIcons) {
            this.toolbarIcons.add(String.valueOf(id));
        }
        preferences.edit().putStringSet(KEY_TOOLBAR_ICONS, this.toolbarIcons).apply();
    }

    public Object getValue(String key) {
        return map.get(key);
    }

    public String getLastOpenPath() {
        return (String) map.get(KEY_LAST_OPEN_PATH);
    }

    public void setLastOpenPath(String path) {
        preferences.edit().putString(KEY_LAST_OPEN_PATH, path).apply();
        map.put(KEY_LAST_OPEN_PATH, path);
    }

    public int getFontSize() {
        return (int) map.get(KEY_FONT_SIZE);
    }

    public int getCursorThickness() {
        int width = (int) map.get(KEY_CURSOR_WIDTH);
        if (width == 0)
            return 0;

        return SysUtils.dpAsPixels(context, width);
    }

    public boolean isReadOnly() {
//        return (boolean) map.get(KEY_READ_ONLY);
        return false;
    }

    public void setReadOnly(boolean b) {
        preferences.edit().putBoolean(KEY_READ_ONLY, b).apply();
        map.put(KEY_READ_ONLY, b);
    }

    public boolean isAutoIndent() {
        return (boolean) map.get(KEY_AUTO_INDENT);
    }

    public boolean isWordWrap() {
        return (boolean) map.get(KEY_WORD_WRAP);
    }

    public boolean isTouchScaleTextSize() {
        return (boolean) map.get(KEY_TOUCH_TO_ADJUST_TEXT_SIZE);
    }

    public boolean isAutoCapitalize() {
        return (boolean) map.get(KEY_AUTO_CAPITALIZE);
    }


    public boolean isOpenLastFiles() {
        return (boolean) map.get(KEY_REMEMBER_LAST_OPENED_FILES);
    }

    public int getTabSize() {
        return (int) map.get(KEY_TAB_SIZE);
    }

    @ScreenOrientation
    public int getScreenOrientation() {
        String ori = (String) map.get(KEY_SCREEN_ORIENTATION);
        if ("landscape".equals(ori)) {
            return SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equals(ori)) {
            return SCREEN_ORIENTATION_PORTRAIT;
        } else {
            return SCREEN_ORIENTATION_AUTO;
        }
    }

    public String getSymbol() {
        return (String) map.get(KEY_SYMBOL);
    }

    public boolean isShowHiddenFiles() {
        return (boolean) map.get(KEY_SHOW_HIDDEN_FILES);
    }

    public void setShowHiddenFiles(boolean b) {
        preferences.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, b).apply();
        map.put(KEY_SHOW_HIDDEN_FILES, b);
    }

    public int getFileSortType() {
        return (int) map.get(KEY_FILE_SORT_TYPE);
    }

    public void setFileSortType(int type) {
        preferences.edit().putInt(KEY_FILE_SORT_TYPE, type).apply();
        map.put(KEY_FILE_SORT_TYPE, type);
    }

    public boolean isFullScreenMode() {
        return (boolean) map.get(KEY_FULL_SCREEN);
    }

    public void setFullScreenMode(boolean b) {
        preferences.edit().putBoolean(KEY_FULL_SCREEN, b).apply();
        map.put(KEY_FULL_SCREEN, b);
    }

    public int getLastTab() {
        return (int) map.get(KEY_LAST_TAB);
    }

    public void setLastTab(int index) {
        preferences.edit().putInt(KEY_LAST_TAB, index).apply();
        map.put(KEY_LAST_TAB, index);
    }

    public boolean isUseLightTheme() {
        return getTheme() == 0;
    }

    @IntDef({SCREEN_ORIENTATION_AUTO, SCREEN_ORIENTATION_LANDSCAPE, SCREEN_ORIENTATION_PORTRAIT})
    public @interface ScreenOrientation {
    }
}
