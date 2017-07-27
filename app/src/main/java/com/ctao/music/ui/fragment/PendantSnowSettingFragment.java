package com.ctao.music.ui.fragment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.ctao.baselib.Global;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.baselib.widget.snowfall.SnowView;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.ui.common.CommonActivity;
import com.ctao.music.ui.common.CommonFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by A Miracle on 2017/7/24.
 */
public class PendantSnowSettingFragment extends CommonFragment implements View.OnClickListener {
    private final static int MAX_COUNT = 200;

    @BindView(R.id.ll_root) LinearLayout ll_root;
    @BindView(R.id.tv_count) TextView tv_count;
    @BindView(R.id.tv_hint) TextView tv_hint;
    @BindView(R.id.bt_level) Button bt_level;

    private TextView tv_menu_right_1;
    private int level;
    private int count = 50;
    private int state;

    private ViewGroup mContentView;
    private SnowView snowView;
    private boolean change;

    @Override
    public String getTitle() {
        return "挂件 - 飘雪";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pendant_setting;
    }

    @Override
    protected void initView() {
        ATE.apply(ll_root, ((CommonActivity)getActivity()).getATEKey());

        setTvCountText();

        tv_hint.setText("最大限制 暴雪(" + MAX_COUNT + ")");
        if(level == 0){
            bt_level.setText("已置底");
        }else{
            bt_level.setText("已置顶");
        }
    }

    private void setTvCountText() {
        String tv_countStr = "";
        switch (count) {
            case 50:
                tv_countStr = "小雪(" + count + ")";
                break;
            case 100:
                tv_countStr = "中雪(" + count + ")";
                break;
            case 150:
                tv_countStr = "大雪(" + count + ")";
                break;
            case 200:
                tv_countStr = "暴雪(" + count + ")";
                break;
        }
        tv_count.setText(tv_countStr);
    }

    @Override
    public void initMenu(View contentView) {
        tv_menu_right_1 = (TextView) contentView.findViewById(R.id.tv_menu_right_1);
        tv_menu_right_1.setVisibility(View.VISIBLE);
        tv_menu_right_1.setOnClickListener(this);

        String pendant = SPUtils.getString(Constant.SP_PENDANT, null);
        if(Constant.PENDANT_SNOW.equals(pendant)){// 已开启
            tv_menu_right_1.setText("已开启");
            state = 1;
        }else{
            tv_menu_right_1.setText("已关闭");
            state = 0;
        }
    }

    // 在 initView 之前
    @Override
    public void initOtherOnCreateInLast(View contentView) {
        mContentView = (ViewGroup) contentView;

        level = SPUtils.getInt(Constant.SP_SNOW_LEVEL, level);
        count = SPUtils.getInt(Constant.SP_SNOW_COUNT, count);

        //create
        snowView = new SnowView(Global.getContext(), count);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(DisplayUtils.width, DisplayUtils.height);
        snowView.setLayoutParams(params);

        //add
        ViewGroup mContentView = (ViewGroup) contentView;
        mContentView.addView(snowView, level == 0 ? 0 : -1);
    }

    @OnClick({R.id.bt_sub, R.id.bt_add, R.id.bt_level})
    @Override
    public void onClick(View v) {
        change = true;
        switch (v.getId()){
            case R.id.bt_sub:
                if ((count -= 50) <= 50) {
                    count = 50;
                }
                snowView.delSnow(50);
                setTvCountText();
                break;
            case R.id.bt_add:
                if ((count += 50) >= MAX_COUNT) {
                    count = MAX_COUNT;
                }
                snowView.addSnow(50);
                setTvCountText();
                break;
            case R.id.bt_level:
                if(level == 0){
                    bt_level.setText("已置顶");
                    level = -1;
                    //
                    mContentView.removeView(snowView);
                    mContentView.addView(snowView);
                }else{
                    bt_level.setText("已置底");
                    level = 0;
                    //
                    mContentView.removeView(snowView);
                    mContentView.addView(snowView, 0);
                }
                break;
            case R.id.tv_menu_right_1:
                if(state == 0){
                    tv_menu_right_1.setText("已开启");
                    state = 1;
                    ToastUtils.show(R.string.pendant_snow_summary_on);
                }else{
                    tv_menu_right_1.setText("已关闭");
                    ToastUtils.show(R.string.pendant_snow_summary_off);
                    state = 0;
                }
                break;
        }
    }

    @Override
    public void onFinish() {
        // 源挂件
        String pendant = SPUtils.getString(Constant.SP_PENDANT, null);

        // save ball count
        // on off state
        SPUtils.Builder builder = SPUtils.builder()
                .putInt(Constant.SP_SNOW_COUNT, count)
                .putInt(Constant.SP_SNOW_LEVEL, level);
        if (state == 1) {
            builder.putString(Constant.SP_PENDANT, Constant.PENDANT_SNOW);
        }else if(Constant.PENDANT_SNOW.equals(pendant)){
            builder.putString(Constant.SP_PENDANT, null);
        }
        builder.commit();

        if(change){
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_PENDANT));
        }
    }
}
