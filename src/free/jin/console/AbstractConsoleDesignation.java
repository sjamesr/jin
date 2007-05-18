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

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import free.jin.Connection;
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;
import free.util.AbstractNamed;
import free.util.TextUtilities;




/**
 * A skeleton implementation of <code>ConsoleDesignation</code> wit some useful
 * facilities.
 */

public abstract class AbstractConsoleDesignation implements ConsoleDesignation{
  
  
  
  /**
   * The name of the designation.
   */
  
  private String name;
  
  
  
  /**
   * The encoding used to encode/decode certain messages sent to/received from
   * the server.
   */
  
  private String encoding;
  
  
  
  /**
   * Whether the console is closeable.
   */
  
  private final boolean isConsoleCloseable;
  
  
  
  /**
   * The console we're responsible for.
   */
  
  private Console console;
  
  
  
  /**
   * Our property change support.
   */
  
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this); 
  
  
  
  /**
   * Creates a new <code>AbstractConsoleDesignation</code> with the specified
   * name, encoding and closeable status.
   */
  
  public AbstractConsoleDesignation(String name, String encoding, boolean isConsoleCloseable){
    this.name = name;
    this.encoding = encoding;
    this.isConsoleCloseable = isConsoleCloseable;
  }
  
  
  
  /**
   * The tag subclasses should use for tagging commands and messages sent to the
   * server. 
   */
  
  public String getTag(){
    return Integer.toHexString(System.identityHashCode(this));
  }
  
  
  
  /**
   * Sets our console.
   */
  
  public void setConsole(Console console){
    this.console = console;
    
    console.addComponentListener(new ComponentAdapter(){
      public void componentShown(ComponentEvent e){
        consoleShown();
      }
      public void componentHidden(ComponentEvent e){
        consoleHidden();
      }
    });
    
    joinForums(console.getConsoleManager().getConn());
  }
  
  
  
  /**
   * Returns the console we're responsible for.
   */
  
  public Console getConsole(){
    return console;
  }
  
  
  
  /**
   * This method should be overridden by subclasses to join any forums this
   * designation accepts which need to be joined explicitly.
   */
  
  protected void joinForums(Connection connection){
    
  }
  
  
  
  /**
   * Invoked when the console we're responsible for is shown.
   */
  
  protected void consoleShown(){
    
  }
  
  
  
  /**
   * Invoked when the console we're responsible for is hidden.
   */
  
  protected void consoleHidden(){
    
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public void addPropertyChangeListener(PropertyChangeListener listener){
    propertyChangeSupport.addPropertyChangeListener(listener);
  }
  
  
  
  /**
   * Removes a property change listener.
   */
  
  public void removePropertyChangeListener(PropertyChangeListener listener){
    propertyChangeSupport.removePropertyChangeListener(listener);
  }
  
  
  
  /**
   * Returns the name of this designation.
   */

  public String getName(){
    return name;
  }
  
  
  
  /**
   * Sets the name of this designation.
   */
  
  protected void setName(String name){
    String oldName = this.name;
    this.name = name;
    
    propertyChangeSupport.firePropertyChange("name", oldName, name);
  }
  
  
  
  /**
   * Encodes the specified string for sending to the server, according to the
   * connection's encoding and the encoding of this console designation.
   */
  
  protected final String encode(String s, Connection conn){
    return convert(s, encoding, conn.getTextEncoding());
  }
  
  
  
  /**
   * Decodes the specified message received from the server according to the
   * connection's encoding and the encoding of this console designation.
   */
  
  protected final String decode(String s, Connection conn){
    return convert(s, conn.getTextEncoding(), encoding);
  }
  
  
  
  /**
   * Converts the specified string from the between the specified encodings.
   * If either of the encodings is <code>null</code>, no conversion is
   * performed.
   */
  
  private static String convert(String s, String fromEncoding, String toEncoding){
    if ((fromEncoding == null) || (toEncoding == null))
      return s;
    else
      return TextUtilities.convert(s, fromEncoding, toEncoding);
  }
  
  
  
  /**
   * Sets the encoding. This method is a temporary measure - the encoding should
   * be a final field when the preferences UI allows it to be adjusted per
   * console. Modifying the encoding should change the designation of the
   * console to a new one. 
   */
  
  void setEncoding(String encoding){
    this.encoding = encoding;
  }
  
  
  
  /**
   * Returns whether the console is closeable.
   */
  
  public boolean isConsoleCloseable(){
    return isConsoleCloseable;
  }
  
  
  
  /**
   * Splits the reception of an event into two phases - accepting (or declining)
   * it and, if accepted, sending it to the console.
   */
  
  public void receive(JinEvent evt){
    if (accept(evt))
      append(evt);
  }
  
  
  
  /**
   * Returns whether the specified event's client tag is the same tag as ours.
   * 
   * @see getTag()
   */
  
  protected boolean isTaggedByUs(JinEvent evt){
    String tag = getTag();
    return (tag != null) && (tag.equals(evt.getClientTag()));
  }
  
  
  
  /**
   * Returns whether the specified event is accepted by this
   * <code>ConsoleDesignation</code>.
   */
  
  protected abstract boolean accept(JinEvent evt);
  
  
  
  /**
   * Appends the specified event to the console, causing it to be displayed
   * there in some manner. The default implementation passes the event to
   * either {@link #appendChat(ChatEvent)} or
   * {@link #appendPlainText(PlainTextEvent)}, based on the class of the event.  
   */
  
  protected void append(JinEvent evt){
    if (evt instanceof PlainTextEvent)
      appendPlainText((PlainTextEvent)evt);
    else if (evt instanceof ChatEvent)
      appendChat((ChatEvent)evt);
  }
  
  
  
  /**
   * Appends the specified chat event to the console. The default implementation
   * appends the simple <code>[username][titles]: [message]</code> string to the
   * console.
   */
  
  protected void appendChat(ChatEvent evt){
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
   * Appends the text of the specified plain text event to the console.
   */
  
  protected void appendPlainText(PlainTextEvent evt){
    getConsole().addToOutput(decode(evt.getText(), evt.getConnection()), "plain");
  }

  
  
  
  /**
   * A skeleton implementation of a command type.
   */
  
  protected abstract class AbstractCommandType extends AbstractNamed implements CommandType{
    
    
    
    /**
     * Creates a new <code>AbstractCommandType</code> with the specified name.
     */
    
    public AbstractCommandType(String name){
      super(name);
    }
    
    
    
    /**
     * Invoked when a command is issued by the user with this command type.
     * In turn, the default implementation invokes <code>executeCommand</code>
     * or <code>sendCommand</code> and, if the <code>doNotEcho</code> flag is
     * unset, <code>echoCommand</code>. If <code>userText</code> is prefixed
     * with a forward slash, it is stripped and passed to
     * <code>sendCommand</code>; otherwise, it is passed as-is to
     * <code>executeCommand</code>.
     */
    
    public void handleCommand(String userText, Connection connection, boolean doNotEcho){
      if (userText.startsWith("/"))
        sendCommand(userText.substring(1), connection);
      else  
        send(userText, connection);
      
      if (!doNotEcho)
        echo(userText, connection.getUser());
    }
    
    
    
    /**
     * Sends the specified command to the server. The default implementation
     * sends a command tagged with {@link AbstractConsoleDesignation#getTag()}.
     */
    
    protected void sendCommand(String command, Connection connection){
      connection.sendTaggedCommand(encode(command, connection), getTag());
    }
    
    
    
    /**
     * "Sends" the specified user text. Usually, this meands sending some
     * command to the server; what exactly, depends on the nature of the
     * console.
     * 
     * @param userText The text entered by the user.
     * @param connection The connection to the server.
     */
    
    protected abstract void send(String userText, Connection connection);
      
    
    
    /**
     * Echoes the text entered by the user to the console.
     * 
     * @param userText The text entered by the user.
     * @param user The user we're logged in with.
     */
    
    protected abstract void echo(String userText, ServerUser user);
      
    
    
  }
  
  
  
}
