/**
 * The workarounds library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;


/**
 * A class containing various utilities allowing to work around various bugs
 * in Java.
 */

public class FixUtils{



  /**
   * The value of CHAR_UNDEFINED in this JRE. This is needed because
   * its value changed between 1.1 and 1.2. See <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4106162.html">http://developer.java.sun.com/developer/bugParade/bugs/4106162.html</A> for more information.
   */

  public static final char CHAR_UNDEFINED;



  static{
    try{
      CHAR_UNDEFINED = KeyEvent.class.getField("CHAR_UNDEFINED").getChar(null);
    } catch (IllegalAccessException e){
        throw new InternalError();
      }
      catch (NoSuchFieldException e){
        throw new InternalError();
      }
  }
  
  
  
  /**
   * The fake clipboard selection we use when running under MS VM as an applet.
   */
   
  private static String clipboard = null;
  
  
  
  /**
   * Pastes via the fake clipboard.
   */
   
  static void fakePaste(JTextComponent tc){
    String text = tc.getText();
    int caretPos = tc.getCaretPosition();
    
    tc.setText(text.substring(0, caretPos) + clipboard + text.substring(caretPos));
    tc.setCaretPosition(caretPos + clipboard.length());
  }
  
  
  
  /**
   * Copies via the fake clipboard.
   */
   
  static void fakeCopy(JTextComponent tc){
    String selection = tc.getSelectedText();
    if ((selection != null) && (selection.length() > 0))
      clipboard = selection; 
  }
  
  
  
  /**
   * Cuts via the fake clipboard.
   */
   
  static void fakeCut(JTextComponent tc){
    String selection = tc.getSelectedText();
    if ((selection != null) && (selection.length() > 0)){
      clipboard = selection;
      
      String text = tc.getText();
      int selectionStart = tc.getSelectionStart();
      int selectionEnd = tc.getSelectionEnd();
      
      tc.setText(text.substring(0, selectionStart) + text.substring(selectionEnd));
      tc.setCaretPosition(selectionStart);
    }
  }
  


}


