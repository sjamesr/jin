/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import free.jin.plugin.PluginInfo;
import free.util.PlatformUtils;


/**
 * An implementation of a JinContext using a JApplet.
 */
 
public class AppletJinContext implements JinContext{
  
  
  
  /**
   * The applet we're using.
   */
   
  private final JApplet applet;
  

  
  /**
   * The application properties.
   */
  
  private final Properties appProps;
  
  
  
  /**
   * User preferences.
   */

  private final Preferences userPrefs;
  
  
  
  /**
   * The server we're connecting to.
   */
   
  private final Server server;
  
  
  
  /**
   * The plugins we'll be using.
   */
   
  private final PluginInfo [] plugins;
  
  

  /**
   * A list of <code>User</code> object representing known accounts.
   */

  private final DefaultListModel users;
  
  

  /**
   * The main Jin frame.
   */

  private final JFrame mainFrame;
  
  

  /**
   * The UI provider.
   */

  private final UIProvider uiProvider;



  /**
   * The connection manager.
   */

  private final ConnectionManager connManager;
  

  
  /**
   * Creates a new instance of <code>AppletJinContext</code> with the specified
   * <code>JApplet</code>.
   */
  
  public AppletJinContext(JApplet applet) throws IOException,
      ClassNotFoundException, InstantiationException, IllegalAccessException{
    
    if (applet == null)
      throw new IllegalArgumentException("Applet argument may not be null");
    
    this.applet = applet;
    
    // Load application properties.
    appProps = JinUtilities.loadAppProps();
    
    // Load user's preferences.
    userPrefs = Preferences.load(applet, "user");
    
    // Load the server we'll be connecting to.
    server = loadServer();
    
    // Load the plugins we'll be running.
    plugins = loadPlugins();
    
    // Load known users (accounts)
    users = loadUsers();
    
    // Restore the look and feel.
    restoreLookAndFeel();
    
    // Apply Swing fixes
    fixSwing();

    // Create the main frame
    mainFrame = createMainFrame();
    
    // Restore the main frame geometry
    JinUtilities.restoreFrameGeometry(mainFrame, userPrefs, "frame");
    
    // Create the UI manager
    uiProvider = new InternalFramesUIProvider(this, TopLevelContainer.getFor(mainFrame, mainFrame.getTitle()));

    // Create the connection manager
    connManager = new ConnectionManager(this);
  }
  
  
  
  /**
   * Loads the server we'll be connecting to.
   */
   
  private Server loadServer() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException{
        
    String className = applet.getParameter("server.classname");
    if (className == null)
      throw new IllegalStateException("No server.classname parameter specified");
    
    Server server = (Server)Class.forName(className).newInstance();
    server.setGuestUser(loadUser("guest", server));
    
    URL documentBase = applet.getDocumentBase(); 
    server.setHost("file".equals(documentBase.getProtocol()) ?
      "localhost" : documentBase.getHost());
      
    String portString = applet.getParameter("port");
    if (portString != null) 
      server.setPort(Integer.parseInt(portString));
    
    return server;
  }
  
  
  
  /**
   * Loads the plugins we'll be using.
   */
   
  private PluginInfo [] loadPlugins() throws IOException, ClassNotFoundException{
    String pluginsCount = applet.getParameter("plugins.count");
    if (pluginsCount == null)
      throw new IllegalStateException("No plugins.count parameter specified");
    
    PluginInfo [] plugins = new PluginInfo[Integer.parseInt(pluginsCount)];
    
    for (int i = 0; i < plugins.length; i++){
      String className = applet.getParameter("plugins." + i + ".classname");
      if (className == null)
        throw new IllegalStateException("Missing classname for plugin No. " + i);

      // We should actually read the definition file here.
      // Currently (30.07.2004) the definition file only contains the class name
      // of the plugin, so we don't really need it, but it might contain
      // additional information in the future, which we might need to read.
      // It seems that we can't access the definition file when running as an
      // applet because Class.getResourceAsStream("/definition") will return
      // the first "definition" file it sees on the applet classpath (as
      // specified by the ARCHIVE tag.
      
      Class pluginClass = Class.forName(className);

      InputStream pluginPrefsIn = pluginClass.getResourceAsStream("preferences");
      Preferences pluginPrefs = (pluginPrefsIn == null ? Preferences.createNew() : Preferences.load(pluginPrefsIn));
  
      if (pluginPrefsIn != null)
        pluginPrefsIn.close();
  
      plugins[i] = new PluginInfo(pluginClass, pluginPrefs);
    }
    
    return plugins;
  }
  
  
  
  /**
   * Loads the user with the specified name.
   */
   
  private User loadUser(String username, Server server){
     Preferences prefs = Preferences.load(applet, "users." + username);
     Hashtable files = new Hashtable(); // TODO: Add loading/saving files 
     
     return new User(server, username, prefs, files);
  }
  
  
  
  /**
   * Loads known users (accounts).
   */
   
  private DefaultListModel loadUsers(){
    DefaultListModel users = new DefaultListModel();
    UsernamePolicy policy = server.getUsernamePolicy();
    
    int accountsCount = userPrefs.getInt("accounts.count", 0);
    
    for (int i = 0; i < accountsCount; i++){
      String username = userPrefs.getString("accounts." + i + ".username");
      if (username == null)
        throw new IllegalStateException("No username specified for account No. " + i);
      
      User user = loadUser(username, server);

      // Skip if the user wasn't loaded or is a guest
      if ((user != null) && !policy.isSame(policy.getGuestUsername(), user.getUsername()))
        users.addElement(user);
    }
    
    return users;
  }
  
  
  
  /**
   * Sets the current look and feel to the one specified in user preferences.
   */

  private void restoreLookAndFeel(){
    String lfClassName = userPrefs.getString("lf.default", UIManager.getSystemLookAndFeelClassName());
    try{
      UIManager.setLookAndFeel(lfClassName);
    } catch (Exception e){}
  }
  


  /**
   * Applies various swing fixes.
   */
   
  private static void fixSwing(){
    try{
      Class.forName("free.workarounds.SwingFix");
    } catch (ClassNotFoundException e){
        e.printStackTrace();
      }
  }
  
  
  
  /**
   * Creates and configures the main Jin frame.
   */

  private JFrame createMainFrame(){
    JFrame frame = new JFrame();
    
    frame.setTitle(appProps.getProperty("frame.title", "Jin"));
    frame.setIconImage(frame.getToolkit().getImage(getClass().getResource("resources/icon.gif")));
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent evt){
        quit(true);
      }
    });

    return frame;
  }
  
  
  
  
  /**
   * Starts Jin.
   */

  public void start(){
    mainFrame.addWindowListener(new WindowAdapter(){
      public void windowOpened(WindowEvent evt){
        mainFrame.removeWindowListener(this);
        
        // Workaround - otherwise menu activation shortcuts don't work
        // immediately. Under OS X, in native menubar mode, this actually breaks things.
        if ((mainFrame.getJMenuBar() != null) && !PlatformUtils.isMacOSX())
          mainFrame.getJMenuBar().requestFocus();
        
        connManager.start();
      }
    });
    mainFrame.setVisible(true);
  }
  
  
  
  /**
   * Returns the user preferences for this application.
   */

  public Preferences getPrefs(){
    return userPrefs;
  }

  

  /**
   * TODO: Implement this.
   */

  public ClassLoader [] loadResources(String resourceType){
    return new ClassLoader[0];
  }



  /**
   * Quits the application, possibly asking the user to confirm quitting first.
   */

  public void quit(boolean askToConfirm){
    Object result = askToConfirm ? 
      OptionPanel.confirm(uiProvider, "Quit", "Quit Jin?", OptionPanel.OK) : OptionPanel.OK;
    
    if (result == OptionPanel.OK)
      shutdown();
  }
    

 
  /**
   * Saves preferences and closes the main window.
   */

  public void shutdown(){
    connManager.closeSession();
    JinUtilities.saveFrameGeometry(mainFrame, userPrefs, "frame");
    
    storeUserPrefs();

    mainFrame.dispose();
  }



  /**
   * Returns the application name.
   */

  public String getAppName(){
    return appProps.getProperty("app.name");
  }



  /**
   * Returns the application version.
   */

  public String getAppVersion(){
    return appProps.getProperty("app.version");
  }



  /**
   * Returns an array containing a single element - the server we're connecting
   * to.
   */
  
  public Server [] getServers(){
    return new Server[]{server};
  }



  /**
   * Returns the plugins associated with the specified server (which must be the
   * server we're connecting to).
   */
  
  public PluginInfo [] getPlugins(Server server){
    if (server != this.server)
      throw new IllegalArgumentException("Unknown server: " + server);
    
    return (PluginInfo [])plugins.clone();
  }



  /**
   * Returns the list of known users (accounts). 
   */
  
  public ListModel getUsers(){
    return users;
  }



  /**
   * Adds the specified user to the list of known users.
   */

  public boolean addUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (user.isGuest())
      throw new IllegalArgumentException("user may not be a guest");

    users.addElement(user);

    return true;
  }



  /**
   * TODO: Implement this. 
   */
  
  public boolean storeUser(User user){
    return false;
  }



  /**
   * Removes the user from the list of known users (accounts). Returns whether
   * successful.
   */

  public boolean removeUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (user.isGuest())
      throw new IllegalArgumentException("user may not be a guest");

    users.removeElement(user);

    // TODO: Implement actual removing, if needed.    
    
    return true;
  }



  /**
   * Returns our ui provider. 
   */
  
  public UIProvider getUIProvider(){
    return uiProvider;
  }



  /**
   * Returns the connection manager for this instance of Jin.
   */

  public ConnectionManager getConnManager(){
    return connManager;
  }
  
  

  /**
   * Stores the user preferences.
   */

  private void storeUserPrefs(){
    // TODO: Implement this.
  }

  
   
}