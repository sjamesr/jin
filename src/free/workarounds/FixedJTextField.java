/**
 * The workarounds library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The workarounds library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The workarounds library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the workarounds library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.workarounds;

import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.UIManager;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;


/**
 * A fix of JTextField. Fixes the following bugs:
 * <UL>
 *   <LI> <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4262163.html">
 *        Ibeam cursor not appearing on TextField or TextArea in editmode</A>.
 *   <LI> <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4133908.html">
 *        [Enter] in JTextComponent should activate DefaultButton</A>.
 *   <LI> <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4145324.html">
 *        JTextField displays multiple Line</A>.
 *   <LI> <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4174290.html">
 *        Disabled JTextField background should be control colour in Windows L&F</A>.
 *   <LI> <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4137845.html">
 *        JTextField draws out of bounds</A>.
 *   <LI> copy()/paste()/cut() throws exceptions under MS VM when run as an
 *        applet.
 * </UL>
 */

public class FixedJTextField extends JTextField{
  

  public FixedJTextField(){
    super();
  }


  public FixedJTextField(Document doc, String text, int columns){
    super(doc, text, columns);
  }


  public FixedJTextField(String text){
    super(text);
  }


  public FixedJTextField(int columns){
    super(columns);
  }


  public FixedJTextField(String text, int columns){
    super(text, columns);
  }



  //
  // When more text is in the textfield than fits, the textfield will draw some
  // of the text under the border (which is not inside the clip rectangle),
  // which causes the first (or last) visible character to be painted only
  // partially.
  //
  // http://developer.java.sun.com/developer/bugParade/bugs/4137845.html
  // 

  public void scrollRectToVisible(Rectangle r) {
    Insets i = getInsets();
    javax.swing.BoundedRangeModel visibility = getHorizontalVisibility();
    int x = r.x + visibility.getValue() - i.left;
    if (x < visibility.getValue()) {
      // Scroll to the left
      visibility.setValue(x);
    } else if(x > visibility.getValue() + visibility.getExtent()) {
      // Scroll to the right
      visibility.setValue(x - visibility.getExtent());
    }
  }

  // http://developer.java.sun.com/developer/bugParade/bugs/4137845.html



  public void setEditable(boolean editable){
    super.setEditable(editable);

    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html 

    Cursor cursor = getCursor();
    if (isEditable() && ((cursor == null) || (cursor.getType() != Cursor.TEXT_CURSOR)))
      setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    else if (!isEditable() && ((cursor == null) || (cursor.getType() != Cursor.DEFAULT_CURSOR)))
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    
    // http://developer.java.sun.com/developer/bugParade/bugs/4262163.html 



    // http://developer.java.sun.com/developer/bugParade/bugs/4174290.html

    updateInactiveColor();

    // http://developer.java.sun.com/developer/bugParade/bugs/4174290.html
  }



  public void updateUI(){
    super.updateUI();

    // http://developer.java.sun.com/developer/bugParade/bugs/4174290.html

    updateInactiveColor();

    // http://developer.java.sun.com/developer/bugParade/bugs/4174290.html

  }




  protected void updateInactiveColor(){
    String lnfClassName = UIManager.getLookAndFeel().getClass().getName();
    if (lnfClassName.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")){
      Color inactiveBackground = UIManager.getColor("TextField.inactiveBackground");
      if (inactiveBackground == null){
        if (!isEditable())
          setBackground(UIManager.getColor("Panel.background"));
        else
          setBackground(UIManager.getColor("TextField.background"));
      }
    }
  }


  protected void processComponentKeyEvent(KeyEvent evt){
    
    // http://developer.java.sun.com/developer/bugParade/bugs/4133908.html

    if ((evt.getKeyCode()==KeyEvent.VK_ENTER)&&(listenerList.getListenerCount(ActionListener.class)==0))
      return;

      // Removes AWT compatibility regarding pressing ENTER generating an 
      // ActionEvent if there are no registered listeners.
    

    super.processComponentKeyEvent(evt);

    // http://developer.java.sun.com/developer/bugParade/bugs/4133908.html

  }



  public void paste(){

    // http://developer.java.sun.com/developer/bugParade/bugs/4145324.html

    try{
      super.paste();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakePaste(this);
        else
          throw e;
      }
      

    int caretPosition = getCaretPosition();
    
    // First replace all \r\n with a space
    String text = getText();
    int index;
    while ((index = text.indexOf("\r\n")) != -1){
      text = text.substring(0, index) + " " + text.substring(index + 2);
      if (index < caretPosition)
        caretPosition--;
    }
    
    // The replace all individual \r and \n with a space
    text = text.replace('\n', ' ').replace('\r', ' ');
    
    setText(text);
    
    // Restore caret position because setText puts it at the end
    setCaretPosition(caretPosition);

    // http://developer.java.sun.com/developer/bugParade/bugs/4145324.html

  }
  
  
  
  public void copy(){
    try{
      super.copy();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakeCopy(this);
        else
          throw e;
      }
  }


  
  public void cut(){
    try{
      super.cut();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakeCut(this);
        else
          throw e;
      }
  }
  
  

}
