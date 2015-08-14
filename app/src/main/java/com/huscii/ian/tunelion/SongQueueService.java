//package com.huscii.ian.tunelion;
//
//import android.app.Service;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * Created by ian on 24/07/15.
// */
//
////BROADCAST when new song is played
//public class SongQueueService extends Service {
//    private final IBinder mBinder = new LocalBinder();
//    private MediaPlayer player;
//
//    //manage song queue
//    private ArrayList<String> songPathQueue;
//    private int currentSongIndex;
//
//    //controls for song player
//    private boolean songReady;
//    private boolean isPrevious;
//    private boolean isShuffle;
//    private boolean loopSong;
//    private boolean loopPlaylist;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        songPathQueue = new ArrayList<String>();
//        //work around to creating media player
//        player = MediaPlayer.create(this, R.raw.opossom_girl);
//        songReady = false;
//        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (determineNextSong()) {
//                    Log.d("OnCompletion", "current song index " + currentSongIndex + " out of " + songPathQueue.size());
//                    player.start();
//                    //player.release();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    public class LocalBinder extends Binder {
//        public SongQueueService getServiceInstance(){
//            return SongQueueService.this;
//        }
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    public void prepareSongQueue(ArrayList<String> songPathList, int startIndex) {
//        songPathQueue = songPathList;
//        currentSongIndex = startIndex;
//        prepareSong(currentSongIndex);
//    }
//
//    private boolean determineNextSong() {
//       boolean playlistOver = false;
//        if(loopSong) {
//            //do nothing, index is the same
//        //check if called by previous
//        } else if(isPrevious) {
//            isPrevious = false;
//            if(currentSongIndex - 1 < 0) {
//                currentSongIndex = songPathQueue.size() - 1;
//                if(!loopPlaylist) {
//                    playlistOver = true;
//                }
//            } else {
//                currentSongIndex--;
//            }
//        //check for shuffle
//        } else if (isShuffle) {
//            currentSongIndex = (int)(Math.random() * songPathQueue.size());
//        } else {
//            if(currentSongIndex + 1 >= songPathQueue.size()) {
//                currentSongIndex = 0;
//                if(loopPlaylist) {
//                    playlistOver = true;
//                }
//            } else {
//                currentSongIndex++;
//            }
//        }
//        if(playlistOver) {
//            return false;
//        }
//        prepareSong(currentSongIndex);
//        return true;
//    }
//
//    private boolean prepareSong(int index) {
//        songReady = false;
//            player.reset();
//            try {
//                player.setDataSource(songPathQueue.get(index));
//                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        songReady = true;
//                    }
//                });
//                player.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//        return true;
//    }
//
//    //trying to commit gradle
//    public void playSong() {
//       if(songReady) {
//           player.start();
//       }
//    }
//
//    public void pauseSong() {
//        player.pause();
//    }
//
//    public void previousSong() {
//        currentSongIndex--;
//        prepareSong(currentSongIndex);
//    }
//
//    public void nextSong() {
//        currentSongIndex++;
//        prepareSong(currentSongIndex);
//    }
//
//    public boolean isPlaying() {
//        return player.isPlaying();
//    }
//
//    public boolean toggleShuffle() {
//        if(isShuffle) {
//            isShuffle = false;
//        } else {
//            isShuffle = true;
//        }
//        return isShuffle;
//    }
//
//    public int toggleRepeat() {
//        if(!loopSong && !loopPlaylist) {
//            loopPlaylist = true;
//            return 1;
//        } else if(loopPlaylist) {
//            loopPlaylist = false;
//            loopSong = true;
//            return 2;
//        } else {
//            return 3;
//        }
//    }
//
//    public int getDuration() {
//        return player.getDuration();
//    }
//
//    public int getPosition() {
//        return player.getCurrentPosition();
//    }
//
//    public void seekTo(int position) {
//        player.seekTo(position);
//    }
//}

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

//BROADCAST when new song is played
public class SongQueueService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer player;

    //manage song queue
    private ArrayList<String> songPathQueue;
    private int currentSongIndex;

    //controls for song player
    private boolean isPreviousPressed;
    private boolean isNextPressed;
    private boolean isShuffle;
    private boolean isPlaying;
    private boolean loopSong;
    private boolean loopPlaylist;

    private final String TAG = "SongQueueService";

    @Override
    public void onCreate() {
        super.onCreate();
        songPathQueue = new ArrayList<String>();

        // Creates the media player
        // *Note: R.raw.opossom_girl is a work-around
        player = MediaPlayer.create(this, R.raw.opossom_girl);

        // --------- starts all fields as false ---------
        isPreviousPressed = false;
        isNextPressed = false;
        isPlaying = false;
        isShuffle = false;
        loopPlaylist = false;
        loopSong = false;
        //-----------------------------------------------

        // This is called when the end of the song is reached
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (determineNextSong()) {
                    Log.d("OnCompletion", "current song index " + currentSongIndex + " out of " + songPathQueue.size());
                    player.start();
                    //player.release();
                }
            }
        });
        // --------------------------------------------------
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

    private boolean determineNextSong() {
        // loopSong may or may not work. not sure yet.
        if (loopSong) {
            //do nothing, index is the same
        } else if (isPreviousPressed) { // previous song selected
            isPreviousPressed = false; // make's previous no longer selected
            if (currentSongIndex - 1 < 0) { // if currentSongIndex is at beginning of songPathQueue
                currentSongIndex = songPathQueue.size() - 1;
                /* Don't need this here, only applicable if the song is playing
                 * and the next song is the end of the playlist
                 */
                // if (!loopPlaylist) {
                //     playlistOver = true;
                // }
            } else {
                currentSongIndex--; // makes currentSongIndex goto previous song
            }
        } else if (isNextPressed) { // next song selected
            Log.d(TAG, "This part was reached");
            if (currentSongIndex + 1 > songPathQueue.size() -1 ) {
                Log.d(TAG, "This part was reached endofPlaylist");
                currentSongIndex = 0;
                /* Don't need this here, only applicable if the song is playing
                 * and the next song is the end of the playlist
                 */
                // if(loopPlaylist) {
                //     playlistOver = true;
                // }
            } else {
                currentSongIndex++;
            }
        } else { // if neither is pressed, then the loop applies
            if (currentSongIndex + 1 > songPathQueue.size() - 1) {
                if (loopPlaylist) {
                    currentSongIndex = 0; // goes to next song
                } else {
                    return false;
                }
            } else {
                currentSongIndex++;
            }
        }

        /* Don't need this here, only applicable if the song is playing
        * and the next song is the end of the playlist
        */
        // if(playlistOver) {
        //     return false;
        // }

        prepareSong(currentSongIndex);
        return true;
    }

    private boolean prepareSong(int index) {
        player.reset();
        try {
            player.setDataSource(songPathQueue.get(index));
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (isPlaying) playSong();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //trying to commit gradle
    public void playSong() {
        isPlaying = true;
        player.start();
    }

    public void pauseSong() {
        isPlaying = false;
        player.pause();
    }

    public void previousSong() {
        isPreviousPressed = true;
        determineNextSong();
    }

    public void nextSong() {
        isNextPressed = true;
        determineNextSong();
    }

    public boolean isPlaying() {
        return isPlaying;
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

    public int getDuration() {
        return player.getDuration();
    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public void seekTo(int position) {
        player.seekTo(position);
    }
}