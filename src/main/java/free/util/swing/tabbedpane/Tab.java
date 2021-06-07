/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.swing.tabbedpane;

import free.util.Utilities;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import javax.swing.event.SwingPropertyChangeSupport;

/** Encapsulates a single tab. */
public class Tab {

  /** The component displayed in the tab. */
  private final Component component;

  /** The title of the tab; may be <code>null</code>. */
  private String title;

  /** The icon of the tab; may be <code>null</code>. */
  private Icon icon;

  /** Whether the tab is closeable. */
  private boolean isCloseable;

  /** Our tab close approver. */
  private TabCloseApprover tabCloseApprover = null;

  /** Property change support. */
  private final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

  /**
   * Creates a new <code>Tab</code> with the specified arguments.
   *
   * @param component The component displayed in the tab.
   * @param title The title of the tab.
   * @param icon The icon of the tab.
   * @param isCloseable Whether the tab should have UI to allow the user to close it.
   */
  public Tab(Component component, String title, Icon icon, boolean isCloseable) {
    if (component == null) throw new IllegalArgumentException("component may not be null");

    this.component = component;
    this.title = title;
    this.icon = icon;
    this.isCloseable = isCloseable;
  }

  /** Adds a property change listener. */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /** Removes a property change listener. */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /** Returns the component displayed in the tab. */
  public Component getComponent() {
    return component;
  }

  /** Returns the tab's title; may be <code>null</code>. */
  public String getTitle() {
    return title;
  }

  /** Sets the title of the tab. */
  public void setTitle(String title) {
    if (Utilities.areEqual(this.title, title)) return;

    String oldValue = this.title;
    this.title = title;

    propertyChangeSupport.firePropertyChange("title", oldValue, title);
  }

  /** Returns the tab's icon; may be <code>null</code>. */
  public Icon getIcon() {
    return icon;
  }

  /** Sets the icon of this tab. */
  public void setIcon(Icon icon) {
    if (Utilities.areEqual(this.icon, icon)) return;

    Icon oldValue = this.icon;
    this.icon = icon;

    propertyChangeSupport.firePropertyChange("icon", oldValue, title);
  }

  /** Returns whether the tab should display UI to allow the user to close it. */
  public boolean isCloseable() {
    return isCloseable;
  }

  /** Sets the closeable state of this tab. */
  public void setCloseable(boolean isCloseable) {
    if (this.isCloseable == isCloseable) return;

    boolean oldValue = this.isCloseable;
    this.isCloseable = isCloseable;

    propertyChangeSupport.firePropertyChange("closeable", oldValue, isCloseable);
  }

  /** Returns the current approver of tab close actions; <code>null</code> if none. */
  public TabCloseApprover getTabCloseApprover() {
    return tabCloseApprover;
  }

  /**
   * Sets the approver of tab close actions. The approver is consulted when the user attempts to
   * close the tab. If the approver disapproves, the action is canceled and the tab isn't closed. A
   * <code>null</code> approver is not consulted and thus the close action always goes forward. The
   * default value is <code>null</code>. In addition to this approver, the tabbed pane's approver is
   * also consulted.
   */
  public void setTabCloseApprover(TabCloseApprover tabCloseApprover) {
    if (Utilities.areEqual(this.tabCloseApprover, tabCloseApprover)) return;

    TabCloseApprover oldValue = this.tabCloseApprover;
    this.tabCloseApprover = tabCloseApprover;

    propertyChangeSupport.firePropertyChange("tabCloseApprover", oldValue, tabCloseApprover);
  }
}
