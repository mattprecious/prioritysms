package com.mattprecious.prioritysms;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Notification extends Activity {
    
    private SharedPreferences settings;
    private MediaPlayer mediaPlayer;
    
    private TextView messageView;
    private Button openButton;
    private Button dismissButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        
        settings        = getSharedPreferences(getPackageName() + "_preferences", 0);
        
        messageView     = (TextView) findViewById(R.id.message);
        openButton      = (Button) findViewById(R.id.open);
        dismissButton   = (Button) findViewById(R.id.dismiss);
        
        Intent intent   = getIntent();
        String number   = intent.getStringExtra("sender");
        String message  = intent.getStringExtra("message");
        
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        
        String[] columns = new String[]{PhoneLookup.DISPLAY_NAME};
        Cursor c = getContentResolver().query(uri, columns, null, null, null);
        
        String sender;
        if (c.moveToFirst()) {
            sender = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        } else {
            sender = number;
        }
        
        setTitle(sender);
        messageView.setText(message);
        
        openButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                stopAlarm();
                
                Intent mIntent = new Intent(Intent.ACTION_MAIN);
                mIntent.setType("vnd.android-dir/mms-sms");
                
                startActivity(mIntent);
                finish();
            }
        });
        
        dismissButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                stopAlarm();
                finish();
            }
        });
            
        startAlarm();
    }
    
    private void startAlarm() {
        String alarm = settings.getString("alarm", null);
        Uri uri = (alarm == null) ? 
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                        Uri.parse(alarm);
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            
        } catch (IOException e) {
            
        }
    }
    
    private void stopAlarm() {
        mediaPlayer.stop();
    }

}
