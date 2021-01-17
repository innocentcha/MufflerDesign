package com.weining.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.weining.android.db.DataAll;
import com.weining.android.myconfiguration.Configure;
import com.weining.android.myconfiguration.SearchConfigureAdapter;

import com.weining.android.util.CountUtil;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static org.litepal.LitePalApplication.getContext;

public class SearchActivity extends AppCompatActivity {

    private Button changeTL;

    private RecyclerView recyclerView;
    private LinearLayout searchView;

    private TextView searchVol,searchSpan,searchCount;
    private EditText searchPeek;
    private Spinner searchType,searchSize,searchCircular,searchAxial,searchCavity;
    private ImageView searchAxialPic,searchCircularPic;
    private Button searchBt,searchBack;
    private TextView titleVol,titleSpan,titleCount,titleCircularPic,titleAxialPic;

    private String typeText,cavityText,axialText,circularText,sizeText;
    private String volText,spanText,decvolText;
    private int typeChoose,sizeChoose,circularChoose,axialChoose,cavityChoose;
    private int myPosition;

    int aimPeek,aimMin,aimMax;

    private EditText myTL;

    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    private List<Configure>  configureList = new ArrayList<>();
    //屏幕的长宽
    private int screenWidth;
    //private int screenHeight;

    //画笔变量
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    //坐标图位置
    int x1, x2, y1, y2;
    //存储数据的数组
    double[] data = new double[CountUtil.tlNum];
    //最大值影响坐标
    double max;
    //纵坐标刻度
    int maxTL;
    int space;
    int blanking;

    SearchConfigureAdapter adapter;
    List<DataAll>  myDataAll;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///设置ToolBar
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("搜索消声器");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        initUI();
        recyclerView = (RecyclerView)findViewById(R.id.search_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchConfigureAdapter(configureList);
        recyclerView.setAdapter(adapter);
        searchView = (LinearLayout) findViewById(R.id.search_view);
        searchView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);//一开始先进行隐藏
        searchVol.setVisibility(View.GONE);
        searchSpan.setVisibility(View.GONE);
        searchCount.setVisibility(View.GONE);
        searchCircularPic.setVisibility(View.GONE);
        searchAxialPic.setVisibility(View.GONE);
        titleVol.setVisibility(View.GONE);
        titleSpan.setVisibility(View.GONE);
        titleCount.setVisibility(View.GONE);
        titleCircularPic.setVisibility(View.GONE);
        titleAxialPic.setVisibility(View.GONE);

        //设置刷新动作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_search);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent swipeIntent = new Intent(getContext(),SearchActivity.class);
                startActivity(swipeIntent);
                finish();
            }
        });


        //获取屏幕的长宽
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();

        //初始化默认画笔，线宽3，颜色黑色
        paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        //初始化画板
        iv_canvas = (ImageView) findViewById(R.id.search_iv_canvas);

        //绘制坐标
        printCoordinate();

    }



    private void  initUI(){
        space = 1000;
        maxTL = 35;
        changeTL = (Button) findViewById(R.id.SearchChangeTL);
        myTL = (EditText) findViewById(R.id.SearchMyTL);
        searchBack = (Button) findViewById(R.id.SearchBack);

        searchVol = (TextView) findViewById(R.id.search_vol);
        searchSpan = (TextView) findViewById(R.id.search_span);
        searchCount = (TextView) findViewById(R.id.search_count);
        searchPeek = (EditText) findViewById(R.id.search_peek);
        searchType = (Spinner) findViewById(R.id.search_type);
        searchSize = (Spinner) findViewById(R.id.search_size);
        searchCircular = (Spinner) findViewById(R.id.search_circular);
        searchAxial = (Spinner) findViewById(R.id.search_axial);
        searchCavity = (Spinner) findViewById(R.id.search_cavity);
        searchAxialPic = (ImageView) findViewById(R.id.search_axial_pic);
        searchCircularPic = (ImageView) findViewById(R.id.search_circular_pic);
        searchBt = (Button) findViewById(R.id.search_bt);

        titleVol = (TextView) findViewById(R.id.title_vol);
        titleSpan = (TextView) findViewById(R.id.title_span);
        titleCount = (TextView) findViewById(R.id.title_count);
        titleCircularPic = (TextView) findViewById(R.id.title_circular_pic);
        titleAxialPic = (TextView) findViewById(R.id.title_axial_pic);

        final String[] type = new String[]{"直管圆周式", "圆管侧向矩形式", "矩形管侧向矩形式", "弯管圆周式" };
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, type);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchType.setAdapter(adapterType);
        searchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Type 动作
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeText = searchType.getItemAtPosition(i).toString();
                typeChoose = (int)searchType.getItemIdAtPosition(i);

                String[] size,axial;

                if(typeText != "矩形管侧向矩形式"){
                    size = new String[]{"D35", "D50", "D65" };
                }else{
                    size = new String[]{"W80", "W100", "W120" };
                }
                ArrayAdapter<String> adapterSize = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, size);
                adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                searchSize.setAdapter(adapterSize);
                searchSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    //Size 动作
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        sizeText = searchSize.getItemAtPosition(i).toString();
                        sizeChoose = (int) searchSize.getItemIdAtPosition(i);

                        String[] cavity;
                        if(sizeText == "D35" && typeText == "直管圆周式"){
                            cavity = new String[]{"","D50", "D65", "D80" , "D95" };
                        }else if(sizeText == "D50" && typeText == "直管圆周式"){
                            cavity = new String[]{"","D70", "D90", "D110" , "D130" };
                        }else if(sizeText == "D60" && typeText == "直管圆周式"){
                            cavity = new String[]{"","D85", "D105", "D125" , "D145" };
                        }else if(sizeText == "D35" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{"","H10", "H20", "H40" , "H60" , "H80"};
                        }else if(sizeText == "D50" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{ "","H20", "H40" , "H60" , "H80","H100"};
                        }else if(sizeText == "D65" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{"","H20", "H40" , "H60" , "H80","H100" };
                        }else if(typeText == "矩形管侧向矩形式"){
                            cavity = new String[]{"","H20", "H40" , "H60" , "H80","H100" };
                        }else if(sizeText == "D35" && typeText == "弯管圆周式"){
                            cavity = new String[]{"","D55", "D75", "D85" , "D95" };
                        }else if(sizeText == "D50" && typeText == "弯管圆周式"){
                            cavity = new String[]{"","D70", "D90", "D110" , "D130" };
                        }else if(sizeText == "D65" && typeText == "弯管圆周式"){
                            cavity = new String[]{"","D85", "D105", "D125" , "D140" };
                        }else{
                            cavity = new String[]{"","D50", "D65", "D80" , "D95" };
                        }

                        ArrayAdapter<String> adapterCavity = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, cavity);
                        adapterCavity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        searchCavity.setAdapter(adapterCavity);
                        searchCavity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            //Cavity 动作
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                cavityText = searchCavity.getItemAtPosition(i).toString();
                                cavityChoose = (int) searchCavity.getItemIdAtPosition(i);
                            }

                            @Override

                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }

                        });
                    }

                    @Override

                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }

                });


                String[] circular;
                if(typeText == "直管圆周式" ){
                    circular = new String[]{"","四面开孔", "三面开孔", "对面开孔", "单面开孔" };
                }else if(typeText == "弯管圆周式"){
                    circular = new String[]{"","三面开孔", "两面开孔", "单面开孔"};
                }else{
                    circular = new String[]{"","大量开孔", "部分开孔", "少量开孔"};
                }
                ArrayAdapter<String> adapterCircular = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, circular);
                adapterCircular.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                searchCircular.setAdapter(adapterCircular);
                searchCircular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    //Circular 动作
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        circularText = searchCircular.getItemAtPosition(i).toString();
                        circularChoose = (int) searchCircular.getItemIdAtPosition(i);
                    }

                    @Override

                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }

                });

            }

            @Override

            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });


        String[] axial = new String[]{"","L20:双列居中", "L20:单列偏置", "L20:单列居中", "L30:三列居中","L30:两列偏置", "L30:单列偏置", "L30:单列居中", "L40:四列居中", "L40:三列偏置" , "L40:两列居中", "L40:单列居中"};
        ArrayAdapter<String> adapterAxial = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, axial);
        adapterAxial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchAxial.setAdapter(adapterAxial);
        searchAxial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Axial 动作
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                axialText = searchAxial.getItemAtPosition(i).toString();
                axialChoose = (int) searchAxial.getItemIdAtPosition(i);
            }

            @Override

            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        //图片的名字反了，懒得改了..
        //应该自己做个映射对应
        searchBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clearCanvas();

                if (typeText == "直管圆周式") {
                    if (circularText == "四面开孔")
                        searchAxialPic.setImageResource(R.drawable.type1axail1);
                    else if (circularText == "三面开孔")
                        searchAxialPic.setImageResource(R.drawable.type1axail2);
                    else if (circularText == "对面开孔")
                        searchAxialPic.setImageResource(R.drawable.type1axail3);
                    else if (circularText == "单面开孔")
                        searchAxialPic.setImageResource(R.drawable.type1axail4);
                    if (axialText == "L20:双列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular1);
                    else if (axialText == "L20:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type1circular2);
                    else if (axialText == "L20:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular3);
                    else if (axialText == "L30:三列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular4);
                    else if (axialText == "L30:两列偏置")
                        searchCircularPic.setImageResource(R.drawable.type1circular5);
                    else if (axialText == "L30:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type1circular6);
                    else if (axialText == "L30:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular7);
                    else if (axialText == "L40:四列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular8);
                    else if (axialText == "L40:三列偏置")
                        searchCircularPic.setImageResource(R.drawable.type1circular9);
                    else if (axialText == "L40:两列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular10);
                    else if (axialText == "L40:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type1circular11);
                } else if (typeText == "圆管侧向矩形式") {
                    if (circularText == "大量开孔")
                        searchAxialPic.setImageResource(R.drawable.type2axail1);
                    else if (circularText == "部分开孔")
                        searchAxialPic.setImageResource(R.drawable.type2axail2);
                    else if (circularText == "少量开孔")
                        searchAxialPic.setImageResource(R.drawable.type2axail3);
                    if (axialText == "L20:双列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular1);
                    else if (axialText == "L20:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type2circular2);
                    else if (axialText == "L20:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular3);
                    else if (axialText == "L30:三列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular4);
                    else if (axialText == "L30:两列偏置")
                        searchCircularPic.setImageResource(R.drawable.type2circular5);
                    else if (axialText == "L30:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type2circular6);
                    else if (axialText == "L30:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular7);
                    else if (axialText == "L40:四列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular8);
                    else if (axialText == "L40:三列偏置")
                        searchCircularPic.setImageResource(R.drawable.type2circular9);
                    else if (axialText == "L40:两列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular10);
                    else if (axialText == "L40:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type2circular11);
                } else if (typeText == "矩形管侧向矩形式") {
                    if (circularText == "大量开孔")
                        searchAxialPic.setImageResource(R.drawable.type3axail1);
                    else if (circularText == "部分开孔")
                        searchAxialPic.setImageResource(R.drawable.type3axail2);
                    else if (circularText == "少量开孔")
                        searchAxialPic.setImageResource(R.drawable.type3axail3);
                    if (axialText == "L20:双列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular1);
                    else if (axialText == "L20:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type3circular2);
                    else if (axialText == "L20:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular3);
                    else if (axialText == "L30:三列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular4);
                    else if (axialText == "L30:两列偏置")
                        searchCircularPic.setImageResource(R.drawable.type3circular5);
                    else if (axialText == "L30:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type3circular6);
                    else if (axialText == "L30:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular7);
                    else if (axialText == "L40:四列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular8);
                    else if (axialText == "L40:三列偏置")
                        searchCircularPic.setImageResource(R.drawable.type3circular9);
                    else if (axialText == "L40:两列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular10);
                    else if (axialText == "L40:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type3circular11);
                } else if (typeText == "弯管圆周式") {
                    if (circularText == "三面开孔")
                        searchAxialPic.setImageResource(R.drawable.type4axail1);
                    else if (circularText == "两面开孔")
                        searchAxialPic.setImageResource(R.drawable.type4axail2);
                    else if (circularText == "单面开孔")
                        searchAxialPic.setImageResource(R.drawable.type4axail3);
                    if (axialText == "L20:双列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular1);
                    else if (axialText == "L20:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type4circular2);
                    else if (axialText == "L20:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular3);
                    else if (axialText == "L30:三列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular4);
                    else if (axialText == "L30:两列偏置")
                        searchCircularPic.setImageResource(R.drawable.type4circular5);
                    else if (axialText == "L30:单列偏置")
                        searchCircularPic.setImageResource(R.drawable.type4circular6);
                    else if (axialText == "L30:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular7);
                    else if (axialText == "L40:四列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular8);
                    else if (axialText == "L40:三列偏置")
                        searchCircularPic.setImageResource(R.drawable.type4circular9);
                    else if (axialText == "L40:两列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular10);
                    else if (axialText == "L40:单列居中")
                        searchCircularPic.setImageResource(R.drawable.type4circular11);
                }
                if(TextUtils.isEmpty(searchPeek.getText())){
                    Toast.makeText(getContext(),"峰值频率不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    aimPeek = Integer.valueOf(searchPeek.getText().toString());
                    aimMax = (int)(((float)aimPeek)*1.1);
                    aimMin = (int)(((float)aimPeek)*0.9);
                    myDataAll = null;
                    if(circularChoose != 0 && axialChoose != 0 && cavityChoose!= 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCircular = ? and dataAxial = ? and dataCavity = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(circularChoose),String.valueOf(axialChoose),String.valueOf(cavityChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose != 0 && axialChoose != 0 && cavityChoose== 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCircular = ? and dataAxial = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(circularChoose),String.valueOf(axialChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose != 0 && axialChoose == 0 && cavityChoose!= 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCircular = ? and dataCavity = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(circularChoose),String.valueOf(cavityChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose == 0 && axialChoose != 0 && cavityChoose!= 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataAxial = ? and dataCavity = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(axialChoose),String.valueOf(cavityChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose != 0 && axialChoose == 0 && cavityChoose== 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCircular = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(circularChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose == 0 && axialChoose != 0 && cavityChoose== 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataAxial = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(axialChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose == 0 && axialChoose == 0 && cavityChoose!= 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCavity = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(cavityChoose),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }else if(circularChoose == 0 && axialChoose == 0 && cavityChoose== 0){
                        myDataAll = LitePal.where("dataType = ? and dataSize = ? and maxF <= ? and maxF >= ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(aimMax),String.valueOf(aimMin)).find(DataAll.class);
                    }

                    configureList.clear();
                    showAllAction();
                    adapter.notifyDataSetChanged();
                    if (myDataAll == null || myDataAll.size() == 0) {
                        Toast.makeText(getContext(),"未找到符合条件的消声器",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        changeTL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(myTL.getText())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"纵坐标最大值不能为空",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    maxTL = Integer.valueOf(myTL.getText().toString());
                    clearCanvas();
                    if(myDataAll != null) showAllAction();
                }

            }
        });

        searchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView.getVisibility() == View.GONE){
                    searchVol.setVisibility(View.VISIBLE);
                    searchSpan.setVisibility(View.VISIBLE);
                    searchCount.setVisibility(View.VISIBLE);
                    searchCircularPic.setVisibility(View.VISIBLE);
                    searchAxialPic.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.GONE);
                    titleVol.setVisibility(View.VISIBLE);
                    titleSpan.setVisibility(View.VISIBLE);
                    titleCount.setVisibility(View.VISIBLE);
                    titleCircularPic.setVisibility(View.VISIBLE);
                    titleAxialPic.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else{
                    searchVol.setVisibility(View.GONE);
                    searchSpan.setVisibility(View.GONE);
                    searchCount.setVisibility(View.GONE);
                    searchCircularPic.setVisibility(View.GONE);
                    searchAxialPic.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);
                    titleVol.setVisibility(View.GONE);
                    titleSpan.setVisibility(View.GONE);
                    titleCount.setVisibility(View.GONE);
                    titleCircularPic.setVisibility(View.GONE);
                    titleAxialPic.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

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
        blanking = (x2-x1)/8;
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
        for (int x = x1; x <= x2 ; x = x + blanking) {
            if (i == 0) {
                canvas.drawText(String.valueOf(0), x , y2 + 40, paint);
            } else if(i*space<100) {
                canvas.drawText(String.valueOf(i * space), x - 13, y2 + 40, paint);
            }else if(i*space<1000){
                canvas.drawText(String.valueOf(i * space), x - 22, y2 + 40, paint);
            }else if(i*space<10000){
                canvas.drawText(String.valueOf(i * space), x - 29, y2 + 40, paint);
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

        float x, y;
        float gap = ((float)blanking)/space*10;
        //上一次绘图坐标点 drawPoint的参数要求float
        float usedx = 0, usedy = 0;
        for (int i = 0; i <= 780; i++) {
            //横坐标   115对应1100长度即对应110个点  相当于每个点115/110=1.045455
            x = (float) gap * (i+20) + x1;
            //纵坐标 y2-y1=490对应70  相当于每个点7
            y = y2 - (float) data[i] * (y2 - y1) / maxTL;
            canvas.drawPoint(x, y, paint);
            //第一次不连线
            if (i != 0) canvas.drawLine(usedx, usedy, x, y, paint);
            usedx = x;
            usedy = y;
        }
        iv_canvas.setImageBitmap(baseBitmap);
    }


    //返回到登录界面事件和action按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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


    public void showAllAction(){
        for(int i=0; i<myDataAll.size(); i++) {
            DataAll dataResult = myDataAll.get(i);
            configureList.add(new Configure(dataResult.getDataId()));
            List<Float> tlList = dataResult.getTlList();
            for (int j = 0; j < CountUtil.tlNum; j++){
                data[j] = tlList.get(j);
            }

            int id = dataResult.getDataId();
            if (id%15 == 1) paint.setColor(0xFF4A7EBB);
            else if (id%15 == 2) paint.setColor(0xFFBE4B48);
            else if (id%15 == 3) paint.setColor(0xFF98B954);
            else if (id%15 == 4) paint.setColor(0xFF495A80);
            else if (id%15 == 5) paint.setColor(0xFFFD5B78);
            else if (id%15 == 6) paint.setColor(0xFF9DC8C8);
            else if (id%15 == 7) paint.setColor(0xFFE53A40);
            else if (id%15 == 8) paint.setColor(0xFFA593E0);
            else if (id%15 == 9) paint.setColor(0xFF5A9367);
            else if (id%15 == 10) paint.setColor(0xFFCFAA9E);
            else if (id%15 == 11) paint.setColor(0xFFEF5285);
            else if (id%15 == 12) paint.setColor(0xFF56A902);
            else if (id%15 == 13) paint.setColor(0xFF71226E);
            else if (id%15 == 14) paint.setColor(0xFF99F19E);
            else if (id%15 == 0) paint.setColor(0xFF376956);

            printGraphic();
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

