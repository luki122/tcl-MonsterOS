package cn.tcl.music.view.mixvibes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.PointF;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tcl.framework.log.NLog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public abstract class AbstractJogWheel extends ImageView {

    public AbstractJogWheel(Context context) {
        super(context);
    }
}
