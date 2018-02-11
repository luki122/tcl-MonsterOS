/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

public interface ModuleInterface {

	public interface CommonModule {
		public void checkViewVisibility();
	}

	public interface BottomBarModule {
		public void onShutterButtonClick();

		public void onShutterButtonLongClick();

		public void onBottomButtonsClick();
	}

	public interface ModeOptionModule {
		public void onModeOptionsClick();
	}

	public interface PreviewScreenModule {

		public void onZoomTouch();

		public void onAutoFocusTouch();
	}
}
