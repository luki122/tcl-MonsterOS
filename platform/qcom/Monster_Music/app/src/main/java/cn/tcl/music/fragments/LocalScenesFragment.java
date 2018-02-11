package cn.tcl.music.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.ScenesDetailActivity;
import cn.tcl.music.adapter.ScenesAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.ScenesBean;
import cn.tcl.music.util.LogUtil;

public class LocalScenesFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = LocalScenesFragment.class.getSimpleName();
    private View rootView;
    private GridView mGridView;
    private Context mContext;
    private ScenesAdapter mScenesAdapter;
    private List<ScenesBean> mList;
    private final static int OTHER_SCENES = 9;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scenes, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mContext = getActivity();
        initData();
        mScenesAdapter = new ScenesAdapter(mContext, mList);
        mGridView.setAdapter(mScenesAdapter);
        mGridView.setOnItemClickListener(this);
        return rootView;
    }

    private void initData() {
        mList = new ArrayList<ScenesBean>();
        mContext = getContext();
        Cursor cursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Scenes.CONTENT_URI, null, null, null, MusicMediaDatabaseHelper.Scenes.ScenesColumns._ID);
        if (cursor != null && !cursor.isClosed()) {
            LogUtil.d(TAG,"cursor count is " + cursor.getCount());
            int[] imageBimap = new int[]{R.drawable.scenes1, R.drawable.scenes2,
                    R.drawable.scenes3, R.drawable.scenes4,
                    R.drawable.scenes5, R.drawable.scenes6,
                    R.drawable.scenes7, R.drawable.scenes8,
                    R.drawable.scenes9, R.drawable.scenes10};
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                ScenesBean bean = new ScenesBean();
                bean.setScenesId(cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Scenes.ScenesColumns._ID)));
                bean.setScenesText(cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Scenes.ScenesColumns.SCENES_TITLE)));
                bean.setScenesIcon(imageBimap[i]);
                mList.add(bean);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }
    private OnFragmentInteractionListener mListener;

    public LocalScenesFragment() {
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (position == OTHER_SCENES) {
            Intent intent = new Intent(getActivity(), ScenesDetailActivity.class);
            intent.putExtra(CommonConstants.BUNDLE_KEY_SCENE, mList.get(position));
            startActivity(intent);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
