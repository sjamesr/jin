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
import free.jin.event.JinEvent;
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
   * Returns whether the specified event is accepted by this
   * <code>ConsoleDesignation</code>.
   */
  
  protected abstract boolean accept(JinEvent evt);
  
  
  
  /**
   * Appends the specified event to the console, causing it to be displayed
   * there in some manner. 
   */
  
  protected abstract void append(JinEvent evt);
  
  
  
}
