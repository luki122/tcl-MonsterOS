package com.android.camera;

/**
 * Created by nielei on 15-11-16.
 */
public interface HelpTipController {

    public void notifyFinishHelpTip();

    public void onUpdateUIChangedFromTutorial();

    public void checkAlarmTaskHelpTip();

    public void removeAlarmTask(int groudid);
}
