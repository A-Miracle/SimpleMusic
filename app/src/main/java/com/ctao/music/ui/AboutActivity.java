package com.ctao.music.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.baselib.utils.ViewUtils;
import com.ctao.music.BuildConfig;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/7/4.
 */
public class AboutActivity extends MvpActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected Toolbar getBackToolBar() {
        return toolbar;
    }

    @Override
    protected boolean onImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 第一步, 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // toolbar 加 高度 加 Margin
            int statusBarHeight = ViewUtils.getStatusBar(this);
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            params.height += statusBarHeight;

            toolbar.setPadding(0, statusBarHeight, 0, 0);
        }
        return true;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        getFragmentManager().beginTransaction().replace(R.id.fl_container, new AboutFragment()).commit();
    }

    public static class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference mVersion;
        private Preference mUpdate;
        private Preference mShare;
        private Preference mStar;
        private Preference mGitHub;
        private Preference mReward;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_about);

            mVersion = findPreference("version");
            mUpdate = findPreference("update");
            mShare = findPreference("share");
            mStar = findPreference("star");
            mGitHub = findPreference("github");
            mReward = findPreference("reward");

            mVersion.setSummary("v " + BuildConfig.VERSION_NAME);

            mUpdate.setOnPreferenceClickListener(this);
            mShare.setOnPreferenceClickListener(this);
            mStar.setOnPreferenceClickListener(this);
            mGitHub.setOnPreferenceClickListener(this);
            mReward.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()){
                case "update":
                    ToastUtils.show("功能暂未实现, 敬请期待!");
                    break;
                case "share":
                    share();
                    break;
                case "star":
                    openUrl("https://github.com/A-Miracle/SimpleMusic");
                    break;
                case "github":
                    openUrl(preference.getSummary().toString());
                    break;
                case "reward":
                    reward();
                    break;
            }
            return true;
        }

        private void share() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "可可音乐...");
            startActivity(Intent.createChooser(intent, "分享"));
        }

        private void openUrl(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }

        private void reward() {
            new MaterialDialog.Builder(getActivity())
                    .theme(Theme.LIGHT)
                    .title("打赏作者")
                    .content("点击打赏按钮，作者支付宝账号便会自动复制到剪贴板上，此时您可以通过手机支付宝打赏作者。作者跪谢～")
                    .positiveText("打赏")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                            // 将文本复制到剪贴板
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("data", "1243679197@qq.com"));
                            ToastUtils.show("已复制到剪贴板");

                            startAliPay();
                        }

                        private void startAliPay() {
                            Intent intent = getActivity().getPackageManager().
                                    getLaunchIntentForPackage("com.eg.android.AlipayGphone");
                            if (intent != null) {
                                getActivity().startActivity(intent);
                            }
                        }
                    })
                    .show();
        }
    }
}
