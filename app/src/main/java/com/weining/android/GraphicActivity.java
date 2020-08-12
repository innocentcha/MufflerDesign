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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.weining.android.db.DataAll;
import com.weining.android.myconfiguration.Configure;
import com.weining.android.myconfiguration.ConfigureAdapter;

import com.weining.android.util.CountUtil;
import org.litepal.LitePal;

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
    double[] data = new double[CountUtil.tlNum];
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
        LinearLayoutManager layoutManager = new FoucsLinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        initUI();

        ConfigureAdapter adapter = new ConfigureAdapter(configureList);
        recyclerView.setAdapter(adapter);

        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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


        //手势动作调整图像坐标轴
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
                List<DataAll> myDataAll = LitePal.where("dataId = ?", id + "").find(DataAll.class);
                DataAll dataResult = myDataAll.get(0);
                List<Float> tlList = dataResult.getTlList();
                for (int j = 0; j < CountUtil.tlNum; j++){
                    data[j] = tlList.get(j);
                }
                correctedData = new double[2001];
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

                for(int j=0; j<=2000; j++) dataAdd[j] += correctedData[j];
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
            //LogUtil.e("requestChildRectangleOnScreen()====> chlild==" + child.getId() + "parent==" + parent.getId());
            return false;
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {

            //这里的child 是整个HeadView 而不是某个具体的editText
            //LogUtil.e("requestChildRectangleOnScreen( focusedChildVisible=)====> chlild==" + child.getId() + "parent==" + parent.getId());
            return false;
        }
    }

    /*获取两指之间的距离
     * TODO 自定义view，把滑动事件view内部的东西
     */
    private float getDistance(MotionEvent event){
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;
    }

}

