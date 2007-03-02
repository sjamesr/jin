/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import free.jin.I18n;
import free.jin.Jin;
import free.jin.Preferences;
import free.jin.SessionEvent;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.util.AWTUtilities;
import free.util.RectDouble;
import free.workarounds.FixedJFrame;



/**
 * An SDI implementation of <code>UIManager</code> - each
 * <code>PluginUIContainer</code> is implemented via a single top level frame. 
 */

public class SdiUiProvider extends AbstractUiProvider{
  
  
  
  /**
   * The number of currently open dialogs.
   */
  
  private int openDialogsCount = 0;



  /**
   * Creates a new <code>SdiUiProvider</code>.
   */

  public SdiUiProvider(){
    
  }



  /**
   * Simply invokes <code>start</code> on the <code>ConnectionManager</code>.
   */

  public void start(){
    super.start();
    
    Jin.getInstance().getConnManager().start();
  }
  
  
  
  /**
   * In SDI mode, we close Jin once the session is closed.
   */
  
  public void sessionClosed(SessionEvent evt){
    super.sessionClosed(evt);
    
    // Check that we've really connected
    if (evt.getSession().getPort() != -1)
      Jin.getInstance().quit(false);
  }



  /**
   * Returns a new UIContainer for the specified plugin.
   */

  public PluginUIContainer createPluginUIContainer(Plugin plugin, String id, int mode){
    AbstractPluginUIContainer container = new FramePluginUIContainer(plugin, id, mode);
    
    addPluginContainer(plugin, id, container);
    
    return container;
  }



  /**
   * Displays the specified <code>DialogPanel</code> via
   * {@link DialogPanel#show(JDialog)}.
   */

  public void showDialog(DialogPanel dialog, Component parent){
    Frame parentFrame = parent == null ? 
        null : AWTUtilities.frameForComponent(parent);

    JDialog jdialog = new JDialog(parentFrame);
    // Count how many open dialogs we have
    jdialog.addWindowListener(new WindowAdapter(){
      // Can't depend on the system to be consistent about open/close events
      private boolean isOpen = false;

      public void windowOpened(WindowEvent evt){
        if (!isOpen){
          isOpen = true;
          openDialogsCount++;
        }
      }
      public void windowClosed(WindowEvent evt){
        if (isOpen){
          isOpen = false;
          openDialogsCount--;
        }
      }
    });
    
    dialog.show(jdialog, parent);
  }
  
  
  
  /**
   * Returns whether any plugin containers or dialogs are visible.
   */
  
  public boolean isUiVisible(){
    Enumeration containers = getExistingPluginUIContainers();
    while (containers.hasMoreElements()){
      PluginUIContainer c = (PluginUIContainer)containers.nextElement();
      if (c.isVisible())
        return true;
    }
    
    return openDialogsCount > 0;
  }




  /**
   * Nothing for us to do here. 
   */

  public void stop(){

  }
  
  
  
  /**
   * An implementation of <code>PluginUIContainer</code> which uses a
   * <code>JFrame</code> for the actual container.
   */
  
  private class FramePluginUIContainer extends AbstractPluginUIContainer 
      implements WindowListener{
    
    
    
    /**
     * The <code>JFrame</code>.
     */
    
    private final JFrame frame;
    
    
    
    /**
     * The menubar of the frame.
     */
    
    private final JMenuBar menubar;
    
    
    
    /**
     * Creates a new <code>FramePluginUIContainer</code>.
     */
    
    public FramePluginUIContainer(Plugin plugin, String id, int mode){
      super(plugin, id, mode);
      
      this.frame = new FixedJFrame("");
      
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(this);

      this.menubar = new JMenuBar();
      
      I18n i18n = I18n.get(SdiUiProvider.class);
      
      PluginContainersMenu windowsMenu = new PluginContainersMenu(getExistingPluginUIContainers());
      i18n.initAbstractButton(windowsMenu, "windowsMenu");
      addPluginUIContainerCreationListener(windowsMenu);
      
      ActionsMenu actionsMenu = new ActionsMenu();
      
      menubar.add(actionsMenu);
      menubar.add(new PrefsMenu());
      menubar.add(windowsMenu);
      menubar.add(new HelpMenu());
      
      frame.setJMenuBar(menubar);
      
      setIconImpl(Toolkit.getDefaultToolkit().getImage(Jin.class.getResource("resources/icon.gif")));
      
      JRootPane rootPane = frame.getRootPane();
      ActionListener closer = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          close();
        }
      };
      KeyStroke closeKeyStroke = 
        KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()); 
      rootPane.registerKeyboardAction(closer, closeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    
    
    /**
     * Disposes of this plugin container.
     */

    public void disposeImpl(){
      setVisible(false);
      frame.dispose();
    }
    
    
    
    /**
     * Adds a plugin menu. 
     */
    
    public void addMenu(JMenu menu){
      insertMenu(menu, getMenuCount() - 1); // Insert before the help menu
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
     * If <code>active == true</code>, makes our frame the currently selected
     * one.   
     */
    
    public void setActive(boolean active){
      if (active){
        if (!isVisible())
          setVisible(true);
        frame.toFront();
      }
      else{
        frame.toBack();
      }
    }
  

    /**
     * Returns whether the frame of this plugin container is currently selected.
     */
  
    public boolean isActive(){
      return frame.isShowing() && (frame.getFocusOwner() != null);
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
      frame.setIconImage(image);
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
      return frame.isVisible();
    }
    
    
    
    /**
     * Shows the frame.
     */
  
    private void show(){
      loadState();
      
      frame.setVisible(true);

      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_SHOWN));
    }
  
  
  
    /**
     * Hides the frame.
     */
  
    private void hide(){
      saveState();
      frame.setVisible(false);
  
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_HIDDEN));
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
      
      saveFrameGeometry(prefs, frame, prefix);
    }
  
  
  
    /**
     * Loads the saved properties of this plugin container and configures it
     * properly.
     */
  
    protected void loadState(){
      String prefix = getPrefsPrefix();
      
      Preferences prefs = getPlugin().getPrefs();

      Dimension screenSize = AWTUtilities.getUsableScreenBounds().getSize();
      
      // We given getInitialBounds a smaller screen size, which causes it to
      // dispense a larger relative size for components with a fixed preferred
      // size. We can then scale that size into a smaller rectangle.
      double sx = 1.0 + 100d/screenSize.width;
      double sy = 1.0 + 100d/screenSize.height;
      screenSize.width = (int)(screenSize.width/sx);
      screenSize.height = (int)(screenSize.height/sy);
      RectDouble defaultBounds = getInitialBounds(frame, screenSize);
      

      // Fit into a smaller rectangle, to avoid various screen decorations (taskbar and such).
      // Yes, getUsableScreenBounds is supposed to do this, but it doesn't seem to be reliable
      // (doesn't work on KDE 3.1, Java 1.5 for example).
      defaultBounds.scale(1/sx, 1/sy);
      defaultBounds.setX(defaultBounds.getX() + (sx - 1.0)/2);
      defaultBounds.setY(defaultBounds.getY() + (sy - 1.0)/2);
      
      restoreFrameGeometry(prefs, frame, prefix, defaultBounds);
    }
    
    
    
    /**
     * Closes this window according to the plugin container mode.
     */
    
    private void close(){
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
    }
    

    
    /**
     * WindowListener implementation.
     */
  
    public void windowActivated(WindowEvent e){
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_ACTIVATED));
    }
    public void windowDeactivated(WindowEvent e){
      firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_DEACTIVATED));
    }
    public void windowClosing(WindowEvent e){
      close();
    }

    public void windowClosed(WindowEvent e){}
    public void windowDeiconified(WindowEvent e){}
    public void windowIconified(WindowEvent e){}
    public void windowOpened(WindowEvent e){}

    

  }
  
  
}
