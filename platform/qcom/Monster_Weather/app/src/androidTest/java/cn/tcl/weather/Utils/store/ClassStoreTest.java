/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.Utils.store;

import android.test.ActivityInstrumentationTestCase2;

import cn.tcl.weather.MainActivity;
import cn.tcl.weather.utils.store.ClassStore;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created  on 16-8-1.
 */
public class ClassStoreTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private ClassStore mClsStore;

    public ClassStoreTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mClsStore = new ClassStore(getActivity());
        mClsStore.init();
    }

    public void testStoreItem() {
//        NormaItem item = new NormaItem();
//        mClsStore.store(item);
//        NormaItem readItem = mClsStore.read(NormaItem.class);
//        Assert.assertTrue("item is the same", readItem.name.equals(item.name) && readItem.i == item.i && readItem.key == readItem.key);
    }

    @Override
    protected void tearDown() throws Exception {
        mClsStore.recycle();
        super.tearDown();
    }


    public static class NormaItem implements Cloneable {
        public String name = "normalItem";
        public int i = 0;
        public long key = 01234556l;
    }

}
