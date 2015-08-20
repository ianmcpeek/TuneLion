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
    private ArrayList<String> shuffleQueue;
    private int currentSongIndex;

    //controls for song player
    private boolean isPreviousPressed;
    private boolean isShuffle;
    private boolean isPlaying;
    private boolean isPlaylistOver;
    private boolean loopSong;
    private boolean loopPlaylist;

    private final String TAG = "SongQueueService";

    @Override
    public void onCreate() {
        super.onCreate();
        songPathQueue = new ArrayList<>();

        // Creates the media player
        // *Note: R.raw.opossom_girl is a work-around
        player = MediaPlayer.create(this, R.raw.opossom_girl);

        // --------- starts all fields as false ---------
        isPreviousPressed = false;
        isPlaying = false;
        isPlaylistOver = false;
        isShuffle = false;
        loopPlaylist = false;
        loopSong = false;

        // This is called when the end of a song is reached
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (determineNextSong()) {
                    Log.d("OnCompletion", "current song index " + currentSongIndex + " out of " + songPathQueue.size());
                } else {
                    player.stop();
                    isPlaying = false;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
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

    /*
     * Called by an activity to pass in a song queue to be played by the player
     */
    public void prepareSongQueue(ArrayList<String> songPathList, int startIndex, boolean isResumed) {
        if(isResumed) {
            Intent i = new Intent("SONG_PREPARED");
            i.putExtra("INDEX", currentSongIndex);
            i.putExtra("PATH", (isShuffle ? shuffleQueue:songPathQueue).get(currentSongIndex));
            sendBroadcast(i);
        } else {
            songPathQueue = songPathList;
            currentSongIndex = startIndex;
            prepareSong(currentSongIndex, songPathQueue);
        }
        //if shuffle is enabled on this call, call createshuffle playlist first
    }

    /*
     * Whether a song is completed, or the user presses the skip or previous button this is called
     * to determine which song to play next in the playlist depending on which options are currently
     * toggled.
     */
    private boolean determineNextSong() {
        ArrayList<String> playlist;
        int indexStart = currentSongIndex;
        //determine which playlist is being used
        if(isShuffle) {
            playlist = shuffleQueue;
        } else {
            playlist = songPathQueue;
        }

        //check if loop song is pressed
        if(loopSong) {
            //don't change index, same one will be used to prepare song
        } else if(isPreviousPressed) {
            currentSongIndex = (currentSongIndex > 0) ? currentSongIndex - 1 : 0;
        //Otherwise when a song is completed or next is clicked, advance index forward
        } else {
            currentSongIndex =
                    (currentSongIndex < playlist.size() - 1)
                            ? currentSongIndex + 1 : playlist.size() - 1;
        }

        //validate whether index is within bounds
        if (!checkPlaylistOver(indexStart, playlist)) {
            prepareSong(currentSongIndex, playlist);
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPlaylistOver(int indexStart, ArrayList<String> playlist) {
        boolean playlistOver = false;
        if (loopPlaylist) {
            //make sure songIndex is 0 and hasn't been set on this method call
            if (isPreviousPressed && currentSongIndex == 0
                    && currentSongIndex == indexStart) {
                currentSongIndex = playlist.size()-1;
            //make sure songIndex is size-1 and hasn't been set on this method call
            }else if (currentSongIndex == playlist.size()-1
                    && currentSongIndex == indexStart) {
                currentSongIndex = 0;
            }
        } else {
            //Check if song is either beginning or end of playlist, and index hasn't been set on this method call
            if ((currentSongIndex == playlist.size()-1
                    || currentSongIndex == 0)
                    && currentSongIndex == indexStart) {
                playlistOver = true;
            }
        }
        isPreviousPressed = false;
        return playlistOver;
    }

    /**
     * After the appropiate song index has been chosen, this method prepares which song to play.
     * @param index
     * @return returns true if song was successfully prepared.
     */
    private boolean prepareSong(int index, ArrayList<String> playlist) {
        player.reset();
        try {
            player.setDataSource(playlist.get(index));
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
        Intent i = new Intent("SONG_PREPARED");
        i.putExtra("INDEX", currentSongIndex);
        i.putExtra("PATH", playlist.get(index));
        sendBroadcast(i);
        return true;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    /*******************
        PLAYER CONTROLS
     *******************/

    public void playSong() {
        isPlaying = true;
        player.start();
    }

    public void pauseSong() {
        isPlaying = false;
        player.pause();
    }

    public void previousSong() {
        if(isPlaylistOver) {
            prepareSong(currentSongIndex, isShuffle?shuffleQueue:songPathQueue);
            isPlaylistOver = false;
            return;
        }
        int length = player.getCurrentPosition();
        if(length>3000) {
            player.seekTo(0);
        } else {
            isPreviousPressed = true;
            determineNextSong();
        }
    }

    public boolean nextSong() {
        isPlaying = true;
        if(!determineNextSong()) {
            player.stop();
            isPlaying = false;
            isPlaylistOver = true;
        }
        return isPlaylistOver;
    }

    public boolean isPlaying() {
        return isPlaying;
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

    /*******************************
        MULTI-FACED BUTTON CONTROLS
     ******************************/

    private void createShuffledPlaylist() {
        //copy all songs in playlist
        ArrayList<String> cpy = new ArrayList<String>();
        for(String songpath:songPathQueue) {
            cpy.add(songpath);
        }

        //grab current index as beginning of shuffledPlaylist
        shuffleQueue = new ArrayList<String>();
        shuffleQueue.add(cpy.get(currentSongIndex));
        cpy.remove(currentSongIndex);
        while(cpy.size()>0) {
            int rndm = (int)(Math.random()*cpy.size());
            shuffleQueue.add(cpy.get(rndm));
            cpy.remove(rndm);
        }
        //set index to beginning of shuffledPlaylist
        currentSongIndex = 0;
    }

    private void resetSongIndex() {
        currentSongIndex = songPathQueue.indexOf(shuffleQueue.get(currentSongIndex));
    }

    public boolean toggleShuffle() {
        if (isShuffle) {
            isShuffle = false;
            resetSongIndex();
        } else {
            isShuffle = true;
            createShuffledPlaylist();
        }
        return isShuffle;
    }

    public int toggleRepeat() {
        if (!loopSong && !loopPlaylist) {
            loopPlaylist = true;
            return 1;
        } else if(loopPlaylist && !loopSong) {
            loopPlaylist = false;
            loopSong = true;
            return 2;
        } else {
            loopPlaylist = false;
            loopSong = false;
            return 3;
        }
    }
}