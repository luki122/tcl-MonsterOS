package com.mst.thememanager.exception;

/**
 * Exception for check MVPView is Attached to Presenter or not
 *
 */
public class MvpViewNotAttachedException extends RuntimeException{
	private static final long serialVersionUID = -2735247193411329157L;

	public MvpViewNotAttachedException() {
        super("Please call Presenter.attachView(MvpView) before" +
                " requesting data to the Presenter");
    }
}
