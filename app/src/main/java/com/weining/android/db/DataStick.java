package com.weining.android.db;

import java.util.ArrayList;
import java.util.List;
import org.litepal.crud.LitePalSupport;

public class DataStick extends LitePalSupport {

    private int dataId;
    private int dataType;
    private int dataSize;
    private int dataLength;
    private int dataCavity;
    private int maxF;
    private List<Float> tlList = new ArrayList<Float>();


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

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getDataCavity() {
        return dataCavity;
    }

    public void setDataCavity(int dataCavity) {
        this.dataCavity = dataCavity;
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
