package cn.tcl.note.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import cn.tcl.note.R;
import cn.tcl.note.ui.ImgCropView;
import cn.tcl.note.util.FileUtils;

public class CropActivity extends RootActivity {
    private ImgCropView mImgCropView;
    private Button mCropOK;
    private Button mCropCancel;
    private String mImgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.crop_action_bar_color));
        window.setNavigationBarColor(getColor(R.color.crop_bg_color));
        window.getDecorView().setSystemUiVisibility(0);

        mImgName = getIntent().getStringExtra(ImgViewPager.KEY_IMG_NAME);
        mImgCropView = (ImgCropView) findViewById(R.id.img_crop_view);
        mImgCropView.setImg(mImgName);
        mCropOK = (Button) findViewById(R.id.crop_ok);
        mCropCancel = (Button) findViewById(R.id.crop_cancel);
        mCropOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap cropBitmap = mImgCropView.getCropBitmap();
                FileUtils.writeImgToName(cropBitmap, mImgName);
                setResult(RESULT_OK);
                finish();
            }
        });
        mCropCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
