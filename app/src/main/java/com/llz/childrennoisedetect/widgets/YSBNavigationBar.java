package com.llz.childrennoisedetect.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.llz.childrennoisedetect.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by yaoshibang on 2014/12/8.
 *
 * This Class is the header of each Activity's layout
 */
public class YSBNavigationBar extends LinearLayout {
    protected ViewHolder viewHolder;

    public YSBNavigationBar(Context context) {
        super(context);
        initLayout();
    }

    public YSBNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public YSBNavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    private void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.base_ysb_navigationbar, this);
        viewHolder = new ViewHolder(this);
        viewHolder.ivNavRight.setVisibility(GONE);
        viewHolder.tvNavRight.setVisibility(GONE);
    }


    /** 设置左布局监听器 */
    public void setLeftListener(View.OnClickListener leftListener) {
        viewHolder.llNavLeft.setOnClickListener(leftListener);
    }

    /** 设置中间title文案 */
    public void setTitle(String title) {
        viewHolder.tvNavMiddle.setText(title);
    }

    /** 设置中间布局监听器 */
    public void setMiddleListener(View.OnClickListener leftListener) {
        viewHolder.llNavMiddle.setOnClickListener(leftListener);
    }

    /** 隐藏左边(gone)。 默认显示 */
    public void setLeftLayoutGone() {
        viewHolder.llNavLeft.setVisibility(View.GONE);
    }

    /** 隐藏左边(invisible)。 默认显示 */
    public void setLeftLayoutInvisible(){
        viewHolder.llNavLeft.setVisibility(View.INVISIBLE);
    }

    /////////////////// 右布局 //////////////////////////////////////////////////////////////////
    /** 使用右文字 */
    public void enableRightTextView(String rightText, OnClickListener listener) {
        viewHolder.tvNavRight.setText(rightText);
        viewHolder.tvNavRight.setOnClickListener(listener);
        viewHolder.tvNavRight.setVisibility(VISIBLE);
    }
    /** 使用右图 */
    public void enableRightImageView(int resid, OnClickListener listener) {
        viewHolder.ivNavRight.setImageResource(resid);
        viewHolder.ivNavRight.setOnClickListener(listener);
        viewHolder.ivNavRight.setVisibility(VISIBLE);
    }

    public void setRightTextSize(int sp){
        viewHolder.tvNavRight.setTextSize(sp);
    }


    /**
     * 添加右布局.
     * 因右布局变化多，如果基本的文字view和图片view不能满足需求，建议另创建一个布局控件再加进来。
     * */
    public void addViewToRightLayout(View view) {
        viewHolder.llNavRight.addView(view);
    }

    protected class ViewHolder {
        @Bind(R.id.llNavLeft)public LinearLayout llNavLeft;
        @Bind(R.id.ivNavLeft)public ImageView ivNavLeft;

        @Bind(R.id.llNavMiddle)public LinearLayout llNavMiddle;
        @Bind(R.id.tvNavMiddle)public TextView tvNavMiddle;

        @Bind(R.id.llNavRight)public LinearLayout llNavRight; // 建议
        @Bind(R.id.tvNavRight)public TextView tvNavRight; // 右布局基本文字
        @Bind(R.id.ivNavRight)public ImageView ivNavRight; //  右布局基本图片


        ViewHolder(View view) {
            ButterKnife.bind(ViewHolder.this, view);
        }
    }
}
