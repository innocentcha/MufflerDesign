package com.weining.android.myconfiguration;

import android.content.SharedPreferences;
import android.content.Context;
//import android.support.v7.widget.RecyclerView;
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
import com.weining.android.SearchActivity;
import com.weining.android.R;
import com.weining.android.db.DataAll;

import org.litepal.LitePal;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.litepal.LitePalApplication.getContext;

public class SearchConfigureAdapter extends RecyclerView.Adapter<SearchConfigureAdapter.ViewHolder> {
    private List<Configure> mConfigureList;
    private String typeText,cavityText,axialText,circularText,sizeText;
    private String volText,spanText,countText,peekText;
    List<DataAll>  myDataAll;
    int myPosition;
    int id;


    static class ViewHolder extends RecyclerView.ViewHolder{
        View configureView;
        TextView configureName,configureVol,configurePeek;
        TextView configureType,configureSize,configureAxial,configureCircular,configureCavity;
        TextView configureSpan,configureCount,configureDevol;
        ImageView configureAxialPic,configureCircularPic;
        Button configureBt;
        public ViewHolder(View view){
            super(view);
            configureView = view;
            configureName = (TextView)view.findViewById(R.id.search_configure_name); //名字
            configureVol = (TextView)view.findViewById(R.id.search_configure_vol);  //当前容积
            configurePeek = (TextView)view.findViewById(R.id.search_configure_peek);  //峰值频率
            configureType = (TextView) view.findViewById(R.id.search_configure_type);   //消声器种类
            configureSize = (TextView) view.findViewById(R.id.search_configure_size);   //主管尺寸
            configureAxial = (TextView) view.findViewById(R.id.search_configure_axial);  //周向开孔方式
            configureCircular = (TextView) view.findViewById(R.id.search_configure_circular);  //轴向开孔方式
            configureCavity = (TextView) view.findViewById(R.id.search_configure_cavity);  //腔体尺寸
            configureSpan = (TextView) view.findViewById(R.id.search_configure_span);   //d/mm
            configureCount = (TextView) view.findViewById(R.id.search_configure_count);  //孔数
            configureAxialPic = (ImageView) view.findViewById(R.id.search_configure_axial_pic);  //周向开孔示意图
            configureCircularPic = (ImageView) view.findViewById(R.id.search_configure_circular_pic);//轴向开孔示意图
            configureBt = (Button) view.findViewById(R.id.search_configure_bt);
        }
    }

    public SearchConfigureAdapter(List<Configure> configureList){

       mConfigureList = configureList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchconfigure_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.configureBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String btString = holder.configureBt.getText().toString();
                myPosition = 0;
                for (int j=0; j<btString.length(); j++){
                    if(btString.charAt(j) <= '9' && btString.charAt(j)>='0') myPosition = myPosition*10+btString.charAt(j)-'0'; //之前只取到第一个数字 忽略了不止一个数字的情况
                }
                Log.d("1122 myposition",String.valueOf(myPosition));
                SharedPreferences.Editor editor = getContext().getSharedPreferences("mySave",MODE_PRIVATE).edit();
                editor.putInt("id",mConfigureList.get(myPosition-1).getNum());
                editor.apply();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Configure configure = mConfigureList.get(position);
        myDataAll = LitePal.where("dataId = ?",String.valueOf(configure.getNum())).find(DataAll.class);
        DataAll dataResult = myDataAll.get(0);
        holder.configureName.setText("消声器 "+(position+1));
        holder.configureBt.setText("保存 "+(position+1));
        id = dataResult.getDataId();
        if (id%15 == 1) holder.configureName.setTextColor(0xFF4A7EBB);
        else if (id%15 == 2) holder.configureName.setTextColor(0xFFBE4B48);
        else if (id%15 == 3) holder.configureName.setTextColor(0xFF98B954);
        else if (id%15 == 4) holder.configureName.setTextColor(0xFF495A80);
        else if (id%15 == 5) holder.configureName.setTextColor(0xFFFD5B78);
        else if (id%15 == 6) holder.configureName.setTextColor(0xFF9DC8C8);
        else if (id%15 == 7) holder.configureName.setTextColor(0xFFE53A40);
        else if (id%15 == 8) holder.configureName.setTextColor(0xFFA593E0);
        else if (id%15 == 9) holder.configureName.setTextColor(0xFF5A9367);
        else if (id%15 == 10) holder.configureName.setTextColor(0xFFCFAA9E);
        else if (id%15 == 11) holder.configureName.setTextColor(0xFFEF5285);
        else if (id%15 == 12) holder.configureName.setTextColor(0xFF56A902);
        else if (id%15 == 13) holder.configureName.setTextColor(0xFF71226E);
        else if (id%15 == 14) holder.configureName.setTextColor(0xFF99F19E);
        else if (id%15 == 0) holder.configureName.setTextColor(0xFF376956);

        int type = dataResult.getDataType();
        if(type == 1) typeText = "直管圆周式";
        else if(type == 2) typeText = "圆管侧向矩形式";
        else if(type == 3) typeText = "矩形管侧向矩形式";
        else if(type == 4) typeText = "弯管圆周式";

        int size = dataResult.getDataSize();
        if(type != 3){
            if(size == 1) sizeText = "D35";
            else if(size == 2) sizeText = "D50";
            else if(size == 3) sizeText = "D65";
        }else{
            if(size == 1) sizeText = "W80";
            else if(size == 2) sizeText = "W100";
            else if(size == 3) sizeText = "W120";
        }

        int cavity = dataResult.getDataCavity();
        if(type == 1){
            if(size == 1){
                if(cavity == 1) cavityText = "D50";
                else if(cavity == 2) cavityText = "D65";
                else if(cavity == 3) cavityText = "D80";
                else if(cavity == 4) cavityText = "D95";
            } else if(size == 2){
                if(cavity == 1) cavityText = "D70";
                else if(cavity == 2) cavityText = "D90";
                else if(cavity == 3) cavityText = "D110";
                else if(cavity == 4) cavityText = "D130";
            }else if(size == 3){
                if(cavity == 1) cavityText = "D85";
                else if(cavity == 2) cavityText = "D105";
                else if(cavity == 3) cavityText = "D125";
                else if(cavity == 4) cavityText = "D145";
            }
        }else if(type == 2){
            if(size == 1){
                if(cavity == 1) cavityText = "H10";
                else if(cavity == 2) cavityText = "H20";
                else if(cavity == 3) cavityText = "H40";
                else if(cavity == 4) cavityText = "H60";
                else if(cavity == 5) cavityText = "H80";
            } else if(size == 2 || size == 3){
                if(cavity == 1) cavityText = "H20";
                else if(cavity == 2) cavityText = "H40";
                else if(cavity == 3) cavityText = "H60";
                else if(cavity == 4) cavityText = "H80";
                else if(cavity == 5) cavityText = "H100";
            }
        }else if(type == 3){
            if(cavity == 1) cavityText = "H20";
            else if(cavity == 2) cavityText = "H40";
            else if(cavity == 3) cavityText = "H60";
            else if(cavity == 4) cavityText = "H80";
            else if(cavity == 5) cavityText = "H100";
        }else if(type == 4){
            if(size == 1){
                if(cavity == 1) cavityText = "D55";
                else if(cavity == 2) cavityText = "D75";
                else if(cavity == 3) cavityText = "D85";
                else if(cavity == 4) cavityText = "D95";
            } else if(size == 2){
                if(cavity == 1) cavityText = "D70";
                else if(cavity == 2) cavityText = "D90";
                else if(cavity == 3) cavityText = "D110";
                else if(cavity == 4) cavityText = "D130";
            }else if(size == 3){
                if(cavity == 1) cavityText = "D85";
                else if(cavity == 2) cavityText = "D105";
                else if(cavity == 3) cavityText = "D125";
                else if(cavity == 4) cavityText = "D145";
            }
        }

        int circular = dataResult.getDataCircular();
        if(type == 1){
            if(circular == 1) circularText = "四面开孔";
            else if(circular == 2) circularText = "三面开孔";
            else if(circular == 3) circularText = "对面开孔";
            else if(circular == 4) circularText = "单面开孔";
        }else if(type == 4){
            if(circular == 1) circularText = "三面开孔";
            else if(circular == 2) circularText = "两面开孔";
            else if(circular == 3) circularText = "单面开孔";
        }else{
            if(circular == 1) circularText = "大量开孔";
            else if(circular == 2) circularText = "部分开孔";
            else if(circular == 3) circularText = "少量开孔";
        }

        int axial = dataResult.getDataAxial();
        if(axial == 1) axialText = "L20:双列居中";
        else if(axial == 2) axialText = "L20:单列偏置";
        else if(axial == 3) axialText = "L20:单列居中";
        else if(axial == 4) axialText = "L30:三列居中";
        else if(axial == 5) axialText = "L30:两列偏置";
        else if(axial == 6) axialText = "L30:单列偏置";
        else if(axial == 7) axialText = "L30:单列居中";
        else if(axial == 8) axialText = "L40:四列居中";
        else if(axial == 9) axialText = "L40:三列偏置";
        else if(axial == 10) axialText = "L40:两列居中";
        else if(axial == 11) axialText = "L40:单列居中";

        float vol = dataResult.getDataVol();
        volText = String.valueOf(vol);

        int span = dataResult.getDataSpan();
        spanText = String.valueOf(span);

        int count  = dataResult.getDataCount();
        countText = String.valueOf(count);

        int peek = dataResult.getMaxF();
        peekText = String.valueOf(peek);

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

        holder.configureType.setText(typeText);
        holder.configureSize.setText(sizeText);
        holder.configureCircular.setText(circularText);
        holder.configureAxial.setText(axialText);
        holder.configureCavity.setText(cavityText);
        holder.configureVol.setText(volText);
        holder.configureSpan.setText(spanText);
        holder.configureCount.setText(countText);
        holder.configurePeek.setText(peekText);
    }

    @Override
    public int getItemCount() {
        return mConfigureList.size();
    }

}
