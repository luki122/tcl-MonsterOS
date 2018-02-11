package cn.tcl.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;

import com.tcl.framework.log.NLog;

import java.lang.reflect.Method;

import cn.tcl.music.R;

/**
 * 公共界面
 * @author xiangxiang.liu
 *
 */
public class FragmentContainerActivity extends BaseActivity {


	/**
	 * 启动一个界面
	 *
	 * @param activity
	 * @param clazz
	 * @param
	 */
	public static void launch(Activity activity, Class<? extends Fragment> clazz) {
		launch(activity, clazz, null);
	}

	/**
	 * 启动一个界面
	 * 
	 * @param activity
	 * @param clazz
	 * @param
	 */
	public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
		Intent intent = new Intent(activity, FragmentContainerActivity.class);
		intent.putExtra("className", clazz.getName());
		if (args != null)
			intent.putExtra("args", args);
		activity.startActivity(intent);
	}
	
	public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args,
									   int requestCode) {
		if(fragment.getActivity() == null)
			return;
		Activity activity = fragment.getActivity();
		
		Intent intent = new Intent(activity, FragmentContainerActivity.class);
		intent.putExtra("className", clazz.getName());
		if (args != null)
			intent.putExtra("args", args);
		fragment.startActivityForResult(intent, requestCode);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String className = getIntent().getStringExtra("className");
		if (TextUtils.isEmpty(className)) {
			finish();
			return;
		}

		FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");

		Fragment fragment = null;
		if (savedInstanceState == null) {
			try {
				Class clazz = Class.forName(className);
				fragment = (Fragment) clazz.newInstance();
				if (values != null) {
					try {
						Method method = clazz.getMethod("setArguments", Bundle.class);
						method.invoke(fragment, FragmentArgs.transToBundle(values));
					} catch (Exception e) {
						NLog.printStackTrace(e);
					}
				}
				try {
					Method method = clazz.getMethod("setTheme");
					if(method != null) {
						int themeRes = Integer.parseInt(method.invoke(fragment).toString());
						setTheme(themeRes);
					}
				} catch (Exception e) {
					NLog.printStackTrace(e);
				}
			} catch (Exception e) {
				e.printStackTrace();
				finish();
				return;
			}
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_fragment_container);
		
		if(fragment != null) {
			getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, className).commit();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

			return;
		}
		
		super.onConfigurationChanged(newConfig);
	}


}
