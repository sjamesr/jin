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

import javax.swing.event.SwingPropertyChangeSupport;

import free.jin.Connection;
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;
import free.util.AbstractNamed;
import free.util.TextUtilities;
import free.util.Utilities;




/**
 * A base implementation of <code>ConsoleDesignation</code> with some useful
 * facilities.
 */

public abstract class AbstractConsoleDesignation implements ConsoleDesignation{
  
  
  
  /**
   * The connection to the server.
   */
  
  protected final Connection connection;
  
  
  
  /**
   * Whether to count unseen messages and display their number in the
   * designation name. 
   */
  
  private final boolean countUnseenMessages;
  
  
  
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
  
  private boolean isConsoleCloseable;
  
  
  
  /**
   * Our command types.
   */
  
  private CommandType [] commandTypes = null;
  
  
  
  /**
   * The console we're responsible for.
   */
  
  private Console console;
  
  
  
  /**
   * The number of unseen (added when the console is invisible) messages we've
   * received.
   */
  
  private int unseenMessageCount = 0;

  
  
  /**
   * Our property change support.
   */
  
  private final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this); 
  
  
  
  /**
   * Creates a new <code>AbstractConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public AbstractConsoleDesignation(Connection connection, String name, String encoding, boolean isConsoleCloseable){
    this(connection, false, name, encoding, isConsoleCloseable);
  }
  
  
  
  /**
   * Creates a new <code>AbstractConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param countUnseenMessages Whether to count the number of unseen messages
   * and display it in the designation's name.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public AbstractConsoleDesignation(Connection connection, boolean countUnseenMessages, String name,
      String encoding, boolean isConsoleCloseable){
    
    if (connection == null)
      throw new IllegalArgumentException("connection may not be null");
    
    this.connection = connection;
    this.countUnseenMessages = countUnseenMessages;
    this.name = name;
    this.encoding = encoding;
    this.isConsoleCloseable = isConsoleCloseable;
  }
  
  
  
  
  /**
   * The tag subclasses should use for tagging commands and messages sent to the
   * server. 
   */
  
  protected String getTag(){
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
    
    joinForums();
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
  
  protected void joinForums(){
    
  }
  
  
  
  /**
   * Sets the number of unseen messages and updates the title accordingly.
   */
  
  protected final void setUnseenMessageCount(int unseenMessageCount){
    String oldName = getName();
    this.unseenMessageCount = unseenMessageCount;
    String newName = getName();
    propertyChangeSupport.firePropertyChange("name", oldName, newName);
  }
  
  
  
  /**
   * Invoked when the console we're responsible for is shown.
   */
  
  protected void consoleShown(){
    if (countUnseenMessages)
      setUnseenMessageCount(0);
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
    if (countUnseenMessages && (unseenMessageCount != 0))
      return name + " (" + unseenMessageCount + ")";
    else
      return name;
  }
  
  
  
  /**
   * Sets the name of this designation. If <code>countUnseenMessages</code> is
   * set, the specified name is treated only as a base-name.
   */
  
  protected void setName(String name){
    if (Utilities.areEqual(this.name, name))
      return;
    
    String oldName = getName();
    this.name = name;
    
    propertyChangeSupport.firePropertyChange("name", oldName, getName());
  }
  
  
  
  /**
   * Encodes the specified string for sending to the server, according to the
   * connection's encoding and the encoding of this console designation.
   */
  
  protected final String encode(String s){
    return convert(s, encoding, connection.getTextEncoding());
  }
  
  
  
  /**
   * Decodes the specified message received from the server according to the
   * connection's encoding and the encoding of this console designation.
   */
  
  protected final String decode(String s){
    return convert(s, connection.getTextEncoding(), encoding);
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
   * Sets the closeable status of the console.
   */
  
  protected void setConsoleCloseable(boolean isConsoleCloseable){
    if (this.isConsoleCloseable == isConsoleCloseable)
      return;
    
    boolean oldValue = this.isConsoleCloseable;
    this.isConsoleCloseable = isConsoleCloseable;
    
    propertyChangeSupport.firePropertyChange("consoleCloseable", oldValue, isConsoleCloseable);
  }
  
  
  
  /**
   * Splits the reception of an event into two phases - accepting (or declining)
   * it and, if accepted, sending it to the console.
   */
  
  public boolean receive(JinEvent evt){
    if (accept(evt)){
      append(evt);
      
      if (countUnseenMessages && !console.isVisible())
        setUnseenMessageCount(unseenMessageCount + 1);
      
      return true;
    }
    else
      return false;
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
    String message = decode(chatEvent.getMessage());
    
    String text = sender.getName() + title + ": " + message;
    String textType = console.textTypeForEvent(chatEvent);
    
    console.addToOutput(text, textType);
  }
  
  
  
  /**
   * Appends the text of the specified plain text event to the console.
   */
  
  protected void appendPlainText(PlainTextEvent evt){
    getConsole().addToOutput(decode(evt.getText()), "plain");
  }
  
  
  
  /**
   * Creates the command types we're capable of sending. This method will only
   * be invoked once.
   */
  
  protected abstract CommandType [] createCommandTypes();
  
  
  
  /**
   * Returns the command types we're capable of sending. This method invokes
   * {@link #createCommandTypes()} once, and will return its result on following
   * invocations.
   */
  
  public final CommandType [] getCommandTypes(){
    if (commandTypes == null)
      commandTypes = createCommandTypes();
    
    return commandTypes;
  }

  
  
  
  /**
   * A skeleton implementation of a command type.
   */
  
  protected abstract class AbstractCommandType extends AbstractNamed implements CommandType{
    
    
    
    /**
     * Creates a new <code>AbstractCommandType</code> with the specified name
     * and connection to the server.
     */
    
    public AbstractCommandType(String name){
      super(name);
    }
    
    
    
    /**
     * Invoked when a command is issued by the user with this command type.
     * If <code>userText</code> is prefixed with a forward slash, the slash is
     * stripped and the remaining command is passed to <code>sendCommand</code>
     * and, if the <code>doNotEcho</code> flag is unset, to
     * <code>echoCommand</code>.
     * Otherwise, it is passed to <code>send</code> and, if the
     * <code>doNotEcho</code> flag is unset, to <code>echo</code>.
     */
    
    public void handleCommand(String userText, boolean doNotEcho){
      if (userText.startsWith("/")){
        String command = userText.substring(1);
        sendCommand(command);
        if (!doNotEcho)
          echoCommand(command);
      }
      else{  
        send(userText);
        if (!doNotEcho)
          echo(userText);
      }
    }
    
    
    
    /**
     * Sends the specified command to the server. The default implementation
     * sends a command tagged with {@link AbstractConsoleDesignation#getTag()}.
     */
    
    protected void sendCommand(String command){
      connection.sendTaggedCommand(encode(command), getTag());
    }
    
    
    
    /**
     * Echoes the specified command to the console. The default implementation
     * simply appends the command's text to the console.
     */
    
    protected void echoCommand(String command){
      Console console = getConsole();
      console.addToOutput(command, console.getUserTextType());
    }
    
    
    
    /**
     * "Sends" the specified user text. Usually, this meands sending some
     * command to the server; what exactly, depends on the nature of the
     * console.
     * 
     * @param userText The text entered by the user.
     * @param connection The connection to the server.
     */
    
    protected abstract void send(String userText);
      
    
    
    /**
     * Echoes the text entered by the user to the console.
     * 
     * @param userText The text entered by the user.
     * @param user The user we're logged in with.
     */
    
    protected abstract void echo(String userText);
      
    
    
  }
  
  
  
}
