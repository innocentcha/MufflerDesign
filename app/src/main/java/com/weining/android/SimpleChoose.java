package com.weining.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.weining.android.simple.DETEC;
import com.weining.android.simple.HR;
import com.weining.android.simple.QWT;
import com.weining.android.simple.SEC;
import com.weining.android.simple.SETEC;

public class SimpleChoose extends AppCompatActivity implements View.OnClickListener{

    private Button sec;
    private Button setec;
    private Button detec;
    private Button hr;
    private Button qwt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_simple_choose);
        sec = (Button)findViewById(R.id.choose_SEC);
        setec = (Button)findViewById(R.id.choose_SETEC);
        detec = (Button)findViewById(R.id.choose_DETEC);
        hr = (Button)findViewById(R.id.choose_HR);
        qwt = (Button)findViewById(R.id.choose_QWT);
        sec.setOnClickListener(this);
        setec.setOnClickListener(this);
        detec.setOnClickListener(this);
        hr.setOnClickListener(this);
        qwt.setOnClickListener(this);
    }

    public void onClick(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.choose_SEC:
                intent=new Intent(this, SEC.class);
                startActivity(intent);
                finish();
                break;
            case R.id.choose_SETEC:
                intent=new Intent(this, SETEC.class);
                startActivity(intent);
                finish();
                break;
            case R.id.choose_DETEC:
                intent=new Intent(this, DETEC.class);
                startActivity(intent);
                finish();
                break;

            case  R.id.choose_HR:
                intent=new Intent(this, HR.class);
                startActivity(intent);
                finish();
                break;

            case  R.id.choose_QWT:
                intent=new Intent(this, QWT.class);
                startActivity(intent);
                finish();
                break;
            default:
        }
    }
}
