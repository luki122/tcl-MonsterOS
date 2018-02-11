package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.MenuItem;

import com.tcl.framework.log.NLog;

import java.lang.reflect.Method;

import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.activities.FragmentArgs;

public class FragmentContainerActivityV2 extends BaseMusicActivity {
    private boolean isDestroyed = false;
    private OnBackPressedCallback onBackPressedCallback;

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
        Intent intent = new Intent(activity, FragmentContainerActivityV2.class);
        intent.putExtra("className", clazz.getName());
        if (args != null) {
            intent.putExtra("args", args);
        }
        activity.startActivity(intent);
    }

    public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args,
                                       int requestCode) {
        if (fragment.getActivity() == null) {
            return;
        }
        Activity activity = fragment.getActivity();
        Intent intent = new Intent(activity, FragmentContainerActivityV2.class);
        intent.putExtra("className", clazz.getName());
        if (args != null) {
            intent.putExtra("args", args);
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    public void setFragmentPadding(boolean enablePadding, int barHeight) {
        final int height = enablePadding ? barHeight : 0;
        findViewById(R.id.sliding_up_external_container).setPadding(0, height, 0, 0);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    protected void setContentView() {
        setContentView(R.layout.activity_fragment_container);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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
                    }
                }
                try {
                    Method method = clazz.getMethod("setTheme");
                    if (method != null) {
                        int themeRes = Integer.parseInt(method.invoke(fragment).toString());
                        setTheme(themeRes);
                    }
                } catch (Exception e) {
                    NLog.printStackTrace(e);
                }
            } catch (Exception e) {
                NLog.printStackTrace(e);
                finish();
                return;
            }
        }
        super.onCreate(savedInstanceState);
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.sliding_up_external_container, fragment, className).commit();
        }
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            return;
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (onBackPressedCallback != null && onBackPressedCallback.onBackPressed()) {
                return true;
            }
            onOptionsItemClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onOptionsItemClicked() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setOnBackPressedCallback(OnBackPressedCallback callback) {
        this.onBackPressedCallback = callback;
    }

    public interface OnBackPressedCallback {
        boolean onBackPressed();
    }
}
