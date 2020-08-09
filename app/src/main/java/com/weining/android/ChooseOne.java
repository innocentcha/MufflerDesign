package com.weining.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class ChooseOne extends AppCompatActivity implements View.OnClickListener{
    private Button chuankong;
    private Button simple;
    private Button charu;
    private SharedPreferences pref;
    boolean isBeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_choose_one);

        chuankong=(Button)findViewById(R.id.choose_one_chuankong);
        simple=(Button)findViewById(R.id.choose_one_simple);
        charu=(Button)findViewById(R.id.choose_one_charu);
        chuankong.setOnClickListener(this);
        simple.setOnClickListener(this);
        charu.setOnClickListener(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isBeta = pref.getBoolean("isBeta",false);
        if(isBeta) charu.setVisibility(View.INVISIBLE);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_one_chuankong:
                if (isBeta) {
                    startNewActivity(BetaActivity.class);
                } else {
                    startNewActivity(GraphicActivity.class);
                }
                break;
            case R.id.choose_one_simple:
                startNewActivity(SimpleChoose.class);
                break;
            case R.id.choose_one_charu:
                startNewActivity(StickActivity.class);
                break;
            default:
        }
    }

    private void startNewActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish();
    }

}
