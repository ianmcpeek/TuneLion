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
//    RelativeLayout ly;
//    RadioButton rdo_gold;
//    RadioButton rdo_red;
//    RadioButton rdo_blue;

    // For Dynamic Background
    List<Integer> backgroundImgList = new ArrayList<Integer>() {{
        add(R.drawable.login_background1);
        add(R.drawable.login_background2);
        add(R.drawable.login_background3);
    }};
    int backgroundImgId = 0;
    ImageView imgBus;
    // USED FOR SWAPPING IMAGES (SLIDESHOW)
//    Runnable swapImage = new Runnable() {
//        @Override
//        public void run() {
//            backgroundImgId++;
//            if (backgroundImgId == 3)
//                backgroundImgId = 0;
//            imgBus.setBackgroundResource(backgroundImgList.get(backgroundImgId));
//            imgBus.postDelayed(swapImage, 10000);
//        }
//    };
    ///////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ly = (RelativeLayout)findViewById(R.id.ly);
//        rdo_gold = (RadioButton)findViewById(R.id.rdo_gold);
//        rdo_red = (RadioButton)findViewById(R.id.rdo_red);
//        rdo_blue = (RadioButton)findViewById(R.id.rdo_blue);

//        SharedPreferences settings = getPreferences(0);
//        String bgColor = settings.getString("color","gold");
//        setBackgroundColor(bgColor);

        TextView loginButton = (TextView) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = LoginDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager().beginTransaction(),
                        "LoginDialogFragment");
            }
        });

        TextView skipLoginButton = (TextView) findViewById(R.id.skipLoginButton);
        skipLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MusicListActivity.class);
                startActivity(intent);
            }
        });

        imgBus = (ImageView) findViewById(R.id.imgBackground);

        imgBus.setBackgroundResource(backgroundImgList.get(backgroundImgId));
        // USED FOR SWAPPING IMAGES (SLIDESHOW) see above
        //imgBus.postDelayed(swapImage, 10000);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void onRadioClicked(View v) {
//        SharedPreferences settings = getPreferences(0);
//        SharedPreferences.Editor edit = settings.edit();
//
//        boolean checked = ((RadioButton)v).isChecked();
//        switch (v.getId()) {
//            case R.id.rdo_gold:
//                ly.setBackgroundResource(R.color.gold_color);
//                edit.putString("color", "gold");
//                break;
//            case R.id.rdo_red:
//                ly.setBackgroundResource(R.color.light_red_color);
//                edit.putString("color", "red");
//                break;
//            case R.id.rdo_blue:
//                ly.setBackgroundResource(R.color.light_blue_color);
//                edit.putString("color", "blue");
//                break;
//        }
//        edit.commit();
//    }
//
//    public void setBackgroundColor(String color) {
//        switch(color) {
//            case "gold":
//                ly.setBackgroundResource(R.color.gold_color);
//                rdo_gold.setChecked(true);
//                break;
//            case "red":
//                ly.setBackgroundResource(R.color.light_red_color);
//                rdo_red.setChecked(true);
//                break;
//            case "blue":
//                ly.setBackgroundResource(R.color.light_blue_color);
//                rdo_blue.setChecked(true);
//                break;
//        }
//    }
}
