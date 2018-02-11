/* ----------|----------------------|---------------------|-------------------*/
/* 23/06/2015|xiaolong.zhang        |PR1023228            |Radio screen miss all covers      */
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.adapter.live;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.adapter.holders.FooterViewHolder;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.OnDetailItemClickListener;

public class LiveArtistAlbumsAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>{

	public static final String TAG = "LiveArtistAlbumsAdapter";
	private Context context;
	private List<AlbumBean> dataList;
	private boolean isMore = true;
	private int state;
	private OnDetailItemClickListener onDetailItemClickListener;
	private AlbumDetailActivity albumDetailActivity;
	String albumId;
	String albumName;
	private int albumSongCount = 0;

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_FOOTER = 1;
	public LiveArtistAlbumsAdapter(Context context){
		this.context = context;
	}


	public void setIsMore(boolean isMore){
		this.isMore = isMore;
	}

	public void setState(int state){
		this.state = state;
		notifyItemChanged(getItemCount()-1);
	}
	public void addDataList(List<AlbumBean> dataList){
		this.dataList = dataList;
	}

	public void setOnDetailItemClickListener(OnDetailItemClickListener onDetailItemClickListener){
		this.onDetailItemClickListener = onDetailItemClickListener;
	}

	@Override
	public int getItemCount() {
		if(dataList ==null){
			return 1;
		}
		return dataList.size()+1;
	}

	@Override
	public int getItemViewType(int position) {
		if (position + 1 == getItemCount()) {
			return TYPE_FOOTER;
		} else {
			return TYPE_ITEM;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if(TYPE_ITEM==viewType){
			View rootView = LayoutInflater.from(context)
					.inflate(R.layout.item_music_row, parent, false);
            RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_content_live_singer);
            relativeLayout.getLayoutParams().width = SystemUtility.getScreenWidth();
			MusicViewHolder musicViewHolder = new MusicViewHolder(rootView);

			return musicViewHolder;
		}else if(TYPE_FOOTER==viewType){
			View rootView = LayoutInflater.from(context)
					.inflate(R.layout.footer_view, parent, false);
			FooterViewHolder footerViewHolder = new FooterViewHolder(rootView);
			footerViewHolder.footerView.hide(); // MODIFIED by beibei.yang, 2016-06-01,BUG-2226088
			return footerViewHolder;
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
		List<AlbumBean> dataList = this.dataList;
		if(getItemCount()==0){
			return;
		}
		if(viewHolder instanceof MusicViewHolder){
			MusicViewHolder holder = (MusicViewHolder)viewHolder;
			final AlbumBean albumBean = dataList.get(position);
			holder.itemView.setId(R.id.item_detail);
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onDetailItemClickListener != null) {
						onDetailItemClickListener.onClick(v, albumBean, position);
						albumId=albumBean.album_id;
						albumName=albumBean.album_name;
						albumSongCount = albumBean.song_count;
						albumDetailActivity=new AlbumDetailActivity();
						AlbumDetailActivity.launch((Activity) context, albumId, albumName, albumSongCount);
					}
				}
			});
			Glide.with(context)
					.load(albumBean.album_logo)
					.placeholder(R.drawable.default_cover_list)
					.into(holder.mediaArtworkImageView);
			if(TextUtils.isEmpty(albumBean.album_name)){
				holder.mediaTitleTextView.setText(context.getString(R.string.unknown));
			}else {
				holder.mediaTitleTextView.setText(albumBean.album_name);
			}

			StringBuilder stringBuilder = new StringBuilder(context.getString(R.string.num_songs,albumBean.song_count));
			stringBuilder.append("  ");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try{
				String publishDate = Util.timestamp2DateString(albumBean.gmt_publish);
				stringBuilder.append(publishDate);
			}catch (Exception e){
				stringBuilder.append(context.getString(R.string.unknown));
			}
			holder.mediaSubtitleTextView.setText(stringBuilder.toString());
			holder.contextMenuImageButton.setImageResource(R.drawable.picto_right);

			holder.contextMenuImageButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view) {
						albumId=albumBean.album_id;
						albumName=albumBean.album_name;
					    albumSongCount = albumBean.song_count;
						albumDetailActivity=new AlbumDetailActivity();
						AlbumDetailActivity.launch((Activity) context, albumId, albumName, albumSongCount);
				}
			});
		}else if(viewHolder instanceof FooterViewHolder){
			FooterViewHolder footerViewHolder = (FooterViewHolder)viewHolder;
			if(!isMore){
				footerViewHolder.itemView.setVisibility(View.GONE);
			}else {
				FooterViewHolder holder = (FooterViewHolder)viewHolder;
				if(!isMore){
					holder.itemView.setVisibility(View.GONE);
				}else {
					holder.footerView.setState(state);
				}
			}
		}

	}

	static class MusicViewHolder extends RecyclerView.ViewHolder{

		public TextView mediaTitleTextView;
		public TextView mediaSubtitleTextView;
		public ImageView mediaArtworkImageView;
		public ImageView contextMenuImageButton;

		public MusicViewHolder(View itemView) {
			super(itemView);

			ViewGroup parent = (ViewGroup) itemView;

			mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
			mediaSubtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
			mediaArtworkImageView = (ImageView) parent.findViewById(R.id.artwork_image_view);
			contextMenuImageButton = (ImageView) parent.findViewById(R.id.item_menu_image_button);
		}
	}

}
