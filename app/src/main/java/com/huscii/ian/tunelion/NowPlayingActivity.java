package com.huscii.ian.tunelion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class NowPlayingActivity extends AppCompatActivity {
    //use boolean to store whether song is playing or not
    //still needs volume control, randomize, repeat, skip
    private boolean paused;
    private Intent songServiceIntent;
    private SongQueueService songService;
    private BroadcastReceiver reciever;
    private PlayCountContract.PlayCountDatabaseHelper dbHelper;
    private TextView mPlayCount;
    private ImageView mPlayButton;
    private ImageView mRepeatButton;
    private ImageView mShuffleButton;

    private SeekBar seekBar;
    private Handler seekHandler;

    private ArrayList<String> songPath;
    private int songIndex;

    // displaying album art
    ImageView mAlbumArt;
    MediaMetadataRetriever mMetaRetriever;
    byte[] art;

    // -- Metadata --
    private TextView mArtistName;
    private TextView mGenreName;
    private TextView mTitleName;
    private TextView mAlbumName;
    // --------------

    private final String TAG = "NowPlayingActivity";

    /**************************
        CORE ACTIVITY METHODS
     **************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        dbHelper = new PlayCountContract.PlayCountDatabaseHelper(getApplicationContext());
        paused = false;
        initViews();

        songServiceIntent = new Intent(this, SongQueueService.class);
        startService(songServiceIntent);
        bindService(songServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //pass in path recieved from song item
        songPath = getIntent().getStringArrayListExtra("song_playlist");
        songIndex = getIntent().getIntExtra("song_index", -1);

        //Register BroadcastReciever
        IntentFilter filter = new IntentFilter();
        filter.addAction("SONG_PREPARED");

        reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songIndex = songService.getCurrentSongIndex();
                //called metadata
                getMetadataForSong();
                seekBar.setMax(songService.getDuration());
            }
        };
        registerReceiver(reciever, filter);

        //check whether connected to internet
//        if(checkForConnection()) {
//            new LastFMTask().execute();
//            Toast.makeText(getApplicationContext(), "Grabbing album name from last.fm", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused) {
            mPlayButton.setImageResource(R.drawable.play_button);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciever);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initViews() {
        // ------- Grab widgets from layout -------
        mAlbumName = (TextView) this.findViewById(R.id.albumNameText);
        mPlayCount = (TextView) this.findViewById(R.id.playCountText);
        mPlayButton = (ImageView)  this.findViewById(R.id.playButton);
        mRepeatButton = (ImageView)  this.findViewById(R.id.repeatButton);
        mShuffleButton = (ImageView)  this.findViewById(R.id.shuffleButton);
        mAlbumName = (TextView) this.findViewById(R.id.albumNameText);
        mArtistName = (TextView) this.findViewById(R.id.artistNameText);
        mTitleName = (TextView) this.findViewById(R.id.titleNameText);

        // --------- Set Play Count ---------
        String dbKey = mTitleName.getText().toString() +
                mArtistName.getText().toString() + mAlbumName.getText().toString();
        int count = 0;
        count =  PlayCountContract.read(dbKey, dbHelper);
        mPlayCount.setText("Play Count: " + count);

        // ------- Grab Seekbar -------
        seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        seekHandler = new Handler();
        //make sure connection is established before wiring up seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    songService.seekTo(progress);
                } else {
                    // nothing
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /******************
        SONG CONTROLS
     *****************/

    public void playSong(View v) {
        if(!songService.isPlaying()) {
            songService.playSong();
            mPlayButton.setImageResource(R.drawable.pause_button);
            paused = false;
            updatePlayCount();
        } else {
            songService.pauseSong();
            mPlayButton.setImageResource(R.drawable.play_button);
            paused = true;
        }
    }

    public void updatePlayCount() {
        //Retrieve key to insert into database
        String dbKey = mTitleName.getText().toString() +
                mArtistName.getText().toString() + mAlbumName.getText().toString();
        //saves play count to database
        int playCount = 0;
        playCount =  PlayCountContract.read(dbKey, dbHelper);
        if(playCount > 0) {
            //update
            PlayCountContract.update(dbKey, playCount, dbHelper);
        } else {
            //insert
            PlayCountContract.insert(dbKey, dbHelper);
        }
        //TextView txtPlay = (TextView)v.findViewById(R.id.txt_playcnt);
        mPlayCount.setText("Play Count: " + playCount);
    }

    public void previousSong(View v) {
        Log.d(TAG, "previousSong got called. songindex: " + songIndex + "\nsongPath: "
                + songPath);
        mMetaRetriever.release();
        songService.previousSong();
        getMetadataForSong();
    }

    public void nextSong(View v) {
        Log.d(TAG, "nextSong got called. songindex: " + songIndex + "\nsongPath: "
                + songPath);
        mMetaRetriever.release();
        songService.nextSong();
        getMetadataForSong();
    }

    public void shuffleSongs(View v) {
        if(songService.toggleShuffle()) {
            mShuffleButton.setImageResource(R.drawable.shuffle_on_button);
        } else {
            mShuffleButton.setImageResource(R.drawable.shuffle_off_button);
        }
    }

    public void repeatSongs(View v) {
        switch(songService.toggleRepeat()) {
            //loop playlist
            case 1:
                mRepeatButton.setImageResource(R.drawable.repeat_all_button);
                break;
            //loop song
            case 2:
                mRepeatButton.setImageResource(R.drawable.repeat_one_button);
                break;
            //off
            case 3:
                mRepeatButton.setImageResource(R.drawable.repeat_off_button);
                break;
        }
    }

    public void updateSeekProgress() {
        seekBar.setProgress(songService.getPosition());
        seekHandler.postDelayed(run, 1000);
    }

    //used to periodically check seek progress
    Runnable run = new Runnable() {
        @Override public void run() {
            updateSeekProgress();
        }
    };

    //grabs song content to display for current song playing
    private void getMetadataForSong() {
        mAlbumArt = (ImageView) findViewById(R.id.albumArt);
        mMetaRetriever = new MediaMetadataRetriever();
        mMetaRetriever.setDataSource(songPath.get(songIndex));
        try {
            //grabbing same data twice, might just send data from activity instead
            art = mMetaRetriever.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            mAlbumArt.setImageBitmap(songImage);
            mAlbumName.setText(mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            mArtistName.setText(mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mTitleName.setText(mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//            mGenreName.setText(mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            mAlbumArt.setImageResource(R.drawable.tunelion_logo);
            mAlbumName.setText("Unknown Album");
            mArtistName.setText("Unknown Artist");
            mTitleName.setText("Unknown Title");
//            mGenreName.setText("Unknown Genre");
        }
    }

    /***************************
        SONG SERVICE CONNECTION
     ***************************/

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SongQueueService.LocalBinder binder = (SongQueueService.LocalBinder) service;
            songService = binder.getServiceInstance();
            songService.prepareSongQueue(songPath, songIndex);
            updateSeekProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //what to do on disconnect
        }
    };

    /***********************
        LAST.FM CONNECTION
     **********************/
//        private class LastFMTask extends AsyncTask {
//
//        @Override
//        protected String doInBackground(Object[] params) {
//            Caller.getInstance().setCache(null);
//            Caller.getInstance().setUserAgent("tst");
//            String key = "c22dfba18c4c23bd20cfb6cd2caad7c1";
//            String secret = "21d5ce8e8f31cfd0ba3fcc49d6226d18";
//            Track track = Track.getInfo("Taylor Swift", "Shake it Off", key);
//            //track.getAlbum();
//            return track.getAlbum();
//        }
//
//        @Override
//        protected void onPostExecute(Object result) {
//            mAlbumName.setText((String)result);
//        }
//    }
    /* Checks if device is connected to the internet.
     * Returns true if connected to a WiFi network.
     */
    public boolean checkForConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
