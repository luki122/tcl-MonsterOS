package mst.view.menu;

import android.content.Context;

public class MstNavigationSubMenu extends MstSubMenuBuilder {
	public MstNavigationSubMenu(Context context, MstNavigationMenu menu, MstMenuItemImpl item) {
        super(context, menu, item);
    }

    @Override
    public void onItemsChanged(boolean structureChanged) {
        super.onItemsChanged(structureChanged);
        ((MstMenuBuilder) getParentMenu()).onItemsChanged(structureChanged);
    }
}
