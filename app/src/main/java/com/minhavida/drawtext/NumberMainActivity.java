package com.minhavida.drawtext;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NumberMainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_number);

        for (int k = 0; k < 10; k++)
        {
            int viewId = getResources().getIdentifier("number_"+k, "id", this.getPackageName());
            final int number = k;
            findViewById(viewId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NumberMainActivity.this, NumberActivity.class);
                    intent.putExtra("number", number);
                    startActivity(intent);
                }
            });
        }
    }
}
