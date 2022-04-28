package com.prince.friend.votingsyatem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prince.friend.votingsyatem.activities.HomeActivity;
import com.prince.friend.votingsyatem.activities.LoginActivity;

public class SplashScreen extends AppCompatActivity {

    // add these lines
    public static final String PREFERENCES = "prefKey";
    SharedPreferences sharedPreferences;
    public static final String IsLogIn = "islogin";
    // add these lines

    // creat simple project
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // change your splash screen code to this
        sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        boolean bol = sharedPreferences.getBoolean(IsLogIn,false);

        new Handler().postDelayed(()->{
            if(bol){
                startActivity(new Intent(SplashScreen.this, HomeActivity.class));
                finish();
            }else{
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                finish();
            }
        },3000);

    }
}