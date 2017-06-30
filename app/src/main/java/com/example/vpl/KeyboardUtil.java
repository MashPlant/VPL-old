package com.example.vpl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import java.util.List;


/**
 * Created by MashPlant on 2016/5/28.
 */

/*bug修复日志2017.1.24
修复键盘相关bug
1.handler内判断next是否为非输入item时应判断hint是否为null.因TextView永不为null
2.快点击时闪退:确定显示关闭动画时将clickable置为false
* */
public class KeyboardUtil implements View.OnClickListener {
    TableLayout tableLayout;
    int currPos = 0;
    private boolean clickable=true;
    ListView listView;
    EditText editText;
    Context mContext;
    Activity mActivity;
    List<SettingItem> settingItemList;

    TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

    public boolean isRunning() {
        return tableLayout.getVisibility() == View.VISIBLE;
    }

    public void hide() {
        tableLayout.startAnimation(mHiddenAction);
        clickable=false;
        handler.sendEmptyMessageDelayed(1, 100);
        delete('|');
        editText = null;
        return;
    }

    public KeyboardUtil(Context context, Dialog view, ListView listView, List<SettingItem> settingItemList) {
        this.settingItemList = settingItemList;
        mContext = context;
        tableLayout = (TableLayout) view.findViewById(R.id.keyboard);
        this.listView = listView;
        mShowAction.setDuration(100);
        mHiddenAction.setDuration(100);
        view.findViewById(R.id.num_t).setOnClickListener(this);
        view.findViewById(R.id.num1).setOnClickListener(this);
        view.findViewById(R.id.num2).setOnClickListener(this);
        view.findViewById(R.id.num3).setOnClickListener(this);
        view.findViewById(R.id.num4).setOnClickListener(this);
        view.findViewById(R.id.num5).setOnClickListener(this);
        view.findViewById(R.id.num6).setOnClickListener(this);
        view.findViewById(R.id.num7).setOnClickListener(this);
        view.findViewById(R.id.num8).setOnClickListener(this);
        view.findViewById(R.id.num9).setOnClickListener(this);
        view.findViewById(R.id.num0).setOnClickListener(this);
        view.findViewById(R.id.num_pi).setOnClickListener(this);
        view.findViewById(R.id.opt_point).setOnClickListener(this);
        view.findViewById(R.id.opt_plus).setOnClickListener(this);
        view.findViewById(R.id.opt_multiply).setOnClickListener(this);
        view.findViewById(R.id.opt_minus).setOnClickListener(this);
        view.findViewById(R.id.opt_divide).setOnClickListener(this);
        view.findViewById(R.id.opt_pow).setOnClickListener(this);
        view.findViewById(R.id.opt_point).setOnClickListener(this);
        view.findViewById(R.id.opt_sqrt).setOnClickListener(this);
        view.findViewById(R.id.opt_sin).setOnClickListener(this);
        view.findViewById(R.id.opt_cos).setOnClickListener(this);
        view.findViewById(R.id.opt_bracket).setOnClickListener(this);
        view.findViewById(R.id.opt_right).setOnClickListener(this);
        view.findViewById(R.id.opt_left).setOnClickListener(this);
        view.findViewById(R.id.opt_sw).setOnClickListener(this);
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.done).setOnClickListener(this);
        view.findViewById(R.id.next).setOnClickListener(this);
    }

    public KeyboardUtil(Activity activity, ListView listView, List<SettingItem> settingItemList) {
        this.settingItemList = settingItemList;
        mContext = activity;
        mActivity = activity;
        tableLayout = (TableLayout) activity.findViewById(R.id.keyboard);
        this.listView = listView;
        mShowAction.setDuration(100);
        mHiddenAction.setDuration(100);
        activity.findViewById(R.id.num_t).setOnClickListener(this);
        activity.findViewById(R.id.num1).setOnClickListener(this);
        activity.findViewById(R.id.num2).setOnClickListener(this);
        activity.findViewById(R.id.num3).setOnClickListener(this);
        activity.findViewById(R.id.num4).setOnClickListener(this);
        activity.findViewById(R.id.num5).setOnClickListener(this);
        activity.findViewById(R.id.num6).setOnClickListener(this);
        activity.findViewById(R.id.num7).setOnClickListener(this);
        activity.findViewById(R.id.num8).setOnClickListener(this);
        activity.findViewById(R.id.num9).setOnClickListener(this);
        activity.findViewById(R.id.num0).setOnClickListener(this);
        activity.findViewById(R.id.num_pi).setOnClickListener(this);
        activity.findViewById(R.id.opt_point).setOnClickListener(this);
        activity.findViewById(R.id.opt_plus).setOnClickListener(this);
        activity.findViewById(R.id.opt_multiply).setOnClickListener(this);
        activity.findViewById(R.id.opt_minus).setOnClickListener(this);
        activity.findViewById(R.id.opt_divide).setOnClickListener(this);
        activity.findViewById(R.id.opt_pow).setOnClickListener(this);
        activity.findViewById(R.id.opt_point).setOnClickListener(this);
        activity.findViewById(R.id.opt_sqrt).setOnClickListener(this);
        activity.findViewById(R.id.opt_sin).setOnClickListener(this);
        activity.findViewById(R.id.opt_cos).setOnClickListener(this);
        activity.findViewById(R.id.opt_bracket).setOnClickListener(this);
        activity.findViewById(R.id.opt_right).setOnClickListener(this);
        activity.findViewById(R.id.opt_left).setOnClickListener(this);
        activity.findViewById(R.id.opt_sw).setOnClickListener(this);
        activity.findViewById(R.id.back).setOnClickListener(this);
        activity.findViewById(R.id.done).setOnClickListener(this);
        activity.findViewById(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!clickable)
            return;
        Editable editable = editText.getText();
        int start = editText.getSelectionStart();
        switch (v.getId()) {
            case R.id.num_t:
                editable.insert(start, "T");
                break;
            case R.id.num0:
                editable.insert(start, "0");
                break;
            case R.id.num1:
                editable.insert(start, "1");
                break;
            case R.id.num2:
                editable.insert(start, "2");
                break;
            case R.id.num3:
                editable.insert(start, "3");
                break;
            case R.id.num4:
                editable.insert(start, "4");
                break;
            case R.id.num5:
                editable.insert(start, "5");
                break;
            case R.id.num6:
                editable.insert(start, "6");
                break;
            case R.id.num7:
                editable.insert(start, "7");
                break;
            case R.id.num8:
                editable.insert(start, "8");
                break;
            case R.id.num9:
                editable.insert(start, "9");
                break;
            case R.id.num_pi:
                editable.insert(start, "pi");
                break;
            case R.id.opt_sw:
                new SquareWaveConstructDialog(mContext, mActivity, new SquareWaveConstructDialog.FinishCallBack() {
                    @Override
                    public void onFinish(Calculator result) {
                        AddObjActivity.squareWaveCalculators[currPos] = result;
                    }
                }).show();
                break;
            case R.id.opt_sqrt:
                editable.insert(start, "√");
                break;
            case R.id.opt_plus:
                editable.insert(start, "+");
                break;
            case R.id.opt_minus:
                editable.insert(start, "-");
                break;
            case R.id.opt_multiply:
                editable.insert(start, "*");
                break;
            case R.id.opt_divide:
                editable.insert(start, "/");
                break;
            case R.id.opt_pow:
                editable.insert(start, "^");
                break;
            case R.id.opt_point:
                editable.insert(start, ".");
                break;
            case R.id.opt_bracket:
                editable.insert(start, "()");
                editText.setSelection(editText.getSelectionStart() - 1);
                break;
            case R.id.opt_sin:
                editable.insert(start, "sin()");
                editText.setSelection(editText.getSelectionStart() - 1);
                break;
            case R.id.opt_cos:
                editable.insert(start, "cos()");
                editText.setSelection(editText.getSelectionStart() - 1);
                break;
            case R.id.done:
                hide();
                return;
            case R.id.next:
                if (settingItemList.get(currPos).getKind() == SettingItem.INPUT || inputIndex == 2) {
                    inputIndex = 1;
                    if (showKeyboard(currPos + 1 - listView.getFirstVisiblePosition())) {
                        hide();
                        return;
                    }
                } else {
                    inputIndex = 2;
                    if (showKeyboard(currPos - listView.getFirstVisiblePosition())) {
                        hide();
                        return;
                    }
                }
                break;
            case R.id.back:
                if (start > 1)
                    editable.delete(start - 2, start - 1);
                break;
            case R.id.opt_left:
                if (start > 1) {
                    editable.delete(start - 1, start);
                    editable.insert(editText.getSelectionStart() - 1, "|");
                    editText.setSelection(editText.getSelectionStart() - 1);
                }
                break;
            case R.id.opt_right:
                if (start < editText.length()) {
                    editText.setSelection(editText.getSelectionStart() + 1);
                }
                break;
        }
        if (editText.getSelectionStart() > start) {
            delete('|');
            editable.insert(editText.getSelectionStart(), "|");
        }
    }

    void delete(char arg) {
        Editable editable = editText.getText();
        for (int i = 0; i < editable.length(); i++) {
            if (editable.charAt(i) == arg) {
                editable.delete(i, i + 1);
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    LinearLayout linearLayout = (LinearLayout) listView.getChildAt(currPos - listView.getFirstVisiblePosition());
                    if (linearLayout != null) {
                        EditText editText1 = (EditText) linearLayout.findViewById(R.id.input);
                        if (!editText1.getHint().toString().equals("null")) {
                            editText = (EditText) linearLayout.findViewById(R.id.input);
                            editText.getText().insert(editText.getSelectionStart(), "|");
                            tableLayout.setVisibility(View.VISIBLE);
                        } else {
                            tableLayout.startAnimation(mHiddenAction);//隐藏过程中点击可能error
                            clickable=false;
                            handler.sendEmptyMessageDelayed(1, 100);
                            delete('|');
                            editText = null;

                        }
                    } else {
                        sendEmptyMessage(0);
                    }
                    break;
                case 1:
                    tableLayout.setVisibility(View.GONE);
                    if (SquareWaveConstructDialog.ensure != null) {
                        SquareWaveConstructDialog.ensure.setVisibility(View.VISIBLE);
                        SquareWaveConstructDialog.add.setVisibility(View.VISIBLE);
                    } else {
                        AddObjActivity.ensure.setVisibility(View.VISIBLE);
                        AddObjActivity.cancel.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    listView.smoothScrollToPosition(currPos - listView.getFirstVisiblePosition());
                    break;
            }
        }
    };
    int inputIndex = 1;

    public boolean showKeyboard(int position) {
        clickable=true;
        currPos = position + listView.getFirstVisiblePosition();
        if (editText != null) {
            delete('|');
        }
        if (currPos >= listView.getCount())
            return true;
        listView.smoothScrollToPosition(currPos);
        handler.sendEmptyMessageDelayed(2, 5);
        LinearLayout linearLayout = (LinearLayout) listView.getChildAt(position);
        if (linearLayout == null) {
            handler.sendEmptyMessage(0);
            return false;
        }
        EditText editText1 = (EditText) linearLayout.findViewById(R.id.input);
        if (editText1.getHint().toString().equals("null")) {
            return true;
        }
        if (settingItemList.get(currPos).getKind() == SettingItem.INPUT) {
            editText = (EditText) linearLayout.findViewById(R.id.input);

        } else {
            switch (inputIndex) {
                case 1:
                    editText = (EditText) linearLayout.findViewById(R.id.sw_input);
                    break;
                case 2:
                    editText = (EditText) linearLayout.findViewById(R.id.input);
                    break;
            }
        }
        editText.getText().insert(editText.getSelectionStart(), "|");
        AddObjActivity.ensure.setVisibility(View.GONE);
        AddObjActivity.cancel.setVisibility(View.GONE);
        if (SquareWaveConstructDialog.ensure != null) {
            SquareWaveConstructDialog.ensure.setVisibility(View.GONE);
            SquareWaveConstructDialog.add.setVisibility(View.GONE);
        }
        if (tableLayout.getVisibility() == View.GONE)
            tableLayout.startAnimation(mShowAction);
        tableLayout.setVisibility(View.VISIBLE);
        return false;
    }

}
