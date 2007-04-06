/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.console.fics;

import free.jin.console.Console;
import free.jin.console.ConsoleDesignation;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the freechess.org server.
 */

public class FreechessConsoleManager extends ConsoleManager{
  
  
  
  /**
   * Returns a FICS-specific system console designation.
   */
  
  protected ConsoleDesignation createSystemConsoleDesignation(){
    return new FreechessSystemConsoleDesignation();
  }



  /**
   * Creates a <code>FreechessConsole</code> with the specified designation.
   */

  protected Console createConsole(ConsoleDesignation designation){
    return new FreechessConsole(this, designation);
  }
  
  
  
  /**
   * Returns the FICS encoding, which is 7-bit ASCII.
   */
  
  public String getEncoding(){
    return "US-ASCII";
  }
  
  
  
  /**
   * Returns <code>false</code>. 
   */
  
  public boolean supportsMultipleEncodings(){
    return false;
  }
  


  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new FreechessConsolePrefsPanel(this);
  }
  
  
  
}
