package cn.tcl.music.loaders;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.CursorLoader;

public class GenresLoader extends CursorLoader {

	public GenresLoader(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GenresLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
		
		
	}

	@Override
	public Cursor loadInBackground() {
		
		String[] columnName = new String[]{MediaStore.Audio.Genres.NAME, "Count(*) AS number_of_tracks"}; 
		Uri.Builder uribuilder = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.buildUpon();
		uribuilder.appendPath("all").appendPath("members");
		
		ContentProviderClient client = getContext().getContentResolver().acquireContentProviderClient(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI);
		
		Cursor c = getContext().getContentResolver().query(uribuilder.build(), columnName, null, null, MediaStore.Audio.Genres.Members.GENRE_ID);
		while(c.moveToNext())
		{
			long genreId = c.getLong(0);
			int numTracks = c.getInt(1);
		}
		c.close();
		return null;
	}
}
