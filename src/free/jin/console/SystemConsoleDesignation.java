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

import free.jin.Connection;
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
   * Our sole command type.
   */
  
  private final CommandType commandIssuing = new IssueCommand(); 
  
  
  
  /**
   * Creates a new <code>SystemConsoleDesignation</code> with the specified
   * encoding.
   */
  
  public SystemConsoleDesignation(String encoding){
    super(I18n.get(SystemConsoleDesignation.class).getString("name"), encoding,
        false);
  }
  
  
  
  /**
   * Returns our sole command type - sending user-typed commands as-is to the
   * server.
   */
  
  public CommandType [] getCommandTypes(){
    return new CommandType[]{commandIssuing};
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
  
  protected void append(JinEvent evt){
    if (evt instanceof PlainTextEvent)
      appendPlainText((PlainTextEvent)evt);
    else if (evt instanceof ChatEvent)
      appendChat((ChatEvent)evt);
  }
  
  
  
  /**
   * Appends the text of the specified plain text event to the console.
   */
  
  private void appendPlainText(PlainTextEvent evt){
    getConsole().addToOutput(decode(evt.getText(), evt.getConnection()), "plain");
  }
  
  
  
  /**
   * Appends the text for the specified chat event to the console.
   * 
   * @see #textForChat(ChatEvent, String)
   */
  
  private void appendChat(ChatEvent evt){
    Console console = getConsole();
    console.addToOutput(textForChat(evt), console.textTypeForEvent(evt));
    if (isPersonalTell(evt))
      console.personalTellReceived(evt.getSender());
  }
  
  
  
  /**
   * Returns the text which should be added to the console for the specified
   * chat event.
   */
  
  protected abstract String textForChat(ChatEvent evt);
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell to the user.
   */
  
  private boolean isPersonalTell(ChatEvent evt){
    return (evt.getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY);
  }
  
  
  
  /**
   * A command type which sends the command typed by the user as-is to the
   * server.
   */
  
  private class IssueCommand extends CommandType{
    
    
    
    /**
     * Creates a new <code>IssueCommand</code>.
     */
    
    public IssueCommand(){
      super(I18n.get(IssueCommand.class).getString("name"));
    }
    
    
    
    /**
     * Issues the specified command.
     */
    
    public void executeCommand(String command, Connection connection){
      connection.sendCommand(encode(command, connection));
    }
    
    
    
  }
  
  
  
}
