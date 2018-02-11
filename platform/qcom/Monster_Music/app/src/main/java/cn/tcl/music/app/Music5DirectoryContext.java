package cn.tcl.music.app;

import android.content.Context;

import com.tcl.framework.fs.Directory;
import com.tcl.framework.fs.DirectroyContext;
import com.tcl.framework.util.TimeConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.download.mie.base.DirType;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/10/30 10:30
 * @copyright TCL-MIE
 */
public class Music5DirectoryContext extends DirectroyContext {

    private Context mContext;

    public Music5DirectoryContext(Context context, String appName)
    {
        super(context);
        this.mContext = context;
    }

    @Override
    protected Collection<Directory> initDirectories()
    {
        List<Directory> children = new ArrayList<Directory>();

        Directory dir = newDirectory(DirType.log);
        children.add(dir);
        dir = newDirectory(DirType.image);
        children.add(dir);
        dir = newDirectory(DirType.crash);
        children.add(dir);
        dir = newDirectory(DirType.cache);
        children.add(dir);
        dir = newDirectory(DirType.lyric);
        children.add(dir);
        dir = newDirectory(DirType.song);
        children.add(dir);
        dir = newDirectory(DirType.apk);
        children.add(dir);
        return children;
    }

    private Directory newDirectory(DirType type) {
        Directory child = new Directory(type.toString(), null);
        child.setType(type.name());
        if (type.equals(DirType.cache))
        {
            child.setForCache(true);
            child.setExpiredTime(TimeConstants.ONE_DAY_MS);
        }
        return child;
    }
}
