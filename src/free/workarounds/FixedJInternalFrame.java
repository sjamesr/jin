/**
 * The workarounds library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import java.awt.Component;
import java.awt.Window;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.beans.PropertyVetoException;


/**
 * A fix of JInternalFrame. Fixes the following bugs:
 * <UL>
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4109910.html">
 *        When a JInternalFrame is activated, focus is not transferred to its children.</A>.
 * </UL>
 */

public class FixedJInternalFrame extends JInternalFrame{


  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * equivalent <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(){
    super();
  }



  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * corresponding <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(String title){
    super(title);
  }



  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * corresponding <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(String title, boolean resizable){
    super(title, resizable);
  }



  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * corresponding <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(String title, boolean resizable, boolean closable){
    super(title, resizable, closable);
  }



  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * corresponding <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(String title, boolean resizable, boolean closable,
      boolean maximizable){
    super(title, resizable, closable, maximizable);
  }



  /**
   * Creates a new <code>FixedJInternalFrame</code>, per the contract of the
   * corresponding <code>JInternalFrame</code> constructor.
   */

  public FixedJInternalFrame(String title, boolean resizable, boolean closable,
      boolean maximizable, boolean iconifiable){
    super(title, resizable, closable, maximizable, iconifiable);
  }



  /**
   * The component that had the focus when this internal frame became
   * deselected.
   */

  private Component previousFocusedComponent = null;



  public void setSelected(boolean selected) throws PropertyVetoException{
    super.setSelected(selected);

    // http://developer.java.sun.com/developer/bugParade/bugs/4109910.html

    if (selected){
      if (findFocusOwner() == null){
        if (previousFocusedComponent != null)
          previousFocusedComponent.requestFocus();
        else if (!requestDefaultFocus())
          requestFocus();
      }
    }
    else
      previousFocusedComponent = findFocusOwner();

    // http://developer.java.sun.com/developer/bugParade/bugs/4109910.html
  }




  /**
   * Returns the current focus owner of this JInternalFrame or null if it's not
   * active or isn't displayable.
   */

  private Component findFocusOwner(){
    if (isSelected()){
      Window window = SwingUtilities.windowForComponent(this);
      if (window == null)
        return null;
      Component component = window.getFocusOwner();
      return isAncestorOf(component) ? component : null;
        // The "null" case shouldn't happen, because we are selected, but just in case...
    }
    else
      return null;
  }

}
