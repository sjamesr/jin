/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import java.awt.Frame;
import java.awt.Component;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.Window;
import free.util.WindowDisposingListener;


/**
 * Provides various swing related utilities.
 */

public class SwingUtils{


  /**
   * The ESCAPE keystroke we use to close a dialog/window.
   */

  private static final KeyStroke CLOSE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);  



  /**
   * Adds a keyboard action to the specified <code>RootPaneContainer</code> so
   * that it is disposed when the ESCAPE key is hit. <B>Note that this only
   * works for subclasses of <code>Window</code>.</B>
   */

  public static void registerEscapeCloser(RootPaneContainer container){
    JRootPane rootPane = container.getRootPane();
    Window window = SwingUtilities.windowForComponent(rootPane);

    ActionListener closer = new WindowDisposingListener(window);
    rootPane.registerKeyboardAction(closer, CLOSE_KEYSTROKE, JComponent.WHEN_IN_FOCUSED_WINDOW);
  }



  /**
   * Returns the parent Frame of the specified <code>Component</code> or
   * <code>null</code> if none exists. This does the same as
   * <code>free.util.AWTUtilities.frameForComponent</code> but also takes care
   * of various swing quirks.
   */

  public static Frame frameForComponent(Component component){
    while (component != null){
      if (component instanceof Frame)
        return (Frame)component;

      if (component instanceof JPopupMenu) // The parent of a popup menu seems to be null
        component = ((JPopupMenu)component).getInvoker();
      else
        component = component.getParent();
    }

    return null;
  }



}
