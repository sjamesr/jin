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
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;
import free.jin.action.ActionInfo;
import free.util.PlatformUtils;
import free.util.IOUtilities;
import free.util.BrowserControl;


/**
 * An implementation of a JinContext using a JApplet.
 */
 
public class AppletJinContext implements JinContext{
  
  
  
  /**
   * The applet we're running as.
   */
   
  private final JinApplet applet;
  

  
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
   * The actions we'll be using.
   */
   
  private final ActionInfo [] actions;
  
  
  
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
   * <code>JinApplet</code>.
   */
  
  public AppletJinContext(JinApplet applet) throws IOException,
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
    
    // Load the actions we'll be using.
    actions = loadActions();
    
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
   * Loads the actions we'll be using.
   */
   
  private ActionInfo [] loadActions() throws IOException, ClassNotFoundException{
    String actionsCount = applet.getParameter("actions.count");
    if (actionsCount == null)
      throw new IllegalStateException("No actions.count parameter specified");
    
    ActionInfo [] actions = new ActionInfo[Integer.parseInt(actionsCount)];
    
    for (int i = 0; i < actions.length; i++){
      String className = applet.getParameter("actions." + i + ".classname");
      if (className == null)
        throw new IllegalStateException("Missing classname for action No. " + i);

      // See the long comment about the definition file in loadPlugins
      
      Class actionClass = Class.forName(className);

      InputStream actionPrefsIn = actionClass.getResourceAsStream("preferences");
      Preferences actionPrefs = (actionPrefsIn == null ? Preferences.createNew() : Preferences.load(actionPrefsIn));
  
      if (actionPrefsIn != null)
        actionPrefsIn.close();
  
      actions[i] = new ActionInfo(actionClass, actionPrefs);
    }
    
    return actions;
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
   * Returns all the resources of the specified type.
   */

  public Resource [] getResources(String resourceType, Plugin plugin){
    String resourcesArg = applet.getParameter("resources." + resourceType);
    if (resourcesArg == null)
      return new Resource[0];
    
    StringTokenizer resourceNames = new StringTokenizer(resourcesArg, " ");
    Resource [] resources = new Resource[resourceNames.countTokens()];
    for (int i = 0; i < resources.length; i++){
      try{
        URL resourceURL = new URL(applet.getCodeBase(), "resources/" + resourceType + "/" 
          + resourceNames.nextToken() + "/");
        resources[i] = loadResource(resourceURL, plugin);
      } catch (IOException e){e.printStackTrace();}
    }
    
    return resources;
  }
  
  
  
  /**
   * Returns the resource with the specified type and id.
   */
   
  public Resource getResource(String type, String id, Plugin plugin){
    try{
      String resourcesArg = applet.getParameter("resources." + type);
      if (resourcesArg == null)
        return null;
      
      StringTokenizer resourceNames = new StringTokenizer(resourcesArg, " ");
      while (resourceNames.hasMoreTokens()){
        String resource = resourceNames.nextToken();
        int slashIndex = resource.indexOf("/");
        String resourceId = slashIndex == -1 ? resource : resource.substring(slashIndex + 1);
        if (id.equals(resourceId)){
          URL resourceURL = new URL(applet.getCodeBase(), "resources/" + type + "/" + resource + "/");
          return loadResource(resourceURL, plugin);
        }
      }
    } catch (IOException e){e.printStackTrace();}
    
    return null;
  }
  
  
  
  
  /**
   * Loads a single resource from the specified URL.
   */
   
  private Resource loadResource(URL url, Plugin plugin) throws IOException{
    URL defURL = new URL(url, "definition");
    IOUtilities.cacheURL(defURL);
    
    Properties def = IOUtilities.loadProperties(defURL);
    String classname = def.getProperty("classname");
    if (classname == null)
      return null;
    
    try{
      // We need to load it with the plugin's classloader because the
      // resource may be of a type which is a part of the plugin.
      Class resourceClass = plugin.getClass().getClassLoader().loadClass(classname);
      Resource resource = (Resource)resourceClass.newInstance();
      resource.load(url, plugin);
      
      return resource;
    } catch (ClassNotFoundException e){e.printStackTrace(); return null;}
      catch (InstantiationException e){e.printStackTrace(); return null;}
      catch (IllegalAccessException e){e.printStackTrace(); return null;}
  }
  
  
  
  /**
   * This method is called by the <code>JinApplet</code> to tell us when the
   * applet's start method was called. We don't do anything because
   * <code>JinApplet</code> has a button which starts/stops us.
   */
   
  void applet_start(){
    
  }
  
  
  
  /**
   * This is called by the <code>JinApplet</code> to tell us when the applet's
   * stop method was called. 
   */
   
  void applet_stop(){
    
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
    
    applet.closed();
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
   * Returns the actions for the specified server (which must be the server
   * we're connecting to).
   */
   
  public ActionInfo [] getActions(Server server){
    if (server != this.server)
      throw new IllegalArgumentException("Unknown server: " + server);
    
    return (ActionInfo [])actions.clone();
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
   * Returns <code>true</code> immediately, since we're uploading all the
   * preferences to the server when the applet is closed. 
   */
  
  public boolean storeUser(User user){
    
    // All the preferences are uploaded when the application is closed.
    return true;
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

    return true;
  }
  
  
  
  /**
   * Returns <code>true</code> iff the applet has been given a
   * <code>savePrefsUrl</code> parameter.
   */
   
  public boolean isSavePrefsCapable(){
    String savePrefsUrl = applet.getParameter("savePrefsUrl");
    return (savePrefsUrl != null) && !"".equals(savePrefsUrl);
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
   * Returns text warning the user about saving his password and asking him to
   * confirm it.
   */
   
  public String getPasswordSaveWarning(){
    boolean isSecure = applet.getDocumentBase().getProtocol().equals("https");
    
    if (isSecure)
      return "Your password will be stored on the server and transferred to the applet\n" +
             "in encrypted form. This is reasonably safe, but your password will still" +
             "be visible via the \"View Page Source\" option in your browser.\n"+
             "Are you sure you want your password saved?";
    else
      return "Your password will be stored on the server and transferred to the applet\n" +
             "as plain text HTML - anyone with access to a router or proxy between your\n" +
             "computer and the server will be able to view your password. This is\n" +
             "dangerous and advised against.\n" +
             "Are you sure you want your password saved?";
  }
  
  
  
  /**
   * The result string of saving the preferences. <code>null</code> if
   * successful or a description of the error if not successful.
   */
   
  private String userPrefsUploadResult = null;
  
  
  
  /**
   * The thread that uploads preferences, <code>null</code> when none.
   */
   
  private Thread uploadThread = null;
  
  
  
  /**
   * The cookieKey.
   */
   
  private String cookieKey = null;
  
  

  /**
   * Stores the user preferences. Returns whether successful.
   */

  private void storeUserPrefs(){
    if (!isSavePrefsCapable())
      return;
    
    if (uploadThread != null)
      return;
    
    // Get the cookieKey
    if (cookieKey == null){
      cookieKey = applet.getParameter("cookieKey");
      
      if (cookieKey == null)
        cookieKey = new AskCookieKeyDialogPanel().askKey();
      
      if ((cookieKey == null) || "".equals(cookieKey))
        return;
    }
    
    userPrefsUploadResult = null;
    
    final OptionPanel infoPanel = new OptionPanel(OptionPanel.INFO, "Uploading Preferences", 
        new Object[]{OptionPanel.CANCEL}, OptionPanel.CANCEL, "Uploading preferences - please wait."){
        
      // We must make sure we are visible before starting the thread.
      // Otherwise the thread might finish before we're visible and the
      // the dialog will never be disposed.
      public void addNotify(){
        super.addNotify();
        
        uploadThread.start();
      }
      
    };
    
    
    uploadThread = new Thread("Preferences-Upload"){
      public void run(){
        try{
          // Add known accounts to the preferences.
          userPrefs.setInt("accounts.count", users.getSize());
          for (int i = 0; i < users.getSize(); i++)
            userPrefs.setString("accounts." + i + ".username", ((User)users.getElementAt(i)).getUsername());
          
          
          URL savePrefsUrl = new URL(applet.getDocumentBase(), applet.getParameter("savePrefsUrl"));
          HttpURLConnection conn = (HttpURLConnection)savePrefsUrl.openConnection();
          conn.setDoOutput(true);
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-type", "application/binary");
          
          DataOutputStream out = new DataOutputStream(conn.getOutputStream());
          out.writeBytes(cookieKey + "\n");
          out.writeBytes("user\n");
          userPrefs.save(out);
          
          for (int i = 0; i < users.getSize(); i++){
            User user = (User)users.getElementAt(i);
            
            out.writeByte('\n');
            out.writeBytes("users." + user.getUsername() + "\n");
            user.getPrefs().save(out);
          }
          
          User guest = server.getGuest();
          out.writeByte('\n');
          out.writeBytes("users.guest\n");
          guest.getPrefs().save(out);
          
          out.writeBytes("Done");
    
          out.close();
          
          conn.connect();
          
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          IOUtilities.pump(conn.getInputStream(), buf);
          String result = new String(buf.toByteArray());
          userPrefsUploadResult =  "".equals(result) ? null : result;
        } catch (MalformedURLException e){e.printStackTrace(); userPrefsUploadResult = e.getMessage();}
          catch (IOException e){e.printStackTrace(); userPrefsUploadResult = e.getMessage();}
          finally{
            if (uploadThread != null){
              uploadThread = null;
              infoPanel.close(null);
            }
          }
        
      }
    };
    
    
    infoPanel.show(uiProvider);
    if (uploadThread != null){ // User pressed "Cancel"
      uploadThread.stop();
      uploadThread = null;
    }
    else if (userPrefsUploadResult != null){
      OptionPanel.error(uiProvider, "Error", 
        "An error occurred while uploading preferences:\n" + userPrefsUploadResult);  
    }
  }
  
  
  
  /**
   * A <code>DialogPanel</code> which asks the user to go to the
   * <code>reserveSpaceUrl</code> and paste his cookieKey.
   */
   
  private class AskCookieKeyDialogPanel extends DialogPanel implements ActionListener{
    
    
    /**
     * The cookie key text field.
     */
     
    private final TextField cookieKeyTF;
    
    
    
    /**
     * The url for reserving space.
     */
     
    private final String reserveSpaceUrl;
    
    
    
    /**
     * Creates the <code>AskCookieKeyDialogPanel</code>.
     */
     
    public AskCookieKeyDialogPanel(){
      setLayout(new BorderLayout(10, 10));

      String url = null;      
      try{
        url = new URL(applet.getDocumentBase(), applet.getParameter("reserveSpaceUrl")).toString();
      } catch (MalformedURLException e){
          e.printStackTrace();
          url = "MalformedURLException";
        }
        
      reserveSpaceUrl = url;
        
      cookieKeyTF = new TextField();
      
      Panel textPanel = new Panel(new GridLayout(4, 1)); 
      textPanel.add(new Label("To be able to save your preferences, you must "));  
      textPanel.add(new Label("first reserve space for them on the server."));
      textPanel.add(new Label("Please visit " + reserveSpaceUrl));
      textPanel.add(new Label("(by clicking the \"Go to URL\" button) and copy the key:"));
      
      add(textPanel, BorderLayout.NORTH);
      
      Panel tfPanel = new Panel(new BorderLayout(10, 10));
      tfPanel.add(new JLabel("Key: "), BorderLayout.WEST);
      tfPanel.add(cookieKeyTF, BorderLayout.CENTER);
      
      add(tfPanel, BorderLayout.CENTER);
      
      Button gotoUrl = new Button("Go to URL");
      Button ok = new Button("OK");
      Button cancel = new Button("Cancel");
      
      Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
      buttonPanel.add(gotoUrl);
      buttonPanel.add(ok);
      buttonPanel.add(cancel);
      
      add(buttonPanel, BorderLayout.SOUTH);
      
      gotoUrl.addActionListener(this);
      ok.addActionListener(this);
      cancel.addActionListener(this);
      
      gotoUrl.setActionCommand("url");
      ok.setActionCommand("ok");
      cancel.setActionCommand("cancel");
    }
    
    
    
    /**
     * <code>ActionListener</code> implementation.
     */
     
    public void actionPerformed(ActionEvent evt){
      String actionCommand = evt.getActionCommand();
      
      if ("ok".equals(actionCommand))
        close(cookieKeyTF.getText()); 
      else if ("url".equals(actionCommand))
        BrowserControl.displayURL(reserveSpaceUrl);
      else if ("cancel".equals(actionCommand))
        close(null);
      else
        throw new IllegalArgumentException("Unknown actionCommand: " + actionCommand);
    }
    
    
    
    /**
     * Returns the title of this panel.
     */
     
    public String getTitle(){
      return "Specify Key";
    }
    
    
    
    /**
     * Displays the dialog, asks the user for the cookie key and returns it.
     */
     
    public String askKey(){
      return (String)askResult(uiProvider);
    }
    
    
  }
  
   
}