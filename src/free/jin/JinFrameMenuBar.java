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

import free.util.swing.LookAndFeelMenu;
import free.util.swing.BackgroundChooser;
import free.util.swing.AdvancedJDesktopPane;
import free.util.StringEncoder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import java.awt.Component;
import java.awt.Color;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.Enumeration;
import java.util.Hashtable;
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
   * Maps JinConnection objects to JMenus for their plugins.
   */

  private final Hashtable pluginsMenus = new Hashtable();




  /**
   * The currently visible PluginsMenu.
   */

  private JMenu currentlyVisiblePluginsMenu = null;




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

    add(createConnectionMenu());
    add(new LookAndFeelMenu(jinFrame.getRootPane()));
    add(createPreferencesMenu());
  }



  /**
   * Creates and returns a "Connection" menu.
   */

  public JMenu createConnectionMenu(){
    final JMenu connMenu = new JMenu("Connection");
    connMenu.setMnemonic('c');

    JMenuItem newConn = new JMenuItem("New Connection");
    newConn.setMnemonic('n');
    newConn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    newConn.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        jinFrame.showConnectionCreationUI();
      }

    });


    JMenuItem openConn = new JMenuItem("Open Connection");
    openConn.setMnemonic('o');
    openConn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    openConn.addActionListener(new ActionListener(){

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


    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.setMnemonic('x');
    exitMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        Jin.close();
      }

    });


    connMenu.add(newConn);
    connMenu.add(openConn);
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

    JMenuItem backgroundMenuItem = new JMenuItem("Background");
    backgroundMenuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AdvancedJDesktopPane desktop = (AdvancedJDesktopPane)jinFrame.getDesktop();
        Color defaultColor = UIManager.getColor("desktop");
        BackgroundChooser bChooser = new BackgroundChooser(jinFrame, desktop, null, AdvancedJDesktopPane.CENTER, defaultColor);
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
   * Adds a plugins JMenu for the given JinConnection.
   */

  public void addPluginsMenu(JinConnection conn){
    JMenu pluginsMenu = new JMenu("Plugins");
    pluginsMenu.setMnemonic('p');

    pluginsMenus.put(conn, pluginsMenu);
  }



  /**
   * Removes the plugins JMenu for the given JinConnection.
   */

  public void removePluginsMenu(JinConnection conn){
    pluginsMenus.remove(conn);
  }




  /**
   * Makes the plugins JMenu for the given JinConnection the currently visible
   * plugins menu.
   */

  public void makePluginsMenuVisible(JinConnection conn){
    JMenu menu = (JMenu)pluginsMenus.get(conn);
    
    if (currentlyVisiblePluginsMenu==menu)
      return;
    if (currentlyVisiblePluginsMenu!=null){
      int index = getComponentIndex(currentlyVisiblePluginsMenu);
      remove(currentlyVisiblePluginsMenu);
      add(menu, index);
    }
    else
      add(menu);
    currentlyVisiblePluginsMenu = menu;
  }




  /**
   * Returns the plugins JMenu for the given JinConnection.
   */

  public JMenu getPluginsMenu(JinConnection conn){
    return (JMenu)pluginsMenus.get(conn);
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


    while (connMenu.getMenuComponent(startSeparatorIndex+1)!=endUserConnSep)
      connMenu.remove(startSeparatorIndex+1);


    ListModel users = Jin.getUsers();
    int usersCount = users.getSize();
    for (int i=0;i<usersCount;i++){
      User user = (User)users.getElementAt(i);
      JMenuItem menuItem = new UserMenuItem(user);
      menuItem.setText(String.valueOf(usersCount-i)+" "+menuItem.getText());
      menuItem.addActionListener(userConnectionListener);
      if (usersCount-i<=9)
        menuItem.setMnemonic(Character.forDigit(usersCount-i,10));
      connMenu.insert(menuItem,startSeparatorIndex+1);
    }
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