package com.huscii.ian.tunelion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

//needs to register a simpleongesturelistener for fling
public class MusicListActivity extends AppCompatActivity {
    BroadcastReceiver reciever;
    private int songIndex;

    private ArrayList<SongData> musicData;

    private ImageView mNowPlayingWidget;

    private final String TAG = "MusicListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        musicData = new ArrayList<>();

        ListView mSongList = (ListView) findViewById(R.id.songList);
        final SongCursorAdapter adapter = new SongCursorAdapter(this, getCursor(), 0);
        mSongList.setAdapter(adapter);
        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(getApplicationContext(), "You touched me! ;)", Toast.LENGTH_SHORT).show();

                //start nowPlaying activity, pass in song path as an extra
                Intent intent = new Intent(v.getContext(), NowPlayingActivity.class);
                intent.putStringArrayListExtra("song_playlist", getSongPaths());
                intent.putExtra("song_index", position);
                intent.putExtra("continue", false);
                Log.d(TAG, "position: "+ position + "Songpath: " + getSongPaths());
                startActivity(intent);
            }
        });

        //Register BroadcastReciever
        IntentFilter filter = new IntentFilter();
        filter.addAction("SONG_PREPARED");

        reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songIndex = intent.getExtras().getInt("INDEX");
                String path = intent.getExtras().getString("PATH");
                //look for songdata matching path
                int i = 0;
                for(SongData songData: musicData) {
                    if(songData.getSongPath().equals(path)) {
                        break; //found match
                    }
                    i++;
                }
                //set now playing fragment
                TextView mSongName = (TextView) findViewById(R.id.songName);
                TextView mSongArtist = (TextView) findViewById(R.id.songArtist);
                TextView mSongAlbum = (TextView) findViewById(R.id.songAlbum);
                ImageView mWidget = (ImageView) findViewById(R.id.nowPlayingWidget);


                mSongName.setText(musicData.get(i).getSongName());
                mSongArtist.setText(musicData.get(i).getSongArtist());
                mSongAlbum.setText(musicData.get(i).getSongAlbum());
                mWidget.setVisibility(View.VISIBLE);
                mSongName.setVisibility(View.VISIBLE);
                mSongArtist.setVisibility(View.VISIBLE);
                mSongAlbum.setVisibility(View.VISIBLE);
            }
        };
        registerReceiver(reciever, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public Cursor getCursor() {
        //retrieve existing music on phone
        String[] projection = {
                //MediaStore.Audio.Media.CONTENT_TYPE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);
        cursor.moveToFirst();
        return cursor;
    }

    public ArrayList<String> getSongPaths() {
        ArrayList<String> songs = new ArrayList<>();
        for (SongData song : musicData) {
            songs.add(song.getSongPath());
        }
        return songs;
    }

    public void continueNowPlaying(View v) {
        Intent intent = new Intent(MusicListActivity.this, NowPlayingActivity.class);
        intent.putStringArrayListExtra("song_playlist", getSongPaths());
        intent.putExtra("song_index", songIndex);
        intent.putExtra("continue", true);
        startActivity(intent);
    }

    private class SongCursorAdapter extends CursorAdapter {
        public SongCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView txtSong = (TextView) view.findViewById(R.id.txt_song);
            TextView txtArtist = (TextView) view.findViewById(R.id.txt_artist);
            TextView txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            TextView txtDuration = (TextView) view.findViewById(R.id.txt_duration);
            TextView txtPath = (TextView) view.findViewById(R.id.txt_path);

            //cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(3) +
            //" " + cursor.getString(4) + " " + cursor.getString(5) + " " + cursor.getString(6)
            String song = cursor.getString(cursor.getColumnIndex("TITLE"));
            String artist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            String album = cursor.getString(cursor.getColumnIndex("ALBUM"));
            int duration = cursor.getInt(cursor.getColumnIndex("DURATION"));
            String dataSource = cursor.getString(3);

            SongData songData = new SongData(song, artist, album, dataSource);
            boolean exists = false;
            for (SongData sData : musicData) {
                if (sData.equals(songData)) {
                    Log.d("SongPathQueue", "Song found " + song);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                musicData.add(songData);
                Log.d("SongPathQueue", "Added song " + song + " size: " + musicData.size());
            }

            //Check how to display data
            txtSong.setText(song);
            txtArtist.setText(artist);
            txtAlbum.setText(album);
            txtDuration.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
            txtPath.setText(dataSource);
        }
    }
}