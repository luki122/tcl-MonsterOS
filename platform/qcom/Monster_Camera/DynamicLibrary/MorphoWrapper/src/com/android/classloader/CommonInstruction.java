package com.android.classloader;

import java.util.concurrent.Callable;

/**
 *   Customized class MUST implement this interface to retrieve any expected callable using getFunctionPointer,
 */
public interface CommonInstruction {
    /**
     * Get the concrete function to be used by sending message
     * @param msg message to be matched to retrieve the expected callable.
     * @param parameters parameters to post to the expected concrete call function, MUST be CONSTANT
     * @return  A Callable whose call method implemented the concrete method expected , or null if msg is invalid
     */
    public  Callable<Object> getFunctionPointer(String msg,final Object... parameters);
}
