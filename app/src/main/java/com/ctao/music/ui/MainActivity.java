package com.ctao.music.ui;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ctao.baselib.utils.DateUtils;
import com.ctao.baselib.utils.FileUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.baselib.utils.ViewUtils;
import com.ctao.music.BuildConfig;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.interact.contract.IUpdateContract;
import com.ctao.music.interact.contract.UpdatePresenter;
import com.ctao.music.interact.model.Update;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.ui.common.CommonActivity;
import com.ctao.music.ui.fragment.ScanFragment;
import com.ctao.music.utils.OtherPendantUtils;
import com.ctao.music.utils.UriUtils;

import java.io.File;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 可可音乐
 */
public class MainActivity extends MvpActivity implements IUpdateContract.View {

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.app_bar) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tv_menu_right_1) TextView tv_menu_right_1;

    ActionBarDrawerToggle mDrawerToggle;
    private IUpdateContract.Presenter mPresenter;
    private MaterialDialog mProgress;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean onImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 第一步, 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // toolbar 加 高度 加 Margin
            int statusBarHeight = ViewUtils.getStatusBar(this);
            ViewGroup.LayoutParams params = mToolbar.getLayoutParams();
            params.height += statusBarHeight;

            mToolbar.setPadding(0, statusBarHeight, 0, 0);
        }
        return true;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        mPresenter = new UpdatePresenter(this);
        setSwipeBackEnable(false);
        initNavigationMenu();
        initMenuRight();
        mAppBarLayout.setBackgroundColor(themeColor);

        deleteHasInstalledPackage();
        String nowDate = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        String oldDate = SPUtils.getString(Constant.SP_LATEST_DATE, "");
        if(!nowDate.equals(oldDate)){ // 每天一次检查更新
            SPUtils.putObject(Constant.SP_LATEST_DATE, nowDate);
            mPresenter.checkUpdate();
        }
    }

    // 初始化Toolbar右侧子菜单
    private void initMenuRight() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/iconfont.ttf");
        tv_menu_right_1.setTypeface(font);
        tv_menu_right_1.setText(getString(R.string.title_scan_icon));
        tv_menu_right_1.setVisibility(View.VISIBLE);
    }

    // 初始化侧滑菜单
    private void initNavigationMenu() {
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, 0, 0);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.tv_menu_right_1)
    public void scanSong(){
        startActivity(new Intent(MainActivity.this, CommonActivity.class)
        .putExtra(CommonActivity.TYPE_FRAGMENT, ScanFragment.class));
    }

    long[] mHits = new long[2]; //设置为双击事件, 3为三击, 4...
    @Override
    public void onBackPressed() {
        // 如果DrawerLayout处于打开状态, 先关闭
        int drawerLockMode = mDrawerLayout.getDrawerLockMode(GravityCompat.START);
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START) && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1); // 数组的值向左移一位
        mHits[mHits.length - 1] = SystemClock.uptimeMillis(); // 距离开机的时间
        if (mHits[0] >= (SystemClock.uptimeMillis() - 1500)) {
            mHits[0] = 0;// 保险起见,摧毁数组第一个元素的值
            ToastUtils.cancel();
            super.onBackPressed();
        }else{
            ToastUtils.show("再次点击返回键切换到桌面");
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(null != mDrawerToggle){
            mDrawerLayout.removeDrawerListener(mDrawerToggle);
        }
        super.onDestroy();
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        super.onMessageEvent(event);
        switch (event.getType()){
            case MessageEvent.MUSIC_CLOSE_DRAWERS:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case MessageEvent.MUSIC_UPDATE_PENDANT:
                ViewGroup mContentView = (ViewGroup) getContentView();
                View pendantView = mContentView.findViewById(R.id.other_pendant);
                if (pendantView != null) {
                    mContentView.removeView(pendantView);
                }
                OtherPendantUtils.addPendant(this, mContentView);
                break;
        }
    }

    @Override
    protected void initOtherOnCreateInLast() {
        OtherPendantUtils.addPendant(this, getContentView());
    }

    //---

    private void deleteHasInstalledPackage() {
        int code = SPUtils.getInt(Constant.SP_LATEST_CODE, -1);
        if(code == -1){
            return;
        }
        if(BuildConfig.VERSION_CODE >= code){ // 已安装最新, 清空apk
            File dir = FileUtils.getExternalFilesDir(Constant.FILE_APK);
            if(dir != null && dir.exists() && dir.isDirectory()){
                for (File file : dir.listFiles()) {
                    if(file != null && file.exists() && file.isFile()){
                        file.delete();
                    }
                }
            }
            SPUtils.putObject(Constant.SP_LATEST_CODE, -1);
        }
    }

    @Override
    public void checkUpdate(final Update update) {
        if(update == null){
            return;
        }

        if(BuildConfig.VERSION_CODE >= update.versionCode){
            return;
        }

        SPUtils.putObject(Constant.SP_LATEST_CODE, update.versionCode); // 存储最新包Code, 用于安装后删除

        new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title("发现新版本：" + update.versionName)
                .content(update.detail)
                .negativeText("取消")
                .positiveText("下载Apk")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE: // 下载Apk
                                File file = new File(FileUtils.getExternalFilesDir(Constant.FILE_APK) + File.separator + update.fileName);
                                if (file != null && file.exists()) {
                                    installApk(file); // 下载过了, 直接安装
                                }else {
                                    mPresenter.downloadApk(update.fileName);
                                }
                                break;
                        }

                    }
                })
                .show();
    }

    private void installApk(File file) {
        if(file != null){ // 启用安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT >= 24){
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(UriUtils.fromFile(file), "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    @Override
    public void downloadComplete(File file) {
        if(mProgress != null){
            mProgress.dismiss();
            mProgress = null;
        }
        if(file == null){
            return;
        }
        ToastUtils.show("下载完成");
        installApk(file);
    }

    @Override
    public void downloadProgress(int progress) {
        if (mProgress == null) {
            mProgress = new MaterialDialog.Builder(this)
                    .content("下载中...")
                    .progress(true, 0)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .progressIndeterminateStyle(true)
                    .build();
            mProgress.show();
        }
        mProgress.setProgress(progress);
    }

    @Override
    public void showFailure(String msg, String... tag) {
        if(tag.length > 0 && "checkUpdate".equals(tag[0])){
            return;
        }
        super.showFailure(msg, tag);
        if(tag.length > 0 && "downloadApk".equals(tag[0])){
            if(mProgress != null){
                mProgress.dismiss();
                mProgress = null;
            }
        }
    }
}
