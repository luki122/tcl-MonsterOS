package com.monster.launcher;

/**
 * Created by antino on 16-8-22.
 */
public class IChangeColors {
    /**
     * Created by antino on 16-8-19.
     */
    public static interface IItemColorChange {
        void changeColors(int[] colors);
    }

    /**
     * Created by antino on 16-8-22.
     */
    public static interface IItemsColorChange {
        void changeItemColors(int[] colors);
    }
}
