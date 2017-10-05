package com.example.albertotsang.foreigner;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;


public class MainActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());

        //this handler delays start screen for 5 seconds
        new Handler().postDelayed(new Runnable() {
            public void run() {

                //After 5 seconds check current facebook session login status
                if (AccessToken.getCurrentAccessToken() != null) {
                    // current facebook session is not null I going to init without login
                    Log.d("Prueba", "Objeto de facebook no es nulo");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    //Inicio el siguiente activity con una animacion
                    //overridePendingTransition(R.anim.my_ani_in, R.anim.hold);

                    //Close this ctivity
                    finish();
                } else {
                    //if facebook session is null go to login screen
                    Log.d("Prueba", "Objeto de Facebook es nulo");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    //Inicio el siguiente activity con una animacion
                    //overridePendingTransition(R.anim.my_ani_in, R.anim.hold);

                    //Close this activity
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }


    public void onDestroy() {
        super.onDestroy();
    }


}
