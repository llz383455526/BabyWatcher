package com.llz.childrennoisedetect;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.llz.childrennoisedetect.config.AppConfig;
import com.llz.childrennoisedetect.widgets.CustomAudioRecord;
import com.llz.childrennoisedetect.widgets.YSBNavigationBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

        phone = AppConfig.getUserDefault(AppConfig.flag_phone, String.class);
        if(!TextUtils.isEmpty(phone))
        {
            holder.settingEtPhone.setText(phone);
        }


        holder.settingNpVolume.setMinValue(40);
        holder.settingNpVolume.setMaxValue(75);
        holder.settingNpVolume.setWrapSelectorWheel(false);
        holder.settingNpVolume.setValue(volumeThreshold);
        holder.settingTvVolume.setText(volumeThreshold + "");

        holder.settingEtTime.setText(volumeContinueTime + "");

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
        holder.nav.enableRightTextView("保存设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出设置时，将设置内容存入sp
                saveSetting();
            }
        });
        /**
         * 音量阈值调节
         */

        holder.settingTvVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 先进行一次自动检测，给用户建议
                 */
                postAutoDetectDialog();
                holder.settingNpVolume.setVisibility(View.VISIBLE);
            }
        });
        holder.settingNpVolume.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                holder.settingTvVolume.setText(newVal + "");
            }
        });


        holder.settingEtTime.setSelection(holder.settingEtTime.getText().toString().length());

    }

    private void postAutoDetectDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("系统即将开始为您自动检测房间声场状态。");
        AlertDialog dialog = builder.create();
        dialog.setContentView(R.layout.auto_detect_dialog_view);
        dialog.setButton(DialogInterface.
                BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final CustomAudioRecord customAudioRecord = new CustomAudioRecord(MySettingsActivity.this);
                try {
                    customAudioRecord.startAudio(new CustomAudioRecord.onVolumeChangeListener() {

                        public int count; //自动检测5s，也就是50次
                        public ArrayList<Integer> list = new ArrayList<Integer>();

                        @Override
                        public void onVolumeChange(float volume) {
                            list.add((int) volume);
                            count++;

                            if(count==50){
                                customAudioRecord.stopAudio();
                                postSuggestionDialog(list);
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        dialog.show();
    }

    private void postSuggestionDialog(ArrayList list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs-rhs;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("系统检测到你房间的音量范围是["+list.get(0)+","+list.get(list.size()-1)+"]。");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        saveSetting();
    }

    private void saveSetting() {
        String time = holder.settingEtTime.getText().toString();
        if(!TextUtils.isEmpty(time)){
            AppConfig.setUserDefault(AppConfig.flag_volume_continue_time, Integer.parseInt(time));
        }else {
            Toast.makeText(MySettingsActivity.this, "哭声持续时间不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        int volume = holder.settingNpVolume.getValue();
        AppConfig.setUserDefault(AppConfig.flag_volume_threshold, volume);

        String phone = holder.settingEtPhone.getText().toString();
        if(!TextUtils.isEmpty(phone))
        {
            AppConfig.setUserDefault(AppConfig.flag_phone, phone);
        }else {
            Toast.makeText(MySettingsActivity.this, "要通知的手机不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        MySettingsActivity.this.finish();
        Toast.makeText(MySettingsActivity.this, "设置已保存", Toast.LENGTH_SHORT).show();
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
        @Bind(R.id.setting_tv_volume)
        TextView settingTvVolume;
        @Bind(R.id.rl_volume)
        RelativeLayout rlVolume;

        ViewHolder(Activity view) {
            ButterKnife.bind(this, view);
        }
    }


}
