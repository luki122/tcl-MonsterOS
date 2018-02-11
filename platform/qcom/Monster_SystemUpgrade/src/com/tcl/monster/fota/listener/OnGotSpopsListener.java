package com.tcl.monster.fota.listener;

import java.util.List;

import com.tcl.monster.fota.model.Spop;

/**
 * Interface for handling special op.
 * @author haijun.chen
 *
 */
public interface OnGotSpopsListener {
	void onGotSpops(List<Spop> spops);
}
