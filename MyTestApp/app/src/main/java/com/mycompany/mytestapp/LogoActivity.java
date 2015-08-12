package com.mycompany.mytestapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class LogoActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        final Context context = this;
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        ActionBar bar = getActionBar();
        if(bar != null) {
            bar.hide();
        }
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    onDestroy();
                }
            }
        };
        t.start();
    }
}
