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

import java.util.EventObject;

/** The event object fired by <code>TabbedPaneModel</code>s. */
public class TabbedPaneEvent extends EventObject {

  /** The id for an event specifying that a tab was added. */
  public static final int TAB_ADDED = 1;

  /** The id for an event specifying that a tab was removed. */
  public static final int TAB_REMOVED = 2;

  /** The id for an event specifying that a tab was selected. */
  public static final int TAB_SELECTED = 3;

  /** The id for an event specifying that a tab was deselected. */
  public static final int TAB_DESELECTED = 4;

  /** The id of this event. */
  private final int id;

  /** The index of the tab affected in this event. */
  private final int tabIndex;

  /**
   * Creates a new <code>TabbedPaneEvent</code> with the specified source <code>TabbedPaneModel
   * </code>, id and index of the selected tab.
   */
  public TabbedPaneEvent(TabbedPaneModel source, int id, int tabIndex) {
    super(source);

    switch (id) {
      case TAB_ADDED:
      case TAB_REMOVED:
      case TAB_SELECTED:
      case TAB_DESELECTED:
        break;
      default:
        throw new IllegalArgumentException("Unknown TabbedPaneEvent id: " + id);
    }

    if ((tabIndex < 0)
        || (tabIndex > source.getTabCount())
        || ((tabIndex == source.getTabCount()) && (id != TAB_REMOVED)))
      throw new IndexOutOfBoundsException(
          "tabIndex (" + tabIndex + ") out of bounds (tabCount=" + source.getTabCount() + ")");

    this.id = id;
    this.tabIndex = tabIndex;
  }

  /** Returns the <code>TabbedPaneModel</code> source of this event. */
  public TabbedPaneModel getTabbedPaneModel() {
    return (TabbedPaneModel) getSource();
  }

  /**
   * Returns the id of this event - one of:
   *
   * <ul>
   *   <li><code>TabbedPaneEvent.TAB_ADDED</code>
   *   <li><code>TabbedPaneEvent.TAB_REMOVED</code>
   *   <li><code>TabbedPaneEvent.TAB_SELECTED</code>
   *   <li><code>TabbedPaneEvent.TAB_DESELECTED</code>
   * </ul>
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the index of the tab affected in this event. In the case of a <code>TAB_REMOVED</code>
   * event, this is the index of the tab before it was removed.
   */
  public int getTabIndex() {
    return tabIndex;
  }
}
