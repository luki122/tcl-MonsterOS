package cn.tcl.music.network;

import com.google.gson.JsonElement;

/**
 * Created by dongdong.huang on 2015/11/3.
 */
public class XiamiApiResponse {
    public String status = "";
    public JsonElement data;
    public String message = "";
    public int state;
    public String request_id = "";

    public XiamiApiResponse() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
