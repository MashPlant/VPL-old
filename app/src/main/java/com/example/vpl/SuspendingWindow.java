package com.example.vpl;

/**
 * Created by MashPlant on 2017/1/14.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SuspendingWindow extends LinearLayout {
    public SuspendingWindow(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        this.setLayoutParams( new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        View view = LayoutInflater.from(context).inflate(
                R.layout.suspend_layout, null);
        this.addView(view);
    }
}
