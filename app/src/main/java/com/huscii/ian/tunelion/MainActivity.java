package com.huscii.ian.tunelion;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.huscii.ian.tunelion.Login.LoginDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    // For Dynamic Background
    List<Integer> backgroundImgList = new ArrayList<Integer>() {{
        add(R.drawable.login_background1);
        add(R.drawable.login_background2);
        add(R.drawable.login_background3);
    }};
    int backgroundImgId = 0;
    ImageView imgBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView loginButton = (TextView) this.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = LoginDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager().beginTransaction(),
                        "LoginDialogFragment");
            }
        });

        TextView skipLoginButton = (TextView) this.findViewById(R.id.skipLoginButton);
        skipLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MusicListActivity.class);
                startActivity(intent);
            }
        });

        imgBus = (ImageView) this.findViewById(R.id.imgBackground);

        imgBus.setBackgroundResource(backgroundImgList.get(backgroundImgId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
