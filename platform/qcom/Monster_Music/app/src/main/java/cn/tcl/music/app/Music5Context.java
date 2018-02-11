package cn.tcl.music.app;

import android.content.Context;

import com.tcl.framework.fs.DirectoryManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.download.mie.base.DirType;
import cn.download.mie.base.util.ServiceContext;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/10/30 10:28
 * @copyright TCL-MIE
 */
public class Music5Context extends ServiceContext {

    private final static String TAG = "Music5Context";
    private final static String GC_ROOT_FOLDER = "Music5";
    public static final String DIR_MANAGER = "dir";
    public static volatile boolean isInitSuccess = false;

    public static boolean initInstance(Context context) {
        if (!isInitSuccess || _instance == null) {
            Music5Context gcContext = new Music5Context(context);

            _instance = gcContext;
            isInitSuccess = gcContext.init();
            return isInitSuccess;
        }
        return true;
    }

    private Map<String, Object> objsMap;

    public Music5Context(Context context) {
        super(context);
        objsMap = new HashMap<String, Object>();
    }

    private boolean init() {

        DirectoryManager dm = new DirectoryManager(new Music5DirectoryContext(getApplicationContext(), GC_ROOT_FOLDER));
        boolean ret = dm.buildAndClean();
        if (!ret) {
            return false;
        }

        registerSystemObject(DIR_MANAGER, dm);
        return ret;
    }

    public static DirectoryManager getDirectoryManager() {
        if (_instance == null) {
            return null;
        }
        return (DirectoryManager)_instance.getSystemObject(DIR_MANAGER);
    }

    public static File getDirectory(DirType type) {
        DirectoryManager manager = getDirectoryManager();
        if (manager == null) {
            return null;
        }
        return manager.getDir(type.name());
    }

    public static String getDirectoryPath(DirType type) {
        File file = getDirectory(type);
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }

    @Override
    public void registerSystemObject(String name, Object obj) {
        objsMap.put(name, obj);
    }

    @Override
    public Object getSystemObject(String name) {
        return objsMap.get(name);
    }

}
