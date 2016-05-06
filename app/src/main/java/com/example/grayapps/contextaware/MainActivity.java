package com.example.grayapps.contextaware;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        title.setText("Study For Test");
        Window w = this.getWindow();
        toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorStress))));


        /** Drawer begins*/
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        //pass your items here
                )
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        /** Drawer ends */
        w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorStressDark)));
    }

}
