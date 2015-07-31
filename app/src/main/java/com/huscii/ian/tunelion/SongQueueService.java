package com.huscii.ian.tunelion;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by ian on 24/07/15.
 */
public class SongQueueService extends Service {
    private final IBinder mBinder = new LocalBinder();
    MediaPlayer player;
    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.shake_it_off);
        //player.setDataSource(String path); will be used for changing songs
        //player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SongQueueService getServiceInstance(){
            return SongQueueService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //trying to commit gradle
    public void playSong() {
        player.start();
    }

    public void pauseSong() {
        player.pause();
    }

    public void backSkipSong() {
        player.seekTo(0);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
