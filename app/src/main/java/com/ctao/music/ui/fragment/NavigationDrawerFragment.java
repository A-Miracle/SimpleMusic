package com.ctao.music.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ctao.baselib.manager.AppManager;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.AppConfig;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.service.MediaPlayerService;
import com.ctao.music.ui.AboutActivity;
import com.ctao.music.ui.SettingActivity;
import com.ctao.music.ui.base.MvpFragment;
import com.ctao.music.utils.ATEUtils;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/6/25.
 * 左侧侧滑菜单
 */
public class NavigationDrawerFragment extends MvpFragment implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.ll_head) LinearLayout ll_head;
    @BindView(R.id.navigation_view)
    NavigationView navigation_view;
    NavigationMenuView menuView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_navigation_drawer;
    }

    @Override
    protected void initView() {
        menuView = (NavigationMenuView) navigation_view.findViewById(R.id.design_navigation_view);
        navigation_view.post(new Runnable() {
            @Override
            public void run() {
                // 很奇怪, xml中设置无效
                navigation_view.setPadding(0, 0, 0, 0);

                // 魅族note5的NavigationMenuView布局还多出个paddingTop, 不知道是不是Android6.0的问题
                if (null != menuView) {
                    menuView.setPadding(0, 0, 0, 0);
                }
            }
        });
        navigation_view.setNavigationItemSelectedListener(this);

        String ateKey = ATEUtils.getATEKey();
        int primaryColor = Config.primaryColor(getContext(), ateKey);
        int primaryColorDark = Config.primaryColorDark(getContext(), ateKey);
        if(primaryColor != Color.TRANSPARENT){// 只要不是全透明, 它这里取的是RGB值
            ll_head.setBackgroundColor(primaryColorDark);
        }else{
            if(ATEUtils.getActivityTheme() == R.style.AppTheme){
                ll_head.setBackgroundResource(R.color.colorPrimaryDark);
            }else{
                ll_head.setBackgroundResource(R.color.colorPrimaryDark_Dark);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean isNotice = true;
        switch (item.getItemId()){
            case R.id.item_myMusic:
                break;
            case R.id.item_mySongList:
                break;
            case R.id.item_folder:
                break;
            case R.id.item_like:
                break;
            case R.id.item_record:
                break;
            case R.id.item_recently_added:
                break;
            case R.id.item_playOut:
                break;
            case R.id.item_setting:
                getActivity().startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case R.id.item_exit:
                isNotice = false;
                exitConfirm();
                break;
            case R.id.item_about:
                getActivity().startActivity(new Intent(getContext(), AboutActivity.class));
                break;
        }
        if(isNotice){
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_CLOSE_DRAWERS));
        }
        return true;
    }

    private void exitConfirm() {
        boolean flag = SPUtils.getBoolean(Constant.SP_EXIT_FLAG, false);
        if(!flag){
            new MaterialDialog.Builder(getActivity())
                    .title("退出可可音乐")
                    .content("退出后, 可可音乐将停止本次运行")
                    .negativeText("取消")
                    .positiveText("确认")
                    .checkBoxPrompt("不再提醒", false, null)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case POSITIVE:
                                    exit();
                                    break;
                            }
                            SPUtils.putObject(Constant.SP_EXIT_FLAG, dialog.isPromptCheckBoxChecked());
                        }
                    })
                    .show();
        }else{
            exit();
        }
    }

    private void exit() {
        AppConfig.APP_CLOSE = true;
        getActivity().stopService(new Intent(getContext(), MediaPlayerService.class));
        AppManager.getInstance().exitApp(true);
    }
}
