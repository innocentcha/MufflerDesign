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
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
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
import com.weining.android.db.DataStick;
import com.weining.android.myconfiguration.Configure;
import com.weining.android.myconfiguration.ConfigureAdapter;
import com.weining.android.myconfiguration.StickAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.litepal.LitePalApplication.getContext;

public class StickActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Button showAll;
    private Button showAdd;
    private Button saveBt;

    private TextView saveArea1,saveLength1,saveDevol1,saveDegree1,savePeek1;
    private ImageView saveType1;
    private TextView saveArea2,saveLength2,saveDevol2,saveDegree2,savePeek2;
    private ImageView saveType2;
    private TextView saveArea3,saveLength3,saveDevol3,saveDegree3,savePeek3;
    private ImageView saveType3;
    private TextView saveArea4,saveLength4,saveDevol4,saveDegree4,savePeek4;
    private ImageView saveType4;
    private TextView saveArea5,saveLength5,saveDevol5,saveDegree5,savePeek5;
    private ImageView saveType5;
    private TextView saveArea6,saveLength6,saveDevol6,saveDegree6,savePeek6;
    private ImageView saveType6;

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
    private List<Configure>  configureList = Arrays.asList( new Configure(1),new Configure(2),new Configure(3),new Configure(4),new Configure(5),new Configure(6));

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
    double[] data = new double[1181];
    double[] correctedData = new double[3001];
    double[] dataAdd = new double[3001];
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
        setContentView(R.layout.activity_stick);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_stick);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("插入管消声器");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        clearSave();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_stick);
        LinearLayoutManager layoutManager = new FoucsLinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        initUI();

        //recyclerView.setFocusableInTouchMode(false);
        StickAdapter adapter = new StickAdapter(configureList);
        recyclerView.setAdapter(adapter);

        //ScrollView scrollView = (ScrollView) findViewById(R.id.graphic_layout);


        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_stick);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent swipeIntent = new Intent(getContext(),StickActivity.class);
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
        iv_canvas = (ImageView) findViewById(R.id.iv_canvas_stick);
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
        space = 1500;
        maxTL = 21;
        minHor = 0;
        showAll = (Button) findViewById(R.id.showAll_stick);
        showAdd = (Button) findViewById(R.id.showAdd_stick);
        saveBt = (Button) findViewById(R.id.saveBt_stick);
        saveContent = (LinearLayout) findViewById(R.id.saveContent_stick);
        saveContent.setVisibility(View.GONE);
        attrText = (LinearLayout) findViewById(R.id.attr_text_stick);
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
                editor.putFloat("area",0);
                editor.putFloat("devol",0);
                editor.putFloat("degree",0);
                editor.putInt("peek",0);
                editor.putFloat("length",0);
                editor.putString("typeText","");

                for(int i=1; i<=6; i++) {
                    pref = getContext().getSharedPreferences("savedStick"+i, MODE_PRIVATE);
                    float savedArea = pref.getFloat("area",0);
                    float savedDevol = pref.getFloat("devol",0);
                    float savedDegree = pref.getFloat("degree",0);
                    int savedPeek = pref.getInt("peek",0);
                    String typeText = pref.getString("typeText","");
                    String lengthText = pref.getString("lengthText","");
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
                        for (int j=0; j<=3000; j++){
                            correctedData[j] = dataAdd[j];
                        }
                        paint.setColor(0xFF4A7EBB);
                        printGraphic();
                    }
                    if(i == 1){
                        save1.setVisibility(View.VISIBLE);
                        saveArea1.setText(String.valueOf(savedArea));
                        saveLength1.setText(lengthText);
                        saveDevol1.setText(String.valueOf(savedDevol));
                        saveDegree1.setText(String.valueOf(savedDegree));
                        savePeek1.setText(String.valueOf(savedPeek));
                        savePic(saveType1,typeText);
                    }else if(i == 2){
                        save2.setVisibility(View.VISIBLE);
                        saveArea2.setText(String.valueOf(savedArea));
                        saveLength2.setText(lengthText);
                        saveDevol2.setText(String.valueOf(savedDevol));
                        saveDegree2.setText(String.valueOf(savedDegree));
                        savePeek2.setText(String.valueOf(savedPeek));
                        savePic(saveType2,typeText);
                    }else if(i == 3){
                        save3.setVisibility(View.VISIBLE);
                        saveArea3.setText(String.valueOf(savedArea));
                        saveLength3.setText(lengthText);
                        saveDevol3.setText(String.valueOf(savedDevol));
                        saveDegree3.setText(String.valueOf(savedDegree));
                        savePeek3.setText(String.valueOf(savedPeek));
                        savePic(saveType3,typeText);
                    }else if(i == 4){
                        save4.setVisibility(View.VISIBLE);
                        saveArea4.setText(String.valueOf(savedArea));
                        saveLength4.setText(lengthText);
                        saveDevol4.setText(String.valueOf(savedDevol));
                        saveDegree4.setText(String.valueOf(savedDegree));
                        savePeek4.setText(String.valueOf(savedPeek));
                        savePic(saveType4,typeText);
                    }else if(i == 5){
                        save5.setVisibility(View.VISIBLE);
                        saveArea5.setText(String.valueOf(savedArea));
                        saveLength5.setText(lengthText);
                        saveDevol5.setText(String.valueOf(savedDevol));
                        saveDegree5.setText(String.valueOf(savedDegree));
                        savePeek5.setText(String.valueOf(savedPeek));
                        savePic(saveType5,typeText);
                    }else if(i == 6){
                        save6.setVisibility(View.VISIBLE);
                        saveArea6.setText(String.valueOf(savedArea));
                        saveLength6.setText(lengthText);
                        saveDevol6.setText(String.valueOf(savedDevol));
                        saveDegree6.setText(String.valueOf(savedDegree));
                        savePeek6.setText(String.valueOf(savedPeek));
                        savePic(saveType6,typeText);
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
        save1 = (LinearLayout) findViewById(R.id.save1_stick);
        saveArea1 = (TextView) findViewById(R.id.save_area1_stick);
        saveLength1 = (TextView) findViewById(R.id.save_length1_stick);
        saveDevol1 = (TextView) findViewById(R.id.save_devol1_stick);
        saveDegree1 = (TextView) findViewById(R.id.save_degree1_stick);
        savePeek1 = (TextView) findViewById(R.id.save_peek1_stick);
        saveType1 = (ImageView) findViewById(R.id.save_type_pic1);

        save2 = (LinearLayout) findViewById(R.id.save2_stick);
        saveArea2 = (TextView) findViewById(R.id.save_area2_stick);
        saveLength2 = (TextView) findViewById(R.id.save_length2_stick);
        saveDevol2 = (TextView) findViewById(R.id.save_devol2_stick);
        saveDegree2 = (TextView) findViewById(R.id.save_degree2_stick);
        savePeek2 = (TextView) findViewById(R.id.save_peek2_stick);
        saveType2 = (ImageView) findViewById(R.id.save_type_pic2);

        save3 = (LinearLayout) findViewById(R.id.save3_stick);
        saveArea3 = (TextView) findViewById(R.id.save_area3_stick);
        saveLength3 = (TextView) findViewById(R.id.save_length3_stick);
        saveDevol3 = (TextView) findViewById(R.id.save_devol3_stick);
        saveDegree3 = (TextView) findViewById(R.id.save_degree3_stick);
        savePeek3 = (TextView) findViewById(R.id.save_peek3_stick);
        saveType3 = (ImageView) findViewById(R.id.save_type_pic3);

        save4 = (LinearLayout) findViewById(R.id.save4_stick);
        saveArea4 = (TextView) findViewById(R.id.save_area4_stick);
        saveLength4 = (TextView) findViewById(R.id.save_length4_stick);
        saveDevol4 = (TextView) findViewById(R.id.save_devol4_stick);
        saveDegree4 = (TextView) findViewById(R.id.save_degree4_stick);
        savePeek4 = (TextView) findViewById(R.id.save_peek4_stick);
        saveType4 = (ImageView) findViewById(R.id.save_type_pic4);

        save5 = (LinearLayout) findViewById(R.id.save5_stick);
        saveArea5 = (TextView) findViewById(R.id.save_area5_stick);
        saveLength5 = (TextView) findViewById(R.id.save_length5_stick);
        saveDevol5 = (TextView) findViewById(R.id.save_devol5_stick);
        saveDegree5 = (TextView) findViewById(R.id.save_degree5_stick);
        savePeek5 = (TextView) findViewById(R.id.save_peek5_stick);
        saveType5 = (ImageView) findViewById(R.id.save_type_pic5);

        save6 = (LinearLayout) findViewById(R.id.save6_stick);
        saveArea6 = (TextView) findViewById(R.id.save_area6_stick);
        saveLength6 = (TextView) findViewById(R.id.save_length6_stick);
        saveDevol6 = (TextView) findViewById(R.id.save_devol6_stick);
        saveDegree6 = (TextView) findViewById(R.id.save_degree6_stick);
        savePeek6 = (TextView) findViewById(R.id.save_peek6_stick);
        saveType6 = (ImageView) findViewById(R.id.save_type_pic6);
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
        for (int i = minHor/10; i <= 3000 && i <= minHor/10+space*8/10; i++) {
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

//    //action按钮注册
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.searchbar, menu);
//        return true;
//    }

    //返回到登录界面事件和action按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog alertDialog = new AlertDialog.Builder(StickActivity.this).setTitle("退出提醒").setMessage("即将退出插入管消声器设计，\n请确定已保存数据。")
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
//            case R.id.search_bar:
//                intent = new Intent(this, SearchActivity.class);
//                startActivity(intent);
//                break;
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

        for(int i=0; i<=3000; i++){
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
            List<DataStick> myDataStick = LitePal.where("dataId = ?", id + "").find(DataStick.class);
            DataStick dataResult = myDataStick.get(0);
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
            id= id+264;
            List<DataStick> myDataStick2 = LitePal.where("dataId = ?", id + "").find(DataStick.class);
            DataStick dataResult2 = myDataStick2.get(0);//其实可以不加2  但既然加了 就全部加上呀！
            data[781] = dataResult2.getF200();
            data[782] = dataResult2.getF210();
            data[783] = dataResult2.getF220();
            data[784] = dataResult2.getF230();
            data[785] = dataResult2.getF240();
            data[786] = dataResult2.getF250();
            data[787] = dataResult2.getF260();
            data[788] = dataResult2.getF270();
            data[789] = dataResult2.getF280();
            data[790] = dataResult2. getF290();
            data[791] = dataResult2.getF300();
            data[792] = dataResult2.getF310();
            data[793] = dataResult2.getF320();
            data[794] = dataResult2.getF330();
            data[795] = dataResult2.getF340();
            data[796] = dataResult2.getF350();
            data[797] = dataResult2.getF360();
            data[798] = dataResult2.getF370();
            data[799] = dataResult2.getF380();
            data[800] = dataResult2.getF390();
            data[801] = dataResult2.getF400();
            data[802] = dataResult2.getF410();
            data[803] = dataResult2.getF420();
            data[804] = dataResult2.getF430();
            data[805] = dataResult2.getF440();
            data[806] = dataResult2.getF450();
            data[807] = dataResult2.getF460();
            data[808] = dataResult2.getF470();
            data[809] = dataResult2.getF480();
            data[810] = dataResult2.getF490();
            data[811] = dataResult2.getF500();
            data[812] = dataResult2.getF510();
            data[813] = dataResult2.getF520();
            data[814] = dataResult2.getF530();
            data[815] = dataResult2.getF540();
            data[816] = dataResult2.getF550();
            data[817] = dataResult2.getF560();
            data[818] = dataResult2.getF570();
            data[819] = dataResult2.getF580();
            data[820] = dataResult2.getF590();
            data[821] = dataResult2.getF600();
            data[822] = dataResult2.getF610();
            data[823] = dataResult2.getF620();
            data[824] = dataResult2.getF630();
            data[825] = dataResult2.getF640();
            data[826] = dataResult2.getF650();
            data[827] = dataResult2.getF660();
            data[828] = dataResult2.getF670();
            data[829] = dataResult2.getF680();
            data[830] = dataResult2.getF690();
            data[831] = dataResult2.getF700();
            data[832] = dataResult2.getF710();
            data[833] = dataResult2.getF720();
            data[834] = dataResult2.getF730();
            data[835] = dataResult2.getF740();
            data[836] = dataResult2.getF750();
            data[837] = dataResult2.getF760();
            data[838] = dataResult2.getF770();
            data[839] = dataResult2.getF780();
            data[840] = dataResult2.getF790();
            data[841] = dataResult2.getF800();
            data[842] = dataResult2.getF810();
            data[843] = dataResult2.getF820();
            data[844] = dataResult2.getF830();
            data[845] = dataResult2.getF840();
            data[846] = dataResult2.getF850();
            data[847] = dataResult2.getF860();
            data[848] = dataResult2.getF870();
            data[849] = dataResult2.getF880();
            data[850] = dataResult2.getF890();
            data[851] = dataResult2.getF900();
            data[852] = dataResult2.getF910();
            data[853] = dataResult2.getF920();
            data[854] = dataResult2.getF930();
            data[855] = dataResult2.getF940();
            data[856] = dataResult2.getF950();
            data[857] = dataResult2.getF960();
            data[858] = dataResult2.getF970();
            data[859] = dataResult2.getF980();
            data[860] = dataResult2.getF990();
            data[861] = dataResult2.getF1000();
            data[862] = dataResult2.getF1010();
            data[863] = dataResult2.getF1020();
            data[864] = dataResult2.getF1030();
            data[865] = dataResult2.getF1040();
            data[866] = dataResult2.getF1050();
            data[867] = dataResult2.getF1060();
            data[868] = dataResult2.getF1070();
            data[869] = dataResult2.getF1080();
            data[870] = dataResult2.getF1090();
            data[871] = dataResult2.getF1100();
            data[872] = dataResult2.getF1110();
            data[873] = dataResult2.getF1120();
            data[874] = dataResult2.getF1130();
            data[875] = dataResult2.getF1140();
            data[876] = dataResult2.getF1150();
            data[877] = dataResult2.getF1160();
            data[878] = dataResult2.getF1170();
            data[879] = dataResult2.getF1180();
            data[880] = dataResult2.getF1190();
            data[881] = dataResult2.getF1200();
            data[882] = dataResult2.getF1210();
            data[883] = dataResult2.getF1220();
            data[884] = dataResult2.getF1230();
            data[885] = dataResult2.getF1240();
            data[886] = dataResult2.getF1250();
            data[887] = dataResult2.getF1260();
            data[888] = dataResult2.getF1270();
            data[889] = dataResult2.getF1280();
            data[890] = dataResult2.getF1290();
            data[891] = dataResult2.getF1300();
            data[892] = dataResult2.getF1310();
            data[893] = dataResult2.getF1320();
            data[894] = dataResult2.getF1330();
            data[895] = dataResult2.getF1340();
            data[896] = dataResult2.getF1350();
            data[897] = dataResult2.getF1360();
            data[898] = dataResult2.getF1370();
            data[899] = dataResult2.getF1380();
            data[900] = dataResult2.getF1390();
            data[901] = dataResult2.getF1400();
            data[902] = dataResult2.getF1410();
            data[903] = dataResult2.getF1420();
            data[904] = dataResult2.getF1430();
            data[905] = dataResult2.getF1440();
            data[906] = dataResult2.getF1450();
            data[907] = dataResult2.getF1460();
            data[908] = dataResult2.getF1470();
            data[909] = dataResult2.getF1480();
            data[910] = dataResult2.getF1490();
            data[911] = dataResult2.getF1500();
            data[912] = dataResult2.getF1510();
            data[913] = dataResult2.getF1520();
            data[914] = dataResult2.getF1530();
            data[915] = dataResult2.getF1540();
            data[916] = dataResult2.getF1550();
            data[917] = dataResult2.getF1560();
            data[918] = dataResult2.getF1570();
            data[919] = dataResult2.getF1580();
            data[920] = dataResult2.getF1590();
            data[921] = dataResult2.getF1600();
            data[922] = dataResult2.getF1610();
            data[923] = dataResult2.getF1620();
            data[924] = dataResult2.getF1630();
            data[925] = dataResult2.getF1640();
            data[926] = dataResult2.getF1650();
            data[927] = dataResult2.getF1660();
            data[928] = dataResult2.getF1670();
            data[929] = dataResult2.getF1680();
            data[930] = dataResult2.getF1690();
            data[931] = dataResult2.getF1700();
            data[932] = dataResult2.getF1710();
            data[933] = dataResult2.getF1720();
            data[934] = dataResult2.getF1730();
            data[935] = dataResult2.getF1740();
            data[936] = dataResult2.getF1750();
            data[937] = dataResult2.getF1760();
            data[938] = dataResult2.getF1770();
            data[939] = dataResult2.getF1780();
            data[940] = dataResult2.getF1790();
            data[941] = dataResult2.getF1800();
            data[942] = dataResult2.getF1810();
            data[943] = dataResult2.getF1820();
            data[944] = dataResult2.getF1830();
            data[945] = dataResult2.getF1840();
            data[946] = dataResult2.getF1850();
            data[947] = dataResult2.getF1860();
            data[948] = dataResult2.getF1870();
            data[949] = dataResult2.getF1880();
            data[950] = dataResult2.getF1890();
            data[951] = dataResult2.getF1900();
            data[952] = dataResult2.getF1910();
            data[953] = dataResult2.getF1920();
            data[954] = dataResult2.getF1930();
            data[955] = dataResult2.getF1940();
            data[956] = dataResult2.getF1950();
            data[957] = dataResult2.getF1960();
            data[958] = dataResult2.getF1970();
            data[959] = dataResult2.getF1980();
            data[960] = dataResult2.getF1990();
            data[961] = dataResult2.getF2000();
            data[962] = dataResult2.getF2010();
            data[963] = dataResult2.getF2020();
            data[964] = dataResult2.getF2030();
            data[965] = dataResult2.getF2040();
            data[966] = dataResult2.getF2050();
            data[967] = dataResult2.getF2060();
            data[968] = dataResult2.getF2070();
            data[969] = dataResult2.getF2080();
            data[970] = dataResult2.getF2090();
            data[971] = dataResult2.getF2100();
            data[972] = dataResult2.getF2110();
            data[973] = dataResult2.getF2120();
            data[974] = dataResult2.getF2130();
            data[975] = dataResult2.getF2140();
            data[976] = dataResult2.getF2150();
            data[977] = dataResult2.getF2160();
            data[978] = dataResult2.getF2170();
            data[979] = dataResult2.getF2180();
            data[980] = dataResult2.getF2190();
            data[981] = dataResult2.getF2200();
            data[982] = dataResult2.getF2210();
            data[983] = dataResult2.getF2220();
            data[984] = dataResult2.getF2230();
            data[985] = dataResult2.getF2240();
            data[986] = dataResult2.getF2250();
            data[987] = dataResult2.getF2260();
            data[988] = dataResult2.getF2270();
            data[989] = dataResult2.getF2280();
            data[990] = dataResult2.getF2290();
            data[991] = dataResult2.getF2300();
            data[992] = dataResult2.getF2310();
            data[993] = dataResult2.getF2320();
            data[994] = dataResult2.getF2330();
            data[995] = dataResult2.getF2340();
            data[996] = dataResult2.getF2350();
            data[997] = dataResult2.getF2360();
            data[998] = dataResult2.getF2370();
            data[999] = dataResult2.getF2380();
            data[1000] = dataResult2.getF2390();
            data[1001] = dataResult2.getF2400();
            data[1002] = dataResult2.getF2410();
            data[1003] = dataResult2.getF2420();
            data[1004] = dataResult2.getF2430();
            data[1005] = dataResult2.getF2440();
            data[1006] = dataResult2.getF2450();
            data[1007] = dataResult2.getF2460();
            data[1008] = dataResult2.getF2470();
            data[1009] = dataResult2.getF2480();
            data[1010] = dataResult2.getF2490();
            data[1011] = dataResult2.getF2500();
            data[1012] = dataResult2.getF2510();
            data[1013] = dataResult2.getF2520();
            data[1014] = dataResult2.getF2530();
            data[1015] = dataResult2.getF2540();
            data[1016] = dataResult2.getF2550();
            data[1017] = dataResult2.getF2560();
            data[1018] = dataResult2.getF2570();
            data[1019] = dataResult2.getF2580();
            data[1020] = dataResult2.getF2590();
            data[1021] = dataResult2.getF2600();
            data[1022] = dataResult2.getF2610();
            data[1023] = dataResult2.getF2620();
            data[1024] = dataResult2.getF2630();
            data[1025] = dataResult2.getF2640();
            data[1026] = dataResult2.getF2650();
            data[1027] = dataResult2.getF2660();
            data[1028] = dataResult2.getF2670();
            data[1029] = dataResult2.getF2680();
            data[1030] = dataResult2.getF2690();
            data[1031] = dataResult2.getF2700();
            data[1032] = dataResult2.getF2710();
            data[1033] = dataResult2.getF2720();
            data[1034] = dataResult2.getF2730();
            data[1035] = dataResult2.getF2740();
            data[1036] = dataResult2.getF2750();
            data[1037] = dataResult2.getF2760();
            data[1038] = dataResult2.getF2770();
            data[1039] = dataResult2.getF2780();
            data[1040] = dataResult2.getF2790();
            data[1041] = dataResult2.getF2800();
            data[1042] = dataResult2.getF2810();
            data[1043] = dataResult2.getF2820();
            data[1044] = dataResult2.getF2830();
            data[1045] = dataResult2.getF2840();
            data[1046] = dataResult2.getF2850();
            data[1047] = dataResult2.getF2860();
            data[1048] = dataResult2.getF2870();
            data[1049] = dataResult2.getF2880();
            data[1050] = dataResult2.getF2890();
            data[1051] = dataResult2.getF2900();
            data[1052] = dataResult2.getF2910();
            data[1053] = dataResult2.getF2920();
            data[1054] = dataResult2.getF2930();
            data[1055] = dataResult2.getF2940();
            data[1056] = dataResult2.getF2950();
            data[1057] = dataResult2.getF2960();
            data[1058] = dataResult2.getF2970();
            data[1059] = dataResult2.getF2980();
            data[1060] = dataResult2.getF2990();
            data[1061] = dataResult2.getF3000();
            data[1062] = dataResult2.getF3010();
            data[1063] = dataResult2.getF3020();
            data[1064] = dataResult2.getF3030();
            data[1065] = dataResult2.getF3040();
            data[1066] = dataResult2.getF3050();
            data[1067] = dataResult2.getF3060();
            data[1068] = dataResult2.getF3070();
            data[1069] = dataResult2.getF3080();
            data[1070] = dataResult2.getF3090();
            data[1071] = dataResult2.getF3100();
            data[1072] = dataResult2.getF3110();
            data[1073] = dataResult2.getF3120();
            data[1074] = dataResult2.getF3130();
            data[1075] = dataResult2.getF3140();
            data[1076] = dataResult2.getF3150();
            data[1077] = dataResult2.getF3160();
            data[1078] = dataResult2.getF3170();
            data[1079] = dataResult2.getF3180();
            data[1080] = dataResult2.getF3190();
            data[1081] = dataResult2.getF3200();
            data[1082] = dataResult2.getF3210();
            data[1083] = dataResult2.getF3220();
            data[1084] = dataResult2.getF3230();
            data[1085] = dataResult2.getF3240();
            data[1086] = dataResult2.getF3250();
            data[1087] = dataResult2.getF3260();
            data[1088] = dataResult2.getF3270();
            data[1089] = dataResult2.getF3280();
            data[1090] = dataResult2.getF3290();
            data[1091] = dataResult2.getF3300();
            data[1092] = dataResult2.getF3310();
            data[1093] = dataResult2.getF3320();
            data[1094] = dataResult2.getF3330();
            data[1095] = dataResult2.getF3340();
            data[1096] = dataResult2.getF3350();
            data[1097] = dataResult2.getF3360();
            data[1098] = dataResult2.getF3370();
            data[1099] = dataResult2.getF3380();
            data[1100] = dataResult2.getF3390();
            data[1101] = dataResult2.getF3400();
            data[1102] = dataResult2.getF3410();
            data[1103] = dataResult2.getF3420();
            data[1104] = dataResult2.getF3430();
            data[1105] = dataResult2.getF3440();
            data[1106] = dataResult2.getF3450();
            data[1107] = dataResult2.getF3460();
            data[1108] = dataResult2.getF3470();
            data[1109] = dataResult2.getF3480();
            data[1110] = dataResult2.getF3490();
            data[1111] = dataResult2.getF3500();
            data[1112] = dataResult2.getF3510();
            data[1113] = dataResult2.getF3520();
            data[1114] = dataResult2.getF3530();
            data[1115] = dataResult2.getF3540();
            data[1116] = dataResult2.getF3550();
            data[1117] = dataResult2.getF3560();
            data[1118] = dataResult2.getF3570();
            data[1119] = dataResult2.getF3580();
            data[1120] = dataResult2.getF3590();
            data[1121] = dataResult2.getF3600();
            data[1122] = dataResult2.getF3610();
            data[1123]  = dataResult2.getF3620();
            data[1124] = dataResult2.getF3630();
            data[1125] = dataResult2.getF3640();
            data[1126] = dataResult2.getF3650();
            data[1127] = dataResult2.getF3660();
            data[1128] = dataResult2.getF3670();
            data[1129] = dataResult2.getF3680();
            data[1130] = dataResult2.getF3690();
            data[1131] = dataResult2.getF3700();
            data[1132] = dataResult2.getF3710();
            data[1133] = dataResult2.getF3720();
            data[1134] = dataResult2.getF3730();
            data[1135] = dataResult2.getF3740();
            data[1136] = dataResult2.getF3750();
            data[1137] = dataResult2.getF3760();
            data[1138] = dataResult2.getF3770();
            data[1139] = dataResult2.getF3780();
            data[1140] = dataResult2.getF3790();
            data[1141] = dataResult2.getF3800();
            data[1142] = dataResult2.getF3810();
            data[1143] = dataResult2.getF3820();
            data[1144] = dataResult2.getF3830();
            data[1145] = dataResult2.getF3840();
            data[1146] = dataResult2.getF3850();
            data[1147] = dataResult2.getF3860();
            data[1148] = dataResult2.getF3870();
            data[1149] = dataResult2.getF3880();
            data[1150] = dataResult2.getF3890();
            data[1151] = dataResult2.getF3900();
            data[1152] = dataResult2.getF3910();
            data[1153] = dataResult2.getF3920();
            data[1154] = dataResult2.getF3930();
            data[1155] = dataResult2.getF3940();
            data[1156] = dataResult2.getF3950();
            data[1157] = dataResult2.getF3960();
            data[1158] = dataResult2.getF3970();
            data[1159] = dataResult2.getF3980();
            data[1160] = dataResult2.getF3990();
            data[1161] = dataResult2.getF4000();
            data[1162] = dataResult2.getF4010();
            data[1163] = dataResult2.getF4020();
            data[1164] = dataResult2.getF4030();
            data[1165] = dataResult2.getF4040();
            data[1166] = dataResult2.getF4050();
            data[1167] = dataResult2.getF4060();
            data[1168] = dataResult2.getF4070();
            data[1169] = dataResult2.getF4080();
            data[1170] = dataResult2.getF4090();
            data[1171] = dataResult2.getF4100();
            data[1172] = dataResult2.getF4110();
            data[1173] = dataResult2.getF4120();
            data[1174] = dataResult2.getF4130();
            data[1175] = dataResult2.getF4140();
            data[1176] = dataResult2.getF4150();
            data[1177] = dataResult2.getF4160();
            data[1178] = dataResult2.getF4170();
            data[1179] = dataResult2.getF4180();
            data[1180] = dataResult2.getF4190();
            correctedData = new double[3001];
            for(int j=0; j<=1180; j++) correctedData[i+20] = data[i];
            int newIndex=0;
            int maxIndex = 0;
            if(correct>1.0){
                for(int j=1200; j>=20; j--){//算法问题 需要倒着换！
                    newIndex= (int) (correct*j);
                    int beforeIndex = (int)(correct*(j+1));
                    if(newIndex > 3000 ) continue; //防止越界！
                    correctedData[newIndex] = data[j-20];
                    if(beforeIndex-newIndex>=2 && beforeIndex<=3000){//消去毛刺
                        int distance = beforeIndex - newIndex;
                        int little = (int)((correctedData[beforeIndex] - correctedData[newIndex])/distance);
                        for(int k=newIndex+1; k<beforeIndex; k++){
                            correctedData[k] = correctedData[k-1]+little;
                        }
                    }
                }
            }else{
                for(int j=20; j<=1200; j++){
                    newIndex = (int) (correct*j);
                    correctedData[newIndex] = data[j-20];
                }
            }

//                dataNum = (int)(correct*780);
//                if(dataNum>780) dataNum = 780;
            for(int j=0; j<=3000; j++) dataAdd[j] += correctedData[j];
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
        for (int i=0; i<=3000; i++){
            correctedData[i] = dataAdd[i];
        }

        paint.setColor(0xFF54546C);
        printGraphic();
    }


//    开槽面积
//    消声容积
//    温度
//    峰值频率
//    单边插入长度
//    插入方式
    private void clearSave(){
        for(int i=0; i<=6; i++){
            SharedPreferences.Editor editor = getContext().getSharedPreferences("savedStick"+i,MODE_PRIVATE).edit();
            editor.putFloat("area",0);
            editor.putFloat("devol",0);
            editor.putFloat("degree",0);
            editor.putInt("peek",0);
            editor.putString("length","");
            editor.putString("typeText","");
            editor.apply();
        }
    }

    public void savePic(ImageView typePic, String typeText){
        if(typeText.equals("单边插入")) typePic.setImageResource(R.drawable.type1);
        else typePic.setImageResource(R.drawable.type2);
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

