package com.example.albertotsang.foreigner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailsActivity extends AppCompatActivity {

    String iduser = null;
    String photo_url_str;
    ImageView profile_photo;
    TextView nametext;
    TextView occupationtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        profile_photo = (ImageView) findViewById(R.id.profileView);
        nametext = (TextView) findViewById(R.id.cardName);
        occupationtext = (TextView) findViewById(R.id.cardOccupation);


        Bundle extras = getIntent().getExtras();
        iduser = extras.getString("iduser");

        nametext.setText(extras.getString("name") + ", " + extras.getInt("age"));
        occupationtext.setText(extras.getString("occupation"));

        photo_url_str = "https://graph.facebook.com/" + iduser + "/picture?height=600&?width=600";
        //photo_url_str = "http://images.en.yibada.com/data/images/full/16847/paulina-vega.jpg?w=685";

        Glide.with(DetailsActivity.this).load(photo_url_str).into(profile_photo);
    }

    @Override
    protected void onStop() {
        this.supportFinishAfterTransition();
        super.onStop();

    }
}
