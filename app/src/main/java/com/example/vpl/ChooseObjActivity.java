package com.example.vpl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by MashPlant on 2016/4/16.
 */
public class ChooseObjActivity extends AppCompatActivity {
    private ObservableListView listView;
    private List<SettingItem> settingItemList = new ArrayList<>();
    private SettingAdapter adapter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("选择生成的对象");
        setContentView(R.layout.setting_layout);
        initSettingItem();
        adapter = new SettingAdapter(ChooseObjActivity.this, R.layout.set_item, settingItemList);
        listView = (ObservableListView) findViewById(R.id.set_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddObjActivity.kind = position;
                Intent intent = new Intent(ChooseObjActivity.this, AddObjActivity.class);
                startActivity(intent);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        findViewById(R.id.button_ensure).setVisibility(View.GONE);
        findViewById(R.id.button_cancel).setVisibility(View.GONE);

       /* findViewById(R.id.button_ensure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        listView.setOnObserveListener(new ObservableListView.OnObserveListener() {
            @Override
            public void onUp(float offsetY) {
                if (actionBar.isShowing()){
                   //actionBar.setHideOffset((int)offsetY);
                }
            }

            @Override
            public void onDown(float offsetY) {
                //actionBar.setHideOffset((int)offsetY);
            }
        });
    }

    private void initSettingItem() {
        settingItemList.add(new SettingItem("运动对象", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("引力质心", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("点电荷", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("匀强电场", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("匀强磁场", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("圆形匀强磁场", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("阻尼介质", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("弹性轻绳", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("轻质硬杆", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("轻质弹簧", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("直线轨道", "", SettingItem.NOTHING));
        settingItemList.add(new SettingItem("圆弧轨道", "", SettingItem.NOTHING));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, AddObjActivity.class);
            AddObjActivity.kind = VPLFrame.SETTING;
            startActivity(intent);
            return true;
        }
        if (id==R.id.action_about){
            Toast.makeText(this,"VPL designed by MashPlant\n鸣谢:金华科仿真物理实验室",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
