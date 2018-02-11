package mst.view.menu;

/**
 * Interface for managing the items in a menu.
 *
 * This version extends the one available in the framework to ensures that any necessary
 * elements added in later versions of the framework, are available for all platforms.
 *
 * @see android.view.Menu
 * @hide
 */
public interface SupportMenu extends android.view.Menu {

    /**
     * This is the part of an order integer that the user can provide.
     *
     * @hide
     */
    static final int USER_MASK = 0x0000ffff;
    /**
     * Bit shift of the user portion of the order integer.
     *
     * @hide
     */
    static final int USER_SHIFT = 0;

    /**
     * This is the part of an order integer that supplies the category of the item.
     *
     * @hide
     */
    static final int CATEGORY_MASK = 0xffff0000;
    /**
     * Bit shift of the category portion of the order integer.
     *
     * @hide
     */
    static final int CATEGORY_SHIFT = 16;
}
