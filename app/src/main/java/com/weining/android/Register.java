package com.weining.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.weining.android.db.DataAll;
import com.weining.android.db.DataStick;
import com.weining.android.db.Record;
import com.weining.android.util.HttpUtil;
import com.weining.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class Register extends AppCompatActivity {

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private CheckBox rememberPass;

    private EditText accountIn;

    private EditText passwordIn;

    private Button login;

    private Button showDeviceKey;

    private int currentYear;

    private int currentMonth;

    private int currentDay;

    private String currentDeviceKey;

    private String currentAccount;

    private String currentPassword;

    private List<Record> recordList;

    private Boolean certify;

    private Boolean hasUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_register);

        requestPermissions();

        initView();

        initPref();

        //获取系统时间
        Calendar calendar= Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH)+1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);


        List<DataAll>  myDataAll = LitePal.where("dataId = ?","1").find(DataAll.class);
        if(myDataAll.size() == 0) {
            requestData();
        }

        List<DataStick>  myDataStick = LitePal.where("dataId = ?","1").find(DataStick.class);
        if(myDataStick.size() == 0) {
            requestStick();
        }

    }

    private void requestPermissions(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_CALENDAR);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.INTERNET);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Register.this,permissions,1);
        }
    }

    private void initView(){
        accountIn = findViewById(R.id.account_in);
        passwordIn = findViewById(R.id.password_in);
        login = findViewById(R.id.login);
        rememberPass = findViewById(R.id.remember_password);
        showDeviceKey = findViewById(R.id.show_deviceKey);

        designInput();

        //设置登录按钮的按击事件
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentAccount = accountIn.getText().toString();
                currentPassword = passwordIn.getText().toString();
                if(currentAccount.equals("试用")){
                    betaRecord();
                }else{
                    if("".equals(currentAccount)){
                        Toast.makeText(getContext(),"请输入账号",Toast.LENGTH_SHORT).show();
                        accountIn.requestFocus();//获取光标位置
                    }else if("".equals(currentPassword)){
                        Toast.makeText(getContext(),"请输入密码",Toast.LENGTH_SHORT).show();
                        passwordIn.requestFocus();//获取光标位置
                    }else{
                        hasUpdate = false;
                        queryRecord();
                    }
                }
            }

        });

        showDeviceKey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("deviceKeyIs:",currentDeviceKey);
            }
        });
    }

    private void initPref() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //设置账号密码保存值
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            String storeAccount = pref.getString("account", "");
            String storePassword = pref.getString("password", "");
            accountIn.setText(storeAccount);
            passwordIn.setText(storePassword);
            rememberPass.setChecked(true);
        }
        currentDeviceKey = pref.getString("deviceKey", "");
        if ("".equals(currentDeviceKey)) {
            editor = pref.edit();
            currentDeviceKey = generateDeviceKey();
            editor.putString("deviceKey", currentDeviceKey);
            editor.apply();
        }
    }

    private void designInput(){
        //设置账号密码输入框的hint
        accountIn.setTag("账号");//预先设置Tag作为hint的值
        mySetHint(accountIn);
        passwordIn.setTag("密码");
        mySetHint(passwordIn);
        accountIn.setOnFocusChangeListener(onFocusChangeListener);
        passwordIn.setOnFocusChangeListener(onFocusChangeListener);
    }

    private void betaRecord(){
        editor = pref.edit();
        editor.putBoolean("isBeta",true);
        editor.putBoolean("remember_password",false);
        editor.apply();
        Intent intent = new Intent(Register.this,ChooseOne.class);
        startActivity(intent);
        finish();
    }

    private void queryRecord(){
        recordList = LitePal.findAll(Record.class);
        if(!hasUpdate && Build.VERSION.SDK_INT>=21){
            if(isNetSystemUsable(getContext())){
                hasUpdate = true;//这句话应该在前面的
                queryFromServer();
            }
        }
        if(recordList.size() > 0){
            certify = false;
            for(Record record:recordList){
                if(currentAccount.equals(record.getAccount()) && currentPassword.equals(record.getPassword()) && currentDeviceKey
                        .equals(record.getDeviceKey())){
                    int year,month,day;
                    year = record.getYear();
                    month = record.getMonth();
                    day = record.getDay();
                    if(currentYear < year || (currentYear == year && currentMonth < month) || (currentYear == year && currentMonth == month && currentDay <= day)){
                        editor = pref.edit();
                        if(rememberPass.isChecked()){
                            editor.putBoolean("remember_password",true);
                            editor.putString("account",currentAccount);
                            editor.putString("password",currentPassword);
                        }else{
                            editor.putBoolean("remember_password",false);
                        }
                        editor.apply();
                        certify = true;
                        registerSuccess();
                    }
                }
            }
            if (!certify){
                Snackbar.make(findViewById(R.id.register_content),"该账户没有权限，请重新输入",Snackbar.LENGTH_SHORT).show();
                passwordIn.setText("");
            }
        }else{
            queryFromServer();
        }
    }

    private void registerSuccess(){
        Intent intent = new Intent(Register.this,ChooseOne.class);
        startActivity(intent);
        finish();
    }

    private void queryFromServer(){
        String url = "https://www.weiningauto.xyz/myAccount1.json";//47.102.105.35
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                result = Utility.handleRecordResponse(responseText);
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(hasUpdate == false)queryRecord();//防止刷新2次
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "第一次使用需要连接网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantRseults){
        switch (requestCode){
            case 1:
                if (grantRseults.length>0){
                    for (int result : grantRseults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                 break;
        }
    }

    /**
     * 使用随机生成的十位字符串作为密钥
     * 无法使用手机deviceId, 老是会变
     * 无法获取IMEI号((International Mobile Equipment Identity,国际移动身份识别码)
     * 原先使用的IMEI码在Android 10已不允许使用
     */
    private String generateDeviceKey() {
        String originalStr = "00112233445566778899abcdefghijklmnopqrstuvwxyz";
        int length = originalStr.length();
        StringBuilder randomKey = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int rand = (int) (Math.random() * length);
            randomKey.append(originalStr.charAt(rand));
        }
        return randomKey.toString();
    }

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()){
                case R.id.account_in:
                    if(hasFocus){
                        accountIn.setHint("");
                    }else{
                        mySetHint(accountIn);
                    }
                    break;
                case R.id.password_in:
                    if(hasFocus){
                        passwordIn.setHint("");
                    }else{
                        mySetHint(passwordIn);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void mySetHint(EditText et){
        String hint=et.getTag().toString();
        SpannableString newHint = new SpannableString(hint);//定义hint的值
        AbsoluteSizeSpan newHintReal = new AbsoluteSizeSpan(19,true);//设置字体大小 true表示单位是sp
        newHint.setSpan(newHintReal, 0, newHint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        et.setHint(new SpannedString(newHint));
    }

    public static boolean isNetSystemUsable(Context context){
        boolean isNetUsable=false;
        ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager==null) return false;
        NetworkInfo networkInfo=manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    private void requestData(){
        String url = "https://www.weiningauto.xyz/android_connect_data/get_all_data.php";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "请等待下载数据...", Toast.LENGTH_SHORT).show();
            }
        });
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("requestData","has web");
                String responseText = response.body().string();
                Log.d("requestData",responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "数据下载成功，请等待导入...", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d("requestData",responseText);
                Utility.handleDataAllResponse(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "更新源数据失败 requestData", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestStick(){
        String url = "https://www.weiningauto.xyz/android_connect_dataStick/get_data_stick.php";
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("Stick","has web");
                String responseText = response.body().string();
                Log.d("Stick",responseText);
                Utility.handleDataStickResponse(responseText);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "更新源数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
