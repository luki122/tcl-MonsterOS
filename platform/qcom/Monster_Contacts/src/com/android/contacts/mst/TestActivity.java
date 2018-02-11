package com.android.contacts.mst;

import com.android.contacts.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class TestActivity extends Activity{
	private static final String TAG = "TestActivity";
	private static final int MST_SCAN_BUSINESS_CARD = 100;
	private ImageView imageview;
	@Override
	protected void onCreate(Bundle savedState) {
		Log.i(TAG, "[onCreate]");
		super.onCreate(savedState);
		setContentView(R.layout.test);
		imageview=(ImageView)findViewById(R.id.imageview1);
		Button button1=(Button)findViewById(R.id.button1);
		Button button2=(Button)findViewById(R.id.button2);
		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {	
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
				Uri uri=getTempUri();
				Log.d(TAG,"uri:"+uri);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);  
//				intent.putExtra("crop", "false");
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				intent.putExtra("return-data", false);
				intent.putExtra("noFaceDetection", true);
				startActivityForResult(intent,MST_SCAN_BUSINESS_CARD);
			}
		});
		
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {	
				Intent intent = new Intent("com.android.contacts.MST_SCAN_BUSINESSCARD_ACTION");  
				Uri uri=getTempUri();
				Log.d(TAG,"uri:"+uri);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);  
//				intent.putExtra("crop", "false");
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				intent.putExtra("return-data", false);
				intent.putExtra("noFaceDetection", true);
				startActivityForResult(intent,MST_SCAN_BUSINESS_CARD);
			}
		});
	}

	Uri tempPhotoUri;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"requestCode:"+requestCode+" resultCode:"+resultCode+" data:"+data);
		if(data!=null){
			Bundle bundle=data.getExtras();
			Log.d(TAG,"bundle:"+bundle);
		}
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MST_SCAN_BUSINESS_CARD &&resultCode == RESULT_OK/*&& data != null*/) {
			Bitmap bitmap = null;
			try {              
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(tempPhotoUri));
				imageview.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private Uri getTempUri() {
		tempPhotoUri = Uri.fromFile(getTempFile());
		return tempPhotoUri;
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {
			
			File file =new File(Environment.getExternalStorageDirectory()+"/businesscard/");
			//如果文件夹不存在则创建
			if  (!file .exists()  && !file .isDirectory())      
			{       
			    file .mkdir();    
			}
			
			File f = new File(file.getAbsolutePath(),System.currentTimeMillis()+".jpg");
			Log.d(TAG,"f:"+f.getAbsolutePath());
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return f;
		}
		return null;
	}

	private boolean isSDCARDMounted(){
		String status = Environment.getExternalStorageState();
		Log.d(TAG,"status:"+status);
		if (status.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}
}
