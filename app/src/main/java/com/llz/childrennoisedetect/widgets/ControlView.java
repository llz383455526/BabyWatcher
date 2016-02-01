package com.llz.childrennoisedetect.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.llz.childrennoisedetect.R;

/**
 * Created by ysbang on 2016/1/29.
 */
public class ControlView extends LinearLayout
{
    private ImageButton btnOp;
    private TextView tvOpState;
    private Context mContext;
    private boolean btnOpState = false;

    public ControlView(Context context) {
        this(context,null);
    }

    public ControlView(Context context,AttributeSet attrs) {
        super(context,attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.control_view_layout, this);
        initView();
    }

    private void initView() {
        btnOp = (ImageButton) findViewById(R.id.btn_op);
        tvOpState = (TextView) findViewById(R.id.tv_op_state);

        btnOp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnOp.setImageDrawable(getResources().getDrawable(R.mipmap.hover));
                        break;
                    case MotionEvent.ACTION_UP:
                        setBtnOpState(!btnOpState);
                        break;
                }
                return false;
            }
        });

    }

    public void setBtnOpClickListener(OnClickListener listener){
        if(listener==null)
        {
            return;
        }else {
            btnOp.setOnClickListener(listener);
        }
    }

    public boolean getBtnOpState(){
        return btnOpState;
    }

    public void setBtnOpState(boolean state){
        btnOpState = state;
        if(state)
        {
            btnOp.setImageDrawable(getResources().getDrawable(R.mipmap.active));
            tvOpState.setText(getResources().getString(R.string.controlviewstate_working));
        }else {
            btnOp.setImageDrawable(getResources().getDrawable(R.mipmap.normal));
<<<<<<< HEAD
            tvOpState.setText(getResources().getString(R.string.controlviewstate_ready));
=======
            tvOpState.setText(getResources().getString(R.string.controlviewstate_working));
>>>>>>> c32afdf53b0ebad25411e87be5c3a7397cde26a9
        }
    }
}
