package com.example.vpl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;

/**
 * Created by MashPlant on 2016/4/4.
 */
public class MainActivity extends AppCompatActivity {
    static String TAG="my";
    static VPLFrame vplFrame = null;
    static PrintTrace printTrace = null;
    static PrintFrame printFrame = null;
    static FloatingActionButton pause = null, play = null, again = null, add = null, edit = null, delete = null, ensure_choose = null;
    static SeekBar speed=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        createWindowManager();
        createDesktopLayout();
        //showDesk();

        if (android.os.Build.VERSION.RELEASE.charAt(0) < '5') {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("你的Android设备很害怕");
            builder.setMessage("需要重新启动,我们也不确定有没有用");
            builder.setPositiveButton("滚到更高的系统", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }

        vplFrame = (VPLFrame) findViewById(R.id.main_frame);
        printFrame = (PrintFrame) findViewById(R.id.frame);
        printTrace = (PrintTrace) findViewById(R.id.trace);

        printFrame.setMonitor(vplFrame);
        printTrace.setMonitor(vplFrame);
        vplFrame.setMonitor(printFrame);
        vplFrame.setMonitor(printTrace);
        vplFrame.initializeSensor((SensorManager) getSystemService(SENSOR_SERVICE));

        pause = (FloatingActionButton) findViewById(R.id.pause);
        play = (FloatingActionButton) findViewById(R.id.play);
        again = (FloatingActionButton) findViewById(R.id.again);
        add = (FloatingActionButton) findViewById(R.id.add);
        delete = (FloatingActionButton) findViewById(R.id.delete);
        edit = (FloatingActionButton) findViewById(R.id.edit);
        ensure_choose = (FloatingActionButton) findViewById(R.id.ensure_choose);
        speed=(SeekBar) findViewById(R.id.speed);
        pause.setVisibility(View.VISIBLE);
        play.setVisibility(View.VISIBLE);
        again.setVisibility(View.VISIBLE);
        add.setVisibility(View.VISIBLE);
        speed.setProgress(50);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vplFrame.timeGap = 0.04 * Math.exp(progress / 10.0 - 5.0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (pause != null)
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vplFrame.pause();
                    removeAllFab();
                    if (vplFrame.isRunning()) {
                        play.setVisibility(View.VISIBLE);
                        again.setVisibility(View.VISIBLE);
                    } else {
                        play.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);
                    }
                }
            });
        if (play != null)
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAllFab();
                    again.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.VISIBLE);
                    vplFrame.start();
                    handler.removeMessages(0);
                }
            });
        if (again != null)
            again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAllFab();
                    play.setVisibility(View.VISIBLE);
                    add.setVisibility(View.VISIBLE);
                    vplFrame.end();
                    vplFrame.getSaveInstance();
                    printTrace.clear(1);
                }
            });
        if (add != null)
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ChooseObjActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        if (edit != null) {
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddObjActivity.class);
                    AddObjActivity.kind = vplFrame.selectedKind;
                    startActivity(intent);
                    vplFrame.notifyChange();
                    removeAllFab();
                    if (vplFrame.isRunning()) {
                        pause.setVisibility(View.VISIBLE);
                        again.setVisibility(View.VISIBLE);
                    } else {
                        play.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        if (delete != null) {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vplFrame.deleteSelected();
                    vplFrame.notifyChange();
                    removeAllFab();
                    if (vplFrame.isRunning()) {
                        pause.setVisibility(View.VISIBLE);
                        again.setVisibility(View.VISIBLE);
                    } else {
                        play.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
        if (ensure_choose != null) {
            ensure_choose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAllFab();
                    add.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(MainActivity.this, AddObjActivity.class);
                    startActivity(intent);

                }
            });
        }

        SharedPreferences datas = getSharedPreferences("datas", Activity.MODE_PRIVATE);
        vplFrame.G = Double.valueOf(datas.getString("G", "6.67E-11"));
        vplFrame.K = Double.valueOf(datas.getString("K", "9E9"));
        vplFrame.g = Double.valueOf(datas.getString("g", "0"));
        vplFrame.c = Double.valueOf(datas.getString("c", "3E8"));
        vplFrame.timeGap = Double.valueOf(datas.getString("timeGap", "0.04"));
        vplFrame.doBetG = Boolean.valueOf(datas.getString("doBetG", "false"));
        vplFrame.doBetE = Boolean.valueOf(datas.getString("doBetE", "false"));
        vplFrame.doImpact = Boolean.valueOf(datas.getString("doImpact", "true"));
        vplFrame.hasBoundary = Boolean.valueOf(datas.getString("hasBoundary", "true"));
        vplFrame.strongInteraction=Boolean.valueOf(datas.getString("strongInteraction", "false"));
        vplFrame.doRelativity = Boolean.valueOf(datas.getString("doRelativity", "false"));
        vplFrame.doImpactSound = Boolean.valueOf(datas.getString("doImpactSound", "true"));
        vplFrame.doDrawTrace = Boolean.valueOf(datas.getString("doDrawTrace", "true"));
        vplFrame.doDynamicOri = Boolean.valueOf(datas.getString("doDynamicOri", "false"));
        vplFrame.doMultiThread=Boolean.valueOf(datas.getString("doMultiThread", "false"));
    }

    public static void removeAllFab() {
        play.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        again.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        ensure_choose.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            vplFrame.setTracingState();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        vplFrame.pause();
        pause.setVisibility(View.GONE);
        play.setVisibility(View.VISIBLE);
        //saveData();
        //write("ceshi");
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    printTrace.clear();
                    sendEmptyMessage(0);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        vplFrame.notifyChange();
        handler.sendEmptyMessage(0);
        vplFrame.selectedKind = -1;
        vplFrame.selectedPos = 0;
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private SuspendingWindow mWatcher;
    private long startTime;
    // 声明屏幕的宽高
    float x, y;
    int top;

    private void createDesktopLayout() {
        mWatcher = new SuspendingWindow(this);
        mWatcher.setOnTouchListener(new View.OnTouchListener() {
            float mTouchStartX;
            float mTouchStartY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.getRawX()+50;
                y = event.getRawY() - top-50; // 25是系统状态栏的高度
                Log.i("startP", "startX" + mTouchStartX + "====startY"
                        + mTouchStartY);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取相对View的坐标，即以此View左上角为原点
                        Log.d(TAG, "touching");
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        Log.i("startP", "startX" + mTouchStartX + "====startY"
                                + mTouchStartY);
                        long end = System.currentTimeMillis() - startTime;
                        // 双击的间隔在 300ms以下
                        if (end < 300) {
                            closeDesk();
                        }
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 更新浮动窗口位置参数
                        mLayout.x = (int) (x - mTouchStartX);
                        mLayout.y = (int) (y - mTouchStartY);
                        mWindowManager.updateViewLayout(v, mLayout);
                        break;
                    case MotionEvent.ACTION_UP:

                        // 更新浮动窗口位置参数
                        mLayout.x = (int) (x - mTouchStartX);
                        mLayout.y = (int) (y - mTouchStartY);
                        mWindowManager.updateViewLayout(v, mLayout);

                        // 可以在此记录最后一次的位置

                        mTouchStartX = mTouchStartY = 0;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Rect rect = new Rect();
        // /取得整个视图部分,注意，如果你要设置标题样式，这个必须出现在标题样式之后，否则会出错
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        top = rect.top;//状态栏的高度，所以rect.height,rect.width分别是系统的高度的宽度

        Log.i("top", "" + top);
    }

    /**
     * 显示DesktopLayout
     */
    private void showDesk() {
        mWindowManager.addView(mWatcher, mLayout);
    }

    /**
     * 关闭DesktopLayout
     */
    private void closeDesk() {
        mWindowManager.removeView(mWatcher);
        finish();
    }

    /**
     * 设置WindowManager
     */
    private void createWindowManager() {
        // 取得系统窗体
        mWindowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        // 窗体的布局样式
        mLayout = new WindowManager.LayoutParams();

        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        // 设置窗体焦点及触摸：
        // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
        mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置显示的模式
        mLayout.format = PixelFormat.RGBA_8888;

        // 设置对齐的方法
        mLayout.gravity = Gravity.TOP | Gravity.LEFT;

        // 设置窗体宽度和高度
        mLayout.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;

    }

}
