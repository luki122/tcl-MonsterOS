package mst.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mst.internal.R;

public class PopupDialog extends Dialog implements View.OnClickListener{


    private TextView mTitleView;

    private Button mPositiveButton;

    private Button mNagativeButton;

    private View mContentView;

    private FrameLayout mCustomPanel;

    private OnClickListener mListener;

    private RelativeLayout mTitleBar;

    public PopupDialog(Context context, int resid) {
        super(context,resid);//,com.mst.R.style.Dialog_Spinner
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.popup_dialog, null);
        mTitleView = (TextView)mContentView.findViewById(android.R.id.text1);
        mPositiveButton = (Button)mContentView.findViewById(android.R.id.button2);
        mNagativeButton = (Button)mContentView.findViewById(android.R.id.button1);
        mCustomPanel = (FrameLayout) mContentView.findViewById(android.R.id.content);
        mTitleBar = (RelativeLayout) mContentView.findViewById(android.R.id.title);
        mNagativeButton.setOnClickListener(this);
        mPositiveButton.setOnClickListener(this);
    }

    public PopupDialog(Context context){
        this(context,com.mst.R.style.Dialog_Spinner);
    }

    public PopupDialog(Context context, OnClickListener listener){
        this(context);
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Window window = getWindow();
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        p.dimAmount = 0.6f;
        p.windowAnimations = R.style.popup_dialog_anim;
        p.gravity = Gravity.BOTTOM;
        window.setAttributes(p);
        super.onCreate(savedInstanceState);
        setContentView(mContentView);
        this.setCanceledOnTouchOutside(true);
    }

    public View findViewById(int id) {
        return mContentView.findViewById(id);
    }

    @Override
    public void setTitle(CharSequence title) {
        // TODO Auto-generated method stub
        mTitleView.setText(title);
        mTitleView.setVisibility(View.VISIBLE);
        updateTitlePanel();
    }

    public void setCustomView(int layout){
        View view = LayoutInflater.from(getContext()).inflate(layout, mCustomPanel,false);
        mCustomPanel.addView(view);
    }

    public void setCustomView(View view){
        mCustomPanel.addView(view);
    }

    public void setPositiveButton(boolean show){
        mPositiveButton.setVisibility(show?View.VISIBLE:View.GONE);
        updateTitlePanel();
    }

    public void setNegativeButton(boolean show){
        mNagativeButton.setVisibility(show?View.VISIBLE:View.GONE);
        updateTitlePanel();
    }

    public void setOnClickListener(OnClickListener listener){
        mListener = listener;
    }

    private void updateTitlePanel(){
        if(mTitleView.getVisibility() == View.VISIBLE || mPositiveButton.getVisibility() == View.VISIBLE || mNagativeButton.getVisibility() == View.VISIBLE){
            mTitleBar.setVisibility(View.VISIBLE);
        }else{
            mTitleBar.setVisibility(View.GONE);
        }
    }

    public Button getButton(int id){
        switch (id){
            case BUTTON_POSITIVE:
                return mPositiveButton;
            case BUTTON_NEGATIVE:
                return mNagativeButton;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mPositiveButton ) {
            if(mListener != null){
                mListener.onClick(this,BUTTON_POSITIVE);
            }
        } else if (v == mNagativeButton) {
            if(mListener != null){
                mListener.onClick(this,BUTTON_NEGATIVE);
            }
        }

    }

}
