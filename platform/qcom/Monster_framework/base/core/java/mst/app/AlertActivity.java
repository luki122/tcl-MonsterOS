package mst.app;


import com.mst.internal.app.AlertController;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * An activity that follows the visual style of an AlertDialog.
 * 
 * @see #mAlert
 * @see #mAlertParams
 * @see #setupAlert()
 */
public abstract class AlertActivity extends Activity implements DialogInterface {

    /**
     * The model for the alert.
     * 
     * @see #mAlertParams
     */
    protected AlertController mAlert;

    /**
     * The parameters for the alert.
     */
    protected AlertController.AlertParams mAlertParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlert = new AlertController(this, this, getWindow());
        mAlertParams = new AlertController.AlertParams(this);
    }

    public void cancel() {
        finish();
    }

    public void dismiss() {
        // This is called after the click, since we finish when handling the
        // click, don't do that again here.
        if (!isFinishing()) {
            finish();
        }
    }

    protected boolean isDefineCloseAnimation(){
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        if(isDefineCloseAnimation()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(Dialog.class.getName());
        event.setPackageName(getPackageName());

        ViewGroup.LayoutParams params = getWindow().getAttributes();
        boolean isFullScreen = (params.width == ViewGroup.LayoutParams.MATCH_PARENT) &&
                (params.height == ViewGroup.LayoutParams.MATCH_PARENT);
        event.setFullScreen(isFullScreen);

        return false;
    }

    /**
     * Sets up the alert, including applying the parameters to the alert model,
     * and installing the alert's content.
     *
     * @see #mAlert
     * @see #mAlertParams
     */
    protected void setupAlert() {
        mAlertParams.apply(mAlert);
        mAlert.installContent();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAlert.onKeyDown(keyCode, event)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mAlert.onKeyUp(keyCode, event)) return true;
        return super.onKeyUp(keyCode, event);
    }
}