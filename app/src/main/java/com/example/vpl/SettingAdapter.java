package com.example.vpl;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.EmptyStackException;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.BrokenBarrierException;

/**
 * Created by MashPlant on 2016/4/16.
 */
public class SettingAdapter extends ArrayAdapter<SettingItem> {
    private int resourceId;
    String[] datas = new String[20];

    public SettingAdapter(Context context, int textViewResourceId, List<SettingItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(final int positition, View convertView, ViewGroup parent) {
        SettingItem settingItem = getItem(positition);
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.layout = (LinearLayout) view.findViewById(R.id.set_item);
            viewHolder.sw_input=(EditText) view.findViewById(R.id.sw_input);
            viewHolder.symbol_goto = (TextView) view.findViewById(R.id.symbol_goto);
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.input = (EditText) view.findViewById(R.id.input);
            viewHolder.choose = (Switch) view.findViewById(R.id.choose);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.name.setText(settingItem.getName());
        switch (settingItem.getKind()) {
            case SettingItem.NOTHING:
                viewHolder.choose.setVisibility(View.GONE);
                viewHolder.input.setVisibility(View.GONE);
                viewHolder.input.setHint("null");
                break;
            case SettingItem.CHOOSE:
                viewHolder.symbol_goto.setVisibility(View.GONE);
                viewHolder.choose.setVisibility(View.VISIBLE);
                viewHolder.input.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(settingItem.getHint())) {
                    String[] strings = settingItem.getHint().split("\\|");
                    viewHolder.choose.setChecked(strings[2].equals("true"));
                    viewHolder.choose.setTextOff(strings[1]);
                    viewHolder.choose.setTextOn(strings[0]);
                }
                /*viewHolder.choose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        datas[positition] = Boolean.toString(isChecked);
                    }
                });*/
                viewHolder.choose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datas[positition] = Boolean.toString(viewHolder.choose.isChecked());
                    }
                });
                viewHolder.input.setHint("null");
                break;
            case SettingItem.INPUT:
                viewHolder.symbol_goto.setVisibility(View.GONE);
                viewHolder.choose.setVisibility(View.GONE);
                viewHolder.input.setVisibility(View.VISIBLE);
                viewHolder.input.setHint(settingItem.getHint());
                //viewHolder.input.setText(settingItem.getHint());
                viewHolder.input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        datas[positition] = s.toString();
                    }
                });
                break;
            case SettingItem.SWINPUT:
                viewHolder.sw_input.setVisibility(View.VISIBLE);
                viewHolder.sw_input.setHint(settingItem.getName());
                viewHolder.name.setVisibility(View.GONE);
                viewHolder.symbol_goto.setVisibility(View.GONE);
                viewHolder.choose.setVisibility(View.GONE);
                viewHolder.input.setVisibility(View.VISIBLE);
                viewHolder.input.setHint(settingItem.getHint());
                break;
        }
        return view;
    }

    class ViewHolder {
        LinearLayout layout;
        TextView name;
        TextView symbol_goto;
        EditText input;
        EditText sw_input;
        Switch choose;
    }
}
