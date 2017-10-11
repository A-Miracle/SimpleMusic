package com.ctao.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ctao.baselib.Global;
import com.ctao.baselib.utils.BitmapUtils;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.FileUtils;
import com.ctao.baselib.utils.ResourcesUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.manager.ThreadManager;
import com.ctao.music.model.SongInfo;
import com.ctao.music.ui.MainActivity;
import com.ctao.music.utils.MediaUtils;
import com.ctao.music.utils.UriUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by A Miracle on 2017/6/30.
 */
final class MediaNotificationManager extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    public static final String ACTION_PAUSE = "com.ctao.music.pause";
    public static final String ACTION_PLAY = "com.ctao.music.play";
    public static final String ACTION_PREV = "com.ctao.music.prev";
    public static final String ACTION_NEXT = "com.ctao.music.next";
    public static final String ACTION_STOP_CASTING = "com.ctao.music.stop_cast";

    private final MediaPlayerService mService;
    private final int mNotificationColor;
    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;

    private boolean mStarted = false;
    public MediaNotificationManager(MediaPlayerService service){
        mService = service;

        mNotificationColor = ResourcesUtils.getThemeColor(mService, R.attr.colorPrimary, Color.DKGRAY);
        mNotificationManager = NotificationManagerCompat.from(service);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        //取消所有通知服务被杀和处理情况
        //重新启动系统。
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /** 发布通知并开始跟踪会话保持更新。通知将自动删除如果会话被摧毁之前{ @link # stopNotification }。 */
    public void startNotification() {
        if (!mStarted) {
            EventBus.getDefault().register(this);
            // 通知必须更新后将开始设置为true
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);

                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    /**
     * 删除通知和停止跟踪会话。如果会话被摧毁这没有影响。
     */
    public void stopNotification() {
        if (mStarted) {
            EventBus.getDefault().unregister(this);
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // 忽略如果接收器没有注册。
            }
            mService.stopForeground(true);

        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);

        // 上一首
        notificationBuilder.addAction(R.mipmap.ic_skip_previous_white_24dp,
                    mService.getString(R.string.label_previous), mPreviousIntent);

        // 播放/暂停
        addPlayPauseAction(notificationBuilder);

        // 下一首
        notificationBuilder.addAction(R.mipmap.ic_skip_next_white_24dp,
                    mService.getString(R.string.label_next), mNextIntent);

        SongInfo songInfo = mService.getCurrentSong();

        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{0, 1, 2})  // show only play/pause in compact view
                        .setMediaSession(mService.getSessionToken()))
                .setColor(mNotificationColor)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(songInfo.getName())
                .setContentText(songInfo.getArtist());

        setNotificationPlaybackState(notificationBuilder);

        Bitmap bitmap = null;
        String albumId = songInfo.getAlbumId();
        File file = new File(FileUtils.getExternalFilesDir(Constant.FILE_IMG), albumId + ".jpg");
        if(file.exists()){
            bitmap = BitmapUtils.readFile2BitmapZoom(file);
        }
        if(null != bitmap){
            notificationBuilder.setLargeIcon(bitmap);
        }else{
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mService.getResources(), R.mipmap.ic_user));
            fetchBitmapFromURLAsync(albumId, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private void fetchBitmapFromURLAsync(final String albumId, final NotificationCompat.Builder builder) {
        String bitmapUrl = MediaUtils.getAlbumArtUri(Long.parseLong(albumId)).toString();
        Glide.with(Global.getContext()).load(bitmapUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Drawable drawable = resource.getCurrent();
                        final Bitmap bitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                        builder.setLargeIcon(bitmap);
                        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

                        // 图片保存到本地
                        ThreadManager.getShortPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                File file = FileUtils.createTmpFile(Constant.FILE_IMG, albumId + ".jpg");
                                FileUtils.saveBitmapToFile(file, UriUtils.fileProvider(), bitmap, Bitmap.CompressFormat.JPEG);
                            }
                        });
                        return true;
                    }
                })
                .into(DisplayUtils.converDip2px(96), DisplayUtils.converDip2px(96));
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {

        // 播放持续时间, 不显示了
        builder.setWhen(0)
                .setShowWhen(false)
                .setUsesChronometer(false);

        // 确保通知可以被用户当我们不玩:
        builder.setOngoing(mService.isPlaying());
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (mService.isPlaying()) {
            label = mService.getString(R.string.label_pause);
            icon = R.mipmap.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.mipmap.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY:
            case ACTION_PAUSE:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY_OR_PAUSE));
                break;
            case ACTION_NEXT:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_NEXT));
                break;
            case ACTION_PREV:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PRE));
                break;
            case ACTION_STOP_CASTING:
                /*Intent i = new Intent(context, MusicService.class);
                i.setAction(MusicService.ACTION_CMD);
                i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
                mService.startService(i);*/
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()){
            case MessageEvent.MUSIC_CHANGE_STATE: // 已经包括更换播放歌曲
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
                break;
        }

    }
}
