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

    //manage song queue
    private ArrayList<String> songPathQueue;
    private int currentSongIndex;

    //controls for song player
    private boolean songReady;
    private boolean isBack;
    private boolean isShuffle;
    private boolean loopSong;
    private boolean loopPlaylist;

    @Override
    public void onCreate() {
        super.onCreate();
        songPathQueue = new ArrayList<String>();
        //work around to creating media player
        player = MediaPlayer.create(this, R.raw.shake_it_off);
        songReady = false;
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (nextSong()) {
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

    private boolean nextSong() {
       boolean playlistOver = false;
        if(loopSong) {
            //do nothing, index is the same
            currentSongIndex = currentSongIndex;
        //check if called by previous
        } else if(isBack) {
            isBack = false;
            if(currentSongIndex-1 < 0) {
                currentSongIndex = songPathQueue.size()-1;
                if(!loopPlaylist) {
                    playlistOver = true;
                }
            } else {
                currentSongIndex--;
            }
        //check for shuffle
        } else if (isShuffle) {
            currentSongIndex = (int)(Math.random() * songPathQueue.size());
        } else {
            if(currentSongIndex+1 >= songPathQueue.size()) {
                currentSongIndex = 0;
                if(loopPlaylist) {
                    playlistOver = true;
                }
            } else {
                currentSongIndex++;
            }
        }
        if(playlistOver) {
            return false;
        }
        prepareSong(currentSongIndex);
        return true;
    }

    private boolean prepareSong(int index) {
        songReady = false;
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
                return false;
            }
        return true;
    }

    //trying to commit gradle
    public void playSong() {
       if(songReady) {
           player.start();
           //player.setLooping(true);
       }
    }

    public void pauseSong() {
        player.pause();
    }

    public void backSkipSong() {
        if(player.getDuration()>3000) {
            player.seekTo(0);
        } else {
            //previous song
            isBack = true;
            nextSong();
        }
    }

    public void forwardSkipSong() { player.seekTo(player.getDuration());}

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean toggleShuffle() {
        if(isShuffle) {
            isShuffle = false;
        } else {
            isShuffle = true;
        }
        return isShuffle;
    }

    public int toggleRepeat() {
        if(!loopSong && !loopPlaylist) {
            loopPlaylist = true;
            return 1;
        } else if(loopPlaylist) {
            loopPlaylist = false;
            loopSong = true;
            return 2;
        } else {
            return 3;
        }
    }
}
