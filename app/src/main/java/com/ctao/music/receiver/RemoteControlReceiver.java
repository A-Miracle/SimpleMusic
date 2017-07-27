package com.ctao.music.receiver;

/**
 * Created by A Miracle on 2017/7/24.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.ctao.music.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 耳机线控
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY_OR_PAUSE));
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_NEXT));
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PRE));
                break;
        }
    }
}
