/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
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
package free.util.swing.tabbedpane;

import java.awt.Component;

/**
 * The interface for implementations of tab components - the ones representing the "handle" of the
 * tab.
 */
public interface TabHandle {

  /**
   * Returns the actual component used for the tab.
   */
  Component getComponent();

  /**
   * Notifies the tab component of the selected state of the tab represented by it.
   */
  void setSelected(boolean isSelected);
}
