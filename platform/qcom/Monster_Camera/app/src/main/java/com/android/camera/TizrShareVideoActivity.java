package com.android.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;


public class TizrShareVideoActivity extends Activity {
    private static final Log.Tag TAG = new Log.Tag("TizrShareVideoActivity");
    private Uri mUri ;
    private Button mStartBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tizr_share_video);
        mUri = getIntent().getData();
        mStartBtn = (Button)findViewById(R.id.startbtn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(CameraUtil.TIZR_URI));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e){
                    Log.e(TAG, "Tony tizr share video Exception");
                    e.printStackTrace();
                }finally {
                    finish();
                }


            }
        });
    }
}
