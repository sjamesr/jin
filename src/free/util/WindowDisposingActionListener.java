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

package free.util;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * An ActionListener which disposes of the given Window when called.
 */

public class WindowDisposingActionListener implements ActionListener{



  /**
   * The target Window.
   */

  private final Window targetWindow;



  /**
   * Creates a new WindowDisposingActionListener with the given target Window.
   */

  public WindowDisposingActionListener(Window targetWindow){
    this.targetWindow = targetWindow;
  }


  
  /**
   * Calls <code>dispose()</code> on the target Window.
   */

  public void actionPerformed(ActionEvent evt){
    targetWindow.dispose();
  }

}
