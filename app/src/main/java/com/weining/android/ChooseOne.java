package com.weining.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.weining.android.db.DataAll;


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

    public void onClick(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.choose_one_chuankong:
                if(isBeta){
                    intent=new Intent(this, BetaActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    intent=new Intent(this, GraphicActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.choose_one_simple:
                intent=new Intent(this, SimpleChoose.class);
                startActivity(intent);
                finish();
                break;
            case R.id.choose_one_charu:
                intent=new Intent(this, StickActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
        }
    }

}
