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

import free.jin.*;
import free.jin.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import free.jin.plugin.Plugin;
import free.jin.plugin.PreferencesPanel;
import free.jin.plugin.PluginUIContainer;
import java.net.URL;
import java.util.Vector;


/**
 * A Plugin which implements the consoles functionality. It's responsible for
 * opening, positioning and closing the various consoles used by the user.
 */

public class ConsoleManager extends Plugin implements PlainTextListener, ChatListener, ConnectionListener, GameListListener{


  
  /**
   * The main console.
   */

  protected Console console;



  /**
   * The container in which the console sits.
   */

  protected PluginUIContainer consoleContainer;



  /**
   * The current game list table display style.
   */

  protected String gameListDisplayStyle;
  
  
  
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
    gameListDisplayStyle = getPrefs().getString("game-list-display-style", "embedded");
  }



  /**
   * Creates and opens the console.
   */

  private void openConsole(){
    console = createConsole();

    consoleContainer = createContainer("");
    consoleContainer.setTitle("Main Console");

    URL iconImageURL = ConsoleManager.class.getResource("icon.gif");
    if (iconImageURL != null)
      consoleContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    consoleContainer.setCloseOperation(PluginUIContainer.CLOSE_SESSION_ON_CLOSE);

    Container content = consoleContainer.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(console, BorderLayout.CENTER);

    consoleContainer.setVisible(true);
  }



  /**
   * Creates the Console for this ConsoleManager.
   */

  protected Console createConsole(){
    return new Console(getConn(), getPrefs());
  }



  /**
   * Creates and returns the JMenu for this plugin.
   */

  public JMenu getPluginMenu(){
    JMenu myMenu = new JMenu("Console");

    if (getConn() instanceof GameListConnection){
      JMenu gameListDisplayStyleMenu = new JMenu("Game lists display style");
      gameListDisplayStyleMenu.setMnemonic('g');

      JCheckBoxMenuItem embeddedCB = new JCheckBoxMenuItem("Embedded", gameListDisplayStyle.equalsIgnoreCase("embedded"));
      JCheckBoxMenuItem framedCB = new JCheckBoxMenuItem("Framed", gameListDisplayStyle.equalsIgnoreCase("framed"));
      JCheckBoxMenuItem noneCB = new JCheckBoxMenuItem("None", gameListDisplayStyle.equalsIgnoreCase("none"));
      embeddedCB.setActionCommand("embedded");
      framedCB.setActionCommand("framed");
      noneCB.setActionCommand("none");
      embeddedCB.setMnemonic('E');
      framedCB.setMnemonic('F');
      noneCB.setMnemonic('N');
      ButtonGroup gameListDisplayStyleCBGroup = new ButtonGroup();
      gameListDisplayStyleCBGroup.add(embeddedCB);
      gameListDisplayStyleCBGroup.add(framedCB);
      gameListDisplayStyleCBGroup.add(noneCB);

      ActionListener gameListDisplayStyleListener = new ActionListener(){
        
        public void actionPerformed(ActionEvent evt){
          String actionCommand = evt.getActionCommand();
          if (actionCommand.equals(gameListDisplayStyle))
            return;
          String oldStyle = gameListDisplayStyle;
          gameListDisplayStyle = actionCommand;

          GameListListenerManager listenerManager = ((GameListConnection)getConn()).getGameListListenerManager();

          if (gameListDisplayStyle.equals("none"))
            listenerManager.removeGameListListener(ConsoleManager.this);
          else if (oldStyle.equalsIgnoreCase("none"))
            listenerManager.addGameListListener(ConsoleManager.this);
        }
      };

      embeddedCB.addActionListener(gameListDisplayStyleListener);
      framedCB.addActionListener(gameListDisplayStyleListener);
      noneCB.addActionListener(gameListDisplayStyleListener);

      gameListDisplayStyleMenu.add(embeddedCB);
      gameListDisplayStyleMenu.add(framedCB);
      gameListDisplayStyleMenu.add(noneCB);

      myMenu.add(gameListDisplayStyleMenu);
    }

    JMenuItem clearMenuItem = new JMenuItem("Clear Console");
    clearMenuItem.setMnemonic('l');
    clearMenuItem.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    clearMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        console.clear();
      }

    });
    myMenu.add(clearMenuItem);


    JCheckBoxMenuItem copyOnSelectCB 
      = new JCheckBoxMenuItem("Copy on Select", getPrefs().getBool("copyOnSelect", true));
    copyOnSelectCB.setMnemonic('C');
    copyOnSelectCB.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        JCheckBoxMenuItem source = (JCheckBoxMenuItem)evt.getSource();
        getPrefs().setBool("copyOnSelect", source.isSelected());
      }
  
    });
    myMenu.add(copyOnSelectCB);

    return myMenu;
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

    if ((conn instanceof GameListConnection) && !gameListDisplayStyle.equalsIgnoreCase("none"))
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

    if ((conn instanceof GameListConnection) && !gameListDisplayStyle.equalsIgnoreCase("none"))
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
      
    console.addToOutput(evt.getText(), "plain");
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

  public void connectionAttempted(ConnectionEvent evt){
    console.addToOutput("Trying to connect to " + evt.getHost() + 
                        " on port " + evt.getPort() + "...", "info");
  }



  /**
   * Gets called when the connection to the server is established.
   */

  public void connectionEstablished(ConnectionEvent evt){
    console.addToOutput("Connected", "info");  
  }



  /**
   * Gets called when the login procedure is done.
   */

  public void connectionLoggedIn(ConnectionEvent evt){
    consoleContainer.setTitle("Main Console - " + getConn().getUsername() +
      " on " + getServer().getLongName());
  }



  /**
   * Gets called when the connection to the server is lost.
   */

  public void connectionLost(ConnectionEvent evt){
    console.addToOutput("WARNING: Disconnected", "info");
  }




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
    
    

    String title = "  "+evt.getListTitle()+".  Displaying items "+evt.getFirstIndex()+"-"+evt.getLastIndex()+" out of "+evt.getItemCount()+"  ";

    // Otherwise, the table header is not created on time for the layout to take account of it
    // and size the scrollpane properly.
    // See bug https://sourceforge.net/tracker/index.php?func=detail&aid=602496&group_id=50386&atid=459537
    scrollPane.setColumnHeaderView(table.getTableHeader());

    if (gameListDisplayStyle.equals("embedded")){
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
      PluginUIContainer container = createContainer(null);
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
    if (getConn() instanceof GameListConnection)
      getPrefs().setString("game-list-display-style", gameListDisplayStyle);
  }



  /**
   * Returns the string <code>"console"</code>. The scripter plugin has this
   * hardcoded.
   */

  public String getId(){
    return "console";
  }



  /**
   * Returns the string "Main Console".
   */

  public String getName(){
    return "Main Console";
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
    return new ConsolePreferencesPanel(this);
  }



}
