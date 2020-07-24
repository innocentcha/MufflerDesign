package com.weining.android.simple;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

public class DETEC extends AppCompatActivity {
    private double d,D,L,Li,Lo,tw,T;
    private double[] dArray = new double[3];
    private double[] DArray = new double[3];
    private double[] LArray = new double[3];
    private double[] LiArray = new double[3];
    private double[] LoArray = new double[3];
    private double[] twArray = new double[3];
    private double[] TArray = new double[3];
    private boolean[] isFilled = new boolean[3];
    private EditText DETEC_d1,DETEC_d2,DETEC_d3,DETEC_D1,DETEC_D2,DETEC_D3,DETEC_L1,DETEC_L2, DETEC_L3,DETEC_Li1,DETEC_Li2, DETEC_Li3,DETEC_Lo1,DETEC_Lo2, DETEC_Lo3,DETEC_tw1,DETEC_tw2, DETEC_tw3,DETEC_T1,DETEC_T2, DETEC_T3;
    private TextView DETEC_fpi1,DETEC_fpi2,DETEC_fpi3,DETEC_fpo1,DETEC_fpo2,DETEC_fpo3;
    private Button bt1,bt2,bt3;
    private LinearLayout fpoDetec,fpiDetec;

    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;

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
    int[] fpi = new int[3];
    int[] fpo = new int[3];
    int minHor;
    int maxHor;

    private int mode = 0;
    private float breRealXMax;
    private float breRealXMin;
    private float breRealYMax;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detec);

        ///设置ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_simple_DETEC);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("双边插入管扩张腔");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        //初始化UI
        initUI();

        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_detec);
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
        iv_canvas = (ImageView) findViewById(R.id.iv_canvas_DETEC);

        //绘制坐标
        printCoordinate();

        //printGraphic();

        iv_canvas.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public  boolean onTouch(View v, MotionEvent event){
                ImageView view = (ImageView) v;
                int imageWidth = screenWidth - 270;  //140 130
                int imageHeight = 490;               // 570-80
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    //单个手指触摸
                    case MotionEvent.ACTION_DOWN:
                        mode = 1;
                        break;

                    //两指触摸
                    case MotionEvent.ACTION_POINTER_DOWN:
                        float breXMax = Math.max(event.getX(1),event.getX(0));
                        float breXMin = Math.min(event.getX(1),event.getX(0));
                        float breYMax = Math.max(event.getY(1),event.getY(0));
                        breRealXMax = (breXMax-140)/imageWidth*space*8+minHor; //别忘记加上基数！！
                        breRealXMin = (breXMin-140)/imageWidth*space*8+minHor;
                        breRealYMax = maxTL-(breYMax-80)/imageHeight*maxTL;
                        //当两指间距大于10时，模式为2
                        if (getDistance(event) > 10f) {
                            mode = 2;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        // 手指离开屏幕时将临时值还原
                        mode = 0;
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = 0;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        //当两指缩放，计算缩放比例
                        if (mode == 2) {
                            if (getDistance(event) > 10f) {
                                float nowXMax = Math.max(event.getX(1),event.getX(0));
                                float nowXMin = Math.min(event.getX(1),event.getX(0));
                                float nowYMax = Math.max(event.getY(1),event.getY(0));
                                float widthUnit = (breRealXMax-breRealXMin)/(nowXMax-nowXMin);
                                minHor = (int)(breRealXMin-(nowXMin-140) * widthUnit);
                                maxHor = (int)(breRealXMax+(imageWidth+140-nowXMax) * widthUnit);
                                maxTL = (int)(breRealYMax/(imageHeight+80-nowYMax)*imageHeight);
                                if (minHor<0) minHor = 0;
                                space = (maxHor-minHor)/8;
                                if(space <= 80) space = 80;
                                maxTL = ((int)(maxTL+3.5))/7*7;
                                if(maxTL <= 7) maxTL = 7;

                                printGraphic();
                            }
                        }
                        break;
                }
                return true;
            }
        });

    }

    private void initUI(){
        DETEC_d1 = (EditText)findViewById(R.id.d_DETEC_1);
        DETEC_d2 = (EditText)findViewById(R.id.d_DETEC_2);
        DETEC_d3 = (EditText)findViewById(R.id.d_DETEC_3);
        DETEC_D1 = (EditText)findViewById(R.id.D_DETEC_1);
        DETEC_D2 = (EditText)findViewById(R.id.D_DETEC_2);
        DETEC_D3 = (EditText)findViewById(R.id.D_DETEC_3);
        DETEC_L1 = (EditText)findViewById(R.id.L_DETEC_1);
        DETEC_L2 = (EditText)findViewById(R.id.L_DETEC_2);
        DETEC_L3 = (EditText)findViewById(R.id.L_DETEC_3);
        DETEC_Li1 = (EditText)findViewById(R.id.Li_DETEC_1);
        DETEC_Li2 = (EditText)findViewById(R.id.Li_DETEC_2);
        DETEC_Li3 = (EditText)findViewById(R.id.Li_DETEC_3);
        DETEC_Lo1 = (EditText)findViewById(R.id.Lo_DETEC_1);
        DETEC_Lo2 = (EditText)findViewById(R.id.Lo_DETEC_2);
        DETEC_Lo3 = (EditText)findViewById(R.id.Lo_DETEC_3);
        DETEC_tw1 = (EditText)findViewById(R.id.tw_DETEC_1);
        DETEC_tw2 = (EditText)findViewById(R.id.tw_DETEC_2);
        DETEC_tw3 = (EditText)findViewById(R.id.tw_DETEC_3);
        DETEC_T1 = (EditText)findViewById(R.id.T_DETEC_1);
        DETEC_T2 = (EditText)findViewById(R.id.T_DETEC_2);
        DETEC_T3 = (EditText)findViewById(R.id.T_DETEC_3);
        bt1 = (Button)findViewById(R.id.bt_DETEC_1);
        bt2 = (Button)findViewById(R.id.bt_DETEC_2);
        bt3 = (Button)findViewById(R.id.bt_DETEC_3);
        DETEC_fpi1 = (TextView)findViewById(R.id.fpi_DETEC_1);
        DETEC_fpi2 = (TextView)findViewById(R.id.fpi_DETEC_2);
        DETEC_fpi3 = (TextView)findViewById(R.id.fpi_DETEC_3);
        DETEC_fpo1 = (TextView)findViewById(R.id.fpo_DETEC_1);
        DETEC_fpo2 = (TextView)findViewById(R.id.fpo_DETEC_2);
        DETEC_fpo3 = (TextView)findViewById(R.id.fpo_DETEC_3);
        fpoDetec = (LinearLayout) findViewById(R.id.fpo_DETEC);
        fpiDetec = (LinearLayout) findViewById(R.id.fpi_DETEC);
        fpoDetec.setVisibility(View.GONE);
        fpiDetec.setVisibility(View.GONE);

        space=300;//控制横坐标的标尺
        maxTL = 105;//控制纵坐标的标尺
        minHor = 0;
        maxHor = 2100;
        for(int i=0; i<3; i++) isFilled[i] = false;
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_d1,S_D1,S_L1,S_Li1,S_Lo1,S_tw1,S_T1;
                S_d1=DETEC_d1.getText().toString();
                S_D1=DETEC_D1.getText().toString();
                S_L1=DETEC_L1.getText().toString();
                S_Li1=DETEC_Li1.getText().toString();
                S_Lo1=DETEC_Lo1.getText().toString();
                S_tw1=DETEC_tw1.getText().toString();
                S_T1=DETEC_T1.getText().toString();
                isFilled[0] = false;
                if(!S_d1.equals("") && !S_D1.equals("") && !S_L1.equals("") && !S_Li1.equals("") && !S_Lo1.equals("") && !S_tw1.equals("") && !S_T1.equals("")  ){
                    isFilled[0] = true;
                    d=Double.valueOf(S_d1).doubleValue();
                    D=Double.valueOf(S_D1).doubleValue();
                    L=Double.valueOf(S_L1).doubleValue();
                    Li=Double.valueOf(S_Li1).doubleValue();
                    Lo=Double.valueOf(S_Lo1).doubleValue();
                    tw=Double.valueOf(S_tw1).doubleValue();
                    T=Double.valueOf(S_T1).doubleValue();
                    dArray[0] = d;
                    DArray[0] = D;
                    LArray[0] = L;
                    LiArray[0] = Li;
                    LoArray[0] = Lo;
                    twArray[0] = tw;
                    TArray[0] = T;
                    printGraphic();
                    DETEC_fpi1.setText(String.valueOf(fpi[0]));
                    fpiDetec.setVisibility(View.VISIBLE);
                    DETEC_fpo1.setText(String.valueOf(fpo[0]));
                    fpoDetec.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_d2,S_D2,S_L2,S_Li2,S_Lo2,S_tw2,S_T2;
                S_d2=DETEC_d2.getText().toString();
                S_D2=DETEC_D2.getText().toString();
                S_L2=DETEC_L2.getText().toString();
                S_Li2=DETEC_Li2.getText().toString();
                S_Lo2=DETEC_Lo2.getText().toString();
                S_tw2=DETEC_tw2.getText().toString();
                S_T2=DETEC_T2.getText().toString();
                isFilled[1] = false;
                if(!S_d2.equals("") && !S_D2.equals("") && !S_L2.equals("") && !S_Li2.equals("") && !S_Lo2.equals("") && !S_tw2.equals("")&& !S_T2.equals("") ){
                    isFilled[0] = true;
                    d=Double.valueOf(S_d2).doubleValue();
                    D=Double.valueOf(S_D2).doubleValue();
                    L=Double.valueOf(S_L2).doubleValue();
                    Li=Double.valueOf(S_Li2).doubleValue();
                    Lo=Double.valueOf(S_Lo2).doubleValue();
                    tw=Double.valueOf(S_tw2).doubleValue();
                    T=Double.valueOf(S_T2).doubleValue();
                    dArray[1] = d;
                    DArray[1] = D;
                    LArray[1] = L;
                    LiArray[1] = Li;
                    LoArray[1] = Lo;
                    twArray[1] = tw;
                    TArray[1] = T;
                    printGraphic();
                    DETEC_fpi2.setText(String.valueOf(fpi[1]));
                    fpiDetec.setVisibility(View.VISIBLE);
                    DETEC_fpo2.setText(String.valueOf(fpo[1]));
                    fpoDetec.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String S_d3,S_D3,S_L3,S_Li3,S_Lo3,S_tw3,S_T3;
                S_d3=DETEC_d3.getText().toString();
                S_D3=DETEC_D3.getText().toString();
                S_L3=DETEC_L3.getText().toString();
                S_Li3=DETEC_Li3.getText().toString();
                S_Lo3=DETEC_Lo3.getText().toString();
                S_tw3=DETEC_tw3.getText().toString();
                S_T3=DETEC_T3.getText().toString();
                isFilled[2] = false;
                if(!S_d3.equals("") && !S_D3.equals("") && !S_L3.equals("") && !S_Li3.equals("") && !S_Lo3.equals("") && !S_tw3.equals("") && !S_T3.equals("")){
                    isFilled[2] = true;
                    d=Double.valueOf(S_d3).doubleValue();
                    D=Double.valueOf(S_D3).doubleValue();
                    L=Double.valueOf(S_L3).doubleValue();
                    Li=Double.valueOf(S_Li3).doubleValue();
                    Lo=Double.valueOf(S_Lo3).doubleValue();
                    tw=Double.valueOf(S_tw3).doubleValue();
                    T=Double.valueOf(S_T3).doubleValue();
                    dArray[2] = d;
                    DArray[2] = D;
                    LArray[2] = L;
                    LiArray[2] = Li;
                    LoArray[2] = Lo;
                    twArray[2] = tw;
                    TArray[2] = T;
                    printGraphic();
                    DETEC_fpi3.setText(String.valueOf(fpi[2]));
                    fpiDetec.setVisibility(View.VISIBLE);
                    DETEC_fpo3.setText(String.valueOf(fpo[2]));
                    fpoDetec.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getContext(),"请输入完整数据",Toast.LENGTH_SHORT).show();
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
        double PI=3.14159,c0;
        double m,TL,f,kl,kli,klo,in,M;
        for(int j=0; j<3; j++) {
            if (!isFilled[j]) continue;
            d = dArray[j];
            D = DArray[j];
            L = LArray[j];
            Li = LiArray[j];
            Lo = LoArray[j];
            tw = twArray[j];
            T = TArray[j];
            if (j == 0) paint.setColor(0xFF4A7EBB);
            else if (j == 1) paint.setColor(0xFFBE4B48);
            else if (j == 2) paint.setColor(0xFF98B954);
            float gap = (float)(screenWidth-270)/(maxHor-minHor);
            c0 =20.05*Math.sqrt(273+T);
            M=(0.005177+0.0909*(D/d)+0.537*(tw/d)-0.008594*(D/d)*(D/d)+0.02616*(D*tw/d/d)-5.425*(tw/d)*(tw/d))*d;
            fpi[j]=(int)(1.0/4*c0/(Li+M)*1000);
            fpo[j]=(int)(1.0/4*c0/(Lo+M)*1000);
            for (int i = minHor; i <= maxHor; i++) {
                m=D*D/d/d;
                f=i;
                kl=2*PI*f/c0*L/1000;
                kli=2*PI*f/c0*(Li+M)/1000;
                klo=2*PI*f/c0*(Lo+M)/1000;
                in=(m-1/m)/2*Math.sin(kl)/Math.cos(kli)/Math.cos(klo);
                TL=10*Math.log10(1+in*in);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simplemenu, menu);
        return true;
    }

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
            case R.id.menu_SEC:
                intent=new Intent(this, SEC.class);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_SETEC:
                intent=new Intent(this, SETEC.class);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_DETEC:
                intent=new Intent(this, DETEC.class);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_HR:
                intent=new Intent(this, HR.class);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_QWT:
                intent=new Intent(this, QWT.class);
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
        DETEC_d1.setText("");
        DETEC_d2.setText("");
        DETEC_d3.setText("");
        DETEC_D1.setText("");
        DETEC_D2.setText("");
        DETEC_D3.setText("");
        DETEC_L1.setText("");
        DETEC_L2.setText("");
        DETEC_L3.setText("");
        DETEC_T1.setText("");
        DETEC_T2.setText("");
        DETEC_T3.setText("");
        DETEC_Li1.setText("");
        DETEC_Li2.setText("");
        DETEC_Li3.setText("");
        DETEC_Lo1.setText("");
        DETEC_Lo2.setText("");
        DETEC_Lo3.setText("");
        DETEC_tw1.setText("");
        DETEC_tw2.setText("");
        DETEC_tw3.setText("");
        DETEC_fpi1.setText("");
        DETEC_fpi2.setText("");
        DETEC_fpi3.setText("");
        DETEC_fpo1.setText("");
        DETEC_fpo2.setText("");
        DETEC_fpo3.setText("");
        for(int i=0; i<3; i++) isFilled[i] = false;
    }

    private int UIdistance(int num){
        if(num>=100) return 57;
        else if(num>=10) return 47;
        else return 37;
    }
    /*获取两指之间的距离*/
    private float getDistance(MotionEvent event){
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;
    }
}

