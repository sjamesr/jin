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
import java.io.*;
import free.util.*;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.Hashtable;
import free.util.zip.ZipClassLoader;
import free.util.zip.ZipURLStreamHandler;
import free.jin.plugin.PluginInfo;


/**
 * The main class for running Jin as an application.
 */

public class JinMain implements JinContext{



  /**
   * Set a URLStreamHandlerFactory which knows about our "zip" protocol.
   */

  static{
    URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory(){
      public URLStreamHandler createURLStreamHandler(String protocol){
        if (protocol.equals("zip"))
          return new ZipURLStreamHandler();
        return null;
      }
    });
  }



  /**
   * Causes the <code>free.workarounds.SwingFix</code> class to be loaded.
   */

  static{
    try{
      Class.forName("free.workarounds.SwingFix");
    } catch (ClassNotFoundException e){
        e.printStackTrace();
      }
  }



  /**
   * The current directory. This should be the Jin directory.
   */

  private static final File jinDir = new File(System.getProperty("user.dir"));



  /**
   * The directory where all jin settings files are kept.
   */

  private final File prefsDir;



  /**
   * Application (Jin) properties.
   */

  private final Properties appProps;



  /**
   * User preferences.
   */

  private final Preferences userPrefs;



  /**
   * Our class loader. Initially it has no delegates - they are added as the
   * various jars for plugins, server definitions etc. are loaded. The structure
   * of the classloader is:
   * <pre>
   *  _______________ _______________ _______________
   *  |             | |             | |             |
   *  | plugin1.jar | | plugin2.jar | | plugin3.jar |   
   *  |_____________| |_____________| |_____________|   
   *         |               |               |          
   *         |               V               |          
   *         |        _______________        |          
   *         |------> |             | <------|          
   *                  |    main     |                  
   *         ######## |_____________| ########                                 
   *         #               |               #
   *         V               |               V          ______________________ 
   *  _______________        |        _______________   |                    | 
   *  |             |        |        |             |   | Legend:            | 
   *  | server1.jar |        |        | server2.jar |   |                    | 
   *  |_____________|        |        |_____________|   |   ----> : parent   | 
   *         |               |               |          |   ####> : delegate | 
   *         |               V               |          |____________________| 
   *         |        _______________        |
   *         |------> |             | <------|
   *                  |    libs     |
   *         |------> |_____________| <------| 
   *         |          #   #  ^  #          | 
   *   ______|_____     #   #  |  #     _____|______
   *   |          | <####   #  |  ####> |          |
   *   | lib1.jar |         V  |        | lib3.jar |
   *   |__________|     _______|____    |__________|
   *                    |          |
   *                    | lib2.jar |
   *                    |__________|
   *                    
   * </pre>
   *
   * This structure allows:
   * <ul>
   *   <li> A plugin to access all the servers and libs but not other plugins.
   *   <li> A server to access all the libs but not other servers or plugins.
   *   <li> A lib to access all the other libs but not servers or plugins.
   * </ul>
   *
   * This variable is a reference to the top delegating classloader.
   */

  private final DelegatingClassLoader mainLoader;



  /**
   * The libraries' classloader. See the documentation of the
   * <code>mainClassLoader</code> instance variable for more details.
   */

  private final ChildClassLoader libsLoader;



  /**
   * A list of <code>Server</code> objects representing the supported servers.
   */

  private final Server [] servers;


  
  /**
   * Maps <code>Server</code> objects to arrays of <code>PluginInfo</code>
   * instances describing plugins for that server.
   */

  private final Hashtable serversToPlugins;



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
   * Creates a new instance of <code>JinMain</code> which will use preferences
   * in the specified directory.
   */

  public JinMain(File prefsDir) throws IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException{

    this.prefsDir = prefsDir;

    // Load application properties
    appProps = loadAppProps();

    // Load user preferences
    userPrefs = loadUserPrefs();
    
    // Create our own special classloader. The delegate classloaders will be
    // added as the various jars (for plugins, server definitions) are loaded.
    libsLoader = createLibsClassLoader();
    mainLoader = new DelegatingClassLoader(libsLoader);

    // Load server(s) support
    servers = loadServers();

    // Load plugins
    serversToPlugins = loadPlugins();

    // Load known users
    users = loadUsers();

    // Install any extra look and feels we're using
    installExtraLookAndFeels();

    // Restore the look and feel
    restoreLookAndFeel();

    // Create the main frame
    mainFrame = createMainFrame();
    
    // Restore the main frame geometry
    restoreMainFrameGeometry();

    // Create the UI manager
    uiProvider = new InternalFramesUIProvider(this, TopLevelContainer.getFor(mainFrame));

    // Create the connection manager
    connManager = new ConnectionManager(this);
  }



  /**
   * Starts Jin.
   */

  public void start(){
    mainFrame.addWindowListener(new WindowAdapter(){
      public void windowOpened(WindowEvent evt){
        mainFrame.removeWindowListener(this);
        
        // Workaround - otherwise menu activation shortcuts don't work
        // immediately
        if (mainFrame.getJMenuBar() != null)
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
   * Returns all the resources for the specified resource type. Resources are
   * assumed to be zip or jar files and are looked up in two directories:
   * <code>jinDir/resources/resType</code> and
   * <code>prefsDir/resources/resType</code>.
   */
   
   public ClassLoader [] loadResources(String resType){
     Vector resources = new Vector();
     
     File jinResDir = new File(new File(jinDir, "resources"), resType);
     File userResDir = new File(new File(prefsDir, "resources"), resType);
                                
     loadResources(jinResDir, resources);
     loadResources(userResDir, resources);
     
     ClassLoader [] resArr = new ClassLoader[resources.size()];
     resources.copyInto(resArr);
     
     return resArr;
   }
   
   
   
   /**
    * Loads resources from the specified directory, adding them to the
    * specified <code>Vector</code>.
    */
    
   private void loadResources(File dir, Vector v){
     String [] filenames = dir.list(new ExtensionFilenameFilter(new String[]{".jar", ".zip"}));
     if (filenames == null)
       return;
     
     for (int i = 0; i < filenames.length; i++){
       File resource = new File(dir, filenames[i]);
       try{
         v.addElement(new ZipClassLoader(resource));
       } catch (IOException e){
           System.out.println("Failed to load resource: " + resource);
           e.printStackTrace();
         }
     }
   }
    

 
  /**
   * Saves preferences and calls <code>System.exit(0)</code>.
   */

  public void shutdown(){
    connManager.closeSession();
    saveMainFrameGeometry();
    saveLookAndFeel();
    
    storeUserPrefs();

    mainFrame.dispose();
    System.exit(0);
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
   * Returns the list of supported servers.
   */

  public Server [] getServers(){
    return (Server [])servers.clone();
  }



  /**
   * Returns the list of plugins for the specified server.
   */

  public PluginInfo [] getPlugins(Server server){
    PluginInfo [] plugins = (PluginInfo [])serversToPlugins.get(server);
    return (PluginInfo [])plugins.clone();
  }



  /**
   * Returns the list of known user accounts.
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
   * Returns the directory where we store the information of the specified user.
   */

  private File dirForUser(User user){
    File usersPrefsDir = new File(prefsDir, "accounts");
    File serverSpecificDir = new File(usersPrefsDir, user.getServer().getId());
    return new File(serverSpecificDir, user.getUsername());
  }



  /**
   * Stores the preferences of the specified user.
   */

  public boolean storeUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");

    // We don't need a special case for guest users because they are stored
    // in the same manner as everyone else.

    String username = user.getUsername();

    File userDir = dirForUser(user);

    if (!(userDir.isDirectory() || userDir.mkdirs())){
      OptionPanel.error(uiProvider, "Saving Account Error",
          "Unable to create directory:\n" + userDir);
      return false;
    }

    Properties props = new Properties();
    props.put("serverId", user.getServer().getId());
    props.put("username", username);

    File propsFile = new File(userDir, "properties");
    try{
      OutputStream out = new FileOutputStream(propsFile);
      props.save(out, "");
      out.close();
    } catch (IOException e){
        OptionPanel.error(uiProvider, "Saving Account Error",
            "Error writing user properties to file :\n" + propsFile);
        return false;
      }

    File prefsFile = new File(userDir, "preferences");
    try{
      OutputStream out = new FileOutputStream(prefsFile);
      user.getPrefs().save(out);
      out.close();
    } catch (IOException e){
        OptionPanel.error(uiProvider, "Saving Account Error",
            "Error writing user preferences to file :\n" + prefsFile);
        return false;
      }

    File filesFile = new File(userDir, "files"); 
    try{
      storeUserFiles(user.getFilesMap(), filesFile);
    } catch (IOException e){
        OptionPanel.error(uiProvider, "Saving Account Error",
            "Error writing user files to file :\n" + filesFile);
        return false;
      }

    return true;
  }



  /**
   * Stores the specified map of filenames to <code>MemoryFile</code> objects
   * into the specified file.
   */

  private void storeUserFiles(Hashtable files, File filesFile) throws IOException{
    DataOutputStream out = 
      new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filesFile)));
    out.writeInt(files.size());
    Enumeration filenames = files.keys();
    while (filenames.hasMoreElements()){
      String filename = (String)filenames.nextElement();
      MemoryFile memFile = (MemoryFile)files.get(filename);
      out.writeUTF(filename);
      synchronized(memFile){
        out.writeInt(memFile.getSize());
        memFile.writeTo(out);
      }
    }
    out.close();
  }




  /**
   * Removes the specified user from the list of known users.
   */

  public boolean removeUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (user.isGuest())
      throw new IllegalArgumentException("user may not be a guest");

    users.removeElement(user);

    File dir = dirForUser(user);
    if (!IOUtilities.rmdir(dir)){
      OptionPanel.error(uiProvider, "Deleting Account Error",
          "Unable to delete directory :\n" + dir);
      return false;
    }

    return true;
  }



  /**
   * Returns the ui provider.
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
   * Loads the application properties.
   */

  private Properties loadAppProps() throws IOException{
    return IOUtilities.loadProperties(getClass().getResourceAsStream("resources/app.props"));
  }



  /**
   * Loads the application-wide user preferences.
   */

  private Preferences loadUserPrefs() throws IOException{
    File prefsFile = new File(prefsDir, "user.prefs");
    if (!prefsFile.exists())
      return Preferences.createNew();

    return Preferences.load(prefsFile);
  }



  /**
   * Creates the classloader that will load the various libraries required by
   * the plugins and server support.
   */

  private DelegatingClassLoader createLibsClassLoader() throws IOException{
    DelegatingClassLoader libsLoader = new DelegatingClassLoader();
    File libDir = new File(jinDir, "libs");
    checkDirectoryExists(libDir);

    String [] jars = libDir.list(new ExtensionFilenameFilter(".jar"));
    for (int i = 0; i < jars.length; i++){
      File jar = new File(libDir, jars[i]);
      libsLoader.addDelegate(new ZipClassLoader(jar, libsLoader));
    }

    return libsLoader;
  }



  /**
   * Loads the <code>Server</code> objects that implement support for the
   * various servers.
   */

  private Server [] loadServers() throws IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException{

    File serversDir = new File(jinDir, "servers");
    checkDirectoryExists(serversDir);

    String [] jars = serversDir.list(new ExtensionFilenameFilter(".jar"));
    if (jars.length == 0)
      throw new IllegalStateException("No server jar files found in:\n" + serversDir);

    Server [] servers = new Server[jars.length];
    for (int i = 0; i < jars.length; i++)
      servers[i] = loadServer(new File(serversDir, jars[i]));

    return servers;
  }



  /**
   * If the specified file does not exist or does not denote a directory, throws
   * an appropriate <code>FileNotFoundException</code>. Otherwise simply
   * returns.
   */

  private void checkDirectoryExists(File file) throws FileNotFoundException{
    if (!(file.exists() && file.isDirectory()))
      throw new FileNotFoundException("Can't find directory:\n" + file + "\n" +
        "The most likely reason is that you are not running Jin from its directory.");
  }


  
  /**
   * Loads a <code>Server</code> from the specified jar.
   */

  private Server loadServer(File jar) throws IOException,
      ClassNotFoundException, InstantiationException, IllegalAccessException{

    if (!jar.isFile())
      throw new FileNotFoundException(jar + " does not exist or is a directory");

    ChildClassLoader loader = new ZipClassLoader(jar, libsLoader);

    InputStream serverDefIn = loader.getResourceAsStream("definition");
    if (serverDefIn == null)
      throw new FileNotFoundException("Unable to find server definition file in " + jar);
    Properties serverDef = IOUtilities.loadProperties(serverDefIn);

    String classname = serverDef.getProperty("classname");
    if (classname == null)
      throw new IOException("Server definition file in " + jar + " does not contain a classname property");
    Server server = (Server)loader.loadClass(classname).newInstance();

    mainLoader.addDelegate(loader);

    File guestDir = new File(new File(new File(prefsDir, "accounts"), server.getId()),
      server.getUsernamePolicy().getGuestUsername());
    server.setGuestUser(loadUser(guestDir, server));
    return server;
  }



  /**
   * Loads the plugin classes for all servers. Returns a hashtable that maps
   * <code>Server</code> objects to arrays of Class objects representing the
   * plugin classes for that server.
   */

  private Hashtable loadPlugins() throws IOException{
    Hashtable plugins = new Hashtable();
    for (int i = 0; i < servers.length; i++)
      plugins.put(servers[i], new Vector());

    // plugins that are shared between all users - usually the ones that come with Jin
    loadPlugins(plugins, new File(jinDir, "plugins")); 

    // user specific plugins, from his own preferences directory
    loadPlugins(plugins, new File(prefsDir, "plugins"));


    // Convert the Server->Vector map to Server->PluginInfo[] map
    Hashtable result = new Hashtable();
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];
      Vector pluginsVector = (Vector)plugins.get(server);
      PluginInfo [] pluginsArray = new PluginInfo[pluginsVector.size()];
      pluginsVector.copyInto(pluginsArray);

      result.put(server, pluginsArray);
    }

    return result;
  }



  /**
   * Loads plugins from the specified directory into the specified hashtable.
   */

  private void loadPlugins(Hashtable plugins, File dir) throws IOException{
    if (!dir.isDirectory())
      return;

    String [] jars;
    FilenameFilter jarsFilter = new ExtensionFilenameFilter(".jar");

    // Load plugins that are for all servers
    jars = dir.list(jarsFilter);
    for (int i = 0; i < jars.length; i++){
      PluginInfo pluginInfo = loadPluginInfo(new File(dir, jars[i]));
      if (pluginInfo == null)
        continue;

      for (int j = 0; j < servers.length; j++)
        ((Vector)plugins.get(servers[j])).addElement(pluginInfo);
    }


    // Load server specific plugins
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];

      File serverSpecificDir = new File(dir, server.getId());
      if (!serverSpecificDir.isDirectory())
        continue;

      jars = serverSpecificDir.list(jarsFilter);
      for (int j = 0; j < jars.length; j++){
        PluginInfo pluginInfo = loadPluginInfo(new File(serverSpecificDir, jars[j]));
        if (pluginInfo == null)
          continue;

        ((Vector)plugins.get(server)).addElement(pluginInfo);
      }
    }
  }



  /**
   * Loads a single plugin description from the specified jar. Returns 
   * <code>null</code> if unable to load the plugin.
   */

  private PluginInfo loadPluginInfo(File jar) throws IOException{
    if (!jar.isFile())
      return null;

    ChildClassLoader loader = new ZipClassLoader(jar, mainLoader);

    InputStream pluginDefIn = loader.getResourceAsStream("definition");
    if (pluginDefIn == null){
      System.err.println(jar + " does not contain a plugin definition file");
      return null;
    }
    Properties pluginDef = IOUtilities.loadProperties(pluginDefIn);

    String classname = pluginDef.getProperty("classname");
    if (classname == null){
      System.out.println("The plugin definition file in " + jar + " does not contain a classname property");
      return null;
    }

    InputStream pluginPrefsIn = loader.getResourceAsStream("preferences");
    Preferences pluginPrefs = (pluginPrefsIn == null ? Preferences.createNew() : Preferences.load(pluginPrefsIn));
    pluginPrefsIn.close();

    try{
      return new PluginInfo(loader.loadClass(classname), pluginPrefs);
    } catch (ClassNotFoundException e){
        e.printStackTrace();
        return null;
      }
  }

  

  /**
   * Loads the known user accounts.
   */

  private DefaultListModel loadUsers() throws IOException{
    File usersDir = new File(prefsDir, "accounts");
    if (!usersDir.exists())
      return new DefaultListModel();

    Vector usersVector = new Vector();
    for (int i = 0; i < servers.length; i++){
      Server server = servers[i];
      File serverSpecificUserDir = new File(usersDir, server.getId());
      if (!serverSpecificUserDir.exists())
        continue;

      UsernamePolicy policy = server.getUsernamePolicy();
      String [] userDirs = serverSpecificUserDir.list();
      for (int j = 0; j < userDirs.length; j++){
        File userDir = new File(serverSpecificUserDir, userDirs[j]);
        User user = loadUser(userDir, null);

        // Skip if the user wasn't loaded or is a guest
        if ((user != null) && !policy.isSame(policy.getGuestUsername(), user.getUsername()))
          usersVector.addElement(user);
      }
    }

    DefaultListModel users = new DefaultListModel();
    for (int i = 0; i < usersVector.size(); i++)
      users.addElement(usersVector.elementAt(i));

    return users;
  }



  /**
   * Loads a User object from the specified directory. Returns <code>null</code>
   * if any required information about the user is missing. The specified server
   * argument indicates the server for the loaded user - it may be null, in
   * which case the server is determined by the serverId property of the user.
   */

  private User loadUser(File dir, Server server) throws IOException{
    if (!dir.isDirectory())
      return null;

    File propsFile = new File(dir, "properties");
    File prefsFile = new File(dir, "preferences");
    File filesFile = new File(dir, "files");

    if (!(propsFile.isFile() && prefsFile.isFile()))
      return null;

    Properties props = IOUtilities.loadProperties(propsFile);
    Preferences prefs = Preferences.load(prefsFile);
    Hashtable files = loadUserFiles(filesFile);

    // We don't use the directories' names for server id and username because
    // we don't know whether the filesystem allows filenames to be what servers
    // allow them to be.
    String serverId = props.getProperty("serverId");
    String username = props.getProperty("username");

    if ((serverId == null) || (username == null))
      return null;

    if (server == null)
      server = JinUtilities.getServerById(this, serverId);

    return new User(server, username, prefs, files);
  }



  /**
   * Loads the user (memory) files from the specified file. Returns a hashtable
   * mapping filenames to <code>MemoryFile</code> objects.
   */

  private Hashtable loadUserFiles(File file) throws IOException{
    Hashtable files = new Hashtable();
    if (!file.isFile())
      return files;

    DataInputStream in =
      new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    int filesCount = in.readInt();
    for (int i = 0; i < filesCount; i++){
      String filename = in.readUTF();
      int length = in.readInt();
      byte [] data = IOUtilities.read(in, length);

      files.put(filename, new MemoryFile(data));
    }

    return files;
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
        Object result =
          OptionPanel.confirm(uiProvider, "Really Close?", "Close Jin?", OptionPanel.OK);

        if (result == OptionPanel.OK)
          shutdown();
      }
    });

    return frame;
  }


  
  /**
   * Returns <code>true</code> if the specified frame bounds are reasonably
   * placed on a screen of the specified dimensions. This is used to avoid
   * situations where a frame is displayed outside of the screen where the user
   * can't change its size and/or move it (can happen for example if the
   * resolution is changed between runs).
   */

  private boolean frameBoundsOk(Dimension screenSize, Rectangle frameBounds){
    if (frameBounds.x + frameBounds.width < 50)
      return false;
    if (frameBounds.y < -10)
      return false;
    if (frameBounds.width < 30)
      return false;
    if (frameBounds.height < 40)
      return false;
    if (frameBounds.x > screenSize.width - 10)
      return false;
    if (frameBounds.y > screenSize.height - 20)
      return false;

    return true;
  }


  
  /**
   * Saves the main frame geometry into user preferences.
   */

  private void saveMainFrameGeometry(){
    userPrefs.setRect("frame.bounds", mainFrame.getBounds());
    
    // Save maximized state
    int state = AWTUtilities.getExtendedFrameState(mainFrame);
    userPrefs.setBool("frame.maximized.vert", (state & Frame.MAXIMIZED_VERT) != 0);
    userPrefs.setBool("frame.maximized.horiz", (state & Frame.MAXIMIZED_HORIZ) != 0);
  }
  
  
  
  /**
   * Restores the main frame geometry from user preferences.
   */
   
  private void restoreMainFrameGeometry(){
    Dimension screenSize = mainFrame.getToolkit().getScreenSize();
    Rectangle defaultFrameBounds = new Rectangle(
      screenSize.width/16, screenSize.height/16, screenSize.width*7/8, screenSize.height*7/8);
      
    // Restore bounds      
    Rectangle frameBounds = userPrefs.getRect("frame.bounds", defaultFrameBounds);
    frameBounds = frameBoundsOk(screenSize, frameBounds) ? frameBounds : defaultFrameBounds;
    mainFrame.setBounds(frameBounds);

    // Bugfix for Java bug 4464714 - setExtendedState only works once the
    // the window is realized
    mainFrame.pack();

    // Restore maximized state
    boolean vertMaximized = userPrefs.getBool("frame.maximized.vert", false);
    boolean horizMaximized = userPrefs.getBool("frame.maximized.horiz", false);
    int state = ((vertMaximized ? Frame.MAXIMIZED_VERT : 0) | (horizMaximized ? Frame.MAXIMIZED_HORIZ : 0));
    AWTUtilities.setExtendedFrameState(mainFrame, state);
  }



  /**
   * Installs any extra look and feels Jin is using.
   */

  private void installExtraLookAndFeels(){
    int extraLooksCount = Integer.parseInt(appProps.getProperty("lf.extra.count", "0"));
    for (int i = 0; i < extraLooksCount; i++){
      String name = appProps.getProperty("lf.extra." + i + ".name");
      String className = appProps.getProperty("lf.extra." + i + ".class");
      try{
        Class.forName(className);
        UIManager.installLookAndFeel(name, className);
      } catch (ClassNotFoundException e){}
    }
  }



  /**
   * Saves the currently used look and feel into user preferences.
   */

  private void saveLookAndFeel(){
    userPrefs.setString("lf.default", UIManager.getLookAndFeel().getClass().getName());
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
   * Stores the preferences on the disk.
   */

  private void storeUserPrefs(){
    File userPrefsFile = new File(prefsDir, "user.prefs");
    try{
      userPrefs.save(userPrefsFile);
    } catch (IOException e){
        OptionPanel.error(uiProvider, "Saving Preferences Error",
          "Unable to save preferences into:\n" + userPrefsFile);
      }
  }



  /**
   * Creates an in instance of this class and invokes the <code>start</code>
   * method.
   */

  public static void main(String [] args){
    try{
      File prefsDir = new File(System.getProperty("user.home"), ".jin");
      createPreferencesDir(prefsDir);

      JinMain app = new JinMain(prefsDir);
      app.start();
    } catch (Throwable t){
        if (t instanceof ThreadDeath)
          throw (ThreadDeath)t;
        t.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error type: " + t.getClass().getName() + "\n" +
          "Error message: " + t.getMessage(), "Jin launch error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
  }



  /**
   * If the specified directory does not exist, attempts to create it. If
   * creating the directory fails, throws an appropriate IOException.
   */

  private static void createPreferencesDir(File dir) throws IOException{
    // delete the old-style preference files, if they exist
    if (new File(dir, "user.properties").exists())
      IOUtilities.rmdir(dir);

    if (!dir.exists()){
      if (!dir.mkdirs())
        throw new IOException("Unable to create preferences directory: " + dir);
    }
    else if (!dir.isDirectory())
      throw new IOException(dir.toString() + " exists but is not a directory");
  }



}
