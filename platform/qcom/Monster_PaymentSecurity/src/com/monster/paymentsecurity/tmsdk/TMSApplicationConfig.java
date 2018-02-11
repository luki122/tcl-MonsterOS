package com.monster.paymentsecurity.tmsdk;

import java.util.HashMap;
import java.util.Map;

import tmsdk.common.ITMSApplicaionConfig;

/**
 * tmsdk 配置
 *
 * Created by logic on 16-12-9.
 */
public class TMSApplicationConfig implements ITMSApplicaionConfig {
    @Override
    public HashMap<String, String> config(
            Map<String, String> src) {
        return new HashMap<>(src);
    }
}
