/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
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

package free.jin;

import javax.swing.*;
import javax.swing.event.*;
import free.util.*;
import free.util.swing.LookAndFeelMenu;
import free.util.swing.BackgroundChooser;
import free.util.swing.AdvancedJDesktopPane;
import free.jin.plugin.Plugin;
import free.jin.plugin.PreferencesPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.EmptyBorder;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;



/**
 * The JMenuBar on Jin's main frame.
 */

public class JinFrameMenuBar extends JMenuBar{



  /**
   * The JinFrame assosiated with this JinFrameMenuBar.
   */

  private final JinFrame jinFrame;



  /**
   * The "Connection" menu.
   */

  private final JMenu connectionMenu;



  /**
   * The "Preferences" menu.
   */

  private final JMenu preferencesMenu;




  /**
   * Holds a list of all the plugin menus we've added.
   */

  private final Vector pluginMenus = new Vector(4);



  /**
   * Holds a list of all the plugin UI menu items we've added.
   */

  private final Vector pluginUIMenuItems = new Vector(4);




  /**
   * Holds a list of JMenuItems whose state (enabled/disabled) needs to be
   * flipped when a connection/disconnection occurs.
   */

  private final Vector connectionSensitiveMenuItems = new Vector();





  /**
   * The ActionListener for JMenuItems representing user connections. The
   * ActionListener invokes the JinFrame.showLoginDialog(User) on the JinFrame
   * of this application when its actionPerformed(ActionEvent) method is invoked.
   */

  private final ActionListener userConnectionListener = new ActionListener(){

    public void actionPerformed(ActionEvent evt){
      User chosenUser = ((UserMenuItem)evt.getSource()).getUser();
      jinFrame.showLoginDialog(chosenUser);
    }

  };



  /**
   * The JSeparator starting the user connections list.
   */

  private final JSeparator startUserConnSep = new JSeparator();




  /**
   * The JSeparator ending the user connections list.
   */

  private final JSeparator endUserConnSep = new JSeparator();



  
  /**
   * Creates a new JinFrameMenuBar.
   * 
   * @param jinFrame The JinFrame to which this JinFrameMenuBar will be added.
   */

  public JinFrameMenuBar(JinFrame jinFrame){
    this.jinFrame = jinFrame;

    add(connectionMenu = createConnectionMenu());
    add(new LookAndFeelMenu(jinFrame.getRootPane()));
    add(preferencesMenu = createPreferencesMenu());
    add(createHelpMenu());

    setBorderPainted(true);
  }



  /**
   * Creates and returns a "Connection" menu.
   */

  public JMenu createConnectionMenu(){
    final JMenu connMenu = new JMenu("Connection");
    connMenu.setMnemonic(KeyEvent.VK_C);

    JMenuItem newConnMenuItem = new JMenuItem("New Connection...");
    newConnMenuItem.setMnemonic(KeyEvent.VK_N);
    newConnMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    newConnMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        jinFrame.showConnectionCreationUI();
      }

    });
    connectionSensitiveMenuItems.addElement(newConnMenuItem);


    JMenuItem openConnMenuItem = new JMenuItem("Open Connection...");
    openConnMenuItem.setMnemonic(KeyEvent.VK_O);
    openConnMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    openConnMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        JFileChooser chooser = new JFileChooser(Jin.usersDir);
        chooser.setCurrentDirectory(Jin.usersDir);
        Enumeration servers = Jin.getServers();
        while (servers.hasMoreElements()){
          Server server = (Server)servers.nextElement();
          chooser.addChoosableFileFilter(new ServerSpecificUserFileFilter(server));
        }
        chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());

        int result = chooser.showOpenDialog(jinFrame);
        if (result!=JFileChooser.APPROVE_OPTION)
          return;

        User chosenUser = Jin.getUserByFilename(chooser.getSelectedFile().getName());
        jinFrame.showLoginDialog(chosenUser);
      }

    });
    connectionSensitiveMenuItems.addElement(openConnMenuItem);

    JMenuItem closeConnMenuItem = new JMenuItem("Close Connection");
    closeConnMenuItem.setMnemonic('l'); 
    closeConnMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        int result = JOptionPane.showConfirmDialog(jinFrame, "Are you sure you want to log out and close the connection?", "Select an option", JOptionPane.YES_NO_OPTION);

        if (result==JOptionPane.YES_OPTION)
          jinFrame.closeConnection();
      }

    });
    closeConnMenuItem.setEnabled(false);
    connectionSensitiveMenuItems.addElement(closeConnMenuItem);

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.setMnemonic(KeyEvent.VK_X);
    exitMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        Jin.close();
      }

    });

    connMenu.add(newConnMenuItem);
    connMenu.add(openConnMenuItem);
    connMenu.add(closeConnMenuItem);
    connMenu.add(startUserConnSep);
    connMenu.add(endUserConnSep);
    update(connMenu);
    connMenu.add(exitMenuItem);

    Jin.getUsers().addListDataListener(new ListDataListener(){

      public void intervalAdded(ListDataEvent evt){
        update(connMenu);
      }

      public void intervalRemoved(ListDataEvent evt){
        update(connMenu);
      }

      public void contentsChanged(ListDataEvent evt){
        update(connMenu);
      }

    });

    return connMenu;
  } 





  /**
   * Creates the Preferences menu.
   */

  public JMenu createPreferencesMenu(){
    final JMenu prefsMenu = new JMenu("Preferences");
    prefsMenu.setMnemonic('p');

    JMenuItem backgroundMenuItem = new JMenuItem("Background");
    backgroundMenuItem.setMnemonic('B');
    backgroundMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AdvancedJDesktopPane desktop = (AdvancedJDesktopPane)jinFrame.getDesktop();
        String wallpaperFilename = Jin.getProperty("desktop.wallpaper");
        File currentImageFile = wallpaperFilename == null ? null : new File(wallpaperFilename);
        BackgroundChooser bChooser = new BackgroundChooser(jinFrame, desktop, null, AdvancedJDesktopPane.TILE, currentImageFile);
        bChooser.setVisible(true);

        Color chosenColor = bChooser.getChosenColor();
        File chosenImageFile = bChooser.getChosenImageFile();
        int chosenLayoutStyle = bChooser.getChosenImageLayoutStyle();

        Jin.setProperty("desktop.background.color", chosenColor == null ? null : StringEncoder.encodeColor(chosenColor));

        Jin.setProperty("desktop.wallpaper", chosenImageFile == null ? null : chosenImageFile.getAbsolutePath());

        switch (chosenLayoutStyle){
          case AdvancedJDesktopPane.CENTER:
            Jin.setProperty("desktop.wallpaper.layout", "center");
            break;
          case AdvancedJDesktopPane.TILE:
            Jin.setProperty("desktop.wallpaper.layout", "tile");
            break;
          case AdvancedJDesktopPane.STRETCH:
            Jin.setProperty("desktop.wallpaper.layout", "stretch");
            break;
          case -1:
            Jin.setProperty("desktop.wallpaper.layout", null);
            break;
        }
      }

    });

    prefsMenu.add(backgroundMenuItem);

    return prefsMenu;
  }




  /**
   * This method is called when a connection is about to be established. It
   * performes whatever necessary modifications to the menubar, such as
   * adding certain menus.
   *
   * @param conn The JinConnection about to be established.
   * @param plugins An Enumeration of all the plugins for the connection.
   */

  void connecting(JinConnection conn, Enumeration plugins){
    while (plugins.hasMoreElements()){
      Plugin plugin = (Plugin)plugins.nextElement();

      JMenu pluginMenu = plugin.createPluginMenu();
      if (pluginMenu != null)
        addPluginMenu(pluginMenu);

      if (plugin.hasPreferencesUI())
        addPluginPreferenceUIMenuItem(plugin);
    }

    int size = connectionSensitiveMenuItems.size();
    for (int i = 0; i < size; i++){
      JMenuItem menuItem = (JMenuItem)connectionSensitiveMenuItems.elementAt(i);
      menuItem.setEnabled(!menuItem.isEnabled());
    }
    
    // Bugfix
    invalidate();
    validate();
  }





  /**
   * This method is called when the connection has been broken. It should return
   * the menubar to the state it was in before
   * <code>connecting(JinConnection, Enumeration)</code> was called.
   */

  void disconnected(JinConnection conn){
    removePluginMenus();
    removePluginPreferenceUIMenuItems();

    int size = connectionSensitiveMenuItems.size();
    for (int i = 0; i < size; i++){
      JMenuItem menuItem = (JMenuItem)connectionSensitiveMenuItems.elementAt(i);
      menuItem.setEnabled(!menuItem.isEnabled());
    }
  }




  /**
   * Adds a menu item which shows the given plugin's preferences UI to the
   * "Preferences" menu.
   */

  private void addPluginPreferenceUIMenuItem(Plugin plugin){
    if (!plugin.hasPreferencesUI())
      return;

    if (pluginUIMenuItems.size() == 0)
      preferencesMenu.addSeparator(); 

    JMenuItem menuItem = new JMenuItem(plugin.getName());
    menuItem.addActionListener(new ShowPreferencesUI(plugin));
    preferencesMenu.add(menuItem);

    pluginUIMenuItems.addElement(menuItem);
  }




  /**
   * An ActionListener implementation which opens a dialog with the given
   * Plugin's preferences UI.
   */

  private class ShowPreferencesUI implements ActionListener{

    private final Plugin targetPlugin;

    public ShowPreferencesUI(Plugin plugin){
      this.targetPlugin = plugin; 
    }

    public void actionPerformed(ActionEvent evt){
      final JDialog dialog = new JDialog(jinFrame, targetPlugin.getName()+" preferences", true);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      ActionListener closer = new WindowDisposingActionListener(dialog);
      dialog.getRootPane().registerKeyboardAction(closer, closeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

      dialog.getContentPane().setLayout(new BorderLayout());

      final PreferencesPanel prefPanel = targetPlugin.getPreferencesUI();
      JPanel prefWrapperPanel = new JPanel(new BorderLayout());
      prefWrapperPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      prefWrapperPanel.add(BorderLayout.CENTER, prefPanel);
      dialog.getContentPane().add(BorderLayout.CENTER, prefWrapperPanel);

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(BorderLayout.CENTER, new JSeparator(JSeparator.HORIZONTAL));
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      JButton okButton = new JButton("OK");
      JButton cancelButton = new JButton("Cancel");
      final JButton applyButton = new JButton("Apply");
      applyButton.setEnabled(false);

      okButton.setMnemonic('O');
      cancelButton.setMnemonic('C');
      applyButton.setMnemonic('A');
      prefPanel.addChangeListener(new ChangeListener(){
        public void stateChanged(ChangeEvent evt){
          applyButton.setEnabled(true);
        }
      });

      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      buttonPanel.add(applyButton);
      bottomPanel.add(BorderLayout.SOUTH, buttonPanel);

      okButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          prefPanel.applyChanges();
          dialog.dispose();
        }
      });

      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          dialog.dispose();
        }
      });

      applyButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          prefPanel.applyChanges();
          applyButton.setEnabled(false);
        }
      });

      dialog.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
      dialog.getRootPane().setDefaultButton(okButton);

      AWTUtilities.centerWindow(dialog, jinFrame);

      dialog.show();
    }

  }





  /**
   * Removes the menus added via the 
   * <code>addPluginPreferenceUIMenuItem(Plugin)</code> method.
   */

  private void removePluginPreferenceUIMenuItems(){
    boolean hadPreferenceMenus = !pluginUIMenuItems.isEmpty();
    int index = pluginUIMenuItems.size()-1;
    while (index >= 0){
      JMenuItem menuItem = (JMenuItem)pluginUIMenuItems.elementAt(index);
      pluginUIMenuItems.removeElementAt(index);
      preferencesMenu.remove(menuItem);
      index--;
    }

    if (hadPreferenceMenus)
      preferencesMenu.remove(preferencesMenu.getMenuComponentCount()-1); // Separator
  }




  /**
   * Adds the given JMenu as a plugin's menu.
   */

  private void addPluginMenu(JMenu menu){
    add(menu, 3);
    pluginMenus.addElement(menu);
  }




  /**
   * Removes all the menus added via the <code>addPluginMenu</code> method.
   */

  private void removePluginMenus(){
    int index = pluginMenus.size() - 1;
    while (index >= 0){
      JMenu menu = (JMenu)pluginMenus.elementAt(index);
      pluginMenus.removeElementAt(index);
      remove(menu);
      index--;
    }

    invalidate();
    validate();
    repaint();
  }

  



  /**
   * Synchronizes the currently known Users (obtained by invoking Jin.getUsers())
   * with the given JMenu. After this method returns, the given JMenu contains
   * only UserMenuItems representing the known users between the startUserConn
   * JSeparator and endUserConn JSeparator.
   */

  private void update(JMenu connMenu){
    int startSeparatorIndex = -1;
    int menuItemCount = connMenu.getMenuComponentCount();
    for (int i=0;i<menuItemCount;i++){
      if (connMenu.getMenuComponent(i)==startUserConnSep){
        startSeparatorIndex = i;
        break;
      }
    }
    if (startSeparatorIndex==-1)
      throw new IllegalStateException("JSeparator starting the user connections list not found.");


    while (connMenu.getMenuComponent(startSeparatorIndex+1)!=endUserConnSep){
      JMenuItem menuItem = connMenu.getItem(startSeparatorIndex+1);
      connMenu.remove(startSeparatorIndex+1);
      connectionSensitiveMenuItems.removeElement(menuItem);
    }


    boolean areMenuItemsEnabled = (jinFrame.getConnection() == null);
    ListModel users = Jin.getUsers();
    int usersCount = users.getSize();
    for (int i=0;i<usersCount;i++){
      User user = (User)users.getElementAt(i);
      JMenuItem menuItem = new UserMenuItem(user);
      menuItem.setText(String.valueOf(usersCount-i)+" "+menuItem.getText());
      menuItem.addActionListener(userConnectionListener);
      if (usersCount - i <= 9)
        menuItem.setMnemonic(Character.forDigit(usersCount - i,10));
      menuItem.setEnabled(areMenuItemsEnabled);
      connMenu.insert(menuItem, startSeparatorIndex+1);
      connectionSensitiveMenuItems.addElement(menuItem);
    }
  }




  /**
   * Creates and returns the "Help" menu.
   */

  protected JMenu createHelpMenu(){
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('h');

    JMenuItem websiteMenuItem = new JMenuItem("Jin Website");
    websiteMenuItem.setMnemonic('J');
    websiteMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String url = "http://www.jinchess.com";
        try{
          BrowserControl.displayURL(url);
        } catch (java.io.IOException e){
            JOptionPane.showMessageDialog(jinFrame, "Unable to display URL: "+url, "Error", JOptionPane.ERROR_MESSAGE);
          }
      }
    });
    helpMenu.add(websiteMenuItem);

    JMenuItem licenseMenuItem = new JMenuItem("Licensing and Copyrights...");
    licenseMenuItem.setMnemonic('L');
    licenseMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        JDialog licenseDialog = new LicenseDialog(jinFrame);
        AWTUtilities.centerWindow(licenseDialog, jinFrame);
        licenseDialog.setResizable(false);
        licenseDialog.setVisible(true);
      }
    });
    helpMenu.add(licenseMenuItem);

    JMenuItem reportBugMenuItem = new JMenuItem("Report a bug");
    reportBugMenuItem.setMnemonic('R');
    reportBugMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String url = "https://sourceforge.net/tracker/?group_id=50386&atid=459537";
        try{
          BrowserControl.displayURL(url);
        } catch (java.io.IOException e){
            JOptionPane.showMessageDialog(jinFrame, "Unable to display URL: "+url, "Error", JOptionPane.ERROR_MESSAGE);
          }
      }
    });
    helpMenu.add(reportBugMenuItem);

    JMenuItem aboutMenuItem = new JMenuItem("About Jin...");
    aboutMenuItem.setMnemonic('A');
    aboutMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        JDialog aboutDialog = new AboutDialog(jinFrame);
        AWTUtilities.centerWindow(aboutDialog, jinFrame);
        aboutDialog.setResizable(false);
        aboutDialog.setVisible(true);
      }
    });
    helpMenu.add(aboutMenuItem);

    return helpMenu;
  }




  /**
   * A JMenuItem representing a User.
   */

  private class UserMenuItem extends JMenuItem{


    /**
     * The User represented by this UserMenuItem.
     */

    private final User user;



    /**
     * Creates a new UserMenuItem which represents the given User.
     */

    public UserMenuItem(User user){
      super(user.getFilename());
      this.user = user;
    }



    /**
     * Returns the User represented by this UserMenuItem.
     */

    public User getUser(){
      return user;
    }


  }

}