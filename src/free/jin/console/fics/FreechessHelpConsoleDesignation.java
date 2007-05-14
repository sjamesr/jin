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

import free.jin.console.HelpConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A console designation for FICS's help channel.
 */

public class FreechessHelpConsoleDesignation extends HelpConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>FreechessHelpConsoleDesignation</code> with the
   * specified encoding and closeable status.
   */
  
  public FreechessHelpConsoleDesignation(String encoding, boolean isCloseable){
    super(encoding, isCloseable);
  }
  
  
  
  /**
   * Returns whether the specified event is a help channel tell.
   */
  
  protected boolean accept(JinEvent evt){
    if (!super.accept(evt))
      return false;
    
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    return "channel-tell".equals(chatEvent.getType()) &&
      Integer.valueOf(1).equals(chatEvent.getForum());
  }
  
  
  
}
