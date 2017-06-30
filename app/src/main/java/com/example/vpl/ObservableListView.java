package com.example.vpl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by MashPlant on 2016/7/17.
 */
public class ObservableListView extends ListView {

    private float startY;


    public interface OnObserveListener {
        void onUp(float offsetY);

        void onDown(float offsetY);
    }

    private OnObserveListener l;

    public void setOnObserveListener(OnObserveListener arg) {
        l = arg;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (l != null) {
                    if (event.getY() - startY <= 0) {
                        l.onUp(event.getY() - startY);
                    } else {
                        l.onDown(event.getY() - startY);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public ObservableListView(Context context) {
        super(context);
        //init();
    }

    public ObservableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public ObservableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }
}
