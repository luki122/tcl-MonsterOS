package mst.content;

import android.content.Intent;

public class AuroraIntent extends Intent{
	
	// Aurora <Luofu> <2014-4-9> add for Sendbroadcast when inputmethod show and hide begin
    public static final String ACTION_INPUT_METHOD_SHOW = "android.intent.action.ACTION_INPUT_METHOD_SHOW";
    
    public static final String ACTION_INPUT_METHOD_HIDE = "android.intent.action.ACTION_INPUT_METHOD_HIDE";
    // Aurora <Luofu> <2014-4-9> add for Sendbroadcast when inputmethod show and hide end

    //Aurora <tangjun> <2014-4-21> add for powerkey shortpress begin
    public static final String ACTION_KEYCODE_POWER_SHORTPRESS = "android.intent.action.ACTION_KEYCODE_POWER_SHORTPRESS";
    //Aurora <tangjun> <2014-4-21> add for powerkey shortpress end

}
