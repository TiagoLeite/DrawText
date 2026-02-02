package com.minhavida.drawtext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LetterConsonantActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private boolean autoSoundEnabled = false;
    private ImageView imageViewLetterConsonant, imageViewFeedback, ivPlaySound;
    private MediaPlayer mediaPlayer;
    private ImageClassifier tfClassifier;
    private float difficulty = 0f;
    private double DIFFICULTY_LEVEL = 0.8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_consonant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }

        number = getIntent().getIntExtra("letter", 0);

        autoSoundEnabled = getIntent().getBooleanExtra("auto_sound_enabled", false);

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);
        canvasView.setNumber(number);

        imageViewLetterConsonant = findViewById(R.id.iv_letter_consonant);
        imageViewFeedback = findViewById(R.id.iv_feedback);
        ivPlaySound = findViewById(R.id.iv_playsound);

        loadLetter();
        loadModel();

        final String arr[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        Button btFind = (Button)findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view)
            {
                Log.d("LetterConsonantActivity", "========================================");
                Log.d("LetterConsonantActivity", "🎯 USUÁRIO CLICOU EM VERIFICAR");
                Log.d("LetterConsonantActivity", "Letra esperada: " + arr[number]);
                Log.d("LetterConsonantActivity", "Nível de dificuldade: " + String.format("%.2f%%", DIFFICULTY_LEVEL * 100));
                Log.d("LetterConsonantActivity", "========================================");
                
                float[] arrayImage = canvasView.getPixelsArray();
                Classification cls = tfClassifier.recognize(arrayImage, 1);

                //saveStatisticsToSpreadSheet(arr[number], cls.getLabel(), cls.getConf(), DIFFICULTY_LEVEL);

                Log.d("LetterConsonantActivity", "========================================");
                Log.d("LetterConsonantActivity", "📝 COMPARAÇÃO:");
                Log.d("LetterConsonantActivity", "  Esperado: " + arr[number]);
                Log.d("LetterConsonantActivity", "  Predito: " + cls.getLabel());
                Log.d("LetterConsonantActivity", "  Confiança: " + String.format("%.2f%%", cls.getConf() * 100));
                Log.d("LetterConsonantActivity", "  Threshold: " + String.format("%.2f%%", DIFFICULTY_LEVEL * 100));

                boolean isConfusingPair = isConfusingLetterPair(arr[number], cls.getLabel());
                double effectiveThreshold = DIFFICULTY_LEVEL;
                
                if (isConfusingPair) {
                    effectiveThreshold = Math.min(0.999, DIFFICULTY_LEVEL + 0.03);
                    Log.d("LetterConsonantActivity", "  ⚠️  Par confuso detectado! Threshold ajustado: " + 
                          String.format("%.2f%%", effectiveThreshold * 100));
                }

                if (cls.getLabel().equals(arr[number]) && cls.getConf() > effectiveThreshold)
                {
                    Log.i("LetterConsonantActivity", "✅ RESPOSTA CORRETA!");
                    Log.d("LetterConsonantActivity", "========================================");
                    
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.correct_answer);
                    mediaPlayer.setVolume(0.025f, 0.025f);
                    mediaPlayer.start();
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.like, null));

                    imageViewFeedback.setVisibility(View.VISIBLE);
                    Animation zoomIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.zoom_out);
                    imageViewFeedback.setAnimation(zoomIn);
                    imageViewFeedback.startAnimation(zoomIn);

                    if (difficulty < 1f)
                        difficulty += .2;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            imageViewFeedback.setVisibility(View.GONE);
                            canvasView.clearCanvas();
                            loadLetter();
                        }
                    }, 2000);
                }
                else
                {
                    Log.w("LetterConsonantActivity", "❌ RESPOSTA INCORRETA!");
                    if (!cls.getLabel().equals(arr[number])) {
                        Log.w("LetterConsonantActivity", "  Motivo: Letra diferente");
                    }
                    if (cls.getConf() <= DIFFICULTY_LEVEL) {
                        Log.w("LetterConsonantActivity", "  Motivo: Confiança insuficiente");
                    }
                    Log.d("LetterConsonantActivity", "========================================");
                    
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.dislike, null));

                    imageViewFeedback.setVisibility(View.VISIBLE);
                    Animation zoomOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.zoom_out);
                    imageViewFeedback.setAnimation(zoomOut);
                    imageViewFeedback.startAnimation(zoomOut);
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.wrong_answer);
                    mediaPlayer.setVolume(0.05f, 0.05f);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            canvasView.clearCanvas();
                            imageViewFeedback.setVisibility(View.GONE);
                        }
                    }, 2000);

                }
            }
        });

    }


    void saveStatisticsToSpreadSheet(final String label, final String prediction, final float confidence,
                                     final double difficultyLevel) {
        StringRequest request = new StringRequest(StringRequest.Method.POST,
                "https://script.google.com/macros/s/AKfycbwrVBeqDs4FTFimDDa3J2AyAxMVbVpQPdAvAkRajX8IlGf5jP5CC9_hZUhgKTzhIGJccw/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("DEBUG", Objects.requireNonNull(error.getMessage()));
                    }
                }
        ) {
            @SuppressLint("DefaultLocale")
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", getOrCreateUserId());
                params.put("action", "addItem");
                params.put("label", label);
                params.put("prediction", prediction);
                params.put("confidence", String.format("%.8f", confidence));
                params.put("difficultyLevel", String.format("%.8f", difficultyLevel));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(20000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


    private String getOrCreateUserId(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String userIdString = prefs.getString("user_id_generated", null);

        if(userIdString == null){
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            //do your thing with PreferenceConnector
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_id_generated", uuid);
            editor.apply();
            return uuid;
        }

        return userIdString;
    }

    private void loadLetter()
    {
        Map<Integer, Character> map = new HashMap<>();
        map.put(1, 'b');
        map.put(2, 'c');
        map.put(3, 'd');
        map.put(5, 'f');
        map.put(6, 'g');
        map.put(7, 'h');
        map.put(8, 'i');
        map.put(9, 'j');
        map.put(10, 'k');
        map.put(11, 'l');
        map.put(12, 'm');
        map.put(13, 'n');
        map.put(15, 'p');
        map.put(16, 'q');
        map.put(17, 'r');
        map.put(18, 's');
        map.put(19, 't');
        map.put(21, 'v');
        map.put(22, 'w');
        map.put(23, 'x');
        map.put(24, 'y');
        map.put(25, 'z');

        int drawableNumberId = getResources().getIdentifier("dotted_letter_"+map.get(number),
                "drawable",
                this.getPackageName());

        imageViewLetterConsonant.setImageDrawable(AnimatedVectorDrawableCompat.create(
                this, drawableNumberId));

        imageViewLetterConsonant.setAlpha(1f-difficulty);

        Drawable drawable = imageViewLetterConsonant.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat)
        {
            AnimatedVectorDrawableCompat avdc = (AnimatedVectorDrawableCompat) drawable;
            avdc.start();
        }
        else if(drawable instanceof AnimatedVectorDrawable)
        {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // show menu when menu button is pressed
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menu.getItem(0).setChecked(true);
        menu.getItem(1).setChecked(false);
        menu.getItem(2).setChecked(false);
        menu.getItem(3).setChecked(false);
        menu.getItem(4).setChecked(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_nivel1) {
            DIFFICULTY_LEVEL = 0.8;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel2) {
            DIFFICULTY_LEVEL = 0.9;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel3) {
            DIFFICULTY_LEVEL = 0.97;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel4) {
            DIFFICULTY_LEVEL = 0.99;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel5) {
            DIFFICULTY_LEVEL = 0.999;
            item.setChecked(true);
        }

        return true;
    }

    /**
     * Verifica se duas letras formam um par confuso que requer maior confiança
     * Exemplos: O/Q, I/L, B/D, etc.
     */
    private boolean isConfusingLetterPair(String expected, String predicted) {
        if (expected == null || predicted == null) return false;
        
        // Normalizar para maiúsculas
        String exp = expected.toUpperCase();
        String pred = predicted.toUpperCase();
        
        // Pares confusos conhecidos (ordem não importa)
        String[][] confusingPairs = {
            {"O", "Q"},  // O e Q são muito similares
            {"O", "S"},  // O e S podem ser confundidos se mal desenhados
            {"I", "L"},  // I e L podem ser confundidos
            {"B", "D"},  // B e D espelhados
            {"M", "N"},  // M e N similares
            {"C", "G"},  // C e G podem ser confundidos
            {"P", "R"},  // P e R similares
            {"V", "Y"},  // V e Y similares
        };
        
        for (String[] pair : confusingPairs) {
            if ((exp.equals(pair[0]) && pred.equals(pair[1])) ||
                (exp.equals(pair[1]) && pred.equals(pair[0]))) {
                return true;
            }
        }
        
        return false;
    }

    private void loadModel()
    {
        try
        {
            // IMPORTANTE: inputSize deve ser 28 (tamanho do modelo EMNIST)
            tfClassifier = ImageClassifier.create(getAssets(),
                    "TensorFlow Lite", "cnn_letters.tflite",
                    "labels_letters.txt", 28, "input_1",
                    "dense_2/Softmax", true, 26);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tfClassifier instanceof ImageClassifier) {
            ((ImageClassifier) tfClassifier).close(); // ✅ Liberar recursos
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void clearCanvas (View v)
    {
        Log.d("debug", "Canvas cleaned!");
        Log.d("debug", canvasView.toString());
        canvasView.clearCanvas();
    }
}
