package com.yoyo.yobutton;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView lv_activity_main_setting;
    List<String> settingList;
    boolean isFloatViewServiceWork;
    BaseAdapter mBaseAdapter;
    boolean isCloseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        isCloseService=false;
        lv_activity_main_setting = (ListView) findViewById(R.id.lv_activity_main_setting);
        settingList = new ArrayList<>();
        mBaseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return settingList.size();
            }

            @Override
            public String getItem(int position) {
                return settingList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_item_main, parent, false);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                if (holder == null) {
                    holder = new ViewHolder();
                    holder.text = (TextView) convertView.findViewById(R.id.tv_view_item_main);
                    holder.tb = (ToggleButton) convertView.findViewById(R.id.tb_view_item_main);
                    convertView.setTag(holder);
                }
                holder.text.setText(getItem(position));
                if (position == 0) {
                    holder.tb.setChecked(isFloatViewServiceWork);
                    holder.tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                startMyService();
                                buttonView.setChecked(isFloatViewServiceWork);
                            } else {
                                stopMyService();
                            }
                        }
                    });
                }

                return convertView;
            }

            class ViewHolder {
                TextView text;
                ToggleButton tb;
            }
        };
        lv_activity_main_setting.setAdapter(mBaseAdapter);
        //refreshList();
    }

    private void refreshList() {
        isFloatViewServiceWork = MyUtils.isAccessibilitySettingsOn(this);
        if(isCloseService&&FloatViewService.initialize!=null&&!isFloatViewServiceWork){
            finish();
            System.exit(0);
            return;
        }else{
            isCloseService=false;
        }
        String[] settings = getResources().getStringArray(R.array.lv_activity_main_setting);
        settingList = Arrays.asList(settings);
        mBaseAdapter.notifyDataSetChanged();
    }

    private void startMyService() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
                return;
            }
        }
        if (!MyUtils.isAccessibilitySettingsOn(this)) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        //finish();
        startService(new Intent(MainActivity.this, FloatViewService.class));
        //startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    private void stopMyService() {
        if (Build.VERSION.SDK_INT >= 24) {
            if (FloatViewService.initialize != null) {
                FloatViewService.initialize.disableSelf();
            }
            return;
        } else {
          /*  Intent intent = new Intent(MainActivity.this, FloatViewService.class);
            // 终止 FloatViewService
            stopService(intent);*/
            // FloatViewService.initialize.disableSelf();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            isCloseService=true;
        }
        // 销毁悬浮窗
       /* Intent intent = new Intent(MainActivity.this, FloatViewService.class);
        // 终止 FloatViewService
        stopService(intent);*/

        /*if(!isFloatViewServiceWork&& FloatViewService.initialize!=null){
            FloatViewService.initialize.disableSelf();
        }*/
        //startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // boolean isServiceWork = MyUtils.isAccessibilitySettingsOn(this);
        // if (isServiceWork != isFloatViewServiceWork) {
        refreshList();
        // }
    }
}
