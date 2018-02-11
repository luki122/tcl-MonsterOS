/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Listener of a finished event of sql insert.Must implement this interface
 * when you want to do something after a insert you call.
 */
public interface OnDoneInsertAndUpdateListener {
    /**
     * Call back method when a inserting or updating has been finished.
     *
     * @param id true:     successful inserting/updating
     *                false:    unsuccessful inserting/updating
     */
    void onDone(long id);
}
