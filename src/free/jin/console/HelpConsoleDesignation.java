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
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A base implementation of a console designation for the server's interactive
 * helping system.
 */

public abstract class HelpConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * A command type which sends the help question to the server.
   */
  
  private final CommandType askHelpQuestion = new AskHelpQuestion();
  
  
  
  /**
   * Creates a new <code>HelpConsoleDesignation</code> with the specified
   * encoding and closeable status.
   */
  
  public HelpConsoleDesignation(String encoding, boolean isCloseable){
    super(I18n.get(HelpConsoleDesignation.class).getString("name"),
        encoding, isCloseable);
  }
  
  
  
  /**
   * Joins the help forum.
   */
  
  protected void joinForums(Connection connection){
    connection.joinHelpForum();
  }
  
  
  
  /**
   * The default implementation accepts all chat events.
   */
  
  protected boolean accept(JinEvent evt){
    return evt instanceof ChatEvent;
  }
  
  
  
  /**
   * Appends the specified help message to the console.
   */

  protected void append(JinEvent evt){
    ChatEvent chatEvent = (ChatEvent)evt;
    Console console = getConsole();
    
    ServerUser sender = chatEvent.getSender();
    String title = chatEvent.getSenderTitle();
    String message = decode(chatEvent.getMessage(), chatEvent.getConnection());
    
    String text = sender.getName() + title + ": " + message;
    String textType = console.textTypeForEvent(chatEvent);
    
    console.addToOutput(text, textType);
  }
  
  
  
  /**
   * Returns the <code>AskHelpQuestion</code> command type.
   */
  
  public CommandType[] getCommandTypes(){
    return new CommandType[]{askHelpQuestion};
  }
  
  
  
  /**
   * A <code>CommandType</code> which asks a help question.
   */
  
  private class AskHelpQuestion extends AbstractCommandType{
    
    
    
    /**
     * Creates a new <code>AskHelpQuestion</code> command type.
     */
    
    public AskHelpQuestion(){
      super(I18n.get(AskHelpQuestion.class).getString("name"));
    }
    
    
    
    /**
     * Asks the specified help question.
     */
    
    protected void executeCommand(String userText, Connection connection){
      connection.sendHelpQuestion(encode(userText, connection));
    }
    
    
    
    /**
     * Echoes the question to the console.
     */
    
    protected void echoCommand(String userText, ServerUser user){
      Console console = getConsole();
      console.addToOutput(user.getName() + ": " + userText, console.getUserTextType());
    }
    
    
    
  }
  
  
  
}
