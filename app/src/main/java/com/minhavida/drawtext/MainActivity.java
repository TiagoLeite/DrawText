package com.minhavida.drawtext;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CardView cardNumber = findViewById(R.id.card_numbers);
        cardNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent  = new Intent(MainActivity.this, NumberMainActivity.class);
                CheckBox cb = cardNumber.findViewById(R.id.cb_enable_sound_numbers);
                intent.putExtra("enable_auto_sound", cb.isChecked());
                startActivity(intent);
                //finish();
            }
        });

        final CardView cardLetter = findViewById(R.id.card_letter);
        cardLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent  = new Intent(MainActivity.this,
                        LetterVowelMainActivity.class);
                CheckBox cb = cardLetter.findViewById(R.id.cb_enable_sound_vowels);
                intent.putExtra("enable_auto_sound", cb.isChecked());
                startActivity(intent);
                //finish();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setHomeButtonEnabled(false);
        }
    }
}
