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
 * This service is used to play songs from a playlist throughout the phone.
 * Activities are notified on song changes.
 * Created by ian on 24/07/15.
 */
public class SongQueueService extends Service {

    /* Used to bind service */
    private final IBinder mBinder = new LocalBinder();
    /* Performs all actions related to a song */
    private MediaPlayer player;

    //manage song queue
    /* Stores the sequential ordering of all song data sources in a playlist */
    private ArrayList<String> songPathQueue;
    /* Stores the shuffled ordering of all song data sources in a playlist */
    private ArrayList<String> shuffleQueue;
    /* Stores the current position in a playlist */
    private int currentSongIndex;

    //controls for song player
    /* Determines whether the previous button was pressed */
    private boolean isPreviousPressed;
    /* Determines whether playlist is shuffled */
    private boolean isShuffle;
    /* Determines whether a song is currently playing */
    private boolean isPlaying;
    /* Determines whether the playlist has ended */
    private boolean isPlaylistOver;
    /* Determines whether one song in a playlist is looping */
    private boolean loopSong;
    /* Determines whether all songs in a playlist are looping */
    private boolean loopPlaylist;

    //bind service boilerplate code
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

    /**
     * Sets the Song Queue with a playlist passed in from the Now Playing Activity. Alternatively
     * fires a "SONG_PREPARED" broadcast to immediately set the controls to match data onResume of
     * Now Playing Activity.
     * @param songPathList list of all song data paths for a playlist
     * @param startIndex position in playlist to start off with
     * @param isResumed flag to determine whether nowPlaying was being created or resumed
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
            //if shuffle is enabled on this call, call createshuffle playlist first
            if(isShuffle) {
                createShuffledPlaylist();
            }
            prepareSong(currentSongIndex, songPathQueue);
        }
    }

    /**
     * Whether a song is completed, or the user presses the skip or previous button this is called
     * to determine which song to play next in the playlist depending on which options are currently
     * toggled.
     * @return whether another song is playing
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

    /**
     * Determines whether playlist is over and resets playlist when appropiate.
     * @param indexStart index used to check whether value was changed on this call
     * @param playlist playlist currently in use of the player
     * @return whether playlist is over
     */
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
     * @param index position to play song from
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

        //send out broadcast to notify user controls of change
        Intent i = new Intent("SONG_PREPARED");
        i.putExtra("INDEX", currentSongIndex);
        i.putExtra("PATH", playlist.get(index));
        sendBroadcast(i);
        return true;
    }

    /**
     * Returns current song position in playlist.
     * @return song position
     */
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    /*******************
        PLAYER CONTROLS
     *******************/

    /**
     * Plays a song.
     */
    public void playSong() {
        isPlaying = true;
        player.start();
    }

    /**
     * Pauses a song.
     */
    public void pauseSong() {
        isPlaying = false;
        player.pause();
    }

    /**
     * Jumps to beginning of song or previous song.
     */
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

    /**
     * Jumps to next song or ends playlist.
     * @return whether playlist is over.
     */
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
    public boolean isShuffled() {
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

    public int getRepeatState() {
        if (!loopSong && !loopPlaylist) {
            return 3;
        } else if(loopPlaylist && !loopSong) {
            return 1;
        } else {
            return 2;
        }
    }
}