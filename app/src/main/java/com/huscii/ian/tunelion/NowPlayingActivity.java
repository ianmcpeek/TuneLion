package com.huscii.ian.tunelion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;


public class NowPlayingActivity extends ActionBarActivity {
    //use boolean to store whether song is playing or not
    //still needs volume control, randomize, repeat, skip
    Button skipBtn;
    Intent songServiceIntent;
    SongQueueService songService;

    ImageView albumArt;

    //MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        songServiceIntent = new Intent(this, SongQueueService.class);
        startService(songServiceIntent);
        bindService(songServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        skipBtn = (Button) this.findViewById(R.id.btn_skip);
        albumArt = (ImageView) this.findViewById(R.id.imageView);
        //player = MediaPlayer.create(this, R.raw.shake_it_off);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //what to do on disconnect
        }
    };

    public void onClick(View v) {
        Button b = (Button)v.findViewById(R.id.btn_play);
        if(!songService.isPlaying()) {
            songService.playSong();
            new LastFMTask().execute();
            b.setText("Pause");
        } else {
            songService.pauseSong();
            b.setText("Play");
        }
    }

    public void back(View v) {
        //Button b = (Button)v.findViewById(R.id.btn_back);
        songService.backSkipSong();
    }

    public void skip(View v) {
//        new LastFMTask().execute();
    }

    private class LastFMTask extends AsyncTask {

        @Override
        protected String doInBackground(Object[] params) {
            Caller.getInstance().setCache(null);
            Caller.getInstance().setUserAgent("tst");
            String key = "c22dfba18c4c23bd20cfb6cd2caad7c1";
            String secret = "21d5ce8e8f31cfd0ba3fcc49d6226d18";
            Album album = Album.getInfo("Opossom", "Electric Hawaii", key);
            String artworkURL = album.getImageURL(ImageSize.LARGESQUARE);
//            Track track = Track.getInfo("The Bangles", "Manic Monday", key);
            //track.getAlbum();
//            return track.getAlbum();
        }

        @Override
        protected void onPostExecute(Object result) {
            skipBtn.setText((String) result);
        }
    }
}
