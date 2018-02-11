/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.robotium;

import com.tct.camera.robotium.Solo;

/**
 * Represents a conditional statement.<br/>
 * Implementations may be used with {@link Solo#waitForCondition(Condition, int)}.
 */
public interface Condition {

	/**
	 * Should do the necessary work needed to check a condition and then return whether this condition is satisfied or not.
	 * @return {@code true} if condition is satisfied and {@code false} if it is not satisfied
	 */
	public boolean isSatisfied();

}
