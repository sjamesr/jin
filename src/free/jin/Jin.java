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

import free.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Properties;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.net.URL;


/**
 * Jin's main class.
 */

public class Jin{



  /**
   * Invokes Class.forName("free.workarounds.SwingFix").
   */

  static{
    try{
      Class.forName("free.workarounds.SwingFix");
    } catch (ClassNotFoundException e){
        e.printStackTrace();
      }
  }



  /**
   * Compatibility version for the user properties and server user settings
   * files. Whenever the user.properties file contains a version different than 
   * this, the user.properties and server user settings files will be
   * deleted/ignored.
   */

  private static final String propsVersion = "1";
  


  /**
   * Jin's main frame.
   */

  private static JinFrame mainFrame;


  
  /**
   * Jin's properties.
   */

  private static final Properties jinProps = new Properties();




  /**
   * The user's properties. The 'user' here isn't the chess server account/user,
   * but the operating system user.
   */

  private static final Properties userProps = new Properties();




  /**
   * The folder where all jin settings files should be written to and read from.
   * Jin and all plugins should only write to that directory.
   */

  public static final File jinUserHome = new File(System.getProperty("user.home"), ".jin");




  /**
   * The folder where the settings of Users are kept.
   */

  public static final File usersDir = new File(jinUserHome, "users");

  



  /**
   * A list of Users known to Jin.
   */

  private static final DefaultListModel users = new DefaultListModel();




  /**
   * Maps server names to supported servers.
   */

  private static final Hashtable servers = new Hashtable();






  /**
   * Create the jin settings directory.
   */

  static{
    if (!jinUserHome.exists()){
      if (!jinUserHome.mkdirs()){
        System.err.println("Failed to create directory "+jinUserHome.getAbsolutePath());
        System.exit(1);
      }
    }
  }




  /**
   * Loads the Jin and the user properties.
   */

  static{
    try{
      InputStream propsIn = Jin.class.getResourceAsStream("resources/jin.properties");
      jinProps.load(propsIn);
      propsIn.close();
    } catch (IOException e){
        System.err.println("Unable to load Jin's properties:");
        e.printStackTrace();
        System.exit(1);
      }

    try{
      File propsFile = new File(jinUserHome, "user.properties");
      if (propsFile.canRead()){
        InputStream propsIn = new FileInputStream(propsFile);
        userProps.load(propsIn);
        propsIn.close();
      }

      String savedPropsVersion = userProps.getProperty("props.version");
      if ((savedPropsVersion == null) || !savedPropsVersion.equals(propsVersion)){
        if (propsFile.exists())
          propsFile.delete();
        if (usersDir.isDirectory())
          IOUtilities.rmdir(usersDir);
        userProps.clear();
        userProps.put("props.version", propsVersion);
      }
    } catch (IOException e){
        e.printStackTrace();
      }
  }




  /**
   * Creates the users directory.
   */

  static{
    if (!usersDir.exists()){
      if (!usersDir.mkdirs()){
        System.err.println("Failed to create directory "+usersDir.getAbsolutePath());
        System.exit(1);
      }
    }
  }




  /**
   * Loads the users.
   */

  static{
    try{
      String [] userFilenames = usersDir.list();
      for (int i=0;i<userFilenames.length;i++){
        String userFilename = userFilenames[i];
        File userFile = new File(usersDir, userFilename);
        if (userFile.isDirectory())
          continue;
        
        User user = User.load(userFile);
        users.addElement(user);
      }
    } catch (IOException e){
        System.err.println("Unable to load the user list:");
        e.printStackTrace();
      }
      catch (RuntimeException e){
        e.printStackTrace();
      }
  }




  /**
   * Loads the supported servers.
   */

  static{
    try{
      InputStream serverListInputStream = Jin.class.getResourceAsStream("resources/servers/list.txt");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      IOUtilities.pump(serverListInputStream, buf);
      StringTokenizer serversTokenizer = new StringTokenizer(new String(buf.toByteArray(), "8859_1"), "\n\r");

      while (serversTokenizer.hasMoreTokens()){
        String serverResourceName = serversTokenizer.nextToken();
        Server server = Server.load(Jin.class.getResourceAsStream(serverResourceName));
        servers.put(server.getName(),server);
      }
    } catch (IOException e){
        System.err.println("Unable to load the server list:");
        e.printStackTrace();
        System.exit(0);
      }
      catch (RuntimeException e){
        e.printStackTrace();
      }
  }





  /**
   * Returns Jin's main frame.
   */

  public static JinFrame getMainFrame(){
    return mainFrame;
  }

  



  /**
   * The operating system user's property with the given name. If no such
   * property exists, Jin's property with the given name, is returned.
   */

  public static String getProperty(String propertyName){
    String val = userProps.getProperty(propertyName);

    if (val==null)
      return jinProps.getProperty(propertyName);
    else
      return val;
  }




  /**
   * Same as <code>Jin.getProperty(String)</code> but if neither a user
   * property nor the Jin property with the given name is found, returns
   * the given default value.
   */

  public static String getProperty(String propertyName, String defaultValue){
    String propertyValue = getProperty(propertyName);
    return propertyValue == null ? defaultValue : propertyValue;
  }




  /**
   * Sets the given user property to have the given value. If the given value is
   * <code>null</code>, the property is removed.
   */

  public static void setProperty(String propertyName, String propertyValue){
    if (propertyValue == null)
      userProps.remove(propertyName);
    else
      userProps.put(propertyName, propertyValue);
  }




  /**
   * Returns the ListModel containing all the Users known to Jin. Note that this
   * returns the actual ListMode, not a copy, so it's possible to get notified
   * about changes to the known user list by registering listeners with the
   * returned ListModel.
   */

  public static ListModel getUsers(){
    return users;
  }




  /**
   * Returns the User loaded from the file with the given filename. Returns null
   * if no User whose filename matches the given value.
   */

  public static User getUserByFilename(String filename){
    int userCount = users.getSize();
    for (int i=0;i<userCount;i++){
      User user = (User)users.getElementAt(i);
      if (user.getFilename().equals(filename))
        return user;
    }
    return null;
  }




  /**
   * Returns true if the given User is a known User.
   */

  public static boolean isKnownUser(User user){
    int userCount = users.getSize();
    for (int i=0;i<userCount;i++){
      User curUser = (User)users.getElementAt(i);
      if (user.equals(curUser))
        return true;
    }

    return false;
  }




  /**
   * Saves the given User. If this is yet an unknown User and the user doesn't
   * abort the save, it is added to the list of known users.
   *
   * @throws IOException if an I/O error occurs while saving the user's
   * settings.
   */

  public static boolean save(User user){
    File userFile;
    String filename = user.getFilename();
    if (filename==null){
      String username = user.getProperty("login.username");
      String serverShortName = user.getServer().getProperty("name.short");
      userFile = new File(usersDir, username+"."+serverShortName);
    }
    else
      userFile = new File(usersDir, filename);

    /* I don't think you really need to ask the user where he wants his settings file. */
//    if ((filename==null)||!userFile.exists()){
//      JFileChooser chooser = new JFileChooser(usersDir);
//      chooser.setSelectedFile(userFile);
//      chooser.addChoosableFileFilter(new ServerSpecificUserFileFilter(user.getServer()));
//      chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
//
//      int result = chooser.showSaveDialog(mainFrame);
//      if (result==JFileChooser.CANCEL_OPTION)
//        return false;
//      userFile = chooser.getSelectedFile();
//
//      // TODO: Add checking whether the file already exists if JFileChooser doesn't
//      // do it automatically.
//      }

    try{
      user.save(userFile);
    } catch (IOException e){
        JOptionPane.showMessageDialog(mainFrame, "Unable to save your user file, your home directory or user file aren't writeable", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

    if (!users.contains(user))
      users.addElement(user);
    else{                         
      users.removeElement(user);  // Replace, this may look like stupid code, but remember Users 
      users.addElement(user);     // can be equal but different (User overrides equals(Object)).
    }

    return true;
  }





  /**
   * Returns an Enumeration of all the supported servers.
   */

  public static Enumeration getServers(){
    return servers.elements();
  }




  /**
   * Returns the server with the given name, or null if the server with the
   * given name is not supported.
   */

  public static Server getServer(String serverName){
    return (Server)servers.get(serverName);
  }




  /**
   * Returns a string describing the name and version of the interface.
   */

  public static String getInterfaceName(){
    return getProperty("name")+" "+getProperty("version")+" ("+System.getProperty("java.vendor")+" "+System.getProperty("java.version")+", "+System.getProperty("os.name")+" "+System.getProperty("os.version")+")";
  }



  /**
   * Jin's main method.
   */

  public static void main(String [] args){
    try{
      try{
        Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
        UIManager.installLookAndFeel("Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
      } catch (ClassNotFoundException e){}

      try{
        Class.forName("swing.addon.plaf.threeD.ThreeDLookAndFeel");
        UIManager.installLookAndFeel("3D Look&Feel", "swing.addon.plaf.threeD.ThreeDLookAndFeel");
      } catch (ClassNotFoundException e){}

      mainFrame = new JinFrame();

      String lnf = getProperty("default.lf");
//      if (lnf==null){
//        String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
//        if (systemLookAndFeelClassName != null)
//          UIManager.setLookAndFeel(systemLookAndFeelClassName);
//      }
//      else
      if (lnf != null)
        UIManager.setLookAndFeel(lnf);

      SwingUtilities.updateComponentTreeUI(mainFrame);
      

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle screenBounds = new Rectangle(screenSize);
      // Pretend the screen is somewhat bigger to allow frame decorations to be
      // placed outside the screen.
      screenBounds.x -= 10;
      screenBounds.y -= 10;
      screenBounds.width += 20;
      screenBounds.height += 20;

      String frameBoundsString = getProperty("frame.bounds");
      Rectangle frameBounds = null;
      if (frameBoundsString != null)
        frameBounds = StringParser.parseRectangle(frameBoundsString);
      
      if (frameBounds == null){
        mainFrame.setLocation(screenSize.width/16, screenSize.height/16);
        mainFrame.setSize(screenSize.width*7/8, screenSize.height*7/8);
      }
      else
        mainFrame.setBounds(frameBounds);


      String title = getProperty("frame.title", "Jin");
      mainFrame.setTitle(title);

      URL iconURL = Jin.class.getResource("resources/jinicon.gif");
      Image iconImage = Toolkit.getDefaultToolkit().getImage(iconURL);
      if (iconImage != null)
        mainFrame.setIconImage(iconImage);

      mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mainFrame.addWindowListener(new JinCloser());

      mainFrame.setVisible(true);
    } catch (Throwable t){
        if (t instanceof ThreadDeath)
          throw (ThreadDeath)t;
        t.printStackTrace();
        System.exit(1);
      }
  }




  /**
   * Are we already showing a closing dialog?
   */

  private static boolean showingCloseDialog = false;




  /**
   * Opens a modal dialog over the closing window, asking the user to confirm
   * he wants to quit. If the user confirms, calls Jin.exit().
   */

  public static void close(){
    if (showingCloseDialog)
      return;
    showingCloseDialog = true;
    int result = JOptionPane.showConfirmDialog(mainFrame, "Close Jin?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
    showingCloseDialog = false;
    switch (result){
      case JOptionPane.CANCEL_OPTION:
      case JOptionPane.CLOSED_OPTION:
        return;
      case JOptionPane.OK_OPTION:
        Jin.exit();
        break;
      default:
        System.err.println("Unknown option type: "+result);
    }
  }





  /**
   * This method is called to execute the exiting from Jin procedure. It's
   * responsible for releasing all the resources allocated by this class, saving
   * the properties etc. The last operation of this method is a call to 
   * System.exit() with the value 0.
   */

  private static void exit(){
    try{
      mainFrame.exiting();

      // Save the l&f class name
      setProperty("default.lf", UIManager.getLookAndFeel().getClass().getName());


      boolean isFrameOK = true;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle frameBounds = mainFrame.getBounds();
      if (frameBounds.x+frameBounds.width < 50)
        isFrameOK = false;
      if (frameBounds.y < -10)
        isFrameOK = false;
      if (frameBounds.width < 30)
        isFrameOK = false;
      if (frameBounds.height < 40)
        isFrameOK = false;
      if (frameBounds.x > screenSize.width - 10)
        isFrameOK = false;
      if (frameBounds.y > screenSize.height - 20)
        isFrameOK = false;

      // Save the bounds of the main frame.
      if (isFrameOK)
        setProperty("frame.bounds", StringEncoder.encodeRectangle(frameBounds));

      OutputStream propsOut = new FileOutputStream(new File(jinUserHome, "user.properties"));
      userProps.save(propsOut, "Jin chess client properties");
      propsOut.close();
      System.exit(0);
    } catch (IOException e){
        System.err.println("Unable to complete exit procedure:");
        e.printStackTrace();
      }
  }




  /**
   * This class is a WindowListener responsible for confirming that the user
   * wants to close Jin, and if the user confirms, calling Jin.exit().
   *
   * @see Jin#exit()
   */

  private static class JinCloser extends WindowAdapter{

    /**
     * Calls Jin.close()
     */

    public void windowClosing(WindowEvent evt){
      Jin.close();
    }

  }

}