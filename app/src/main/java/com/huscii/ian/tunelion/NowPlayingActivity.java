package com.huscii.ian.tunelion;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

// lastfm java library
//import de.umass.lastfm.Artist;
//import de.umass.lastfm.Caller;


public class NowPlayingActivity extends AppCompatActivity {
    //use boolean to store whether song is playing or not
    //still needs volume control, randomize, repeat, skip
    private Intent songServiceIntent;
    private SongQueueService songService;
    private BroadcastReceiver receiver;
    private PlayCountContract.PlayCountDatabaseHelper dbHelper;
    private TextView mPlayCount;
    private ImageView mPlayButton;
    private ImageView mRepeatButton;
    private ImageView mShuffleButton;

    private SeekBar mSeekBar;
    private Handler seekHandler;

    // playCount update
    private boolean isResumed;

    private ArrayList<String> songPath;
    private int mSongIndex;

    // displaying album art
    ImageView mAlbumArt;
    MediaMetadataRetriever mMetaRetriever;
    byte[] art;

    // -- Metadata --
    private TextView mArtistName;
    private TextView mTitleName;
    private TextView mAlbumName;
    // --------------

    // vars for Last.FM Browser
    private String artistNameBio;

    // vars for theming
    ImageView mButtonBackground;
    ImageView mSongMetadataBackground;

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

        initViews();
        SharedPreferences settings = getPreferences(0);
        String bgColor = settings.getString("color","one_day_explosion");
        switch (bgColor) {
            case "sofft_reddy_pink":
                mButtonBackground
                        .setBackgroundResource(R.color.sofft_reddy_pink);
                mSongMetadataBackground
                        .setBackgroundResource(R.color.sofft_reddy_pink);
                break;
            case "holland":
                mButtonBackground
                        .setBackgroundResource(R.color.holland);
                mSongMetadataBackground
                        .setBackgroundResource(R.color.holland);
                break;
            case "over_easy_please":
                mButtonBackground
                        .setBackgroundResource(R.color.over_easy_please);
                mSongMetadataBackground
                        .setBackgroundResource(R.color.over_easy_please);
                break;
            case "bug_leg":
                mButtonBackground
                        .setBackgroundResource(R.color.bug_leg);
                mSongMetadataBackground
                        .setBackgroundResource(R.color.bug_leg);
                break;
            case "one_day_explosion":
                mButtonBackground
                        .setBackgroundResource(R.color.one_day_explosion);
                mSongMetadataBackground
                        .setBackgroundResource(R.color.one_day_explosion);
                break;
        }

        songServiceIntent = new Intent(this, SongQueueService.class);
        startService(songServiceIntent);
        bindService(songServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //pass in path recieved from song item
        songPath = getIntent().getStringArrayListExtra("song_playlist");
        mSongIndex = getIntent().getIntExtra("song_index", 0);
        isResumed = getIntent().getExtras().getBoolean("continue");

        //Register BroadcastReciever
       prepareSongPreparedReciever();

        //check whether connected to internet
        if(checkForConnection()) {
            Toast.makeText(getApplicationContext(), "(((o(*ﾟ▽ﾟ*)o)))", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "ಠ╭╮ಠ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(mConnection);
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

        //LastFMTask lastFMTask = new LastFMTask();
        /**
         * This is for grabbing the write artist info to
         * open within the browser
         */
        String[] strings = artistNameBio.split(" ");
        String finalString = "";

        /**
         * This is for changing the theme of the NowPlayingActivity
         * and MusicListActivity
         */
        CharSequence colors[] = new CharSequence[] {
                "sofft reddy pink",
                "holland",
                "over easy, please",
                "bug leg",
                "one day explosion"
        };

        if (id == R.id.themeChanger) {
            SharedPreferences settings = getPreferences(0);
            final SharedPreferences.Editor edit = settings.edit();

            new AlertDialog.Builder(NowPlayingActivity.this)
                    .setTitle("Change me please (・ω・)")
                    .setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int color) {
                            switch (color) {
                                case 0:
                                    mButtonBackground
                                            .setBackgroundResource(R.color.sofft_reddy_pink);
                                    mSongMetadataBackground
                                            .setBackgroundResource(R.color.sofft_reddy_pink);
                                    edit.putString("color", "sofft_reddy_pink");
                                    break;
                                case 1:
                                    mButtonBackground
                                            .setBackgroundResource(R.color.holland);
                                    mSongMetadataBackground
                                            .setBackgroundResource(R.color.holland);
                                    edit.putString("color", "holland");
                                    break;
                                case 2:
                                    mButtonBackground
                                            .setBackgroundResource(R.color.over_easy_please);
                                    mSongMetadataBackground
                                            .setBackgroundResource(R.color.over_easy_please);
                                    edit.putString("color", "over_easy_please");
                                    break;
                                case 3:
                                    mButtonBackground
                                            .setBackgroundResource(R.color.bug_leg);
                                    mSongMetadataBackground
                                            .setBackgroundResource(R.color.bug_leg);
                                    edit.putString("color", "bug_leg");
                                    break;
                                case 4:
                                    mButtonBackground
                                            .setBackgroundResource(R.color.one_day_explosion);
                                    mSongMetadataBackground
                                            .setBackgroundResource(R.color.one_day_explosion);
                                    edit.putString("color", "one_day_explosion");
                                    break;
                            }
                            edit.apply();
                        }
                    })
                    .setPositiveButton("I changed my mind ಠ_ಠ",
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // lolz
                        }
                    })
                    .show();

            return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.lastfmArtistInfo) {
            //lastFMTask.execute();
            for (int i = 0; i < strings.length; i++) {
                if (i == 0) finalString += strings[i];
                else finalString += "+" + strings[i];
            }
            Log.d(TAG, "finalString == " + finalString);
            goToUrl("http://www.last.fm/music/" + finalString);
//            LastFMTask lastFMTask = new LastFMTask();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
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

        mSongMetadataBackground = (ImageView) findViewById(R.id.songMetadataBackground);
        mButtonBackground = (ImageView) findViewById(R.id.buttonBackground);

        displayPlayCount();

        // ------- Grab Seekbar -------
        mSeekBar = (SeekBar) this.findViewById(R.id.seekBar);
        seekHandler = new Handler();
        //make sure connection is established before wiring up seekbar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // for skipping through the song when holding skip bar
                if (fromUser) songService.seekTo(progress);

                // 1000 == 1 seconds
                if (progress >= songService.getDuration() - 1000) {
                    updatePlayCount();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /************************
        BROADCAST RECIEVERS
     ************************/

    private void prepareSongPreparedReciever() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("SONG_PREPARED");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // gets current song
                mSongIndex = songService.getCurrentSongIndex();

                // display metadata when new song appears
                getMetadataForSong(intent.getExtras().getString("PATH"));

                // display proper playcount
                displayPlayCount();

                // sets correct seekbar
                mSeekBar.setProgress(0);
                mSeekBar.setMax(songService.getDuration());

                if(songService.isPlaying()) {
                    mPlayButton.setImageResource(R.drawable.pause_button);
                } else {
                    mPlayButton.setImageResource(R.drawable.play_button);
                }

                if(songService.isShuffled()) {
                    mShuffleButton.setImageResource(R.drawable.shuffle_on_button);
                } else {
                    mShuffleButton.setImageResource(R.drawable.shuffle_off_button);
                }

                switch(songService.getRepeatState()) {
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
        };
        registerReceiver(receiver, filter);
    }

    /******************
        SONG CONTROLS
     *****************/

    public void playSong(View v) {
        if(!songService.isPlaying()) {
            songService.playSong();
            mPlayButton.setImageResource(R.drawable.pause_button);
        } else {
            songService.pauseSong();
            mPlayButton.setImageResource(R.drawable.play_button);
        }
    }

    public void previousSong(View v) {
        Log.d(TAG, "previousSong got called. songindex: " + mSongIndex + "\nsongPath: "
                + songPath);
        mMetaRetriever.release();
        songService.previousSong();
    }

    public void nextSong(View v) {
        Log.d(TAG, "nextSong got called. songindex: " + mSongIndex + "\nsongPath: "
                + songPath);
        mMetaRetriever.release();
        if(songService.nextSong()) {
            mPlayButton.setImageResource(R.drawable.play_button);
        }
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


    public void updatePlayCount() {
        //Retrieve key to insert into database
        String dbKey = mTitleName.getText().toString() +
                mArtistName.getText().toString() + mAlbumName.getText().toString();
        //saves play count to database
        int playCount;
        playCount =  PlayCountContract.read(dbKey, dbHelper);
        if(playCount > 0) {
            //update
            PlayCountContract.update(dbKey, playCount, dbHelper);
        } else {
            //insert
            PlayCountContract.insert(dbKey, dbHelper);
        }
    }

    private void displayPlayCount() {
        String dbKey = mTitleName.getText().toString() +
                mArtistName.getText().toString() + mAlbumName.getText().toString();
        int playCount;
        playCount =  PlayCountContract.read(dbKey, dbHelper);
        mPlayCount.setText("Play Count: " + playCount);
    }

    public void updateSeekProgress() {
        mSeekBar.setProgress(songService.getPosition());
        seekHandler.postDelayed(run, 1000);
    }

    //used to periodically check seek progress
    Runnable run = new Runnable() {
        @Override public void run() {
            updateSeekProgress();
        }
    };

    //grabs song content to display for current song playing
    private void getMetadataForSong(String path) {
        mAlbumArt = (ImageView) findViewById(R.id.albumArt);
        mMetaRetriever = new MediaMetadataRetriever();
        mMetaRetriever.setDataSource(path);
        try {
            //grabbing same data twice, might just send data from activity instead
            art = mMetaRetriever.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            mAlbumArt.setImageBitmap(songImage);
            mAlbumName.setText
                    (mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            mArtistName.setText
                    (mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mTitleName.setText
                    (mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            // Set artist name for Last.FM
            artistNameBio =
                    mMetaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception e) {
            mAlbumArt.setImageResource(R.drawable.tunelion_logo);
            mAlbumName.setText("Unknown Album");
            mArtistName.setText("Unknown Artist");
            mTitleName.setText("Unknown Title");
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
            songService.prepareSongQueue(songPath, mSongIndex, isResumed);
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
//            Artist artist = Artist.getInfo(artistNameBio, key);
//            return artist.getWikiSummary();
//        }
//
//        @Override
//        protected void onPostExecute(Object result) {
//            new AlertDialog.Builder(NowPlayingActivity.this)
//                    .setTitle(artistNameBio)
//                    .setMessage((String) result)
//                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // continue with delete
//                        }
//                    })
//                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // do nothing
//                        }
//                    })
//                    .show();
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
