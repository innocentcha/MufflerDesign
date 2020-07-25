package com.weining.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(this, Register.class);
        startActivity(intent);
        finish();
//        Intent intent=new Intent(this, GraphicActivity.class);
//        startActivity(intent);
//        finish();
    }
}
