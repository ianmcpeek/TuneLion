package com.huscii.ian.tunelion;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ian on 24/07/15.
 */
public class SongQueueService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer player;
    private ArrayList<String> songPathQueue;
    private int currentSongIndex;
    private boolean songReady;

    @Override
    public void onCreate() {
        super.onCreate();
        songPathQueue = new ArrayList<String>();
        player = MediaPlayer.create(this, R.raw.shake_it_off);
        songReady = false;
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(prepareSong(++currentSongIndex)) {
                    Log.d("OnCompletion", "current song index " + currentSongIndex + " out of " + songPathQueue.size());
                        player.start();
                        //player.release();

                }
            }
        });
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

    public void prepareSongQueue(ArrayList<String> songPathList, int startIndex) {
        songPathQueue = songPathList;
        currentSongIndex = startIndex;
        prepareSong(currentSongIndex);
    }

    private boolean prepareSong(int index) {
        songReady = false;
        if(index<songPathQueue.size()) {
            player.reset();
            try {
                player.setDataSource(songPathQueue.get(index));
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        songReady = true;
                    }
                });
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //trying to commit gradle
    public void playSong() {
       if(songReady) {
           player.start();
           player.setLooping(true);
       }
    }

    public void pauseSong() {
        player.pause();
    }

    public void backSkipSong() {
        player.seekTo(0);
    }

    public void forwardSkipSong() { player.seekTo(player.getDuration());}

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
