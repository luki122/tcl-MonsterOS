package mst.view.menu;

import android.content.Context;
import android.view.SubMenu;

public class MstNavigationMenu extends MstMenuBuilder {

    public MstNavigationMenu(Context context) {
        super(context);
    }

    @Override
    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        final MstMenuItemImpl item = (MstMenuItemImpl) addInternal(group, id, categoryOrder, title);
        final MstSubMenuBuilder subMenu = new MstNavigationSubMenu(getContext(), this, item);
        item.setSubMenu(subMenu);
        return subMenu;
    }
}
