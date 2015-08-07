package com.huscii.ian.tunelion;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MusicListActivity extends AppCompatActivity {
    private ArrayList<String> songPathList;
    private int songindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        songPathList = new ArrayList<>();
        songindex = 0;

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
        ListView lv = (ListView) findViewById(R.id.list_view);
        SongCursorAdapter adapter = new SongCursorAdapter(this, cursor, 0);
        lv.setAdapter(adapter);

        //Used to change contents within list bro
        //adapter.changeCursor(newCursor);

        //cursor.close();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemClicked(View v) {
        Toast.makeText(getApplicationContext(), "You touched me! ;)", Toast.LENGTH_SHORT).show();

        TextView txtIndex = (TextView)v.findViewById(R.id.txt_index);
        int myIndex = Integer.parseInt(txtIndex.getText().toString());
        //start nowPlaying activity, pass in song path as an extra
        Intent intent = new Intent(v.getContext(), NowPlayingActivity.class);
        intent.putStringArrayListExtra("song_playlist", songPathList);
        intent.putExtra("song_index", myIndex);
        startActivity(intent);
    }

    public class SongCursorAdapter extends CursorAdapter {

        public SongCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, 0);
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
            TextView txtIndex = (TextView) view.findViewById(R.id.txt_index);

            //cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(3) +
            //" " + cursor.getString(4) + " " + cursor.getString(5) + " " + cursor.getString(6)
            String song = cursor.getString(cursor.getColumnIndex("TITLE"));
            String artist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            String album = cursor.getString(cursor.getColumnIndex("ALBUM"));
            int duration = cursor.getInt(cursor.getColumnIndex("DURATION"));
            //Used to change song
            String dataSource = cursor.getString(3);
            songPathList.add(dataSource);
            txtSong.setText(song);
            txtArtist.setText(artist);
            txtAlbum.setText(song);
            txtDuration.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
            txtIndex.setText(Integer.toString(songindex));
            songindex++;

        }


    }
}
