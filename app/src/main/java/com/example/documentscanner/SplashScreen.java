package com.example.documentscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        auth=FirebaseAuth.getInstance();
        Handler h=new Handler();

        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(auth.getCurrentUser()!=null)
                {
                    Intent i=new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
                else
                {
                    Intent i=new Intent(SplashScreen.this,LoginActitvity.class);
                    startActivity(i);
                    finish();
                }

            }
        },3000);
    }
}
