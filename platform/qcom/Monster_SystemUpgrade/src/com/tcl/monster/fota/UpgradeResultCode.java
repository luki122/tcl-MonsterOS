package com.tcl.monster.fota;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to define all the result code from recovery.See the document for detail,
 * "GOTU-Three-Tier-Structure-V1.7-Design" 4.1.1.3 section.
 * <p>
 * Created by zgzeng on 10/21/16.
 */

public final class UpgradeResultCode {

    public final static int CODE_SUCCESS = 999;

    private final static Map<Integer, String> internal = new HashMap();

    static {
        internal.put(999, "INSTALL SUCCESS");

        // the error codes below are belong to Android M,and the value will be report byt recovery.
        internal.put(2000, "Update.zip is not correct:ro.product.device");
        internal.put(2001, "Update.zip is not correct:ro.build.fingerprint");
        internal.put(2002, "Update.zip is not correct:apply_patch_check");
        internal.put(2003, "Signature verification failed");
        internal.put(2004, "Can not find update.zip");
        internal.put(2005, "Update.zip is not correct:package_extract_file");
        internal.put(2006, "Update.zip is not correct:run_program");
        internal.put(2007, "Update.zip is not correct:sha1_check");
        internal.put(2008, "Update.zip is not correct:apply_patch_space");
        internal.put(2009, "Update.zip is not correct:ro.build.date.utc");
        internal.put(2010, "Update.zip is not correct:Undefine");

        // the error codes below are belong to Android N,and the key will be report by recovery.
//        internal.put(3020, "LowBattery");
//        internal.put(3021, "ZipVerificationFailure");
//        internal.put(3022, "ZipOpenFailure");
//        internal.put(3100, "SYSTEM_VERIFICATION_FAILURE");
//        internal.put(3101, "SYSTEM_UPDATE_FAILURE");
//        internal.put(3102, "SYSTEM_UNEXPECTED_CONTENTS");
//        internal.put(3103, "SYSTEM_NONZERO_CONTENTS");
//        internal.put(3104, "SYSTEM_RECOVER_FAILURE");
//        internal.put(3200, "VENDOR_VERIFICATION_FAILURE");
//        internal.put(3201, "VENDOR_UPDATE_FAILURE");
//        internal.put(3202, "VENDOR_UNEXPECTED_CONTENTS");
//        internal.put(3203, "VENDOR_NONZERO_CONTENTS");
//        internal.put(3204, "VENDOR_RECOVER_FAILURE");
//        internal.put(3300, "OEM_PROP_MISMATCH");
//        internal.put(3301, "FINGERPRINT_MISMATCH");
//        internal.put(3302, "THUMBPRINT_MISMATCH");
//        internal.put(3303, "OLDER_BUILD");
//        internal.put(3304, "DEVICE_MISMATCH");
//        internal.put(3305, "BAD_PATCH_FILE");
//        internal.put(3306, "INSUFFICIENT_CACHE_SPACE");
//        internal.put(3307, "TUNE_PARTITION_FAILURE");
//        internal.put(3308, "APPLY_PATCH_FAILURE");
    }

    // we should always send the code to the server.
    public static int detectCode(String status) {
        if (status == null) {
            throw new NullPointerException("The status should not be null!");
        }

        try {
            return Integer.parseInt(status.trim());
        } catch (NumberFormatException e) {
            for (Map.Entry<Integer, String> entry : internal.entrySet()) {
                if (status.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }

        // TODO define a default error code.
        return 2001;
    }

}
