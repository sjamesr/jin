/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/**
 * A WindowListener which calls <code>System.exit(0)</code> when a 
 * <code>WINDOW_CLOSING</code> event occurs on a window it is a listener of.
 * Add an instance of this class to be a WindowListener of the main frame of 
 * your application.
 */

public class AppKiller extends WindowAdapter{

  
  /**
   * Calls <code>System.exit(0)</code>.
   */

  public void windowClosing(WindowEvent evt){
    System.exit(0);
  }

}
