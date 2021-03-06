package com.weining.android;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
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

    private TextView useBeta;

    private TextView showDeviceKey;

    private int currentYear;

    private int currentMonth;

    private int currentDay;

    private String currentDeviceKey;

    private String currentAccount;

    private String currentPassword;

    private List<Record> recordList;

    private Boolean certify;

    private Boolean hasUpdate;

    private Boolean hasLogged;

    private ProgressDialog progressDialog;

    private int successCount;

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

        getSystemTime();
    }

    private void requestPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CALENDAR);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.INTERNET);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Register.this, permissions, 1);
        }
    }

    private void initView() {
        accountIn = findViewById(R.id.account_in);
        passwordIn = findViewById(R.id.password_in);
        login = findViewById(R.id.login);
        rememberPass = findViewById(R.id.remember_password);
        useBeta = findViewById(R.id.use_beta);
        showDeviceKey = findViewById(R.id.show_deviceKey);

        designInput();

        //设置登录按钮的按击事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAccount = accountIn.getText().toString();
                currentPassword = passwordIn.getText().toString();
                if ("".equals(currentAccount)) {
                    Toast.makeText(getContext(), "请输入账号", Toast.LENGTH_SHORT).show();
                    accountIn.requestFocus();//获取光标位置
                } else if ("".equals(currentPassword)) {
                    Toast.makeText(getContext(), "请输入密码", Toast.LENGTH_SHORT).show();
                    passwordIn.requestFocus();//获取光标位置
                } else {
                    hasUpdate = false;
                    queryRecord();
                }
            }

        });

        useBeta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("isBeta", true);
                editor.apply();
                registerSuccess();
            }
        });

        showDeviceKey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new Builder(Register.this);
                dialog.setTitle("许可")
                        .setMessage("您的密钥为：" + currentDeviceKey + "\n请向管理员申请权限")
                        .show();
            }
        });
    }

    private void initPref() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        //设置账号密码保存值
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            String storeAccount = pref.getString("account", "");
            String storePassword = pref.getString("password", "");
            accountIn.setText(storeAccount);
            passwordIn.setText(storePassword);
            rememberPass.setChecked(true);
        }

        //设置设备密钥
        currentDeviceKey = pref.getString("deviceKey", "");
        if ("".equals(currentDeviceKey)) {
            currentDeviceKey = generateDeviceKey();
            editor.putString("deviceKey", currentDeviceKey);
            editor.apply();
        }

        hasLoggedOn();
    }

    //设置是否曾登录的标志，若第一次登录需加载数据
    private void hasLoggedOn() {
        List<DataAll> myDataAll = LitePal.where("dataId = ?", "1").find(DataAll.class);
        List<DataStick> myDataStick = LitePal.where("dataId = ?", "1").find(DataStick.class);
        if (myDataAll.size() == 0 || myDataStick.size() == 0) {
            hasLogged = false;
        } else {
            hasLogged = true;
        }
    }

    //设置账号密码输入框的hint，其中Tag为hint的值
    private void designInput() {
        accountIn.setTag("账号");
        setHint(accountIn);
        passwordIn.setTag("密码");
        setHint(passwordIn);
        accountIn.setOnFocusChangeListener(onFocusChangeListener);
        passwordIn.setOnFocusChangeListener(onFocusChangeListener);
    }

    //获取系统时间
    private void getSystemTime(){
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    //比较输入的帐号信息和服务器端的帐号信息
    private void queryRecord() {
        recordList = LitePal.findAll(Record.class);
        if (!hasUpdate && Build.VERSION.SDK_INT >= 21) {
            if (isNetSystemUsable(getContext())) {
                hasUpdate = true;
                queryFromServer();
            }
        }
        if (recordList.size() > 0) {
            certify = false;
            for (Record record : recordList) {
                if (currentAccount.equals(record.getAccount()) && currentPassword.equals(record.getPassword())
                        && currentDeviceKey
                        .equals(record.getDeviceKey())) {
                    int year, month, day;
                    year = record.getYear();
                    month = record.getMonth();
                    day = record.getDay();
                    if (currentYear < year || (currentYear == year && currentMonth < month) || (currentYear == year
                            && currentMonth == month && currentDay <= day)) {
                        if (rememberPass.isChecked()) {
                            editor.putBoolean("remember_password", true);
                            editor.putString("account", currentAccount);
                            editor.putString("password", currentPassword);
                        } else {
                            editor.putBoolean("remember_password", false);
                        }
                        editor.putBoolean("isBeta", false);
                        editor.apply();
                        certify = true;

                        if (hasLogged) {
                            registerSuccess();
                        } else {
                            loadMufflerData();
                        }

                    }
                }
            }
            if (!certify) {
                Snackbar.make(findViewById(R.id.register_content), "该账户没有权限，请重新输入", Snackbar.LENGTH_SHORT).show();
                passwordIn.setText("");
            }
        } else {
            queryFromServer();
        }
    }

    //登录成功，进入功能选择界面
    private void registerSuccess() {
        Intent intent = new Intent(Register.this, ChooseOne.class);
        startActivity(intent);
        finish();
    }

    //加载动画
    private void loadMufflerData() {
        progressDialog = new ProgressDialog(Register.this);
        progressDialog.setTitle("正在导入消声器数据···");
        progressDialog.setMessage("请稍等5-8分钟");
        progressDialog.setCancelable(false);
        progressDialog.show();
        successCount = 0;
        requestData();
        requestStick();
    }

    //数据导入成功，回调登录成功方法
    private void loadMufflerDataSuccess() {
        if(successCount == 2){
            progressDialog.dismiss();
            registerSuccess();
        }
    }

    //从服务器查询帐号密码
    private void queryFromServer() {
        String url = ServerInfo.accountUrl;
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean handleSuccess = Utility.handleRecordResponse(responseText);
                if (handleSuccess) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (hasUpdate == false) {
                                queryRecord();//防止刷新2次
                            }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantRseults) {
        switch (requestCode) {
            case 1:
                if (grantRseults.length > 0) {
                    for (int result : grantRseults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
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

    //UI相关，输入框的聚焦事件
    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.account_in:
                    if (hasFocus) {
                        accountIn.setHint("");
                    } else {
                        setHint(accountIn);
                    }
                    break;
                case R.id.password_in:
                    if (hasFocus) {
                        passwordIn.setHint("");
                    } else {
                        setHint(passwordIn);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //设置hint的格式
    private void setHint(EditText et) {
        String hint = et.getTag().toString();
        //定义hint的值
        SpannableString newHint = new SpannableString(hint);
        //设置字体大小 true表示单位是sp
        AbsoluteSizeSpan newHintReal = new AbsoluteSizeSpan(19, true);
        newHint.setSpan(newHintReal, 0, newHint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        et.setHint(new SpannedString(newHint));
    }

    //确认是否联网状态
    public static boolean isNetSystemUsable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    //请求穿孔消声器数据
    private void requestData() {
        String url = ServerInfo.allDataUrl;
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Log.d("requestData", responseText);
                if(Utility.handleDataAllResponse(responseText)){
                    successCount += 1;
                    loadMufflerDataSuccess();
                }
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

    //请求插入管消声器数据
    private void requestStick() {
        String url = ServerInfo.stickDataUrl;
        HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //Log.d("Stick", responseText);
                if(Utility.handleDataStickResponse(responseText)){
                    successCount += 1;
                    loadMufflerDataSuccess();
                }
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
