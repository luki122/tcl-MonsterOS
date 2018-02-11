package com.android.gallery3d.filtershow.controller;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.pipeline.RenderingRequest;
import com.android.gallery3d.filtershow.pipeline.RenderingRequestCaller;
import com.android.gallery3d.filtershow.editors.Editor;
import com.android.gallery3d.filtershow.editors.EditorDraw;

import java.util.Vector;

public class StyleChooser implements Control {
    private final String LOGTAG = "StyleChooser";
    protected ParameterStyles mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    private Vector<ImageButton> mIconButton = new Vector<ImageButton>();
    protected int mLayoutID = R.layout.filtershow_control_style_chooser;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 begin
    private ImageButton[] mStyleButton;
    private int[] mBrushIcons;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 end

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        Context context = container.getContext();
        mParameter = (ParameterStyles) parameter;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listStyles);
        mTopView.setVisibility(View.VISIBLE);
        int n = mParameter.getNumberOfStyles();
        mIconButton.clear();
        Resources res = context.getResources();
        int dim = res.getDimensionPixelSize(R.dimen.draw_style_icon_dim);
        LayoutParams lp = new LayoutParams(dim, dim);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 begin
        mStyleButton = new ImageButton[n];
        mBrushIcons = EditorDraw.brushIcons;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 end
        for (int i = 0; i < n; i++) {
            final ImageButton button = new ImageButton(context);
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 begin
            mStyleButton[i] = button;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), mBrushIcons[i]);
            button.setImageBitmap(bitmap);
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 end
            button.setScaleType(ScaleType.CENTER_CROP);
            button.setLayoutParams(lp);
            button.setBackgroundResource(android.R.color.transparent);
            mIconButton.add(button);
            final int buttonNo = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mParameter.setSelected(buttonNo);
                    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 begin
                    resetStyle(arg0,buttonNo);
                    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 end
                }
            });
            mLinearLayout.addView(button);
        }
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 begin
    public void resetStyle(View button, int buttonNo) {
        int mSelectedButton = buttonNo;
        for (int i = 0; i < mStyleButton.length; i++) {
            int rid = (i == mSelectedButton) ? R.color.color_chooser_slected_border
                    : R.color.color_chooser_unslected_border;
            mStyleButton[i].setBackgroundResource(rid);
        }
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR897214 end

    @Override
    public View getTopView() {
        return mTopView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterStyles) parameter;
        updateUI();
    }

    @Override
    public void updateUI() {
        if (mParameter == null) {
            return;
        }
    }

}
