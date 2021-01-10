package com.weining.android.myconfiguration;

import android.content.SharedPreferences;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.weining.android.GraphicActivity;
import com.weining.android.R;
import com.weining.android.db.DataAll;

import org.litepal.LitePal;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.litepal.LitePalApplication.getContext;

public class ConfigureAdapter extends RecyclerView.Adapter<ConfigureAdapter.ViewHolder> {
    private List<Configure> mConfigureList;
    private String typeText,cavityText,axialText,circularText,sizeText;
    private String volText,spanText,decvolText;
    private int typeChoose,sizeChoose,circularChoose,axialChoose,cavityChoose;
    private int myPosition;



    static class ViewHolder extends RecyclerView.ViewHolder{
        View configureView;
        TextView configureName,configureVol,configurePeek;
        Spinner configureType,configureSize,configureAxial,configureCircular,configureCavity;
        EditText configureSpan,configureCount,configureDevol,configureDegree;
        ImageView configureAxialPic,configureCircularPic;
        Button configureBt,configureSave;
        public ViewHolder(View view){
            super(view);
            configureView = view;
            configureName = (TextView)view.findViewById(R.id.configure_name); //名字
            configureVol = (TextView)view.findViewById(R.id.configure_vol);  //当前容积
            configurePeek = (TextView)view.findViewById(R.id.configure_peek);  //峰值频率
            configureType = (Spinner) view.findViewById(R.id.configure_type);   //消声器种类
            configureSize = (Spinner) view.findViewById(R.id.configure_size);   //主管尺寸
            configureAxial = (Spinner) view.findViewById(R.id.configure_axial);  //周向开孔方式
            configureCircular = (Spinner) view.findViewById(R.id.configure_circular);  //轴向开孔方式
            configureCavity = (Spinner) view.findViewById(R.id.configure_cavity);  //腔体尺寸
            configureSpan = (EditText) view.findViewById(R.id.configure_span);   //d/mm
            configureCount = (EditText) view.findViewById(R.id.configure_count);  //孔数
            configureDevol = (EditText) view.findViewById(R.id.configure_decvol);  //消声容积
            configureDegree = (EditText) view.findViewById(R.id.configure_degree);  //消声容积
            configureAxialPic = (ImageView) view.findViewById(R.id.configure_axial_pic);  //周向开孔示意图
            configureCircularPic = (ImageView) view.findViewById(R.id.configure_circular_pic);//轴向开孔示意图
            configureBt = (Button) view.findViewById(R.id.configure_bt);
            configureSave = (Button) view.findViewById(R.id.configure_save);
        }
    }

    public ConfigureAdapter(List<Configure> configureList){
        mConfigureList = configureList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.configure_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        final String[] type = new String[]{"直管圆周式", "圆管侧向矩形式", "矩形管侧向矩形式", "弯管圆周式" };
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, type);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.configureType.setAdapter(adapterType);
        holder.configureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Type 动作
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeText = holder.configureType.getItemAtPosition(i).toString();
                typeChoose = (int)holder.configureType.getItemIdAtPosition(i);
                Log.d("myChoose type",String.valueOf(typeChoose));

                String[] size,axial;

                if(typeText != "矩形管侧向矩形式"){
                    size = new String[]{"D35", "D50", "D65" };
                }else{
                    size = new String[]{"W80", "W100", "W120" };
                }
                ArrayAdapter<String> adapterSize = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, size);
                adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.configureSize.setAdapter(adapterSize);
                holder.configureSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    //Size 动作
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        sizeText = holder.configureSize.getItemAtPosition(i).toString();
                        sizeChoose = (int) holder.configureSize.getItemIdAtPosition(i);
                        Log.d("myChoose size",String.valueOf(sizeChoose));

                        String[] cavity;
                        if(sizeText == "D35" && typeText == "直管圆周式"){
                            cavity = new String[]{"D50", "D65", "D80" , "D95" };//1
                        }else if(sizeText == "D50" && typeText == "直管圆周式"){
                            cavity = new String[]{"D70", "D90", "D110" , "D130" };//2
                        }else if(sizeText == "D65" && typeText == "直管圆周式"){
                            cavity = new String[]{"D85", "D105", "D125" , "D145" };//3
                        }else if(sizeText == "D35" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{"H10", "H20", "H40" , "H60" , "H80"};//4
                        }else if(sizeText == "D50" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{ "H20", "H40" , "H60" , "H80","H100"};//5
                        }else if(sizeText == "D65" && typeText == "圆管侧向矩形式"){
                            cavity = new String[]{"H20", "H40" , "H60" , "H80","H100" };//5
                        }else if(typeText == "矩形管侧向矩形式"){
                            cavity = new String[]{"H20", "H40" , "H60" , "H80","H100" };//5
                        }else if(sizeText == "D35" && typeText == "弯管圆周式"){
                            cavity = new String[]{"D55", "D75", "D85" , "D95" };//6
                        }else if(sizeText == "D50" && typeText == "弯管圆周式"){
                            cavity = new String[]{"D70", "D90", "D110" , "D130" };//2
                        }else if(sizeText == "D65" && typeText == "弯管圆周式"){
                            cavity = new String[]{"D85", "D105", "D125" , "D140" };//7
                        }else{
                            cavity = new String[]{"D50", "D65", "D80" , "D95" };//1
                        }

                        ArrayAdapter<String> adapterCavity = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, cavity);
                        adapterCavity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        holder.configureCavity.setAdapter(adapterCavity);
                        holder.configureCavity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            //Cavity 动作
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                cavityText = holder.configureCavity.getItemAtPosition(i).toString();
                                cavityChoose = (int) holder.configureCavity.getItemIdAtPosition(i);
                                Log.d("myChoose cavity",String.valueOf(cavityChoose));
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
                    circular = new String[]{"四面开孔", "三面开孔", "对面开孔", "单面开孔" };
                }else if(typeText == "弯管圆周式"){
                    circular = new String[]{"三面开孔", "两面开孔", "单面开孔"};
                }else{
                    circular = new String[]{"大量开孔", "部分开孔", "少量开孔"};
                }
                ArrayAdapter<String> adapterCircular = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, circular);
                adapterCircular.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.configureCircular.setAdapter(adapterCircular);
                holder.configureCircular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    //Circular 动作
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        circularText = holder.configureCircular.getItemAtPosition(i).toString();
                        circularChoose = (int) holder.configureCircular.getItemIdAtPosition(i);
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


        String[] axial = new String[]{"L20:双列居中", "L20:单列偏置", "L20:单列居中", "L30:三列居中","L30:两列偏置", "L30:单列偏置", "L30:单列居中", "L40:四列居中", "L40:三列偏置" , "L40:两列居中", "L40:单列居中"};
        ArrayAdapter<String> adapterAxial = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, axial);
        adapterAxial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.configureAxial.setAdapter(adapterAxial);
        holder.configureAxial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Axial 动作
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    axialText = holder.configureAxial.getItemAtPosition(i).toString();
                    axialChoose = (int) holder.configureAxial.getItemIdAtPosition(i);
            }

            @Override

            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        //图片的名字反了  懒得改了..
        holder.configureBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                axialText = holder.configureAxial.getSelectedItem().toString();
                axialChoose = circularChoose = holder.configureAxial.getSelectedItemPosition();
                circularText = holder.configureCircular.getSelectedItem().toString();
                circularChoose = holder.configureCircular.getSelectedItemPosition();
                cavityText = holder.configureCavity.getSelectedItem().toString();
                cavityChoose =  holder.configureCavity.getSelectedItemPosition();
                sizeText = holder.configureSize.getSelectedItem().toString();
                sizeChoose = holder.configureSize.getSelectedItemPosition();
                typeText = holder.configureType.getSelectedItem().toString();
                typeChoose = holder.configureType.getSelectedItemPosition();
                if(typeText == "直管圆周式"){
                    if(circularText == "四面开孔") holder.configureAxialPic.setImageResource(R.drawable.type1axail1);
                    else if(circularText == "三面开孔") holder.configureAxialPic.setImageResource(R.drawable.type1axail2);
                    else if(circularText == "对面开孔") holder.configureAxialPic.setImageResource(R.drawable.type1axail3);
                    else if(circularText == "单面开孔") holder.configureAxialPic.setImageResource(R.drawable.type1axail4);
                    if(axialText == "L20:双列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular1);
                    else if(axialText == "L20:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type1circular2);
                    else if(axialText == "L20:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular3);
                    else if(axialText == "L30:三列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular4);
                    else if(axialText == "L30:两列偏置") holder.configureCircularPic.setImageResource(R.drawable.type1circular5);
                    else if(axialText == "L30:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type1circular6);
                    else if(axialText == "L30:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular7);
                    else if(axialText == "L40:四列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular8);
                    else if(axialText == "L40:三列偏置") holder.configureCircularPic.setImageResource(R.drawable.type1circular9);
                    else if(axialText == "L40:两列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular10);
                    else if(axialText == "L40:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type1circular11);
                }
                else if(typeText == "圆管侧向矩形式"){
                    if(circularText == "大量开孔") holder.configureAxialPic.setImageResource(R.drawable.type2axail1);
                    else if(circularText == "部分开孔") holder.configureAxialPic.setImageResource(R.drawable.type2axail2);
                    else if(circularText == "少量开孔") holder.configureAxialPic.setImageResource(R.drawable.type2axail3);
                    if(axialText == "L20:双列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular1);
                    else if(axialText == "L20:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type2circular2);
                    else if(axialText == "L20:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular3);
                    else if(axialText == "L30:三列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular4);
                    else if(axialText == "L30:两列偏置") holder.configureCircularPic.setImageResource(R.drawable.type2circular5);
                    else if(axialText == "L30:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type2circular6);
                    else if(axialText == "L30:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular7);
                    else if(axialText == "L40:四列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular8);
                    else if(axialText == "L40:三列偏置") holder.configureCircularPic.setImageResource(R.drawable.type2circular9);
                    else if(axialText == "L40:两列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular10);
                    else if(axialText == "L40:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type2circular11);
                }
                else if(typeText == "矩形管侧向矩形式"){
                    if(circularText == "大量开孔") holder.configureAxialPic.setImageResource(R.drawable.type3axail1);
                    else if(circularText == "部分开孔") holder.configureAxialPic.setImageResource(R.drawable.type3axail2);
                    else if(circularText == "少量开孔") holder.configureAxialPic.setImageResource(R.drawable.type3axail3);
                    if(axialText == "L20:双列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular1);
                    else if(axialText == "L20:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type3circular2);
                    else if(axialText == "L20:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular3);
                    else if(axialText == "L30:三列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular4);
                    else if(axialText == "L30:两列偏置") holder.configureCircularPic.setImageResource(R.drawable.type3circular5);
                    else if(axialText == "L30:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type3circular6);
                    else if(axialText == "L30:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular7);
                    else if(axialText == "L40:四列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular8);
                    else if(axialText == "L40:三列偏置") holder.configureCircularPic.setImageResource(R.drawable.type3circular9);
                    else if(axialText == "L40:两列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular10);
                    else if(axialText == "L40:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type3circular11);
                }
                else if(typeText == "弯管圆周式"){
                    if(circularText == "三面开孔") holder.configureAxialPic.setImageResource(R.drawable.type4axail1);
                    else if(circularText == "两面开孔") holder.configureAxialPic.setImageResource(R.drawable.type4axail2);
                    else if(circularText == "单面开孔") holder.configureAxialPic.setImageResource(R.drawable.type4axail3);
                    if(axialText == "L20:双列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular1);
                    else if(axialText == "L20:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type4circular2);
                    else if(axialText == "L20:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular3);
                    else if(axialText == "L30:三列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular4);
                    else if(axialText == "L30:两列偏置") holder.configureCircularPic.setImageResource(R.drawable.type4circular5);
                    else if(axialText == "L30:单列偏置") holder.configureCircularPic.setImageResource(R.drawable.type4circular6);
                    else if(axialText == "L30:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular7);
                    else if(axialText == "L40:四列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular8);
                    else if(axialText == "L40:三列偏置") holder.configureCircularPic.setImageResource(R.drawable.type4circular9);
                    else if(axialText == "L40:两列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular10);
                    else if(axialText == "L40:单列居中") holder.configureCircularPic.setImageResource(R.drawable.type4circular11);
                }

                String btString = holder.configureBt.getText().toString();
                for (int j=0; j<btString.length(); j++){
                    if(btString.charAt(j) <= '9' && btString.charAt(j)>='0') myPosition = btString.charAt(j)-'0';
                }
                //不能改变数据源typeSize的值呀！！
                List<DataAll>  myDataAll = LitePal.where("dataType = ? and dataSize = ? and dataCircular = ? and dataAxial = ? and dataCavity = ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(circularChoose+1),String.valueOf(axialChoose+1),String.valueOf(cavityChoose+1)).find(DataAll.class);
                DataAll dataResult = myDataAll.get(0);

                int beforeSpan = dataResult.getDataSpan();
                int beforeCount = dataResult.getDataCount();
                float beforeVol = dataResult.getDataVol();
                holder.configureVol.setText(String.valueOf(beforeVol)+"/"+String.valueOf(beforeSpan)+"/"+String.valueOf(beforeCount));
                Log.d("wangting",holder.configureSpan.getText().toString()+" 11");
                if(TextUtils.isEmpty(holder.configureSpan.getText()) || TextUtils.isEmpty(holder.configureCount.getText()) || TextUtils.isEmpty(holder.configureDevol.getText())|| TextUtils.isEmpty(holder.configureDegree.getText())){
                    Toast.makeText(getContext(),"孔径、孔数、消声容积、温度均不能为空",Toast.LENGTH_SHORT).show();
                }
                else{
                    double nowSpan = Double.valueOf(holder.configureSpan.getText().toString());
                    double nowCount = Double.valueOf(holder.configureCount.getText().toString());
                    double nowDegree = Double.valueOf(holder.configureDegree.getText().toString());
                    float nowVol =  Float.valueOf(holder.configureDevol.getText().toString());
                    float correct = (float)( Math.sqrt((float)(nowSpan * nowSpan * nowCount)/(beforeSpan * beforeSpan * beforeCount)*beforeVol/nowVol)*(20.05*Math.sqrt(273+nowDegree)/343.2));
                    Log.d("wangting nV",String.valueOf(nowCount));
                    Log.d("wangting nV",String.valueOf(nowVol));
                    int nowPeek = (int)(correct * dataResult.getMaxF());
                    holder.configurePeek.setText(String.valueOf(nowPeek));

                    SharedPreferences.Editor editor = getContext().getSharedPreferences("choosedId"+myPosition,MODE_PRIVATE).edit();
                    editor.putInt("id",dataResult.getDataId());
                    editor.putFloat("correct",correct);
                    editor.apply();

                    SharedPreferences.Editor editor2 = getContext().getSharedPreferences("savedContent"+myPosition,MODE_PRIVATE).edit();
                    editor2.putFloat("span",(float)nowSpan);
                    editor2.putFloat("count",(float)nowCount);
                    editor2.putFloat("degree",(float)nowDegree);
                    editor2.putFloat("devol",nowVol);
                    editor2.putInt("peek",nowPeek);
                    editor2.putString("typeText",typeText);
                    editor2.putString("circularText",circularText);
                    editor2.putString("axialText",axialText);
                    editor2.apply();

                    if (myPosition == 1) holder.configureName.setTextColor(0xFF4A7EBB);
                    else if (myPosition == 2) holder.configureName.setTextColor(0xFFBE4B48);
                    else if (myPosition == 3) holder.configureName.setTextColor(0xFF98B954);
                    else if (myPosition == 4) holder.configureName.setTextColor(0xFF495A80);
                    else if (myPosition == 5) holder.configureName.setTextColor(0xFFFD5B78);
                    else if (myPosition == 6) holder.configureName.setTextColor(0xFF376956);

                }
            }
        });
        volText = holder.configureVol.getText().toString();
        spanText = holder.configureSpan.getText().toString();
        decvolText = holder.configureDevol.getText().toString();

        holder.configureSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getContext().getSharedPreferences("mySave",MODE_PRIVATE);
                int id = pref.getInt("id",0);
                if(id != 0){
                    List<DataAll>  SavedData = LitePal.where("dataId = ?",String.valueOf(id)).find(DataAll.class);
                    DataAll savedData = SavedData.get(0);
                    int type = savedData.getDataType();
                    int size = savedData.getDataSize();
                    int circular = savedData.getDataCircular();
                    int axial = savedData.getDataAxial();
                    int cavity = savedData.getDataCavity();
                    int span = savedData.getDataSpan();
                    int count = savedData.getDataCount();
                    float vol = savedData.getDataVol();
                    int peek = savedData.getMaxF();
                    holder.configureType.setSelection(type-1,true);
                    holder.configureSize.setSelection(size-1,true);
                    holder.configureCircular.setSelection(circular-1,true);
                    holder.configureAxial.setSelection(axial-1,true);
                    holder.configureCavity.setSelection(cavity-1,true);
                    holder.configureVol.setText(String.valueOf(vol)+"/"+String.valueOf(span)+"/"+String.valueOf(count));
                    holder.configureSpan.setText(String.valueOf(span));
                    holder.configureCount.setText(String.valueOf(count));
                    holder.configureDevol.setText(String.valueOf(vol));
                    holder.configurePeek.setText(String.valueOf(peek));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.setIsRecyclable(false);
        Configure configure = mConfigureList.get(position);
        holder.configureName.setText("消声器 "+configure.getNum());
        holder.configureBt.setText("显示 "+configure.getNum());
        holder.configureSave.setText("加载");
    }

    @Override
    public int getItemCount() {
        return mConfigureList.size();
    }





}
