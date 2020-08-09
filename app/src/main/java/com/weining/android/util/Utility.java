package com.weining.android.util;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import com.weining.android.db.DataAll;
import com.weining.android.db.DataStick;
import com.weining.android.db.Record;

public class Utility {
    /**
     * 解析和处理服务器返回的账号信息
     */
    public static boolean handleRecordResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allRecords = new JSONArray(response);
                LitePal.deleteAll(Record.class);
                for (int i = 0; i < allRecords.length(); i++) {
                    JSONObject recordObject = allRecords.getJSONObject(i);
                    Record record = new Record();
                    record.setAccount(recordObject.getString("account"));
                    record.setPassword(recordObject.getString("password"));
                    record.setIMEI(recordObject.getString("IMEI"));
                    record.setYear(recordObject.getInt("year"));
                    record.setMonth(recordObject.getInt("month"));
                    record.setDay(recordObject.getInt("day"));
                    record.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的数据信息
     */
    public static boolean handleDataAllResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allData = new JSONArray(response);
                LitePal.deleteAll(DataAll.class);
                for (int i = 0; i < allData.length(); i++) {
                    JSONObject dataObject = allData.getJSONObject(i);
                    DataAll data = new DataAll();
                    List<Float> tlList = data.getTlList();
                    data.setDataId(dataObject.getInt("id"));
                    data.setDataType(dataObject.getInt("type"));
                    data.setDataSize(dataObject.getInt("size"));
                    data.setDataCircular(dataObject.getInt("circular"));
                    data.setDataAxial(dataObject.getInt("axial"));
                    data.setDataCavity(dataObject.getInt("cavity"));
                    data.setDataSpan(dataObject.getInt("span"));
                    data.setDataCount(dataObject.getInt("count"));
                    data.setMaxF(dataObject.getInt("maxF"));
                    data.setDataVol((float) dataObject.getDouble("vol"));
                    for (int f = CountUtil.freqMin; f <= CountUtil.freqMax; f += CountUtil.freqStep) {
                        tlList.add((float) dataObject.getDouble("f" + f));
                    }
                    data.setTlList(tlList);
                    data.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的数据信息
     */
    public static boolean handleDataStickResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allData = new JSONArray(response);
                LitePal.deleteAll(DataStick.class);
                for (int i = 0; i < allData.length(); i++) {
                    //Log.d("Wangting before",String.valueOf(i));
                    JSONObject dataObject = allData.getJSONObject(i);
                    DataStick data = new DataStick();
                    List<Float> tlList = data.getTlList();
                    data.setDataId(dataObject.getInt("id"));
                    data.setDataType(dataObject.getInt("type"));
                    data.setDataSize(dataObject.getInt("size"));
                    data.setDataLength(dataObject.getInt("length"));
                    data.setDataCavity(dataObject.getInt("cavity"));
                    data.setMaxF(dataObject.getInt("maxF"));
                    for (int f = CountUtil.freqMin; f <= CountUtil.freqMax; f += CountUtil.freqStep) {
                        tlList.add((float) dataObject.getDouble("f" + f));
                    }
                    data.setTlList(tlList);
                    data.save();
                    //Log.d("Wangting save",String.valueOf(i));
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
