package com.ctao.music.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ctao.baselib.utils.BarUtils;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;

import java.util.Calendar;

import butterknife.BindView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 可可音乐
 */
@RuntimePermissions
public class SplashActivity extends MvpActivity {
    @BindView(R.id.tv_copyright) TextView tvCopyright;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initSkin() {
        //根据SwipeBackActivity的设置, DecorView 不能有背景, WindowBackground也应为透明
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_splash);
        getContentView().setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        setSwipeBackEnable(false);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(getString(R.string.copyright, year));

        loadData();
    }

    @Override
    protected boolean onImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        return true;
    }

    @Override
    protected boolean onImmersiveNavBar() {
        BarUtils.setNavigationBarColor(this, Color.BLACK);
        return true;
    }

    // 加载数据, 此处模拟延时
    private void loadData() {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... strings) {
                SystemClock.sleep(2200);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                SplashActivityPermissionsDispatcher.goHomeWithPermissionCheck(SplashActivity.this);
            }
        }.execute();
    }

    /**
     * 跳转到主页面
     */
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    protected void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() { }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        // 解释为什么需要许可。它传入一个PermissionRequest对象，可以用于在用户输入时继续或中止当前的权限请求
        new MaterialDialog.Builder(this)
                .title("权限申请")
                .content("可可音乐需要一些必须的权限, 请熟知")
                .positiveText("继续")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                request.proceed();
                                break;
                        }
                    }
                })
                .cancelable(false)
                .show();
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        // 如果用户没有授予权限，则调用该方法
        new MaterialDialog.Builder(this)
                .title("权限异常")
                .content("请重新打开应用并允许权限")
                .positiveText("我知道了")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                finish();
                                break;
                        }
                    }
                })
                .cancelable(false)
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        // 如果用户选择让设备“不再询问”一个权限，则调用该方法
        new MaterialDialog.Builder(this)
                .title("权限异常")
                .content("请重新进入应用设置开启相关权限, 否则应用可能无法正常运行")
                .positiveText("我知道了")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                finish();
                                break;
                        }
                    }
                })
                .cancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将权限处理委托给生成的方法
        SplashActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
