/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2006 Alexander Maryanovsky. All rights reserved.
 *
 * The utillib library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.swing;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.util.Utilities;

/**
 * A <code>ComboBoxModel</code> which wraps the specified<code>ListModel</code> for its actual list,
 * and manages the selected object in the most straightforward way.
 */
public class ListWrapperComboBoxModel extends AbstractListModel implements ComboBoxModel {

  /**
   * The list model.
   */
  private ListModel listModel;

  /**
   * The selected object.
   */
  private Object selected;

  /**
   * Creates a new <code>ListWrapperComboBoxModel</code> which wraps the specified
   * <code>ListModel</code>.
   */
  public ListWrapperComboBoxModel(ListModel listModel) {
    this.listModel = listModel;

    listModel.addListDataListener(
        new ListDataListener() {
          @Override
          public void contentsChanged(ListDataEvent e) {
            fireContentsChanged(this, e.getIndex0(), e.getIndex1());
          }

          @Override
          public void intervalAdded(ListDataEvent e) {
            fireIntervalAdded(this, e.getIndex0(), e.getIndex1());
          }

          @Override
          public void intervalRemoved(ListDataEvent e) {
            fireIntervalRemoved(this, e.getIndex0(), e.getIndex1());
          }
        });
  }

  /**
   * Returns the <code>index</code> element.
   */
  @Override
  public Object getElementAt(int index) {
    return listModel.getElementAt(index);
  }

  /**
   * Returns the size of the list.
   */
  @Override
  public int getSize() {
    return listModel.getSize();
  }

  /**
   * Returns the selected object.
   */
  @Override
  public Object getSelectedItem() {
    return selected;
  }

  /**
   * Sets the selected item.
   */
  @Override
  public void setSelectedItem(Object item) {
    if (!Utilities.areEqual(selected, item)) {
      selected = item;
      fireContentsChanged(this, -1, -1);
    }
  }
}
