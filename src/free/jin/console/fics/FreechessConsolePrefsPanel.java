/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
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

import free.jin.console.ConsoleManager;
import free.jin.console.prefs.ChannelTextPrefsPanel;
import free.jin.console.prefs.ConsolePrefsPanel;
import free.jin.console.prefs.TextPrefsPanel;



/**
 * A FICS specific console preferences panel.
 */

public class FreechessConsolePrefsPanel extends ConsolePrefsPanel{



  /**
   * Creates a new <code>FreechessConsolePrefsPanel</code>.
   */
  
  public FreechessConsolePrefsPanel(ConsoleManager cm){
    super(cm);
  }
  
  
  
  /**
   * Overrides <code>createTextPrefsPanel</code> to return a
   * <code>ChannelTextPrefsPanel</code> instead of a regular
   * <code>TextPrefsPanel</code>.
   */
  
  protected TextPrefsPanel createTextPrefsPanel(ConsoleManager cm){
    return new ChannelTextPrefsPanel(cm);
  }
  

  
}
