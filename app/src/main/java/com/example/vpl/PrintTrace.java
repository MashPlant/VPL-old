package com.example.vpl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.SurfaceHolder.Callback;
import android.view.WindowManager;


/**
 * Created by MashPlant on 2016/4/5.
 */
public class PrintTrace extends SurfaceView implements Callback, Runnable {
    VPLFrame vplFrame = null;
    SurfaceHolder mSurfaceHolder = null;
    Canvas mCanvas = null;
    Thread thread;
    boolean mRunning = false;
    boolean mClear = false;
    double[][] savePos = null;
    private float ratio = 20;
    private int height = 1920, width = 1080;
    private double deltaX = 0, deltaY = 0;

    public PrintTrace(Context context, AttributeSet attrs) {
        super(context, attrs);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;

        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mCanvas = new Canvas();
    }


    private void drawTrace() {
        if (vplFrame != null && mCanvas != null && vplFrame.doDrawTrace && vplFrame.isActive()) {
            ratio = vplFrame.getRatio();
            deltaX = vplFrame.getDeltaX();
            deltaY = vplFrame.getDeltaY();
            for (int i = 0; i < vplFrame.getMovingObjList().size(); i++) {
                VPLFrame.MovingObj movingObj = vplFrame.getMovingObjList().get(i);
                if (savePos[i][0] != 0 && savePos[i][1] != 0) {
                    mCanvas.drawLine((float) savePos[i][0], (float) savePos[i][1], (float) (movingObj.x * ratio + width / 2 + deltaX), (float) (-movingObj.y * ratio + height / 2 + deltaY), movingObj.line);
                }
                savePos[i][0] = movingObj.x * ratio + width / 2 + deltaX;
                savePos[i][1] = -movingObj.y * ratio + height / 2 + deltaY;
            }
        }
    }

    public void clear() {
        mClear = true;
    }

    public void clear(int noUse) {
        for (int i = 0; i < 10; i++) {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
        mClear = false;
        for (int i = 0; i < savePos.length; i++) {
            savePos[i][0] = savePos[i][1] = 0;
        }
    }


    @Override
    public void run() {
        while (mRunning) {
            long startTime = System.currentTimeMillis();
            synchronized (mSurfaceHolder) {
                if (mClear) {
                    for (int i = 0; i < 10; i++) {
                        mCanvas = mSurfaceHolder.lockCanvas();
                        if (mCanvas != null) {
                            mCanvas.drawColor(Color.WHITE);
                            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                        }
                    }
                    mClear = false;

                } else {
                    mCanvas = mSurfaceHolder.lockCanvas();
                    if (mCanvas != null) {
                        drawTrace();
                        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            int diffTime = (int) (endTime - startTime);
            if (!vplFrame.isRunning())
                while (diffTime <= 35) {
                    diffTime = (int) (System.currentTimeMillis() - startTime);
                    Thread.yield();
                }
        }
    }

    public void setMonitor(VPLFrame arg_vplframe) {
        vplFrame = arg_vplframe;
        ratio = vplFrame.getRatio();
        deltaX = vplFrame.getDeltaX();
        deltaY = vplFrame.getDeltaY();
        List<VPLFrame.MovingObj> movingObjList = arg_vplframe.getMovingObjList();
        savePos = new double[50][2];
        for (int i = 0; i < movingObjList.size(); i++) {
            VPLFrame.MovingObj movingObj = movingObjList.get(i);
            savePos[i][0] = movingObj.x * ratio + width / 2 + deltaX;
            savePos[i][1] = -movingObj.y * ratio + height / 2 + deltaY;
        }
        mRunning = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mRunning = false;
    }

}