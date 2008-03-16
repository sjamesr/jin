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
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import free.jin.*;
import free.jin.action.JinAction;
import free.jin.event.*;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.PreferencesPanel;
import free.jin.ui.UIProvider;
import free.util.TextUtilities;
import free.util.swing.tabbedpane.*;


/**
 * A Plugin which implements the consoles functionality. It's responsible for
 * opening, positioning and closing the various consoles used by the user.
 * The class is abstract because some of the functionality is delegated to
 * server-specific classes.
 */

public abstract class ConsoleManager extends Plugin implements PlainTextListener,
    ChatListener, ConnectionListener{
  
  
  
  /**
   * The id of this plugin.
   */
  
  public static final String PLUGIN_ID = "console"; 
  
  
  
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
   * (lazily created) map of channel IDs to channels.
   */
  
  private SortedMap channels = null;
  
  
  
  /**
   * The consoles.
   */

  private final List consoles = new ArrayList();
  
  
  
  /**
   * Our main container.
   */

  private PluginUIContainer uiContainer;
  
  
  
  /**
   * The tabbed pane model of the  in which the consoles sit.
   */
  
  private TabbedPane consolesTabbedPane;
  
  
  
  /**
   * Our encoding.
   */
  
  private String encoding;
  
  
  
  /**
   * Are we currently paused?
   */
   
  private boolean isPaused = false;
  
  
  
  /**
   * A queue of the events we've accumulated while being paused.
   */
   
  private final Vector pausedEventsQueue = new Vector();
  
  
  
  /**
   * The system console designation, if any.
   */
  
  private ConsoleDesignation systemConsoleDesignation;
  
  
  
  /**
   * The help console designation, if any.
   */
  
  private ConsoleDesignation helpConsoleDesignation;
  
  
  
  /**
   * The number of temporary console's we've added in the lifetime of this
   * plugin.
   */
  
  private int temporaryConsoleCount = 0;
  
  
  
  /**
   * A game listener which invokes private methods for the events we care about.
   */
  
  private final GameListener gameListener = new GameAdapter(){
    public void gameStarted(GameStartEvent evt){
      ConsoleManager.this.gameStarted(evt);
    }
  };
  
  
  
  /**
   * Starts this plugin.
   */

  public void start(){
    createUI();
    loadState();
    uiContainer.setVisible(true);
    registerConnListeners();
    exportAction(new AskHelpQuestionAction());
  }



  /**
   * Stops the plugin.
   */

  public void stop(){
    unregisterConnListeners();
  }
  
  
  
  /**
   * Loads the plugin's state from preferences.
   */
  
  private void loadState(){
    loadEncoding();
    createConsoles();
  }
  
  
  
  /**
   * Loads the console manager's encoding from preferences.
   */
  
  private void loadEncoding(){
    String encoding = getPrefs().getString("encoding", TextUtilities.getDefaultCharsetName());

    this.encoding = Charset.isSupported(encoding) ? encoding : null;
  }



  /**
   * Creates the consoles we'll be displaying. 
   */
  
  private void createConsoles(){
    Preferences prefs = getPrefs();
    int consoleCount = prefs.getInt("consoles.count");
    int activeConsole = prefs.getInt("consoles.selected", consoleCount - 1);
    for (int i = 0; i < consoleCount; i++){
      ConsoleDesignation designation = 
        loadConsoleDesignation("consoles." + i + ".");
      addConsole(designation, i == activeConsole);
    }
  }
  
  
  
  /**
   * Creates a <code>ConsoleDesignation</code> from preferences, using the
   * specified prefix.
   */
  
  private ConsoleDesignation loadConsoleDesignation(String prefsPrefix){
    Preferences prefs = getPrefs();

    String type = prefs.getString(prefsPrefix + "type");
    if ("system".equals(type)){
      systemConsoleDesignation = createSystemConsoleDesignation();
      return systemConsoleDesignation;
    }
    else if ("help".equals(type)){
      boolean isCloseable = prefs.getBool(prefsPrefix + "closeable", true);
      helpConsoleDesignation = createHelpConsoleDesignation(isCloseable);
      return helpConsoleDesignation;
    }
    else if ("chat".equals(type)){
      boolean isCloseable = prefs.getBool(prefsPrefix + "closeable", true);
      return createGeneralChatConsoleDesignation(isCloseable);
    }
    else if ("custom".equals(type)){
      String title = prefs.getString(prefsPrefix + "title");
      
      String encoding = prefs.getString(prefsPrefix + "encoding");
      
      Object channelsPref = prefs.get(prefsPrefix + "channels", null);
      List channels = channelsPref == null ? Collections.EMPTY_LIST : parseConsoleChannelsPref(channelsPref);
      
      String messageRegex = prefs.getString(prefsPrefix + "messageRegex", null);
      Pattern messageRegexPattern = messageRegex == null ? null : Pattern.compile(messageRegex);
      
      return loadCustomConsoleDesignation(prefsPrefix, title, encoding, channels, messageRegexPattern);
    }
    else
      throw new IllegalArgumentException("Unrecognized designation type: " + type);
  }
  
  
  
  /**
   * Creates the system console designation. This method is meant to be
   * implemented by a server-specific console manager. Note that the returned
   * designation is treated specially - it is only passed events which were not
   * already handled by any of the other consoles.
   */
  
  protected abstract ConsoleDesignation createSystemConsoleDesignation();
  
  
  
  /**
   * Creates the help console designation. This method is meant to be
   * implemented by a server-specific console manager.
   */
  
  protected abstract ConsoleDesignation createHelpConsoleDesignation(boolean isCloseable);
  
  
  
  /**
   * Creates the general chat console designation. This method is meant to be
   * implemented by a server-specific console manager.
   */
  
  protected abstract ConsoleDesignation createGeneralChatConsoleDesignation(boolean isCloseable);
  
  
  
  /**
   * Creates a console designation for personal chat with the specified user.
   */
  
  protected abstract ConsoleDesignation createPersonalChatConsoleDesignation(ServerUser user, boolean isCloseable);
  
  
  
  /**
   * Creates a console designation for chat at a board.
   */
  
  protected abstract ConsoleDesignation createGameConsoleDesignation(Game game);
  
  
  
  /**
   * Loads a custom console designation from the preferences using the specified
   * prefix and the already retrieved data.
   */
  
  protected abstract CustomConsoleDesignation loadCustomConsoleDesignation(String prefsPrefix, 
      String title, String encoding, List channels, Pattern messageRegex);
  
  
  
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
    
    consolesTabbedPane = new TabbedPane(SwingConstants.TOP);
    consolesTabbedPane.setAlwaysShowTabs(false);
    consolesTabbedPane.setBorder(null);
    consolesTabbedPane.getModel().addTabbedPaneListener(new TabbedPaneListener(){
      public void tabRemoved(TabbedPaneEvent evt){
        consoleTabRemoved(evt);
      }
      public void tabAdded(TabbedPaneEvent evt){}
      public void tabSelected(TabbedPaneEvent evt){}
      public void tabDeselected(TabbedPaneEvent evt){}
    });
    
    Container content = uiContainer.getContentPane();
    content.setLayout(new BorderLayout(0, 0));
    content.add(consolesTabbedPane);
    
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
      console = (Console)(consolesTabbedPane.getModel().getSelectedTab().getComponent());
    
    if (console != null)
      console.obtainFocus();
  }
  
  
  
  /**
   * Invoked when a console tab is removed.
   */
  
  private void consoleTabRemoved(TabbedPaneEvent evt){
    consoles.remove(evt.getTabIndex());
  }

  
  
  /**
   * Returns the console with the specified designation, or <code>null</code> if
   * none exists.
   */
  
  public Console getConsole(ConsoleDesignation designation){
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      if (console.getDesignation().equals(designation))
        return console;
    }
    
    return null;
  }
  
  
  
  /**
   * Adds a console with the specified designation, possibly making it the
   * active console. If a console with the specified designation already exists,
   * it is merely made selected, if <code>makeSelected</code> is
   * <code>true</code> (otherwise, no action is taken).
   */
  
  public void addConsole(final ConsoleDesignation designation, boolean makeSelected){
    Console console = getConsole(designation);
    
    if (console == null){
      console = createConsole(designation);
      consoles.add(console);
      designation.setConsole(console);
      
      final Tab tab = new Tab(console, designation.getName(), null, designation.isConsoleCloseable());
      consolesTabbedPane.getModel().addTab(tab);
      
      designation.addPropertyChangeListener(new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent evt){
          String propertyName = evt.getPropertyName();
          if ("name".equals(propertyName))
            tab.setTitle(designation.getName());
          else if ("consoleCloseable".equals(propertyName))
            tab.setCloseable(designation.isConsoleCloseable());
        }
      });
        
      console.addComponentListener(new ComponentAdapter(){
        public void componentShown(ComponentEvent e){
          Console console = (Console)e.getSource();
          console.obtainFocus();
        }
      });
    }
    
    if (makeSelected)
      makeConsoleSelected(console);
  }
  
  
  
  /**
   * Makes the specified console selected, which means that it is the one
   * displayed in the consoles tabbed pane.
   */
  
  protected void makeConsoleSelected(Console console){
    int index = consoles.indexOf(console);
    
    if (index >= 0)
      consolesTabbedPane.getModel().setSelectedIndex(index);
  }
  
  
  
  /**
   * Makes our UI active.
   */
  
  public void makeActive(){
    if (uiContainer != null)
      uiContainer.setActive(true);
  }
  
  
  
  /**
   * Adds a closeable console for chatting with the specified user, possibly
   * making it the active console. If one already exists, it is merely
   * activated, if <code>makeActive</code> is <code>true</code> (otherwise, no
   * action is taken).
   * 
   * @returns the newly created personal chat designation.
   */
  
  public ConsoleDesignation addPersonalChatConsole(ServerUser user, boolean makeActive){
    ConsoleDesignation designation = createPersonalChatConsoleDesignation(user, true);
    addConsole(designation, makeActive);
    return designation;
  }
  
  
  
  /**
   * Adds a new temporary console which will send the specified commands to the
   * server and display their output.
   */
  
  public void addTemporaryConsole(String [] commands){
    int index;
    synchronized(this){
      index = ++temporaryConsoleCount;
    }
    
    String consoleName = getI18n().getFormattedString("temporaryConsole.title", new Object[]{String.valueOf(index)});
    ConsoleDesignation designation = new TemporaryConsoleDesignation(getConn(), consoleName, getEncoding(), true, commands);
    addConsole(designation, true);
  }
  
  
  
  /**
   * Adds a closeable console for getting help on the server. If such a console
   * already exists, it is merely activated.
   */
  
  public void activateHelpConsole(){
    Console helpConsole = null;
    
    if (helpConsoleDesignation == null)
      helpConsoleDesignation = createHelpConsoleDesignation(true);
    else
      helpConsole = getConsole(helpConsoleDesignation);
    
    if (helpConsole == null){
      addConsole(helpConsoleDesignation, true);
      helpConsole = getConsole(helpConsoleDesignation);
    }
    else{
      makeConsoleSelected(helpConsole);
      makeActive();
    }
    
    helpConsole.flashInputField();
  }
  
  
  
  /**
   * Removes the specified console.
   */
  
  public void removeConsole(Console console){
    TabbedPaneModel model = consolesTabbedPane.getModel();
    model.removeTab(model.indexOfComponent(console));
  }
  
  
  
  /**
   * Creates a single <code>Console</code> for this <code>ConsoleManager</code>.
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
   * Returns the console manager's encoding.
   */
  
  public String getEncoding(){
    return encoding;
  }
  
  
  
  /**
   * Sets the encoding with which we decode/encode text received/sent from/to
   * the server. This is only relevant if the connections text encoding is
   * non-null. See {@link free.jin.Connection#getTextEncoding()} for more
   * information. A <code>null</code> value means the encoding will be re-set
   * to the system's default encoding.
   */
  
  public void setEncoding(String encoding) throws UnsupportedEncodingException{
    if (encoding == null)
      encoding = TextUtilities.getDefaultCharsetName();
    
    if (!Charset.isSupported(encoding))
      throw new UnsupportedEncodingException(encoding);
    
    getPrefs().setString("encoding", encoding);
  }
  
  
  
  /**
   * Encodes the specified string for sending to the server, according to the
   * connection's encoding and the specified encoding.
   */
  
  protected final String encode(String s, String encoding){
    return TextUtilities.convert(s, encoding, getConn().getTextEncoding());
  }
  
  
  
  /**
   * Decodes the specified message received from the server according to the
   * connection's encoding and the specified encoding.
   */
  
  protected final String decode(String s, String encoding){
    return TextUtilities.convert(s, getConn().getTextEncoding(), encoding);
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
    listenerManager.addGameListener(gameListener);
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
    listenerManager.removeGameListener(gameListener);
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
    
    boolean handled = false;
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      ConsoleDesignation designation = console.getDesignation();
      if (designation != systemConsoleDesignation)
        handled |= console.getDesignation().receive(evt);
    }
    
    
    // We open a new personal chat console only if the event wasn't already handled. 
    if (!handled && (evt instanceof ChatEvent) && 
        ((((ChatEvent)evt).getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY) && 
        getPrefs().getBool("newConsoleOnPersonalTell", false))){
      ChatEvent chatEvent = (ChatEvent)evt;
      ConsoleDesignation designation = addPersonalChatConsole(chatEvent.getSender(), false);
      handled |= designation.receive(evt);
    }
    
    
    // We treat the system console specially because we want it to only receive
    // events which weren't handled by any of the other consoles.
    if (!handled && (systemConsoleDesignation != null))
      systemConsoleDesignation.receive(evt);
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
   * Returns the default text to be added to a console when receiving the
   * specified chat event (decoded to the specified encoding).
   */

  protected String getDefaultTextForChat(ChatEvent evt, String encoding){
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
    
    String message = getI18n().getString("loggedInMessage");
    for (int i = 0; i < consoles.size(); i++){
      Console console = (Console)consoles.get(i);
      console.addToOutput(message, "info");
    }
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
   * Invoked when a new game has started.
   */
  
  protected void gameStarted(GameEvent evt){
    if (getPrefs().getBool("newConsoleOnGameStart", false)){
      // Ideally, we want to select and make active iff the game was started due
      // to a direct user action, but we don't know it, and the only case where
      // it's not true is the examine-on-login game, so we approximate this way
      boolean makeSelectedAndActive = !(
          (evt.getGame().getGameType() == Game.MY_GAME) &&
          !evt.getGame().isPlayed());
          
      addConsole(createGameConsoleDesignation(evt.getGame()), makeSelectedAndActive);
      if (makeSelectedAndActive)
        makeActive();
    }
  }
  
  
  
  /**
   * Returns the channels/rooms available on the server we're logged on to
   * (a map from channel IDs to {@link Channel} objects). The order of the map
   * is the order in which the channels should be normally displayed to the
   * user. 
   */
  
  public SortedMap getChannels(){
    if (channels == null)
      channels = createChannels();
    
    return Collections.unmodifiableSortedMap(channels);
  }
  
  
  
  /**
   * Returns the set of channels/rooms for the server we're connecting to (a map
   * from channel IDs to {@link Channel} objects).
   * This method is meant to be implemented by server-specific classes.
   */
  
  protected abstract SortedMap createChannels();
  
  
  
  /**
   * Encodes the specified list of channels into a preference property value.
   */
  
  public abstract Object encodeConsoleChannelsPref(List channels);
  
  
  
  /**
   * Parses the value of the preference specifying the list of channels a
   * console displays.
   */
  
  public abstract List parseConsoleChannelsPref(Object channelsPrefsValue);
  
  
  
  /**
   * Saves the current state into the user file.
   */

  public void saveState(){
    Preferences prefs = getPrefs();
    
    prefs.setInt("consoles.selected", consolesTabbedPane.getModel().getSelectedIndex());
    prefs.setString("encoding", getEncoding());
  }



  /**
   * Returns the ID of this plugin. See also {@linkplain #PLUGIN_ID}.
   */

  public String getId(){
    return PLUGIN_ID;
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

  public abstract PreferencesPanel getPreferencesUI();
  
  
  
  /**
   * An action which guides the user to ask a question using the help console.
   */
  
  private class AskHelpQuestionAction extends JinAction{
    
    
    
    /**
     * Returns the string <code>"askhelpquestion"</code>.
     */
    
    public String getId(){
      return "askhelpquestion";
    }
    
    
    
    /**
     * Displays the help console and flashes the input field.
     */
    
    public void actionPerformed(ActionEvent e){
      activateHelpConsole();
    }
    
    
    
  }
  
  
  
}
