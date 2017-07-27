package com.ctao.music.module.imageselect;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.ctao.baselib.Global;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.FileUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.module.imageselect.adapter.FolderAdapter;
import com.ctao.music.module.imageselect.adapter.ImageGridAdapter;
import com.ctao.music.module.imageselect.bean.Folder;
import com.ctao.music.module.imageselect.bean.Image;
import com.ctao.music.ui.base.MvpFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by A Miracle on 2016/10/27.
 */

public class ImageSelectFragment extends MvpFragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener, View.OnClickListener {

    public static final String TAG = ImageSelectFragment.class.getSimpleName();

    // loaders
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    private static final String KEY_TEMP_FILE = "key_temp_file";
    private static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 0x111;
    private static final int REQUEST_CAMERA = 0x222;

    private Callback mCallback;

    //图像结果数据集
    private ArrayList<String> resultList = new ArrayList<>();
    //文件夹的结果数据集
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    @BindView(R.id.gv_imageselect) GridView mGridView;
    @BindView(R.id.rl_footer) View mPopupAnchorView;
    @BindView(R.id.bt_category) Button mCategoryText;
    @BindView(R.id.bt_preview) Button mPreview;

    private ListPopupWindow mFolderPopupWindow;

    private boolean hasFolderGened = false; //文件夹基因

    private File mTmpFile;

    private int mode;

    private boolean isInitSetup;

    private int themeColor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //检查Activity是否实现了ImageSelectFragment.Callback接口
        try {
            mCallback = (Callback) getActivity();
        }catch (ClassCastException e){
            throw new ClassCastException("The Activity must implement ImageSelectFragment.Callback interface...");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_imageselect;
    }

    @Override
    protected void initView() {
        mCategoryText.setText(R.string.folder_all);
    }

    @Override
    protected void initListener() {
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(this);
        mCategoryText.setOnClickListener(this);
        mPreview.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mode = selectMode();
        themeColor = getThemeColor();
        if(mode == ImageSelectActivity.MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(ImageSelectActivity.EXTRA_DEFAULT_SELECTED_LIST);
            if(tmp != null && tmp.size()>0) {
                resultList = tmp;
            }
            updatePreviewText();
        }else{
            mPreview.setVisibility(View.GONE);
        }

        mImageAdapter = new ImageGridAdapter(showCamera(), 3);
        mImageAdapter.setShowSelectIndicator(mode == ImageSelectActivity.MODE_MULTI);
        mGridView.setAdapter(mImageAdapter);

        mFolderAdapter = new FolderAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_TEMP_FILE, mTmpFile);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTmpFile = (File) savedInstanceState.getSerializable(KEY_TEMP_FILE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 加载图像数据
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(mFolderPopupWindow != null){
            if(mFolderPopupWindow.isShowing()){
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    private boolean showCamera(){
        return getArguments() == null || getArguments().getBoolean(ImageSelectActivity.EXTRA_SHOW_CAMERA, true);
    }

    private int selectMode(){
        return getArguments() == null ? ImageSelectActivity.MODE_MULTI : getArguments().getInt(ImageSelectActivity.EXTRA_SELECT_MODE);
    }

    private int selectImageCount(){
        return getArguments() == null ? 9 : getArguments().getInt(ImageSelectActivity.EXTRA_SELECT_COUNT);
    }

    private int getThemeColor(){
        return getArguments() == null ? 0 : getArguments().getInt(ImageSelectActivity.TYPE_THEME_COLOR);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mImageAdapter.isShowCamera()) {
            if (position == 0) {
                showCameraAction(); //拍摄照片
            } else {
                Image image = (Image) parent.getAdapter().getItem(position);
                selectImageFromGrid(image, mode); // 选中
            }
        } else {
            Image image = (Image) parent.getAdapter().getItem(position);
            selectImageFromGrid(image, mode); // 选中
        }
    }

    // 拍照
    private void showCameraAction() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_rationale_write_storage), REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                if (mTmpFile == null) {
                    mTmpFile = FileUtils.createTmpFile(Constant.FILE_CACHE, "cameraTemp.jpg");
                }
                if (mTmpFile != null && mTmpFile.exists()) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    ToastUtils.show(getString(R.string.error_image_not_exist));
                }
            } else {
                ToastUtils.show(getString(R.string.msg_no_camera));
            }
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode){
        if(shouldShowRequestPermissionRationale(permission)){
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.permission_dialog_cancel, null)
                    .create().show();
        }else{
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_STORAGE_WRITE_ACCESS_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showCameraAction();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(mTmpFile);
                    }
                }
            }else{
                // delete tmp file
                while (mTmpFile != null && mTmpFile.exists()){
                    boolean success = mTmpFile.delete();
                    if(success){
                        mTmpFile = null;
                    }
                }
            }
        }
    }

    // 选中图片
    private void selectImageFromGrid(Image image, int mode) {
        if(image != null) {
            if(mode == ImageSelectActivity.MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    if(selectImageCount() == resultList.size()){
                        ToastUtils.show(getString(R.string.msg_amount_limit));
                        return;
                    }
                    resultList.add(image.path);
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);
            }else if(mode == ImageSelectActivity.MODE_SINGLE){
                if(mCallback != null){
                    mCallback.onSingleImageSelected(image.path);
                }
            }
        }
        updatePreviewText();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING) { //滚动状态, 暂停加载
            Picasso.with(view.getContext()).pauseTag(TAG);
        } else {
            Picasso.with(view.getContext()).resumeTag(TAG);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_category: {
                if (mFolderPopupWindow == null) {
                    createPopupFolderList();
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();

                    ListView listView = mFolderPopupWindow.getListView();
                    if(!isInitSetup){ //去除下拉阴影
                        listView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
                        isInitSetup = true;
                    }

                    int index = mFolderAdapter.getSelectedIndex();
                    index = index == 0 ? index : index - 1;
                    listView.setSelection(index);
                }
            }
                break;
            case R.id.bt_preview:
                startActivity(new Intent(getContext(), ImageBrowseActivity.class)
                .putExtra(ImageBrowseActivity.TYPE_THEME_COLOR, themeColor)
                .putExtra(ImageBrowseActivity.ExtraArrayPath, resultList));
                break;
        }
    }

    private void updatePreviewText(){
        int size = 0;
        if(resultList == null || resultList.size()<=0){
            mPreview.setText(R.string.action_preview);
            mPreview.setEnabled(false);
        }else{
            size = resultList.size();
            mPreview.setEnabled(true);
        }
        mPreview.setText(getString(R.string.action_preview)+"(" + size +")");
    }

    private void createPopupFolderList() {
        int width = DisplayUtils.width;
        int height = (int) (DisplayUtils.height * (4.5f/8.0f));
        mFolderPopupWindow = new ListPopupWindow(getActivity());
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);



        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectedIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                Global.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.folder_all);
                            if (showCamera()) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.images);
                                mCategoryText.setText(folder.name);
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = null;
            if(id == LOADER_ALL) {
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[3]+"=? OR "+IMAGE_PROJECTION[3]+"=? ",
                        new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
            }else if(id == LOADER_CATEGORY){
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[0]+" like '%"+args.getString("path")+"%'",
                        null, IMAGE_PROJECTION[2] + " DESC");
            }
            return cursorLoader;
        }

        private boolean fileExist(String path){
            if(!TextUtils.isEmpty(path)){
                return new File(path).exists();
            }
            return false;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    List<Image> images = new ArrayList<>();
                    data.moveToFirst();
                    do{
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if(!fileExist(path)){continue;}
                        Image image = null;
                        if (!TextUtils.isEmpty(name)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }
                        if( !hasFolderGened ) {
                            // get all folder data
                            File folderFile = new File(path).getParentFile();
                            if(folderFile != null && folderFile.exists()){
                                String fp = folderFile.getAbsolutePath();
                                Folder f = getFolderByPath(fp);
                                if(f == null){
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = fp;
                                    folder.cover = image;
                                    List<Image> imageList = new ArrayList<>();
                                    imageList.add(image);
                                    folder.images = imageList;
                                    mResultFolder.add(folder);
                                }else {
                                    f.images.add(image);
                                }
                            }
                        }

                    }while(data.moveToNext());

                    mImageAdapter.setData(images);
                    if(resultList != null && resultList.size()>0){
                        mImageAdapter.setDefaultSelected(resultList);
                    }
                    if(!hasFolderGened) {
                        mFolderAdapter.setData(mResultFolder);
                        hasFolderGened = true;
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private Folder getFolderByPath(String path){
        if(mResultFolder != null){
            for (Folder folder : mResultFolder) {
                if(TextUtils.equals(folder.path, path)){
                    return folder;
                }
            }
        }
        return null;
    }

    public interface Callback{
        void onImageUnselected(String path); //去消选中
        void onImageSelected(String path); //选中
        void onSingleImageSelected(String path); //单选
        void onCameraShot(File mTmpFile); //相机拍摄的
    }
}
