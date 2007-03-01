/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003-2005 Alexander Maryanovsky.
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

package free.jin.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import free.jin.*;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.util.AWTUtilities;
import free.util.PlatformUtils;
import free.util.RectDouble;
import free.util.Utilities;
import free.util.swing.AdvancedJDesktopPane;
import free.util.swing.BackgroundChooser;
import free.util.swing.InternalFrameSwitcher;
import free.workarounds.FixedJInternalFrame;


/**
 * An MDI implementation of <code>UIProvider</code> - each
 * <code>PluginUIContainer</code> is implemented via a
 * <code>JInternalFrame</code>, all of which sit inside a main
 * <code>JFrame</code>.
 */

public class MdiUiProvider extends AbstractUiProvider{
  
  
  
  /**
   * The main frame.
   */

  private JFrame mainFrame;



  /**
   * The desktop pane.
   */

  private AdvancedJDesktopPane desktop;



  /**
   * The menubar.
   */

  private JMenuBar menubar;
  
  
  
  /**
   * The "Windows" menu.
   */
  
  private PluginContainersMenu windowsMenu;
  
  
  
  /**
   * The "Actions" menu.
   */
  
  private ActionsMenu actionsMenu;


  
  /**
   * The internal frame switcher that tracks the order of internal frames.
   */

  private InternalFrameSwitcher frameSwitcher;


  
  /**
   * Creates a new <code>MdiUiProvider</code>.
   */

  public MdiUiProvider(){
    
  }
  


  /**
   * Creates all the UI, makes the main frame visible and then invokes the
   * <code>start</code> method of the connection manager.
   */
   
  public void start(){
    super.start();
    
    I18n i18n = I18n.get(MdiUiProvider.class);
    mainFrame = createMainFrame();
    restoreFrameGeometry(Jin.getInstance().getPrefs(), mainFrame, "frame.",
        new RectDouble(1d/16, 1d/16, 7d/8, 7d/8));
    
    configureDesktop(desktop = new AdvancedJDesktopPane());
    configureMenubar(menubar = new JMenuBar());

    mainFrame.setContentPane(desktop);
    mainFrame.setJMenuBar(menubar);

    windowsMenu = new PluginContainersMenu();
    i18n.initAbstractButton(windowsMenu, "windowsMenu");
    addPluginUIContainerCreationListener(windowsMenu);
    
    actionsMenu = new ActionsMenu();
    
    menubar.add(new ConnectionMenu());
    menubar.add(new MdiPrefsMenu());
    menubar.add(new HelpMenu());
    
    frameSwitcher = new InternalFrameSwitcher(desktop);
    desktop.setDesktopManager(new DesktopManager());
    
    // TODO: Find a way to implement ctrl+TAB without requiring security permissions.
    try{
      FocusManager.setCurrentManager(new FocusManager());
    } catch (SecurityException e){}
    
    
    mainFrame.addWindowListener(new WindowAdapter(){
      public void windowOpened(WindowEvent evt){
        mainFrame.removeWindowListener(this);
        
        // Workaround - otherwise menu activation shortcuts don't work
        // immediately. Under OS X, in native menubar mode, this actually breaks things.
        if ((mainFrame.getJMenuBar() != null) &&
            (mainFrame.getFocusOwner() != null) &&
            !PlatformUtils.isMacOSX()){
          mainFrame.getJMenuBar().requestFocus();
        }

        Jin.getInstance().getConnManager().start();
      }
    });
    
    mainFrame.setVisible(true);
  }
  
  
  
  /**
   * Creates and configures the main frame.
   */

  private JFrame createMainFrame(){
    JFrame frame = new JFrame();
    
    frame.setTitle(Jin.getInstance().getAppName());
    frame.setIconImage(frame.getToolkit().getImage(Jin.class.getResource("resources/icon.gif")));
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent evt){
        Jin.getInstance().quit(true);
      }
    });

    return frame;
  }
  



  /**
   * Configures the desktop according to user preferences.
   */

  private void configureDesktop(AdvancedJDesktopPane desktop){
    Preferences prefs = Jin.getInstance().getPrefs();

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

    //desktop.putClientProperty("JDesktopPane.dragMode", "outline");
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
    Frame parentFrame = mainFrame;
    if (parent != null)
      parentFrame = AWTUtilities.frameForComponent(parent);

    dialog.show(new JDialog(parentFrame), parent == null ? parentFrame : parent);
  }
  

  
  /**
   * SessionListener implementation. Adjusts the title of the main frame. 
   */
  
  public void sessionEstablished(SessionEvent evt){
    super.sessionEstablished(evt);
    
    Session session = evt.getSession();
    
    String username = session.getUser().getUsername();
    String serverName = session.getServer().getShortName();
    String appName = Jin.getInstance().getAppName();
    
    I18n i18n = I18n.get(MdiUiProvider.class);
    mainFrame.setTitle(i18n.getFormattedString("mainFrame.title", new Object[]{username, serverName, appName}));

    menubar.add(actionsMenu, 1);
    menubar.add(windowsMenu, 3);
  }
  
  
  
  /**
   * SessionListener implementation. Adjusts the title of the main frame.
   */
  
  public void sessionClosed(SessionEvent evt){
    super.sessionClosed(evt);
    
    mainFrame.setTitle(Jin.getInstance().getAppName());
    
    menubar.remove(actionsMenu);
    menubar.remove(windowsMenu);

    // Bugfix - otherwise activating the menu via keyboard stops working.
    // On OS X, with native menubar, this actually breaks things.
    if (!PlatformUtils.isMacOSX())
      menubar.requestFocus();
  }
  



  /**
   * Returns a new UIContainer for the specified plugin.
   */

  public PluginUIContainer createPluginUIContainer(Plugin plugin, String id, int mode){
    AbstractPluginUIContainer container = new InternalFramePluginUIContainer(plugin, id, mode);
    
    addPluginContainer(plugin, id, container);
    
    return container;
  }
  
  
  
  /**
   * Returns whether the main frame is visible.
   */
  
  public boolean isUiVisible(){
    return mainFrame.isVisible();
  }



  
  /**
   * Stores our state (such as main window bounds) to preferences and disposes
   * of the main window.
   */
   
  public void stop(){
    saveFrameGeometry(Jin.getInstance().getPrefs(), mainFrame, "frame.");
    mainFrame.dispose();    
  }


  
  /**
   * The connection menu.
   */

  private class ConnectionMenu extends JMenu implements ActionListener, SessionListener{



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
      I18n i18n = I18n.get(MdiUiProvider.class);
      
      i18n.initAbstractButton(this, "connectionMenu");

      add(newConnection = i18n.createMenuItem("newConnectionMenuItem"));
      add(closeConnection = i18n.createMenuItem("closeConnectionMenuItem"));
      separatorIndex = getItemCount();

      addSeparator();
      add(exit = i18n.createMenuItem("exitJinMenuItem"));
      
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
     * Registers us as session listener.
     */
    
    public void addNotify(){
      super.addNotify();
      
      Jin.getInstance().getConnManager().addSessionListener(this);
      Session session = Jin.getInstance().getConnManager().getSession();
      setConnected(session != null, session);
    }
    
    
    
    /**
     * Unregisters us as a session listener.
     */
    
    public void removeNotify(){
      super.removeNotify();
      
      Jin.getInstance().getConnManager().removeSessionListener(this);
    }
    
    
    
    /**
     * SessionListener implementation. Simply delegates to
     * <code>setConnected</code>.
     */
    
    public void sessionEstablished(SessionEvent evt){
      setConnected(true, evt.getSession());
    }
    
    
    
    /**
     * SessionListener implementation. Simple delegates to
     * <code>setConnected</code>.
     */
    
    public void sessionClosed(SessionEvent evt){
      setConnected(false, evt.getSession());
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
        if (Jin.getInstance().isKnownUser(user) && !user.isGuest()){
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
      ConnectionManager connManager = Jin.getInstance().getConnManager();
      Object source = evt.getSource();
      if (source == newConnection){
        connManager.displayNewConnUI();
      }
      else if (source == closeConnection){
        Object result = OptionPanel.OK;
        Session session = Jin.getInstance().getConnManager().getSession();
        if ((session != null) && session.isConnected())
          result = I18n.get(MdiUiProvider.class).confirm(OptionPanel.OK, "closeConnectionConfirmationDialog");

        if (result == OptionPanel.OK)
          connManager.closeSession();
      }
      else if (source == exit){
        Jin.getInstance().quit(true);
      }
      else{ // One of the recent account menu items
        int index = Utilities.indexOf(getMenuComponents(), source);
        User user = (User)recentAccounts.elementAt(index - separatorIndex - 1);
        connManager.displayNewConnUI(user);
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
      I18n i18n = I18n.get(MdiUiProvider.class);
      for (int i = 1; i <= recentAccounts.size(); i++){
        User user = (User)recentAccounts.elementAt(i - 1);
        String label = i18n.getFormattedString("recentAccountMenuItem.text",
            new Object[]{user.getUsername(), user.getServer().getShortName()});
        JMenuItem menuItem = new JMenuItem(i + " " + label);
        if (i <= 9)
          menuItem.setDisplayedMnemonicIndex(0);
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
      Preferences prefs = Jin.getInstance().getPrefs();

      int count = prefs.getInt("accounts.recent.count", 0);
      for (int i = 0; i < count; i++){
        String username = prefs.getString("accounts.recent." + i + ".username");
        String serverId = prefs.getString("accounts.recent." + i + ".serverId");

        Server server = Jin.getInstance().getServerById(serverId);
        if (server == null)
          continue;

        User user = Jin.getInstance().getUser(server, username);
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
      Preferences prefs = Jin.getInstance().getPrefs();

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
   * An extended preferences menu for MDI mode.
   */
  
  private class MdiPrefsMenu extends PrefsMenu{
    
    
    
    /**
     * Creates a new MdiPrefsMenu. 
     */
    
    public MdiPrefsMenu(){
      JMenuItem bgMenuItem = I18n.get(MdiUiProvider.class).createMenuItem("backgroundPrefsMenuItem");
      add(bgMenuItem);
      
      bgMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          Preferences prefs = Jin.getInstance().getPrefs();
          String wallpaperFilename = prefs.getString("desktop.wallpaper.filename", null);
          File currentImageFile = wallpaperFilename == null ? null : new File(wallpaperFilename);
          
          BackgroundChooser bChooser = new BackgroundChooser(mainFrame, desktop,
            null, null, AdvancedJDesktopPane.CENTER, desktop.getBackground(), currentImageFile, 
            desktop.getWallpaperLayoutStyle());
          bChooser.setVisible(true);

          Color chosenColor = bChooser.getColor();
          File chosenImageFile = bChooser.getImageFile();
          int chosenLayoutStyle = bChooser.getImageLayoutStyle();

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
      });
    }
    
    
    
  }
  
  
  
  
  /**
   * An implementation of <code>PluginContainer</code> which uses a
   * <code>JInternalFrame</code> for the actual container.
   */
  
  private class InternalFramePluginUIContainer extends AbstractPluginUIContainer
      implements VetoableChangeListener, InternalFrameListener{
    

    
    /**
     * The actual internal frame.
     */

    private final MdiInternalFrame frame;
    
    
    
    /**
     * Creates a new <code>InternalFranePluginUIContainer</code>.
     */

    public InternalFramePluginUIContainer(Plugin plugin, String id, int mode){
      super(plugin, id, mode);
      
      this.frame = new MdiInternalFrame();
      
      // See http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for the 
      // reason I do this instead of adding an InternalFrameListener like a sane person.
      frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
      frame.addVetoableChangeListener(this);
      frame.addInternalFrameListener(this);

      frame.setVisible(false); // internal frames are initially visible in 1.1
      
      setIconImpl(Toolkit.getDefaultToolkit().getImage(Jin.class.getResource("resources/icon.gif")));
    }
    
    
    
    /**
     * Disposes of this plugin container.
     */

    public void disposeImpl(){
      setVisible(false);
      frame.dispose();
    }
    
    
    
    /**
     * Inserts a menu at the specified index.
     */
    
    protected void insertMenu(JMenu menu, int index){
      JMenuBar menubar = frame.getJMenuBar();
      if (menubar == null)
        frame.setJMenuBar(menubar = new JMenuBar());
      
      menubar.add(menu, index);
    }
    
    
    
    /**
     * Returns the amount of menus added.
     */
    
    protected int getMenuCount(){
      JMenuBar menubar = frame.getJMenuBar();
      return (menubar == null) ? 0 : menubar.getMenuCount(); 
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
     * Sets the resizable state of this plugin container's frame.
     */
    
    public void setResizable(boolean resizable){
      frame.setResizable(resizable);
    }
    
    
    
    /**
     * Returns whether the frame of this plugin container is currently
     * resizable.
     */
    
    public boolean isResizable(){
      return frame.isResizable();
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

    public void setTitleImpl(String title){
      frame.setTitle(title);
      frame.repaint(); // Bugfix: the title bar doesn't repaint itself in MS VM
    }



    /**
     * Sets the icon of the frame.
     */

    public void setIconImpl(Image image){
      // InternalFrames don't seem to scale their icons properly on their own
      Icon defaultIcon = UIManager.getIcon("InternalFrame.icon");
      if (defaultIcon != null){
        int w = defaultIcon.getIconWidth();
        int h = defaultIcon.getIconHeight();
        if ((image.getWidth(null) != w) || (image.getHeight(null) != h))
          image = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
      }
      
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
     * Shows the frame.
     */

    private void show(){
      loadState();
      
      desktop.add(frame);
      frame.setVisible(true);
      
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_SHOWN));
    }



    /**
     * Hides the frame.
     */

    private void hide(){
      saveState();
      
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
     * VetoableChangeListener implementation. See
     * http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for
     * the reason this is needed.
     */

    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
      if (pce.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY) &&
          pce.getOldValue().equals(Boolean.FALSE) && pce.getNewValue().equals(Boolean.TRUE)){

        switch (getMode()){
          case HIDEABLE_CONTAINER_MODE:
          case CLOSEABLE_CONTAINER_MODE:
            setVisible(false);
            break;
          case ESSENTIAL_CONTAINER_MODE:
            closeSession(frame);
            break;
          case SELF_MANAGED_CONTAINER_MODE:
            firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_CLOSING));
            break;
        }

        throw new PropertyVetoException("Canceled closing", pce);
      }
    }
    
    
    /**
     * Saves the properties of this plugin container into user preferences.
     */

    protected void saveState(){
      String id = getId();
      if (id == null)
        return;

      Preferences prefs = getPlugin().getPrefs();
      String prefix = getPrefsPrefix();

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
      
//      if (getMode() == HIDEABLE_CONTAINER_MODE)
//        prefs.setBool(prefix + "visible", isVisible());
    }



    /**
     * Loads the saved properties of this plugin container and configures it
     * properly.
     */

    protected void loadState(){
      String id = getId();
      Preferences prefs = getPlugin().getPrefs();
      
      String prefix = getPrefsPrefix();

      RectDouble defaultBounds = getInitialBounds(frame, mainFrame.getContentPane().getSize());
      RectDouble bounds = (id == null ? defaultBounds :
        prefs.getRectDouble(prefix + "iframe.bounds", defaultBounds));

      int desktopWidth = desktop.getWidth();
      int desktopHeight = desktop.getHeight();
      
      // Scale to the desktop size
      bounds = bounds.scale(desktopWidth, desktopHeight);
      
      // Fix the bounds in case they went bad for some reason.
      bounds.setX(Math.min(desktopWidth - 40, Math.max(0, bounds.getX())));
      bounds.setY(Math.min(desktopHeight - 40, Math.max(0, bounds.getY())));
      bounds.setWidth(Math.min(desktopWidth, Math.max(40, bounds.getWidth())));
      bounds.setHeight(Math.min(desktopHeight, Math.max(40, bounds.getHeight())));

      frame.setBounds(bounds.toRect());

      try{
        frame.setMaximum(prefs.getBool(prefix + "iframe.isMaximized", false));
        frame.setIcon(prefs.getBool(prefix + "iframe.isIconified", false));
      } catch (java.beans.PropertyVetoException ex){}
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
    
    
    
    private class MdiInternalFrame extends FixedJInternalFrame{
      
      
      
      /**
       * Creates a new MdiInternalFrame.
       */
      
      public MdiInternalFrame(){
        super("", true, true, true, true);
        setContentPane(new JPanel());
      }
      
      
      
      /**
       * Returns the minimum size of the frame, based on the minimum size of its
       * contents.
       */
      
      public Dimension getMinimumSize(){
        Insets insets = getInsets();
        Dimension dim = this.getContentPane().getLayout().minimumLayoutSize(this.getContentPane());
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return dim;
      }
    
      
      
    }
    
    
    
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
      if (!SwingUtilities.isDescendingFrom(evt.getComponent(), mainFrame.getContentPane())){
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
      
      // Don't allow sizes smaller than the minimum size of the component
      Dimension minSize = f.getMinimumSize();
      newWidth = Math.max(newWidth, minSize.width);
      newHeight = Math.max(newHeight, minSize.height);
      
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
