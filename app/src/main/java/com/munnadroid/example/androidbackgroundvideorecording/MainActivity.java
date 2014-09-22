package com.munnadroid.example.androidbackgroundvideorecording;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.munnadroid.backgroundrecord.RecorderService;

public class MainActivity extends Activity implements SurfaceHolder.Callback {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button startVideoButton = (Button) findViewById(R.id.button1);
        startVideoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent recoderServiceIntent = new Intent(MainActivity.this, RecorderService.class);
                recoderServiceIntent.putExtra(RecorderService.INTENT_VIDEO_PATH, "/aa/");
                startService(recoderServiceIntent);

            }
        });

        Button stopVideoButton = (Button) findViewById(R.id.button2);
        stopVideoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent in = new Intent(MainActivity.this, RecorderService.class);
                stopService(in);

            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

}
