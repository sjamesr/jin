/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import free.jin.plugin.*;
import free.util.*;
import free.util.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import free.workarounds.FixedJInternalFrame;
import free.util.PlatformUtils;


/**
 * An implementation of the <code>UIProvider</code> interface via a single
 * main container, a <code>JDesktopPane</code> and <code>JInternalFrames</code>
 * for ui containers.
 */

public class InternalFramesUIProvider implements UIProvider{



  /**
   * The context in which we are running.
   */

  private final JinContext context;



  /**
   * The main container.
   */

  private final TopLevelContainer mainContainer;



  /**
   * The desktop pane.
   */

  private final AdvancedJDesktopPane desktop;



  /**
   * The menubar.
   */

  private final JMenuBar menubar;



  /**
   * Maps Pair<Plugin, String> (the string being plugin container ids) to
   * the PluginContainers for that plugin and container id.
   */

  private final Hashtable pluginContainers = new Hashtable();


  
  /**
   * The connection menu.
   */

  private final ConnectionMenu connMenu;
  
  
  
  /**
   * The look and feel menu.
   */
   
  private final LookAndFeelMenu lnfMenu;



  /**
   * The preferences menu.
   */

  private final PreferencesMenu prefsMenu;



  /**
   * The help menu.
   */

  private final JMenu helpMenu;



  /**
   * The internal frame switcher that tracks the order of internal frames.
   */

  private final InternalFrameSwitcher frameSwitcher;


  
  /**
   * Creates a new <code>InternalFramesUIProvider</code> with the specified
   * context and main container.
   */

  public InternalFramesUIProvider(JinContext context, TopLevelContainer mainContainer){
    this.context = context;
    this.mainContainer = mainContainer;

    configureDesktop(desktop = new AdvancedJDesktopPane());
    configureMenubar(menubar = new JMenuBar());

    mainContainer.setContentPane(desktop);
    mainContainer.setMenuBar(menubar);

    menubar.add(connMenu = new ConnectionMenu());
    menubar.add(lnfMenu = new LookAndFeelMenu(mainContainer.getTopMostFrame()));
    menubar.add(prefsMenu = new PreferencesMenu());
    menubar.add(helpMenu = new HelpMenu(context));
    
    frameSwitcher = new InternalFrameSwitcher(desktop);
    desktop.setDesktopManager(new DesktopManager());
    
    // TODO: Find a way to implement ctrl+TAB without requiring security permissions.
    try{
      FocusManager.setCurrentManager(new FocusManager());
    } catch (SecurityException e){}
  }



  /**
   * Configures the desktop according to user preferences.
   */

  private void configureDesktop(AdvancedJDesktopPane desktop){
    Preferences prefs = context.getPrefs();

    Color bgColor = prefs.getColor("desktop.bgcolor", null);
    String wallpaper = prefs.getString("desktop.wallpaper.filename", null);
    String layout = prefs.getString("desktop.wallpaper.layout", "center");

    int layoutStyle;
    if ("scale".equals(layout))
      layoutStyle = AdvancedJDesktopPane.SCALE;
    else if ("tile".equals(layout))
      layoutStyle = AdvancedJDesktopPane.TILE;
    else
      layoutStyle = AdvancedJDesktopPane.CENTER;

    if (bgColor != null)
      desktop.setBackground(bgColor);
    desktop.setWallpaperLayoutStyle(layoutStyle);
    if (wallpaper != null)
      desktop.setWallpaper(desktop.getToolkit().getImage(wallpaper));

    desktop.putClientProperty("JDesktopPane.dragMode", "outline");
  }



  /**
   * Configures the menubar according to user preferences.
   */

  private void configureMenubar(JMenuBar menubar){
    
  }



  /**
   * Displays the specified <code>DialogPanel</code> via
   * {@link DialogPanel#show(JDialog)}.
   */

  public void showDialog(DialogPanel dialog, Component parent){
    Frame parentFrame = mainContainer.getTopMostFrame();
    if (parent != null)
      parentFrame = AWTUtilities.frameForComponent(parent);

    dialog.show(new JDialog(parentFrame), parent == null ? parentFrame : parent);
  }



  /**
   * Returns a new UIContainer for the specified plugin.
   */

  public PluginUIContainer createPluginUIContainer(Plugin plugin, String id){
    PluginContainer container = new PluginContainer(plugin, id);
    
    if (id != null){
      Object key = new Pair(plugin, id);
      Object oldContainer = pluginContainers.put(key, container);
      
      if (oldContainer != null){
        pluginContainers.put(key, oldContainer);
        throw new IllegalArgumentException("Cannot allocate a container with the same id twice");
      }
    }
    
    return container;
  }


  
  /**
   * This method is called by the <code>ConnectionManager</code> to notify the
   * UI provider that a session has been created/closed. The UI provider, in
   * response, should make any relevant changes to the UI.
   */

  public void setConnected(boolean isConnected, Session session){
    connMenu.setConnected(isConnected, session);
    prefsMenu.setConnected(isConnected, session);

    if (isConnected){
      addPluginMenus(session.getPlugins());
      loadSelectedFrame(session);
      
      mainContainer.setTitle(session.getUser().getUsername() + " at " + session.getServer().getShortName());
    }
    else{
      removePluginMenus();
      saveSelectedFrame(session);
      removePluginContainers();
      
      mainContainer.setTitle("");
    }

    // Bugfix - otherwise activating the menu via keyboard stops working.
    // On OS X, with native menubar, this actually breaks things.
    if (!isConnected && !PlatformUtils.isMacOSX())
      menubar.requestFocus();
  }



  /**
   * Adds the plugin menus for the specified plugin list.
   */

  private void addPluginMenus(Plugin [] plugins){
    menubar.removeAll();
    
    menubar.add(connMenu);
    menubar.add(lnfMenu);
    menubar.add(prefsMenu);
    
    for (int i = 0; i < plugins.length; i++){
      Plugin plugin = plugins[i];
      JMenu menu = plugin.createPluginMenu();
      if (menu != null)
        menubar.add(menu);
    }
    
    menubar.add(helpMenu);    
  }



  /**
   * Removes all the plugin menus.
   */

  private void removePluginMenus(){
    menubar.removeAll();
    
    menubar.add(connMenu);
    menubar.add(lnfMenu);
    menubar.add(prefsMenu);
    menubar.add(helpMenu);
  }



  /**
   * Sets the selected frame to the frame we remembered was selected last time.
   */

  private void loadSelectedFrame(Session session){
    User user = session.getUser();
    String pluginId = user.getPrefs().getString("iframe.selected.plugin", null);
    String containerId = user.getPrefs().getString("iframe.selected.id", null);

    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements()){
      PluginContainer c = (PluginContainer)e.nextElement();

      if (c.plugin.getId().equals(pluginId) && c.getId().equals(containerId)){
        if (c.isVisible())
          c.setActive(true);
        break;
      }
    }
  }



  /**
   * Sets a preference specifying which one was selected.
   */

  private void saveSelectedFrame(Session session){
    User user = session.getUser();

    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements()){
      PluginContainer c = (PluginContainer)e.nextElement();
      if (c.isActive()){
        Plugin plugin = c.plugin;

        user.getPrefs().setString("iframe.selected.plugin", plugin.getId());
        user.getPrefs().setString("iframe.selected.id", c.getId());
        break;
      }
    }
  }




  /**
   * Disposes of all the plugin containers and removes them from the
   * <code>pluginContainers</code> hashtable.
   */
  
  private void removePluginContainers(){
    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements()){
      PluginContainer c = (PluginContainer)e.nextElement();
      c.dispose();
    }

    pluginContainers.clear();
  }
  

  
  /**
   * The connection menu.
   */

  private class ConnectionMenu extends JMenu implements ActionListener{



    /**
     * The maximum accounts on the recently used accounts list.
     */

    private static final int MAX_RECENT_LIST = 5;



    /**
     * The "New Connection..." menu item.
     */

    private final JMenuItem newConnection;



    /**
     * The "Close Connection" menu item.
     */

    private final JMenuItem closeConnection;



    /**
     * The "Exit" menu item.
     */

    private final JMenuItem exit;



    /**
     * A vector holding the history of recently used accounts, in descending
     * order (last used first).
     */

    private final Vector recentAccounts;



    /**
     * The index of the separator following the close connection menu item.
     */

    private final int separatorIndex;



    /**
     * Creates a new <code>ConnectionMenu</code>.
     */

    public ConnectionMenu(){
      super("Connection");
      setMnemonic('C');

      add(newConnection = new JMenuItem("New Connection...", 'N'));
      // If you use 'c' as the mnemonic here, alt+c won't work for the menu,
      // see http://developer.java.sun.com/developer/bugParade/bugs/4213634.html
      add(closeConnection = new JMenuItem("Close Connection", 'l'));
      separatorIndex = getItemCount();

      addSeparator();
      add(exit = new JMenuItem("Exit", 'x'));
      
      exit.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      newConnection.addActionListener(this);
      closeConnection.addActionListener(this);
      exit.addActionListener(this);

      recentAccounts = loadRecentAccounts();
      updateRecentAccountsMenuItems();

      setConnected(false, null);
    }



    /**
     * This method is called to notify the menu of the current state of the
     * connection - whether we are currently connected to the server or not.
     */

    public void setConnected(boolean connected, Session session){
      newConnection.setEnabled(!connected);
      closeConnection.setEnabled(connected);

      if (!connected && (session != null)){
        User user = session.getUser();
        if (JinUtilities.isKnownUser(context, user) && !user.isGuest()){
          recentAccounts.removeElement(user);
          recentAccounts.insertElementAt(user, 0);
          updateRecentAccountsMenuItems();
          saveRecentAccounts(recentAccounts);
        }
      }

      setRecentAccountsMenuItemsEnabled(!connected);
    }



    /**
     * <code>ActionListener</code> implementation. Listens to activation of the
     * various menu items and performs the desired operation.
     */

    public void actionPerformed(ActionEvent evt){
      ConnectionManager connManager = context.getConnManager();
      Object source = evt.getSource();
      if (source == newConnection){
        connManager.showNewConnectionUI();
      }
      else if (source == closeConnection){
        Object result = OptionPanel.OK;
        Session session = context.getConnManager().getSession();
        if ((session != null) && session.isConnected()){
          result = OptionPanel.confirm(InternalFramesUIProvider.this, "Close Session?",
            "Disconnect from the server and close the session?", OptionPanel.OK);
        }

        if (result == OptionPanel.OK)
          connManager.closeSession();
      }
      else if (source == exit){
        context.quit(true);
      }
      else{ // One of the recent account menu items
        int index = Utilities.indexOf(getMenuComponents(), source);
        User user = (User)recentAccounts.elementAt(index - separatorIndex - 1);
        connManager.showLoginInformationUI(user.getServer(), user.getPreferredConnDetails());
      }
    }



    /**
     * Brings the recent history menu item list up-to-date.
     */

    private void updateRecentAccountsMenuItems(){
      // Remove all existing history menu items
      while (getItem(separatorIndex + 1) != exit)
        remove(separatorIndex + 1);

      // Add them again
      for (int i = 1; i <= recentAccounts.size(); i++){
        User user = (User)recentAccounts.elementAt(i - 1);
        String label = i + " " + user.getUsername() + " at " + user.getServer().getShortName();
        JMenuItem menuItem = new JMenuItem(label);
        if (i <= 8)
          menuItem.setMnemonic(Character.forDigit(i, 10));
        menuItem.addActionListener(this);

        insert(menuItem, separatorIndex + i);
      }

      if (recentAccounts.size() != 0)
        insertSeparator(separatorIndex + recentAccounts.size() + 1);
    }



    /**
     * Enables or disables the recent accounts menu items.
     */

    private void setRecentAccountsMenuItemsEnabled(boolean isEnabled){
      for (int i = 1; i <= recentAccounts.size(); i++)
        getItem(separatorIndex + i).setEnabled(isEnabled);
    }



    /**
     * Loads the recently used accounts list into a vector and returns it.
     */

    private Vector loadRecentAccounts(){
      Vector accounts = new Vector(MAX_RECENT_LIST);
      Preferences prefs = context.getPrefs();

      int count = prefs.getInt("accounts.recent.count", 0);
      for (int i = 0; i < count; i++){
        String username = prefs.getString("accounts.recent." + i + ".username");
        String serverId = prefs.getString("accounts.recent." + i + ".serverId");

        Server server = JinUtilities.getServerById(context, serverId);
        if (server == null)
          continue;

        User user = JinUtilities.getUser(context, server, username);
        if (user == null)
          continue;

        accounts.addElement(user);
        if (accounts.size() == MAX_RECENT_LIST)
          break;
      }

      return accounts;
    }



    /**
     * Saves the recent account list into user preferences.
     */

    private void saveRecentAccounts(Vector accounts){
      Preferences prefs = context.getPrefs();

      int count = accounts.size();
      prefs.setInt("accounts.recent.count", count);
      
      for (int i = 0; i < count; i++){
        User user = (User)accounts.elementAt(i);

        String username = user.getUsername();
        String serverId = user.getServer().getId();

        prefs.setString("accounts.recent." + i + ".username", username);
        prefs.setString("accounts.recent." + i + ".serverId", serverId);
      }
    }



  }


  
  /**
   * The preferences menu.
   */

  private class PreferencesMenu extends JMenu implements ActionListener{



    /**
     * The "Background" menu item.
     */

    private final JMenuItem bgMenu;



    /**
     * Are we currently in the "connected" state?
     */

    private boolean isConnected = false;



    /**
     * The index of the separator, -1 when none.
     */

    private int separatorIndex = -1;



    /**
     * The plugins in the current session, <code>null</code> when none.
     */

    private Plugin [] plugins = null;



    /**
     * Creates a new <code>PreferencesMenu</code>.
     */

    public PreferencesMenu(){
      super("Preferences");
      setMnemonic('P');

      add(bgMenu = new JMenuItem("Background", 'B'));

      bgMenu.addActionListener(this);
    }



    /**
     * Modifies the state of the menu to match the specified state.
     */

    public void setConnected(boolean isConnected, Session session){
      if (this.isConnected == isConnected)
        return;

      this.isConnected = isConnected;

      this.plugins = session.getPlugins();

      if (isConnected)
        addPluginPreferenceMenuItems();
      else
        removePluginPreferenceMenuItems();
    }



    /**
     * Adds the menu items for opening preference dialogs for the specified list
     * of plugins.
     */

    private void addPluginPreferenceMenuItems(){
      separatorIndex = getItemCount();
      addSeparator();

      for (int i = 0; i < plugins.length; i++){
        Plugin plugin = plugins[i];
        if (!plugin.hasPreferencesUI())
          continue;

        JMenuItem menuItem = new JMenuItem(plugins[i].getName());
        menuItem.setActionCommand(String.valueOf(i));
        menuItem.addActionListener(this);
        add(menuItem);
      }
    }



    /**
     * Removes the menu items associated with displaying the preference panels
     * of the various plugins.
     */

    private void removePluginPreferenceMenuItems(){
      while (separatorIndex < getItemCount())
        remove(separatorIndex);

      separatorIndex = -1;
    }



    /**
     * <code>ActionListener</code> implementation. Listens to activation of the
     * various menu items and performs the desired operation.
     */

    public void actionPerformed(ActionEvent evt){
      Object source = evt.getSource();
      String actionCommand = evt.getActionCommand();

      if (source == bgMenu)
        showBGDialog();
      else{
        int pluginIndex = Integer.parseInt(actionCommand);
        Plugin plugin = plugins[pluginIndex];
        JDialog dialog = new PrefsDialog(mainContainer.getTopMostFrame(), plugin);
        AWTUtilities.centerWindow(dialog, mainContainer.getTopMostFrame());
        dialog.setVisible(true);
      }
    }



    /**
     * Displays the background preferences dialog.
     */

    private void showBGDialog(){
      Preferences prefs = context.getPrefs();
      String wallpaperFilename = prefs.getString("desktop.wallpaper.filename", null);
      File currentImageFile = wallpaperFilename == null ? null : new File(wallpaperFilename);
      BackgroundChooser bChooser = new BackgroundChooser(mainContainer.getTopMostFrame(), desktop,
        null, AdvancedJDesktopPane.CENTER, currentImageFile);
      bChooser.setVisible(true);

      Color chosenColor = bChooser.getChosenColor();
      File chosenImageFile = bChooser.getChosenImageFile();
      int chosenLayoutStyle = bChooser.getChosenImageLayoutStyle();

      prefs.setColor("desktop.bgcolor", chosenColor);
      prefs.setString("desktop.wallpaper.filename", chosenImageFile == null ? null : chosenImageFile.getAbsolutePath());

      String layoutStyle;
      switch (chosenLayoutStyle){
        case AdvancedJDesktopPane.CENTER: layoutStyle = "center"; break;
        case AdvancedJDesktopPane.TILE: layoutStyle = "tile"; break;
        case AdvancedJDesktopPane.SCALE: layoutStyle = "scale"; break;
        default: layoutStyle = null; break;
      }
      prefs.setString("desktop.wallpaper.layout", layoutStyle);
    }



  }



  /**
   * The dialog displaying the preferences panel for a specified plugin.
   */

  private class PrefsDialog extends JDialog implements ChangeListener, ActionListener{



    /**
     * The preferences panel.
     */

    private final PreferencesPanel prefsPanel;



    /**
     * The ok button.
     */

    private final JButton okButton;



    /**
     * The apply button.
     */

    private final JButton applyButton;



    /**
     * The cancel button.
     */

    private final JButton cancelButton;



    /**
     * Creates a new <code>PrefsDialog</code> with the specified parent frame
     * and for the specified plugin.
     */

    public PrefsDialog(Frame parent, Plugin plugin){
      super(parent, plugin.getName() + " Preferences", true);

      this.prefsPanel = plugin.getPreferencesUI();
      this.applyButton = new JButton("Apply");
      this.okButton = new JButton("OK");
      this.cancelButton = new JButton("Cancel");

      createUI();

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      SwingUtils.registerEscapeCloser(this);

      okButton.addActionListener(this);
      applyButton.addActionListener(this);
      cancelButton.addActionListener(new WindowDisposingListener(this));
      prefsPanel.addChangeListener(this);
    }



    /**
     * Creates the UI.
     */

    private void createUI(){
      Container content = getContentPane();
      content.setLayout(new BorderLayout());

      JPanel prefWrapperPanel = new JPanel(new BorderLayout());
      prefWrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      prefWrapperPanel.add(prefsPanel, BorderLayout.CENTER);
      content.add(prefWrapperPanel, BorderLayout.CENTER);

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      applyButton.setEnabled(false);
      applyButton.setMnemonic('A');

      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      buttonPanel.add(applyButton);
      bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

      content.add(bottomPanel, BorderLayout.SOUTH);
      getRootPane().setDefaultButton(okButton);
    }



    /**
     * ChangeListener implementation. Registered with the preferences panel,
     * enables the apply button when invoked.
     */

    public void stateChanged(ChangeEvent evt){
      applyButton.setEnabled(true);
    }



    /**
     * ActionListener implementation. Registered with the ok and apply buttons.
     */

    public void actionPerformed(ActionEvent evt){
      try{
        if (applyButton.isEnabled())
          prefsPanel.applyChanges();
        applyButton.setEnabled(false);

        if (evt.getSource() == okButton)
          dispose();
      } catch (BadChangesException e){
          OptionPanel.error(InternalFramesUIProvider.this, "Illegal Preferences",e.getMessage());
          if (e.getErrorComponent() != null)
            e.getErrorComponent().requestFocus();
        }
    }

  }



  /**
   * An implementation of a PluginUIContainer which uses a
   * <code>JInternalFrame</code>.
   */

  private class PluginContainer implements PluginUIContainer, VetoableChangeListener,
      InternalFrameListener{



    /**
     * The plugin this plugin container is for.
     */

    private final Plugin plugin;



    /**
     * The id of this plugin ui container.
     */

    private final String id;

    
    
    /**
     * The actual <code>JInternalFrame</code>.
     */

    private final JInternalFrame frame;



    /**
     * A list of PluginUIListeners.
     */

    private final free.util.EventListenerList listenerList = new free.util.EventListenerList();



    /**
     * The close operation of this plugin container.
     */

    private int closeOperation = HIDE_ON_CLOSE;



    /**
     * Creates a new <code>PluginContainer</code>.
     */

    public PluginContainer(Plugin plugin, String id){
      this.plugin = plugin;
      this.id = id;
      this.frame = new FixedJInternalFrame("", true, true, true, true);
      
      // See http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for the 
      // reason I do this instead of adding an InternalFrameListener like a sane person.
      frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
      frame.addVetoableChangeListener(this);
      frame.addInternalFrameListener(this);

      frame.setVisible(false); // internal frames are initially visible in 1.1
    }



    /**
     * Returns the id of this <code>PluginUIContainer</code>.
     */

    private String getId(){
      return id;
    }


  
    /**
     * Returns the desired initial proportions for the frame of this plugin
     * container.
     */
  
    private RectDouble getInitialBounds(){
      String pluginId = plugin.getId();
      
      if ("console".equals(pluginId)){
        if ("".equals(id))
          return new RectDouble(0, 0, 0.75, 0.75);
      }
      else if ("seek".equals(pluginId)){
        if ("".equals(id))
          return new RectDouble(0.5, 0.5, 0.5, 0.5);
      }
      else if ("board".equals(pluginId)){
        try{
          int boardIndex = Integer.parseInt(id);
          return new RectDouble(0.25, 0.05*(boardIndex%6), 0.75, 0.75);
        } catch (NumberFormatException e){}
      }
  
      int margin = 50;
      Dimension prefSize = frame.getPreferredSize();
      Dimension contentPaneSize = mainContainer.getContentPane().getSize();
      double w = Math.min(prefSize.width, contentPaneSize.width - margin);
      double h = Math.min(prefSize.height, contentPaneSize.height - margin);
      double x = (contentPaneSize.width - margin - w)/2;
      double y = (contentPaneSize.height - margin - h)/2;
      
      return new RectDouble(x / contentPaneSize.width, y / contentPaneSize.height,
                            w / contentPaneSize.width, h / contentPaneSize.height);
    }
  
  
  
    /**
     * Disposes of this plugin container.
     */

    private void dispose(){
      setVisible(false);
      frame.dispose();
    }



    /**
     * Either brings the frame to front and makes it selected or moves it to
     * back and selects a different frame.
     */

    public void setActive(boolean active){
      try{
        if (active){
          if (!isVisible())
            setVisible(true);
          if (frame.isIcon())
            frame.setIcon(false);
          frame.setSelected(true);
          frame.toFront();
        }
        else{
          frameSwitcher.selectPrevious();
        }
      } catch (PropertyVetoException e){}
    }



    /**
     * Returns whether the frame of this plugin container is currently selected.
     */

    public boolean isActive(){
      return frame.isVisible() && frame.isSelected();
    }



    /**
     * Adds a <code>PluginUIListener</code>.
     */

    public void addPluginUIListener(PluginUIListener listener){
      listenerList.add(PluginUIListener.class, listener);
    }



    /**
     * Removes a <code>PluginUIListener</code>.
     */

    public void removePluginUIListener(PluginUIListener listener){
      listenerList.remove(PluginUIListener.class, listener);
    }


    
    /**
     * Fires the specified PluginUIEvent.
     */

    private void firePluginUIEvent(PluginUIEvent evt){
      PluginUIListener [] listeners =
        (PluginUIListener [])listenerList.getListeners(PluginUIListener.class);
      for (int i = 0; i < listeners.length; i++){
        PluginUIListener listener = listeners[i];
        evt.dispatch(listener);
      }
    }



    /**
     * Returns the content pane of the frame.
     */

    public Container getContentPane(){
      return frame.getContentPane();
    }



    /**
     * Sets the title of the frame.
     */

    public void setTitle(String title){
      frame.setTitle(title);
      frame.repaint(); // Bugfix: the title bar doesn't repaint itself in MS VM
    }



    /**
     * Sets the icon of the frame.
     */

    public void setIcon(Image image){
      frame.setFrameIcon(new ImageIcon(image));
    }



    /**
     * Adds the frame to the desktop and makes it visible.
     */

    public void setVisible(boolean isVisible){
      if (isVisible == isVisible())
        return;

      if (isVisible)
        show();
      else
        hide();
    }



    /**
     * Returns whether the frame is currently visible.
     */

    public boolean isVisible(){
      return (frame.getParent() != null) ||
        ((frame.getDesktopIcon() != null) && (frame.getDesktopIcon().getParent() != null));
    }



    /**
     * Sets this plugin container's close operation to the specified value.
     */

    public void setCloseOperation(int val){
      switch (val){
        case HIDE_ON_CLOSE:
        case CLOSE_SESSION_ON_CLOSE:
        case DO_NOTHING_ON_CLOSE:
          break;
        default:
          throw new IllegalArgumentException("Bad close operation value: " + val);
      }

      this.closeOperation = val;
    }



    /**
     * Shows the frame.
     */

    private void show(){
      loadProps();

      desktop.add(frame);
      frame.setVisible(true);
      
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_SHOWN));
    }



    /**
     * Hides the frame.
     */

    private void hide(){
      saveProps();

      if (frame.isSelected())
        frameSwitcher.selectPrevious();
      
      frame.setVisible(false);
      desktop.remove(frame);

      // Workaround: the desktop icon is not removed by itself (at least in MS VM).
      JComponent desktopIcon = frame.getDesktopIcon();
      if ((desktopIcon != null) && (desktopIcon.getParent() != null)){
        desktop.remove(desktopIcon);
        desktop.repaint();
      }
      // end workaround

      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_HIDDEN));
    }



    /**
     * Saves the properties of this plugin container into user preferences.
     */

    private void saveProps(){
      if (id == null)
        return;

      Preferences prefs = plugin.getPrefs();
      String prefix = "".equals(id) ? "" : id + ".";

      if (frame.isMaximum() != prefs.getBool(prefix + "iframe.isMaximized", false))
        prefs.setBool(prefix + "iframe.isMaximized", frame.isMaximum());

      if (frame.isIcon() != prefs.getBool(prefix + "iframe.isIconified", false))
        prefs.setBool(prefix + "iframe.isIconified", frame.isIcon());

      // This is the only way to retrieve the "normal" bounds of the frame under
      // JDK1.2 and earlier. JDK1.3 has a getNormalBounds() method.
      try{
        if (frame.isMaximum())
          frame.setMaximum(false);
      } catch (java.beans.PropertyVetoException ex){}

      RectDouble bounds = new RectDouble(frame.getBounds());
      bounds.scale(1 / (double)desktop.getWidth(), 1 / (double)desktop.getHeight());
      prefs.setRectDouble(prefix + "iframe.bounds", bounds);
    }



    /**
     * Loads the saved properties of this plugin container and configures it
     * properly.
     */

    private void loadProps(){
      Preferences prefs = plugin.getPrefs();
      String prefix = "".equals(id) ? "" : id + ".";

      RectDouble defaultBounds = getInitialBounds();
      RectDouble bounds = (id == null ? defaultBounds :
        prefs.getRectDouble(prefix + "iframe.bounds", defaultBounds));
        
      // Fix the bounds in case they went bad for some reason.
      bounds.setX(Math.min(1, Math.max(0, bounds.getX())));
      bounds.setY(Math.min(1, Math.max(0, bounds.getY())));
      bounds.setWidth(Math.min(1, Math.max(0.02, bounds.getWidth())));
      bounds.setHeight(Math.min(1, Math.max(0.02, bounds.getHeight())));

      frame.setBounds(bounds.scale(desktop.getWidth(), desktop.getHeight()).toRect());

      try{
        frame.setMaximum(prefs.getBool(prefix + "iframe.isMaximized", false));
        frame.setIcon(prefs.getBool(prefix + "iframe.isIconified", false));
      } catch (java.beans.PropertyVetoException ex){}
    }



    /**
     * VetoableChangeListener implementation. See
     * http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for
     * the reason this is needed.
     */

    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
      if (pce.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY) &&
          pce.getOldValue().equals(Boolean.FALSE) && pce.getNewValue().equals(Boolean.TRUE)){

        switch (closeOperation){
          case HIDE_ON_CLOSE:
            setVisible(false);
            break;
          case CLOSE_SESSION_ON_CLOSE:
            Object result = OptionPanel.OK;
            Session session = context.getConnManager().getSession();
            if ((session != null) && session.isConnected()){
              result = OptionPanel.confirm(InternalFramesUIProvider.this, "Close Session?",
                "Close this window and disconnect?", OptionPanel.OK);
            }

            if (result == OptionPanel.OK)
              context.getConnManager().closeSession();
            break;
          case DO_NOTHING_ON_CLOSE:
            firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_CLOSING));
            break;
        }

        throw new PropertyVetoException("Canceled closing", pce);
      }
    }



    /**
     * InternalFrameListener implementation.
     */

    public void internalFrameActivated(InternalFrameEvent e){
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_ACTIVATED));
    }
    public void internalFrameDeactivated(InternalFrameEvent e){
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_DEACTIVATED));
    }
    
    public void internalFrameOpened(InternalFrameEvent e){}
    public void internalFrameClosed(InternalFrameEvent e){}
    public void internalFrameClosing(InternalFrameEvent e){}
    public void internalFrameDeiconified(InternalFrameEvent e){}
    public void internalFrameIconified(InternalFrameEvent e){}


  }



  /**
   * A custom focus manager we use to implement custom functionality and
   * functionality that Swing is supposed to have but doesn't (like ctrl+tab
   * switching between internal frames).
   */

  private class FocusManager extends DefaultFocusManager{

    

    /**
     * Overrides </code>DefaultFocusManager.processKeyEvent</code> to implement
     * window switching on ctrl+tab.
     */

    public void processKeyEvent(Component focusedComponent, KeyEvent evt){
      if (!SwingUtilities.isDescendingFrom(evt.getComponent(), mainContainer.getContentPane())){
        super.processKeyEvent(focusedComponent, evt);
        return;
      }

      int keyCode = evt.getKeyCode();
      if (((keyCode == KeyEvent.VK_TAB) && evt.isControlDown()) ||
          ((keyCode == KeyEvent.VK_F6) && evt.isControlDown()) ||
          ((keyCode == KeyEvent.VK_BACK_QUOTE) && evt.isControlDown())){
        evt.consume();
        if (evt.getID() == KeyEvent.KEY_RELEASED)
          return;

        if (evt.isShiftDown())
          frameSwitcher.selectPrevious();
        else
          frameSwitcher.selectNext();
      }
      else{
        super.processKeyEvent(focusedComponent, evt);
      }
    }

  }



  /**
   * An extension of the default desktop manager to add our own functionality.
   */

  private class DesktopManager extends DefaultDesktopManager{



    /**
     * The amount of pixels on the x axis of an internal frame that must always
     * be visible.
     */

    private final static int X_MARGIN = 100;
    // This must be big enough for the draggable area of the title bar to always
    // be visible.



    /**
     * The amount of pixels on the y axis of an internal frame that must always
     * be visible.
     */

    private final static int Y_MARGIN = 50; 
    // Must be at least the height of the title bar



    /**
     * This method makes sure the user doesn't do stupid things like moving the
     * internal frame out of reach.
     */

    public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight){
      Dimension desktopSize = desktop.getSize();
      Rectangle jifBounds = f.getBounds();

      // Determine which border is being dragged by checking which sides are
      // not at their original locations.

      boolean left = (jifBounds.x != newX);
      boolean top = (jifBounds.y != newY);
      boolean right = (newX + newWidth != jifBounds.x + jifBounds.width);
      boolean bottom = (newY + newHeight != jifBounds.y + jifBounds.height);

      int x1 = newX;
      int y1 = newY;
      int x2 = x1 + newWidth;
      int y2 = y1 + newHeight;

      // Don't impose margins bigger than the frame itself (if it's iconified, for example)
      int xMargin = jifBounds.width < X_MARGIN ? jifBounds.width : X_MARGIN;
      int yMargin = jifBounds.height < Y_MARGIN ? jifBounds.height : Y_MARGIN;

      // Adjust the appropriate sides
      if (right)
        x2 = Math.min(Math.max(x2, xMargin), x1 + desktopSize.width);
      if (bottom)
        y2 = Math.min(y2, y1 + desktopSize.height);
      if (left)
        x1 = Math.min(Math.max(x1, x2 - desktopSize.width), desktopSize.width - xMargin);
      if (top)
        y1 = Math.max(Math.min(Math.max(y1, 0), desktopSize.height - yMargin),
                      y2 - desktopSize.height);
      
      super.resizeFrame(f, x1, y1, x2 - x1, y2 - y1);
    }


    /**
     * This method makes sure the user doesn't do stupid things like moving the
     * internal frame out of reach.
     */

    public void dragFrame(JComponent f, int newX, int newY){
      Dimension desktopSize = desktop.getSize();
      Rectangle jifBounds = f.getBounds();

      // Don't impose margins bigger than the frame itself (if it's iconified, for example)
      int xMargin = jifBounds.width < X_MARGIN ? jifBounds.width : X_MARGIN;
      int yMargin = jifBounds.height < Y_MARGIN ? jifBounds.height : Y_MARGIN;

      newX = Math.max(Math.min(newX, desktopSize.width - xMargin), xMargin - f.getWidth());
      newY = Math.max(Math.min(newY, desktopSize.height - yMargin), 0);

      super.dragFrame(f, newX, newY);
    }


  }



}
