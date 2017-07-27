package com.ctao.music;

import android.app.Application;
import android.content.Intent;

import com.afollestad.appthemeengine.ATE;
import com.ctao.baselib.Global;
import com.ctao.baselib.utils.LogUtils;
import com.ctao.music.service.MediaPlayerService;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by A Miracle on 2017/6/25.
 */
public class App extends Application {
    public static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        Global.init(this);
        LogUtils.init(true, "Music", null);
        initTheme();

        // 启动服务
        startService(new Intent(this, MediaPlayerService.class));

        //if(!BuildConfig.BUGLY_DEBUG){
            // 初始化 Bugly [全开]
            CrashReport.initCrashReport(this, "APP ID", BuildConfig.BUGLY_DEBUG);
        //}
    }

    public static App getApp(){
        return mApp;
    }

    private void initTheme() {
        if (!ATE.config(this, "light_theme").isConfigured(1)) {
            defaultLightTheme();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(1)) {
            defaultDarkTheme();
        }
    }

    public void defaultLightTheme() {
        ATE.config(this, "light_theme")
                .activityTheme(R.style.AppTheme)
                .primaryColorRes(R.color.colorPrimary)
                .autoGeneratePrimaryDark(true) //自动生成 colorPrimaryDark
                .accentColorRes(R.color.colorAccent)
                .textColorPrimaryRes(R.color.text_color_primary) //?android:textColorPrimary
                .textColorSecondaryRes(R.color.text_color_secondary) //?android:textColorSecondary
                .coloredNavigationBar(true)
                .usingMaterialDialogs(true)
                .navigationViewThemed(true)
                .navigationViewSelectedIconRes(R.color.colorPrimaryDark)
                .navigationViewSelectedTextRes(R.color.colorPrimaryDark)
                .commit();
    }

    public void defaultDarkTheme() {
        ATE.config(this, "dark_theme")
                .activityTheme(R.style.AppThemeDark)
                .primaryColorRes(R.color.colorPrimary_Dark)
                .autoGeneratePrimaryDark(true)
                .accentColorRes(R.color.colorAccent_Dark)
                .textColorPrimaryRes(R.color.text_color_primary_dark)
                .textColorSecondaryRes(R.color.text_color_secondary_dark)
                .coloredNavigationBar(true)
                .usingMaterialDialogs(true)
                .navigationViewThemed(true)
                .navigationViewSelectedIconRes(R.color.colorPrimaryDark_Dark)
                .navigationViewSelectedTextRes(R.color.colorPrimaryDark_Dark)
                .commit();
    }

    // 设置参考
    /*private void theme(){
        // 上下文和可选配置关键作为参数配置()
        ATE.config(this, null)
                // 0禁用,设置一个默认主题的所有活动使用这个配置的关键
                .activityTheme(R.style.AppTheme)
                // 真正 在默认情况下,颜色支持行动栏和工具栏
                .coloredActionBar(true)
                // 默认为colorPrimary属性值
                .primaryColor(getResources().getColor(R.color.md_amber))
                // 从primaryColor时真的, primaryColorDark是自动生成的
                .autoGeneratePrimaryDark(true)
                // 默认为colorPrimaryDark属性值
                .primaryColorDark(getResources().getColor(R.color.md_lime))
                // 默认为colorAccent属性值
                .accentColor(getResources().getColor(R.color.md_purple))
                // 默认情况下,等于primaryColorDark价值
                .statusBarColor(getResources().getColor(R.color.md_pink))
                // 真正 在默认情况下,设置为false禁用色素即使statusBarColor设置
                .coloredStatusBar(true)
                // 黑暗状态栏图标棉花糖(API 23)+,汽车使用光primaryColor时状态栏模式
                .lightStatusBarMode(com.afollestad.appthemeengine.Config.LIGHT_STATUS_BAR_AUTO)
                // 设置颜色工具栏,默认为primaryColor()值。
                // 这也会正确地应用于CollapsingToolbarLayouts。
                .toolbarColor(getResources().getColor(R.color.md_blue_grey))
                // 在,使工具栏导航图标,标题和菜单图标黑色
                .lightToolbarMode(com.afollestad.appthemeengine.Config.LIGHT_TOOLBAR_AUTO)
                // 默认情况下,等于primaryColor除非coloredNavigationBar是错误的
                .navigationBarColor(getResources().getColor(R.color.md_blue))
                // 假在默认情况下,设置为false禁用色素即使navigationBarColor设置
                .coloredNavigationBar(false)
                // 默认 ?android:textColorPrimary 属性值
                .textColorPrimary(getResources().getColor(R.color.md_blue))
                // 默认 ?android:textColorSecondary 属性值
                .textColorSecondary(getResources().getColor(R.color.md_blue))
                // 真正在默认情况下,设置为false禁用自动下4修饰符的使用。
                .navigationViewThemed(true)
                // 颜色选择NavigationView项图标。默认为你的口音的颜色。
                .navigationViewSelectedIcon(getResources().getColor(R.color.md_blue))
                // 文本的颜色选择NavigationView项目。默认为你的口音的颜色。
                .navigationViewSelectedText(getResources().getColor(R.color.md_blue))
                // 未被选中的颜色NavigationView项图标。默认为材料设计准则的颜色。
                .navigationViewNormalIcon(getResources().getColor(R.color.md_blue))
                // 未被选中的颜色NavigationView项文本。默认为材料设计准则的颜色。
                .navigationViewNormalText(getResources().getColor(R.color.md_blue))
                // 背景的选择NavigationView项目。默认为材料设计准则的颜色。
                .navigationViewSelectedBg(getResources().getColor(R.color.md_blue))
                // 应用程序的目标参数,接受不同的参数类型/计数
                .commit();//.apply(this);
    }*/
}
