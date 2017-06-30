package com.example.vpl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by MashPlant on 2016/4/10.
 */
public class PrintFrame extends View {
    private float ratio = 20;
    private int height = 1920, width = 1080;
    private double deltaX = 0, deltaY = 0;
    private VPLFrame vplFrame;
    static public Paint normalLine = new Paint() {
        {
            setARGB(255, 0, 0, 0);
            setAntiAlias(true);
            setStrokeWidth(2);
        }
    };
    static public Paint mainLine = new Paint() {
        {
            setARGB(255, 0, 0, 0);
            setAntiAlias(true);
            setStyle(Style.STROKE);
            setStrokeWidth(4);
        }
    };
    static public Paint redLine = new Paint() {
        {
            setARGB(255, 255, 0, 0);
            setAntiAlias(true);
            setStyle(Style.STROKE);
            setStrokeWidth(4);
        }
    };
    static public Paint greenLine = new Paint() {
        {
            setARGB(255, 0, 255, 0);
            setAntiAlias(true);
            setStyle(Style.STROKE);
            setStrokeWidth(4);
        }
    };

    static public Paint linePainter = new Paint() {
        {
            setARGB(200, 200, 200, 200);
            setAntiAlias(true);
            setStyle(Style.STROKE);
            setStrokeWidth(3);
        }
    };
    static public Paint wordPainter = new Paint() {
        {
            setTextSize(35);
            setARGB(200, 0, 0, 0);
            setAntiAlias(true);
        }
    };

    public PrintFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
    }

    private double toRawX(double x0) {
        return x0 * ratio + width / 2 + deltaX;
    }

    private double toRawY(double y0) {
        return -y0 * ratio + height / 2 + deltaY;
    }

    public void onDraw(Canvas canvas) {
        if (vplFrame != null) {
            ratio = vplFrame.getRatio();
            deltaX = vplFrame.getDeltaX();
            deltaY = vplFrame.getDeltaY();
            int magnitube = (int) Math.log10(30/ ratio);
            float ratio1 = ratio * (float) Math.pow(10, magnitube);
            for (int i = 0; i < (width / 2 + Math.abs(deltaX)) / 10 / ratio1; i++) {
                canvas.drawLine((float) (width / 2 + deltaX + i * 10 * ratio1), 0, (float) (width / 2 + deltaX + i * 10 * ratio1), height, linePainter);
                canvas.drawLine((float) (width / 2 + deltaX - i * 10 * ratio1), 0, (float) (width / 2 + deltaX - i * 10 * ratio1), height, linePainter);
                canvas.drawText(Integer.toString(10 * i) + (magnitube != 0 ? "E" + magnitube : ""), (float) (width / 2 + deltaX + i * 10 * ratio1), (float) (height / 2 + deltaY), wordPainter);
                canvas.drawText(Integer.toString(-10 * i) + (magnitube != 0 ? "E" + magnitube : ""), (float) (width / 2 + deltaX - i * 10 * ratio1), (float) (height / 2 + deltaY), wordPainter);
            }
            for (int i = 0; i < (height / 2 + Math.abs(deltaY)) / 10 / ratio1; i++) {
                canvas.drawLine(0, (float) (height / 2 + deltaY + i * 10 * ratio1), width, (float) (height / 2 + deltaY + i * 10 * ratio1), linePainter);
                canvas.drawLine(0, (float) (height / 2 + deltaY - i * 10 * ratio1), width, (float) (height / 2 + deltaY - i * 10 * ratio1), linePainter);
                canvas.drawText(Integer.toString(10 * i) + (magnitube != 0 ? "E" + magnitube : ""), (float) (width / 2 + deltaX), (float) (height / 2 + deltaY - i * 10 * ratio1), wordPainter);
                canvas.drawText(Integer.toString(-10 * i) + (magnitube != 0 ? "E" + magnitube : ""), (float) (width / 2 + deltaX), (float) (height / 2 + deltaY + i * 10 * ratio1), wordPainter);
            }

            canvas.drawLine(0, (float) (height / 2 + deltaY), width, (float) (height / 2 + deltaY), mainLine);
            canvas.drawLine((float) (width / 2 + deltaX), 0, (float) (width / 2 + deltaX), height, mainLine);

            List<VPLFrame.Field> fieldList = vplFrame.getFieldList();
            for (int i = 0; i < fieldList.size(); i++) {
                VPLFrame.Field field = fieldList.get(i);
                double x1 = Math.max(field.x1, field.x2);
                double x2 = Math.min(field.x1, field.x2);
                double y1 = Math.max(field.y1, field.y2);
                double y2 = Math.min(field.y1, field.y2);
                if (field.kind != VPLFrame.CIRCLE_M_FIELD && (x1 == x2 || y1 == y2)) {
                    break;
                }
                switch (field.kind) {
                    case VPLFrame.E_FIELD: {
                        switch (field.direction) {
                            case VPLFrame.Field.HORIZONTAL:
                                for (double y0 = y2; y0 <= y1 + 0.1; y0 += (y1 - y2) / 5) {
                                    drawAL(field.value >= 0 ? x2 : x1, y0, field.value > 0 ? x1 : x2, y0, canvas, mainLine);
                                }
                                break;
                            case VPLFrame.Field.VERTICAL:
                                for (double x0 = x2; x0 <= x1 + 0.1; x0 += (x1 - x2) / 5) {
                                    drawAL(x0, field.value >= 0 ? y2 : y1, x0, field.value > 0 ? y1 : y2, canvas, mainLine);
                                }
                                break;
                        }
                        x1 = toRawX(x1);
                        x2 = toRawX(x2);
                        y1 = toRawY(y1);
                        y2 = toRawY(y2);
                        canvas.drawRect((float) x2, (float) y1, (float) x1, (float) y2, PrintFrame.greenLine);
                        break;
                    }
                    case VPLFrame.M_FIELD:
                        if (field.value < 0) {
                            for (double x0 = x2; x0 <= x1 + 0.1; x0 += (x1 - x2) / 8) {
                                for (double y0 = y2; y0 <= y1 + 0.1; y0 += (y1 - y2) / 8)
                                    canvas.drawCircle((float) (x0 * ratio + width / 2 + deltaX), (float) (-y0 * ratio + height / 2 + deltaY), 0.2f * ratio, normalLine);
                            }
                        } else {
                            for (double x0 = x2; x0 <= x1 + 0.1; x0 += (x1 - x2) / 8) {
                                for (double y0 = y2; y0 <= y1 + 0.1; y0 += (y1 - y2) / 8)
                                    drawCross(x0, y0, 0.2f, canvas);
                            }
                        }
                        x1 = toRawX(x1);
                        x2 = toRawX(x2);
                        y1 = toRawY(y1);
                        y2 = toRawY(y2);
                        canvas.drawRect((float) x2, (float) y1, (float) x1, (float) y2, PrintFrame.greenLine);
                        break;
                    case VPLFrame.CIRCLE_M_FIELD:
                        double r = field.x2;
                        x1 = field.x1;
                        y1 = field.y1;
                        for (double y0 = y1 - r; y0 <= y1 + r + 1; y0 += 2 * r / 8) {
                            for (double x0 = 0; (y0 - y1) * (y0 - y1) + x0 * x0 <= r * r; x0 += 2 * r / 8) {
                                if (field.value < 0) {
                                    canvas.drawCircle((float) toRawX(x1 + x0), (float) toRawY(y0), 0.2f * ratio, normalLine);
                                    canvas.drawCircle((float) toRawX(x1 - x0), (float) toRawY(y0), 0.2f * ratio, normalLine);
                                } else {
                                    drawCross(x1 + x0, y0, 0.2f, canvas);
                                    drawCross(x1 - x0, y0, 0.2f, canvas);
                                }
                            }
                        }
                        x1 = toRawX(x1);
                        y1 = toRawY(y1);
                        canvas.drawCircle((float) x1, (float) y1, (float) r * ratio, greenLine);
                        break;
                    case VPLFrame.DAMP:
                        x1 = toRawX(x1);
                        x2 = toRawX(x2);
                        y1 = toRawY(y1);
                        y2 = toRawY(y2);
                        canvas.drawRect((float) x2, (float) y1, (float) x1, (float) y2, PrintFrame.greenLine);
                        for (int k = 0; k < 64; k++) {
                            canvas.drawCircle(rand(x1, x2), rand(y1, y2), 0.15f * ratio, normalLine);
                        }
                        break;
                }
            }
            List<VPLFrame.ForceCenter> forceCenterList = vplFrame.getForceCenterList();
            for (int i = 0; i < forceCenterList.size(); i++) {
                VPLFrame.ForceCenter forceCenter = forceCenterList.get(i);
                double x = width / 2 + forceCenter.x * ratio;
                double y = height / 2 - forceCenter.y * ratio;
                canvas.drawCircle((float) (x + deltaX), (float) (y + deltaY), 2 * ratio, forceCenter.paint);
            }
            List<VPLFrame.Track> trackList = vplFrame.getTrackList();
            for (int i = 0; i < trackList.size(); i++) {
                VPLFrame.Track track = trackList.get(i);
                double x1 = width / 2 + track.x1 * ratio + deltaX;
                double y1 = height / 2 - track.y1 * ratio + deltaY;
                switch (track.kind) {
                    case VPLFrame.LINE_TRACK:
                        double x2 = width / 2 + track.x2 * ratio + deltaX;
                        double y2 = height / 2 - track.y2 * ratio + deltaY;
                        canvas.drawLine((float) x1, (float) y1, (float) x2, (float) y2, mainLine);
                        break;
                    case VPLFrame.CIRCLE_TRACK:
                        double r = track.radius * ratio;
                        RectF rectF = new RectF((float) (x1 - r), (float) (y1 - r), (float) (x1 + r), (float) (y1 + r));
                        canvas.drawArc(rectF, -(float) track.start, -(float) (track.end - track.start), false, mainLine);
                        break;
                }
            }
        }
    }

    private float rand(double a, double b) {
        if (a < b)
            return (float) (a + Math.random() * (b - a + 1));
        else return (float) (b + Math.random() * (a - b + 1));
    }


    private void drawCross(double arg_x, double arg_y, double stroke, Canvas canvas) {
        double x = arg_x * ratio + width / 2 + deltaX;
        double y = -arg_y * ratio + height / 2 + deltaY;
        stroke *= ratio;
        canvas.drawLine((float) (x - stroke), (float) (y - stroke), (float) (x + stroke), (float) (y + stroke), normalLine);
        canvas.drawLine((float) (x - stroke), (float) (y + stroke), (float) (x + stroke), (float) (y - stroke), normalLine);
    }

    private void drawAL(double arg_sx, double arg_sy, double arg_ex, double arg_ey, Canvas canvas, Paint paint) {
        double sx = arg_sx * ratio + width / 2 + deltaX;
        double sy = -arg_sy * ratio + height / 2 + deltaY;
        double ex = arg_ex * ratio + width / 2 + deltaX;
        double ey = -arg_ey * ratio + height / 2 + deltaY;
        double H = 18; // 箭头高度
        double L = 3.5; // 底边的一半
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        MathVector v = new MathVector(ex - sx, ey - sy);
        MathVector v1 = MathVector.formVectorAs((double) arraow_len, v.revolve(awrad));
        MathVector v2 = MathVector.formVectorAs((double) arraow_len, v.revolve(-awrad));
        double x_3 = ex - v1.x;
        double y_3 = ey - v1.y;
        double x_4 = ex - v2.x;
        double y_4 = ey - v2.y;
        canvas.drawLine((float) sx, (float) sy, (float) ex, (float) ey, paint);
        canvas.drawLine((float) ex, (float) ey, (float) x_3, (float) y_3, paint);
        canvas.drawLine((float) ex, (float) ey, (float) x_4, (float) y_4, paint);
    }

    public void setMonitor(VPLFrame arg_vplframe) {
        vplFrame = arg_vplframe;
        ratio = vplFrame.getRatio();
        deltaX = vplFrame.getDeltaX();
        deltaY = vplFrame.getDeltaY();
        invalidate();

        //handler.sendEmptyMessage(0);
    }

    private Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //flushState();
                    //invalidate();
                    handler.sendEmptyMessageDelayed(0, 25);
                    break;
                default:
                    break;
            }
        }
    };
    /*public void setArg(List<MovingObj> movingObjs, double arg_ratio, int arg_height, int arg_width) {
        newList = Collections.synchronizedList(new ArrayList<>(movingObjs));
        oldList = Collections.synchronizedList(new ArrayList<>(movingObjs));
        height = arg_height;
        width = arg_width;
        ratio = arg_ratio;
    }

    public void reset(final List<MovingObj> latest) {
        oldList = Collections.synchronizedList(new ArrayList<>(newList));
        newList = Collections.synchronizedList(new ArrayList<>(latest));
    }*/
}

