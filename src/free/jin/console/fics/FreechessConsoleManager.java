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

import java.util.HashMap;
import java.util.Map;

import free.jin.Game;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.ConsoleDesignation;
import free.jin.console.ConsoleManager;
import free.jin.console.PersonalChatConsoleDesignation;
import free.jin.console.ics.ShoutChatConsoleDesignation;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the freechess.org server.
 */

public class FreechessConsoleManager extends ConsoleManager{
  
  
  
  /**
   * Returns a FICS-specific system console designation.
   */
  
  protected ConsoleDesignation createSystemConsoleDesignation(){
    return new FreechessSystemConsoleDesignation(getConn(), getEncoding());
  }
  
  
  
  /**
   * Returns a FICS-specific help console designation.
   */
  
  protected ConsoleDesignation createHelpConsoleDesignation(boolean isCloseable){
    return new FreechessHelpConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Returns a FICS-specific general chat console designation.
   */
  
  protected ConsoleDesignation createGeneralChatConsoleDesignation(boolean isCloseable){
    return new ShoutChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Returns a FICS-specific personal chat console designation.
   */
  
  protected ConsoleDesignation createPersonalChatConsoleDesignation(ServerUser user, boolean isCloseable){
    return new PersonalChatConsoleDesignation(getConn(), user, getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Creates an ICC-specific game chat console designation.
   */
  
  protected ConsoleDesignation createGameConsoleDesignation(Game game){
    return new FreechessGameConsoleDesignation(getConn(), game, getEncoding());
  }
  
  
  
  /**
   * Creates a <code>FreechessConsole</code> with the specified designation.
   */

  protected Console createConsole(ConsoleDesignation designation){
    return new FreechessConsole(this, designation);
  }
  
  
  
  /**
   * Returns the set of FICS channels.
   */
  
  protected Map createChannels(){
    Map channels = new HashMap();
    
    for (int i = 0; i < 256; i++)
      channels.put(new Integer(i), new FicsChannel(i));
    
    return channels;
  }
  
  
  
  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new FreechessConsolePrefsPanel(this);
  }
  
  
  
}
