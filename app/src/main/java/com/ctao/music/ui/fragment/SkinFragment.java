package com.ctao.music.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ctao.baselib.lib.explosionfield.ExplosionFieldView;
import com.ctao.baselib.ui.adpter.RBaseAdapter;
import com.ctao.baselib.ui.adpter.holder.RViewHolder;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.FileUtils;
import com.ctao.baselib.utils.LogUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.baselib.widget.TagImageView;
import com.ctao.music.App;
import com.ctao.music.AppCache;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.CommonModel;
import com.ctao.music.module.imageselect.ImageSelectActivity;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.ui.common.CommonActivity;
import com.ctao.music.ui.common.CommonFragment;
import com.ctao.music.utils.ATEUtils;
import com.ctao.music.utils.UIUtils;
import com.ctao.music.utils.WallpaperUtils;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/7/25.
 */
public class SkinFragment extends CommonFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = SkinFragment.class.getSimpleName();
    private static final int SKIN_CUSTOM_COUNT = 5;
    private static final int REQUEST_CORP = 0x321;

    @BindView(R.id.ll_head) LinearLayout ll_head;
    @BindView(R.id.rv_skin)
    RecyclerView rv_skin;
    @BindView(R.id.sb_blurry) SeekBar sb_blurry;
    @BindView(R.id.cb_skin) CheckBox cb_skin;

    private int mResId[] = {
            R.mipmap.wallpaper_default,
    };

    private int column = 3;
    private int itemWidth;
    private int itemHeight;

    private TextView menuRight1;
    private ExplosionFieldView mExplosionField;
    private SkinAdapter mAdapter;
    private List<CommonModel> mData;

    private Uri mDestinationUri;
    private MaterialDialog dialog;

    @Override
    public String getTitle() {
        return "壁纸设置";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_skin;
    }

    @Override
    protected void initView() {
        itemWidth = DisplayUtils.width / column;
        itemHeight = (int) (itemWidth * 1.618f + 0.5f); //黄金比例

        mAdapter = new SkinAdapter();
        mData = mAdapter.getData();
        initAdapterData();

        rv_skin.setLayoutManager(new GridLayoutManager(getContext(), column)); // GridView 三列
        rv_skin.setItemAnimator(new DefaultItemAnimator());
        rv_skin.addItemDecoration(new SkinItemDecoration(getResources().getDimensionPixelSize(R.dimen.item_skin_spacing)));
        rv_skin.setAdapter(mAdapter);

        mExplosionField = ExplosionFieldView.attach2Window(getActivity());

        ATE.apply(ll_head, ATEUtils.getATEKey());

        sb_blurry.setMax(51); // 255/5
        sb_blurry.setProgress(SPUtils.getInt(Constant.SP_SKIN_ALPHA, 220) / 5);
        cb_skin.setChecked(SPUtils.getBoolean(Constant.SP_SKIN_IS_ON, false));
        onCheckedChanged(cb_skin, cb_skin.isChecked());
    }

    @Override
    protected void initListener() {
        sb_blurry.setOnSeekBarChangeListener(this);
        cb_skin.setOnCheckedChangeListener(this);
    }

    @Override
    public void initMenu(View contentView) {
        menuRight1 = (TextView) contentView.findViewById(R.id.tv_menu_right_1);
        menuRight1.setVisibility(View.VISIBLE);
        menuRight1.setText(R.string.custom);
        menuRight1.setOnClickListener(this);
    }

    private void initAdapterData() {
        String selectedName = AppCache.getInstance().getSkinName();
        boolean interruptFlag = false;

        mData.clear();

        // 初始化 mData 数据(添加系统壁纸)
        CommonModel commonModel = new CommonModel();
        commonModel.setIconBitmap(WallpaperUtils.getWallpaperBitmap(getContext()));
        if(TextUtils.isEmpty(selectedName)){
            commonModel.setSelected(true);
            interruptFlag = true;
        }
        mData.add(commonModel);


        // (res mipmap)
        for (int resId : mResId) {
            commonModel = new CommonModel();
            commonModel.setIconId(resId);

            if(!interruptFlag){
                String name = getResources().getResourceEntryName(resId);
                if(!TextUtils.isEmpty(name) && name.equals(selectedName)){
                    commonModel.setSelected(true);
                    interruptFlag = true;
                }
            }

            mData.add(commonModel);
        }

        // file
        File skin = FileUtils.getExternalFilesDir(Constant.FILE_SKIN);
        File[] files = skin.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.lastModified() > o2.lastModified() ? 1 : -1;
            }
        });
        if(files != null && files.length > 0){
            for (File file : files) {
                if(file != null){
                    commonModel = new CommonModel();
                    commonModel.setIconFile(file);
                    commonModel.setTag(App.getApp().getString(R.string.long_press_delete));

                    if(!interruptFlag){
                        String name = file.getName();
                        if(!TextUtils.isEmpty(name) && name.equals(selectedName)){
                            commonModel.setSelected(true);
                            interruptFlag = true;
                        }
                    }

                    mData.add(commonModel);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_menu_right_1:
                // file
                File skin = FileUtils.getExternalFilesDir(Constant.FILE_SKIN);
                File[] files = skin.listFiles();
                if (files != null && files.length >= SKIN_CUSTOM_COUNT) {
                    ToastUtils.show(R.string.cannot_custom_hint);
                    return;
                }

                if(mDestinationUri == null){
                    mDestinationUri = Uri.parse("file://" + FileUtils.createTmpFile(Constant.FILE_SKIN, new Date().getTime() + ".jpg"));
                }

                // 选择图片, 单选, 裁剪
                Intent intent = new Intent(getContext(), ImageSelectActivity.class);
                intent.putExtra(ImageSelectActivity.EXTRA_SELECT_MODE, ImageSelectActivity.MODE_SINGLE);

                intent.putExtra(ImageSelectActivity.EXTRA_BOOLEAN_CORP, true);
                intent.putExtra(ImageSelectActivity.EXTRA_URI_DESTINATION, mDestinationUri);
                intent.putExtra(ImageSelectActivity.EXTRA_INT_OUT_WIDTH, DisplayUtils.width);
                intent.putExtra(ImageSelectActivity.EXTRA_INT_OUT_HEIGHT, DisplayUtils.height);
                intent.putExtra(ImageSelectActivity.EXTRA_STRING_DESCRIBE, getString(R.string.to_screen_ratio));

                intent.putExtra(MvpActivity.TYPE_THEME_COLOR, Config.primaryColor(getContext(), ((CommonActivity)getActivity()).getATEKey()));
                startActivityForResult(intent, REQUEST_CORP);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CORP){
            cropResult(resultCode, data);
            return;
        }
    }

    private void cropResult(int resultCode, Intent data) {
        if (mDestinationUri != null) {
            String path = mDestinationUri.getPath();
            File file = new File(path);
            if (resultCode == Activity.RESULT_OK) {
                CommonModel commonBean = new CommonModel();
                commonBean.setIconFile(file);
                commonBean.setTag(App.getApp().getString(R.string.long_press_delete));

                for (CommonModel bean : mData) {
                    bean.setSelected(false);
                }
                commonBean.setSelected(true);

                mData.add(commonBean);
                mAdapter.notifyDataSetChanged();

                // 更改 bitmap path
                SPUtils.builder()
                        .putString(Constant.SP_SKIN_PATH, Constant.SKIN_TYPE_FILE + file.getAbsolutePath()).commit();

                AppCache.getInstance().setSkinName(file.getName());

                // 更新 皮肤
                showProgress();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_SKIN));

            } else {
                FileUtils.deleteFile(file); //删除临时文件
                if(resultCode == UCrop.RESULT_ERROR){
                    final Throwable cropError = UCrop.getError(data);
                    LogUtils.e(cropError);
                }
            }
            mDestinationUri = null;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 更改透明度
        int progress = seekBar.getProgress() * 5;
        UIUtils.setAlpha(progress);
        if(cb_skin.isChecked()){
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_SKIN_ALPHA));
            SPUtils.putObject(Constant.SP_SKIN_ALPHA, sb_blurry.getProgress() * 5);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        rv_skin.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        menuRight1.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_SKIN_ON_OFF, isChecked));
        SPUtils.putObject(Constant.SP_SKIN_IS_ON, cb_skin.isChecked());
    }

    private class SkinAdapter extends RBaseAdapter<CommonModel> {

        @Override
        public RViewHolder onCreateRViewHolder(ViewGroup parent, int viewType) {
            return new RViewHolder(R.layout.item_skin, parent);
        }

        @Override
        public void onBindRViewHolder(RViewHolder holder, int position, int viewType) {
            CommonModel commonModel = mList.get(position);
            TagImageView imageView = holder.getView(R.id.item_riv_bg);
            if(commonModel.getIconId() != 0){
                imageView.setImageResource(commonModel.getIconId());
            }else if(commonModel.getIconFile() != null){
                File iconFile = commonModel.getIconFile();
                if (iconFile != null){
                    Picasso.with(getContext())
                            .load(iconFile)
                            .tag(TAG)
                            .resize(itemWidth, itemHeight)
                            .centerCrop()
                            .into(imageView);
                }
            }else{
                Bitmap bitmap = commonModel.getIconBitmap();
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
            String tag = commonModel.getTag();
            if(!TextUtils.isEmpty(tag)){
                imageView.setEnable(true);
                imageView.setTagText(tag);
            }else{
                imageView.setEnable(false);
            }
            imageView.invalidate();

            holder.setVisibility(R.id.item_iv_icon, commonModel.isSelected() ? View.VISIBLE : View.GONE);

            // add Listener
            onBindViewListener(holder, position);
        }

        private void onBindViewListener(final RViewHolder holder, final int position) {
            final CommonModel commonModel = mList.get(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!commonModel.isSelected()){
                        // res
                        int iconId = commonModel.getIconId();
                        if(iconId != 0){
                            // 更改 res name
                            String resName = getResources().getResourceEntryName(iconId);
                            SPUtils.builder()
                                    .putString(Constant.SP_SKIN_PATH, Constant.SKIN_TYPE_RES + resName).commit();

                            AppCache.getInstance().setSkinName(resName);
                            AppCache.getInstance().setSkinBitmap(BitmapFactory.decodeResource(getResources(), iconId));

                        }else if(commonModel.getIconFile() != null){
                            // path bitmap
                            File iconFile = commonModel.getIconFile();
                            if(iconFile != null){

                                // 更改 bitmap path
                                SPUtils.builder()
                                        .putString(Constant.SP_SKIN_PATH, Constant.SKIN_TYPE_FILE + iconFile.getAbsolutePath()).commit();

                                AppCache.getInstance().setSkinName(iconFile.getName());

                            }
                        }else{
                            Bitmap bitmap = commonModel.getIconBitmap();

                            if(bitmap != null){
                                // 更改 bitmap path
                                SPUtils.builder()
                                        .putString(Constant.SP_SKIN_PATH, null).commit();

                                AppCache.getInstance().setSkinName(null);
                            }
                        }

                        for (CommonModel bean : mList) {
                            bean.setSelected(false);
                        }

                        commonModel.setSelected(true);

                        notifyDataSetChanged();

                        // 更新 皮肤
                        showProgress();
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_SKIN));
                    }
                }
            });

            // 长按删除
            if(commonModel.getTag() != null) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        if (commonModel.isSelected()) {
                            ToastUtils.show(App.getApp().getString(R.string.cannot_delete_hint));
                            return true;
                        }

                        mExplosionField.explode(v, true, new ExplosionFieldView.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                //恢复
                                v.setTranslationX(0);
                                v.setTranslationY(0);
                                v.setScaleX(1);
                                v.setScaleY(1);
                                v.setAlpha(1);

                                CommonModel remove = mList.remove(position);
                                notifyDataSetChanged();

                                FileUtils.deleteFile(remove.getIconFile()); //删除
                            }
                        });
                        return true;
                    }
                });
            }else{
                holder.itemView.setOnLongClickListener(null);
            }
        }
    }

    private class SkinItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SkinItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = space;
            outRect.bottom = space;
            outRect.left = space;
            outRect.right = space;
        }
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        super.onMessageEvent(event);
        switch (event.getType()){
            case MessageEvent.MUSIC_UPDATE_SKIN_BLURRY: // 模糊图片处理完成
                hideProgress();
                break;
        }
    }

    @Override
    public void showProgress() {
        if (null == dialog) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .content("设置中...")
                    .progress(true, 0)
                    .cancelable(false)
                    .build();
        }
        dialog.show();

    }

    @Override
    public void hideProgress() {
        if(null != dialog){
            dialog.dismiss();
        }
    }
}
