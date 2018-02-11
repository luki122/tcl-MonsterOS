package cn.tcl.music.model.live;

import java.io.Serializable;

public class LiveLoginBean extends BaseSong implements Serializable {

    private String user_id;         //虾米用户id, BIGINT类型
    private String access_token;
    private int access_expires;
    private String refresh_token;
    private int refresh_expires;
    private boolean is_first;
    private boolean need_bind;
    private String login_url;
    private String open_id;
}
