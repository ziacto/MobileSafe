package com.ztjc.mobilesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.ztjc.mobilesafe.R;
import com.ztjc.mobilesafe.util.StreamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Android手机卫士，闪屏页面
 */
public class SplashActivity extends Activity {

    private static final int CODE_UPDATE_DIALOG = 1;  // 更新对话框
    private static final int CODE_ENTER_HOME = 2;   // 进入主页面
    private static final int CODE_URL_ERROR = 3;   // url错误
    private static final int CODE_NET_ERROR = 4;   // 网络错误
    private static final int CODE_JSON_ERROR = 5;   // json解析错误
    private TextView tvVersion;
    private TextView tvProgress;

    private String mVersionName; // 版本名
    private int mVersionCode; // 版本号
    private String mDesc; // 版本描述
    private String mDownload; // 下载地址

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_UPDATE_DIALOG:
                    showUploadDialog();
                    break;
                case CODE_ENTER_HOME:
                    enterHome();
                    break;
                case CODE_URL_ERROR:
                    Toast.makeText(SplashActivity.this, "url错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_NET_ERROR:
                    Toast.makeText(SplashActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_JSON_ERROR:
                    Toast.makeText(SplashActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvProgress = (TextView) findViewById(R.id.tv_progress);

        tvVersion.setText("版本:" + getVersionName());

        checkVersion();
    }

    /**
     * 获取版本名称
     *
     * @return
     */
    public String getVersionName() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public int getVersioinCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 检测服务器版本
     */
    private void checkVersion() {
        final long startTime = System.currentTimeMillis();
        // 启动子线程异步加载数据
        new Thread() {
            @Override
            public void run() {
                Message message = Message.obtain();
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://192.168.1.105:8080/json/mobilesafe.json");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    if (connection.getResponseCode() == 200) {
                        // 请求成功 获取服务器返回的json
                        String result = StreamUtil.decodeToString(connection.getInputStream());
                        // 使用jsonObject解析json，json的解析有多种方式（fastjson、gson、jsonObject等，先实现功能）
                        JSONObject jsonObject = new JSONObject(result);
                        mVersionName = jsonObject.getString("versionName");
                        mDesc = jsonObject.getString("description");
                        mDownload = jsonObject.getString("downLoadUrl");
                        mVersionCode = jsonObject.getInt("versionCode");

                        // 判断服务器和本地版本
                        if (mVersionCode > getVersioinCode()) {
                            // 服务器版本大于本地版本，弹出更新提示
                            message.what = CODE_UPDATE_DIALOG;
                        } else {
                            // 没有版本更新，直接进入主页面
                            message.what = CODE_ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    // URL错误
                    message.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    // 网络错误
                    message.what = CODE_NET_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    // json解析失败
                    message.what = CODE_JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect(); // 关闭网络连接
                    }
                    long endTime = System.currentTimeMillis();
                    long useTime = endTime - startTime;   // 总共花费的时间
                    if (useTime < 2000) {
                        // 强制休眠一段时间，保证闪屏在2秒以上
                        try {
                            Thread.sleep(2000 - useTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(message);
                }
            }
        }.start();
    }

    /**
     * 升级对话框
     */
    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本:" + mVersionName);
        builder.setMessage(mDesc);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                donwload();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });
        // 设置取消监听，用户点击返回键时触发
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();
            }
        });
        builder.show();
    }

    /**
     * 下载
     */
    private void donwload() {
        // 判断sd卡是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            tvProgress.setVisibility(View.VISIBLE);
            String target = Environment.getExternalStorageDirectory() + "/update.apk";

            // Xutils下载
            HttpUtils utils = new HttpUtils();
            utils.download(mDownload, target, new RequestCallBack<File>() {

                // 下载文件进度
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);

                    tvProgress.setText("下载进度" + current * 100 / total + "%");
                }

                // 下载成功
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {

                    // 安装应用
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(responseInfo.result), "application/vnd.android.package-archive");
                    startActivityForResult(intent, 0); // 如果用户点击取消，会调用onActivityResult方法
                }

                // 下载失败
                @Override
                public void onFailure(HttpException error, String msg) {
                    Toast.makeText(SplashActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "没找到SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    // 如果用户取消安装进入主页面
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHome();
    }

    /**
     * 进入主页面
     */
    private void enterHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

}
