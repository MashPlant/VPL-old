package com.example.vpl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by MashPlant on 2016/4/16.
 */
public class AddObjActivity extends AppCompatActivity {
    private ObservableListView listView;
    private List<SettingItem> settingItemList = new ArrayList<>();
    private SettingAdapter adapter;
    static int kind = 0;
    static VPLFrame vplFrame = MainActivity.vplFrame;
    private KeyboardUtil keyboardUtil;
    Double[] values = new Double[10];
    static Button ensure, cancel;
    static final int PHOTO_REQUEST_GALLERY = 1;
    static final int PHOTO_REQUEST_CUT = 2;
    private Bitmap bitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                // 裁剪图片意图
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(uri, "image/*");
                intent.putExtra("crop", "true");

                // 裁剪框的比例，1：1
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                // 裁剪后输出图片的尺寸大小
                intent.putExtra("outputX", 250);
                intent.putExtra("outputY", 250);

                // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
                startActivityForResult(intent, PHOTO_REQUEST_CUT);
            }
        }
        if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                bitmap = data.getParcelableExtra("data");
            }
        }

    }

    private boolean hasT(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == 'T') {
                return true;
            }
        }
        return false;
    }

    public static Calculator[] squareWaveCalculators;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        squareWaveCalculators = new Calculator[20];
        switch (kind) {
            case VPLFrame.MOVINGOBJ:
                setTitle("运动对象");
                break;
            case VPLFrame.G_CENTER:
                setTitle("引力质心");
                break;
            case VPLFrame.E_CENTER:
                setTitle("点电荷");
                break;
            case VPLFrame.E_FIELD:
                setTitle("匀强电场");
                break;
            case VPLFrame.M_FIELD:
                setTitle("匀强磁场");
                break;
            case VPLFrame.CIRCLE_M_FIELD:
                setTitle("圆形匀强磁场");
                break;
            case VPLFrame.DAMP:
                setTitle("阻尼介质");
                break;
            case VPLFrame.ROPE:
                setTitle("弹性轻绳");
                break;
            case VPLFrame.POLE:
                setTitle("轻质硬杆");
                break;
            case VPLFrame.SPRING:
                setTitle("轻质弹簧");
                break;
            case VPLFrame.LINE_TRACK:
                setTitle("直线轨道");
                break;
            case VPLFrame.CIRCLE_TRACK:
                setTitle("圆弧轨道");
                break;
            case VPLFrame.PARTICLE_GENERATOR:
                setTitle("粒子发生器");
                break;
            case VPLFrame.SETTING:
                setTitle("通用设置");
                break;
        }
        setContentView(R.layout.setting_layout);
        initSettingItem();
        adapter = new SettingAdapter(AddObjActivity.this, R.layout.set_item, settingItemList);
        listView = (ObservableListView) findViewById(R.id.set_list_view);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                for (int i = listView.getFirstVisiblePosition(); i <= listView.getLastVisiblePosition(); i++) {
                    if (adapter.getItem(i).getKind() == SettingItem.INPUT) {
                        final int position = i - listView.getFirstVisiblePosition();
                        listView.getChildAt(position).findViewById(R.id.input).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                keyboardUtil.showKeyboard(position);
                            }
                        });
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (int i = 0; i < listView.getChildCount(); i++) {
                    if (adapter.getItem(i + listView.getFirstVisiblePosition()).getKind() == SettingItem.CHOOSE) {
                        if (!TextUtils.isEmpty(adapter.datas[i + listView.getFirstVisiblePosition()]))
                            ((Switch) listView.getChildAt(i).findViewById(R.id.choose)).setChecked(Boolean.valueOf(adapter.datas[i + listView.getFirstVisiblePosition()]));
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout layout = (LinearLayout) (listView.getChildAt(position - listView.getFirstVisiblePosition()));
                switch (settingItemList.get(position).getKind()) {
                    case SettingItem.CHOOSE:
                        Switch s = (Switch) layout.findViewById(R.id.choose);
                        s.setChecked(!s.isChecked());
                        s.callOnClick();
                        break;
                    case SettingItem.INPUT:
                        keyboardUtil.showKeyboard(position - listView.getFirstVisiblePosition());
                        break;
                    case SettingItem.NOTHING:
                        if (kind == VPLFrame.MOVINGOBJ) {
                            if (position - listView.getFirstVisiblePosition() == 7) {
                                new ColorPickerDialog(AddObjActivity.this, new ColorPickerDialog.OnColorChangedListener() {
                                    @Override
                                    public void colorChanged(int color) {
                                        chosenColor = color;
                                    }
                                }, chosenColor).show();
                            } else {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
                                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                            }
                        }
                        break;
                }
                if (kind == VPLFrame.POLE || kind == VPLFrame.SPRING || kind == VPLFrame.ROPE) {
                    if (position == 2) {
                        if (vplFrame.getMovingObjList().size() >= 2) {
                            vplFrame.isChoosingConnectObj = true;
                            MainActivity.removeAllFab();
                            MainActivity.ensure_choose.setVisibility(View.VISIBLE);
                            finish();
                        }
                    }
                }
                if (kind==VPLFrame.PARTICLE_GENERATOR){
                    if (position==1){
                        if (vplFrame.getMovingObjList().size() >= 1) {
                            vplFrame.isChoosingGenerateObj = true;
                            MainActivity.removeAllFab();
                            MainActivity.ensure_choose.setVisibility(View.VISIBLE);
                            finish();
                        }
                    }
                }
            }
        });
        keyboardUtil = new KeyboardUtil(this, listView, settingItemList);
        handler.sendEmptyMessageDelayed(0, 10);

        ensure = (Button) findViewById(R.id.button_ensure);
        cancel = (Button) findViewById(R.id.button_cancel);
        if (cancel != null)
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (vplFrame.selectedKind == -1) {
                        Intent intent = new Intent(AddObjActivity.this, ChooseObjActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            });
        final ArrayList<Integer> variables = new ArrayList<>();
        final ArrayList<Calculator> exps = new ArrayList<>();
        if (ensure != null)
            ensure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (kind) {
                        case VPLFrame.MOVINGOBJ: {
                            for (int i = 0; i < settingItemList.size() - 2; i++) {
                                EditText editText = (EditText) listView.getAdapter().getView(i, null, null).findViewById(R.id.input);
                                String string = adapter.datas[i];
                                string = TextUtils.isEmpty(string) ? editText.getHint().toString() : string;
                                try {
                                    boolean addVar = false;
                                    if (squareWaveCalculators[i] != null) {
                                        values[i] = 1.0;
                                        exps.add(squareWaveCalculators[i]);
                                        squareWaveCalculators[i] = null;
                                        addVar = true;
                                    } else {
                                        values[i] = Calculator.resultOf(string);
                                    }
                                    if (addVar || hasT(string)) {
                                        if (!addVar)
                                            exps.add(new Calculator(string));
                                        switch (i) {
                                            case 0:
                                                variables.add("x".hashCode());
                                                break;
                                            case 1:
                                                variables.add("y".hashCode());
                                                break;
                                            case 2:
                                                variables.add("vx".hashCode());
                                                break;
                                            case 3:
                                                variables.add("vy".hashCode());
                                                break;
                                            case 4:
                                                variables.add("radius".hashCode());
                                                break;
                                            case 5:
                                                variables.add("m".hashCode());
                                                break;
                                            case 6:
                                                variables.add("q".hashCode());
                                                break;
                                        }
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (values[5] <= 0) {
                                Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            VPLFrame.MovingObj movingObj = vplFrame.new MovingObj(values[0], values[1], values[2], values[3], values[4], values[5], values[6], chosenColor);
                            movingObj.variables = variables;
                            movingObj.exps = exps;
                            if (bitmap != null) {
                                movingObj.setBitmap(bitmap);
                            }
                            if (vplFrame.selectedKind == VPLFrame.MOVINGOBJ) {
                                vplFrame.getMovingObjList().set(vplFrame.selectedPos, movingObj);
                            } else {
                                vplFrame.addMovingObj(movingObj);
                            }

                            finish();
                            break;
                        }
                        case VPLFrame.G_CENTER:
                        case VPLFrame.E_CENTER: {
                            for (int i = 0; i < settingItemList.size(); i++) {
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (kind == VPLFrame.G_CENTER) {
                                if (values[2] < 0) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (vplFrame.selectedKind == VPLFrame.G_CENTER) {
                                    vplFrame.getForceCenterList().set(vplFrame.selectedPos, vplFrame.new ForceCenter(values[0], values[1], values[2], VPLFrame.G_CENTER));
                                } else {
                                    vplFrame.addForceCenter(vplFrame.new ForceCenter(values[0], values[1], values[2], VPLFrame.G_CENTER));
                                }
                            } else {
                                if (vplFrame.selectedKind == VPLFrame.E_CENTER) {
                                    vplFrame.getForceCenterList().set(vplFrame.selectedPos, vplFrame.new ForceCenter(values[0], values[1], values[2], VPLFrame.E_CENTER));
                                } else {
                                    vplFrame.addForceCenter(vplFrame.new ForceCenter(values[0], values[1], values[2], VPLFrame.E_CENTER));
                                }
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.E_FIELD:
                        case VPLFrame.M_FIELD: {
                            for (int i = 0; i < settingItemList.size(); i++) {
                                if (adapter.getItem(i).getKind() != SettingItem.INPUT)
                                    continue;
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                string = string.isEmpty() ? editText.getHint().toString() : string;
                                try {
                                    boolean addVar = false;
                                    if (squareWaveCalculators[i] != null) {
                                        values[i] = 1.0;
                                        exps.add(squareWaveCalculators[i]);
                                        squareWaveCalculators[i] = null;
                                        addVar = true;
                                    } else {
                                        values[i] = Calculator.resultOf(string);
                                    }
                                    if (addVar || hasT(string)) {
                                        if (!addVar)
                                            exps.add(new Calculator(string));
                                        switch (i) {
                                            case 0:
                                                variables.add("x1".hashCode());
                                                break;
                                            case 1:
                                                variables.add("y1".hashCode());
                                                break;
                                            case 2:
                                                variables.add("x2".hashCode());
                                                break;
                                            case 3:
                                                variables.add("y2".hashCode());
                                                break;
                                            case 4:
                                                variables.add("value".hashCode());
                                                break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (values[0].equals(values[1]) && values[0].equals(values[2]) && values[0].equals(values[3])) {
                                Toast.makeText(AddObjActivity.this, "请输入合法边界", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            LinearLayout layout = (LinearLayout) (listView.getChildAt(settingItemList.size() - 1));
                            Switch choose;
                            boolean getChecked = false;
                            if (layout != null) {
                                choose = (Switch) layout.findViewById(R.id.choose);
                                getChecked = choose.isChecked();
                            }
                            if (kind == VPLFrame.E_FIELD) {
                                VPLFrame.Field field = vplFrame.new Field(values[0], values[1], values[2], values[3], values[4], VPLFrame.E_FIELD, getChecked ? VPLFrame.Field.HORIZONTAL : VPLFrame.Field.VERTICAL);
                                field.variables = variables;
                                field.exps = exps;
                                if (vplFrame.selectedKind == VPLFrame.E_FIELD) {
                                    vplFrame.getFieldList().set(vplFrame.selectedPos, field);
                                } else {
                                    vplFrame.addField(field);
                                }
                            } else {
                                VPLFrame.Field field = vplFrame.new Field(values[0], values[1], values[2], values[3], values[4], VPLFrame.M_FIELD, 0);
                                field.variables = variables;
                                field.exps = exps;
                                if (vplFrame.selectedKind == VPLFrame.M_FIELD) {
                                    vplFrame.getFieldList().set(vplFrame.selectedPos, field);
                                } else {
                                    vplFrame.addField(field);
                                }
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.CIRCLE_M_FIELD: {
                            for (int i = 0; i < settingItemList.size(); i++) {
                                if (adapter.getItem(i).getKind() != SettingItem.INPUT)
                                    continue;
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                string = string.isEmpty() ? editText.getHint().toString() : string;
                                try {
                                    boolean addVar = false;
                                    if (squareWaveCalculators[i] != null) {
                                        values[i] = 1.0;
                                        exps.add(squareWaveCalculators[i]);
                                        squareWaveCalculators[i] = null;
                                        addVar = true;
                                    } else {
                                        values[i] = Calculator.resultOf(string);
                                    }
                                    if (addVar || hasT(string)) {
                                        if (!addVar)
                                            exps.add(new Calculator(string));
                                        switch (i) {
                                            case 0:
                                                variables.add("x1".hashCode());
                                                break;
                                            case 1:
                                                variables.add("y1".hashCode());
                                                break;
                                            case 2:
                                                variables.add("x2".hashCode());
                                                break;
                                            case 3:
                                                variables.add("value".hashCode());
                                                break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (values[2] < 0) {
                                Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            VPLFrame.Field field = vplFrame.new Field(values[0], values[1], values[2], values[3]);
                            field.variables = variables;
                            field.exps = exps;
                            if (vplFrame.selectedKind == VPLFrame.CIRCLE_M_FIELD) {
                                vplFrame.getFieldList().set(vplFrame.selectedPos, field);
                            } else {
                                vplFrame.addField(field);
                            }
                            finish();
                            break;
                        }

                        case VPLFrame.DAMP: {
                            for (int i = 0; i < settingItemList.size(); i++) {
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (values[0].equals(values[1]) && values[0].equals(values[2]) && values[0].equals(values[3])) {
                                Toast.makeText(AddObjActivity.this, "请输入合法边界", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (vplFrame.selectedKind == VPLFrame.DAMP) {
                                vplFrame.getFieldList().set(vplFrame.selectedPos, vplFrame.new Field(values[0], values[1], values[2], values[3], values[4], values[5]));
                            } else {
                                vplFrame.addField(vplFrame.new Field(values[0], values[1], values[2], values[3], values[4], values[5]));
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.SETTING: {
                            for (int i = 0; i < 6; i++) {
                                EditText editText = (EditText) listView.getAdapter().getView(i, null, null).findViewById(R.id.input);
                                String string = adapter.datas[i];
                                try {
                                    values[i] = Calculator.resultOf(TextUtils.isEmpty(string) ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            boolean[] chooses = new boolean[settingItemList.size()];
                            for (int i = 6; i < settingItemList.size(); i++) {
                                Switch s = (Switch) listView.getAdapter().getView(i, null, null).findViewById(R.id.choose);
                                String string = adapter.datas[i];
                                chooses[i] = TextUtils.isEmpty(string) ? s.isChecked() : Boolean.valueOf(string);
                            }
                            if (values[0] < 0 || values[1] < 0 || values[2] < 0 || values[3] < 0 || values[4] < 0 || values[5] <= 0) {
                                Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            vplFrame.G = values[0];
                            vplFrame.K = values[1];
                            vplFrame.g = values[2];
                            vplFrame.c = values[3];
                            vplFrame.timeGap = values[4] * 0.2;
                            vplFrame.ratio = values[5].floatValue();
                            vplFrame.doBetG = chooses[6];
                            vplFrame.doBetE = chooses[7];
                            vplFrame.doImpact = chooses[8];
                            vplFrame.hasBoundary = chooses[9];
                            vplFrame.doRelativity = chooses[10];
                            vplFrame.strongInteraction = chooses[11];
                            vplFrame.doImpactSound = chooses[12];
                            vplFrame.doDrawTrace = chooses[13];
                            vplFrame.doDynamicOri = chooses[14];
                            SharedPreferences.Editor editor = getSharedPreferences("datas", Activity.MODE_PRIVATE).edit();
                            editor.putString("G", Double.toString(vplFrame.G));
                            editor.putString("K", Double.toString(vplFrame.K));
                            editor.putString("g", Double.toString(vplFrame.g));
                            editor.putString("c", Double.toString(vplFrame.c));
                            editor.putString("timeGap", Double.toString(vplFrame.timeGap));
                            editor.putString("doBetG", Boolean.toString(vplFrame.doBetG));
                            editor.putString("doBetE", Boolean.toString(vplFrame.doBetE));
                            editor.putString("doImpact", Boolean.toString(vplFrame.doImpact));
                            editor.putString("hasBoundary", Boolean.toString(vplFrame.hasBoundary));
                            editor.putString("doRelativity", Boolean.toString(vplFrame.doRelativity));
                            editor.putString("doImpactSound", Boolean.toString(vplFrame.doImpactSound));
                            editor.putString("doDrawTrace", Boolean.toString(vplFrame.doDrawTrace));
                            editor.putString("doDynamicOri", Boolean.toString(vplFrame.doDynamicOri));
                            editor.apply();
                            finish();
                            break;
                        }
                        case VPLFrame.ROPE:
                        case VPLFrame.POLE: {
                            for (int i = 0; i < 2; i++) {
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请选择合法对象", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            int obj1 = values[0].intValue(), obj2 = values[1].intValue();
                            if (obj1 < 0 || obj2 < 0 || obj1 == obj2 || obj1 >= vplFrame.getMovingObjList().size() || obj2 >= vplFrame.getMovingObjList().size()) {
                                Toast.makeText(AddObjActivity.this, "请选择合法对象", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < vplFrame.getConnecterList().size(); i++) {
                                VPLFrame.Connecter connecter = vplFrame.getConnecterList().get(i);
                                if (kind == connecter.kind && (connecter.obj1 == obj1 && connecter.obj2 == obj2) || (connecter.obj1 == obj2 && connecter.obj2 == obj1)) {
                                    Toast.makeText(AddObjActivity.this, "对象之间已经存在该连接体", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (vplFrame.selectedKind == VPLFrame.POLE || vplFrame.selectedKind == VPLFrame.ROPE) {
                                vplFrame.getConnecterList().set(vplFrame.selectedPos, vplFrame.new Connecter(obj1, obj2, kind));
                            } else {
                                vplFrame.addConnecter(vplFrame.new Connecter(obj1, obj2, kind));
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.SPRING: {
                            for (int i = 0; i < settingItemList.size(); i++) {
                                if (i == 2)
                                    continue;
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    if (i <= 1) {
                                        Toast.makeText(AddObjActivity.this, "请选择合法对象", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                            }
                            int obj1 = values[0].intValue(), obj2 = values[1].intValue();
                            if (obj1 < 0 || obj2 < 0 || obj1 == obj2 || obj1 >= vplFrame.getMovingObjList().size() || obj2 >= vplFrame.getMovingObjList().size()) {
                                Toast.makeText(AddObjActivity.this, "请选择合法对象", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (vplFrame.selectedKind == VPLFrame.SPRING) {
                                vplFrame.getConnecterList().set(vplFrame.selectedPos, vplFrame.new Connecter(obj1, obj2, values[3], values[4], kind));
                            } else {
                                vplFrame.addConnecter(vplFrame.new Connecter(obj1, obj2, values[3], values[4], kind));
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.LINE_TRACK: {
                            for (int i = 0; i < 4; i++) {
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (vplFrame.selectedKind == VPLFrame.LINE_TRACK) {
                                vplFrame.getTrackList().set(vplFrame.selectedPos, vplFrame.new Track(values[0], values[1], values[2], values[3], ((Switch) (listView.getChildAt(4).findViewById(R.id.choose))).isChecked()));
                            } else {
                                vplFrame.addTrack(vplFrame.new Track(values[0], values[1], values[2], values[3], ((Switch) (listView.getChildAt(4).findViewById(R.id.choose))).isChecked()));
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.CIRCLE_TRACK: {
                            for (int i = 0; i < 5; i++) {
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (values[3] >= values[4] || values[3] < 0 || values[3] > 360 || values[4] < 0 || values[4] > 360) {
                                Toast.makeText(AddObjActivity.this, "角度不合法", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (vplFrame.selectedKind == VPLFrame.CIRCLE_TRACK) {
                                vplFrame.getTrackList().set(vplFrame.selectedPos, vplFrame.new Track(values[0], values[1], values[2], values[3], values[4], ((Switch) (listView.getChildAt(5).findViewById(R.id.choose))).isChecked()));
                            } else {
                                vplFrame.addTrack(vplFrame.new Track(values[0], values[1], values[2], values[3], values[4], ((Switch) (listView.getChildAt(5).findViewById(R.id.choose))).isChecked()));
                            }
                            finish();
                            break;
                        }
                        case VPLFrame.PARTICLE_GENERATOR:{
                            for (int i=0;i<6;i++){
                                if (i==2||i==1)
                                    continue;
                                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                                EditText editText = (EditText) layout.findViewById(R.id.input);
                                String string = editText.getText().toString();
                                try {
                                    values[i] = Calculator.resultOf(string.isEmpty() ? editText.getHint().toString() : string);
                                } catch (Exception e) {
                                    Toast.makeText(AddObjActivity.this, "请输入合法实数", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            boolean b=((Switch) (listView.getChildAt(5).findViewById(R.id.choose))).isChecked();
                            int i=values[0].intValue();
                            if (i<0||i>=vplFrame.getMovingObjList().size()){
                                Toast.makeText(AddObjActivity.this, "请选择合法对象", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            vplFrame.getGeneratorList().add(vplFrame.new ParticleGenerator(vplFrame.getMovingObjList().get(i).clone(),b ? VPLFrame.GENERATOR_TIME:VPLFrame.GENERATOR_TIME,values[3],values[4],values[5]));
                            finish();
                            break;
                        }
                    }
                }
            });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (keyboardUtil.isRunning()) {
                keyboardUtil.hide();
                return false;
            } else {
                if (vplFrame.selectedKind == -1) {
                    Intent intent = new Intent(AddObjActivity.this, ChooseObjActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (int i = listView.getFirstVisiblePosition(); i <= listView.getLastVisiblePosition(); i++) {
                if (adapter.getItem(i).getKind() == SettingItem.INPUT) {
                    final int position = i;
                    listView.getChildAt(i).findViewById(R.id.input).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            keyboardUtil.showKeyboard(position - listView.getFirstVisiblePosition());
                        }
                    });
                }
            }
        }
    };

    int chosenColor = 0;

    private void initSettingItem() {
        switch (kind) {
            case VPLFrame.MOVINGOBJ:
                bitmap = null;
                if (vplFrame.selectedKind == VPLFrame.MOVINGOBJ) {
                    VPLFrame.MovingObj movingObj = vplFrame.getMovingObjList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("初始位置x", movingObj.variables.indexOf(120) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(120)).getExp() : Double.toString(movingObj.x), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y", movingObj.variables.indexOf(121) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(121)).getExp() : Double.toString(movingObj.y), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("水平速度Vx", movingObj.variables.indexOf(3778) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(3778)).getExp() : Double.toString(movingObj.vx), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("竖直速度Vy", movingObj.variables.indexOf(3779) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(3779)).getExp() : Double.toString(movingObj.vy), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", movingObj.variables.indexOf(-938578798) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(-938578798)).getExp() : Double.toString(movingObj.radius), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("质量m", movingObj.variables.indexOf(109) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(109)).getExp() : Double.toString(movingObj.m), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电荷q", movingObj.variables.indexOf(113) != -1 ? movingObj.exps.get(movingObj.variables.indexOf(113)).getExp() : Double.toString(movingObj.q), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("颜色", Integer.toString(chosenColor = movingObj.paint.getColor()), SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("贴图", "1", SettingItem.NOTHING));
                    bitmap = movingObj.bitmap;
                } else {
                    settingItemList.add(new SettingItem("初始位置x", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("水平速度Vx", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("竖直速度Vy", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", "1", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("质量m", "1", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电荷q", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("颜色", Integer.toString(chosenColor = new Random().nextInt(255 * 255 * 255)), SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("贴图", "1", SettingItem.NOTHING));
                }
                break;
            case VPLFrame.G_CENTER:
            case VPLFrame.E_CENTER:
                if (vplFrame.selectedKind == VPLFrame.G_CENTER || vplFrame.selectedKind == VPLFrame.E_CENTER) {
                    VPLFrame.ForceCenter forceCenter = vplFrame.getForceCenterList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("初始位置x", Double.toString(forceCenter.x), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y", Double.toString(forceCenter.y), SettingItem.INPUT));
                    settingItemList.add(new SettingItem(kind == VPLFrame.E_CENTER ? "电荷q" : "质量m", Double.toString(forceCenter.value), SettingItem.INPUT));
                } else {
                    settingItemList.add(new SettingItem("初始位置x", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem(kind == VPLFrame.E_CENTER ? "电荷q" : "质量m", "1", SettingItem.INPUT));
                }
                break;
            case VPLFrame.E_FIELD:
                if (vplFrame.selectedKind == VPLFrame.E_FIELD) {
                    VPLFrame.Field field = vplFrame.getFieldList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("初始位置x1", Double.toString(field.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", Double.toString(field.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", Double.toString(field.x2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", Double.toString(field.y2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电场强度E", field.variables.indexOf(111972721) != -1 ? field.exps.get(field.variables.indexOf(111972721)).getExp() : Double.toString(field.value), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电场方向", "横|竖|" + Boolean.toString(field.direction == VPLFrame.Field.HORIZONTAL), SettingItem.CHOOSE));
                } else {
                    settingItemList.add(new SettingItem("初始位置x1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电场强度E", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("电场方向", "横|竖|false", SettingItem.CHOOSE));
                }
                break;
            case VPLFrame.M_FIELD:
                if (vplFrame.selectedKind == VPLFrame.M_FIELD) {
                    VPLFrame.Field field = vplFrame.getFieldList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("初始位置x1", Double.toString(field.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", Double.toString(field.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", Double.toString(field.x2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", Double.toString(field.y2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("磁感应强度B", field.variables.indexOf(111972721) != -1 ? field.exps.get(field.variables.indexOf(111972721)).getExp() : Double.toString(field.value), SettingItem.INPUT));
                } else {
                    settingItemList.add(new SettingItem("初始位置x1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("磁感应强度B", "1", SettingItem.INPUT));
                }
                break;
            case VPLFrame.CIRCLE_M_FIELD: {
                if (vplFrame.selectedKind == VPLFrame.CIRCLE_M_FIELD) {
                    VPLFrame.Field field = vplFrame.getFieldList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("圆心横坐标x", Double.toString(field.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("圆心纵坐标y", Double.toString(field.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", Double.toString(field.x2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("磁感应强度B", field.variables.indexOf(111972721) != -1 ? field.exps.get(field.variables.indexOf(111972721)).getExp() : Double.toString(field.value), SettingItem.INPUT));
                } else {
                    settingItemList.add(new SettingItem("圆心横坐标x", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("圆心纵坐标y", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("磁感应强度B", "1", SettingItem.INPUT));
                }
                break;
            }
            case VPLFrame.DAMP:
                if (vplFrame.selectedKind == VPLFrame.DAMP) {
                    VPLFrame.Field field = vplFrame.getFieldList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("初始位置x1", Double.toString(field.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", Double.toString(field.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", Double.toString(field.x2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", Double.toString(field.y2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("阻尼系数", Double.toString(field.value), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("关于速度n次方", Double.toString(field.coefficient), SettingItem.INPUT));
                } else {
                    settingItemList.add(new SettingItem("初始位置x1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置x2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("初始位置y2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("阻尼系数", "1", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("关于速度n次方", "1", SettingItem.INPUT));
                }
                break;
            case VPLFrame.SETTING:
                settingItemList.add(new SettingItem("万有引力常量G", Double.toString(vplFrame.G), SettingItem.INPUT));
                settingItemList.add(new SettingItem("静电力常量K", Double.toString(vplFrame.K), SettingItem.INPUT));
                settingItemList.add(new SettingItem("重力加速度g", Double.toString(vplFrame.g), SettingItem.INPUT));
                settingItemList.add(new SettingItem("真空光速c", Double.toString(vplFrame.c), SettingItem.INPUT));
                settingItemList.add(new SettingItem("运行速度", Double.toString(vplFrame.timeGap / 0.2), SettingItem.INPUT));
                settingItemList.add(new SettingItem("绘图比例", Double.toString(vplFrame.ratio), SettingItem.INPUT));
                settingItemList.add(new SettingItem("考虑对象间引力", " | |" + vplFrame.doBetG, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("考虑对象间电场力", " | |" + vplFrame.doBetE, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("考虑对象间碰撞", " | |" + vplFrame.doImpact, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("考虑边界", " | |" + vplFrame.hasBoundary, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("考虑相对论", " | |" + vplFrame.doRelativity, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("强相互作用力(实验功能)", " | |" + vplFrame.strongInteraction, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("碰撞音效", " | |" + vplFrame.doImpactSound, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("绘制轨迹", " | |" + vplFrame.doDrawTrace, SettingItem.CHOOSE));
                settingItemList.add(new SettingItem("动态重力方向", " | |" + vplFrame.doDynamicOri, SettingItem.CHOOSE));
                break;
            case VPLFrame.ROPE:
            case VPLFrame.POLE:
                if (vplFrame.isChoosingConnectObj) {
                    vplFrame.isChoosingConnectObj = false;
                    settingItemList.add(new SettingItem("连接对象1", Integer.toString(vplFrame.chooseObj1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("连接对象2", Integer.toString(vplFrame.chooseObj2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                } else {
                    if (vplFrame.selectedKind == VPLFrame.ROPE || vplFrame.selectedKind == VPLFrame.POLE) {
                        VPLFrame.Connecter connecter = vplFrame.getConnecterList().get(vplFrame.selectedPos);
                        settingItemList.add(new SettingItem("连接对象1", Integer.toString(connecter.obj1), SettingItem.INPUT));
                        settingItemList.add(new SettingItem("连接对象2", Integer.toString(connecter.obj2), SettingItem.INPUT));
                        settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    } else {
                        settingItemList.add(new SettingItem("连接对象1", "0", SettingItem.INPUT));
                        settingItemList.add(new SettingItem("连接对象2", "1", SettingItem.INPUT));
                        settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    }
                }
                break;
            case VPLFrame.PARTICLE_GENERATOR:
                if (vplFrame.isChoosingGenerateObj) {
                    vplFrame.isChoosingGenerateObj = false;
                    settingItemList.add(new SettingItem("发射粒子编号", Integer.toString(vplFrame.chooseObj1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("类型", " | |false", SettingItem.CHOOSE));
                    settingItemList.add(new SettingItem("起始", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("发射间隔", "0", SettingItem.INPUT));
                } else if (vplFrame.selectedKind == VPLFrame.PARTICLE_GENERATOR) {
                   /* VPLFrame.ParticleGenerator generator=vplFrame.getGeneratorList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("发射粒子编号", Integer.toString(), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("起始", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("发射间隔", "0", SettingItem.INPUT));*/
                } else {
                    settingItemList.add(new SettingItem("发射粒子编号", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("类型", " | |false", SettingItem.CHOOSE));
                    settingItemList.add(new SettingItem("起始", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("发射间隔", "0", SettingItem.INPUT));
                }
                break;
            case VPLFrame.SPRING:
                if (vplFrame.isChoosingConnectObj) {
                    vplFrame.isChoosingConnectObj = false;
                    settingItemList.add(new SettingItem("连接对象1", Integer.toString(vplFrame.chooseObj1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("连接对象2", Integer.toString(vplFrame.chooseObj2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                    settingItemList.add(new SettingItem("劲度系数k", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("弹簧长度(0为默认)", "0", SettingItem.INPUT));
                } else {
                    if (vplFrame.selectedKind == VPLFrame.SPRING) {
                        VPLFrame.Connecter connecter = vplFrame.getConnecterList().get(vplFrame.selectedPos);
                        settingItemList.add(new SettingItem("连接对象1", Integer.toString(connecter.obj1), SettingItem.INPUT));
                        settingItemList.add(new SettingItem("连接对象2", Integer.toString(connecter.obj2), SettingItem.INPUT));
                        settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                        settingItemList.add(new SettingItem("劲度系数k", Double.toString(connecter.k), SettingItem.INPUT));
                        settingItemList.add(new SettingItem("弹簧长度(0为默认)", Double.toString(connecter.length), SettingItem.INPUT));
                    } else {
                        settingItemList.add(new SettingItem("连接对象1", "0", SettingItem.INPUT));
                        settingItemList.add(new SettingItem("连接对象2", "1", SettingItem.INPUT));
                        settingItemList.add(new SettingItem("在VPLFrame中选取", "", SettingItem.NOTHING));
                        settingItemList.add(new SettingItem("劲度系数k", "10", SettingItem.INPUT));
                        settingItemList.add(new SettingItem("弹簧长度(0为默认)", "0", SettingItem.INPUT));
                    }
                }
                break;
            case VPLFrame.LINE_TRACK:
                if (vplFrame.selectedKind == VPLFrame.LINE_TRACK) {
                    VPLFrame.Track track = vplFrame.getTrackList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("起始位置x1", Double.toString(track.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("起始位置y1", Double.toString(track.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止位置x2", Double.toString(track.x2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止位置x2", Double.toString(track.y2), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("粒子回收器", " | |" + track.objRemover, SettingItem.CHOOSE));
                } else {
                    settingItemList.add(new SettingItem("起始位置x1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("起始位置y1", "-10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止位置x2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止位置x2", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("粒子回收器", " | |false", SettingItem.CHOOSE));
                }
                break;
            case VPLFrame.CIRCLE_TRACK:
                if (vplFrame.selectedKind == VPLFrame.CIRCLE_TRACK) {
                    VPLFrame.Track track = vplFrame.getTrackList().get(vplFrame.selectedPos);
                    settingItemList.add(new SettingItem("圆心横坐标x", Double.toString(track.x1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("圆心纵坐标y", Double.toString(track.y1), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", Double.toString(track.radius), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("起始角度", Double.toString(track.start), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止角度", Double.toString(track.end), SettingItem.INPUT));
                    settingItemList.add(new SettingItem("粒子回收器", " | |" + track.objRemover, SettingItem.CHOOSE));
                } else {
                    settingItemList.add(new SettingItem("圆心横坐标x", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("圆心纵坐标y", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("半径r", "10", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("起始角度", "0", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("终止角度", "360", SettingItem.INPUT));
                    settingItemList.add(new SettingItem("粒子回收器", " | |false", SettingItem.CHOOSE));
                }
                break;
        }
    }

}
