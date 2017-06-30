package com.example.vpl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MashPlant on 2016/12/30.
 */

public class SquareWaveConstructDialog extends Dialog {
    Context mContext;
    Activity mActivity;
    private ObservableListView listView;
    private SettingAdapter adapter;
    private List<SettingItem> settingItemList = new ArrayList<>();
    public static Button add;
    public static Button ensure;
    private KeyboardUtil keyboardUtil;
    public FinishCallBack mFinishCallBack;
    final String TAG = "my";

    public interface FinishCallBack {
        void onFinish(Calculator calculator);
    }

    public SquareWaveConstructDialog(Context context, Activity activity, FinishCallBack finishCallBack) {
        super(context);
        mContext = context;
        mActivity = activity;
        mFinishCallBack = finishCallBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        setTitle("Enter SquareWave Function");
        initSettingItem();
        adapter = new SettingAdapter(mContext, R.layout.set_item, settingItemList);
        listView = (ObservableListView) findViewById(R.id.set_list_view);
        add = (Button) findViewById(R.id.button_cancel);
        add.setText("添加");
        ensure = (Button) findViewById(R.id.button_ensure);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.insert(new SettingItem("0.0", "1.0", SettingItem.SWINPUT),settingItemList.size()-1);
                adapter.notifyDataSetChanged();
            }
        });

        ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("my", "onClick: ");
                ArrayList<Calculator> exps = new ArrayList<>();
                ArrayList<Double> times = new ArrayList<>();
                double T = 0;
                for (int i = 0; i < settingItemList.size(); i++) {
                    LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                    EditText time = (EditText) layout.findViewById(R.id.input);
                    String str_time = time.getText().toString();
                    str_time = TextUtils.isEmpty(str_time) ? time.getHint().toString() : str_time;
                    try {
                        if (i != settingItemList.size() - 1) {
                            EditText exp = (EditText) layout.findViewById(R.id.sw_input);
                            String str_exp = exp.getText().toString();
                            str_exp = TextUtils.isEmpty(str_exp) ? exp.getHint().toString() : str_exp;
                            exps.add(new Calculator(str_exp));
                            times.add(Calculator.resultOf(str_time));
                        } else {
                            T = Calculator.resultOf(str_time);
                        }
                    } catch (Exception e) {
                        Toast.makeText(mActivity, "请输入合法实数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Calculator calculator = new Calculator("sWT");
                calculator.constructSquareWave(exps, times, T);
                mFinishCallBack.onFinish(calculator);
                dismiss();
                ensure = null;
                add = null;
            }
        });
        listView.setAdapter(adapter);
        final Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        lp.width = d.widthPixels;
        lp.height = (int) (d.heightPixels * 0.85f);
        dialogWindow.setAttributes(lp);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // LinearLayout layout = (LinearLayout) (listView.getChildAt(position - listView.getFirstVisiblePosition()));
                switch (settingItemList.get(position).getKind()) {
                    case SettingItem.INPUT:
                    case SettingItem.SWINPUT:
                        keyboardUtil.showKeyboard(position - listView.getFirstVisiblePosition());
                        break;
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //adapter.remove(adapter.getItem(position));
                Log.d(TAG, "onItemLongClick: "+position);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        keyboardUtil = new KeyboardUtil(mContext, this, listView, settingItemList);
    }

    private void initSettingItem() {
        settingItemList.add(new SettingItem("10.0", "0.5", SettingItem.SWINPUT));
        settingItemList.add(new SettingItem("0.0", "1.0", SettingItem.SWINPUT));
        settingItemList.add(new SettingItem("周期T", "1.0", SettingItem.INPUT));
    }
}
