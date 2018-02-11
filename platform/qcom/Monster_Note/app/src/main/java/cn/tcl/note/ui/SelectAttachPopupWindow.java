/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupWindow;

import cn.tcl.note.R;
import mst.view.menu.BottomWidePopupMenu;

/**
 * when tap add img or audio,the window will pop.
 */
public class SelectAttachPopupWindow extends PopupWindow {
    /**
     * pop a window
     *
     * @param context
     * @param listener  click item listener
     * @param firstStr  text showed in first line
     * @param secondStr text showed in first line
     */
    public SelectAttachPopupWindow(Context context, BottomWidePopupMenu.OnMenuItemClickListener listener, int firstStr, int secondStr) {
        super(context);
        BottomWidePopupMenu bottomWidePopupMenu = new BottomWidePopupMenu(context);
        bottomWidePopupMenu.inflateMenu(R.menu.menu_pop_bottom);

        Menu menu = bottomWidePopupMenu.getMenu();
        MenuItem firstItem = menu.findItem(R.id.first_line);
        MenuItem secondItem = menu.findItem(R.id.second_line);
        firstItem.setTitle(firstStr);
        secondItem.setTitle(secondStr);


        bottomWidePopupMenu.setOnMenuItemClickedListener(listener);
        bottomWidePopupMenu.setCanceledOnTouchOutside(true);
        bottomWidePopupMenu.show();

    }
}
