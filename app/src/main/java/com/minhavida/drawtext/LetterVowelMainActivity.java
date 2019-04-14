package com.minhavida.drawtext;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class LetterVowelMainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_letter);

        final char[] letters = {'A', 'E', 'I', 'O', 'U'};

        final boolean autoSoundEnabled = getIntent().getBooleanExtra("enable_auto_sound",
                false);

        for (int k = 0; k < 5; k++)
        {
            int viewId = getResources().getIdentifier("letter_"+letters[k], "id", this.getPackageName());
            final int finalK = k;
            findViewById(viewId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LetterVowelMainActivity.this, LetterVowelActivity.class);
                    intent.putExtra("letter", finalK);
                    intent.putExtra("auto_sound_enabled", autoSoundEnabled);
                    startActivity(intent);
                }
            });
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
