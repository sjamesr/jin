/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

import free.jin.Connection;
import free.jin.GameListConnection;
import free.jin.I18n;
import free.jin.console.prefs.ConsolePrefsPanel;
import free.jin.event.*;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.PreferencesPanel;
import free.jin.ui.UIProvider;


/**
 * A Plugin which implements the consoles functionality. It's responsible for
 * opening, positioning and closing the various consoles used by the user.
 */

public class ConsoleManager extends Plugin implements PlainTextListener, ChatListener, ConnectionListener, GameListListener{
  
  
  
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
   * The main console.
   */

  protected Console console;



  /**
   * The container in which the console sits.
   */

  protected PluginUIContainer consoleContainer;



  /**
   * The current game lists display style.
   */

  private int gameListsDisplayStyle;
  
  
  
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
    initState();
    openConsole();
    registerConnListeners();
  }



  /**
   * Stops the plugin.
   */

  public void stop(){
    unregisterConnListeners();
  }



  /**
   * Sets all the variables representing the current settings from the parameters.
   */

  protected void initState(){
    String gameListsDisplayStyleString = getPrefs().getString("game-list-display-style", "embedded");
    if ("embedded".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(EMBEDDED_GAME_LISTS);
    else if ("framed".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(EXTERNAL_GAME_LISTS);
    else if ("none".equals(gameListsDisplayStyleString))
      setGameListsDisplayStyle(NO_GAME_LISTS);
  }



  /**
   * Creates and opens the console.
   */

  private void openConsole(){
    console = createConsole();

    consoleContainer = createContainer("", UIProvider.ESSENTIAL_CONTAINER_MODE);
    consoleContainer.setTitle(getI18n().getString("mainConsole.initialTitle"));

    URL iconImageURL = ConsoleManager.class.getResource("icon.gif");
    if (iconImageURL != null)
      consoleContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));
    
    Container content = consoleContainer.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(console, BorderLayout.CENTER);
    
    consoleContainer.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIActivated(PluginUIEvent evt){
        console.requestDefaultFocus();
      }
    });

    consoleContainer.setVisible(true);
  }



  /**
   * Creates the Console for this ConsoleManager.
   */

  protected Console createConsole(){
    return new Console(this);
  }
  
  
  
  /**
   * Sets the current game lists display style to the specified value. Possible
   * values are <code>EMBEDDED_GAME_LISTS</code>,
   * <code>EXTERNAL_GAME_LISTS</code> and <code>NO_GAME_LISTS</code>. 
   */
  
  public void setGameListsDisplayStyle(int style){
    switch (style){
      case EMBEDDED_GAME_LISTS:
      case EXTERNAL_GAME_LISTS:
      case NO_GAME_LISTS:
        break;
      default:
        throw new IllegalArgumentException("Bad game lists display style value: " + style);
    }

    if (getConn() instanceof GameListConnection){
      GameListListenerManager listenerManager = ((GameListConnection)getConn()).getGameListListenerManager();
  
      if (style == NO_GAME_LISTS)
        listenerManager.removeGameListListener(ConsoleManager.this);
      else if (gameListsDisplayStyle == NO_GAME_LISTS)
        listenerManager.addGameListListener(ConsoleManager.this);
    }
    
    this.gameListsDisplayStyle = style;
  }
  
  
  
  /**
   * Returns the current game lists display style.
   */
  
  public int getGameListsDisplayStyle(){
    return gameListsDisplayStyle;
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
   * Rereads the plugin/user properties and changes the assosiated console
   * manager's settings accordingly. This method should be called when the user
   * changes the preferences.
   */

  public void refreshFromProperties(){
    console.refreshFromProperties();
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

    if ((conn instanceof GameListConnection) && (getGameListsDisplayStyle() != NO_GAME_LISTS))
      ((GameListConnection)conn).getGameListListenerManager().addGameListListener(this);
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

    if ((conn instanceof GameListConnection) && (getGameListsDisplayStyle() != NO_GAME_LISTS))
      ((GameListConnection)conn).getGameListListenerManager().removeGameListListener(this);
  }



  /**
   * Adds the specified line of text to the console.
   */

  public void addSpecialLine(String line){
    console.addToOutput(line, "special");
  }


  /**
   * Listens to plain text and adds it to the console.
   */

  public void plainTextReceived(PlainTextEvent evt){
    if (isPaused()){
      pausedEventsQueue.addElement(evt);
      return;
    }
      
    console.addToOutput(decode(evt.getText()), "plain");
  }



  /**
   * Listens to ChatEvents and adds appropriate text to the console.
   */

  public void chatMessageReceived(ChatEvent evt){
    if (isPaused()){
      pausedEventsQueue.addElement(evt);
      return;
    }
    
    String type = evt.getType();
    Object forum = evt.getForum();
    String sender = evt.getSender();
    String chatMessageType = type + "." + (forum == null ? "" : forum.toString()) + "." + sender;

    console.addToOutput(translateChat(evt), chatMessageType);
  }
  
  
  
  /**
   * Sends the specified user-inputted command to the server.
   */
  
  public void sendUserCommand(String command){
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
    console.addToOutput(message, "info");
  }



  /**
   * Gets called when the connection to the server is established.
   */

  public void connectionEstablished(Connection conn){
    console.addToOutput(getI18n().getString("connectedMessage"), "info");  
  }



  /**
   * Gets called when the login procedure is done.
   */

  public void loginSucceeded(Connection conn){
    String title = getI18n().getFormattedString("mainConsole.title",
      new Object[]{getConn().getUsername(), getServer().getLongName()});
    consoleContainer.setTitle(title);
  }



  /**
   * Gets called when the connection to the server is lost.
   */

  public void connectionLost(Connection conn){
    console.addToOutput(getI18n().getString("disconnectedWarning"), "info");
  }
  
  
  
  // The rest of ConnectionListener's methods.
  public void connectingFailed(Connection conn, String reason){}
  public void loginFailed(Connection conn, String reason){}



  /**
   * Creates a table to display a game list item for the given GameListEvent.
   */

  protected JTable createGameListTable(GameListEvent evt){
    return new GameListTable(console, evt);
  }

  


  /**
   * Gets called when a game list arrives from the server.
   * Adds a JTable displaying the list to the console.
   */

  public void gameListArrived(GameListEvent evt){
    final JTable table = createGameListTable(evt);
    JTableHeader header = table.getTableHeader();
    Dimension originalPrefSize = header.getPreferredSize();
    // This abomination is needed because Metal L&F has a too small preferred label height on 1.1
    header.setPreferredSize(new Dimension(originalPrefSize.width, Math.max(originalPrefSize.height, 18)));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // The following block sets the preferred sizes of the columns to the maximum
    // preferred width of the cells in that column.
    TableColumnModel columnModel = table.getColumnModel();
    TableModel model = table.getModel();
    for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++){
      TableColumn column = columnModel.getColumn(columnIndex);
      Component headerRendererComponent = column.getHeaderRenderer().getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, columnIndex);
      int maxWidth = headerRendererComponent.getPreferredSize().width;
      for (int rowIndex=0;rowIndex<model.getRowCount();rowIndex++){
        TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex, columnIndex);
        Object value = model.getValueAt(rowIndex, columnIndex);
        Component rendererComponent = cellRenderer.getTableCellRendererComponent(table, value, false, false, rowIndex, columnIndex);
        int cellWidth = rendererComponent.getPreferredSize().width;
        if (cellWidth>maxWidth)
          maxWidth = cellWidth;
      }
      if (maxWidth>150) // This is probably the "note" column, which is very wide but we don't want it to take all the space
        column.setPreferredWidth(50);
      else
        column.setPreferredWidth(maxWidth);
    }
    
    
    String title = getI18n().getFormattedString("gameListTitle",
      new Object[]{
        evt.getListTitle(),
        new Integer(evt.getFirstIndex()),
        new Integer(evt.getLastIndex()),
        new Integer(evt.getItemCount())
      });
    title = "  " + title + "  ";

    // Otherwise, the table header is not created on time for the layout to take account of it
    // and size the scrollpane properly.
    // See bug https://sourceforge.net/tracker/index.php?func=detail&aid=602496&group_id=50386&atid=459537
    scrollPane.setColumnHeaderView(table.getTableHeader());

    if (getGameListsDisplayStyle() == EMBEDDED_GAME_LISTS){
      scrollPane.setBorder(new TitledBorder(title));
      int maxHeight = (console.getOutputArea().height - 40) * 2/3;
      if (scrollPane.getPreferredSize().height > maxHeight)
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, maxHeight));

      // This is stupid, but fixes the bug in the embedded case case described at
      // https://sourceforge.net/tracker/index.php?func=detail&aid=602496&group_id=50386&atid=459537 for JDK1.4
      scrollPane.setPreferredSize(scrollPane.getPreferredSize());

      console.addToOutput(scrollPane);
    }
    else{
      PluginUIContainer container = createContainer(null, UIProvider.CLOSEABLE_CONTAINER_MODE);
      container.setTitle(title);

      Container content = container.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(scrollPane, BorderLayout.CENTER);

      container.setVisible(true);
    }
  }



  /**
   * Saves the current state into the user file.
   */

  public void saveState(){
    if (getConn() instanceof GameListConnection){
      String displayStyleString;
      switch (getGameListsDisplayStyle()){
        case EMBEDDED_GAME_LISTS: displayStyleString = "embedded"; break;
        case EXTERNAL_GAME_LISTS: displayStyleString = "framed"; break;
        case NO_GAME_LISTS: displayStyleString = "none"; break;
        default:
          throw new IllegalStateException("Bad gameListsDisplayStyle value");
      }
      getPrefs().setString("game-list-display-style", displayStyleString);
    }
  }



  /**
   * Returns the string <code>"console"</code>. The scripter plugin has this
   * hardcoded.
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
