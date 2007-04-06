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

package free.jin.console;

import free.jin.I18n;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;



/**
 * A base implementation of the designation for a "System" console. This class
 * is meant to be subclassed by server-specific classes.
 */

public abstract class SystemConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>SystemConsoleDesignation</code>.
   */
  
  public SystemConsoleDesignation(){
    super(I18n.get(SystemConsoleDesignation.class).getString("name"), false);
  }
  
  
  
  /**
   * The system console displays all events.
   */
  
  protected boolean accept(JinEvent evt){
    return true;
  }
  
  
  
  /**
   * Appends the specified event to the console.
   */
  
  protected void append(JinEvent evt, String encoding, Console console){
    if (evt instanceof PlainTextEvent)
      appendPlainText((PlainTextEvent)evt, encoding, console);
    else if (evt instanceof ChatEvent)
      appendChat((ChatEvent)evt, encoding, console);
  }
  
  
  
  /**
   * Appends the text of the specified plain text event to the console.
   */
  
  private void appendPlainText(PlainTextEvent evt, String encoding, Console console){
    console.addToOutput(evt.getText(encoding), "plain");
  }
  
  
  
  /**
   * Appends the text for the specified chat event to the console.
   * 
   * @see #textForChat(ChatEvent, String)
   */
  
  private void appendChat(ChatEvent evt, String encoding, Console console){
    console.addToOutput(textForChat(evt, encoding), console.textTypeForEvent(evt));
    if (isPersonalTell(evt))
      console.personalTellReceived(evt.getSender());
  }
  
  
  
  /**
   * Returns the text which should be added to the console for the specified
   * chat event.
   */
  
  protected abstract String textForChat(ChatEvent evt, String encoding);
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell to the user.
   */
  
  protected abstract boolean isPersonalTell(ChatEvent evt);
  
  
  
}
