package android.widget;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import java.lang.ref.WeakReference;

import com.mst.internal.R;
import com.mst.internal.app.AlertController;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import mst.view.menu.BottomMenuListView;
import android.annotation.ArrayRes;
import android.annotation.AttrRes;
import android.annotation.DrawableRes;
import android.annotation.StringRes;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
/**
 * List dialog that show from screen bottom.
 *
 */
public class SpinnerPopupDialog extends Dialog  implements View.OnClickListener{

	
	private TextView mTitleView;
	
	private BottomMenuListView mList;
	
	private Button mNagativeButton;
	
	private Button mPositiveButton;
	
	private boolean mSingleChoice = true;
	
	private ListAdapter mListAdapter;
	
	 private Handler mHandler;
	 
	 private Message mButtonPositiveMessage;
	 
	 private Message mButtonNegativeMessage;
	
	 private View mContentView;
	 
	 private View mButtonDivider;
	 
	 private View mView;
	 
	 private int mViewLayoutResId;
	 
	 private Context mContext;
	 
	 
	public SpinnerPopupDialog(Context context) {
		this(context, com.mst.R.style.Dialog_Spinner);
	}
	
	
	public SpinnerPopupDialog(Context context,int themeResId){
		super(context,themeResId);
		mHandler = new ButtonHandler(this);
		
		mContentView = LayoutInflater.from(getContext()).inflate(com.mst.R.layout.spinner_popup_window, null);
		mList = (BottomMenuListView)mContentView.findViewById(android.R.id.list);
		mTitleView = (TextView)mContentView.findViewById(com.android.internal.R.id.alertTitle);
		mButtonDivider = mContentView.findViewById(com.mst.internal.R.id.dialog_button_divider1);
		
		mNagativeButton = (Button)mContentView.findViewById(android.R.id.button1);
		
		mNagativeButton.setOnClickListener(this);
		
		mPositiveButton = (Button)mContentView.findViewById(android.R.id.button2);
		
		mPositiveButton.setOnClickListener(this);
		
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Window window = getWindow();
		WindowManager.LayoutParams p = window.getAttributes();
		p.width = ViewGroup.LayoutParams.MATCH_PARENT;
		p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		p.dimAmount = 0.6f;
        p.windowAnimations = R.style.popup_dialog_anim;
        p.gravity = Gravity.BOTTOM;
		window.setAttributes(p);
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		
		setCanceledOnTouchOutside(true);
		
		 final ViewGroup customPanel = (ViewGroup) mContentView.findViewById(com.android.internal.R.id.customPanel);
	        setupCustomContent(customPanel);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
//		super.setTitle(title);
		if(mTitleView != null){
			mTitleView.setText(title);
		}
		mTitleView.setVisibility(View.VISIBLE);
		mList.setPadding(mList.getPaddingLeft(), 0, mList.getPaddingRight(), mList.getPaddingBottom());
	}
	
	
	public ListView getListView() {
		// TODO Auto-generated method stub
		return mList;
	}
	
	public BottomMenuListView getMenuView(){
		return mList;
	}
	
	private void updateDividerBackground(){
		mButtonDivider.setBackgroundResource(com.mst.R.color.material_grey_100);
	}
	
	public void setPositiveButton(DialogInterface.OnClickListener listener){
		mButtonPositiveMessage = mHandler.obtainMessage(DialogInterface.BUTTON_POSITIVE,listener);
		mPositiveButton.setVisibility(View.VISIBLE);
		updateDividerBackground();
	}
	
	public void setNegativeButton(DialogInterface.OnClickListener listener){
		mButtonNegativeMessage = mHandler.obtainMessage(DialogInterface.BUTTON_NEGATIVE,listener);
		mNagativeButton.setVisibility(View.VISIBLE);
		updateDividerBackground();
	}

	
	public void setAdapter(ListAdapter adapter,final DialogInterface.OnClickListener listener){
		if(adapter != null){
			mList.setAdapter(adapter);
		     if (listener != null) {
		    	 mList.setOnItemClickListener(new OnItemClickListener() {
	                    @Override
	                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                    	listener.onClick(SpinnerPopupDialog.this, position);
	                    }
	                });
	            }
		}
	}
	
	
	
	public void setSingleChoiceItems(ListAdapter adapter,
			int selectedItemPosition, final OnClickListener listener) {
		// TODO Auto-generated method stub
		mSingleChoice = true;
		updateTitlePanel(mSingleChoice);
		if(adapter != null){
			mList.setAdapter(adapter);
			mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			if(selectedItemPosition != -1){
				mList.setItemChecked(selectedItemPosition, true);
				mList.setSelection(selectedItemPosition);
			}
		     if (listener != null) {
		    	 mList.setOnItemClickListener(new OnItemClickListener() {
	                    @Override
	                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                    	listener.onClick(SpinnerPopupDialog.this, position);
	                    }
	                });
	            }
		}
	}
	
	
	public void setSingleChoiceItems(CharSequence[] entries,
			int selectedItemPosition, final DialogInterface.OnClickListener listener) {
		// TODO Auto-generated method stub
		mSingleChoice = true;
		updateTitlePanel(mSingleChoice);
		mListAdapter = createAdapter(entries);
		setSingleChoiceItems(mListAdapter, selectedItemPosition, listener);
	}
	
	
	public void setMultipleChoiceItems(ListAdapter adapter,
			boolean[] selectedItem, final DialogInterface.OnMultiChoiceClickListener multiChoiceListener) {
		// TODO Auto-generated method stub
		mSingleChoice = false;
		updateTitlePanel(mSingleChoice);
		if(adapter != null){
			mList.setAdapter(adapter);
			mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		     if (multiChoiceListener != null) {
		    	 mList.setOnItemClickListener(new OnItemClickListener() {
	                    @Override
	                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                    	multiChoiceListener.onClick(SpinnerPopupDialog.this, position, mList.isItemChecked(position));
	                    }
	                });
	            }
		}
	}
	
	public void setMultipleChoiceItems(CharSequence[] entries,
			boolean[] selectedItem, final DialogInterface.OnMultiChoiceClickListener multiChoiceListener) {
		// TODO Auto-generated method stub
		mListAdapter = createMultiAdapter(entries, selectedItem, mList);
		setMultipleChoiceItems(mListAdapter, selectedItem, multiChoiceListener);
	}
	
	
	private ListAdapter createMultiAdapter(CharSequence[] entries,final boolean checkedItems[],final ListView listView) {
		// TODO Auto-generated method stub
		return   new ArrayAdapter<CharSequence>(
                getContext(), com.mst.R.layout.spinner_popup_dialog_multichoice_material, com.android.internal.R.id.text1, entries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (checkedItems != null) {
                    boolean isItemChecked = checkedItems[position];
                    if (isItemChecked) {
                        listView.setItemChecked(position, true);
                    }
                }
                return view;
            }
        };
	}

	private void updateTitlePanel(boolean singleChoice){
//		mTitleView.setVisibility(singleChoice?View.GONE:View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Message m = null;
		if (v == mPositiveButton ) {
            m = Message.obtain(mButtonPositiveMessage);
        } else if (v == mNagativeButton) {
            m = Message.obtain(mButtonNegativeMessage);
        }
		if(m != null){
			m.sendToTarget();
		}
		mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, this)
        .sendToTarget();
		
	}
	
	
	
	
	private ListAdapter createAdapter(CharSequence[] entries){
		return new ArrayAdapter<CharSequence>(
                getContext(), com.mst.R.layout.spinner_popup_dialog_singlechoice_material, entries);
	}
	
	
	
	
	 private static final class ButtonHandler extends Handler {
	        // Button clicks have Message.what as the BUTTON{1,2,3} constant
	        private static final int MSG_DISMISS_DIALOG = 1;

	        private WeakReference<DialogInterface> mDialog;

	        public ButtonHandler(DialogInterface dialog) {
	            mDialog = new WeakReference<DialogInterface>(dialog);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {

	                case DialogInterface.BUTTON_POSITIVE:
	                case DialogInterface.BUTTON_NEGATIVE:
	                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
	                    break;

	                case MSG_DISMISS_DIALOG:
	                    ((DialogInterface) msg.obj).dismiss();
	            }
	        }
	    }




	public void setView(View contentView) {
		// TODO Auto-generated method stub
		mView = contentView;
	}
	
	
	public void setView(int contentViewResId) {
		// TODO Auto-generated method stub
		mViewLayoutResId = contentViewResId;
	}
	
    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }

        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup)v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }

        return false;
    }
	
	   private void setupCustomContent(ViewGroup customPanel) {
	        final View customView;
	        if (mView != null) {
	            customView = mView;
	        } else if (mViewLayoutResId != 0) {
	            final LayoutInflater inflater = LayoutInflater.from(mContext);
	            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
	        } else {
	            customView = null;
	        }

	        final boolean hasCustomView = customView != null;
	        if (!hasCustomView || !canTextInput(customView)) {
	            getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	        }

	        if (hasCustomView) {
	            final FrameLayout custom = (FrameLayout) getWindow().findViewById(com.android.internal.R.id.custom);
	            custom.addView(customView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
	        } else {
	            customPanel.setVisibility(View.GONE);
	        }
	    }
	
	
	
	
	

}
