package com.weining.android.db;

import org.litepal.crud.LitePalSupport;

public class Record extends LitePalSupport {
    private String account;
    private String password;
    private String IMEI;
    private int year;
    private int month;
    private int day;

    public String getAccount(){
        return account;
    }
    public void setAccount(String account){
        this.account=account;
    }

    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }

    public String getIMEI(){
        return IMEI;
    }
    public void setIMEI(String IMEI){
        this.IMEI=IMEI;
    }

    public int getYear(){
        return year;
    }
    public void setYear(int year){
        this.year=year;
    }

    public int getMonth(){
        return month;
    }
    public void setMonth(int month){
        this.month=month;
    }

    public int getDay(){
        return day;
    }
    public void setDay(int day){
        this.day=day;
    }
}
