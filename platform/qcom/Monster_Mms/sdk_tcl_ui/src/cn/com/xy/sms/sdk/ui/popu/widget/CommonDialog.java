package cn.com.xy.sms.sdk.ui.popu.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;

public class CommonDialog extends Dialog {

    TextView title;
    TextView content;

    TextView confirmButton;
    TextView cancleButton;

    RelativeLayout title_layout;
    ViewGroup bottom_layout;
    View duoqu_bottom_split_line;

    public CommonDialog(Context context) {
        super(context);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.duoqu_common_dialog);

        title_layout = (RelativeLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.content);
        bottom_layout = (ViewGroup) findViewById(R.id.bottom_layout);
        duoqu_bottom_split_line = findViewById(R.id.duoqu_bottom_split_line);
        try {
            ViewManger.setViewBg(Constant.getContext(), title_layout,
                    "#FAFAFA", R.drawable.duoqu_top_rectangle, -1);
            ViewManger.setViewBg(Constant.getContext(), bottom_layout,
                    "#FAFAFA", R.drawable.duoqu_bottom_rectangle, -1);
            ViewManger.setViewBg(Constant.getContext(),
                    duoqu_bottom_split_line, "#d1d1d1", R.drawable.duoqu_line,
                    1);

        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("CommonDialog error:", e);
        }

        confirmButton = (TextView) findViewById(R.id.confirm);
        cancleButton = (TextView) findViewById(R.id.cancel);

        setCanceledOnTouchOutside(true);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams wl = window.getAttributes();

        wl.dimAmount = 0.6f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        int padding = (Math.round(Constant.getContext().getResources()
                .getDimension(R.dimen.popup_content_padding)) * 2);

        wl.width = Constant.getWidth(Constant.getContext()) - padding;
        window.setAttributes(wl);

        confirmButton.setOnClickListener(new ButtonClickListener());
        cancleButton.setOnClickListener(new ButtonClickListener());
    }

    public CommonDialog(Context context, String title, String content,
            String leftText, String rightText, onExecListener execListener,
            onCancelListener cancelListener, OnDismissListener onDismissListener) {
        this(context);
        this.title.setText(title);
        this.content.setText(content);
        this.confirmButton.setText(rightText);
        this.cancleButton.setText(leftText);
        this.execListener = execListener;
        this.cancelListener = cancelListener;
        setOnDismissListener(onDismissListener);
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.confirm) {
                callBack();
                CommonDialog.this.dismiss();
            } else if (v.getId() == R.id.cancel) {
                callBackCancel();
                CommonDialog.this.dismiss();
            }

        }
    }

    protected onExecListener execListener;

    private onCancelListener cancelListener;

    public void setOnExecListener(onExecListener listener) {
        execListener = listener;
    }

    public void setOnCancelListener(onCancelListener onCancelListener) {
        this.cancelListener = onCancelListener;
    }

    public interface onExecListener {
        public void execSomething();
    }

    public interface onCancelListener {
        public void execCancelSomething();
    }

    public void callBack() {
        if (execListener != null) {
            execListener.execSomething();
        }
    }

    public void callBackCancel() {
        if (cancelListener != null) {
            cancelListener.execCancelSomething();
        }
    }

}
