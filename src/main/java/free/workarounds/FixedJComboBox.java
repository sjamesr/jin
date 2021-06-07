/**
 * The workarounds library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The workarounds library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * <p>The workarounds library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the
 * workarounds library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.workarounds;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * A fix of JComboBox. Fixes the following bugs:
 *
 * <UL>
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4262163.html"> Ibeam cursor
 *       not appearing on JTextComponents in editmode</A>.
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4269430.html"> JComboBox -
 *       Should match preferred height of JTextField</A>.
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4513773.html"> requestFocus
 *       on an editable JComboBox sends focus to the combobox button</A>.
 * </UL>
 */
public class FixedJComboBox extends JComboBox {

  public FixedJComboBox() {
    super();
  }

  public FixedJComboBox(ComboBoxModel model) {
    super(model);
  }

  public FixedJComboBox(Object[] items) {
    super(items);
  }

  public FixedJComboBox(Vector items) {
    super(items);
  }

  @Override
  public void addNotify() {
    super.addNotify();

    // http://developer.java.sun.com/developer/bugParade/bugs/4269430.html //

    Dimension maximumSize = getPreferredSize();
    maximumSize.width = Short.MAX_VALUE;
    setMaximumSize(maximumSize);

    // http://developer.java.sun.com/developer/bugParade/bugs/4269430.html //
  }

  @Override
  public void setEditable(boolean editable) {
    super.setEditable(editable);

    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html //

    Component editorComponent = getEditor().getEditorComponent();
    Cursor cursor = editorComponent.getCursor();
    if (isEditable() && ((cursor == null) || (cursor.getType() != Cursor.TEXT_CURSOR)))
      editorComponent.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    else if (!isEditable() && ((cursor == null) || (cursor.getType() != Cursor.TEXT_CURSOR)))
      editorComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html //

  }

  @Override
  public void requestFocus() {

    // http://developer.java.sun.com/developer/bugParade/bugs/4513773.html

    if (isEditable()) getEditor().getEditorComponent().requestFocus();
    else super.requestFocus();

    // http://developer.java.sun.com/developer/bugParade/bugs/4513773.html

  }
}
