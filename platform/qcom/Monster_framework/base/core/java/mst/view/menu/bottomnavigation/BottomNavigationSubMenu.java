package mst.view.menu.bottomnavigation;

import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuItemImpl;
import mst.view.menu.MstSubMenuBuilder;
import android.content.Context;

public class BottomNavigationSubMenu extends MstSubMenuBuilder {
	public BottomNavigationSubMenu(Context context, BottomNavigationMenu menu, MstMenuItemImpl item) {
        super(context, menu, item);
    }

    @Override
    public void onItemsChanged(boolean structureChanged) {
        super.onItemsChanged(structureChanged);
        ((MstMenuBuilder) getParentMenu()).onItemsChanged(structureChanged);
    }
}
