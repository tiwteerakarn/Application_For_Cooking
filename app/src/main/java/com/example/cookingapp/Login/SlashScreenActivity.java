package com.example.cookingapp.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cookingapp.R;

public class SlashScreenActivity extends AppCompatActivity {
    private final  int slash_time = 4000;
    ImageView imageView;
    TextView textView;
    Animation annotation,bottonanime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slash_screen);
        imageView = findViewById(R.id.imageView);
        //textView = findViewById(R.id.textView);

        annotation = AnimationUtils.loadAnimation(this,
                R.anim.side_anim);
        bottonanime = AnimationUtils.loadAnimation(this,
                R.anim.botton_anime);
        imageView.setAnimation(annotation);
        //textView.setAnimation(bottonanime);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SlashScreenActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();

            }
        },slash_time);
    }
}
