/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils.store;

import android.content.Context;
import android.text.TextUtils;

import com.leon.tools.IStringBean;

import java.lang.reflect.Constructor;

import cn.tcl.weather.utils.IManager;


/**
 * Created by thundersoft on 16-8-1.
 */
public class ClassStore implements IManager {

    private Context mContext;
    private IStore<String> mStore;

    public ClassStore(Context context) {
        mContext = context;
    }

    public void setStore(IStore<String> store) {
        mStore = store;
    }

    @Override
    public void init() {
        if (null == mStore)
            mStore = new SharedPreferenceStore(mContext, "clsStore.sp");
        mStore.init();
    }

    /**
     * store a object to store
     *
     * @param bean
     */
    public void store(IStringBean bean) {
        mStore.store(bean.getClass().getName(), bean.packString());
    }

    /**
     * read a object form store
     *
     * @param cls
     * @param <T>
     * @return
     */
    public <T extends IStringBean> T read(Class<T> cls) {
        T t = null;
        try {
            final String data = mStore.read(cls.getName());
            if (!TextUtils.isEmpty(data)) {
                Constructor<T> constructor = (Constructor<T>) cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                t = constructor.newInstance();
                constructor.setAccessible(false);
                t.parserString(data);
            }
        } catch (Exception e) {
            t = null;
        }
        return t;
    }


    public IStore<String> getRealStore() {
        return mStore;
    }

    /**
     * remove a object from store
     *
     * @param cls
     */
    public void remove(Class<?> cls) {
        mStore.remove(cls.getName());
    }

    @Override
    public void recycle() {
        mStore.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mStore.onTrimMemory(level);
    }
}
