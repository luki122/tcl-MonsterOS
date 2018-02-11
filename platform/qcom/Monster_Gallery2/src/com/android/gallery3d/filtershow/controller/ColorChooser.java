package com.android.gallery3d.filtershow.controller;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.colorpicker.ColorListener;
import com.android.gallery3d.filtershow.colorpicker.ColorPickerDialog;
import com.android.gallery3d.filtershow.editors.Editor;

import java.util.Arrays;
import java.util.Vector;

public class ColorChooser implements Control {
    private final String LOGTAG = "StyleChooser";
    protected ParameterColor mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    private Vector<Button> mIconButton = new Vector<Button>();
    protected int mLayoutID = R.layout.filtershow_control_color_chooser;
    Context mContext;
    private int mTransparent;
    private int mSelected;
    private static final int OPACITY_OFFSET = 3;
    private int[] mButtonsID = {
            R.id.draw_color_button01,
            R.id.draw_color_button02,
            R.id.draw_color_button03,
            R.id.draw_color_button04,
            R.id.draw_color_button05,
    };
    private Button[] mButton = new Button[mButtonsID.length];

    int mSelectedButton = 0;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        Resources res = container.getContext().getResources();
        mTransparent  = res.getColor(R.color.color_chooser_unslected_border);
        mSelected    = res.getColor(R.color.color_chooser_slected_border);
        mEditor = editor;
        mContext = container.getContext();
        int iconDim = res.getDimensionPixelSize(R.dimen.draw_style_icon_dim);
        mParameter = (ParameterColor) parameter;
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listStyles);
        mTopView.setVisibility(View.VISIBLE);

        mIconButton.clear();
        LayoutParams lp = new LayoutParams(iconDim, iconDim);
        int [] palette = mParameter.getColorPalette();
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 begin
        int colorValue = mParameter.getValue();
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 end
        for (int i = 0; i < mButtonsID.length; i++) {
            final Button button = (Button) mTopView.findViewById(mButtonsID[i]);
            mButton[i] = button;
            float[] hsvo = new float[4];
            Color.colorToHSV(palette[i], hsvo);
            hsvo[OPACITY_OFFSET] = (0xFF & (palette[i] >> 24)) / (float) 255;
            button.setTag(hsvo);
            GradientDrawable sd = ((GradientDrawable) button.getBackground());
            sd.setColor(palette[i]);
            sd.setStroke(3, (mSelectedButton == i) ? mSelected : mTransparent);

            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 begin
            if (colorValue == palette[i]) {
                mSelectedButton = i;
            }
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 end

            final int buttonNo = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    selectColor(arg0, buttonNo);
                }
            });
        }
        ImageButton button = (ImageButton) mTopView.findViewById(R.id.draw_color_popupbutton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showColorPicker();
            }
        });
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 begin
        if (mParameter.isClearColor()) {
            mSelectedButton = 0;
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 begin
        selectColor(mButton[mSelectedButton], mSelectedButton);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 end
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 end
    }

    public void setColorSet(int[] basColors) {
        int []palette = mParameter.getColorPalette();
        for (int i = 0; i < palette.length; i++) {
            palette[i] = basColors[i];
            float[] hsvo = new float[4];
            Color.colorToHSV(palette[i], hsvo);
            hsvo[OPACITY_OFFSET] = (0xFF & (palette[i] >> 24)) / (float) 255;
            mButton[i].setTag(hsvo);
            GradientDrawable sd = ((GradientDrawable) mButton[i].getBackground());
            sd.setColor(palette[i]);
        }

    }

    public int[] getColorSet() {
        return  mParameter.getColorPalette();
    }

    private void resetBorders() {
        int []palette = mParameter.getColorPalette();
        for (int i = 0; i < mButtonsID.length; i++) {
            final Button button = mButton[i];
            GradientDrawable sd = ((GradientDrawable) button.getBackground());
            sd.setColor(palette[i]);
            sd.setStroke(3, (mSelectedButton == i) ? mSelected : mTransparent);
        }
    }

/*
    public void selectColor(View button, int buttonNo) {
        mSelectedButton = buttonNo;
        float[] hsvo = (float[]) button.getTag();
        mParameter.setValue(Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo));
        resetBorders();
        mEditor.commitLocalRepresentation();
    }
*/
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 begin
    public void selectColor(View button, int buttonNo) {
        mSelectedButton = buttonNo;
        float[] hsvo = (float[]) button.getTag();
        int value = Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo);
        mParameter.setValue(value);
        int[] palette = mParameter.getColorPalette();
        palette[buttonNo] = value;
        mParameter.setColorpalette(palette);
        resetBorders();
        mEditor.commitLocalRepresentation();
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003186 end

    @Override
    public View getTopView() {
        return mTopView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterColor) parameter;
        updateUI();
    }

    @Override
    public void updateUI() {
        if (mParameter == null) {
            return;
        }
    }

    public void changeSelectedColor(float[] hsvo) {
        int []palette = mParameter.getColorPalette();
        int c = Color.HSVToColor((int) (hsvo[3] * 255), hsvo);
        final Button button = mButton[mSelectedButton];
        GradientDrawable sd = ((GradientDrawable) button.getBackground());
        sd.setColor(c);
        palette[mSelectedButton] = c;
        mParameter.setValue(Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo));
        button.setTag(hsvo);
        mEditor.commitLocalRepresentation();
        button.invalidate();
    }

    public void showColorPicker() {
        ColorListener cl = new ColorListener() {
            @Override
            public void setColor(float[] hsvo) {
                changeSelectedColor(hsvo);
            }
            @Override
            public void addColorListener(ColorListener l) {
            }
        };
        ColorPickerDialog cpd = new ColorPickerDialog(mContext, cl);
        float[] c = (float[]) mButton[mSelectedButton].getTag();
        cpd.setColor(Arrays.copyOf(c, 4));
        cpd.setOrigColor(Arrays.copyOf(c, 4));
        cpd.show();
    }
}
