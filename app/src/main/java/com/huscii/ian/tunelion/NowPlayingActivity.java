package com.huscii.ian.tunelion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Track;


public class NowPlayingActivity extends ActionBarActivity {
    //use boolean to store whether song is playing or not
    //still needs volume control, randomize, repeat, skip
    private boolean paused;
    private Intent songServiceIntent;
    private SongQueueService songService;
    private PlayCountContract.PlayCountDatabaseHelper dbHelper;
    private TextView txt_album;
    private TextView txt_plays;
    private Button btn_play;

    private ArrayList<String> songPath;
    private int songIndex;

    // displaying metadeta
//    TextView album, artist, genre;

    // displaying album art
    ImageView album_art;
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
        txt_album = (TextView)this.findViewById(R.id.txt_albumname);
        txt_plays = (TextView)this.findViewById(R.id.txt_playcount);
        btn_play = (Button)this.findViewById(R.id.btn_play);

        int count =  PlayCountContract.read("shake_it_off.mp3", dbHelper);
        txt_plays.setText("Play Count = " + count);


        songServiceIntent = new Intent(this, SongQueueService.class);
        startService(songServiceIntent);
        bindService(songServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //pass in path recieved from song item
        songPath = getIntent().getStringArrayListExtra("song_playlist");
        songIndex = getIntent().getIntExtra("song_index", -1);

        //check whether connected to internet
        if(checkForConnection()) {
            new LastFMTask().execute();
            Toast.makeText(getApplicationContext(), "Grabbing album name from last.fm", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();
        }

        // retrieve album art from song
        album_art = (ImageView) findViewById(R.id.album_art);
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(songPath.get(songIndex));
        try {
            art = metaRetriver.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            album_art.setImageBitmap(songImage);
//            album.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//            artist.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//            genre.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
//            album.setText("Unknown Album");
//            artist.setText("Unknown Artist");
//            genre.setText("Unknown Genre");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused) {
            btn_play.setText("Pause");
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //what to do on disconnect
        }
    };

    public void onClick(View v) {

        if(!songService.isPlaying()) {
            songService.playSong();
            btn_play.setText("Pause");
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
            txt_plays.setText("Play Count = " + playCount);
        } else {
            songService.pauseSong();
            btn_play.setText("Play");
            paused = true;

        }

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

    private class LastFMTask extends AsyncTask {

        @Override
        protected String doInBackground(Object[] params) {
            Caller.getInstance().setCache(null);
            Caller.getInstance().setUserAgent("tst");
            String key = "c22dfba18c4c23bd20cfb6cd2caad7c1";
            String secret = "21d5ce8e8f31cfd0ba3fcc49d6226d18";
            Track track = Track.getInfo("Taylor Swift", "Shake it Off", key);
            //track.getAlbum();
            return track.getAlbum();
        }

        @Override
        protected void onPostExecute(Object result) {
            txt_album.setText((String)result);
        }
    }

}
