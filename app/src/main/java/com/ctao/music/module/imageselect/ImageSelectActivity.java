package com.ctao.music.module.imageselect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.utils.UriUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by A Miracle on 2016/10/27.
 * 感谢: https://github.com/lovetuzitong/MultiImageSelector
 */
public class ImageSelectActivity extends MvpActivity implements ImageSelectFragment.Callback{

    // 单选
    public static final int MODE_SINGLE = 0;
    // 多选
    public static final int MODE_MULTI = 1;

    // 默认最大选择个数
    private static final int DEFAULT_IMAGE_SIZE = 9;

    /** 多选最大选择个数，int，默认值 {@link #DEFAULT_IMAGE_SIZE} */
    public static final String EXTRA_SELECT_COUNT = "select_count";
    /** 模式，默认 {@link #MODE_MULTI} */
    public static final String EXTRA_SELECT_MODE = "select_mode";
    /** 是否显示拍摄图片，默认 true */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /** 选择结果，ArrayList&lt;String&gt;*/
    public static final String EXTRA_RESULT = "result";
    /** 默认已选中 */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_selected_list";

    /** 是否裁剪 */
    public static final String EXTRA_BOOLEAN_CORP = "EXTRA_BOOLEAN_CORP";
    public static final String EXTRA_URI_DESTINATION = "EXTRA_URI_DESTINATION";
    public static final String EXTRA_INT_OUT_WIDTH = "EXTRA_INT_OUT_WIDTH";
    public static final String EXTRA_INT_OUT_HEIGHT = "EXTRA_INT_OUT_HEIGHT";
    public static final String EXTRA_STRING_DESCRIBE = "EXTRA_STRING_DESCRIBE";

    private int mDefaultCount = DEFAULT_IMAGE_SIZE;

    private ArrayList<String> resultList = new ArrayList<>();

    private boolean isCorp; //是否裁剪
    private int mOutMaxWidth, mOutMaxHeight; //输出图片大小
    private String mDescribe; //裁剪描述
    private Uri mDestinationUri; //裁剪输出目录

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_menu_right_1) TextView tv_menu_right_1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_imageselect;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {

        toolbar.setTitle(R.string.image);
        setSupportActionBar(toolbar);

        // 添加返回
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        isCorp = intent.getBooleanExtra(EXTRA_BOOLEAN_CORP,false);
        mDestinationUri = intent.getParcelableExtra(EXTRA_URI_DESTINATION);
        mOutMaxWidth = intent.getIntExtra(EXTRA_INT_OUT_WIDTH, DisplayUtils.width);
        mOutMaxHeight = intent.getIntExtra(EXTRA_INT_OUT_HEIGHT, DisplayUtils.height);
        mDescribe = intent.getStringExtra(EXTRA_STRING_DESCRIBE);
        if (TextUtils.isEmpty(mDescribe)){
            mDescribe = getString(R.string.corp);
        }

        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_IMAGE_SIZE);
        final int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        final boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        if(mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            ArrayList<String> defaultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
            if(defaultList != null && defaultList.size() > 0){
                resultList.addAll(defaultList);
            }
        }

        if(mode == MODE_MULTI){
            updateDoneText(resultList);
            tv_menu_right_1.setVisibility(View.VISIBLE);
            tv_menu_right_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(resultList != null && resultList.size() >0){
                        // Notify success
                        Intent data = new Intent();
                        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                        setResult(RESULT_OK, data);
                    }else{
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                }
            });
        }else{
            tv_menu_right_1.setVisibility(View.GONE);
        }

        // replace Fragment
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE_THEME_COLOR, themeColor);
        bundle.putInt(EXTRA_SELECT_COUNT, mDefaultCount);
        bundle.putInt(EXTRA_SELECT_MODE, mode);
        bundle.putBoolean(EXTRA_SHOW_CAMERA, isShow);
        bundle.putStringArrayList(EXTRA_DEFAULT_SELECTED_LIST, resultList);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_containers, Fragment.instantiate(this, ImageSelectFragment.class.getName(), bundle))
                .commit();
    }

    private void updateDoneText(ArrayList<String> resultList){
        int size = 0;
        if(resultList == null || resultList.size()<=0){
            tv_menu_right_1.setText(R.string.action_done);
            tv_menu_right_1.setEnabled(false);
        }else{
            size = resultList.size();
            tv_menu_right_1.setEnabled(true);
        }
        tv_menu_right_1.setText(getString(R.string.action_done)+"(" + size + "/" + mDefaultCount + ")");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageUnselected(String path) {
        if(resultList.contains(path)){
            resultList.remove(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onImageSelected(String path) {
        if(!resultList.contains(path)) {
            resultList.add(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onSingleImageSelected(String path) {
        if(!isCorp){
            Intent data = new Intent();
            resultList.add(path);
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
            return;
        }

        uCrop(path);
    }

    private void uCrop(String path) {
        Uri sourceUri = UriUtils.fromFile(new File(path));

        // 打开裁剪
        UCrop uCrop = UCrop.of(sourceUri, mDestinationUri);

        //.withAspectRatio(DisplayUtils.width, DisplayUtils.height) //裁剪框比例(为了同事使用缩放旋转手势, 采用 setAspectRatioOptions 方法)
        uCrop.withMaxResultSize(mOutMaxWidth, mOutMaxHeight); //返回结果最大大小

        UCrop.Options options = new UCrop.Options();

        options.setStatusBarColor(themeColor);
        options.setToolbarColor(themeColor);
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setAspectRatioOptions(0, new AspectRatio(mDescribe, mOutMaxWidth, mOutMaxHeight));
        uCrop.withOptions(options);

        // 获取Intent, 更改目标Activity
        Intent intent = uCrop.getIntent(this);
        intent.setClass(this, CropActivity.class);
        intent.putExtra(MvpActivity.TYPE_THEME_COLOR, themeColor);
        startActivityForResult(intent, UCrop.REQUEST_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(RESULT_OK, new Intent());

                finish();
                overridePendingTransition(0,0);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                ToastUtils.show(cropError.getMessage());
            }
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if(imageFile != null) {
            // 通知系统的图像有变化
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, UriUtils.fromFile(imageFile)));

            if(!isCorp){
                Intent data = new Intent();
                resultList.add(imageFile.getAbsolutePath());
                data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                setResult(RESULT_OK, data);
                finish();
                return;
            }

            uCrop(imageFile.getAbsolutePath());
        }
    }
}
