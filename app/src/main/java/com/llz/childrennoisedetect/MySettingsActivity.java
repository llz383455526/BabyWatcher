package com.llz.childrennoisedetect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.llz.childrennoisedetect.config.AppConfig;
import com.llz.childrennoisedetect.widgets.YSBNavigationBar;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ysbang on 2015/12/31.
 */
public class MySettingsActivity extends Activity {

    private ViewHolder holder;
    private int volumeThreshold = 0;
    private int volumeContinueTime = 0;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ButterKnife.bind(this);

        //读sp
        holder = new ViewHolder(this);
        if((volumeThreshold = AppConfig.getUserDefault(AppConfig.flag_volume_threshold, int.class)) == -1)
        {
            volumeThreshold = 55;   //默认值 55db
        }

        if((volumeContinueTime = AppConfig.getUserDefault(AppConfig.flag_volume_continue_time, int.class)) == -1)
        {
            volumeContinueTime = 2; //默认2s
        }

        if((phone = AppConfig.getUserDefault(AppConfig.flag_phone, String.class)) == null)
        {
            phone = "13356539463" ;
        }


        holder.settingNpVolume.setMinValue(40);
        holder.settingNpVolume.setMaxValue(75);
        holder.settingNpVolume.setWrapSelectorWheel(false);
        holder.settingNpVolume.setValue(volumeThreshold);
        holder.settingTvVolumeAdjustTitle.setText("拨动滚轮调节声音响应阈值 "+volumeThreshold+" dB");

        holder.settingEtTime.setText(volumeContinueTime + "");
//        holder.settingEtVolume.setText(volumeThreshold + "");
        holder.settingEtPhone.setText(phone);
        setViews();
    }

    private void setViews() {
        holder.nav.setTitle("设置");
        holder.nav.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySettingsActivity.this.finish();
            }
        });
        holder.nav.enableRightTextView("保存", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySettingsActivity.this.finish();
            }
        });
        /**
         * 音量阈值调节
         */

        holder.settingNpVolume.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                holder.settingTvVolumeAdjustTitle.setText("拨动滚轮调节声音响应阈值 ("+newVal+" dB)");
            }
        });



        holder.settingEtTime.setSelection(holder.settingEtTime.getText().toString().length());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();


        //退出设置时，将设置内容存入sp

        saveSetting();


    }

    private void saveSetting() {
        String time = holder.settingEtTime.getText().toString();
        if(time != null && !time.equals("")){
            AppConfig.setUserDefault(AppConfig.flag_volume_continue_time, Integer.parseInt(time));
        }

        int volume = holder.settingNpVolume.getValue();
        AppConfig.setUserDefault(AppConfig.flag_volume_threshold, volume);
        String phone = holder.settingEtPhone.getText().toString();
        AppConfig.setUserDefault(AppConfig.flag_phone, phone);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    static class ViewHolder {
        @Bind(R.id.nav)
        YSBNavigationBar nav;
        @Bind(R.id.setting_tv_volume_adjust_title)
        TextView settingTvVolumeAdjustTitle;
        @Bind(R.id.setting_btn_plus)
        Button settingBtnPlus;
        @Bind(R.id.setting_et_volume)
        EditText settingEtVolume;
        @Bind(R.id.setting_btn_delete)
        Button settingBtnDelete;
        @Bind(R.id.setting_tv_time_title)
        TextView settingTvTimeTitle;
        @Bind(R.id.setting_et_time)
        EditText settingEtTime;
        @Bind(R.id.setting_tv_phone_title)
        TextView settingTvPhoneTitle;
        @Bind(R.id.setting_et_phone)
        EditText settingEtPhone;
        @Bind(R.id.setting_np_volume)
        NumberPicker settingNpVolume;

        ViewHolder(Activity view) {
            ButterKnife.bind(this, view);
        }
    }


}
