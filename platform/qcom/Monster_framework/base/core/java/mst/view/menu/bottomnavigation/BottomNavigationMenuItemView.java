package mst.view.menu.bottomnavigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mst.R;

import mst.view.menu.MstMenuItemImpl;

public class BottomNavigationMenuItemView extends LinearLayout {

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	private int mIconSize;

	private TextView mTextView;
	
	private ImageView mIconOnly;
	
//	private View mButtonParent;
//	
//	private View mIconParent;

	private FrameLayout mActionArea;

	private MstMenuItemImpl mItemData;

	private ColorStateList mIconTintList;
	
	

	public BottomNavigationMenuItemView(Context context) {
		this(context, null);
	}

	public BottomNavigationMenuItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BottomNavigationMenuItemView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(VERTICAL);
		createDefaultBackground();
		LayoutInflater.from(context).inflate(
				R.layout.bottom_navigation_menu_item, this, true);
		initializeView(context);
	}

	protected void initializeView(Context context) {
		mIconSize = context.getResources().getDimensionPixelSize(
				R.dimen.navigation_icon_size);
		mTextView = (TextView) findViewById(R.id.menu_item_text);
		mIconOnly = (ImageView)findViewById(R.id.menu_item_image);
		mTextView.setDuplicateParentStateEnabled(true);
		mIconOnly.setDuplicateParentStateEnabled(true);
		
	}

	public void initialize(MstMenuItemImpl itemData, int menuType) {
		mItemData = itemData;
		setVisibility(itemData.isVisible() ? VISIBLE : GONE);

		setId(itemData.getItemId());
//		super.setEnabled(itemData.isEnabled());
//		Drawable icon = itemData.getIcon();
//		icon.setTintList(mIconTintList);
		setTitle(itemData.getTitle());
		setIcon(itemData.getIcon());
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		mItemData.setEnabled(enabled);
		mIconOnly.setEnabled(enabled);
	}
	

	public void recycle() {
		if (mActionArea != null) {
			mActionArea.removeAllViews();
		}
		mTextView.setCompoundDrawables(null, null, null, null);
	}

	private void createDefaultBackground() {
		setBackgroundResource(com.mst.R.drawable.item_background_borderless_material);
	}
	

	public MstMenuItemImpl getItemData() {
		return mItemData;
	}

	public void setTitle(CharSequence title) {
		mTextView.setText(title);
	}



	public void setIcon(Drawable icon) {
		if (icon != null) {
			icon = icon.mutate();
			icon.setBounds(0, 0, mIconSize, mIconSize);
			icon.setTintList(mIconTintList);
		}
		
			mIconOnly.setImageDrawable(icon);
	}


	public void setIconTintList(ColorStateList tintList) {
		mIconTintList = tintList;
		if (mItemData != null) {
			// Update the icon so that the tint takes effect
			setIcon(mItemData.getIcon());
		}
	}

	public void setTextAppearance(Context context, int textAppearance) {
		mTextView.setTextAppearance(context, textAppearance);
	}

	public void setTextColor(ColorStateList colors) {
		mTextView.setTextColor(colors);
	}

}
