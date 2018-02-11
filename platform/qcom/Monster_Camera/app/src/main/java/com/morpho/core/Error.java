package com.morpho.core;

public class Error {
	
    public static final int MORPHO_OK              = 0x00000000;
    public static final int MORPHO_DOPROCESS       = 0x00000001;
    public static final int ERROR_GENERAL_ERROR    = 0x80000000;
    public static final int ERROR_PARAM            = 0x80000001;
    public static final int ERROR_STATE            = 0x80000002;
    public static final int ERROR_MALLOC           = 0x80000004;
    public static final int ERROR_IO               = 0x80000008;
    public static final int ERROR_UNSUPPORTED      = 0x80000010;
    public static final int ERROR_UNKNOWN          = 0xC0000000;
	
	
}