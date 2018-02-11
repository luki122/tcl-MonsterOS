package mst.view.menu.bottomnavigation;

import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuItemImpl;
import mst.view.menu.MstSubMenuBuilder;
import android.content.Context;
import android.view.SubMenu;

public class BottomNavigationMenu extends MstMenuBuilder {

	   public BottomNavigationMenu(Context context) {
	        super(context);
	    }

	    @Override
	    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
	        final MstMenuItemImpl item = (MstMenuItemImpl) addInternal(group, id, categoryOrder, title);
	        final MstSubMenuBuilder subMenu = new BottomNavigationSubMenu(getContext(), this, item);
	        item.setSubMenu(subMenu);
	        return subMenu;
	    }
	
}
