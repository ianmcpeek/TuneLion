package com.huscii.ian.tunelion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class NowPlayingActivity extends ActionBarActivity {
    //use boolean to store whether song is playing or not
    //still needs volume control, randomize, repeat, skip
    private boolean paused;
    private Intent songServiceIntent;
    private SongQueueService songService;
    private PlayCountContract.PlayCountDatabaseHelper dbHelper;
    private TextView mAlbumName;
    private TextView mPlayCount;
    private ImageView mPlayButton;
    private TextView mArtistName;
    private TextView mGenreName;
    private TextView mTitleName;
    private SeekBar seekBar;
    private Handler seekHandler;

    private ArrayList<String> songPath;
    private int songIndex;

    // displaying metadata
//    TextView album, artist, genre;

    // displaying album art
    ImageView mAlbumArt;
    MediaMetadataRetriever metaRetriver;
    byte[] art;

    //MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        paused = false;
        dbHelper = new PlayCountContract.PlayCountDatabaseHelper(getApplicationContext());

        // ------- Grab widgets from layout -------
        mAlbumName = (TextView) this.findViewById(R.id.albumNameText);
        mPlayCount = (TextView) this.findViewById(R.id.playCountText);
        mPlayButton = (ImageView)  this.findViewById(R.id.playButton);
        mAlbumName = (TextView) this.findViewById(R.id.albumNameText);
        mArtistName = (TextView) this.findViewById(R.id.artistNameText);
        mTitleName = (TextView) this.findViewById(R.id.titleNameText);
        seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        seekHandler = new Handler();
        // ------- End of grabbing widgets -------

        int count =  PlayCountContract.read("shake_it_off.mp3", dbHelper);
        mPlayCount.setText("Play Count: " + count);

        songServiceIntent = new Intent(this, SongQueueService.class);
        startService(songServiceIntent);
        bindService(songServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //pass in path recieved from song item
        songPath = getIntent().getStringArrayListExtra("song_playlist");
        songIndex = getIntent().getIntExtra("song_index", -1);

        //check whether connected to internet
//        if(checkForConnection()) {
//            new LastFMTask().execute();
//            Toast.makeText(getApplicationContext(), "Grabbing album name from last.fm", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
//        }

        // ------- Retrieve metadata from song -------
        mAlbumArt = (ImageView) findViewById(R.id.albumArt);
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(songPath.get(songIndex));
        try {
            //grabbing same data twice, might just send data from activity instead
            art = metaRetriver.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            mAlbumArt.setImageBitmap(songImage);
            mAlbumName.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            mArtistName.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mTitleName.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//            mGenreName.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            mAlbumArt.setImageResource(R.drawable.tunelion_logo);
            mAlbumName.setText("Unknown Album");
            mArtistName.setText("Unknown Artist");
            mTitleName.setText("Unknown Title");
//            mGenreName.setText("Unknown Genre");
        }
        // ------- End of retrieving metadata -------
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused) {
            mPlayButton.setImageResource(R.drawable.play_button);
        }
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

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SongQueueService.LocalBinder binder = (SongQueueService.LocalBinder) service;
            songService = binder.getServiceInstance();
            songService.prepareSongQueue(songPath, songIndex);
            seekBar.setMax(songService.getDuration());
            updateSeekProgress();
            //make sure connection is established before wiring up seekbar
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    songService.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //what to do on disconnect
        }
    };

    public void onClick(View v) {

        if(!songService.isPlaying()) {
            songService.playSong();
            mPlayButton.setImageResource(R.drawable.pause_button);
            paused = false;

            //save play count to database
            int playCount = 0;
                    playCount =  PlayCountContract.read("shake_it_off.mp3", dbHelper);
            if(playCount > 0) {
                //update
                PlayCountContract.update("shake_it_off.mp3", playCount, dbHelper);
            } else {
                //insert
                PlayCountContract.insert("shake_it_off.mp3", dbHelper);
            }

            //TextView txtPlay = (TextView)v.findViewById(R.id.txt_playcnt);
            this.mPlayCount.setText("Play Count: " + playCount);
        } else {
            songService.pauseSong();
            mPlayButton.setImageResource(R.drawable.play_button);
            paused = true;

        }

    }

    Runnable run = new Runnable() { @Override public void run() { updateSeekProgress(); } };

    public void updateSeekProgress() {
        seekBar.setProgress(songService.getPosition());
        seekHandler.postDelayed(run, 1000);
    }

    public void back(View v) {
        //Button b = (Button)v.findViewById(R.id.btn_back);
        songService.backSkipSong();
    }

    public void skip(View v) {
        //btn_skip = (Button)v.findViewById(R.id.btn_skip);
        songService.forwardSkipSong();
    }

    public boolean checkForConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

//    private class LastFMTask extends AsyncTask {
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
}
