/**
 * The workarounds library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2003 Alexander Maryanovsky. All rights reserved.
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

import java.awt.Cursor;
import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * A fix of JTextArea. Fixes the following bugs:
 *
 * <UL>
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4262163.html"> Ibeam cursor
 *       not appearing on TextField or TextArea in editmode</A>.
 *   <LI>copy()/paste()/cut() throws exceptions under MS VM when run as an applet.
 * </UL>
 */
public class FixedJTextArea extends JTextArea {

  public FixedJTextArea() {
    super();
  }

  public FixedJTextArea(Document doc) {
    super(doc);
  }

  public FixedJTextArea(Document doc, String text, int rows, int columns) {
    super(doc, text, rows, columns);
  }

  public FixedJTextArea(int rows, int columns) {
    super(rows, columns);
  }

  public FixedJTextArea(String text) {
    super(text);
  }

  public FixedJTextArea(String text, int rows, int columns) {
    super(text, rows, columns);
  }

  @Override
  public void setEditable(boolean editable) {
    super.setEditable(editable);

    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html

    Cursor cursor = getCursor();
    if (isEditable() && ((cursor == null) || (cursor.getType() != Cursor.TEXT_CURSOR)))
      setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    else if (!isEditable() && ((cursor == null) || (cursor.getType() != Cursor.DEFAULT_CURSOR)))
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html

  }

  @Override
  public void paste() {
    try {
      super.paste();
    } catch (RuntimeException e) { // MS VM throws a com.ms.security.SecurityExceptionEx
      if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
        FixUtils.fakePaste(this);
      else throw e;
    }
  }

  @Override
  public void copy() {
    try {
      super.copy();
    } catch (RuntimeException e) { // MS VM throws a com.ms.security.SecurityExceptionEx
      if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
        FixUtils.fakeCopy(this);
      else throw e;
    }
  }

  @Override
  public void cut() {
    try {
      super.cut();
    } catch (RuntimeException e) { // MS VM throws a com.ms.security.SecurityExceptionEx
      if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
        FixUtils.fakeCut(this);
      else throw e;
    }
  }
}
