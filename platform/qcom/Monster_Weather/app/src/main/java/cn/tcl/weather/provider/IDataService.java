/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import cn.tcl.weather.utils.IManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 */
public interface IDataService extends IManager {
    /**
     * request data
     *
     * @param params
     * @return true add to request list succeed，false add to request lsit failed
     */
    boolean requestData(DataParam params);

}
