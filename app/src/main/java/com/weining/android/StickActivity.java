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
import android.util.Log;
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
import com.weining.android.db.DataStick;
import com.weining.android.myconfiguration.Configure;
import com.weining.android.myconfiguration.StickAdapter;

import com.weining.android.util.CountUtil;
import org.litepal.LitePal;

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
    double[] data = new double[CountUtil.tlStickNum];
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

        StickAdapter adapter = new StickAdapter(configureList);
        recyclerView.setAdapter(adapter);

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
        //第一次绘图初始化内存图片
        if (baseBitmap == null) {
            baseBitmap = Bitmap.createBitmap(screenWidth, 700, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
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
        for(int i=1; i<=6; i++) {
            pref = getContext().getSharedPreferences("choosedId"+i, MODE_PRIVATE);
            int id = pref.getInt("id", 0);
            float correct = pref.getFloat("correct",0);
            if(id == 0) continue;
            List<DataStick> myDataStick = LitePal.where("dataId = ?", id + "").find(DataStick.class);
            DataStick dataResult = myDataStick.get(0);
            List<Float> tlList = dataResult.getTlList();
            for (int j = 0; j < CountUtil.tlNum; j++) {
                data[j] = tlList.get(j);
            }

            id = id + CountUtil.StickIdStep;
            List<DataStick> myDataStickExtra = LitePal.where("dataId = ?", id + "").find(DataStick.class);
            DataStick dataResultExtra = myDataStickExtra.get(0);
            for (int j = CountUtil.tlNum; j < CountUtil.tlStickNum; j++) {
                data[j] = tlList.get(j - CountUtil.tlNum);
            }

            correctedData = new double[3001];
            for(int j=0; j<=1180; j++) correctedData[i+20] = data[i];
            int newIndex=0;
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

            for(int j=0; j<=3000; j++) dataAdd[j] += correctedData[j];
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
        for (int i=0; i<=3000; i++){
            correctedData[i] = dataAdd[i];
        }

        paint.setColor(0xFF54546C);
        printGraphic();
    }

    /*
     * area: 开槽面积
     * devol: 消声容积
     * degree: 温度
     * peek: 峰值频率
     * length: 单边插入长度
     * typeText: 插入方式
     */
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
            return false;
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
            return false;
        }
    }

    //获取两指之间的距离
    private float getDistance(MotionEvent event){
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;
    }
}

