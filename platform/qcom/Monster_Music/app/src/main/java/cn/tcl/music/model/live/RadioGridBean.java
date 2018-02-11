package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.ArrayList;

public class RadioGridBean implements Serializable {
    public ArrayList<RadioBean> radioBeans = new ArrayList<>();

    @Override
    public String toString() {
        return "RadioGridBean{" +
                "radioBeans=" + radioBeans +
                '}';
    }
}
