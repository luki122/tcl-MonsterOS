/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils.store;

import cn.tcl.weather.utils.IManager;

/**
 * Created by thundersoft on 16-8-1.
 * it is a base interface for stores
 */
public interface IStore<T> extends IManager {

    /**
     * store item to storage
     *
     * @param key
     * @param item
     */
    void store(String key, T item);

    /**
     * read item from storage
     *
     * @param key
     * @return
     */
    T read(String key);

    /**
     * remove item from storage
     *
     * @param key
     */
    void remove(String key);

    /**
     * clear all item in storage
     */
    void clearAll();

}
