/* 
 * Copyright 2010 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * addItemAfter method was added by David R. Damerell (david@nixbioinf.org)
 */
package ac.uk.icl.dell.vaadin.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.ui.VMenuBar;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * <p>
 * A class representing a horizontal menu bar. The menu can contain MenuItem
 * objects, which in turn can contain more MenuBars. These sub-level MenuBars
 * are represented as vertical menu.
 * </p>
 */
@SuppressWarnings("serial")

@ClientWidget(value=ac.uk.icl.dell.vaadin.glycanbuilder.widgetset.client.ui.VCustomMenuBar.class,loadStyle = LoadStyle.LAZY)
public class CustomMenuBar extends AbstractComponent{
	    // Items of the top-level menu
	    private List<MenuItem> menuItems;

	    // Number of items in this menu
	    private int numberOfItems = 0;

	    /**
	     * @deprecated
	     * @see #setCollapse(boolean)
	     */
	    @Deprecated
	    private boolean collapseItems;

	    /**
	     * @deprecated
	     * @see #setSubmenuIcon(Resource)
	     */
	    @Deprecated
	    private Resource submenuIcon;

	    private MenuItem moreItem;

	    private boolean openRootOnHover;

	    /** Paint (serialise) the component for the client. */
	    @Override
	    public void paintContent(PaintTarget target) throws PaintException {

	        // Superclass writes any common attributes in the paint target.
	        super.paintContent(target);

	        target.addAttribute(VMenuBar.OPEN_ROOT_MENU_ON_HOWER, openRootOnHover);

	        target.startTag("options");

	        if (submenuIcon != null) {
	            target.addAttribute("submenuIcon", submenuIcon);
	        }

	        if (getWidth() > -1) {
	            target.startTag("moreItem");
	            target.addAttribute("text", moreItem.getText());
	            if (moreItem.getIcon() != null) {
	                target.addAttribute("icon", moreItem.getIcon());
	            }
	            target.endTag("moreItem");
	        }

	        target.endTag("options");
	        target.startTag("items");

	        // This generates the tree from the contents of the menu
	        for (MenuItem item : menuItems) {
	            paintItem(target, item);
	        }

	        target.endTag("items");
	    }

	    private void paintItem(PaintTarget target, MenuItem item)
	            throws PaintException {
	        if (!item.isVisible()) {
	            return;
	        }

	        target.startTag("item");

	        target.addAttribute("id", item.getId());

	        if (item.getStyleName() != null) {
	            target.addAttribute("style", item.getStyleName());
	        }

	        if (item.isSeparator()) {
	            target.addAttribute("separator", true);
	        } else {
	            target.addAttribute("text", item.getText());

	            Command command = item.getCommand();
	            if (command != null) {
	                target.addAttribute("command", true);
	            }

	            Resource icon = item.getIcon();
	            if (icon != null) {
	                target.addAttribute("icon", icon);
	            }

	            if (!item.isEnabled()) {
	                target.addAttribute("disabled", true);
	            }

	            String description = item.getDescription();
	            if (description != null && description.length() > 0) {
	                target.addAttribute("description", description);
	            }
	            if (item.isCheckable()) {
	                // if the "checked" attribute is present (either true or false),
	                // the item is checkable
	                target.addAttribute(VMenuBar.ATTRIBUTE_CHECKED,
	                        item.isChecked());
	            }
	            if (item.hasChildren()) {
	                for (MenuItem child : item.getChildren()) {
	                    paintItem(target, child);
	                }
	            }

	        }

	        target.endTag("item");
	    }

	    /** Deserialize changes received from client. */
	    @Override
	    public void changeVariables(Object source, Map<String, Object> variables) {
	        Stack<MenuItem> items = new Stack<MenuItem>();
	        boolean found = false;

	        if (variables.containsKey("clickedId")) {

	            Integer clickedId = (Integer) variables.get("clickedId");
	            Iterator<MenuItem> itr = getItems().iterator();
	            while (itr.hasNext()) {
	                items.push(itr.next());
	            }

	            MenuItem tmpItem = null;

	            // Go through all the items in the menu
	            while (!found && !items.empty()) {
	                tmpItem = items.pop();
	                found = (clickedId.intValue() == tmpItem.getId());

	                if (tmpItem.hasChildren()) {
	                    itr = tmpItem.getChildren().iterator();
	                    while (itr.hasNext()) {
	                        items.push(itr.next());
	                    }
	                }

	            }// while

	            // If we got the clicked item, launch the command.
	            if (found && tmpItem.isEnabled()) {
	                if (tmpItem.isCheckable()) {
	                    tmpItem.setChecked(!tmpItem.isChecked());
	                }
	                if (null != tmpItem.getCommand()) {
	                    tmpItem.getCommand().menuSelected(tmpItem);
	                }
	            }
	        }// if
	    }// changeVariables

	    /**
	     * Constructs an empty, horizontal menu
	     */
	    public CustomMenuBar() {
	        menuItems = new ArrayList<MenuItem>();
	        setCollapse(true);
	        setMoreMenuItem(null);
	    }

	    /**
	     * Add a new item to the menu bar. Command can be null, but a caption must
	     * be given.
	     * 
	     * @param caption
	     *            the text for the menu item
	     * @param command
	     *            the command for the menu item
	     * @throws IllegalArgumentException
	     */
	    public CustomMenuBar.MenuItem addItem(String caption, CustomMenuBar.Command command) {
	        return addItem(caption, null, command);
	    }

	    /**
	     * Add a new item to the menu bar. Icon and command can be null, but a
	     * caption must be given.
	     * 
	     * @param caption
	     *            the text for the menu item
	     * @param icon
	     *            the icon for the menu item
	     * @param command
	     *            the command for the menu item
	     * @throws IllegalArgumentException
	     */
	    public CustomMenuBar.MenuItem addItem(String caption, Resource icon,
	    		CustomMenuBar.Command command) {
	        if (caption == null) {
	            throw new IllegalArgumentException("caption cannot be null");
	        }
	        MenuItem newItem = new MenuItem(caption, icon, command);
	        menuItems.add(newItem);
	        requestRepaint();

	        return newItem;

	    }

	    /**
	     * Add an item before some item. If the given item does not exist the item
	     * is added at the end of the menu. Icon and command can be null, but a
	     * caption must be given.
	     * 
	     * @param caption
	     *            the text for the menu item
	     * @param icon
	     *            the icon for the menu item
	     * @param command
	     *            the command for the menu item
	     * @param itemToAddBefore
	     *            the item that will be after the new item
	     * @throws IllegalArgumentException
	     */
	    public CustomMenuBar.MenuItem addItemBefore(String caption, Resource icon,
	    		CustomMenuBar.Command command, CustomMenuBar.MenuItem itemToAddBefore) {
	        if (caption == null) {
	            throw new IllegalArgumentException("caption cannot be null");
	        }

	        MenuItem newItem = new CustomMenuBar.MenuItem(caption, icon, command);
	        if (menuItems.contains(itemToAddBefore)) {
	            int index = menuItems.indexOf(itemToAddBefore);
	            menuItems.add(index, newItem);

	        } else {
	            menuItems.add(newItem);
	        }

	        requestRepaint();

	        return newItem;
	    }
	    
	    /**
	     * Add an item before some item. If the given item does not exist the item
	     * is added at the end of the menu. Icon and command can be null, but a
	     * caption must be given.
	     * 
	     * @param caption
	     *            the text for the menu item
	     * @param icon
	     *            the icon for the menu item
	     * @param command
	     *            the command for the menu item
	     * @param itemToAddBefore
	     *            the item that will be after the new item
	     * @throws IllegalArgumentException
	     */
	    public CustomMenuBar.MenuItem addItemAfter(String caption, Resource icon,
	    		CustomMenuBar.Command command, CustomMenuBar.MenuItem itemToAddAfter) {
	        if (caption == null) {
	            throw new IllegalArgumentException("caption cannot be null");
	        }

	        MenuItem newItem = new CustomMenuBar.MenuItem(caption, icon, command);
	        if (menuItems.contains(itemToAddAfter)) {
	            int index = menuItems.indexOf(itemToAddAfter)+1;
	            
	            if(index<menuItems.size()){
	            	menuItems.add(index, newItem);
	            }else{
	            	menuItems.add(newItem);
	            }

	        } else {
	            menuItems.add(newItem);
	        }

	        requestRepaint();

	        return newItem;
	    }

	    /**
	     * Returns a list with all the MenuItem objects in the menu bar
	     * 
	     * @return a list containing the MenuItem objects in the menu bar
	     */
	    public List<MenuItem> getItems() {
	        return menuItems;
	    }

	    /**
	     * Remove first occurrence the specified item from the main menu
	     * 
	     * @param item
	     *            The item to be removed
	     */
	    public void removeItem(CustomMenuBar.MenuItem item) {
	        if (item != null) {
	            menuItems.remove(item);
	        }
	        requestRepaint();
	    }

	    /**
	     * Empty the menu bar
	     */
	    public void removeItems() {
	        menuItems.clear();
	        requestRepaint();
	    }

	    /**
	     * Returns the size of the menu.
	     * 
	     * @return The size of the menu
	     */
	    public int getSize() {
	        return menuItems.size();
	    }

	    /**
	     * Set the icon to be used if a sub-menu has children. Defaults to null;
	     * 
	     * @param icon
	     * @deprecated (since 6.2, will be removed in 7.0) Icon is set in theme, no
	     *             need to worry about the visual representation here.
	     */
	    @Deprecated
	    public void setSubmenuIcon(Resource icon) {
	        submenuIcon = icon;
	        requestRepaint();
	    }

	    /**
	     * @deprecated
	     * @see #setSubmenuIcon(Resource)
	     */
	    @Deprecated
	    public Resource getSubmenuIcon() {
	        return submenuIcon;
	    }

	    /**
	     * Enable or disable collapsing top-level items. Top-level items will
	     * collapse together if there is not enough room for them. Items that don't
	     * fit will be placed under the "More" menu item.
	     * 
	     * Collapsing is enabled by default.
	     * 
	     * @param collapse
	     * @deprecated (since 6.2, will be removed in 7.0) Collapsing is always
	     *             enabled if the MenuBar has a specified width.
	     */
	    @Deprecated
	    public void setCollapse(boolean collapse) {
	        collapseItems = collapse;
	        requestRepaint();
	    }

	    /**
	     * @see #setCollapse(boolean)
	     * @deprecated
	     */
	    @Deprecated
	    public boolean getCollapse() {
	        return collapseItems;
	    }

	    /**
	     * Set the item that is used when collapsing the top level menu. All
	     * "overflowing" items will be added below this. The item command will be
	     * ignored. If set to null, the default item with a downwards arrow is used.
	     * 
	     * The item command (if specified) is ignored.
	     * 
	     * @param item
	     */
	    public void setMoreMenuItem(MenuItem item) {
	        if (item != null) {
	            moreItem = item;
	        } else {
	            moreItem = new CustomMenuBar.MenuItem("", null, null);
	        }
	        requestRepaint();
	    }

	    /**
	     * Get the MenuItem used as the collapse menu item.
	     * 
	     * @return
	     */
	    public MenuItem getMoreMenuItem() {
	        return moreItem;
	    }

	    /**
	     * Using this method menubar can be put into a special mode where top level
	     * menus opens without clicking on the menu, but automatically when mouse
	     * cursor is moved over the menu. In this mode the menu also closes itself
	     * if the mouse is moved out of the opened menu.
	     * <p>
	     * Note, that on touch devices the menu still opens on a click event.
	     * 
	     * @param autoOpenTopLevelMenu
	     *            true if menus should be opened without click, the default is
	     *            false
	     */
	    public void setAutoOpen(boolean autoOpenTopLevelMenu) {
	        if (autoOpenTopLevelMenu != openRootOnHover) {
	            openRootOnHover = autoOpenTopLevelMenu;
	            requestRepaint();
	        }
	    }

	    /**
	     * Detects whether the menubar is in a mode where top level menus are
	     * automatically opened when the mouse cursor is moved over the menu.
	     * Normally root menu opens only by clicking on the menu. Submenus always
	     * open automatically.
	     * 
	     * @return true if the root menus open without click, the default is false
	     */
	    public boolean isAutoOpen() {
	        return openRootOnHover;
	    }

	    /**
	     * This interface contains the layer for menu commands of the
	     * {@link com.vaadin.ui.MenuBar} class. It's method will fire when the user
	     * clicks on the containing {@link com.vaadin.ui.MenuBar.MenuItem}. The
	     * selected item is given as an argument.
	     */
	    public interface Command extends Serializable {
	        public void menuSelected(CustomMenuBar.MenuItem selectedItem);
	    }

	    /**
	     * A composite class for menu items and sub-menus. You can set commands to
	     * be fired on user click by implementing the
	     * {@link com.vaadin.ui.MenuBar.Command} interface. You can also add
	     * multiple MenuItems to a MenuItem and create a sub-menu.
	     * 
	     */
	    public class MenuItem implements Serializable {

	        /** Private members * */
	        private final int itsId;
	        private Command itsCommand;
	        private String itsText;
	        private List<MenuItem> itsChildren;
	        private Resource itsIcon;
	        private MenuItem itsParent;
	        private boolean enabled = true;
	        private boolean visible = true;
	        private boolean isSeparator = false;
	        private String styleName;
	        private String description;
	        private boolean checkable = false;
	        private boolean checked = false;

	        /**
	         * Constructs a new menu item that can optionally have an icon and a
	         * command associated with it. Icon and command can be null, but a
	         * caption must be given.
	         * 
	         * @param text
	         *            The text associated with the command
	         * @param command
	         *            The command to be fired
	         * @throws IllegalArgumentException
	         */
	        public MenuItem(String caption, Resource icon, CustomMenuBar.Command command) {
	            if (caption == null) {
	                throw new IllegalArgumentException("caption cannot be null");
	            }
	            itsId = ++numberOfItems;
	            itsText = caption;
	            itsIcon = icon;
	            itsCommand = command;
	        }

	        /**
	         * Checks if the item has children (if it is a sub-menu).
	         * 
	         * @return True if this item has children
	         */
	        public boolean hasChildren() {
	            return !isSeparator() && itsChildren != null;
	        }

	        /**
	         * Adds a separator to this menu. A separator is a way to visually group
	         * items in a menu, to make it easier for users to find what they are
	         * looking for in the menu.
	         * 
	         * @author Jouni Koivuviita / IT Mill Ltd.
	         * @since 6.2.0
	         */
	        public CustomMenuBar.MenuItem addSeparator() {
	            MenuItem item = addItem("", null, null);
	            item.setSeparator(true);
	            return item;
	        }

	        public CustomMenuBar.MenuItem addSeparatorBefore(MenuItem itemToAddBefore) {
	            MenuItem item = addItemBefore("", null, null, itemToAddBefore);
	            item.setSeparator(true);
	            return item;
	        }

	        /**
	         * Add a new item inside this item, thus creating a sub-menu. Command
	         * can be null, but a caption must be given.
	         * 
	         * @param caption
	         *            the text for the menu item
	         * @param command
	         *            the command for the menu item
	         */
	        public CustomMenuBar.MenuItem addItem(String caption, CustomMenuBar.Command command) {
	            return addItem(caption, null, command);
	        }

	        /**
	         * Add a new item inside this item, thus creating a sub-menu. Icon and
	         * command can be null, but a caption must be given.
	         * 
	         * @param caption
	         *            the text for the menu item
	         * @param icon
	         *            the icon for the menu item
	         * @param command
	         *            the command for the menu item
	         * @throws IllegalStateException
	         *             If the item is checkable and thus cannot have children.
	         */
	        public CustomMenuBar.MenuItem addItem(String caption, Resource icon,
	        		CustomMenuBar.Command command) throws IllegalStateException {
	            if (isSeparator()) {
	                throw new UnsupportedOperationException(
	                        "Cannot add items to a separator");
	            }
	            if (isCheckable()) {
	                throw new IllegalStateException(
	                        "A checkable item cannot have children");
	            }
	            if (caption == null) {
	                throw new IllegalArgumentException("Caption cannot be null");
	            }

	            if (itsChildren == null) {
	                itsChildren = new ArrayList<MenuItem>();
	            }

	            MenuItem newItem = new MenuItem(caption, icon, command);

	            // The only place where the parent is set
	            newItem.setParent(this);
	            itsChildren.add(newItem);

	            requestRepaint();

	            return newItem;
	        }

	        /**
	         * Add an item before some item. If the given item does not exist the
	         * item is added at the end of the menu. Icon and command can be null,
	         * but a caption must be given.
	         * 
	         * @param caption
	         *            the text for the menu item
	         * @param icon
	         *            the icon for the menu item
	         * @param command
	         *            the command for the menu item
	         * @param itemToAddBefore
	         *            the item that will be after the new item
	         * @throws IllegalStateException
	         *             If the item is checkable and thus cannot have children.
	         */
	        public CustomMenuBar.MenuItem addItemBefore(String caption, Resource icon,
	        		CustomMenuBar.Command command, CustomMenuBar.MenuItem itemToAddBefore)
	                throws IllegalStateException {
	            if (isCheckable()) {
	                throw new IllegalStateException(
	                        "A checkable item cannot have children");
	            }
	            MenuItem newItem = null;

	            if (hasChildren() && itsChildren.contains(itemToAddBefore)) {
	                int index = itsChildren.indexOf(itemToAddBefore);
	                newItem = new MenuItem(caption, icon, command);
	                newItem.setParent(this);
	                itsChildren.add(index, newItem);
	            } else {
	                newItem = addItem(caption, icon, command);
	            }

	            requestRepaint();

	            return newItem;
	        }

	        /**
	         * For the associated command.
	         * 
	         * @return The associated command, or null if there is none
	         */
	        public Command getCommand() {
	            return itsCommand;
	        }

	        /**
	         * Gets the objects icon.
	         * 
	         * @return The icon of the item, null if the item doesn't have an icon
	         */
	        public Resource getIcon() {
	            return itsIcon;
	        }

	        /**
	         * For the containing item. This will return null if the item is in the
	         * top-level menu bar.
	         * 
	         * @return The containing {@link com.vaadin.ui.MenuBar.MenuItem} , or
	         *         null if there is none
	         */
	        public CustomMenuBar.MenuItem getParent() {
	            return itsParent;
	        }

	        /**
	         * This will return the children of this item or null if there are none.
	         * 
	         * @return List of children items, or null if there are none
	         */
	        public List<MenuItem> getChildren() {
	            return itsChildren;
	        }

	        /**
	         * Gets the objects text
	         * 
	         * @return The text
	         */
	        public java.lang.String getText() {
	            return itsText;
	        }

	        /**
	         * Returns the number of children.
	         * 
	         * @return The number of child items
	         */
	        public int getSize() {
	            if (itsChildren != null) {
	                return itsChildren.size();
	            }
	            return -1;
	        }

	        /**
	         * Get the unique identifier for this item.
	         * 
	         * @return The id of this item
	         */
	        public int getId() {
	            return itsId;
	        }

	        /**
	         * Set the command for this item. Set null to remove.
	         * 
	         * @param command
	         *            The MenuCommand of this item
	         */
	        public void setCommand(CustomMenuBar.Command command) {
	            itsCommand = command;
	        }

	        /**
	         * Sets the icon. Set null to remove.
	         * 
	         * @param icon
	         *            The icon for this item
	         */
	        public void setIcon(Resource icon) {
	            itsIcon = icon;
	            requestRepaint();
	        }

	        /**
	         * Set the text of this object.
	         * 
	         * @param text
	         *            Text for this object
	         */
	        public void setText(java.lang.String text) {
	            if (text != null) {
	                itsText = text;
	            }
	            requestRepaint();
	        }

	        /**
	         * Remove the first occurrence of the item.
	         * 
	         * @param item
	         *            The item to be removed
	         */
	        public void removeChild(CustomMenuBar.MenuItem item) {
	            if (item != null && itsChildren != null) {
	                itsChildren.remove(item);
	                if (itsChildren.isEmpty()) {
	                    itsChildren = null;
	                }
	                requestRepaint();
	            }
	        }

	        /**
	         * Empty the list of children items.
	         */
	        public void removeChildren() {
	            if (itsChildren != null) {
	                itsChildren.clear();
	                itsChildren = null;
	                requestRepaint();
	            }
	        }

	        /**
	         * Set the parent of this item. This is called by the addItem method.
	         * 
	         * @param parent
	         *            The parent item
	         */
	        protected void setParent(CustomMenuBar.MenuItem parent) {
	            itsParent = parent;
	        }

	        public void setEnabled(boolean enabled) {
	            this.enabled = enabled;
	            requestRepaint();
	        }

	        public boolean isEnabled() {
	            return enabled;
	        }

	        public void setVisible(boolean visible) {
	            this.visible = visible;
	            requestRepaint();
	        }

	        public boolean isVisible() {
	            return visible;
	        }

	        private void setSeparator(boolean isSeparator) {
	            this.isSeparator = isSeparator;
	            requestRepaint();
	        }

	        public boolean isSeparator() {
	            return isSeparator;
	        }

	        public void setStyleName(String styleName) {
	            this.styleName = styleName;
	            requestRepaint();
	        }

	        public String getStyleName() {
	            return styleName;
	        }

	        /**
	         * Sets the items's description. See {@link #getDescription()} for more
	         * information on what the description is. This method will trigger a
	         * {@link com.vaadin.terminal.Paintable.RepaintRequestEvent
	         * RepaintRequestEvent}.
	         * 
	         * @param description
	         *            the new description string for the component.
	         */
	        public void setDescription(String description) {
	            this.description = description;
	            requestRepaint();
	        }

	        /**
	         * <p>
	         * Gets the items's description. The description can be used to briefly
	         * describe the state of the item to the user. The description string
	         * may contain certain XML tags:
	         * </p>
	         * 
	         * <p>
	         * <table border=1>
	         * <tr>
	         * <td width=120><b>Tag</b></td>
	         * <td width=120><b>Description</b></td>
	         * <td width=120><b>Example</b></td>
	         * </tr>
	         * <tr>
	         * <td>&lt;b></td>
	         * <td>bold</td>
	         * <td><b>bold text</b></td>
	         * </tr>
	         * <tr>
	         * <td>&lt;i></td>
	         * <td>italic</td>
	         * <td><i>italic text</i></td>
	         * </tr>
	         * <tr>
	         * <td>&lt;u></td>
	         * <td>underlined</td>
	         * <td><u>underlined text</u></td>
	         * </tr>
	         * <tr>
	         * <td>&lt;br></td>
	         * <td>linebreak</td>
	         * <td>N/A</td>
	         * </tr>
	         * <tr>
	         * <td>&lt;ul><br>
	         * &lt;li>item1<br>
	         * &lt;li>item1<br>
	         * &lt;/ul></td>
	         * <td>item list</td>
	         * <td>
	         * <ul>
	         * <li>item1
	         * <li>item2
	         * </ul>
	         * </td>
	         * </tr>
	         * </table>
	         * </p>
	         * 
	         * <p>
	         * These tags may be nested.
	         * </p>
	         * 
	         * @return item's description <code>String</code>
	         */
	        public String getDescription() {
	            return description;
	        }

	        /**
	         * Gets the checkable state of the item - whether the item has checked
	         * and unchecked states. If an item is checkable its checked state (as
	         * returned by {@link #isChecked()}) is indicated in the UI.
	         * 
	         * <p>
	         * An item is not checkable by default.
	         * </p>
	         * 
	         * @return true if the item is checkable, false otherwise
	         * @since 6.6.2
	         */
	        public boolean isCheckable() {
	            return checkable;
	        }

	        /**
	         * Sets the checkable state of the item. If an item is checkable its
	         * checked state (as returned by {@link #isChecked()}) is indicated in
	         * the UI.
	         * 
	         * <p>
	         * An item is not checkable by default.
	         * </p>
	         * 
	         * <p>
	         * Items with sub items cannot be checkable.
	         * </p>
	         * 
	         * @param checkable
	         *            true if the item should be checkable, false otherwise
	         * @throws IllegalStateException
	         *             If the item has children
	         * @since 6.6.2
	         */
	        public void setCheckable(boolean checkable)
	                throws IllegalStateException {
	            if (hasChildren()) {
	                throw new IllegalStateException(
	                        "A menu item with children cannot be checkable");
	            }
	            this.checkable = checkable;
	            requestRepaint();
	        }

	        /**
	         * Gets the checked state of the item (checked or unchecked). Only used
	         * if the item is checkable (as indicated by {@link #isCheckable()}).
	         * The checked state is indicated in the UI with the item, if the item
	         * is checkable.
	         * 
	         * <p>
	         * An item is not checked by default.
	         * </p>
	         * 
	         * <p>
	         * The CSS style corresponding to the checked state is "-checked".
	         * </p>
	         * 
	         * @return true if the item is checked, false otherwise
	         * @since 6.6.2
	         */
	        public boolean isChecked() {
	            return checked;
	        }

	        /**
	         * Sets the checked state of the item. Only used if the item is
	         * checkable (indicated by {@link #isCheckable()}). The checked state is
	         * indicated in the UI with the item, if the item is checkable.
	         * 
	         * <p>
	         * An item is not checked by default.
	         * </p>
	         * 
	         * <p>
	         * The CSS style corresponding to the checked state is "-checked".
	         * </p>
	         * 
	         * @return true if the item is checked, false otherwise
	         * @since 6.6.2
	         */
	        public void setChecked(boolean checked) {
	            this.checked = checked;
	            requestRepaint();
	        }

	    }// class MenuItem

}
