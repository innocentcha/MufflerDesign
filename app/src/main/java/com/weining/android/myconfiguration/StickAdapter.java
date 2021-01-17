package com.weining.android.myconfiguration;

import android.content.SharedPreferences;
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

import androidx.recyclerview.widget.RecyclerView;
import com.weining.android.R;
import com.weining.android.db.DataStick;

import org.litepal.LitePal;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.litepal.LitePalApplication.getContext;

public class StickAdapter extends RecyclerView.Adapter<StickAdapter.ViewHolder> {

    private List<Configure> mConfigureList;
    private String typeText, cavityText, lengthText, sizeText;
    private String volText, areaText, decvolText, peekText;
    private int typeChoose, sizeChoose, lengthChoose, cavityChoose;
    private int myPosition;


    static class ViewHolder extends RecyclerView.ViewHolder {

        View configureView;
        TextView configureName, configureVol, configurePeek;
        Spinner configureType, configureSize, configureLength, configureCavity;
        EditText configureArea, configureDevol, configureDegree;
        ImageView configureTypePic;
        Button configureBt, configureSave;

        public ViewHolder(View view) {
            super(view);
            configureView = view;
            configureName = (TextView) view.findViewById(R.id.stick_name); //名字
            configureVol = (TextView) view.findViewById(R.id.stick_vol);  //当前容积
            configurePeek = (TextView) view.findViewById(R.id.stick_peek);  //峰值频率
            configureType = (Spinner) view.findViewById(R.id.stick_type);   //消声器种类
            configureSize = (Spinner) view.findViewById(R.id.stick_size);   //主管尺寸
            configureLength = (Spinner) view.findViewById(R.id.stick_length);  //单边插孔长度
            configureCavity = (Spinner) view.findViewById(R.id.stick_cavity);  //腔体尺寸
            configureArea = (EditText) view.findViewById(R.id.stick_area);   //开槽面积
            configureDevol = (EditText) view.findViewById(R.id.stick_decvol);  //消声容积
            configureDegree = (EditText) view.findViewById(R.id.stick_degree);  //温度
            configureTypePic = (ImageView) view.findViewById(R.id.stick_type_pic);
            configureBt = (Button) view.findViewById(R.id.stick_bt);
            configureSave = (Button) view.findViewById(R.id.stick_save);
        }
    }

    public StickAdapter(List<Configure> configureList) {
        mConfigureList = configureList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stick_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        final String[] type = new String[]{"单边插入", "双边插入" };
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, type);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.configureType.setAdapter(adapterType);
        holder.configureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Type 动作
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeText = holder.configureType.getItemAtPosition(i).toString();
                typeChoose = (int)holder.configureType.getItemIdAtPosition(i);

                String[] length;
                if(typeText == "单边插入"){
                    length= new String[]{"L20:2", "L20:4", "L20:6", "L30:2","L30:4", "L30:6", "L30:8", "L40:2", "L40:4" , "L40:6", "L40:8"};
                }else{
                    length= new String[]{"L20:4", "L20:8", "L20:12", "L30:4","L30:8", "L30:12", "L30:16", "L40:4", "L40:8" , "L40:12", "L40:16"};
                }
                ArrayAdapter<String> adapterLength = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, length);
                adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.configureLength.setAdapter(adapterLength);
                holder.configureLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    //Axial 动作
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        lengthText = holder.configureLength.getItemAtPosition(i).toString();
                        lengthChoose = (int) holder.configureLength.getItemIdAtPosition(i);

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


        String[] size;
        size = new String[]{"D35", "D50", "D65" };
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
                if(sizeText == "D35"){
                    cavity = new String[]{"D45", "D55", "D65" , "D75" };
                }else if(sizeText == "D50"){
                    cavity = new String[]{"D60", "D70", "D80" , "D90" };
                }else{
                    cavity = new String[]{"D75", "D85", "D95", "D105"};
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


        //图片的名字反了  懒得改了..
        holder.configureBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeText = holder.configureType.getSelectedItem().toString();
                typeChoose = holder.configureType.getSelectedItemPosition();
                sizeText = holder.configureSize.getSelectedItem().toString();
                sizeChoose = holder.configureSize.getSelectedItemPosition();
                cavityText = holder.configureCavity.getSelectedItem().toString();
                cavityChoose = holder.configureCavity.getSelectedItemPosition();
                lengthText = holder.configureLength.getSelectedItem().toString();
                lengthChoose = holder.configureLength.getSelectedItemPosition();
                if (typeText == "单边插入") {
                    holder.configureTypePic.setImageResource(R.drawable.type1);
                } else {
                    holder.configureTypePic.setImageResource(R.drawable.type2);
                }

                String btString = holder.configureBt.getText().toString();
                for (int j = 0; j < btString.length(); j++) {
                    if (btString.charAt(j) <= '9' && btString.charAt(j) >= '0') {
                        myPosition = btString.charAt(j) - '0';
                    }
                }
                //不要去改变数据源typeSize的值！！
                List<DataStick>  myDataStick = LitePal.where("dataType = ? and dataSize = ? and dataLength = ? and dataCavity = ?",String.valueOf(typeChoose+1),String.valueOf(sizeChoose+1),String.valueOf(lengthChoose+1),String.valueOf(cavityChoose+1)).find(DataStick.class);
                DataStick dataResult = myDataStick.get(0);
                for(DataStick data :myDataStick){
                    if(data.getDataId() < 265) {dataResult = data;break;}
                }

                int tmpCoefficient,tmpL,tmp;
                if(lengthChoose >= 0 && lengthChoose <= 2) {tmpCoefficient = 0; tmpL = (lengthChoose+1)*2*(typeChoose+1);}
                else if(lengthChoose >= 3 && lengthChoose <= 6) {tmpCoefficient = 1; tmpL = (lengthChoose+1-3)*2*(typeChoose+1);}
                else  {tmpCoefficient = 2; tmpL = (lengthChoose+1-7)*2*(typeChoose+1);}

                float beforeArea = (float) 3.14159*(35+15*sizeChoose)*(20+tmpCoefficient*10-tmpL);
                //基数不一定都是45！  和sizeChoose也有关系
                tmp = 45+sizeChoose*15+10*cavityChoose;
                float beforeVol = (float)(0.25*3.14159*(tmp*tmp-(40+15*sizeChoose)*(40+15*sizeChoose))*(20+tmpCoefficient*10)/1000000);
                holder.configureVol.setText(String.format("%.6f",beforeVol)+"/"+String.format("%.2f",beforeArea));
                holder.configurePeek.setText(String.valueOf(dataResult.getMaxF()));
                if(TextUtils.isEmpty(holder.configureArea.getText()) ||  TextUtils.isEmpty(holder.configureDevol.getText())|| TextUtils.isEmpty(holder.configureDegree.getText())){
                    Toast.makeText(getContext(),"开槽面积、消声容积、温度均不能为空",Toast.LENGTH_SHORT).show();
                } else{
                    double nowArea = Double.valueOf(holder.configureArea.getText().toString());
                    double nowDegree = Double.valueOf(holder.configureDegree.getText().toString());
                    float nowVol =  Float.valueOf(holder.configureDevol.getText().toString());
                    float correct = (float)( Math.sqrt((float)(nowArea / nowVol)/(beforeArea / beforeVol))*(20.05*Math.sqrt(273+nowDegree)/342.2));
                    int nowPeek = (int)(correct * dataResult.getMaxF());
                    holder.configurePeek.setText(String.valueOf(nowPeek));

                    SharedPreferences.Editor editor = getContext().getSharedPreferences("choosedId"+myPosition,MODE_PRIVATE).edit();
                    editor.putInt("id",dataResult.getDataId());
                    editor.putFloat("correct",correct);
                    editor.apply();

                    SharedPreferences.Editor editor2 = getContext().getSharedPreferences("savedStick"+myPosition,MODE_PRIVATE).edit();
                    editor2.putFloat("area",(float)nowArea);
                    editor2.putString("lengthText",lengthText);
                    editor2.putFloat("degree",(float)nowDegree);
                    editor2.putFloat("devol",nowVol);
                    editor2.putInt("peek",nowPeek);
                    editor2.putString("typeText",typeText);
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
        decvolText = holder.configureDevol.getText().toString();

        holder.configureSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getContext().getSharedPreferences("StickSave", MODE_PRIVATE);
                int id = pref.getInt("id", 0);
                if (id != 0) {
                    List<DataStick> SavedData = LitePal.where("dataId = ?", String.valueOf(id)).find(DataStick.class);
                    DataStick savedData = SavedData.get(0);
                    int type = savedData.getDataType();
                    int size = savedData.getDataSize();
                    int length = savedData.getDataLength();
                    int cavity = savedData.getDataCavity();
                    int peek = savedData.getMaxF();
                    holder.configureType.setSelection(type - 1, true);
                    holder.configureSize.setSelection(size - 1, true);
                    holder.configureLength.setSelection(length - 1, true);
                    holder.configureCavity.setSelection(cavity - 1, true);
                    holder.configurePeek.setText(String.valueOf(peek));

                    int tmp = length;
                    int tmpL = 2;
                    if (tmp >= 1 && tmp <= 3) {
                        tmpL = tmp * 2;
                    } else if (tmp == 7 || tmp == 11) {
                        tmpL = 8;
                    } else {
                        tmpL = (tmp - 3) % 4 * 2;
                    }
                    float area = (float) 3.14159 * 35 * (20 - tmpL);
                    tmp = 35 + 10 * cavity;
                    float vol = (float) (0.25 * 3.14159 * (tmp * tmp - 1600) * 20 / 1000000);
                    holder.configureVol.setText(String.format("%.6f", vol) + "/" + String.format("%.2f", area));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Configure configure = mConfigureList.get(position);
        holder.configureName.setText("消声器 " + configure.getNum());
        holder.configureBt.setText("显示 " + configure.getNum());
        holder.configureSave.setText("加载");
    }

    @Override
    public int getItemCount() {
        return mConfigureList.size();
    }
}
