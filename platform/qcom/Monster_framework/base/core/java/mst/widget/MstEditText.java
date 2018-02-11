package mst.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class MstEditText extends EditText {
	
	private CharSequence mErrorText;
	

	public MstEditText(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public MstEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public MstEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MstEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	public void setErrorText(CharSequence errorText){
		this.mErrorText = errorText;
	}
	

	
	
}
