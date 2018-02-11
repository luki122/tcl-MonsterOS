package com.android.contacts.activities;

//add by liyang 2016－11－1
import com.mst.t9search.ContactsHelper.OnContactsLoad;
import com.mst.t9search.Contacts;
import com.mst.t9search.ContactsHelper;
import com.mst.t9search.ViewUtil;
import com.t9search.util.PinyinUtil;
import mst.widget.toolbar.Toolbar;
import android.widget.MstSearchView;
import android.widget.MstSearchView.OnQueryTextListener;
import android.widget.MstSearchView.OnCloseListener;
import android.widget.MstSearchView.OnSuggestionListener;
import com.android.contacts.common.mst.ContactForIndex;
import com.android.contacts.common.mst.DensityUtil;
import com.android.contacts.common.mst.FragmentCallbacks;
import com.android.contacts.common.util.MstUtils;
import com.android.contacts.R;
import com.mediatek.contacts.list.ContactListMultiChoiceActivity;
import com.mediatek.contacts.list.ContactsRequestAction;
import com.t9search.util.PinyinUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.MstIndexBar;
import mst.widget.ActionMode.Item;
import mst.widget.MstIndexBar.Letter;
import android.app.Activity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MstSimContactsActivity extends mst.app.MstActivity implements MstIndexBar.OnTouchStateChangedListener,MstIndexBar.OnSelectListener
,OnScrollListener,OnContactsLoad{
	private static final String TAG = "MstSimContactsActivity";
	private ActionMode actionMode;
	private BottomNavigationView bottomBar;

	//	private TextView mEmptyText;
	private ListView listView;
//	private SimpleAdapter adapter;
	private MstSimContactsAdapter mstSimContactsAdapter;

	//	private List<Map<String,Object>> mData=null;
	private List<Contacts> contacts;
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		bindViews();	

		initializeSearchView();

		Bundle bundle=getIntent().getExtras();
		if(bundle==null) return;
		String[] names=bundle.getStringArray("name");
		String[] numbers=bundle.getStringArray("number");
		int[] types=bundle.getIntArray("type");
		str_other = getString(R.string.mst_other_string);
		//		mData= new ArrayList<Map<String,Object>>();
		char c2=0;
		Contacts cs=null;
		String sortOrder=null;
		contacts = new ArrayList<Contacts>();
		for(int i =0; i < names.length; i++) {    
			//			Map<String,Object> item = new HashMap<String,Object>();
			//
			String[] strings=MstUtils.getFullSpell(names[i]);

			c2=strings[0].charAt(0);
			if(!(c2>=65&&c2<=90)){
				//				item.put("sortOrder","ZZZZZZZ"+strings[0]);
				sortOrder="ZZZZZZZ"+strings[0];
			}else{
				//				item.put("sortOrder",strings[0]);
				sortOrder=strings[0];
			}
			//			item.put("quanpinyin",strings[0]);
			//			item.put("jianpinyin",strings[1]);
			//			item.put("name", names[i]); 
			//			item.put("number", numbers[i]);
			//			item.put("type", types[i]);
			//			mData.add(item);
			cs = new Contacts(names[i], numbers[i], types[i],sortOrder,strings[0],strings[1],5,i);
			PinyinUtil.parse(cs.getNamePinyinSearchUnits());
			contacts.add(cs);
		}

		//		for(Map map:mData){
		//			Log.d(TAG,"map:"+map);
		//		}
		//		Log.d(TAG,"mData:"+mData);

		mLetterComparator=new AlphabetComparator();
		Collections.sort(contacts, mLetterComparator);		

		if(mContactsHelper==null) {
			mContactsHelper=new ContactsHelper();
		}
		mContactsHelper.setContext(MstSimContactsActivity.this);
		mContactsHelper.setOnContactsLoad(MstSimContactsActivity.this);
		boolean startLoad = mContactsHelper.startLoadContactsForChoiceSearch(contacts);

		mstSimContactsAdapter=new MstSimContactsAdapter(mContactsHelper
				.getSearchContacts());
		listView.setAdapter(mstSimContactsAdapter);

		if(mIndexBar!=null){
			mTask=null;
			mTask =new InitIndexBarTask();
			mTask.execute();
		}

		//		Log.d(TAG,"mData1:"+mData);

		createCheckedArray(contacts.size());
		//		adapter = new SimpleAdapter(  
		//				this,  
		//				mData,  
		//				com.mst.R.layout.list_item_2_line_multiple_choice, 
		//				COLUMN_NAMES, 
		//				VIEW_NAMES){
		//
		//			@Override    
		//			public View getView(final int position, View convertView, ViewGroup parent) {	
		//				View view = super.getView(position, convertView, parent);  
		//				CheckBox checkBox=(CheckBox)view.findViewById(android.R.id.button1);
		//				checkBox.setChecked(getCheckedArrayValue(position));
		//				return view;
		//			}
		//		};

		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Contacts contact=mContactsHelper.getSearchContacts().get((int)arg3);
				Log.d(TAG,"arg0:"+arg0+" arg1:"+arg1+" arg2:"+arg2+" arg3:"+arg3);
				CheckBox checkBox=(CheckBox)arg1.findViewById(android.R.id.button1);
				//				Log.d(TAG,"checkBox:"+checkBox);
				checkBox.toggle();
				setCheckedArrayValue((int)contact.getContactId(), checkBox.isChecked());
				updateActionMode();
			}});

		header= inflater.inflate(R.layout.mst_listview_search_header, null, false);
		header.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"search header onclick");
				showHeader(false);				
				mSearchView.clearFocus();
				showActionMode(false);
				mSearchView.setVisibility(View.VISIBLE);
				mSearchView.requestFocus();
				mIndexBar.setVisibility(View.GONE);
				toolbar.setBackgroundColor(getResources().getColor(R.color.mst_toolbar_background_color));
				//				toolbar.getNavigationIcon().setVisible(false, false);
				//				backIcon.setVisibility(View.VISIBLE);
				getWindow().setStatusBarColor(getResources().getColor(R.color.mst_toolbar_background_color)); 
				listView.setVisibility(View.GONE);
			}
		});
		listView.addHeaderView(header, null, true);
	}

	@Override
	public void onContactsLoadSuccess() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onContactsLoadSuccess");
		//		setLoadSearchContactsState(2);
		//		Log.d(TAG,"isWaitingQuerying:"+isWaitingQuerying);
		//
		//		if(isWaitingQuerying){
		//			setQueryStringMst(mQueryString);
		//			isWaitingQuerying=false;
		//			//			mSearchZero.setVisibility(View.GONE);
		//			showSearchProgress(false);
		//		}
		//		showSearchProgress(false);


	}

	@Override
	public void onContactsLoadFailed() {

	}
	LayoutInflater inflater;
	private void initializeSearchView() {
		// TODO Auto-generated method stub
		inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		//		View backIconView=inflater.inflate(R.layout.mst_back_icon_view, null);
		//
		//		backIcon=(ImageView)backIconView.findViewById(R.id.mst_back_icon_img);
		//		backIconView.setOnClickListener(new View.OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				Log.d(TAG,"backIconView onclick");
		//				// TODO Auto-generated method stub
		//				back();
		//			}
		//		});
		//		backIcon.setVisibility(View.GONE);
		//		backIcon.setColorFilter(getResources().getColor(R.color.mst_toolbar_icon_normal_color));
		//		toolbar.addView(backIconView,
		//				new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));

		mSearchContainer = inflater.inflate(R.layout.mst_search_bar_expanded,
				null);
		mSearchView = (MstSearchView) mSearchContainer
				.findViewById(R.id.search_view);
		//		toolbar.setBackgroundColor(getResources().getColor(R.color.mst_toolbar_background_color));
		mSearchView.needHintIcon(false);
		//		mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		//		mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
		//		Log.d(TAG,"mSearchContainer:"+mSearchContainer);
		toolbar.addView(mSearchContainer,
				new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));
		//		mSearchView = (MstSearchView)mToolbar.getMenu().findItem(R.id.menu_search).getActionView();
		mSearchView.setIconifiedByDefault(false);
		mSearchView.setQueryHint(getString(R.string.mst_search_contacts_hint));
		mSearchView.setQueryHintTextColor(getResources().getColor(R.color.mst_searchview_hint_text_color));
		mSearchView.setOnQueryTextListener(new OnQueryTextListener(){
			@Override
			public boolean onQueryTextChange(String queryString) {
				Log.d(TAG,"onQueryTextChange,queryString:"+queryString);
				if(TextUtils.equals(queryString, mQueryString)) return false;
				mQueryString=queryString;
				mContactsHelper.queryForChoiceSearch(mQueryString);

				refreshContactsLv();
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String str) {
				return false;
			}
		});

		mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus){
				if(hasFocus){
					Log.d(TAG,"hasFocus,v:"+v);
					showInputMethod(mSearchView.findFocus());
					mIsSearchMode=true;					
					listView.setBackgroundColor(getResources().getColor(R.color.contact_main_background));
					mContactsHelper.t9Search(null);
				}
			}
		});

	}
	private TextView mSearchZero;
	private void refreshContactsLv() {
		if (null == listView) {
			return;
		}

		Log.d(TAG,"mListView:"+listView+"\ncontactsAdapter:"+mstSimContactsAdapter+"\nlistview.getAdapter:"+listView.getAdapter()
				+"\nsize:"+mContactsHelper
				.getSearchContacts().size()+"\nmContactsAdapter.getCount() :"+mstSimContactsAdapter.getCount());
		if (null != mstSimContactsAdapter) {
			mstSimContactsAdapter.notifyDataSetChanged();
			if (mstSimContactsAdapter.getCount() > 0) {
				if(mSearchZero!=null) mSearchZero.setVisibility(View.GONE);
				ViewUtil.showView(listView);
			} else {
				if(mSearchZero!=null){
					mSearchZero.setVisibility(View.VISIBLE);
					mSearchZero.setText(R.string.mst_search_contacts_empty);
				}
				ViewUtil.hideView(listView);
			}
		}
	}

	private String mQueryString;
	View header;
	public void showHeader(boolean show){
		if(show){
			header.setVisibility(View.VISIBLE);
			header.setPadding(0, 0, 0, 0);
		}else{
			header.setVisibility(View.GONE);
			header.setPadding(0, -2000, 0, 0);
		}
	}

	/**
	 * Hide software keyboard for the given {@link View}.
	 */
	public void hideInputMethod(Context context, View view) {
		if(view==null) return;
		InputMethodManager imm = (InputMethodManager) context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void back(){
		Log.d(TAG, "[back],mIsSearchMode:"+mIsSearchMode);
		if (mSearchView != null && !mSearchView.isFocused()&&mIsSearchMode) {
			if (!TextUtils.isEmpty(mSearchView.getQuery())) {
				mSearchView.setQuery(null, true);
			}
			hideInputMethod(MstSimContactsActivity.this,mSearchView.findFocus());
			//			mListFragment.updateSelectedItemsView();
			showHeader(true);
			mSearchView.clearFocus();
			mSearchView.setVisibility(View.GONE);
			showActionMode(true);
			mIndexBar.setVisibility(View.VISIBLE);
			//			mListFragment.startLoad();
			mIsSearchMode=false;
			toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
			//			toolbar.getNavigationIcon().setVisible(true, false);
			//			backIcon.setVisibility(View.GONE);
			getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar_background_color)); 
			return;
		}
		Log.d(TAG,"back1");
		super.onBackPressed();
	}

	public void onBackPressed() {
		Log.i(TAG, "[onBackPressed]");
		back();	
	}

	private boolean mIsSearchMode = false;

	public void showInputMethod(View view) {
		if(view==null) return;
		final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			//			Log.d(TAG,"showinput");
			imm.showSoftInput(view, 0);
		}
	}
	public MstSearchView mSearchView;
	public View mSearchContainer;
	private Toolbar toolbar;

	public class MstSimContactsAdapter extends BaseAdapter{

		private List<Contacts> contacts;
		public MstSimContactsAdapter(List<Contacts> contacts){
			Log.d(TAG,"contacts,:"+contacts.size());
			this.contacts=contacts;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return contacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = null;
			ViewHolder viewHolder;

			//			Log.d(TAG,"getView");
			Contacts map=contacts.get(position);


			if (convertView == null) {
				view = View.inflate(MstSimContactsActivity.this, R.layout.mst_sim_contacts_import_item, null);
				viewHolder = new ViewHolder();
				viewHolder.name=(TextView)view.findViewById(android.R.id.text1);
				viewHolder.number=(TextView)view.findViewById(android.R.id.text2);
				viewHolder.checkBox=(CheckBox)view.findViewById(android.R.id.button1);
				viewHolder.header=(TextView)view.findViewById(R.id.listview_item_header);
				view.setTag(viewHolder);
			}else{
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			switch (map.getSearchByType()) {
			case SearchByNull:
				ViewUtil.showTextNormal(viewHolder.name, map.getName());
				ViewUtil.showTextNormal(viewHolder.number,
						map.getPhoneNumber());
				break;
			case SearchByPhoneNumber:
				ViewUtil.showTextNormal(viewHolder.name, map.getName());
				ViewUtil.showTextHighlight(viewHolder.number, map.getmPhoneNumber(), map.getMatchKeywords().toString());
				break;
			case SearchByName:
				ViewUtil.showTextHighlight(viewHolder.name, map.getName(),
						map.getMatchKeywords().toString());
				ViewUtil.showTextNormal(viewHolder.number,
						map.getmPhoneNumber());
				break;
			}


			//			viewHolder.name.setText(map.getName());
			//			viewHolder.number.setText(map.getmPhoneNumber());
			viewHolder.checkBox.setChecked(getCheckedArrayValue((int)map.getContactId()));

			if(position>0){
				Contacts preMap=contacts.get(position-1);
				String c1=null;
				String c2=null;			
				if(TextUtils.isEmpty(map.getQuanpinyin().toString())){
					c1=str_other;
				}else{
					if(map.getSortOrder().startsWith("ZZZZZZZ")){
						c1=str_other;
					}else{
						c1=map.getQuanpinyin().substring(0,1);
					}
				}

				if(TextUtils.isEmpty(preMap.getQuanpinyin())){
					c2=str_other;
				}else{
					if(preMap.getSortOrder().startsWith("ZZZZZZZ")){
						c2=str_other;
					}else{
						c2=preMap.getQuanpinyin().substring(0,1);
					}
				}

				//				Log.d(TAG,"position:"+position+" c1:"+c1+" c2:"+c2);
				if(!TextUtils.equals(c1, c2)){
					viewHolder.header.setText(c1);
					viewHolder.header.setVisibility(View.VISIBLE);
				}else{
					viewHolder.header.setVisibility(View.GONE);
				}
			}else{
				viewHolder.header.setText(TextUtils.isEmpty(map.getQuanpinyin())?str_other:map.getQuanpinyin().substring(0,1));
				viewHolder.header.setVisibility(View.VISIBLE);
			}

			return view;
		}	
	}
	public enum SearchByType{
		SearchByNull,
		SearchByName,
		SearchByPhoneNumber,
	}
	protected class ViewHolder {
		//		public QuickContactBadge quickContactBadge;
		public TextView name;
		public TextView header;
		public TextView number;
		public CheckBox checkBox;
	}
	/** 
	 * 按名字对列表进行排序 
	 * */  
	private AlphabetComparator mLetterComparator;
	public class AlphabetComparator implements Comparator<Contacts> {

		// java提供的对照器  
		//		private RuleBasedCollator collator = null;  

		/** 
		 * 默认构造器是按中文字母表进行排序 
		 * */  
		public AlphabetComparator() {
			//			collator = (RuleBasedCollator) Collator  
			//					.getInstance(java.util.Locale.CHINA);  
		}  

		/** 
		 * 可以通过传入Locale值实现按不同语言进行排序
		 * */  
		public AlphabetComparator(Locale locale) { 
			//			collator = (RuleBasedCollator) Collator.getInstance(locale);  
		}  

		public int compare(Contacts obj1, Contacts obj2) {  
			//			CollationKey c1 = collator.getCollationKey(obj1.text);  
			//			CollationKey c2 = collator.getCollationKey(obj2.text);  
			//
			//			return collator.compare(((CollationKey) c1).getSourceString(),  
			//					((CollationKey) c2).getSourceString());  
			//obj1是下一个比较的对象
			String c1 =obj1.getSortOrder();
			String c2 =obj2.getSortOrder();

			return c1.compareTo(c2);
		}  
	}

	@Override
	public void onStateChanged(MstIndexBar.TouchState old, MstIndexBar.TouchState news) {
		//		Log.d(TAG,"Touch state : "+news);
		//		if(mCallbacks==null) return;
		//		if(getAdapter().isSelectMode()) return;
		//		if(TextUtils.equals("DOWN", news.toString())){
		//			mCallbacks.onFragmentCallback(FragmentCallbacks.SHOW_ADD_FAB, 0);
		//		}else if(TextUtils.equals("UP", news.toString())){
		//			mCallbacks.onFragmentCallback(FragmentCallbacks.SHOW_ADD_FAB, 1);
		//		}
	}

	@Override
	public void onSelect(int index, int layer, MstIndexBar.Letter letter) {
		//		Log.d(TAG,"onSelect0,index:"+index+" layer:"+layer+" letter:"+letter.text);
		if(index==0&&layer == 0){
			listView.setSelection(0);
			return;
		}
		int listindex = letter.list_index;
		//		Log.d(TAG,"listindex0:"+listindex);
		listindex--;
		if(layer == 0){
		}
		int offset=0;
		for(int k:indexArrayList){
			if(k<listindex) offset++;
			if(k>=listindex) break;
		}
		Log.d(TAG,"onSelect,index:"+index+" letter:"+letter.text+" listindex:"+listindex+" offset:"+offset
				+" indexArrayList:"+indexArrayList+"getListView().getHeaderViewsCount():"+listView.getHeaderViewsCount());

		listView.setSelectionFromTop(listindex+listView.getHeaderViewsCount()-offset,-6);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		Contacts contact = contacts.get(firstVisibleItem);
		if(contact != null) {
			int index = -1;
			if (!TextUtils.isEmpty(contact.getQuanpinyin())) {
				String fir = contact.getQuanpinyin().substring(0,1);
				index = mIndexBar.getIndex(fir);
				//                Log.d(TAG,"onScroll1,firstVisi:"+firstVisibleItem+" fir:"+fir+" index:"+index);
			} else {
				String fir = "#";
				index = mIndexBar.getIndex(fir);
				//                Log.d(TAG,"onScroll2,firstVisi:"+firstVisibleItem+" fir:"+fir+" index:"+index);
			}


			if(index == -1){
				index = mIndexBar.size() - 1;
			}

			mIndexBar.setFocus(index);
		}

	}
	private String str_other = null;
	private enum TaskStatus {
		NEW, RUNNING, FINISHED, CANCELED
	}
	private ContactsHelper mContactsHelper;
	protected ArrayList<ContactForIndex> contactForIndexs;
	protected HashMap<String, Integer> indexHashMap;
	protected ArrayList<Integer> indexArrayList;
	private class InitIndexBarTask extends AsyncTask<Void, Void, Boolean> {
		private InitIndexBarTask mInstance = null;
		private TaskStatus mTaskStatus;
		private boolean mResult;


		private InitIndexBarTask() {
			super();
			mTaskStatus = TaskStatus.NEW;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mTaskStatus = TaskStatus.CANCELED;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG,"onPostExecute");
			mIndexBar.invalidate();
			//			mIndexBar.setVisibility(result?View.VISIBLE:View.GONE);			
			super.onPostExecute(result);
		}


		@Override
		protected Boolean doInBackground(Void... params) {		
			if(contacts==null||contacts.size()==0) return false;
			contactForIndexs=new ArrayList<ContactForIndex>();
			indexHashMap=new HashMap<String, Integer>();
			indexArrayList=new ArrayList<Integer>();
			String last = null;
			String pinyin="*";
			String jian="*";
			String name=null;
			String sortOrder=null;
			ContactForIndex contactForIndex;
			String fistLetter;
			ContactForIndex g;
			int indexCount=0;
			boolean hasAddOther=false;

			int i=0;
			for(Contacts map:contacts){
				contactForIndex=new ContactForIndex();
				name=map.getName();
				pinyin=map.getQuanpinyin();
				jian=map.getJianpinyin();
				sortOrder=map.getSortOrder();

				contactForIndex.name=name;
				contactForIndex.pinyin=pinyin;

				if(!sortOrder.startsWith("ZZZZZZZ")){
					for (int k = 0;k<jian.length(); k++) {
						fistLetter = jian.substring(k,k+1);
						if(k == 0){
							if(!fistLetter.equals(last)){
								g = new ContactForIndex();
								g.name = fistLetter;
								g.type = 1;
								contactForIndexs.add(g);
								indexHashMap.put(fistLetter,indexCount);
								indexArrayList.add(i+indexCount);
								indexCount++;
							}
							last = fistLetter;
						}
						contactForIndex.firstLetter.add(fistLetter);
					}
				}else{
					if(!hasAddOther){
						g = new ContactForIndex();
						g.name = str_other;
						g.type = 1;
						contactForIndexs.add(g);
						hasAddOther=true;
					}
					contactForIndex.firstLetter.add(str_other);
				}

				contactForIndexs.add(contactForIndex);
				i++;
			}

			initIndexBar(contactForIndexs);

			return true;

		}

		public boolean isTaskRunning() {
			return mTaskStatus == TaskStatus.RUNNING;
		}

		public boolean getResult() {
			return mResult;
		}

		public boolean isTaskFinished() {
			return mTaskStatus == TaskStatus.FINISHED;
		}

		public void abort() {
			if (mInstance != null) {
				Log.d(TAG, "mInstance.cancel(true)");
				mInstance.cancel(true);
				mInstance = null;
			}
		}

		public InitIndexBarTask createNewTask() {
			if (mInstance != null) {
				Log.d(TAG, "cancel existing task instance");
				mInstance.abort();
			}
			mInstance = new InitIndexBarTask();
			return mInstance;
		}

		public InitIndexBarTask getExistTask() {
			return mInstance;
		}
	}

	private void initIndexBar(ArrayList<ContactForIndex> array){
		//		for(ContactForIndex item:array){
		//			Log.d(TAG,"item:"+item.toString());
		//		}
		if(array==null) return;
		for(int m=0;m<28;m++){
			mIndexBar.setEnables(false,m);
		}
		//		if(mStarredCount>0) mIndexBar.setEnables(true,0);

		List<MstIndexBar.Letter> sub = null;
		String last = "";
		int lastindex = -1;
		int otherindex = -1;
		boolean changed = false;
		ContactForIndex c=null;
		int namesize;
		int index;
		String firletter=null,secletter=null;
		Letter letter=null;
		Letter letter2=null;
		for(int p=0;p<array.size();p++){
			c = (ContactForIndex) array.get(p);
			namesize = c.firstLetter.size();
			firletter = "";
			secletter = "";
			for(int i=0;i<namesize;i++) {
				if(i == 0) {
					firletter = c.firstLetter.get(0);
					changed = !firletter.equals(last);
					last = firletter;
				}else if(i == 1){
					secletter = c.firstLetter.get(1);
				}
			}
			if(changed){
				if(sub != null && lastindex != -1){
					mIndexBar.setSubList(lastindex,sub);
				}
				if(!"".equals(firletter)) {
					index = mIndexBar.getIndex(firletter);
					if(index != -1) {
						lastindex = index;
						sub = new ArrayList<>();
					}else{
						sub = null;
					}
					if(index == -1){//其他（#）的索引
						index = 27;
						if(otherindex == -1){
							otherindex = p;
						}
					}
					//设置第一个字母对应的列表索引
					letter = mIndexBar.getLetter(index);
					if(letter != null){
						//						Log.d(TAG,"index:"+index+" letter:"+letter.text+" list_index:"+p);
						letter.list_index = /*index == 1 ? otherindex : */p;
					}
					mIndexBar.setEnables(true,index);
				}
			}
			//设置第二个字母的列表索引
			if(sub != null && secletter != "") {
				if(!sub.contains(Letter.valueOf(secletter))) {
					letter2 = Letter.valueOf(secletter);
					letter2.enable = true;
					letter2.list_index = p;
					sub.add(letter2);
				}
			}
		}
		if(sub != null && lastindex != -1){
			//			Log.d(TAG,"setSubList2,lastindex:"+lastindex+" sub:"+sub);

			//			Collections.sort(sub, mLetterComparator);
			mIndexBar.setSubList(lastindex,sub);
		}
	}


	private InitIndexBarTask mTask;


	public void bindViews(){
		setMstContentView(R.layout.mst_sim_contacts_list);
		//        mEmptyText = (TextView) findViewById(android.R.id.empty);
		toolbar = getToolbar();
		//		toolbar.setElevation(0f);		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"NavigationOnClickListener");
				if(mIsSearchMode){
					back();
				}else{
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			}
		});

		setupActionModeWithDecor(toolbar);
		toolbar.setElevation(0f);
		listView=(ListView)findViewById(android.R.id.list);
		if(mSearchZero==null){		
			//			mSearchProgress = getView().findViewById(R.id.search_progress);
			mSearchZero=(TextView)findViewById(R.id.mSearchZero);
		}
		actionMode=getActionMode();
		actionMode.setNagativeText(getString(R.string.mst_cancel));
		actionMode.setPositiveText(getString(R.string.mst_actionmode_selectall));
		actionMode.bindActionModeListener(new ActionModeListener(){
			/**
			 * ActionMode上面的操作按钮点击时触发，在这个回调中，默认提供两个ID使用，
			 * 确定按钮的ID是ActionMode.POSITIVE_BUTTON,取消按钮的ID是ActionMode.NAGATIVE_BUTTON
			 * @param view
			 */
			public void onActionItemClicked(Item item){
				Log.d(TAG,"onActionItemClicked,itemid:"+item.getItemId());
				switch (item.getItemId()) {
				case ActionMode.POSITIVE_BUTTON:	{
					boolean mIsSelectedAll = isAllSelect();
					if(mIsSelectedAll){
						setAllSelect(false);
					}else{
						setAllSelect(true);
					}
					updateActionMode();
					listView.setAdapter(mstSimContactsAdapter);
					mstSimContactsAdapter.notifyDataSetChanged();
					break;
				}

				case ActionMode.NAGATIVE_BUTTON:
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
				default:
					break;
				}
			}

			/**
			 * ActionMode显示的时候触发
			 * @param actionMode
			 */
			public void onActionModeShow(ActionMode actionMode){

			}

			/**
			 * ActionMode消失的时候触发
			 * @param actionMode
			 */
			public void onActionModeDismiss(ActionMode actionMode){

			}
		});


		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				showActionMode(true);
			}
		},300);

		bottomBar = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
		bottomBar.setVisibility(View.GONE);
		bottomBar.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {

			@Override
			public boolean onNavigationItemSelected(MenuItem arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onNavigationItemSelected,arg0.getItemId():"+arg0.getItemId());
				switch (arg0.getItemId()) {
				case R.id.mst_ok:
					int checkedCount=getCheckedCount();

					if(checkedCount>0){
						ArrayList<Integer> ids=new ArrayList<Integer>();
						for(int i=0;i<count;i++){
							if(getCheckedArrayValue(i)){
								ids.add(i);
							}
						}
						Log.d(TAG,"ids:"+ids);

						final Intent retIntent = new Intent();
						retIntent.putIntegerArrayListExtra("ids", ids);
						MstSimContactsActivity.this.setResult(Activity.RESULT_OK, retIntent);
						MstSimContactsActivity.this.finish();
					}else{
						Toast.makeText(MstSimContactsActivity.this, getString(R.string.mst_import_sim_contacts_choose_first), Toast.LENGTH_LONG).show();
					}


					break;

				default:
					break;
				} 
				return false;
			}
		});

		mIndexBar = (MstIndexBar) findViewById(R.id.index_bar);
		mIndexBar.setOnSelectListener(this);
		mIndexBar.setOnTouchStateChangedListener(this);		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//		if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
		//			mPhotoManager.pause();
		//		} else if (isPhotoLoaderEnabled()) {
		//			mPhotoManager.resume();
		//		}
	}

	private MstIndexBar mIndexBar;
	public void updateActionMode(){
		if(isAllSelect()){
			actionMode.setPositiveText(getString(R.string.mst_actionmode_selectnone));
		}else{
			actionMode.setPositiveText(getString(R.string.mst_actionmode_selectall));
		}
		actionMode.setTitle(String.format(
				getString(R.string.mst_menu_actionbar_selected_items),
				getCheckedCount()));	

		if(getCheckedCount()>0){
			if(bottomBar.getVisibility()!=View.VISIBLE){
				bottomBar.setVisibility(View.VISIBLE);
			}
		}else{
			if(bottomBar.getVisibility()!=View.GONE){
				bottomBar.setVisibility(View.GONE);
			}
		}
	}

	private boolean[] checkeds;
	private boolean is_all_checked = false;
	private int count;
	public void createCheckedArray(int count) {
		this.count=count;
		if (checkeds == null || count != checkeds.length)
			checkeds = new boolean[count];
		for (int i = 0; i < count; i++)
			checkeds[i] = false;
	}


	public void setCheckedArrayValue(int position, boolean value) {
		Log.d(TAG,"setCheckedArrayValue,po:"+position+" value:"+value);
		checkeds[position] = value;
	}

	public void setAllSelect(boolean isAllselect){
		for(int i=0;i<checkeds.length;i++){
			checkeds[i]=isAllselect;
		}
	}

	public boolean getCheckedArrayValue(int position) {
		return checkeds[position];
	}

	public boolean isAllSelect() {
		for (int i = 0; i < checkeds.length; i++) {
			if (!checkeds[i])
				return false;
		}
		return true;
	}

	public int getCheckedCount() {
		int mChecked = 0;
		for (int i = 0; i < checkeds.length; i++) {
			if (checkeds[i]) {
				mChecked++;
			}
		}
		return mChecked;
	}

	public void clearAllcheckes() {
		for (int i = 0; i < checkeds.length; i++)
			checkeds[i] = false;
	}	


	private static final String[] COLUMN_NAMES = new String[] {
		"name",
		"number"
	};
	protected static final int NAME_COLUMN = 0;
	protected static final int NUMBER_COLUMN = 1;
	protected static final int EMAILS_COLUMN = 2;

	private static final int[] VIEW_NAMES = new int[] {
		android.R.id.text1,
		android.R.id.text2
	};
}
