package com.tcl.monster.fota.misc;

public enum State {
	/**
	 * idle state,ini state or after checked but no new version
	 */
	IDLE,

	CHECKING,

	/**
	 * state after checking,may be new version available ,may be no new
	 * version
	 */
	CHECKED,

	/**
	 * state after checked with new version available
	 */
	STARTING,

	DOWNLOADING,

	PAUSING,

	PAUSED,

	RESUMING,

	DOWNLOADED,

	VERIFYING,

	VERIFIED,

	INSTALL_DISABLE,

	INSTALLING,
	/**
	 * after download complete ,verify failed.
	 */
	ERROR,
	UNUSED
}
