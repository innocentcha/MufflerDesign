package com.weining.android.db;

import java.util.ArrayList;
import java.util.List;
import org.litepal.crud.LitePalSupport;

public class DataAll extends LitePalSupport {

    private int dataId;
    private int dataType;
    private int dataSize;
    private int dataCircular;
    private int dataAxial;
    private int dataCavity;
    private int dataSpan;
    private int dataCount;
    private float dataVol;
    private int maxF;
    private List<Float> tlList = new ArrayList<Float>();

    //Getters and Setters below
    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public int getDataCircular() {
        return dataCircular;
    }

    public void setDataCircular(int dataCircular) {
        this.dataCircular = dataCircular;
    }

    public int getDataAxial() {
        return dataAxial;
    }

    public void setDataAxial(int dataAxial) {
        this.dataAxial = dataAxial;
    }

    public int getDataCavity() {
        return dataCavity;
    }

    public void setDataCavity(int dataCavity) {
        this.dataCavity = dataCavity;
    }

    public int getDataSpan() {
        return dataSpan;
    }

    public void setDataSpan(int dataSpan) {
        this.dataSpan = dataSpan;
    }

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public float getDataVol() {
        return dataVol;
    }

    public void setDataVol(float dataVol) {
        this.dataVol = dataVol;
    }

    public int getMaxF() {
        return maxF;
    }

    public void setMaxF(int maxF) {
        this.maxF = maxF;
    }

    public List<Float> getTlList() {
        return tlList;
    }

    public void setTlList(List<Float> tlList) {
        this.tlList = tlList;
    }

}
