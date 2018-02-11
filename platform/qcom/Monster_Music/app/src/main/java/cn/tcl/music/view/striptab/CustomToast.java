package cn.tcl.music.view.striptab;

import android.content.Context;
import android.widget.Toast;

import cn.tcl.music.util.LogUtil;

/**
 * Created by Administrator on 2015/11/30.
 */
public class CustomToast {

    private static Toast mToast;
    private static long time =0;
    private static String text1="";

    public static void showToast(Context mContext, String text, int duration,int gravity) {
        LogUtil.d("mytest","time= "+(System.currentTimeMillis()-time));
        if(time!=0&&!text1.isEmpty()&&mToast!=null){
            if(text.equals(text1)&&System.currentTimeMillis()-time<1000){
                return ;
            }
        }

        if (mToast != null) {
            mToast.setText(text);
        }else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        mToast.setGravity(gravity, 0, 10);
        mToast.show();
        text1=text;
        time=System.currentTimeMillis();

    }

    public static void showToast(Context mContext, int resId, int duration,int gravity ) {
        showToast(mContext, mContext.getResources().getString(resId), duration,gravity);
    }

}
