package com.ctao.music.utils;

import android.preference.PreferenceManager;

import com.ctao.baselib.Global;
import com.ctao.music.R;

/**
 * Created by A Miracle on 2017/7/26.
 */
public class ATEUtils {

    public static String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getContext()).getBoolean("dark_theme", false)
                ? "dark_theme" : "light_theme";
    }

    public static int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getContext()).getBoolean("dark_theme", false)
                ? R.style.AppThemeDark : R.style.AppTheme;
    }
}
