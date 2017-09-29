package com.example.vpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.os.Message;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by MashPlant on 2016/4/4.
 */
public class VPLFrame extends View implements SensorEventListener {
    private Context mContext;
    private static String TAG = "my";
    private ArrayList<MovingObj> movingObjList = new ArrayList<>();
    private ArrayList<ForceCenter> forceCenterList = new ArrayList<>();
    private ArrayList<Field> fieldList = new ArrayList<>();
    private ArrayList<Connecter> connecterList = new ArrayList<>();
    private ArrayList<Track> trackList = new ArrayList<>();
    private ArrayList<ParticleGenerator> generatorList = new ArrayList<>();
    float ratio = 20;
    private int height, width;
    private double deltaX = 0, deltaY = 0;
    private boolean mRunning = false;
    double timeGap = 0.04;
    double G = 6.67E-11, K = 9E9, g = 0, c = 3E8;
    private double gx = 0, gy = 0;
    boolean doImpact = true, hasBoundary = true, doBetG = false, doBetE = false, doRelativity = false;
    boolean doImpactSound = true, hasImpactSound = false, doDrawTrace = true, doDynamicOri = false;
    boolean doMultiThread = false;
    boolean strongInteraction = false;
    private PrintFrame printFrame;
    private PrintTrace printTrace;
    private SaveInstance saveInstance;
    SoundPool soundPool = new SoundPool(5, 0, 5);
    private HashMap<Integer, Integer> soundId = new HashMap<>();
    static final int MOVINGOBJ = 0;
    static final int G_CENTER = 1;
    static final int E_CENTER = 2;
    static final int E_FIELD = 3;
    static final int M_FIELD = 4;
    static final int CIRCLE_M_FIELD = 5;
    static final int DAMP = 6;
    static final int ROPE = 7;
    static final int POLE = 8;
    static final int SPRING = 9;
    static final int LINE_TRACK = 10;
    static final int CIRCLE_TRACK = 11;
    static final int PARTICLE_GENERATOR = 12;
    static final int SETTING = 100;
    static final int GENERATOR_ANGLE = 2;
    static final int GENERATOR_TIME = 1;

    public void setSaveInstance() {
        this.saveInstance.movingObjList.clear();
        this.saveInstance.fieldList.clear();
        this.saveInstance.connecterList.clear();
        this.saveInstance.forceCenterList.clear();
        this.saveInstance.trackList.clear();
        this.saveInstance.generatorList.clear();
        Iterator localIterator1 = this.movingObjList.iterator();
        Iterator localIterator2 = this.fieldList.iterator();
        Iterator localIterator3 = this.connecterList.iterator();
        Iterator localIterator4 = this.forceCenterList.iterator();
        Iterator localIterator5 = this.trackList.iterator();
        Iterator localIterator6 = this.generatorList.iterator();
        while (localIterator1.hasNext()) {
            this.saveInstance.movingObjList.add(((MovingObj) localIterator1.next()).clone());
        }
        while (localIterator2.hasNext()) {
            this.saveInstance.fieldList.add(((Field) localIterator2.next()).clone());
        }
        while (localIterator3.hasNext()) {
            this.saveInstance.connecterList.add(((Connecter) localIterator3.next()).clone());
        }
        while (localIterator4.hasNext()) {
            this.saveInstance.forceCenterList.add(((ForceCenter) localIterator4.next()).clone());
        }
        while (localIterator5.hasNext()) {
            this.saveInstance.trackList.add(((Track) localIterator5.next()).clone());
        }
        while (localIterator6.hasNext()) {
            this.saveInstance.generatorList.add(((ParticleGenerator) localIterator6.next()).clone());
        }
    }

    public void getSaveInstance() {
        this.movingObjList.clear();
        this.fieldList.clear();
        this.connecterList.clear();
        this.forceCenterList.clear();
        this.trackList.clear();
        this.generatorList.clear();
        Iterator localIterator1 = this.saveInstance.movingObjList.iterator();
        Iterator localIterator2 = this.saveInstance.fieldList.iterator();
        Iterator localIterator3 = this.saveInstance.connecterList.iterator();
        Iterator localIterator4 = this.saveInstance.forceCenterList.iterator();
        Iterator localIterator5 = this.saveInstance.trackList.iterator();
        Iterator localIterator6 = this.saveInstance.generatorList.iterator();
        while (localIterator1.hasNext()) {
            this.movingObjList.add(((MovingObj) localIterator1.next()).clone());
        }
        while (localIterator2.hasNext()) {
            this.fieldList.add(((Field) localIterator2.next()).clone());
        }
        while (localIterator3.hasNext()) {
            this.connecterList.add(((Connecter) localIterator3.next()).clone());
        }
        while (localIterator4.hasNext()) {
            this.forceCenterList.add(((ForceCenter) localIterator4.next()).clone());
        }
        while (localIterator5.hasNext()) {
            this.trackList.add(((Track) localIterator5.next()).clone());
        }
        while (localIterator6.hasNext()) {
            this.generatorList.add(((ParticleGenerator) localIterator6.next()).clone());
        }
    }

    public void notifyChange() {
        printFrame.invalidate();
        printTrace.setMonitor(this);
        if (!mRunning)
            setSaveInstance();
    }

    public void addConnecter(Connecter connecter) {
        if (connecter.kind != SPRING || connecter.length == 0) {
            connecter.length = getDistance(movingObjList.get(connecter.obj1), movingObjList.get(connecter.obj2));
        }
        connecterList.add(connecter);
    }

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isActive() {
        return handler.hasMessages(0);
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void setMonitor(PrintFrame arg) {
        printFrame = arg;
    }

    public void setMonitor(PrintTrace arg) {
        printTrace = arg;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float arg) {
        ratio = arg;
    }

    public void addMovingObj(MovingObj movingObj) {
        movingObjList.add(movingObj);
    }

    public void addField(Field field) {
        fieldList.add(field);
    }

    public void addTrack(Track track) {
        trackList.add(track);
    }

    public ArrayList<Track> getTrackList() {
        return trackList;
    }

    public ArrayList<Connecter> getConnecterList() {
        return connecterList;
    }

    public ArrayList<ParticleGenerator> getGeneratorList() {return generatorList;}

    public ArrayList<Field> getFieldList() {
        return fieldList;
    }

    public ArrayList<MovingObj> getMovingObjList() {
        return movingObjList;
    }

    public ArrayList<ForceCenter> getForceCenterList() {
        return forceCenterList;
    }

    public void addForceCenter(ForceCenter forceCenter) {
        forceCenterList.add(forceCenter);
    }

    public void initializeSensor(SensorManager arg) {
        sensorManager = arg;
        gSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, gSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (doDynamicOri) {
            gx = -g * event.values[0] / SensorManager.STANDARD_GRAVITY;
            gy = g * event.values[1] / SensorManager.STANDARD_GRAVITY;
            try {
                if (!isActive())
                    invalidate();
            } catch (Exception e) {

            }
        } else {
            gy = g;
        }
    }

    public VPLFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
        saveInstance = new SaveInstance();
        soundId.put(1, soundPool.load(context, R.raw.sound_impact1, 1));
    }

    private void drawSpring(int index, Canvas canvas, Paint paint) {
        Connecter connecter = connecterList.get(index);
        MovingObj obj1 = movingObjList.get(connecter.obj1);
        MovingObj obj2 = movingObjList.get(connecter.obj2);
        double x1 = toRawX(obj1.x);
        double y1 = toRawY(obj1.y);
        double x2 = toRawX(obj2.x);
        double y2 = toRawY(obj2.y);
        double r = Math.min(obj1.radius * ratio, obj2.radius * ratio);
        MathVector line = new MathVector((x2 - x1) / 10, (y2 - y1) / 10);
        MathVector right = MathVector.formVectorAs(2 * r, new MathVector(-line.y, line.x));
        line.add(right);
        MathVector lineUp = new MathVector(line);
        line.substract(new MathVector(2 * right.x, 2 * right.y));
        MathVector lineDown = new MathVector(line);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineUp.x / 2), (float) (y1 += lineUp.y / 2), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineDown.x), (float) (y1 += lineDown.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineUp.x), (float) (y1 += lineUp.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineDown.x), (float) (y1 += lineDown.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineUp.x), (float) (y1 += lineUp.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineDown.x), (float) (y1 += lineDown.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineUp.x), (float) (y1 += lineUp.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineDown.x), (float) (y1 += lineDown.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineUp.x), (float) (y1 += lineUp.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 += lineDown.x), (float) (y1 += lineDown.y), paint);
        canvas.drawLine((float) x1, (float) y1, (float) (x1 + lineUp.x / 2), (float) (y1 + lineUp.y / 2), paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isTracing) {
            canvas.drawLine(0, toRawY(traceY), width, toRawY(traceY), PrintFrame.greenLine);
            canvas.drawLine(toRawX(traceX), 0, toRawX(traceX), height, PrintFrame.greenLine);
            canvas.drawCircle(toRawX(traceX), toRawY(traceY), 10, PrintFrame.redLine);
            canvas.drawText("(" + String.format("%.2f", traceX) + "," + String.format("%.2f", traceY) + ")", 50, 100, PrintFrame.wordPainter);
        }
        if (g != 0) {
            drawAL(width / 2, height - 80, width / 2 + 80 * gx / g, height - 80 + 80 * gy / g, true, canvas, PrintFrame.redLine);
            canvas.drawText("g", width / 2 + 20, height - 40, PrintFrame.wordPainter);
        }
        canvas.drawText(String.format("%.1f", timeCounter * timeGap / 1000), 50, 50, PrintFrame.wordPainter);
        for (int i = 0; i < connecterList.size(); i++) {
            Connecter connecter = connecterList.get(i);
            MovingObj obj1 = movingObjList.get(connecter.obj1);
            MovingObj obj2 = movingObjList.get(connecter.obj2);
            switch (connecter.kind) {
                case ROPE:
                    drawLine(obj1.x, obj1.y, obj2.x, obj2.y, canvas, PrintFrame.linePainter);
                    break;
                case POLE:
                    drawLine(obj1.x, obj1.y, obj2.x, obj2.y, canvas, PrintFrame.mainLine);
                    break;
                case SPRING: {
                    drawSpring(i, canvas, PrintFrame.linePainter);
                    break;
                }
            }
        }
        for (int i = 0; i < movingObjList.size(); i++) {
            MovingObj movingObj = movingObjList.get(i);
            if (movingObj.bitmap != null) {
                canvas.drawBitmap(movingObj.bitmap, toRawX(movingObj.x) - movingObj.bitmap.getHeight() / 2, toRawY(movingObj.y) - movingObj.bitmap.getHeight() / 2, movingObj.paint);
            } else {
                drawCircle(movingObj.x, movingObj.y, movingObj.radius, canvas, movingObj.paint);
            }
        }
        for (int i = 0; i < generatorList.size(); i++) {
            MovingObj movingObj = generatorList.get(i).movingObj;
            drawCircle(movingObj.x, movingObj.y, movingObj.radius, canvas, PrintFrame.greenLine);
        }
        switch (selectedKind) {
            case -1:
                break;
            case MOVINGOBJ: {
                MovingObj movingObj = movingObjList.get(selectedPos);
                drawCircle(movingObj.x, movingObj.y, movingObj.radius, canvas, PrintFrame.redLine);
                break;
            }
            case E_CENTER:
            case G_CENTER: {
                ForceCenter forceCenter = forceCenterList.get(selectedPos);
                drawCircle(forceCenter.x, forceCenter.y, 2, canvas, PrintFrame.redLine);
                break;
            }
            case M_FIELD:
            case E_FIELD:
            case DAMP: {
                Field field = fieldList.get(selectedPos);
                double x1 = toRawX(field.x1);
                double x2 = toRawX(field.x2);
                if (x1 > x2) {
                    double temp = x1;
                    x1 = x2;
                    x2 = temp;
                }
                double y1 = toRawY(field.y1);
                double y2 = toRawY(field.y2);
                if (y1 > y2) {
                    double temp = y1;
                    y1 = y2;
                    y2 = temp;
                }
                canvas.drawRect((float) x1, (float) y1, (float) x2, (float) y2, PrintFrame.redLine);
                drawAL(x2, y2, x2 + 2 * ratio, y2 + 2 * ratio, true, canvas, PrintFrame.redLine);
                drawAL(x2, y1, x2 + 2 * ratio, y1 - 2 * ratio, true, canvas, PrintFrame.redLine);
                drawAL(x1, y2, x1 - 2 * ratio, y2 + 2 * ratio, true, canvas, PrintFrame.redLine);
                drawAL(x1, y1, x1 - 2 * ratio, y1 - 2 * ratio, true, canvas, PrintFrame.redLine);
                break;
            }
            case CIRCLE_M_FIELD: {
                Field field = fieldList.get(selectedPos);
                double x1 = toRawX(field.x1);
                double y1 = toRawY(field.y1);
                double r = field.x2 * ratio;
                canvas.drawCircle((float) x1, (float) y1, (float) r, PrintFrame.redLine);
                break;
            }
            case ROPE:
            case POLE: {
                Connecter connecter = connecterList.get(selectedPos);
                MovingObj obj1 = movingObjList.get(connecter.obj1);
                MovingObj obj2 = movingObjList.get(connecter.obj2);
                drawLine(obj1.x, obj1.y, obj2.x, obj2.y, canvas, PrintFrame.redLine);
                break;
            }
            case SPRING: {
                drawSpring(selectedPos, canvas, PrintFrame.redLine);
                break;
            }
            case LINE_TRACK: {
                Track track = trackList.get(selectedPos);
                drawLine(track.x1, track.y1, track.x2, track.y2, canvas, PrintFrame.redLine);
                break;
            }
            case CIRCLE_TRACK: {
                Track track = trackList.get(selectedPos);
                double x1 = toRawX(track.x1);
                double y1 = toRawY(track.y1);
                double r = track.radius * ratio;
                RectF rectF = new RectF((float) (x1 - r), (float) (y1 - r), (float) (x1 + r), (float) (y1 + r));
                canvas.drawArc(rectF, -(float) track.start, -(float) (track.end - track.start), false, PrintFrame.redLine);
                double start = track.start * Math.PI / 180;
                double end = track.end * Math.PI / 180;
                MathVector startTan = MathVector.formVectorAs(-4, -Math.sin(start), Math.cos(start));
                MathVector endTan = MathVector.formVectorAs(4, -Math.sin(end), Math.cos(end));
                drawAL(track.x1 + track.radius * Math.cos(start), track.y1 + track.radius * Math.sin(start),
                        track.x1 + track.radius * Math.cos(start) + startTan.x, track.y1 + track.radius * Math.sin(start) + startTan.y, false, canvas, PrintFrame.normalLine);
                drawAL(track.x1 + track.radius * Math.cos(end), track.y1 + track.radius * Math.sin(end),
                        track.x1 + track.radius * Math.cos(end) + endTan.x, track.y1 + track.radius * Math.sin(end) + endTan.y, false, canvas, PrintFrame.normalLine);
                break;
            }
            case PARTICLE_GENERATOR: {
                MovingObj movingObj = generatorList.get(selectedPos).movingObj;
                drawCircle(movingObj.x, movingObj.y, movingObj.radius, canvas, PrintFrame.redLine);
                break;
            }
        }
        if (isChoosingConnectObj) {
            MovingObj obj1 = movingObjList.get(chooseObj1);
            MovingObj obj2 = movingObjList.get(chooseObj2);
            drawCircle(obj1.x, obj1.y, obj1.radius, canvas, PrintFrame.redLine);
            drawCircle(obj2.x, obj2.y, obj2.radius, canvas, PrintFrame.redLine);
            if (choosingX != 0 && choosingY != 0) {
                drawLine(obj1.x, obj1.y, toFrameX(choosingX), toFrameY(choosingY), canvas, PrintFrame.redLine);
            }
        }
        if (isChoosingGenerateObj){
            MovingObj obj1 = movingObjList.get(chooseObj1);
            drawCircle(obj1.x, obj1.y, obj1.radius, canvas, PrintFrame.redLine);
        }

    }

    private void drawLine(double x1, double y1, double x2, double y2, Canvas canvas, Paint paint) {
        x1 = toRawX(x1);
        x2 = toRawX(x2);
        y1 = toRawY(y1);
        y2 = toRawY(y2);
        canvas.drawLine((float) x1, (float) y1, (float) x2, (float) y2, paint);
    }

    private void drawCircle(double x, double y, double r, Canvas canvas, Paint paint) {
        x = toRawX(x);
        y = toRawY(y);
        canvas.drawCircle((float) x, (float) y, (float) r * ratio, paint);
    }


    public void deleteSelected() {
        switch (selectedKind) {
            case -1:
                break;
            case MOVINGOBJ:
                movingObjList.remove(selectedPos);
                for (int i = 0; i < connecterList.size(); i++) {
                    Connecter connecter = connecterList.get(i);
                    if (connecter.obj1 == selectedPos || connecter.obj2 == selectedPos) {
                        connecterList.remove(i--);
                        continue;
                    }
                    if (connecter.obj1 > selectedPos)
                        connecter.obj1--;
                    if (connecter.obj2 > selectedPos)
                        connecter.obj2--;
                }
                break;
            case M_FIELD:
            case E_FIELD:
            case DAMP:
            case CIRCLE_M_FIELD:
                fieldList.remove(selectedPos);
                break;
            case G_CENTER:
            case E_CENTER:
                forceCenterList.remove(selectedPos);
                break;
            case SPRING:
            case POLE:
            case ROPE:
                connecterList.remove(selectedPos);
                break;
            case LINE_TRACK:
            case CIRCLE_TRACK:
                trackList.remove(selectedPos);
                break;
            case PARTICLE_GENERATOR:
                generatorList.remove(selectedPos);
                break;
        }
        selectedKind = -1;
        selectedPos = 0;
        invalidate();
        printFrame.invalidate();
    }

    private double startX = 0, startY = 0, oldDist = 1, oldX = 0, oldY = 0;
    private boolean doMove = true;
    private int numPoint = 0, judgeClick = 0, stretchOri = 0;
    public int selectedKind = -1, selectedPos = 0;
    public int chooseObj1 = 0, chooseObj2 = 1;
    public boolean isChoosingConnectObj = false;
    public boolean isChoosingGenerateObj = false;
    private float choosingX = 0, choosingY = 0;
    private int endOfTrack = 0;
    private double downX = 0, downY = 0;
    private long clickStartTime = 0;
    private MovingObj copyMovingObj = null;
    private boolean copyFinish = false;
    private int clickTimes = 0;
    private long doubleClickStartTime = 0;

    private long deltaT = 0l, t0 = 0l;
    private int frameVIndex = 0;
    private float[] frameVx = new float[6], frameVy = new float[6];
    private boolean isSlowingDown = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        deltaT = System.currentTimeMillis() - t0;
        t0 = System.currentTimeMillis();
        isSlowingDown = false;
        if (isTracing) {
            traceX = toFrameX(event.getRawX());
            traceY = toFrameY(event.getRawY());
            invalidate();
        } else {
            if (!isChoosingConnectObj&&!isChoosingGenerateObj) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        double x = toFrameX(event.getRawX());
                        double y = toFrameY(event.getRawY());
                        downX = event.getRawX();
                        downY = event.getRawY();
                        for (int i = 0; i < movingObjList.size(); i++) {
                            MovingObj movingObj = movingObjList.get(i);
                            if (Math.sqrt(Math.pow(movingObj.x - x, 2) + Math.pow(movingObj.y - y, 2)) <= movingObj.radius + 1) {
                                copyMovingObj = movingObjList.get(i);
                            }
                        }
                        clickStartTime = System.currentTimeMillis();
                        handler.sendEmptyMessage(1);
                    }
                    doMove = true;
                    numPoint = 1;
                    judgeClick = 0;
                    startX = event.getRawX();
                    startY = event.getRawY();
                    if (selectedKind == M_FIELD || selectedKind == E_FIELD || selectedKind == DAMP) {
                        Field field = fieldList.get(selectedPos);
                        double x1 = toRawX(field.x1);
                        double x2 = toRawX(field.x2);
                        if (x1 > x2) {
                            double temp = x1;
                            x1 = x2;
                            x2 = temp;
                        }
                        double y1 = toRawY(field.y1);
                        double y2 = toRawY(field.y2);
                        if (y1 > y2) {
                            double temp = y1;
                            y1 = y2;
                            y2 = temp;
                        }
                        stretchOri = 0;
                    /*
                    1 2
                    3 4
                     */
                        if (Math.pow(startX - x1, 2) + Math.pow(startY - y1, 2) <= 10 * ratio * ratio)
                            stretchOri = 1;
                        if (Math.pow(startX - x2, 2) + Math.pow(startY - y1, 2) <= 10 * ratio * ratio)
                            stretchOri = 2;
                        if (Math.pow(startX - x1, 2) + Math.pow(startY - y2, 2) <= 10 * ratio * ratio)
                            stretchOri = 3;
                        if (Math.pow(startX - x2, 2) + Math.pow(startY - y2, 2) <= 10 * ratio * ratio)
                            stretchOri = 4;
                    }
                    if (selectedKind == CIRCLE_TRACK || selectedKind == LINE_TRACK) {
                        Track track = trackList.get(selectedPos);
                        double x1 = track.x1, y1 = track.y1, x2, y2;
                        if (selectedKind == CIRCLE_TRACK) {
                            if (Math.sqrt(Math.pow(toRawX(x1 + Math.cos(track.start * Math.PI / 180) * track.radius) - startX, 2) + Math.pow(toRawY(y1 + Math.sin(track.start * Math.PI / 180) * track.radius) - startY, 2)) <= 3 * ratio)
                                endOfTrack = 1;
                            if (Math.sqrt(Math.pow(toRawX(x1 + Math.cos(track.end * Math.PI / 180) * track.radius) - startX, 2) + Math.pow(toRawY(y1 + Math.sin(track.end * Math.PI / 180) * track.radius) - startY, 2)) <= 3 * ratio)
                                endOfTrack = 2;
                        } else {
                            x2 = track.x2;
                            y2 = track.y2;
                            if (Math.sqrt(Math.pow(toRawX(x1) - startX, 2) + Math.pow(toRawY(y1) - startY, 2)) <= 3 * ratio)
                                endOfTrack = 1;
                            if (Math.sqrt(Math.pow(toRawX(x2) - startX, 2) + Math.pow(toRawY(y2) - startY, 2)) <= 3 * ratio)
                                endOfTrack = 2;

                        }
                    }

                    break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        judgeClick = 10;
                        oldDist = spacing(event);
                        oldX = event.getX(0);
                        oldY = event.getY(0);
                        numPoint++;
                        doMove = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.pow(event.getRawX() - downX, 2) + Math.pow(event.getRawY() - downY, 2) >= 100) {
                            handler.removeMessages(1);
                            if (!copyFinish) {
                                copyMovingObj = null;
                            }
                        }
                        double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
                        if (stretchOri != 0) {
                            Field field = fieldList.get(selectedPos);
                            x1 = toRawX(field.x1);
                            x2 = toRawX(field.x2);
                            if (x1 > x2) {
                                double temp = x1;
                                x1 = x2;
                                x2 = temp;
                            }
                            y1 = toRawY(field.y1);
                            y2 = toRawY(field.y2);
                            if (y1 > y2) {
                                double temp = y1;
                                y1 = y2;
                                y2 = temp;
                            }
                        }
                        switch (stretchOri) {
                            case 0:
                                if (doMove) {
                                    judgeClick++;
                                    switch (selectedKind) {
                                        case -1:
                                            deltaX += event.getRawX() - startX;
                                            deltaY += event.getRawY() - startY;
                                            frameVx[(++frameVIndex) % 5] = (float) (event.getRawX() - startX) / deltaT;
                                            frameVy[(frameVIndex % 5)] = (float) (event.getRawY() - startY) / deltaT;
                                            Log.d(TAG, "onTouchEvent: " + deltaT + " " + frameVx + " " + frameVy);
                                            break;
                                        case MOVINGOBJ: {
                                            MovingObj movingObj = movingObjList.get(selectedPos);
                                            movingObj.x += (event.getRawX() - startX) / ratio;
                                            movingObj.y -= (event.getRawY() - startY) / ratio;
                                            break;
                                        }
                                        case PARTICLE_GENERATOR: {
                                            MovingObj movingObj=generatorList.get(selectedPos).movingObj;
                                            movingObj.x += (event.getRawX() - startX) / ratio;
                                            movingObj.y -= (event.getRawY() - startY) / ratio;
                                            break;
                                        }
                                        case M_FIELD:
                                        case E_FIELD:
                                        case DAMP:
                                        case CIRCLE_M_FIELD:
                                            Field field = fieldList.get(selectedPos);
                                            field.x1 += (event.getRawX() - startX) / ratio;
                                            field.y1 -= (event.getRawY() - startY) / ratio;
                                            if (selectedKind != CIRCLE_M_FIELD) {
                                                field.x2 += (event.getRawX() - startX) / ratio;
                                                field.y2 -= (event.getRawY() - startY) / ratio;
                                            }
                                            break;
                                        case G_CENTER:
                                        case E_CENTER:
                                            ForceCenter forceCenter = forceCenterList.get(selectedPos);
                                            forceCenter.x += (event.getRawX() - startX) / ratio;
                                            forceCenter.y -= (event.getRawY() - startY) / ratio;
                                            break;
                                        case LINE_TRACK: {
                                            Track track = trackList.get(selectedPos);
                                            switch (endOfTrack) {
                                                case 0:
                                                    track.x1 += (event.getRawX() - startX) / ratio;
                                                    track.y1 -= (event.getRawY() - startY) / ratio;
                                                    track.x2 += (event.getRawX() - startX) / ratio;
                                                    track.y2 -= (event.getRawY() - startY) / ratio;
                                                    break;
                                                case 1:
                                                    track.x1 += (event.getRawX() - startX) / ratio;
                                                    track.y1 -= (event.getRawY() - startY) / ratio;
                                                    break;
                                                case 2:
                                                    track.x2 += (event.getRawX() - startX) / ratio;
                                                    track.y2 -= (event.getRawY() - startY) / ratio;
                                                    break;
                                            }

                                            break;
                                        }
                                        case CIRCLE_TRACK: {
                                            Track track = trackList.get(selectedPos);
                                            switch (endOfTrack) {
                                                case 0:
                                                    track.x1 += (event.getRawX() - startX) / ratio;
                                                    track.y1 -= (event.getRawY() - startY) / ratio;
                                                    break;
                                                case 1: {
                                                    double x = toFrameX(event.getRawX()), y = toFrameY(event.getRawY());
                                                    MathVector v1 = new MathVector(Math.cos(track.start * Math.PI / 180), Math.sin(track.start * Math.PI / 180));
                                                    MathVector v2 = new MathVector(x - track.x1, y - track.y1);
                                                    double start = Math.acos(v1.multiply(v2) / v2.getLength()) * 180 / Math.PI;
                                                    if (isInArc(track.start, track.end, x, y, track.x1, track.y1, Math.sqrt((x - track.x1) * (x - track.x1) + (y - track.y1) * (y - track.y1)))) {
                                                        track.start += start;
                                                    } else {
                                                        track.start -= start;
                                                    }
                                                    if (track.start <= 0) {
                                                        track.start = 0;
                                                    }
                                                    if (track.start >= track.end) {
                                                        track.start = track.end;
                                                    }
                                                }
                                                break;
                                                case 2: {
                                                    double x = toFrameX(event.getRawX()), y = toFrameY(event.getRawY());
                                                    MathVector v1 = new MathVector(Math.cos(track.end * Math.PI / 180), Math.sin(track.end * Math.PI / 180));
                                                    MathVector v2 = new MathVector(x - track.x1, y - track.y1);
                                                    double end = Math.acos(v1.multiply(v2) / v2.getLength()) * 180 / Math.PI;
                                                    if (isInArc(track.start, track.end, x, y, track.x1, track.y1, Math.sqrt((x - track.x1) * (x - track.x1) + (y - track.y1) * (y - track.y1)))) {
                                                        track.end -= end;
                                                    } else {
                                                        track.end += end;
                                                    }
                                                    if (track.end >= 360) {
                                                        track.end = 360;
                                                    }
                                                    if (track.start >= track.end) {
                                                        track.end = track.start;
                                                    }
                                                }
                                                break;
                                            }
                                            break;
                                        }

                                    }
                                    startX = event.getRawX();
                                    startY = event.getRawY();
                                    invalidate();
                                    printTrace.clear();
                                    printFrame.invalidate();
                                } else {
                                    if (numPoint >= 2) {
                                        double newDist = spacing(event);
                                        double dist = newDist / oldDist;
                                        if (selectedKind != CIRCLE_TRACK && selectedKind != CIRCLE_M_FIELD) {
                                            setRatio(ratio * (float) dist);
                                            deltaX += (toFrameX(event.getX(0)) - toFrameX(oldX) * dist) * ratio;
                                            deltaY -= (toFrameY(event.getY(0)) - toFrameY(oldY) * dist) * ratio;
                                        } else if (selectedKind == CIRCLE_TRACK) {
                                            Track track = trackList.get(selectedPos);
                                            track.radius *= dist;
                                        } else if (selectedKind == CIRCLE_M_FIELD) {
                                            Field field = fieldList.get(selectedPos);
                                            field.x2 *= dist;
                                        }
                                        oldDist = newDist;
                                        oldX = event.getX(0);
                                        oldY = event.getY(0);
                                        invalidate();
                                        printFrame.invalidate();
                                        printTrace.clear();
                                    }
                                }

                                break;
                            case 1: {
                                x1 += event.getRawX() - startX;
                                y1 += event.getRawY() - startY;
                                Field field = fieldList.get(selectedPos);
                                if (field.x1 < field.x2) {
                                    field.x1 = toFrameX(x1);
                                } else {
                                    field.x2 = toFrameX(x1);
                                }
                                if (field.y1 > field.y2) {
                                    field.y1 = toFrameY(y1);
                                } else {
                                    field.y2 = toFrameY(y1);
                                }
                                startX = event.getRawX();
                                startY = event.getRawY();
                                invalidate();
                                printFrame.invalidate();
                                break;
                            }
                            case 2: {
                                x2 += event.getRawX() - startX;
                                y1 += event.getRawY() - startY;
                                Field field = fieldList.get(selectedPos);
                                if (field.x1 > field.x2) {
                                    field.x1 = toFrameX(x2);
                                } else {
                                    field.x2 = toFrameX(x2);
                                }
                                if (field.y1 > field.y2) {
                                    field.y1 = toFrameY(y1);
                                } else {
                                    field.y2 = toFrameY(y1);
                                }
                                startX = event.getRawX();
                                startY = event.getRawY();
                                invalidate();
                                printFrame.invalidate();
                            }
                            break;
                            case 3: {
                                x1 += event.getRawX() - startX;
                                y2 += event.getRawY() - startY;
                                Field field = fieldList.get(selectedPos);
                                if (field.x1 < field.x2) {
                                    field.x1 = toFrameX(x1);
                                } else {
                                    field.x2 = toFrameX(x1);
                                }
                                if (field.y1 < field.y2) {
                                    field.y1 = toFrameY(y2);
                                } else {
                                    field.y2 = toFrameY(y2);
                                }
                                startX = event.getRawX();
                                startY = event.getRawY();
                                invalidate();
                                printFrame.invalidate();
                            }
                            break;
                            case 4: {
                                x2 += event.getRawX() - startX;
                                y2 += event.getRawY() - startY;
                                Field field = fieldList.get(selectedPos);
                                if (field.x1 > field.x2) {
                                    field.x1 = toFrameX(x2);
                                } else {
                                    field.x2 = toFrameX(x2);
                                }
                                if (field.y1 < field.y2) {
                                    field.y1 = toFrameY(y2);
                                } else {
                                    field.y2 = toFrameY(y2);
                                }
                                startX = event.getRawX();
                                startY = event.getRawY();
                                invalidate();
                                printFrame.invalidate();
                            }
                            break;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        numPoint--;
                        stretchOri = 0;
                        endOfTrack = 0;
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeMessages(1);
                        if (!copyFinish) {
                            copyMovingObj = null;
                        }
                        doMove = true;
                        numPoint = 0;
                        if (judgeClick < 10) {
                            double x = toFrameX(event.getRawX());
                            double y = toFrameY(event.getRawY());
                            boolean hasSelected = false;
                            for (int i = 0; i < fieldList.size(); i++) {
                                if (fieldList.get(i).belongTo(x, y)) {
                                    selectedKind = fieldList.get(i).kind;
                                    selectedPos = i;
                                    hasSelected = true;
                                }
                            }
                            for (int i = 0; i < forceCenterList.size(); i++) {
                                ForceCenter forceCenter = forceCenterList.get(i);
                                if (Math.sqrt(Math.pow(forceCenter.x - x, 2) + Math.pow(forceCenter.y - y, 2)) <= 3) {
                                    selectedKind = forceCenter.kind;
                                    selectedPos = i;
                                    hasSelected = true;
                                }
                            }
                            for (int i = 0; i < connecterList.size(); i++) {
                                Connecter connecter = connecterList.get(i);
                                MovingObj obj1 = movingObjList.get(connecter.obj1);
                                MovingObj obj2 = movingObjList.get(connecter.obj2);
                                if (MathVector.distanceDotToLine(x, y, obj1.x, obj1.y, obj2.x, obj2.y) <= 2) {
                                    if (x < Math.max(obj1.x, obj2.x) && x > Math.min(obj1.x, obj2.x)) {
                                        selectedKind = connecter.kind;
                                        selectedPos = i;
                                        hasSelected = true;
                                    }
                                }
                            }
                            for (int i = 0; i < trackList.size(); i++) {
                                Track track = trackList.get(i);
                                switch (track.kind) {
                                    case LINE_TRACK:
                                        if (MathVector.distanceDotToLine(x, y, track.x1, track.y1, track.x2, track.y2) <= 2) {
                                            if (x <= Math.max(track.x1, track.x2) + 1 && x >= Math.min(track.x1, track.x2) - 1) {
                                                selectedKind = track.kind;
                                                selectedPos = i;
                                                hasSelected = true;
                                            }
                                        }
                                        break;
                                    case CIRCLE_TRACK:
                                        double distance = Math.sqrt(Math.pow(track.x1 - x, 2) + Math.pow(track.y1 - y, 2));
                                        if (Math.abs(distance - track.radius) <= 2) {
                                            if (isInArc(track.start, track.end, x, y, track.x1, track.y1, distance)) {
                                                selectedKind = track.kind;
                                                selectedPos = i;
                                                hasSelected = true;
                                            }
                                        }
                                        break;
                                }

                            }
                            for (int i = 0; i < movingObjList.size(); i++) {
                                MovingObj movingObj = movingObjList.get(i);
                                if (Math.sqrt(Math.pow(movingObj.x - x, 2) + Math.pow(movingObj.y - y, 2)) <= movingObj.radius + 1) {
                                    selectedKind = MOVINGOBJ;
                                    selectedPos = i;
                                    hasSelected = true;
                                }
                            }
                            for (int i = 0; i < generatorList.size(); i++) {
                                MovingObj movingObj = generatorList.get(i).movingObj;
                                if (Math.sqrt(Math.pow(movingObj.x - x, 2) + Math.pow(movingObj.y - y, 2)) <= movingObj.radius + 1) {
                                    selectedKind = PARTICLE_GENERATOR;
                                    selectedPos = i;
                                    hasSelected = true;
                                }
                            }
                            if (!hasSelected) {
                                clickTimes++;
                                if (clickTimes == 2 && System.currentTimeMillis() - doubleClickStartTime <= 600) {
                                    handler.sendEmptyMessage(3);
                                }
                                if (clickTimes >= 2)
                                    clickTimes = 0;
                                doubleClickStartTime = System.currentTimeMillis();
                            }
                            if (!hasSelected && stretchOri == 0 && endOfTrack == 0) {
                                if (selectedKind == MOVINGOBJ) {
                                    for (int i = 0; i < connecterList.size(); i++) {
                                        Connecter connecter = connecterList.get(i);
                                        if (connecter.kind != SPRING && (connecter.obj1 == selectedPos || connecter.obj2 == selectedPos)) {
                                            connecter.length = getDistance(movingObjList.get(connecter.obj1), movingObjList.get(connecter.obj2));
                                        }
                                    }
                                }
                                selectedKind = -1;
                                selectedPos = 0;
                                endOfTrack = 0;
                                MainActivity.removeAllFab();
                                if (mRunning) {
                                    if (handler.hasMessages(0)) {
                                        MainActivity.pause.setVisibility(View.VISIBLE);
                                    } else {
                                        MainActivity.play.setVisibility(View.VISIBLE);
                                    }
                                    MainActivity.again.setVisibility(View.VISIBLE);
                                } else {
                                    MainActivity.play.setVisibility(View.VISIBLE);
                                    MainActivity.add.setVisibility(View.VISIBLE);
                                }
                                notifyChange();
                            } else {
                                MainActivity.removeAllFab();
                                MainActivity.delete.setVisibility(VISIBLE);
                                MainActivity.edit.setVisibility(VISIBLE);
                            }
                            stretchOri = 0;
                            invalidate();
                        }
                        endOfTrack = 0;
                        if (frameVx[0] != 0 || frameVy[0] != 0) {
                            for (int i = 0; i < 5; i++) {
                                frameVx[5] += frameVx[i] / 5;
                                frameVy[5] += frameVy[i] / 5;
                                frameVx[i] = frameVy[i] = 0;
                            }
                            isSlowingDown = true;
                            handler.sendEmptyMessage(5);
                        }
                        break;
                }
            } else if (isChoosingConnectObj){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < movingObjList.size(); i++) {
                            MovingObj movingObj = movingObjList.get(i);
                            if (Math.sqrt(Math.pow(movingObj.x - toFrameX(event.getRawX()), 2) + Math.pow(movingObj.y - toFrameY(event.getRawY()), 2)) <= movingObj.radius + 1) {
                                chooseObj1 = i;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        choosingX = event.getRawX();
                        choosingY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        for (int i = 0; i < movingObjList.size(); i++) {
                            MovingObj movingObj = movingObjList.get(i);
                            if (Math.sqrt(Math.pow(movingObj.x - toFrameX(event.getRawX()), 2) + Math.pow(movingObj.y - toFrameY(event.getRawY()), 2)) <= movingObj.radius + 1) {
                                chooseObj2 = i;
                            }
                        }
                        break;
                }
                invalidate();
            }else{
                for (int i = 0; i < movingObjList.size(); i++) {
                    MovingObj movingObj = movingObjList.get(i);
                    if (Math.sqrt(Math.pow(movingObj.x - toFrameX(event.getRawX()), 2) + Math.pow(movingObj.y - toFrameY(event.getRawY()), 2)) <= movingObj.radius + 1) {
                        chooseObj1 = i;
                    }
                }
            }
        }

        return true;

    }
    private void flushState(int i) {
        MovingObj movingObj = movingObjList.get(i);
        MathVector v = new MathVector(gx, -gy);
        for (int j = 0; j < fieldList.size(); j++) {
            Field field = fieldList.get(j);
            if (!field.belongTo(movingObj))
                continue;
            switch (field.kind) {
                case E_FIELD:
                    if (field.direction == Field.VERTICAL)
                        v.add(new MathVector(0, field.value * movingObj.q / movingObj.m));
                    else
                        v.add(new MathVector(field.value * movingObj.q / movingObj.m, 0));
                    break;
                case M_FIELD:
                case CIRCLE_M_FIELD:
                    double omega = timeGap * field.value * movingObj.q / movingObj.m / 1000;
                    MathVector v1 = new MathVector(movingObj.vx, movingObj.vy).revolve(omega);
                    movingObj.vx = v1.x;
                    movingObj.vy = v1.y;
                    break;
                case DAMP:
                    if (movingObj.getV() != 0)
                        v.add(MathVector.formVectorAs(field.value * Math.pow(movingObj.getV(), field.coefficient) / movingObj.m, new MathVector(-movingObj.vx, -movingObj.vy)));
                    break;
            }
        }
        for (int j = 0; j < forceCenterList.size(); j++) {
            ForceCenter forceCenter = forceCenterList.get(j);
            MathVector distance = new MathVector(forceCenter.x - movingObj.x, forceCenter.y - movingObj.y);
            switch (forceCenter.kind) {
                case G_CENTER:
                    v.add(MathVector.formVectorAs((G * forceCenter.value / Math.pow(distance.getLength(), 2)), distance));
                    break;
                case E_CENTER:
                    v.add(MathVector.formVectorAs((-K * forceCenter.value * movingObj.q / movingObj.m / Math.pow(distance.getLength(), 2)), distance));
                    break;
            }
        }
        if (doBetE) {
            for (int j = 0; j < movingObjList.size(); j++) {
                if (i == j)
                    continue;
                MovingObj movingObj1 = movingObjList.get(j);
                MathVector distance = new MathVector(movingObj.x - movingObj1.x, movingObj.y - movingObj1.y);
                v.add(MathVector.formVectorAs((K * movingObj.q * movingObj1.q / movingObj.m / Math.pow(distance.getLength(), 2)), distance));
            }
        }
        if (doBetG) {
            for (int j = 0; j < movingObjList.size(); j++) {
                if (i == j)
                    continue;
                MovingObj movingObj1 = movingObjList.get(j);
                MathVector distance = new MathVector(movingObj.x - movingObj1.x, movingObj.y - movingObj1.y);
                v.add(MathVector.formVectorAs(-(G * movingObj1.m / Math.pow(distance.getLength(), 2)), distance));
            }
        }
        movingObj.vx += v.x * timeGap / 1000;
        movingObj.vy += v.y * timeGap / 1000;

        if (hasBoundary) {
            double x = toRawX(movingObj.x), y = toRawY(movingObj.y), r = movingObj.radius * ratio;
            if (x + r >= width || x - r <= 0) {
                movingObj.vx = -movingObj.vx;
                if (movingObj.vx != 0)
                    hasImpactSound = true;
            }
            if (y + r >= height || y - r <= 0) {
                movingObj.vy = -movingObj.vy;
                if (movingObj.vy != 0)
                    hasImpactSound = true;
            }
        }

        for (int k = 0; k < trackList.size(); k++) {
            Track track = trackList.get(k);
            double distance;
            MathVector speed = new MathVector(movingObj.vx, movingObj.vy);
            switch (track.kind) {
                case LINE_TRACK:
                    distance = MathVector.distanceDotToLine(movingObj.x, movingObj.y, track.x1, track.y1, track.x2, track.y2);
                    if (distance <= movingObj.radius) {
                        double[] pedal = MathVector.pedalDotToLine(movingObj.x, movingObj.y, track.x1, track.y1, track.x2, track.y2);
                        if ((pedal[0] <= Math.max(track.x1, track.x2) && pedal[0] >= Math.min(track.x1, track.x2)) || (pedal[1] >= Math.min(track.y1, track.y2) && pedal[1] <= Math.max(track.y1, track.y2))) {
                            MathVector line = new MathVector(track.x1 - track.x2, track.y1 - track.y2);
                            if (track.objRemover) {
                                movingObjList.remove(i);
                            } else {
                                if (speed.multiply(line) < 0) {
                                    line.x = -line.x;
                                    line.y = -line.y;
                                }
                                speed.substract(speed.shadow(line));
                                movingObj.vx -= 2 * speed.x;
                                movingObj.vy -= 2 * speed.y;
                            }
                        }
                    }
                    break;
                case CIRCLE_TRACK:
                    distance = Math.sqrt(Math.pow(track.x1 - movingObj.x, 2) + Math.pow(track.y1 - movingObj.y, 2));
                    if (distance <= movingObj.radius + track.radius && distance >= -movingObj.radius + track.radius) {
                        if (isInArc(track.start, track.end, movingObj.x, movingObj.y, track.x1, track.y1, distance)) {
                            if (track.objRemover) {
                                movingObjList.remove(i);
                            } else {
                                MathVector tangent = new MathVector(track.y1 - movingObj.y, -track.x1 + movingObj.x);
                                if (speed.multiply(tangent) < 0) {
                                    tangent.x = -tangent.x;
                                    tangent.y = -tangent.y;
                                }
                                speed.substract(speed.shadow(tangent));
                                movingObj.vx -= 2 * speed.x;
                                movingObj.vy -= 2 * speed.y;
                            }

                        }
                    }
                    break;
            }
        }
        if (doImpact) {
            for (int k = i + 1; k < movingObjList.size(); k++) {
                MovingObj movingObj1 = movingObjList.get(k);
                double distance = getDistance(movingObj, movingObj1);
                if (distance <= movingObj.radius + movingObj1.radius
                        && (strongInteraction || distance >= getLaterDistance(movingObj1, movingObj, timeGap))) {
                    if (movingObj.getV() != 0 || movingObj1.getV() != 0) {
                        hasImpactSound = true;
                        impact(movingObj, movingObj1);
                    }
                }
            }
        }

        if (doRelativity) {
            double speed = movingObj.getV();
            if (speed >= 0.25 * c) {
                if (speed >= c) {
                    movingObj.vx *= c / speed;
                    movingObj.vy *= c / speed;
                } else
                    movingObj.m = movingObj.m0 / Math.sqrt(1 - speed * speed / c / c);
            }
        }
        for (int j = 0; j < connecterList.size(); j++) {
            Connecter connecter = connecterList.get(j);
            MovingObj obj1 = movingObjList.get(connecter.obj1);
            MovingObj obj2 = movingObjList.get(connecter.obj2);
            double distance = getDistance(obj1, obj2);
            switch (connecter.kind) {
                case ROPE:
                    if (distance >= connecter.length
                            && distance <= getLaterDistance(obj1, obj2, timeGap))
                        impact(obj1, obj2);
                    break;
                case POLE:
                    impact(obj1, obj2);
                    break;
                case SPRING:
                    MathVector ve = MathVector.formVectorAs(connecter.k * (distance - connecter.length) * timeGap / 1000, new MathVector(obj1.x - obj2.x, obj1.y - obj2.y));
                    obj1.vx -= ve.x / obj1.m;
                    obj1.vy -= ve.y / obj1.m;
                    obj2.vx += ve.x / obj2.m;
                    obj2.vy += ve.y / obj2.m;
                    break;
            }

        }
    }
    //private AtomicInteger cnt=new AtomicInteger(0);
    public void flushState() {
        if (doMultiThread && movingObjList.size() > 2) {
            //cnt.getAndSet(0);
            for (int z = 0; z < movingObjList.size(); z++) {
                final int i = z;
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        flushState(i);
                        //cnt.getAndIncrement();
                    }
                });
            }
            //while (cnt.intValue() != movingObjList.size());
            for (int z = 0; z < movingObjList.size(); z++) {
                final int i = z;
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        MovingObj movingObj = movingObjList.get(i);
                        movingObj.x += movingObj.vx * timeGap / 1000;
                        movingObj.y += movingObj.vy * timeGap / 1000;
                    }
                });
            }


        } else {
            for (int z = 0; z < movingObjList.size(); z++)
                flushState(z);
            for (int z = 0; z < movingObjList.size(); z++){
                MovingObj movingObj=movingObjList.get(z);
                movingObj.x += movingObj.vx * timeGap / 1000;
                movingObj.y += movingObj.vy * timeGap / 1000;
            }
        }
    }

    private boolean isTracing = false;
    private float traceX = 0.0f, traceY = 0.0f;

    public void setTracingState() {
        isTracing = !isTracing;
        Toast.makeText(mContext, (isTracing ? "进入" : "退出") + "测量模式", Toast.LENGTH_SHORT);
    }

    private SensorManager sensorManager = null;
    private Sensor gSensor = null;
    private Toast mToast;

    private int timeCounter = 1;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    flushState();
                    if ((timeCounter++) % 375 == 0) {
                        invalidate();
                        printFrame.invalidate();
                    }
                    if ((timeCounter % 30) == 0) {
                        for (MovingObj movingObj : movingObjList) {
                            movingObj.change(timeCounter * timeGap / 1000);
                        }
                        for (Field field : fieldList) {
                            field.change(timeCounter * timeGap / 1000);
                        }
                        if (doImpactSound && hasImpactSound) {
                            handler.sendEmptyMessage(4);
                            hasImpactSound = false;
                        }
                    }
                    for (ParticleGenerator generator : generatorList) {
                        if (generator.on) {
                            MovingObj movingObj;
                            switch (generator.kind) {
                                default:
                                    break;
                                case GENERATOR_TIME:
                                    if ((VPLFrame.this.timeCounter * VPLFrame.this.timeGap / 1000.0D <= generator.now) && ((VPLFrame.this.timeCounter + 1) * VPLFrame.this.timeGap / 1000.0D > generator.now) && (generator.now <= generator.to)) {
                                        movingObj = generator.movingObj.clone();
                                        VPLFrame.this.movingObjList.add(movingObj);
                                        generator.now += generator.gap;
                                    }
                                    break;
                                case GENERATOR_ANGLE:
                                    for (double d = generator.from; d <= generator.to; d += generator.gap) {
                                        movingObj = generator.movingObj.clone();
                                        movingObj.vx = (generator.movingObj.getV() * Math.cos(Math.PI * d / 180.0D));
                                        movingObj.vy = (generator.movingObj.getV() * Math.sin(Math.PI * d / 180.0D));
                                        VPLFrame.this.movingObjList.add(movingObj);
                                    }
                                    generator.on = false;
                            }
                        }
                    }
                    handler.sendEmptyMessage(0);
                    break;
                case 1:
                    if (System.currentTimeMillis() - clickStartTime >= 600) {
                        if (copyMovingObj != null) {
                            if (Math.sqrt(Math.pow(copyMovingObj.x - toFrameX(downX), 2) + Math.pow(copyMovingObj.y - toFrameY(downY), 2)) <= copyMovingObj.radius + 1) {
                                mToast = Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT);
                                mToast.show();
                                sendEmptyMessageDelayed(2, 600);
                                copyFinish = true;
                            } else {
                                MovingObj movingObj = copyMovingObj.clone();
                                movingObj.x = toFrameX(downX);
                                movingObj.y = toFrameY(downY);
                                movingObjList.add(movingObj);
                                mToast = Toast.makeText(mContext, "粘贴成功", Toast.LENGTH_SHORT);
                                mToast.show();
                                sendEmptyMessageDelayed(2, 600);
                            }
                        }
                        return;
                    }
                    sendEmptyMessage(1);
                    break;
                case 2:
                    mToast.cancel();
                    break;
                case 3:
                    deltaX -= deltaX / 10;
                    deltaY -= deltaY / 10;
                    invalidate();
                    printFrame.invalidate();
                    printTrace.clear();
                    if (Math.abs(deltaX) >= 10 || Math.abs(deltaY) >= 10)
                        sendEmptyMessageDelayed(3, 15);
                    break;
                case 4:
                    AudioManager mgr = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                    float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                    float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    float volume = streamVolumeCurrent / streamVolumeMax;
                    soundPool.play(soundId.get(1), volume, volume, 1, 0, 1f);
                    break;
                case 5:
                    if (isSlowingDown) {
                        deltaX += frameVx[5] * 10;
                        deltaY += frameVy[5] * 10;
                        frameVx[5] *= 0.95;
                        frameVy[5] *= 0.95;
                        invalidate();
                        printFrame.invalidate();
                        printTrace.clear();
                        if (Math.abs(frameVx[5]) > 0.1 || Math.abs(frameVy[5]) > 0.1) {
                            sendEmptyMessageDelayed(5, 10);
                        } else {
                            isSlowingDown = false;
                        }
                    }
                    break;

            }
        }
    };

    private class SaveInstance {
        ArrayList<MovingObj> movingObjList;
        ArrayList<ForceCenter> forceCenterList;
        ArrayList<Field> fieldList;
        ArrayList<Connecter> connecterList;
        ArrayList<Track> trackList;
        ArrayList<ParticleGenerator> generatorList;

        public SaveInstance() {
            movingObjList = new ArrayList<>();
            forceCenterList = new ArrayList<>();
            fieldList = new ArrayList<>();
            connecterList = new ArrayList<>();
            trackList = new ArrayList<>();
            generatorList = new ArrayList<>();
        }

    }
    ExecutorService threadPool;
    public void start() {
        handler.sendEmptyMessage(0);
        mRunning = true;
        if (movingObjList.size()!=0)
            threadPool = Executors.newFixedThreadPool(movingObjList.size());
    }

    public void end() {
        pause();
        timeCounter = 1;
        handler.sendEmptyMessage(3);
        choosingX = choosingY = 0;
        mRunning = false;
        invalidate();
        printFrame.invalidate();
    }

    public void pause() {
        if (threadPool!=null)
            threadPool.shutdownNow();
        choosingX = choosingY = 0;
        handler.removeMessages(0);
    }

    private boolean isInArc(double start, double end, double x, double y, double x0, double y0, double distance) {
        boolean inArc;
        double theta1 = Math.asin((y - y0) / distance) * 180 / Math.PI;
        double theta2 = Math.acos((x - x0) / distance) * 180 / Math.PI;
        if (theta1 >= 0) {
            if (theta2 <= 90) {
                inArc = theta1 <= end && theta1 >= start;
            } else {
                inArc = theta2 <= end && theta2 >= start;
            }
        } else {
            if (theta2 <= 90) {
                inArc = theta1 + 360 <= end && theta1 + 360 >= start;
            } else {
                inArc = 360 - theta2 <= end && 360 - theta2 >= start;
            }
        }
        return inArc;
    }

    private float toFrameX(double x0) {
        return (float) (x0 - width / 2 - deltaX) / ratio;
    }

    private float toFrameY(double y0) {
        return -(float) (y0 - height / 2 - deltaY) / ratio;
    }

    private float toRawX(double x0) {
        return (float) (x0 * ratio + width / 2 + deltaX);
    }

    private float toRawY(double y0) {
        return (float) (-y0 * ratio + height / 2 + deltaY);
    }

    private double spacing(MotionEvent event) {
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    private void drawAL(double sx, double sy, double ex, double ey, boolean isRaw, Canvas canvas, Paint paint) {
        if (!isRaw) {
            sx = toRawX(sx);
            sy = toRawY(sy);
            ex = toRawX(ex);
            ey = toRawY(ey);
        }
        double H = 18; // 箭头高度
        double L = 3.5; // 底边的一半
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        MathVector v = new MathVector(ex - sx, ey - sy);
        MathVector v1 = MathVector.formVectorAs(arraow_len, v.revolve(awrad));
        MathVector v2 = MathVector.formVectorAs(arraow_len, v.revolve(-awrad));
        double x_3 = ex - v1.x;
        double y_3 = ey - v1.y;
        double x_4 = ex - v2.x;
        double y_4 = ey - v2.y;
        canvas.drawLine((float) sx, (float) sy, (float) ex, (float) ey, paint);
        canvas.drawLine((float) ex, (float) ey, (float) x_3, (float) y_3, paint);
        canvas.drawLine((float) ex, (float) ey, (float) x_4, (float) y_4, paint);
    }

    private static double getDistance(final MovingObj arg1, final MovingObj arg2) {
        return Math.sqrt(Math.pow(arg1.x - arg2.x, 2) + Math.pow(arg1.y - arg2.y, 2));
    }

    private static double getLaterDistance(final MovingObj arg1, final MovingObj arg2, double timeGap) {
        return Math.sqrt(Math.pow(arg1.x + arg1.vx * timeGap / 1000 - arg2.x - arg2.vx * timeGap / 1000, 2) +
                Math.pow(arg1.y + arg1.vy * timeGap / 1000 - arg2.y - arg2.vy * timeGap / 1000, 2));
    }

    private static void impact(MovingObj arg1, MovingObj arg2) {
        if (arg1.x == arg2.x && arg1.y == arg2.y)
            return;
        MathVector v1 = new MathVector(arg1.vx, arg1.vy);
        MathVector v2 = new MathVector(arg2.vx, arg2.vy);
        MathVector v1s = v1.shadow(new MathVector(arg2.x - arg1.x, arg2.y - arg1.y));
        MathVector v2s = v2.shadow(new MathVector(arg1.x - arg2.x, arg1.y - arg2.y));
        v1.substract(v1s);
        v2.substract(v2s);
        double vx1 = v1s.x, vx2 = v2s.x, vy1 = v1s.y, vy2 = v2s.y, m1 = arg1.m, m2 = arg2.m;
        //记着:高考考场上公式写错绝不是仅仅是这两周里面我白白浪费掉的苦苦思索所能弥补的
        v1s.x = ((m1 - m2) * vx1 + 2 * m2 * vx2) / (m1 + m2);
        v2s.x = ((m2 - m1) * vx2 + 2 * m1 * vx1) / (m1 + m2);
        v1s.y = ((m1 - m2) * vy1 + 2 * m2 * vy2) / (m1 + m2);
        v2s.y = ((m2 - m1) * vy2 + 2 * m1 * vy1) / (m1 + m2);
        v1.add(v1s);
        v2.add(v2s);
        arg1.vx = v1.x;
        arg1.vy = v1.y;
        arg2.vx = v2.x;
        arg2.vy = v2.y;
    }

    public class MovingObj extends Changeable implements Cloneable, Serializable {
        @Override
        public MovingObj clone() {
            MovingObj movingObj = null;
            try {
                movingObj = (MovingObj) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return movingObj;
        }

        @Override
        public void change(double time) {
            for (int i = 0; i < variables.size(); i++) {
                switch (variables.get(i)) {
                    case 109:
                        m = m0 = exps.get(i).getResult(time);
                        break;
                    case 120:
                        x = exps.get(i).getResult(time);
                        break;
                    case 121:
                        y = exps.get(i).getResult(time);
                        break;
                    case 3778:
                        vx = exps.get(i).getResult(time);
                        break;
                    case 3779:
                        vy = exps.get(i).getResult(time);
                        break;
                    case 113:
                        q = exps.get(i).getResult(time);
                        break;
                    case -938578798:
                        radius = exps.get(i).getResult(time);
                        break;
                }
            }
        }

        private void initPaint(int r, int g, int b) {
            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setARGB(255, r, g, b);
            this.paint = paint;
            Paint line = new Paint();
            line.setStrokeWidth(4);
            line.setAntiAlias(true);
            line.setStyle(Paint.Style.STROKE);
            line.setARGB(255, r, g, b);
            this.line = line;
        }

        public double getV() {
            return Math.sqrt(vx * vx + vy * vy);
        }

        public MovingObj(double arg_x, double arg_y, double arg_vx, double arg_vy, double arg_radius, double arg_m, double arg_q, int color) {
            x = arg_x;
            y = arg_y;
            vx = arg_vx;
            vy = arg_vy;
            radius = arg_radius;
            m0 = m = arg_m;
            q = arg_q;
            int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
            initPaint(r, g, b);
        }

        public void setBitmap(Bitmap arg) {
            bitmap = arg;
        }

        Bitmap bitmap;
        Paint paint;
        Paint line;
        double q;
        double m, m0;
        double x, y;
        double vx, vy;
        double radius;
    }

    public class Field extends Changeable implements Cloneable {
        static final int HORIZONTAL = 0;//水平
        static final int VERTICAL = 1;//竖直
        double x1, y1, x2, y2;
        double value;
        int kind;
        int direction;
        double coefficient = 1;

        public Field clone() {
            try {
                Field localField = (Field) super.clone();
                return localField;
            } catch (CloneNotSupportedException localCloneNotSupportedException) {
                localCloneNotSupportedException.printStackTrace();
            }
            return null;
        }

        @Override
        public void change(double time) {
            for (int i = 0; i < variables.size(); i++) {
                switch (variables.get(i)) {
                    case 3769:
                        x1 = exps.get(i).getResult(time);
                        break;
                    case 3800:
                        y1 = exps.get(i).getResult(time);
                        break;
                    case 3770:
                        x2 = exps.get(i).getResult(time);
                        break;
                    case 3801:
                        y2 = exps.get(i).getResult(time);
                        break;
                    case 111972721:
                        value = exps.get(i).getResult(time);
                        break;
                }
            }
        }

        public Field(double arg_x, double arg_y, double arg_r, double arg_value) {
            x1 = arg_x;
            y1 = arg_y;
            x2 = arg_r;
            value = arg_value;
            kind = VPLFrame.CIRCLE_M_FIELD;
        }

        public Field(double arg_x1, double arg_y1, double arg_x2, double arg_y2, double arg_value, double arg_coefficient) {
            x1 = arg_x1;
            y1 = arg_y1;
            x2 = arg_x2;
            y2 = arg_y2;
            value = arg_value;
            kind = VPLFrame.DAMP;
            coefficient = arg_coefficient;
        }

        public Field(double arg_x1, double arg_y1, double arg_x2, double arg_y2, double arg_value, int arg_kind, int arg_direction) {
            x1 = arg_x1;
            y1 = arg_y1;
            x2 = arg_x2;
            y2 = arg_y2;
            value = arg_value;
            kind = arg_kind;
            direction = arg_direction;
        }

        public boolean belongTo(double x, double y) {
            if (kind != VPLFrame.CIRCLE_M_FIELD) {
                if (x1 >= x2) {
                    if (y1 >= y2)
                        return x < x1 && x > x2 && y < y1 && y > y2;
                    else return x < x1 && x > x2 && y > y1 && y < y2;
                } else {
                    if (y1 >= y2)
                        return x > x1 && x < x2 && y < y1 && y > y2;
                    else return x > x1 && x < x2 && y > y1 && y < y2;
                }
            } else return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)) <= x2;
        }

        public boolean belongTo(MovingObj movingObj) {
            double x = movingObj.x, y = movingObj.y;
            return belongTo(x, y);
        }
    }

    public class ParticleGenerator
            implements Cloneable {
        double from;
        double gap;
        int kind;
        VPLFrame.MovingObj movingObj;
        double now;
        boolean on = true;
        double to;

        public ParticleGenerator(VPLFrame.MovingObj paramMovingObj, int paramInt, double paramDouble1, double paramDouble2, double paramDouble3) {
            this.movingObj = paramMovingObj;
            this.kind = paramInt;
            this.from = paramDouble1;
            this.to = paramDouble2;
            this.gap = paramDouble3;
            this.now = paramDouble3;
        }

        public ParticleGenerator clone() {
            try {
                ParticleGenerator generator = (ParticleGenerator) super.clone();
                return generator;
            } catch (CloneNotSupportedException localCloneNotSupportedException) {
                localCloneNotSupportedException.printStackTrace();
            }
            return null;
        }
    }

    public class Track implements Cloneable {
        int kind;
        double x1, y1, x2, y2;
        double radius, start, end;
        boolean objRemover = false;

        public Track clone() {
            try {
                Track localTrack = (Track) super.clone();
                return localTrack;
            } catch (CloneNotSupportedException localCloneNotSupportedException) {
                localCloneNotSupportedException.printStackTrace();
            }
            return null;
        }

        public Track(double arg_x1, double arg_y1, double arg_x2, double arg_y2, boolean arg_remover) {
            x1 = arg_x1;
            y1 = arg_y1;
            x2 = arg_x2;
            y2 = arg_y2;
            kind = VPLFrame.LINE_TRACK;
            objRemover = arg_remover;
        }

        public Track(double arg_x, double arg_y, double arg_radius, double arg_start, double arg_end, boolean arg_remover) {
            x1 = arg_x;
            y1 = arg_y;
            radius = arg_radius;
            start = arg_start;
            end = arg_end;
            kind = VPLFrame.CIRCLE_TRACK;
            objRemover = arg_remover;
        }

    }

    public class ForceCenter implements Cloneable {
        int kind;
        Paint paint;
        double value;
        double x, y;

        public ForceCenter clone() {
            try {
                ForceCenter localForceCenter = (ForceCenter) super.clone();
                return localForceCenter;
            } catch (CloneNotSupportedException localCloneNotSupportedException) {
                localCloneNotSupportedException.printStackTrace();
            }
            return null;
        }

        public ForceCenter(double arg_x, double arg_y, double arg_value, int arg_kind) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setARGB(255, 0, 0, 0);
            x = arg_x;
            y = arg_y;
            value = arg_value;
            kind = arg_kind;
        }
    }

    public class Connecter implements Cloneable {
        double length = 0;
        int obj1, obj2;
        int kind;
        double k = 0;

        public Connecter clone() {
            try {
                Connecter localConnecter = (Connecter) super.clone();
                return localConnecter;
            } catch (CloneNotSupportedException localCloneNotSupportedException) {
                localCloneNotSupportedException.printStackTrace();
            }
            return null;
        }

        public Connecter(int arg_obj1, int arg_obj2, double arg_k, double arg_length, int arg_kind) {
            obj1 = arg_obj1;
            obj2 = arg_obj2;
            kind = arg_kind;
            k = arg_k;
            length = arg_length;
        }

        public Connecter(int arg_obj1, int arg_obj2, int arg_kind) {
            obj1 = arg_obj1;
            obj2 = arg_obj2;
            kind = arg_kind;
        }
    }

    public abstract class Changeable {
        ArrayList<Integer> variables = new ArrayList<>();
        ArrayList<Calculator> exps = new ArrayList<>();

        abstract void change(double time);
    }

}
