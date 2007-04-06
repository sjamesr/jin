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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTabbedPane;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.ServerUser;
import free.jin.console.prefs.ConsolePrefsPanel;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ConnectionListener;
import free.jin.event.JinEvent;
import free.jin.event.ListenerManager;
import free.jin.event.PlainTextEvent;
import free.jin.event.PlainTextListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.PreferencesPanel;
import free.jin.ui.UIProvider;


/**
 * A Plugin which implements the consoles functionality. It's responsible for
 * opening, positioning and closing the various consoles used by the user.
 * The class is abstract because some of the functionality is delegated to
 * server-specific classes.
 */

public abstract class ConsoleManager extends Plugin implements PlainTextListener,
    ChatListener, ConnectionListener{
  
  
  
  /**
   * The constant specifying "embedded" game lists display style.
   */
  
  public static final int EMBEDDED_GAME_LISTS = 0;
  
  
  
  /**
   * The constant specifying "external" game lists display style.
   */
  
  public static final int EXTERNAL_GAME_LISTS = 1;
  
  
  
  /**
   * The constant specifying that game lists aren't displayed specially.
   */
  
  public static final int NO_GAME_LISTS = 2;


  
  /**
   * The consoles.
   */

  private final List consoles = new ArrayList();



  /**
   * Our main container.
   */

  private PluginUIContainer uiContainer;
  
  
  
  /**
   * The tabbed pane in which the consoles sit.
   */
  
  private JTabbedPane consolesTabbedPane;



  /**
   * Are we currently paused?
   */
   
  private boolean isPaused = false;
  
  
  
  /**
   * A queue of the events we've accumulated while being paused.
   */
   
  private final Vector pausedEventsQueue = new Vector();



  /**
   * Starts this plugin.
   */

  public void start(){
    createUI();
    createConsoles();
    uiContainer.setVisible(true);
    registerConnListeners();
  }



  /**
   * Stops the plugin.
   */

  public void stop(){
    unregisterConnListeners();
  }



  /**
   * Creates the consoles we'll be displaying. 
   */
  
  private void createConsoles(){
    int consoleCount = getPrefs().getInt("consoles.count", 0);
    for (int i = 0; i < consoleCount; i++){
      ConsoleDesignation designation = 
        loadConsoleDesignation("consoles." + i + ".");
      addConsole(designation, false);
    }
  }
  
  
  
  /**
   * Creates a <code>ConsoleDesignation</code> from preferences, using the
   * specified prefix.
   */
  
  private ConsoleDesignation loadConsoleDesignation(String prefsPrefix){
    Preferences prefs = getPrefs();
    I18n i18n = getI18n();

    String type = prefs.getString(prefsPrefix + "type");
    if ("system".equals(type))
      return createSystemConsoleDesignation();
    else if ("chat".equals(type)){
      String name = prefs.getString(prefsPrefix + "name", null);
      if (name == null)
        name = i18n.getString(prefs.getString(prefsPrefix + "nameKey"));
      
      ChatConsoleDesignation designation =
          new ChatConsoleDesignation(name, false);
      
      int acceptedCount = prefs.getInt(prefsPrefix + "accepted.count");
      for (int j = 0; j < acceptedCount; j++){
        String acceptedPrefix = prefsPrefix + "accepted." + j + ".";
        String chatType = prefs.getString(acceptedPrefix + "chatType", null);
        Object forum = prefs.get(acceptedPrefix + "forum", null);
        String senderName = prefs.getString(acceptedPrefix + "sender", null);
        ServerUser sender = senderName == null ? 
            null : getConn().userForName(senderName);
        
        designation.addAccepted(chatType, forum, sender);
      }
      
      return designation;
    }
//    else if ("text".equals(type)){
//
//    }
//    else if ("union".equals(type)){
//      
//    }
    else
      throw new IllegalArgumentException("Unrecognized designation type: " + type);
  }
  
  
  
  /**
   * Creates the "system" console designation. This method is meant to be
   * implemented by a server-specific console manager.
   */
  
  protected abstract ConsoleDesignation createSystemConsoleDesignation();
  
  
  
  /**
   * Creates and displays the plugin's UI.
   */

  private void createUI(){
    I18n i18n = getI18n();

    uiContainer = createContainer("", UIProvider.ESSENTIAL_CONTAINER_MODE);
    uiContainer.setTitle(i18n.getString("mainConsole.initialTitle"));

    URL iconImageURL = ConsoleManager.class.getResource("icon.gif");
    if (iconImageURL != null)
      uiContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));
    
    consolesTabbedPane = new JTabbedPane(JTabbedPane.TOP);
    
    Container content = uiContainer.getContentPane();
    content.setLayout(new BorderLayout());
    
    uiContainer.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIActivated(PluginUIEvent evt){
        obtainFocus();
      }
    });
  }
  
  
  
  /**
   * Transfers the focus to the currently visible console.
   */
  
  private void obtainFocus(){
    Console console;
    if (consoles.size() == 1)
      console = (Console)consoles.get(0);
    else
      console = (Console)consolesTabbedPane.getSelectedComponent();
    
    if (console != null)
      console.obtainFocus();
  }
  
  
  
  /**
   * Adds a console with the specified designation, possibly making it the
   * active console.
   */
  
  public void addConsole(ConsoleDesignation designation, boolean makeActive){
    Console console = createConsole(designation);
    consoles.add(console);
    
    if (consoles.size() == 1)
      setSingleConsoleMode();
    else if (consoles.size() == 2)
      setMultiConsoleMode();
    else
      consolesTabbedPane.addTab(designation.getName(), console);
      
    console.addComponentListener(new ComponentAdapter(){
      public void componentShown(ComponentEvent e){
        Console console = (Console)e.getSource();
        console.obtainFocus();
      }
    });
    
    if (makeActive){
      if (consoles.size() > 1)
        consolesTabbedPane.setSelectedIndex(consolesTabbedPane.getComponentCount() - 1);
      if (uiContainer != null)
        uiContainer.setActive(true);
    }
  }
  
  
  
  /**
   * Puts us into single console mode.
   */
  
  private void setSingleConsoleMode(){
    Container contentPane = uiContainer.getContentPane();
    contentPane.removeAll();
    contentPane.add((Console)consoles.get(0));
  }
  
  
  
  /**
   * Puts us into multi console mode.
   */
  
  private void setMultiConsoleMode(){
    Container contentPane = uiContainer.getContentPane();
    
    consolesTabbedPane.removeAll();
    for (Iterator i = consoles.iterator(); i.hasNext();){
      Console console = (Console)i.next();
      consolesTabbedPane.addTab(console.getDesignation().getName(), console);
    }
    
    contentPane.removeAll();
    contentPane.add(consolesTabbedPane);
  }
  
  
  
  
  
  /**
   * Adds a temporary console for chatting with the specified user, possibly
   * making it the active console.
   */
  
  public void addPersonalChatConsole(ServerUser user, boolean makeActive){
    addConsole(new PersonalChatConsoleDesignation(user, true), makeActive);
  }
  
  
  
  /**
   * Removes the specified console.
   */
  
  public void removeConsole(Console console){
    int index = consoles.indexOf(console);
    if (index == -1)
      throw new IllegalArgumentException("Unknown console: " + console);
    
    consoles.remove(index);
    
    if (consoles.size() == 1)
      setSingleConsoleMode();
    else
      consolesTabbedPane.removeTabAt(index);
  }
  
  
  
  /**
   * Creates a single <code>Console</code> for this <code>ConsoleManager</code>.
   * @param designation TODO
   */

  protected Console createConsole(ConsoleDesignation designation){
    return new Console(this, designation);
  }
  
  
  
  /**
   * Sets whether text is copied to the clipboard automatically, upon selection.
   */
  
  public void setCopyOnSelect(boolean isCopyOnSelect){
    getPrefs().setBool("copyOnSelect", isCopyOnSelect);
  }
  
  
  
  /**
   * Returns whether text is copied to the clipboard automatically, upon
   * selection.
   */
  
  public boolean isCopyOnSelect(){
    return getPrefs().getBool("copyOnSelect", true);
  }
  
  
  
  /**
   * Returns the encoding with which we decode/encode text received/sent from/to the server.
   * The default implementation retrieves it from preferences.
   */
  
  public String getEncoding(){
    return getPrefs().getString("encoding", "utf8");
  }
  
  
  
  /**
   * Sets the encoding with which we decode/encode text received/sent from/to the server.
   * Throws an exception if multiple encodings are unsupported, otherwise stores the
   * encoding in preferences.
   */
  
  public void setEncoding(String encoding) throws UnsupportedEncodingException{
    if (!supportsMultipleEncodings())
      throw new IllegalStateException("Multiple encodings are unsupported");
    if (!Charset.isSupported(encoding))
      throw new UnsupportedEncodingException(encoding);
    
    getPrefs().setString("encoding", encoding);
  }
  
  
  
  /**
   * Returns whether this console manager supports more than its default encoding.
   * The default implementation returns <code>false</code>. 
   */
  
  public boolean supportsMultipleEncodings(){
    return false;
  }
  
  
  
  /**
   * Encodes a string to be sent to the server.
   */
  
  public String encode(String s){
    try{
      return new String(s.getBytes(getEncoding()), "ISO8859_1");
    } catch (UnsupportedEncodingException e){
        e.printStackTrace(); // Shouldn't happen - we're using supported encodings
      }
    
    return s;
  }
  
  
  
  /**
   * Decodes a string received from the server.
   */
  
  public String decode(String s){
    try{
      return new String(s.getBytes("ISO8859_1"), getEncoding());
    } catch (UnsupportedEncodingException e){
        e.printStackTrace(); // Shouldn't happen - we're using supported encodings
      }
    
    return s;
  }
  


  /**
   * Rereads the plugin/user properties and changes settings accordingly.
   * This method should be called when the user changes the preferences.
   */

  public void refreshFromProperties(){
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.refreshFromProperties();
    }
  }
  
  
  
  /**
   * Registers all the necessary listeners to JinConnection events.
   */

  protected void registerConnListeners(){
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.addPlainTextListener(this);
    listenerManager.addChatListener(this);
    listenerManager.addConnectionListener(this);
  }




  /**
   * Unregisters all the listeners we've registered with the JinConnection.
   */

  protected void unregisterConnListeners(){
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.removePlainTextListener(this);
    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);
  }



  /**
   * Adds the specified line of text to all the consoles.
   */

  public void addSpecialLine(String line){
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.addToOutput(line, "special");
    }
  }
  
  
  
  /**
   * Listens to plain text events and adds them to the consoles.
   */

  public void plainTextReceived(PlainTextEvent evt){
    eventForConsoleReceived(evt);
  }
  
  
  
  /**
   * Listens to ChatEvents and adds appropriate text to the console.
   */

  public void chatMessageReceived(ChatEvent evt){
    eventForConsoleReceived(evt);
  }
  
  
  
  /**
   * Invoked when an event which may be displayed in a console occurs.
   */
  
  protected void eventForConsoleReceived(JinEvent evt){
    if (isPaused()){
      pausedEventsQueue.addElement(evt);
      return;
    }
    
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.getDesignation().receive(evt, getEncoding(), console);
    }
  }
  
  
  
  /**
   * Sends the specified user-inputted command to the server on behalf of the
   * specified console.
   */
  
  public void sendUserCommand(String command, Console console){
    Connection conn = getConn();
    if (conn.isConnected())
      conn.sendCommand(encode(command));
    else
      console.addToOutput(I18n.get(ConsoleManager.class).getString("unconnectedWarningMessage"), "info");
  }
  
  
  
  /**
   * Sets the pause state of the console manager.
   */
   
  public void setPaused(boolean isPaused){
    this.isPaused = isPaused;
    
    if (!isPaused){
      int size = pausedEventsQueue.size();
      for (int i = 0; i < size; i++){
        try{
          Object evt = pausedEventsQueue.elementAt(i);
          if (evt instanceof PlainTextEvent)
            plainTextReceived((PlainTextEvent)evt);
          else if (evt instanceof ChatEvent)
            chatMessageReceived((ChatEvent)evt);
        } catch (Exception e){e.printStackTrace();}
      }
      pausedEventsQueue.removeAllElements();
    }
  }
  
  
  
  /**
   * Returns whether the console manager is currently paused.
   */
   
  public boolean isPaused(){
    return isPaused;
  }




  /**
   * Translates the given ChatEvent into a string to be added to the console's
   * output. This method is intended to be overridden by server specific classes.
   * The default implementation returns a string useful only for debugging.
   */

  protected String translateChat(ChatEvent evt){
    return evt.toString();
  }
  
  
  
  /**
   * Gets called when a connection is attempted.
   */

  public void connectionAttempted(Connection conn, String hostname, int port){
    // We pass a String instead of an Integer for the port because an Integer is translated
    // according to the locale and then we get something like
    // "Connecting to chessclub.com on port 5,000"
    String message = getI18n().getFormattedString("tryingToConnectMessage",
      new Object[]{hostname, String.valueOf(port)});
    
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.addToOutput(message, "info");
    }
  }



  /**
   * Gets called when the connection to the server is established.
   */

  public void connectionEstablished(Connection conn){
    String message = getI18n().getString("connectedMessage");
    
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.addToOutput(message, "info");
    }
  }



  /**
   * Gets called when the login procedure is done.
   */

  public void loginSucceeded(Connection conn){
    String title = getI18n().getFormattedString("mainConsole.title",
      new Object[]{getConn().getUser().getName(), getServer().getLongName()});
    uiContainer.setTitle(title);
  }



  /**
   * Gets called when the connection to the server is lost.
   */

  public void connectionLost(Connection conn){
    String message = getI18n().getString("disconnectedWarning");
    
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.addToOutput(message, "info");
    }
  }
  
  
  
  // The rest of ConnectionListener's methods.
  public void connectingFailed(Connection conn, String reason){}
  public void loginFailed(Connection conn, String reason){}



  /**
   * Saves the current state into the user file.
   */

  public void saveState(){
    
  }



  /**
   * Returns the string <code>"console"</code>.
   */

  public String getId(){
    return "console";
  }



  /**
   * Returns the name of the plugin.
   */

  public String getName(){
    return getI18n().getString("pluginName");
  }



  /**
   * Overrides <code>hasPreverencesUI</code> to return whether the plugin
   * will display a preferences UI (the setting is taken from the
   * <pre>"preferences.show"</pre> property.
   */

  public boolean hasPreferencesUI(){
    return getPrefs().getBool("preferences.show", true);
  }




  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new ConsolePrefsPanel(this);
  }



}
