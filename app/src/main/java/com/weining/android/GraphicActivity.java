package com.weining.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.weining.android.db.DataAll;
import com.weining.android.myconfiguration.Configure;
import com.weining.android.myconfiguration.ConfigureAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.litepal.LitePalApplication.getContext;

public class GraphicActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Button showAll;
    private Button showAdd;
    private Button saveBt;

    private TextView saveSpan1,saveCount1,saveDevol1,saveDegree1,savePeek1;
    private ImageView saveCirPic1,saveAxialPic1;
    private TextView saveSpan2,saveCount2,saveDevol2,saveDegree2,savePeek2;
    private ImageView saveCirPic2,saveAxialPic2;
    private TextView saveSpan3,saveCount3,saveDevol3,saveDegree3,savePeek3;
    private ImageView saveCirPic3,saveAxialPic3;
    private TextView saveSpan4,saveCount4,saveDevol4,saveDegree4,savePeek4;
    private ImageView saveCirPic4,saveAxialPic4;
    private TextView saveSpan5,saveCount5,saveDevol5,saveDegree5,savePeek5;
    private ImageView saveCirPic5,saveAxialPic5;
    private TextView saveSpan6,saveCount6,saveDevol6,saveDegree6,savePeek6;
    private ImageView saveCirPic6,saveAxialPic6;

    private RecyclerView recyclerView;
    private LinearLayout attrText;
    private LinearLayout saveContent;
    private LinearLayout save1;
    private LinearLayout save2;
    private LinearLayout save3;
    private LinearLayout save4;
    private LinearLayout save5;
    private LinearLayout save6;

    private int dataNum;//显示的点数
    private int dataAllNum;


    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    private List<Configure>  configureList = Arrays.asList( new Configure(1),new Configure(2),new Configure(3),new Configure(4),new Configure(5),new Configure(6));//我去掉了7和8

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
    //存储数据的数组
    double[] data = new double[781];
    double[] correctedData = new double[2001];
    double[] dataAdd = new double[2001];
    //最大值影响坐标
    double dataLength;
    //纵坐标刻度
    int maxTL;
    int space;
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
        ///设置ToolBar
        setContentView(R.layout.activity_graphic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("穿孔消声器");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        clearSave();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        recyclerView.setLayoutManager(new FoucsLinearLayoutManager(getContext()));
        LinearLayoutManager layoutManager = new FoucsLinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        initUI();

        //recyclerView.setFocusableInTouchMode(false);
        ConfigureAdapter adapter = new ConfigureAdapter(configureList);
        recyclerView.setAdapter(adapter);

        //ScrollView scrollView = (ScrollView) findViewById(R.id.graphic_layout);


        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                clearCanvas();
//                clearShared();
                Intent swipeIntent = new Intent(getContext(),GraphicActivity.class);
                startActivity(swipeIntent);
                finish();
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
        iv_canvas = (ImageView) findViewById(R.id.per_iv_canvas);
        //绘制坐标
        printCoordinate();

        clearShared();


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

                                clearCanvas();
                                showAllAction();
                            }
                        }
                        break;
                }
                return true;
            }
        });

    }


    private void  initUI(){
        space = 1000;
        maxTL = 35;
        minHor = 0;
        showAll = (Button) findViewById(R.id.showAll);
        showAdd = (Button) findViewById(R.id.showAdd);
        saveBt = (Button) findViewById(R.id.saveBt);
        saveContent = (LinearLayout) findViewById(R.id.saveContent);
        saveContent.setVisibility(View.GONE);
        attrText = (LinearLayout) findViewById(R.id.attr_text);

        initSave();

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveContent.getVisibility() == View.GONE){
                    saveContent.setVisibility(View.VISIBLE);
                    attrText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }else{
                    saveContent.setVisibility(View.GONE);
                    attrText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                for(int i=1; i<=6; i++) {
                    pref = getContext().getSharedPreferences("savedContent"+i, MODE_PRIVATE);
                    float savedSpan = pref.getFloat("span",0);
                    float savedCount = pref.getFloat("count",0);
                    float savedDevol = pref.getFloat("devol",0);
                    float savedDegree = pref.getFloat("degree",0);
                    int savedPeek = pref.getInt("peek",0);
                    String typeText = pref.getString("typeText","");
                    String circularText = pref.getString("circularText","");
                    String axialText = pref.getString("axialText","");
                    if(savedPeek == 0) {
                        if(i == 1) save1.setVisibility(View.GONE);
                        else if(i == 2) save2.setVisibility(View.GONE);
                        else if(i == 3) save3.setVisibility(View.GONE);
                        else if(i == 4) save4.setVisibility(View.GONE);
                        else if(i == 5) save5.setVisibility(View.GONE);
                        else if(i == 6) save6.setVisibility(View.GONE);
                        continue;
                    }else{
                        clearCanvas();
                        for (int j=0; j<=2000; j++){
                            correctedData[j] = dataAdd[j];
                        }
                        paint.setColor(0xFF4A7EBB);
                        printGraphic();
                    }
                    if(i == 1){
                        save1.setVisibility(View.VISIBLE);
                        saveSpan1.setText(String.valueOf(savedSpan));
                        saveCount1.setText(String.valueOf(savedCount));
                        saveDevol1.setText(String.valueOf(savedDevol));
                        saveDegree1.setText(String.valueOf(savedDegree));
                        savePeek1.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic1,saveAxialPic1,typeText,circularText,axialText);
                    }else if(i == 2){
                        save2.setVisibility(View.VISIBLE);
                        saveSpan2.setText(String.valueOf(savedSpan));
                        saveCount2.setText(String.valueOf(savedCount));
                        saveDevol2.setText(String.valueOf(savedDevol));
                        saveDegree2.setText(String.valueOf(savedDegree));
                        savePeek2.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic2,saveAxialPic2,typeText,circularText,axialText);
                    }else if(i == 3){
                        save3.setVisibility(View.VISIBLE);
                        saveSpan3.setText(String.valueOf(savedSpan));
                        saveCount3.setText(String.valueOf(savedCount));
                        saveDevol3.setText(String.valueOf(savedDevol));
                        saveDegree3.setText(String.valueOf(savedDegree));
                        savePeek3.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic3,saveAxialPic3,typeText,circularText,axialText);
                    }else if(i == 4){
                        save4.setVisibility(View.VISIBLE);
                        saveSpan4.setText(String.valueOf(savedSpan));
                        saveCount4.setText(String.valueOf(savedCount));
                        saveDevol4.setText(String.valueOf(savedDevol));
                        saveDegree4.setText(String.valueOf(savedDegree));
                        savePeek4.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic4,saveAxialPic4,typeText,circularText,axialText);
                    }else if(i == 5){
                        save5.setVisibility(View.VISIBLE);
                        saveSpan5.setText(String.valueOf(savedSpan));
                        saveCount5.setText(String.valueOf(savedCount));
                        saveDevol5.setText(String.valueOf(savedDevol));
                        saveDegree5.setText(String.valueOf(savedDegree));
                        savePeek5.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic5,saveAxialPic5,typeText,circularText,axialText);
                    }else if(i == 6){
                        save6.setVisibility(View.VISIBLE);
                        saveSpan6.setText(String.valueOf(savedSpan));
                        saveCount6.setText(String.valueOf(savedCount));
                        saveDevol6.setText(String.valueOf(savedDevol));
                        saveDegree6.setText(String.valueOf(savedDegree));
                        savePeek6.setText(String.valueOf(savedPeek));
                        savePic(saveCirPic6,saveAxialPic6,typeText,circularText,axialText);
                    }
                }

            }
        });

        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllAction();
            }
        });

        showAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAction();
            }
        });

    }

    private void initSave(){
        save1 = (LinearLayout) findViewById(R.id.save1);
        saveSpan1 = (TextView) findViewById(R.id.save_span1);
        saveCount1 = (TextView) findViewById(R.id.save_count1);
        saveDevol1 = (TextView) findViewById(R.id.save_devol1);
        saveDegree1 = (TextView) findViewById(R.id.save_degree1);
        savePeek1 = (TextView) findViewById(R.id.save_peek1);
        saveCirPic1 = (ImageView) findViewById(R.id.save_circular_pic1);
        saveAxialPic1 = (ImageView) findViewById(R.id.save_axial_pic1);
        save2 = (LinearLayout) findViewById(R.id.save2);
        saveSpan2 = (TextView) findViewById(R.id.save_span2);
        saveCount2 = (TextView) findViewById(R.id.save_count2);
        saveDevol2 = (TextView) findViewById(R.id.save_devol2);
        saveDegree2 = (TextView) findViewById(R.id.save_degree2);
        savePeek2 = (TextView) findViewById(R.id.save_peek2);
        saveCirPic2 = (ImageView) findViewById(R.id.save_circular_pic2);
        saveAxialPic2 = (ImageView) findViewById(R.id.save_axial_pic2);
        save3 = (LinearLayout) findViewById(R.id.save3);
        saveSpan3 = (TextView) findViewById(R.id.save_span3);
        saveCount3 = (TextView) findViewById(R.id.save_count3);
        saveDevol3 = (TextView) findViewById(R.id.save_devol3);
        saveDegree3 = (TextView) findViewById(R.id.save_degree3);
        savePeek3 = (TextView) findViewById(R.id.save_peek3);
        saveCirPic3 = (ImageView) findViewById(R.id.save_circular_pic3);
        saveAxialPic3 = (ImageView) findViewById(R.id.save_axial_pic3);
        save4 = (LinearLayout) findViewById(R.id.save4);
        saveSpan4 = (TextView) findViewById(R.id.save_span4);
        saveCount4 = (TextView) findViewById(R.id.save_count4);
        saveDevol4 = (TextView) findViewById(R.id.save_devol4);
        saveDegree4 = (TextView) findViewById(R.id.save_degree4);
        savePeek4 = (TextView) findViewById(R.id.save_peek4);
        saveCirPic4 = (ImageView) findViewById(R.id.save_circular_pic4);
        saveAxialPic4 = (ImageView) findViewById(R.id.save_axial_pic4);
        save5 = (LinearLayout) findViewById(R.id.save5);
        saveSpan5 = (TextView) findViewById(R.id.save_span5);
        saveCount5 = (TextView) findViewById(R.id.save_count5);
        saveDevol5 = (TextView) findViewById(R.id.save_devol5);
        saveDegree5 = (TextView) findViewById(R.id.save_degree5);
        savePeek5 = (TextView) findViewById(R.id.save_peek5);
        saveCirPic5 = (ImageView) findViewById(R.id.save_circular_pic5);
        saveAxialPic5 = (ImageView) findViewById(R.id.save_axial_pic5);
        save6 = (LinearLayout) findViewById(R.id.save6);
        saveSpan6 = (TextView) findViewById(R.id.save_span6);
        saveCount6 = (TextView) findViewById(R.id.save_count6);
        saveDevol6 = (TextView) findViewById(R.id.save_devol6);
        saveDegree6 = (TextView) findViewById(R.id.save_degree6);
        savePeek6 = (TextView) findViewById(R.id.save_peek6);
        saveCirPic6 = (ImageView) findViewById(R.id.save_circular_pic6);
        saveAxialPic6 = (ImageView) findViewById(R.id.save_axial_pic6);
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
        int blanking = (x2-x1)/8;
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
        if (baseBitmap == null) {
            baseBitmap = Bitmap.createBitmap(screenWidth, 700, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
        }

        paint.setStrokeWidth(4);

        int blanking = (screenWidth-270)/8;
        float gap = ((float)blanking)/space*10;
        float x, y;
        //上一次绘图坐标点 drawPoint的参数要求float
        float usedx = 0, usedy = 0;
        boolean flag = false;
        for (int i = minHor/10; i <= 2000 && i <= minHor/10+space*8/10; i++) {
            x = (float) gap * (i-minHor/10) + x1;//i的范围和减去的内容都要注意！
            //纵坐标 y2-y1=490对应70  相当于每个点7
            y = y2 - (float) correctedData[i] * (y2 - y1) / maxTL;
            if(x>x2) break;
            if(correctedData[i] <= 0.01){
                usedy = y;
                continue;
            }
            canvas.drawPoint(x, y, paint);
            //第一次不连线
            if (flag ) canvas.drawLine(usedx, usedy, x, y, paint);
            flag = true;
            usedx = x;
            usedy = y;
        }
        iv_canvas.setImageBitmap(baseBitmap);
    }

    //action按钮注册
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbar, menu);
        return true;
    }

    //返回到登录界面事件和action按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog alertDialog = new AlertDialog.Builder(GraphicActivity.this).setTitle("退出提醒").setMessage("即将退出穿孔消声器设计，\n请确定已保存数据。")
                        .setPositiveButton("退出",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface,int i){
                                Intent intent = new Intent(getContext(), ChooseOne.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface,int i){
                            }
                        }).create();
                alertDialog.show();
                break;
            case R.id.search_bar:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
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

    private int UIdistance(int num){
        if(num>=100) return 57;
        else if(num>=10) return 47;
        else return 37;
    }

    private void clearShared(){
        for(int i=1; i<=6; i++){
            editor = getContext().getSharedPreferences("choosedId"+i,MODE_PRIVATE).edit();
            editor.putInt("id",0);
            editor.putFloat("correct",0);
            editor.apply();
        }
    }

    public void showAllAction(){
            clearCanvas();

            for(int i=0; i<=2000; i++){
                dataAdd[i] = 0;
            }
            dataAllNum = 0;
            for(int i=1; i<=6; i++) {
                pref = getContext().getSharedPreferences("choosedId"+i, MODE_PRIVATE);
                int id = pref.getInt("id", 0);
                float correct = pref.getFloat("correct",0);
                if(id == 0) continue;
                Log.d("wangting corr",String.valueOf(correct));
                //SharedPreferences.Editor editor = pref.edit();
                //editor.clear();
                Log.d("myIdqqqis", String.valueOf(id));
                List<DataAll> myDataAll = LitePal.where("dataId = ?", id + "").find(DataAll.class);
                DataAll dataResult = myDataAll.get(0);
                Log.d("f200 is", String.valueOf(dataResult.getF200()));
                data[0] = dataResult.getF200();
                data[1] = dataResult.getF210();
                data[2] = dataResult.getF220();
                data[3] = dataResult.getF230();
                data[4] = dataResult.getF240();
                data[5] = dataResult.getF250();
                data[6] = dataResult.getF260();
                data[7] = dataResult.getF270();
                data[8] = dataResult.getF280();
                data[9] = dataResult.getF290();
                data[10] = dataResult.getF300();
                data[11] = dataResult.getF310();
                data[12] = dataResult.getF320();
                data[13] = dataResult.getF330();
                data[14] = dataResult.getF340();
                data[15] = dataResult.getF350();
                data[16] = dataResult.getF360();
                data[17] = dataResult.getF370();
                data[18] = dataResult.getF380();
                data[19] = dataResult.getF390();
                data[20] = dataResult.getF400();
                data[21] = dataResult.getF410();
                data[22] = dataResult.getF420();
                data[23] = dataResult.getF430();
                data[24] = dataResult.getF440();
                data[25] = dataResult.getF450();
                data[26] = dataResult.getF460();
                data[27] = dataResult.getF470();
                data[28] = dataResult.getF480();
                data[29] = dataResult.getF490();
                data[30] = dataResult.getF500();
                data[31] = dataResult.getF510();
                data[32] = dataResult.getF520();
                data[33] = dataResult.getF530();
                data[34] = dataResult.getF540();
                data[35] = dataResult.getF550();
                data[36] = dataResult.getF560();
                data[37] = dataResult.getF570();
                data[38] = dataResult.getF580();
                data[39] = dataResult.getF590();
                data[40] = dataResult.getF600();
                data[41] = dataResult.getF610();
                data[42] = dataResult.getF620();
                data[43] = dataResult.getF630();
                data[44] = dataResult.getF640();
                data[45] = dataResult.getF650();
                data[46] = dataResult.getF660();
                data[47] = dataResult.getF670();
                data[48] = dataResult.getF680();
                data[49] = dataResult.getF690();
                data[50] = dataResult.getF700();
                data[51] = dataResult.getF710();
                data[52] = dataResult.getF720();
                data[53] = dataResult.getF730();
                data[54] = dataResult.getF740();
                data[55] = dataResult.getF750();
                data[56] = dataResult.getF760();
                data[57] = dataResult.getF770();
                data[58] = dataResult.getF780();
                data[59] = dataResult.getF790();
                data[60] = dataResult.getF800();
                data[61] = dataResult.getF810();
                data[62] = dataResult.getF820();
                data[63] = dataResult.getF830();
                data[64] = dataResult.getF840();
                data[65] = dataResult.getF850();
                data[66] = dataResult.getF860();
                data[67] = dataResult.getF870();
                data[68] = dataResult.getF880();
                data[69] = dataResult.getF890();
                data[70] = dataResult.getF900();
                data[71] = dataResult.getF910();
                data[72] = dataResult.getF920();
                data[73] = dataResult.getF930();
                data[74] = dataResult.getF940();
                data[75] = dataResult.getF950();
                data[76] = dataResult.getF960();
                data[77] = dataResult.getF970();
                data[78] = dataResult.getF980();
                data[79] = dataResult.getF990();
                data[80] = dataResult.getF1000();
                data[81] = dataResult.getF1010();
                data[82] = dataResult.getF1020();
                data[83] = dataResult.getF1030();
                data[84] = dataResult.getF1040();
                data[85] = dataResult.getF1050();
                data[86] = dataResult.getF1060();
                data[87] = dataResult.getF1070();
                data[88] = dataResult.getF1080();
                data[89] = dataResult.getF1090();
                data[90] = dataResult.getF1100();
                data[91] = dataResult.getF1110();
                data[92] = dataResult.getF1120();
                data[93] = dataResult.getF1130();
                data[94] = dataResult.getF1140();
                data[95] = dataResult.getF1150();
                data[96] = dataResult.getF1160();
                data[97] = dataResult.getF1170();
                data[98] = dataResult.getF1180();
                data[99] = dataResult.getF1190();
                data[100] = dataResult.getF1200();
                data[101] = dataResult.getF1210();
                data[102] = dataResult.getF1220();
                data[103] = dataResult.getF1230();
                data[104] = dataResult.getF1240();
                data[105] = dataResult.getF1250();
                data[106] = dataResult.getF1260();
                data[107] = dataResult.getF1270();
                data[108] = dataResult.getF1280();
                data[109] = dataResult.getF1290();
                data[110] = dataResult.getF1300();
                data[111] = dataResult.getF1310();
                data[112] = dataResult.getF1320();
                data[113] = dataResult.getF1330();
                data[114] = dataResult.getF1340();
                data[115] = dataResult.getF1350();
                data[116] = dataResult.getF1360();
                data[117] = dataResult.getF1370();
                data[118] = dataResult.getF1380();
                data[119] = dataResult.getF1390();
                data[120] = dataResult.getF1400();
                data[121] = dataResult.getF1410();
                data[122] = dataResult.getF1420();
                data[123] = dataResult.getF1430();
                data[124] = dataResult.getF1440();
                data[125] = dataResult.getF1450();
                data[126] = dataResult.getF1460();
                data[127] = dataResult.getF1470();
                data[128] = dataResult.getF1480();
                data[129] = dataResult.getF1490();
                data[130] = dataResult.getF1500();
                data[131] = dataResult.getF1510();
                data[132] = dataResult.getF1520();
                data[133] = dataResult.getF1530();
                data[134] = dataResult.getF1540();
                data[135] = dataResult.getF1550();
                data[136] = dataResult.getF1560();
                data[137] = dataResult.getF1570();
                data[138] = dataResult.getF1580();
                data[139] = dataResult.getF1590();
                data[140] = dataResult.getF1600();
                data[141] = dataResult.getF1610();
                data[142] = dataResult.getF1620();
                data[143] = dataResult.getF1630();
                data[144] = dataResult.getF1640();
                data[145] = dataResult.getF1650();
                data[146] = dataResult.getF1660();
                data[147] = dataResult.getF1670();
                data[148] = dataResult.getF1680();
                data[149] = dataResult.getF1690();
                data[150] = dataResult.getF1700();
                data[151] = dataResult.getF1710();
                data[152] = dataResult.getF1720();
                data[153] = dataResult.getF1730();
                data[154] = dataResult.getF1740();
                data[155] = dataResult.getF1750();
                data[156] = dataResult.getF1760();
                data[157] = dataResult.getF1770();
                data[158] = dataResult.getF1780();
                data[159] = dataResult.getF1790();
                data[160] = dataResult.getF1800();
                data[161] = dataResult.getF1810();
                data[162] = dataResult.getF1820();
                data[163] = dataResult.getF1830();
                data[164] = dataResult.getF1840();
                data[165] = dataResult.getF1850();
                data[166] = dataResult.getF1860();
                data[167] = dataResult.getF1870();
                data[168] = dataResult.getF1880();
                data[169] = dataResult.getF1890();
                data[170] = dataResult.getF1900();
                data[171] = dataResult.getF1910();
                data[172] = dataResult.getF1920();
                data[173] = dataResult.getF1930();
                data[174] = dataResult.getF1940();
                data[175] = dataResult.getF1950();
                data[176] = dataResult.getF1960();
                data[177] = dataResult.getF1970();
                data[178] = dataResult.getF1980();
                data[179] = dataResult.getF1990();
                data[180] = dataResult.getF2000();
                data[181] = dataResult.getF2010();
                data[182] = dataResult.getF2020();
                data[183] = dataResult.getF2030();
                data[184] = dataResult.getF2040();
                data[185] = dataResult.getF2050();
                data[186] = dataResult.getF2060();
                data[187] = dataResult.getF2070();
                data[188] = dataResult.getF2080();
                data[189] = dataResult.getF2090();
                data[190] = dataResult.getF2100();
                data[191] = dataResult.getF2110();
                data[192] = dataResult.getF2120();
                data[193] = dataResult.getF2130();
                data[194] = dataResult.getF2140();
                data[195] = dataResult.getF2150();
                data[196] = dataResult.getF2160();
                data[197] = dataResult.getF2170();
                data[198] = dataResult.getF2180();
                data[199] = dataResult.getF2190();
                data[200] = dataResult.getF2200();
                data[201] = dataResult.getF2210();
                data[202] = dataResult.getF2220();
                data[203] = dataResult.getF2230();
                data[204] = dataResult.getF2240();
                data[205] = dataResult.getF2250();
                data[206] = dataResult.getF2260();
                data[207] = dataResult.getF2270();
                data[208] = dataResult.getF2280();
                data[209] = dataResult.getF2290();
                data[210] = dataResult.getF2300();
                data[211] = dataResult.getF2310();
                data[212] = dataResult.getF2320();
                data[213] = dataResult.getF2330();
                data[214] = dataResult.getF2340();
                data[215] = dataResult.getF2350();
                data[216] = dataResult.getF2360();
                data[217] = dataResult.getF2370();
                data[218] = dataResult.getF2380();
                data[219] = dataResult.getF2390();
                data[220] = dataResult.getF2400();
                data[221] = dataResult.getF2410();
                data[222] = dataResult.getF2420();
                data[223] = dataResult.getF2430();
                data[224] = dataResult.getF2440();
                data[225] = dataResult.getF2450();
                data[226] = dataResult.getF2460();
                data[227] = dataResult.getF2470();
                data[228] = dataResult.getF2480();
                data[229] = dataResult.getF2490();
                data[230] = dataResult.getF2500();
                data[231] = dataResult.getF2510();
                data[232] = dataResult.getF2520();
                data[233] = dataResult.getF2530();
                data[234] = dataResult.getF2540();
                data[235] = dataResult.getF2550();
                data[236] = dataResult.getF2560();
                data[237] = dataResult.getF2570();
                data[238] = dataResult.getF2580();
                data[239] = dataResult.getF2590();
                data[240] = dataResult.getF2600();
                data[241] = dataResult.getF2610();
                data[242] = dataResult.getF2620();
                data[243] = dataResult.getF2630();
                data[244] = dataResult.getF2640();
                data[245] = dataResult.getF2650();
                data[246] = dataResult.getF2660();
                data[247] = dataResult.getF2670();
                data[248] = dataResult.getF2680();
                data[249] = dataResult.getF2690();
                data[250] = dataResult.getF2700();
                data[251] = dataResult.getF2710();
                data[252] = dataResult.getF2720();
                data[253] = dataResult.getF2730();
                data[254] = dataResult.getF2740();
                data[255] = dataResult.getF2750();
                data[256] = dataResult.getF2760();
                data[257] = dataResult.getF2770();
                data[258] = dataResult.getF2780();
                data[259] = dataResult.getF2790();
                data[260] = dataResult.getF2800();
                data[261] = dataResult.getF2810();
                data[262] = dataResult.getF2820();
                data[263] = dataResult.getF2830();
                data[264] = dataResult.getF2840();
                data[265] = dataResult.getF2850();
                data[266] = dataResult.getF2860();
                data[267] = dataResult.getF2870();
                data[268] = dataResult.getF2880();
                data[269] = dataResult.getF2890();
                data[270] = dataResult.getF2900();
                data[271] = dataResult.getF2910();
                data[272] = dataResult.getF2920();
                data[273] = dataResult.getF2930();
                data[274] = dataResult.getF2940();
                data[275] = dataResult.getF2950();
                data[276] = dataResult.getF2960();
                data[277] = dataResult.getF2970();
                data[278] = dataResult.getF2980();
                data[279] = dataResult.getF2990();
                data[280] = dataResult.getF3000();
                data[281] = dataResult.getF3010();
                data[282] = dataResult.getF3020();
                data[283] = dataResult.getF3030();
                data[284] = dataResult.getF3040();
                data[285] = dataResult.getF3050();
                data[286] = dataResult.getF3060();
                data[287] = dataResult.getF3070();
                data[288] = dataResult.getF3080();
                data[289] = dataResult.getF3090();
                data[290] = dataResult.getF3100();
                data[291] = dataResult.getF3110();
                data[292] = dataResult.getF3120();
                data[293] = dataResult.getF3130();
                data[294] = dataResult.getF3140();
                data[295] = dataResult.getF3150();
                data[296] = dataResult.getF3160();
                data[297] = dataResult.getF3170();
                data[298] = dataResult.getF3180();
                data[299] = dataResult.getF3190();
                data[300] = dataResult.getF3200();
                data[301] = dataResult.getF3210();
                data[302] = dataResult.getF3220();
                data[303] = dataResult.getF3230();
                data[304] = dataResult.getF3240();
                data[305] = dataResult.getF3250();
                data[306] = dataResult.getF3260();
                data[307] = dataResult.getF3270();
                data[308] = dataResult.getF3280();
                data[309] = dataResult.getF3290();
                data[310] = dataResult.getF3300();
                data[311] = dataResult.getF3310();
                data[312] = dataResult.getF3320();
                data[313] = dataResult.getF3330();
                data[314] = dataResult.getF3340();
                data[315] = dataResult.getF3350();
                data[316] = dataResult.getF3360();
                data[317] = dataResult.getF3370();
                data[318] = dataResult.getF3380();
                data[319] = dataResult.getF3390();
                data[320] = dataResult.getF3400();
                data[321] = dataResult.getF3410();
                data[322] = dataResult.getF3420();
                data[323] = dataResult.getF3430();
                data[324] = dataResult.getF3440();
                data[325] = dataResult.getF3450();
                data[326] = dataResult.getF3460();
                data[327] = dataResult.getF3470();
                data[328] = dataResult.getF3480();
                data[329] = dataResult.getF3490();
                data[330] = dataResult.getF3500();
                data[331] = dataResult.getF3510();
                data[332] = dataResult.getF3520();
                data[333] = dataResult.getF3530();
                data[334] = dataResult.getF3540();
                data[335] = dataResult.getF3550();
                data[336] = dataResult.getF3560();
                data[337] = dataResult.getF3570();
                data[338] = dataResult.getF3580();
                data[339] = dataResult.getF3590();
                data[340] = dataResult.getF3600();
                data[341] = dataResult.getF3610();
                data[342] = dataResult.getF3620();
                data[343] = dataResult.getF3630();
                data[344] = dataResult.getF3640();
                data[345] = dataResult.getF3650();
                data[346] = dataResult.getF3660();
                data[347] = dataResult.getF3670();
                data[348] = dataResult.getF3680();
                data[349] = dataResult.getF3690();
                data[350] = dataResult.getF3700();
                data[351] = dataResult.getF3710();
                data[352] = dataResult.getF3720();
                data[353] = dataResult.getF3730();
                data[354] = dataResult.getF3740();
                data[355] = dataResult.getF3750();
                data[356] = dataResult.getF3760();
                data[357] = dataResult.getF3770();
                data[358] = dataResult.getF3780();
                data[359] = dataResult.getF3790();
                data[360] = dataResult.getF3800();
                data[361] = dataResult.getF3810();
                data[362] = dataResult.getF3820();
                data[363] = dataResult.getF3830();
                data[364] = dataResult.getF3840();
                data[365] = dataResult.getF3850();
                data[366] = dataResult.getF3860();
                data[367] = dataResult.getF3870();
                data[368] = dataResult.getF3880();
                data[369] = dataResult.getF3890();
                data[370] = dataResult.getF3900();
                data[371] = dataResult.getF3910();
                data[372] = dataResult.getF3920();
                data[373] = dataResult.getF3930();
                data[374] = dataResult.getF3940();
                data[375] = dataResult.getF3950();
                data[376] = dataResult.getF3960();
                data[377] = dataResult.getF3970();
                data[378] = dataResult.getF3980();
                data[379] = dataResult.getF3990();
                data[380] = dataResult.getF4000();
                data[381] = dataResult.getF4010();
                data[382] = dataResult.getF4020();
                data[383] = dataResult.getF4030();
                data[384] = dataResult.getF4040();
                data[385] = dataResult.getF4050();
                data[386] = dataResult.getF4060();
                data[387] = dataResult.getF4070();
                data[388] = dataResult.getF4080();
                data[389] = dataResult.getF4090();
                data[390] = dataResult.getF4100();
                data[391] = dataResult.getF4110();
                data[392] = dataResult.getF4120();
                data[393] = dataResult.getF4130();
                data[394] = dataResult.getF4140();
                data[395] = dataResult.getF4150();
                data[396] = dataResult.getF4160();
                data[397] = dataResult.getF4170();
                data[398] = dataResult.getF4180();
                data[399] = dataResult.getF4190();
                data[400] = dataResult.getF4200();
                data[401] = dataResult.getF4210();
                data[402] = dataResult.getF4220();
                data[403] = dataResult.getF4230();
                data[404] = dataResult.getF4240();
                data[405] = dataResult.getF4250();
                data[406] = dataResult.getF4260();
                data[407] = dataResult.getF4270();
                data[408] = dataResult.getF4280();
                data[409] = dataResult.getF4290();
                data[410] = dataResult.getF4300();
                data[411] = dataResult.getF4310();
                data[412] = dataResult.getF4320();
                data[413] = dataResult.getF4330();
                data[414] = dataResult.getF4340();
                data[415] = dataResult.getF4350();
                data[416] = dataResult.getF4360();
                data[417] = dataResult.getF4370();
                data[418] = dataResult.getF4380();
                data[419] = dataResult.getF4390();
                data[420] = dataResult.getF4400();
                data[421] = dataResult.getF4410();
                data[422] = dataResult.getF4420();
                data[423] = dataResult.getF4430();
                data[424] = dataResult.getF4440();
                data[425] = dataResult.getF4450();
                data[426] = dataResult.getF4460();
                data[427] = dataResult.getF4470();
                data[428] = dataResult.getF4480();
                data[429] = dataResult.getF4490();
                data[430] = dataResult.getF4500();
                data[431] = dataResult.getF4510();
                data[432] = dataResult.getF4520();
                data[433] = dataResult.getF4530();
                data[434] = dataResult.getF4540();
                data[435] = dataResult.getF4550();
                data[436] = dataResult.getF4560();
                data[437] = dataResult.getF4570();
                data[438] = dataResult.getF4580();
                data[439] = dataResult.getF4590();
                data[440] = dataResult.getF4600();
                data[441] = dataResult.getF4610();
                data[442] = dataResult.getF4620();
                data[443] = dataResult.getF4630();
                data[444] = dataResult.getF4640();
                data[445] = dataResult.getF4650();
                data[446] = dataResult.getF4660();
                data[447] = dataResult.getF4670();
                data[448] = dataResult.getF4680();
                data[449] = dataResult.getF4690();
                data[450] = dataResult.getF4700();
                data[451] = dataResult.getF4710();
                data[452] = dataResult.getF4720();
                data[453] = dataResult.getF4730();
                data[454] = dataResult.getF4740();
                data[455] = dataResult.getF4750();
                data[456] = dataResult.getF4760();
                data[457] = dataResult.getF4770();
                data[458] = dataResult.getF4780();
                data[459] = dataResult.getF4790();
                data[460] = dataResult.getF4800();
                data[461] = dataResult.getF4810();
                data[462] = dataResult.getF4820();
                data[463] = dataResult.getF4830();
                data[464] = dataResult.getF4840();
                data[465] = dataResult.getF4850();
                data[466] = dataResult.getF4860();
                data[467] = dataResult.getF4870();
                data[468] = dataResult.getF4880();
                data[469] = dataResult.getF4890();
                data[470] = dataResult.getF4900();
                data[471] = dataResult.getF4910();
                data[472] = dataResult.getF4920();
                data[473] = dataResult.getF4930();
                data[474] = dataResult.getF4940();
                data[475] = dataResult.getF4950();
                data[476] = dataResult.getF4960();
                data[477] = dataResult.getF4970();
                data[478] = dataResult.getF4980();
                data[479] = dataResult.getF4990();
                data[480] = dataResult.getF5000();
                data[481] = dataResult.getF5010();
                data[482] = dataResult.getF5020();
                data[483] = dataResult.getF5030();
                data[484] = dataResult.getF5040();
                data[485] = dataResult.getF5050();
                data[486] = dataResult.getF5060();
                data[487] = dataResult.getF5070();
                data[488] = dataResult.getF5080();
                data[489] = dataResult.getF5090();
                data[490] = dataResult.getF5100();
                data[491] = dataResult.getF5110();
                data[492] = dataResult.getF5120();
                data[493] = dataResult.getF5130();
                data[494] = dataResult.getF5140();
                data[495] = dataResult.getF5150();
                data[496] = dataResult.getF5160();
                data[497] = dataResult.getF5170();
                data[498] = dataResult.getF5180();
                data[499] = dataResult.getF5190();
                data[500] = dataResult.getF5200();
                data[501] = dataResult.getF5210();
                data[502] = dataResult.getF5220();
                data[503] = dataResult.getF5230();
                data[504] = dataResult.getF5240();
                data[505] = dataResult.getF5250();
                data[506] = dataResult.getF5260();
                data[507] = dataResult.getF5270();
                data[508] = dataResult.getF5280();
                data[509] = dataResult.getF5290();
                data[510] = dataResult.getF5300();
                data[511] = dataResult.getF5310();
                data[512] = dataResult.getF5320();
                data[513] = dataResult.getF5330();
                data[514] = dataResult.getF5340();
                data[515] = dataResult.getF5350();
                data[516] = dataResult.getF5360();
                data[517] = dataResult.getF5370();
                data[518] = dataResult.getF5380();
                data[519] = dataResult.getF5390();
                data[520] = dataResult.getF5400();
                data[521] = dataResult.getF5410();
                data[522] = dataResult.getF5420();
                data[523] = dataResult.getF5430();
                data[524] = dataResult.getF5440();
                data[525] = dataResult.getF5450();
                data[526] = dataResult.getF5460();
                data[527] = dataResult.getF5470();
                data[528] = dataResult.getF5480();
                data[529] = dataResult.getF5490();
                data[530] = dataResult.getF5500();
                data[531] = dataResult.getF5510();
                data[532] = dataResult.getF5520();
                data[533] = dataResult.getF5530();
                data[534] = dataResult.getF5540();
                data[535] = dataResult.getF5550();
                data[536] = dataResult.getF5560();
                data[537] = dataResult.getF5570();
                data[538] = dataResult.getF5580();
                data[539] = dataResult.getF5590();
                data[540] = dataResult.getF5600();
                data[541] = dataResult.getF5610();
                data[542] = dataResult.getF5620();
                data[543] = dataResult.getF5630();
                data[544] = dataResult.getF5640();
                data[545] = dataResult.getF5650();
                data[546] = dataResult.getF5660();
                data[547] = dataResult.getF5670();
                data[548] = dataResult.getF5680();
                data[549] = dataResult.getF5690();
                data[550] = dataResult.getF5700();
                data[551] = dataResult.getF5710();
                data[552] = dataResult.getF5720();
                data[553] = dataResult.getF5730();
                data[554] = dataResult.getF5740();
                data[555] = dataResult.getF5750();
                data[556] = dataResult.getF5760();
                data[557] = dataResult.getF5770();
                data[558] = dataResult.getF5780();
                data[559] = dataResult.getF5790();
                data[560] = dataResult.getF5800();
                data[561] = dataResult.getF5810();
                data[562] = dataResult.getF5820();
                data[563] = dataResult.getF5830();
                data[564] = dataResult.getF5840();
                data[565] = dataResult.getF5850();
                data[566] = dataResult.getF5860();
                data[567] = dataResult.getF5870();
                data[568] = dataResult.getF5880();
                data[569] = dataResult.getF5890();
                data[570] = dataResult.getF5900();
                data[571] = dataResult.getF5910();
                data[572] = dataResult.getF5920();
                data[573] = dataResult.getF5930();
                data[574] = dataResult.getF5940();
                data[575] = dataResult.getF5950();
                data[576] = dataResult.getF5960();
                data[577] = dataResult.getF5970();
                data[578] = dataResult.getF5980();
                data[579] = dataResult.getF5990();
                data[580] = dataResult.getF6000();
                data[581] = dataResult.getF6010();
                data[582] = dataResult.getF6020();
                data[583] = dataResult.getF6030();
                data[584] = dataResult.getF6040();
                data[585] = dataResult.getF6050();
                data[586] = dataResult.getF6060();
                data[587] = dataResult.getF6070();
                data[588] = dataResult.getF6080();
                data[589] = dataResult.getF6090();
                data[590] = dataResult.getF6100();
                data[591] = dataResult.getF6110();
                data[592] = dataResult.getF6120();
                data[593] = dataResult.getF6130();
                data[594] = dataResult.getF6140();
                data[595] = dataResult.getF6150();
                data[596] = dataResult.getF6160();
                data[597] = dataResult.getF6170();
                data[598] = dataResult.getF6180();
                data[599] = dataResult.getF6190();
                data[600] = dataResult.getF6200();
                data[601] = dataResult.getF6210();
                data[602] = dataResult.getF6220();
                data[603] = dataResult.getF6230();
                data[604] = dataResult.getF6240();
                data[605] = dataResult.getF6250();
                data[606] = dataResult.getF6260();
                data[607] = dataResult.getF6270();
                data[608] = dataResult.getF6280();
                data[609] = dataResult.getF6290();
                data[610] = dataResult.getF6300();
                data[611] = dataResult.getF6310();
                data[612] = dataResult.getF6320();
                data[613] = dataResult.getF6330();
                data[614] = dataResult.getF6340();
                data[615] = dataResult.getF6350();
                data[616] = dataResult.getF6360();
                data[617] = dataResult.getF6370();
                data[618] = dataResult.getF6380();
                data[619] = dataResult.getF6390();
                data[620] = dataResult.getF6400();
                data[621] = dataResult.getF6410();
                data[622] = dataResult.getF6420();
                data[623] = dataResult.getF6430();
                data[624] = dataResult.getF6440();
                data[625] = dataResult.getF6450();
                data[626] = dataResult.getF6460();
                data[627] = dataResult.getF6470();
                data[628] = dataResult.getF6480();
                data[629] = dataResult.getF6490();
                data[630] = dataResult.getF6500();
                data[631] = dataResult.getF6510();
                data[632] = dataResult.getF6520();
                data[633] = dataResult.getF6530();
                data[634] = dataResult.getF6540();
                data[635] = dataResult.getF6550();
                data[636] = dataResult.getF6560();
                data[637] = dataResult.getF6570();
                data[638] = dataResult.getF6580();
                data[639] = dataResult.getF6590();
                data[640] = dataResult.getF6600();
                data[641] = dataResult.getF6610();
                data[642] = dataResult.getF6620();
                data[643] = dataResult.getF6630();
                data[644] = dataResult.getF6640();
                data[645] = dataResult.getF6650();
                data[646] = dataResult.getF6660();
                data[647] = dataResult.getF6670();
                data[648] = dataResult.getF6680();
                data[649] = dataResult.getF6690();
                data[650] = dataResult.getF6700();
                data[651] = dataResult.getF6710();
                data[652] = dataResult.getF6720();
                data[653] = dataResult.getF6730();
                data[654] = dataResult.getF6740();
                data[655] = dataResult.getF6750();
                data[656] = dataResult.getF6760();
                data[657] = dataResult.getF6770();
                data[658] = dataResult.getF6780();
                data[659] = dataResult.getF6790();
                data[660] = dataResult.getF6800();
                data[661] = dataResult.getF6810();
                data[662] = dataResult.getF6820();
                data[663] = dataResult.getF6830();
                data[664] = dataResult.getF6840();
                data[665] = dataResult.getF6850();
                data[666] = dataResult.getF6860();
                data[667] = dataResult.getF6870();
                data[668] = dataResult.getF6880();
                data[669] = dataResult.getF6890();
                data[670] = dataResult.getF6900();
                data[671] = dataResult.getF6910();
                data[672] = dataResult.getF6920();
                data[673] = dataResult.getF6930();
                data[674] = dataResult.getF6940();
                data[675] = dataResult.getF6950();
                data[676] = dataResult.getF6960();
                data[677] = dataResult.getF6970();
                data[678] = dataResult.getF6980();
                data[679] = dataResult.getF6990();
                data[680] = dataResult.getF7000();
                data[681] = dataResult.getF7010();
                data[682] = dataResult.getF7020();
                data[683] = dataResult.getF7030();
                data[684] = dataResult.getF7040();
                data[685] = dataResult.getF7050();
                data[686] = dataResult.getF7060();
                data[687] = dataResult.getF7070();
                data[688] = dataResult.getF7080();
                data[689] = dataResult.getF7090();
                data[690] = dataResult.getF7100();
                data[691] = dataResult.getF7110();
                data[692] = dataResult.getF7120();
                data[693] = dataResult.getF7130();
                data[694] = dataResult.getF7140();
                data[695] = dataResult.getF7150();
                data[696] = dataResult.getF7160();
                data[697] = dataResult.getF7170();
                data[698] = dataResult.getF7180();
                data[699] = dataResult.getF7190();
                data[700] = dataResult.getF7200();
                data[701] = dataResult.getF7210();
                data[702] = dataResult.getF7220();
                data[703] = dataResult.getF7230();
                data[704] = dataResult.getF7240();
                data[705] = dataResult.getF7250();
                data[706] = dataResult.getF7260();
                data[707] = dataResult.getF7270();
                data[708] = dataResult.getF7280();
                data[709] = dataResult.getF7290();
                data[710] = dataResult.getF7300();
                data[711] = dataResult.getF7310();
                data[712] = dataResult.getF7320();
                data[713] = dataResult.getF7330();
                data[714] = dataResult.getF7340();
                data[715] = dataResult.getF7350();
                data[716] = dataResult.getF7360();
                data[717] = dataResult.getF7370();
                data[718] = dataResult.getF7380();
                data[719] = dataResult.getF7390();
                data[720] = dataResult.getF7400();
                data[721] = dataResult.getF7410();
                data[722] = dataResult.getF7420();
                data[723] = dataResult.getF7430();
                data[724] = dataResult.getF7440();
                data[725] = dataResult.getF7450();
                data[726] = dataResult.getF7460();
                data[727] = dataResult.getF7470();
                data[728] = dataResult.getF7480();
                data[729] = dataResult.getF7490();
                data[730] = dataResult.getF7500();
                data[731] = dataResult.getF7510();
                data[732] = dataResult.getF7520();
                data[733] = dataResult.getF7530();
                data[734] = dataResult.getF7540();
                data[735] = dataResult.getF7550();
                data[736] = dataResult.getF7560();
                data[737] = dataResult.getF7570();
                data[738] = dataResult.getF7580();
                data[739] = dataResult.getF7590();
                data[740] = dataResult.getF7600();
                data[741] = dataResult.getF7610();
                data[742] = dataResult.getF7620();
                data[743] = dataResult.getF7630();
                data[744] = dataResult.getF7640();
                data[745] = dataResult.getF7650();
                data[746] = dataResult.getF7660();
                data[747] = dataResult.getF7670();
                data[748] = dataResult.getF7680();
                data[749] = dataResult.getF7690();
                data[750] = dataResult.getF7700();
                data[751] = dataResult.getF7710();
                data[752] = dataResult.getF7720();
                data[753] = dataResult.getF7730();
                data[754] = dataResult.getF7740();
                data[755] = dataResult.getF7750();
                data[756] = dataResult.getF7760();
                data[757] = dataResult.getF7770();
                data[758] = dataResult.getF7780();
                data[759] = dataResult.getF7790();
                data[760] = dataResult.getF7800();
                data[761] = dataResult.getF7810();
                data[762] = dataResult.getF7820();
                data[763] = dataResult.getF7830();
                data[764] = dataResult.getF7840();
                data[765] = dataResult.getF7850();
                data[766] = dataResult.getF7860();
                data[767] = dataResult.getF7870();
                data[768] = dataResult.getF7880();
                data[769] = dataResult.getF7890();
                data[770] = dataResult.getF7900();
                data[771] = dataResult.getF7910();
                data[772] = dataResult.getF7920();
                data[773] = dataResult.getF7930();
                data[774] = dataResult.getF7940();
                data[775] = dataResult.getF7950();
                data[776] = dataResult.getF7960();
                data[777] = dataResult.getF7970();
                data[778] = dataResult.getF7980();
                data[779] = dataResult.getF7990();
                data[780] = dataResult.getF8000();
                correctedData = new double[2001];
//                for(int j=0; j<=780; j++) correctedData[j+20] = data[j];
                int newIndex=0;
                int maxIndex = 0;
                if(correct>1.0){
                    for(int j=800; j>=20; j--){//算法问题 需要倒着换！
                        newIndex= (int) (correct*j);
                        int beforeIndex = (int)(correct*(j+1));
                        if(newIndex > 2000 ) continue; //防止越界！
                        correctedData[newIndex] = data[j-20];
                        if(beforeIndex-newIndex>=2 && beforeIndex<=2000){//消去毛刺
                            int distance = beforeIndex - newIndex;
                            int little = (int)((correctedData[beforeIndex] - correctedData[newIndex])/distance);
                            for(int k=newIndex+1; k<beforeIndex; k++){
                                correctedData[k] = correctedData[k-1]+little;
                            }
                        }
                    }
                }else{
                    for(int j=20; j<=800; j++){
                        newIndex = (int) (correct*j);
                        correctedData[newIndex] = data[j-20];
                    }
                }

//                dataNum = (int)(correct*780);
//                if(dataNum>780) dataNum = 780;
                for(int j=0; j<=2000; j++) dataAdd[j] += correctedData[j];
//                if(dataAllNum <dataNum) dataAllNum = dataNum;
//                Log.d("mycolor is",String.valueOf(i));
                if (i == 1)paint.setColor(0xFF4A7EBB);
                else if (i == 2)paint.setColor(0xFFBE4B48);
                else if (i == 3)paint.setColor(0xFF98B954);
                else if (i == 4)paint.setColor(0xFF495A80);//
                else if (i == 5)paint.setColor(0xFFFD5B78);
                else if (i == 6)paint.setColor(0xFF376956);
                printGraphic();
            }
    }

    public void showAddAction(){
//        dataNum = dataAllNum;
        for (int i=0; i<=2000; i++){
            correctedData[i] = dataAdd[i];
        }

        paint.setColor(0xFF54546C);
        printGraphic();
    }

    private void clearSave(){
        for(int i=0; i<=6; i++){
            SharedPreferences.Editor editor = getContext().getSharedPreferences("savedContent"+i,MODE_PRIVATE).edit();
            editor.putFloat("span",0);
            editor.putFloat("count",0);
            editor.putFloat("degree",0);
            editor.putFloat("devol",0);
            editor.putInt("peek",0);
            editor.putString("typeText","");
            editor.putString("circularText","");
            editor.putString("axialText","");
            editor.apply();
        }
    }

    public void savePic(ImageView CircularPic,ImageView AxialPic,String typeText,String circularText,String axialText){
        if(typeText.equals("直管圆周式")){
            if(circularText.equals("四面开孔")) AxialPic.setImageResource(R.drawable.type1axail1);
            else if(circularText.equals("三面开孔")) AxialPic.setImageResource(R.drawable.type1axail2);
            else if(circularText.equals("对面开孔")) AxialPic.setImageResource(R.drawable.type1axail3);
            else if(circularText.equals("单面开孔")) AxialPic.setImageResource(R.drawable.type1axail4);
            if(axialText.equals("L20:双列居中")) CircularPic.setImageResource(R.drawable.type1circular1);
            else if(axialText.equals("L20:单列偏置")) CircularPic.setImageResource(R.drawable.type1circular2);
            else if(axialText.equals("L20:单列居中")) CircularPic.setImageResource(R.drawable.type1circular3);
            else if(axialText.equals("L30:三列居中")) CircularPic.setImageResource(R.drawable.type1circular4);
            else if(axialText.equals("L30:两列偏置")) CircularPic.setImageResource(R.drawable.type1circular5);
            else if(axialText.equals("L30:单列偏置")) CircularPic.setImageResource(R.drawable.type1circular6);
            else if(axialText.equals("L30:单列居中")) CircularPic.setImageResource(R.drawable.type1circular7);
            else if(axialText.equals("L40:四列居中")) CircularPic.setImageResource(R.drawable.type1circular8);
            else if(axialText.equals("L40:三列偏置")) CircularPic.setImageResource(R.drawable.type1circular9);
            else if(axialText.equals("L40:两列居中")) CircularPic.setImageResource(R.drawable.type1circular10);
            else if(axialText.equals("L40:单列居中")) CircularPic.setImageResource(R.drawable.type1circular11);
        }
        else if(typeText == "圆管侧向矩形式"){
            if(circularText == "大量开孔") AxialPic.setImageResource(R.drawable.type2axail1);
            else if(circularText == "部分开孔") AxialPic.setImageResource(R.drawable.type2axail2);
            else if(circularText == "少量开孔") AxialPic.setImageResource(R.drawable.type2axail3);
            if(axialText == "L20:双列居中") CircularPic.setImageResource(R.drawable.type2circular1);
            else if(axialText == "L20:单列偏置") CircularPic.setImageResource(R.drawable.type2circular2);
            else if(axialText == "L20:单列居中") CircularPic.setImageResource(R.drawable.type2circular3);
            else if(axialText == "L30:三列居中") CircularPic.setImageResource(R.drawable.type2circular4);
            else if(axialText == "L30:两列偏置") CircularPic.setImageResource(R.drawable.type2circular5);
            else if(axialText == "L30:单列偏置") CircularPic.setImageResource(R.drawable.type2circular6);
            else if(axialText == "L30:单列居中") CircularPic.setImageResource(R.drawable.type2circular7);
            else if(axialText == "L40:四列居中") CircularPic.setImageResource(R.drawable.type2circular8);
            else if(axialText == "L40:三列偏置") CircularPic.setImageResource(R.drawable.type2circular9);
            else if(axialText == "L40:两列居中") CircularPic.setImageResource(R.drawable.type2circular10);
            else if(axialText == "L40:单列居中") CircularPic.setImageResource(R.drawable.type2circular11);
        }
        else if(typeText == "矩形管侧向矩形式"){
            if(circularText == "大量开孔") AxialPic.setImageResource(R.drawable.type3axail1);
            else if(circularText == "部分开孔") AxialPic.setImageResource(R.drawable.type3axail2);
            else if(circularText == "少量开孔") AxialPic.setImageResource(R.drawable.type3axail3);
            if(axialText == "L20:双列居中") CircularPic.setImageResource(R.drawable.type3circular1);
            else if(axialText == "L20:单列偏置") CircularPic.setImageResource(R.drawable.type3circular2);
            else if(axialText == "L20:单列居中") CircularPic.setImageResource(R.drawable.type3circular3);
            else if(axialText == "L30:三列居中") CircularPic.setImageResource(R.drawable.type3circular4);
            else if(axialText == "L30:两列偏置") CircularPic.setImageResource(R.drawable.type3circular5);
            else if(axialText == "L30:单列偏置") CircularPic.setImageResource(R.drawable.type3circular6);
            else if(axialText == "L30:单列居中") CircularPic.setImageResource(R.drawable.type3circular7);
            else if(axialText == "L40:四列居中") CircularPic.setImageResource(R.drawable.type3circular8);
            else if(axialText == "L40:三列偏置") CircularPic.setImageResource(R.drawable.type3circular9);
            else if(axialText == "L40:两列居中") CircularPic.setImageResource(R.drawable.type3circular10);
            else if(axialText == "L40:单列居中") CircularPic.setImageResource(R.drawable.type3circular11);
        }
        else if(typeText == "弯管圆周式"){
            if(circularText == "三面开孔") AxialPic.setImageResource(R.drawable.type4axail1);
            else if(circularText == "两面开孔") AxialPic.setImageResource(R.drawable.type4axail2);
            else if(circularText == "单面开孔") AxialPic.setImageResource(R.drawable.type4axail3);
            if(axialText == "L20:双列居中") CircularPic.setImageResource(R.drawable.type4circular1);
            else if(axialText == "L20:单列偏置") CircularPic.setImageResource(R.drawable.type4circular2);
            else if(axialText == "L20:单列居中") CircularPic.setImageResource(R.drawable.type4circular3);
            else if(axialText == "L30:三列居中") CircularPic.setImageResource(R.drawable.type4circular4);
            else if(axialText == "L30:两列偏置") CircularPic.setImageResource(R.drawable.type4circular5);
            else if(axialText == "L30:单列偏置") CircularPic.setImageResource(R.drawable.type4circular6);
            else if(axialText == "L30:单列居中") CircularPic.setImageResource(R.drawable.type4circular7);
            else if(axialText == "L40:四列居中") CircularPic.setImageResource(R.drawable.type4circular8);
            else if(axialText == "L40:三列偏置") CircularPic.setImageResource(R.drawable.type4circular9);
            else if(axialText == "L40:两列居中") CircularPic.setImageResource(R.drawable.type4circular10);
            else if(axialText == "L40:单列居中") CircularPic.setImageResource(R.drawable.type4circular11);
        }
    }

    /**
     * 为了解决recycle人View上添加的headView 中的EditText等控件获取了焦点导致RecyclerView 莫名滚动
     */
    class FoucsLinearLayoutManager extends LinearLayoutManager {

        public FoucsLinearLayoutManager(Context context) {
            super(context);
        }

        public FoucsLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public FoucsLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }


        /**
         * 　　public boolean requestChildRectangleOnScreen (View child, Rect rectangle, boolean immediate)
         * <p>
         * 　　当组里的某个子视图需要被定位在屏幕的某个矩形范围时，调用此方法。重载此方法的ViewGroup可确认以下几点：
         * <p>
         * 　　* 子项目将是组里的直系子项
         * 　　* 矩形将在子项目的坐标体系中
         * 　　重载此方法的ViewGroup应该支持以下几点：
         * 　　* 若矩形已经是可见的，则没有东西会改变
         * 　　* 为使矩形区域全部可见，视图将可以被滚动显示
         * 　　参数
         * 　　child        发出请求的子视图
         * 　　rectangle    子项目坐标系内的矩形，即此子项目希望在屏幕上的定位
         * 　　immediate   设为true，则禁止动画和平滑移动滚动条
         * <p>
         * 　　返回值
         * 　　进行了滚动操作的这个组（group），是否处理此操作。
         *
         * @param parent
         * @param child
         * @param rect
         * @param immediate
         * @return
         */
        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {

//这里的child 是整个HeadView 而不是某个具体的editText
//            LogUtil.e("requestChildRectangleOnScreen()====> chlild==" + child.getId() + "parent==" + parent.getId());
            return false;
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {

//这里的child 是整个HeadView 而不是某个具体的editText
//            LogUtil.e("requestChildRectangleOnScreen( focusedChildVisible=)====> chlild==" + child.getId() + "parent==" + parent.getId());
            return false;
        }
    }

    /*获取两指之间的距离*/
    private float getDistance(MotionEvent event){
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;
    }

}

