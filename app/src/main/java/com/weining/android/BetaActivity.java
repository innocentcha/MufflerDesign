package com.weining.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.weining.android.ChooseOne;
import com.weining.android.GraphicActivity;
import com.weining.android.R;

import static org.litepal.LitePalApplication.getContext;

public class BetaActivity extends AppCompatActivity {
    private double V,d,D,n,tw,T;
    private double[] VArray = new double[3];
    private double[] dArray = new double[3];
    private double[] DArray = new double[3];
    private double[] nArray = new double[3];
    private double[] twArray = new double[3];
    private double[] TArray = new double[3];
    private boolean[] isFilled = new boolean[3];
    private EditText Beta_V1,Beta_V2,Beta_V3,Beta_d1,Beta_d2,Beta_d3,Beta_D1,Beta_D2,Beta_D3,Beta_n1,Beta_n2,Beta_n3,Beta_tw1,Beta_tw2,Beta_tw3,Beta_T1,Beta_T2,Beta_T3;
    private TextView Beta_fp1,Beta_fp2,Beta_fp3,fp_text;
    private Button bt1,bt2,bt3;
    private LinearLayout fpBeta;
    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;

    private EditText BETA_TL,BETA_minF,BETA_maxF;
    private Button BETA_tlBt,BETA_fBt;

    //屏幕的长宽
    private int screenWidth;
    private int screenHeight;

    //画笔变量
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    //坐标图位置
    int x1, x2, y1, y2;

    //最大值影响坐标
//    double max;
    //纵坐标刻度
    int maxTL;
    int space;
    int fp;
    int minHor;
    int maxHor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beta);

        ///设置ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_simple_Beta);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("穿孔消声器");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        //初始化UI
        initUI();

        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_beta);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearAll();
            }
        });


        //获取屏幕的长宽
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        //初始化默认画笔，线宽3，颜色黑色
        paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        //初始化画板
        iv_canvas = (ImageView) findViewById(R.id.iv_canvas_BETA);

        //绘制坐标
        printCoordinate();

        //printGraphic();

    }

    private void initUI(){
        Beta_V1 = (EditText) findViewById(R.id.V_BETA_1);
        Beta_V2 = (EditText) findViewById(R.id.V_BETA_2);
        Beta_V3 = (EditText) findViewById(R.id.V_BETA_3);
        Beta_d1 = (EditText) findViewById(R.id.d_BETA_1);
        Beta_d2 = (EditText) findViewById(R.id.d_BETA_2);
        Beta_d3 = (EditText) findViewById(R.id.d_BETA_3);
        Beta_n1 = (EditText) findViewById(R.id.n_BETA_1);
        Beta_n2 = (EditText) findViewById(R.id.n_BETA_2);
        Beta_n3 = (EditText) findViewById(R.id.n_BETA_3);
        Beta_D1 = (EditText) findViewById(R.id.D_BETA_1);
        Beta_D2 = (EditText) findViewById(R.id.D_BETA_2);
        Beta_D3 = (EditText) findViewById(R.id.D_BETA_3);
        Beta_tw1 = (EditText) findViewById(R.id.tw_BETA_1);
        Beta_tw2 = (EditText) findViewById(R.id.tw_BETA_2);
        Beta_tw3 = (EditText) findViewById(R.id.tw_BETA_3);
        Beta_T1 = (EditText) findViewById(R.id.T_BETA_1);
        Beta_T2 = (EditText) findViewById(R.id.T_BETA_2);
        Beta_T3 = (EditText) findViewById(R.id.T_BETA_3);
        bt1 = (Button)findViewById(R.id.bt_BETA_1);
        bt2 = (Button)findViewById(R.id.bt_BETA_2);
        bt3 = (Button)findViewById(R.id.bt_BETA_3);
        Beta_fp1 = (TextView) findViewById(R.id.fp_BETA_1);
        Beta_fp2 = (TextView) findViewById(R.id.fp_BETA_2);
        Beta_fp3 = (TextView) findViewById(R.id.fp_BETA_3);
        fp_text = (TextView)findViewById(R.id.fp_BETA_text);
        fpBeta = (LinearLayout) findViewById(R.id.fp_BETA);
        fpBeta.setVisibility(View.GONE);
        BETA_TL = (EditText) findViewById(R.id.beta_tl);
        BETA_minF = (EditText) findViewById(R.id.beta_min_f);
        BETA_maxF = (EditText) findViewById(R.id.beta_max_f);
        BETA_tlBt = (Button) findViewById(R.id.beta_tl_bt);
        BETA_fBt = (Button) findViewById(R.id.beta_f_bt) ;
        space= 300;
        maxTL = 70;//控制纵坐标的标尺
        minHor = 0;
        maxHor = 2100;
        for(int i=0; i<3; i++) isFilled[i] = false;
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_V1,S_d1,S_D1,S_n1,S_tw1,S_T1;
                S_V1=Beta_V1.getText().toString();
                S_d1=Beta_d1.getText().toString();
                S_D1=Beta_D1.getText().toString();
                S_n1=Beta_n1.getText().toString();
                S_T1=Beta_T1.getText().toString();
                S_tw1=Beta_tw1.getText().toString();
                isFilled[0] = false;
                if(!S_V1.equals("")  && !S_d1.equals("") && !S_D1.equals("") && !S_n1.equals("") && !S_T1.equals("") && !S_tw1.equals("")){
                    isFilled[0] = true;
                    V=Double.valueOf(S_V1).doubleValue();
                    d=Double.valueOf(S_d1).doubleValue();
                    D=Double.valueOf(S_D1).doubleValue();
                    n=Double.valueOf(S_n1).doubleValue();
                    T=Double.valueOf(S_T1).doubleValue();
                    tw=Double.valueOf(S_tw1).doubleValue();
                    VArray[0] = V;
                    dArray[0] = d;
                    DArray[0] = D;
                    nArray[0] = n;
                    twArray[0] = tw;
                    TArray[0] = T;
                    printGraphic();
                    Beta_fp1.setText(String.valueOf(fp));
                    fpBeta.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_V2,S_d2,S_D2,S_n2,S_T2,S_tw2;
                S_V2=Beta_V2.getText().toString();
                S_d2=Beta_d2.getText().toString();
                S_D2=Beta_D2.getText().toString();
                S_n2=Beta_n2.getText().toString();
                S_T2=Beta_T2.getText().toString();
                S_tw2=Beta_tw2.getText().toString();
                isFilled[1] = false;
                if(!S_V2.equals("") && !S_d2.equals("") && !S_D2.equals("") && !S_n2.equals("") && !S_T2.equals("") && !S_tw2.equals("")){
                    isFilled[1] = true;
                    V=Double.valueOf(S_V2).doubleValue();
                    d=Double.valueOf(S_d2).doubleValue();
                    D=Double.valueOf(S_D2).doubleValue();
                    n=Double.valueOf(S_n2).doubleValue();
                    T=Double.valueOf(S_T2).doubleValue();
                    tw=Double.valueOf(S_tw2).doubleValue();
                    VArray[1] = V;
                    dArray[1] = d;
                    DArray[1] = D;
                    nArray[1] = n;
                    twArray[1] = tw;
                    TArray[1] = T;
                    printGraphic();
                    Beta_fp2.setText(String.valueOf(fp));
                    fp_text.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_V3,S_d3,S_D3,S_n3,S_tw3,S_T3;
                S_V3=Beta_V3.getText().toString();
                S_d3=Beta_d3.getText().toString();
                S_D3=Beta_D3.getText().toString();
                S_n3=Beta_n3.getText().toString();
                S_T3=Beta_T3.getText().toString();
                S_tw3=Beta_tw3.getText().toString();
                isFilled[2] = false;
                if(!S_V3.equals("") && !S_d3.equals("") && !S_D3.equals("") && !S_n3.equals("") && !S_T3.equals("") && !S_tw3.equals("")){
                    isFilled[2] = true;
                    V=Double.valueOf(S_V3).doubleValue();
                    d=Double.valueOf(S_d3).doubleValue();
                    D=Double.valueOf(S_D3).doubleValue();
                    n=Double.valueOf(S_n3).doubleValue();
                    T=Double.valueOf(S_T3).doubleValue();
                    tw=Double.valueOf(S_tw3).doubleValue();
                    VArray[2] = V;
                    dArray[2] = d;
                    DArray[2] = D;
                    nArray[2] = n;
                    twArray[2] = tw;
                    TArray[2] = T;
                    printGraphic();
                    Beta_fp3.setText(String.valueOf(fp));
                    fp_text.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        BETA_tlBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(BETA_TL.getText())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"请输入纵坐标最大值",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    maxTL = Integer.valueOf(BETA_TL.getText().toString());
                    printGraphic();
                }
            }
        });
        BETA_fBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(BETA_minF.getText()) || TextUtils.isEmpty(BETA_maxF.getText())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"请输入横坐标最大值或最小值",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    int minHorin = 0;
                    if(!TextUtils.isEmpty(BETA_minF.getText())) minHorin = Integer.valueOf(BETA_minF.getText().toString());
                    if(!TextUtils.isEmpty(BETA_maxF.getText())) maxHor = Integer.valueOf(BETA_maxF.getText().toString());
                    if(minHor >= maxHor){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"请确保横坐标最小值小于最大值",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        minHor = minHorin;
                        space = (maxHor-minHor)/7;
                        clearCanvas();
                        printGraphic();
                    }
                }
            }
        });
    }
    private void printCoordinate() {
        //第一次绘图初始化内存图片，指定背景为白色
        if (baseBitmap == null) {
            baseBitmap = Bitmap.createBitmap(screenWidth, 700, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
            //canvas.drawColor(Color.WHITE);//背景变白色会有点突兀
        }

        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        //参考坐标点
        x1 = 140;
        y1 = 80;
        x2 = screenWidth - 130;
        y2 = 570;
        int arrowSize = 14;
        int timesSize = 2 * arrowSize;

        //下横线
        canvas.drawLine(x1, y2, x2, y2, paint);
        //左竖线
        canvas.drawLine(x1, y1, x1, y2, paint);
        //横坐标箭头
        canvas.drawLine(x2, y2, x2 + timesSize, y2, paint);
        canvas.drawLine(x2 + timesSize, y2, x2 + timesSize - arrowSize, y2 + arrowSize / 2, paint);
        canvas.drawLine(x2 + timesSize, y2, x2 + timesSize - arrowSize, y2 - arrowSize / 2, paint);
        //纵坐标箭头
        canvas.drawLine(x1, y1, x1, y1 - timesSize, paint);
        canvas.drawLine(x1, y1 - timesSize, x1 + arrowSize / 2, y1 - timesSize + arrowSize, paint);
        canvas.drawLine(x1, y1 - timesSize, x1 - arrowSize / 2, y1 - timesSize + arrowSize, paint);

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2);
        //上横线
        canvas.drawLine(x1, y1, x2, y1, paint);
        //右竖线
        //canvas.drawLine(x2, y1, x2, y2, paint);

        paint.setStrokeWidth(1);
        //网格：横线
        for (int y = y1; y < y2; y = y + 70) {
            canvas.drawLine(x1, y, x2, y, paint);
        }
        //网格：竖线
        int blanking = (x2-x1)/7;
        for (int x = x1; x <= x2; x = x + blanking) {
            canvas.drawLine(x, y1, x, y2, paint);
        }

        //坐标系文字
        paint.setTextSize(28);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);//抗齿距
        paint.setShader(null);
        canvas.drawText("Frequency/HZ", screenWidth / 2 - 80, y2 + 90, paint);
        canvas.rotate(-90);//旋转坐标系 实现字的旋转功能
        canvas.drawText("TL/dB", -363, x1 - 68, paint);
        canvas.rotate(90);//将坐标轴旋转回去

        //纵坐标标尺
        canvas.drawText(String.valueOf(maxTL), x1 - UIdistance(maxTL), y1 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 6), x1 - UIdistance(maxTL / 7 * 6), y1 + (y2 - y1) / 7 * 1 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 5), x1 - UIdistance(maxTL / 7 * 5), y1 + (y2 - y1) / 7 * 2 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 4), x1 - UIdistance(maxTL / 7 * 4), y1 + (y2 - y1) / 7 * 3 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 3), x1 - UIdistance(maxTL / 7 * 3), y1 + (y2 - y1) / 7 * 4 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 2), x1 - UIdistance(maxTL / 7 * 2), y1 + (y2 - y1) / 7 * 5 + 12, paint);
        canvas.drawText(String.valueOf(maxTL / 7 * 1), x1 - UIdistance(maxTL / 7 * 1), y1 + (y2 - y1) / 7 * 6 + 12, paint);
        canvas.drawText("0", x1 - UIdistance(maxTL / 7 * 0), y2 + 10, paint);

        //横坐标标尺
        int i = 0;
        for (int x = x1; x <= x2; x = x + blanking) {
            if (minHor + i == 0) {
                canvas.drawText(String.valueOf(minHor), x , y2 + 40, paint);
            } else if(minHor + i*space<100) {
                canvas.drawText(String.valueOf(minHor + i * space), x - 13, y2 + 40, paint);
            }else if(minHor + i*space<1000){
                canvas.drawText(String.valueOf(minHor + i * space), x - 22, y2 + 40, paint);
            }else if(minHor + i*space<10000){
                canvas.drawText(String.valueOf(minHor + i * space), x - 29, y2 + 40, paint);
            }else if(minHor + i*space<100000){
                canvas.drawText(String.valueOf(minHor + i * space), x - 33, y2 + 40, paint);
            }

            i++;
        }

        iv_canvas.setImageBitmap(baseBitmap);
    }

    private void printGraphic() {
        clearCanvas();
        printCoordinate();
        if (baseBitmap == null) {
            baseBitmap = Bitmap.createBitmap(screenWidth, 700, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
        }

        paint.setStrokeWidth(4);
        float x, y;
        //上一次绘图坐标点 drawPoint的参数要求float
        float usedx = 0, usedy = 0;
        double PI=3.14159,c0,c,sectionTrous,sectionConduit;
        double Leq,TL,f,in,m;
        for(int j=0; j<3; j++) {
            if (!isFilled[j]) continue;
            V = VArray[j];
            d = dArray[j];
            D = DArray[j];
            n = nArray[j];
            tw = twArray[j];
            T = TArray[j];
            if (j == 0) paint.setColor(0xFF4A7EBB);
            else if (j == 1) paint.setColor(0xFFBE4B48);
            else if (j == 2) paint.setColor(0xFF98B954);
            float gap = (float)(screenWidth-270)/(maxHor-minHor);
            c = 20.05 * Math.sqrt(273+T);
            sectionTrous = d*d*PI/4000000;
            c0=n*sectionTrous/(tw/1000+0.8*Math.sqrt(sectionTrous));
            fp=(int)((0.5*c/PI)*Math.sqrt(c0/V*1000));
            sectionConduit = D*D*PI/4000000;
            Log.d("deta test c",String.valueOf(c));
            Log.d("deta test c0",String.valueOf(c0));
            Log.d("deta test fp",String.valueOf(fp));
            for (int i = minHor; i <= maxHor; i++) {
                f = i;
                in = 2*PI*f*1.2/c0-1.2*c*c/(2*PI*f*V/1000);
                TL = 10*Math.log10(1+0.25*(c*1.2/sectionConduit)*(c*1.2/sectionConduit)/in/in);
                //横坐标   115对应space长度即对应115/space个点
                x = gap * (i-minHor) + x1;
                //纵坐标 y2-y1=490对应35  相当于每个点14
                y = y2 - (float) TL * (y2 - y1) / maxTL;
                canvas.drawPoint(x, y, paint);
                //第一次不连线
                if (i != minHor) canvas.drawLine(usedx, usedy, x, y, paint);
                usedx = x;
                usedy = y;
            }
        }

        iv_canvas.setImageBitmap(baseBitmap);
    }

    //action按钮注册
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.simplemenu, menu);
//        return true;
//    }

    //返回到登录界面事件和action按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, ChooseOne.class);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_simple_end:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            default:
                break;
        }
        return true;
    }

    //重置画板
    private void clearCanvas() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        printCoordinate();
        swipeRefresh.setRefreshing(false);
    }

    //重置所有
    private void clearAll() {
        clearCanvas();
        Beta_V1.setText("");
        Beta_V2.setText("");
        Beta_V3.setText("");
        Beta_d1.setText("");
        Beta_d2.setText("");
        Beta_d3.setText("");
        Beta_n1.setText("");
        Beta_n2.setText("");
        Beta_n3.setText("");
        Beta_D1.setText("");
        Beta_D2.setText("");
        Beta_D3.setText("");
        Beta_tw1.setText("");
        Beta_tw2.setText("");
        Beta_tw3.setText("");
        Beta_T1.setText("");
        Beta_T2.setText("");
        Beta_T3.setText("");
        Beta_fp1.setText("");
        Beta_fp2.setText("");
        Beta_fp3.setText("");
        for(int i=0; i<3; i++) isFilled[i] = false;
    }

    private int UIdistance(int num){
        if(num>=100) return 57;
        else if(num>=10) return 47;
        else return 37;
    }
}

