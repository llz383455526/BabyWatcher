package com.llz.childrennoisedetect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

        holder = new ViewHolder(this);
        volumeThreshold = AppConfig.getUserDefault(AppConfig.flag_volume_threshold, int.class);
        volumeContinueTime = AppConfig.getUserDefault(AppConfig.flag_volume_continue_time, int.class);
        phone = AppConfig.getUserDefault(AppConfig.flag_phone, String.class);

        holder.settingEtTime.setText(volumeContinueTime + "");
        holder.settingEtVolume.setText(volumeThreshold + "");
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
        /**
         * 音量阈值调节
         */
        holder.settingBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (volumeThreshold > 90) {
                    Toast.makeText(MySettingsActivity.this, "音量检测阈值不要高于90db", Toast.LENGTH_SHORT).show();
                } else {
                    volumeThreshold++;
                    holder.settingEtVolume.setText(volumeThreshold + "");
                    AppConfig.setUserDefault(AppConfig.flag_volume_threshold, volumeThreshold);
                }

            }
        });

        holder.settingBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (volumeThreshold > 10) {
                    volumeThreshold--;
                    holder.settingEtVolume.setText(volumeThreshold + "");
                    AppConfig.setUserDefault(AppConfig.flag_volume_threshold, volumeThreshold);
                } else {
                    Toast.makeText(MySettingsActivity.this, "音量检测阈值不要低于10db", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.settingEtVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /**
         * 声音持续时间调节
         */
        holder.settingEtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.settingEtTime.setText("");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String time = holder.settingEtTime.getText().toString();
        AppConfig.setUserDefault(AppConfig.flag_volume_continue_time, Integer.parseInt(time));
        String volume = holder.settingEtVolume.getText().toString();
        AppConfig.setUserDefault(AppConfig.flag_volume_threshold, Integer.parseInt(volume));
        String phone = holder.settingEtPhone.getText().toString();
        if (!phone.equals("18933948952")) {
            AppConfig.setUserDefault(AppConfig.flag_phone, phone);
        }


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

        ViewHolder(Activity view) {
            ButterKnife.bind(this, view);
        }
    }


}
