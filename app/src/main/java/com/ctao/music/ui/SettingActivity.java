package com.ctao.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.appthemeengine.util.Util;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.App;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.ui.common.CommonActivity;
import com.ctao.music.ui.fragment.PendantSnowSettingFragment;
import com.ctao.music.ui.fragment.SkinFragment;
import com.ctao.music.ui.prefs.ATEPreference;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/7/21.
 */
public class SettingActivity extends MvpActivity implements ColorChooserDialog.ColorCallback{
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected Toolbar getBackToolBar() {
        return toolbar;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
//        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.fl_container, new SettingsFragment()).commit();
//        } else {
//            SettingsFragment frag = (SettingsFragment) getFragmentManager().findFragmentById(R.id.fl_container);
//            if (frag != null) frag.invalidateSettings();
//        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final Config config = ATE.config(this, getATEKey());
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                config.primaryColor(selectedColor);
                if(selectedColor != Color.TRANSPARENT){
                    int colorDark = Util.darkenColor(selectedColor);
                    config.navigationViewSelectedIcon(colorDark)
                            .navigationViewSelectedText(colorDark);
                }else{
                    // 设置为默认主题色
                    if(getActivityTheme() == R.style.AppTheme){
                        config.navigationViewSelectedIconRes(R.color.colorPrimaryDark)
                                .navigationViewSelectedTextRes(R.color.colorPrimaryDark);
                    }else {
                        config.navigationViewSelectedIconRes(R.color.colorPrimaryDark_Dark)
                                .navigationViewSelectedTextRes(R.color.colorPrimaryDark_Dark);
                    }
                }
                break;
            case R.string.accent_color:
                config.accentColor(selectedColor);
                break;
            case R.string.primary_text_color:
                config.textColorPrimary(selectedColor);
                break;
            case R.string.secondary_text_color:
                config.textColorSecondary(selectedColor);
                break;
        }
        config.commit();
        recreate(); // recreation needed to reach the checkboxes in the preferences layout
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    public static class SettingsFragment extends PreferenceFragment {
        private String mAteKey;
        private ATEPreference pendant_snow;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // 是这样的, 我的魅族note5 Android 6.0有点问题, 所以暂时屏蔽Android 5.0以上的挂件
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                addPreferencesFromResource(R.xml.preference_setting_pendant);
            }else{
                addPreferencesFromResource(R.xml.preference_setting);
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
        }

        private void invalidateSettings() {
            mAteKey = ((SettingActivity) getActivity()).getATEKey();
            findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // 标志着这两个主题配置改变MainActivity重启本身在返回
                    Config.markChanged(getActivity(), "light_theme");
                    Config.markChanged(getActivity(), "dark_theme");
                    // dark_theme偏好值被Android在默认PreferenceManager保存。
                    // 它是用于getATEKey()的活动。
                    getActivity().recreate();
                    return true;
                }
            });

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingActivity) getActivity(), R.string.primary_color)
                            .preselect(Config.primaryColor(getActivity(), mAteKey))
                            .doneButton(R.string.color_done)
                            .cancelButton(R.string.color_cancel)
                            .backButton(R.string.color_back)
                            .customButton(R.string.color_custom)
                            .presetsButton(R.string.color_presets)
                            .show();
                    return true;
                }
            });
            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
            accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingActivity) getActivity(), R.string.accent_color)
                            .preselect(Config.accentColor(getActivity(), mAteKey))
                            .doneButton(R.string.color_done)
                            .cancelButton(R.string.color_cancel)
                            .backButton(R.string.color_back)
                            .customButton(R.string.color_custom)
                            .presetsButton(R.string.color_presets)
                            .show();
                    return true;
                }
            });

            ATEColorPreference textColorPrimaryPref = (ATEColorPreference) findPreference("text_primary");
            textColorPrimaryPref.setColor(Config.textColorPrimary(getActivity(), mAteKey), Color.BLACK);
            textColorPrimaryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingActivity) getActivity(), R.string.primary_text_color)
                            .preselect(Config.textColorPrimary(getActivity(), mAteKey))
                            .doneButton(R.string.color_done)
                            .cancelButton(R.string.color_cancel)
                            .backButton(R.string.color_back)
                            .customButton(R.string.color_custom)
                            .presetsButton(R.string.color_presets)
                            .show();
                    return true;
                }
            });

            ATEColorPreference textColorSecondaryPref = (ATEColorPreference) findPreference("text_secondary");
            textColorSecondaryPref.setColor(Config.textColorSecondary(getActivity(), mAteKey), Color.BLACK);
            textColorSecondaryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingActivity) getActivity(), R.string.secondary_text_color)
                            .preselect(Config.textColorSecondary(getActivity(), mAteKey))
                            .doneButton(R.string.color_done)
                            .cancelButton(R.string.color_cancel)
                            .backButton(R.string.color_back)
                            .customButton(R.string.color_custom)
                            .presetsButton(R.string.color_presets)
                            .show();
                    return true;
                }
            });

            ATEPreference reset = (ATEPreference) findPreference("reset");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(getActivity())
                            .title("重置颜色")
                            .content("重置以上自定义颜色为系统默认")
                            .negativeText("取消")
                            .positiveText("确认")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    switch (which) {
                                        case POSITIVE: // 重置颜色
                                            App.getApp().defaultLightTheme();
                                            App.getApp().defaultDarkTheme();
                                            getActivity().recreate();
                                            break;
                                    }
                                }
                            })
                            .show();
                    return true;
                }
            });

            ATEPreference skin = (ATEPreference) findPreference("skin");
            skin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().startActivity(new Intent(getActivity(), CommonActivity.class)
                            .putExtra(CommonActivity.TYPE_FRAGMENT, SkinFragment.class));
                    return true;
                }
            });

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                pendant_snow = (ATEPreference) findPreference("pendant_snow");
                refreshView();
                pendant_snow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        getActivity().startActivity(new Intent(getActivity(), CommonActivity.class)
                                .putExtra(CommonActivity.TYPE_FRAGMENT, PendantSnowSettingFragment.class));
                        return true;
                    }
                });
            }

        }

        public void refreshView(){
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                String pendant = SPUtils.getString(Constant.SP_PENDANT, null);
                if(Constant.PENDANT_SNOW.equals(pendant)){
                    pendant_snow.setSummary(R.string.pendant_snow_summary_on);
                }else{
                    pendant_snow.setSummary(R.string.pendant_snow_summary_off);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            SettingsFragment frag = (SettingsFragment) getFragmentManager().findFragmentById(R.id.fl_container);
            if (frag != null){
                frag.refreshView();
            }
        }
    }
}
