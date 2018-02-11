package cn.download.mie.base;

public enum DirType {
	root,
	log,
	image,
	cache,
	crash,
	lyric,
	song,
	apk;

	public int value()
	{
		return ordinal() + 1;
	}
}
