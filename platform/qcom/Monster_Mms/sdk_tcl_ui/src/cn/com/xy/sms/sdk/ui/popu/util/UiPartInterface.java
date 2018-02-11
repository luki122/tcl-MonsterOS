package cn.com.xy.sms.sdk.ui.popu.util;

public interface UiPartInterface {
    public Object doUiAction(int type, Object data);
    
    public Object doUiActionMulti(int type, Object... data);
}
