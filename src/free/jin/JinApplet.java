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

import java.awt.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.applet.Applet;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.Hashtable;
import java.util.StringTokenizer;
import free.jin.action.ActionInfo;
import free.jin.plugin.PluginInfo;
import free.jin.plugin.Plugin;
import free.util.IOUtilities;
import free.util.AWTUtilities;


/**
 * A <code>JinContext</code> implementation for running Jin as an applet.
 */
 
public class JinApplet extends Applet implements JinContext{
  
  
  
  /**
   * These override any applet parameters for the <code>getParameter</code>
   * method.
   */
   
  private final Properties params = new Properties();
  
  
  
  /**
   * The server we're connecting to.
   */
   
  private Server server;
  
  
  
  /**
   * The actions we'll be using.
   */
   
  private ActionInfo [] actions;
  
  
  
  /**
   * The plugins we'll be using.
   */
   
  private PluginInfo [] plugins;
  
  
  
  /**
   * The preferences, created after authenticating the user.
   */
   
  private Preferences prefs;
  
  
  
  /**
   * The list of known accounts, created after authenticating the user.
   */
   
  private User [] users;
  
  
  
  /**
   * The username with which we authenticated the user.
   */
   
  private String username;
  
  
  
  /**
   * The password with which we authenticated the user.
   */
   
  private String password;
  
  
  
  /**
   * Initializes the applet.
   */
   
  public void init(){

    try{    
      // Load the server we'll be connecting to
      server = loadServer();
      
      // Load the actions we'll be using.
      actions = loadActions();
      
      // Load the plugins we'll be running.
      plugins = loadPlugins();
      
      
      setLayout(new FlowLayout());
      add(new UserAuthPanel());
    } catch (Throwable t){
        createErrorUI(t);
      }
  }
  
  
  
  /**
   * Starts Jin with the specified application <code>Preferences</code> and list
   * of known users. Autologin parameters are set to login with the specified
   * username and password.
   */
   
  private void start(Preferences prefs, User guestUser, User [] users, String username, String password){
    this.username = username;
    this.password = password;
    this.prefs = prefs;
    this.users = users;
    
    server.setGuestUser(guestUser);
    
    params.put("login.username", username);
    params.put("login.password", password);
    params.put("autologin", "true");
    
    Jin.createInstance(this);
    Jin.getInstance().start();
  }
  
  
  
  /**
   * Starts Jin and logs on as a guest.
   */
   
  private void startAsGuest(){
    this.username = null;
    this.password = null;
    this.prefs = Preferences.createNew();
    this.users = new User[0];
    
    server.setGuestUser(null);
    
    params.put("login.guest", "true");
    params.put("autologin", "true");
    
    Jin.createInstance(this);
    Jin.getInstance().start();
  }
  
  
  
  /**
   * Creates and returns the <code>Server</code> object for the server we'll be
   * connecting to. 
   */
   
  private Server loadServer() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException{
        
    String className = getParameter("server.classname");
    if (className == null)
      throw new IllegalStateException("No server.classname parameter specified");
    
    Server server = (Server)Class.forName(className).newInstance();
    
    URL documentBase = getDocumentBase(); 
    server.setHost("file".equals(documentBase.getProtocol()) ?
      "localhost" : documentBase.getHost());
      
    String portString = getParameter("port");
    if (portString != null) 
      server.setPort(Integer.parseInt(portString));
    
    return server;
  }
  
  
  
  /**
   * Loads the actions we'll be using.
   */
   
  private ActionInfo [] loadActions() throws IOException, ClassNotFoundException{
    String actionsCount = getParameter("actions.count");
    if (actionsCount == null)
      throw new IllegalStateException("No actions.count parameter specified");
    
    ActionInfo [] actions = new ActionInfo[Integer.parseInt(actionsCount)];
    
    for (int i = 0; i < actions.length; i++){
      String className = getParameter("actions." + i + ".classname");
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
    String pluginsCount = getParameter("plugins.count");
    if (pluginsCount == null)
      throw new IllegalStateException("No plugins.count parameter specified");
    
    PluginInfo [] plugins = new PluginInfo[Integer.parseInt(pluginsCount)];
    
    for (int i = 0; i < plugins.length; i++){
      String className = getParameter("plugins." + i + ".classname");
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
   * Overrides the <code>getParameter</code> method to allow us to add our own
   * parameters, via the props Properties object.
   */
   
  public String getParameter(String paramName){
    String paramValue = params.getProperty(paramName);
    
    return paramValue == null ? super.getParameter(paramName) : paramValue;
  }
  
  
  
  /**
   * Returns the application-wide preferences.
   */
   
  public Preferences getPrefs(){
    return prefs;
  }
  
  
  
  /**
   * Returns all the resources of the specified type.
   */

  public Resource [] getResources(String resourceType, Plugin plugin){
    String resourcesArg = getParameter("resources." + resourceType);
    if (resourcesArg == null)
      return new Resource[0];
    
    StringTokenizer resourceNames = new StringTokenizer(resourcesArg, " ");
    Resource [] resources = new Resource[resourceNames.countTokens()];
    for (int i = 0; i < resources.length; i++){
      try{
        URL resourceURL = new URL(getCodeBase(), "resources/" + resourceType + "/" 
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
      String resourcesArg = getParameter("resources." + type);
      if (resourcesArg == null)
        return null;
      
      StringTokenizer resourceNames = new StringTokenizer(resourcesArg, " ");
      while (resourceNames.hasMoreTokens()){
        String resource = resourceNames.nextToken();
        int slashIndex = resource.indexOf("/");
        String resourceId = slashIndex == -1 ? resource : resource.substring(slashIndex + 1);
        if (id.equals(resourceId)){
          URL resourceURL = new URL(getCodeBase(), "resources/" + type + "/" + resource + "/");
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
   * The thread that uploads the user settings. <code>null</code> when none.
   */
   
  private Thread settingsUploadThread = null;
  
  
  
  /**
   * The dialog displayed to the user while settings are uploaded.
   */
   
  private Dialog settingsUploadDialog = null;
  
  
  
  /**
   * Uploads preferences and reinitializes the applet, so that it's ready to
   * go again.
   */
   
  public void shutdown(){
    if (username != null){ // Not logged in as guest
      
      settingsUploadThread = new Thread(){
        public void run(){
          try{
            String result = uploadSettings();
            if (!"OK".equals(result)){
              showErrorDialog(result);
              return;
            }
          } catch (IOException e){
              ByteArrayOutputStream buf = new ByteArrayOutputStream();
              e.printStackTrace(new PrintStream(buf));
              showErrorDialog(buf.toString());
            }
            finally{
              synchronized(JinApplet.this){
                if (settingsUploadThread == Thread.currentThread()){
                  settingsUploadThread = null;
                  if (settingsUploadDialog != null){
                    settingsUploadDialog.dispose();
                    settingsUploadDialog = null;
                  }
                }
              }
            }
        }
        
        private void showErrorDialog(String message){
          Dialog errorDialog = 
            new SettingsUploadErrorDialog(AWTUtilities.frameForComponent(JinApplet.this), message);
          AWTUtilities.centerWindow(errorDialog, JinApplet.this);
          errorDialog.setVisible(true);
          
        }
      };
      
      settingsUploadDialog = new SettingsUploadDialog(AWTUtilities.frameForComponent(this)){
        public void addNotify(){
          super.addNotify();
          if ((settingsUploadThread != null) && !settingsUploadThread.isAlive())
            settingsUploadThread.start();
        }
        public void canceled(){
          synchronized(JinApplet.this){
            this.dispose();
          }
        }
      };
      
      AWTUtilities.centerWindow(settingsUploadDialog, this);
      settingsUploadDialog.setVisible(true);
    }

    username = null;
    password = null;
    
    removeAll();
    init();
    validate();
  }
  
  
  

  /**
   * Stores the user settings.
   */

  private String uploadSettings() throws IOException{
    URL savePrefsUrl = new URL(getDocumentBase(), getParameter("savePrefsURL"));
    URLConnection conn = savePrefsUrl.openConnection();
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-type", "application/binary");
    
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()));
    out.writeBytes(username + "\n");
    out.writeBytes(password + "\n");

    
    // Write application-wide prefs
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    prefs.save(buf);
    out.writeInt(buf.size());
    buf.writeTo(out);
    
    // Write guest
    writeUser(out, server.getGuest());
    
    // Write users
    out.writeInt(users.length);
    for (int i = 0; i < users.length; i++)
      writeUser(out, users[i]);
    
    out.writeBytes("PREFS_UPLOAD_END");
    
    out.close();
    
    conn.connect();
    
    InputStream in = new BufferedInputStream(conn.getInputStream());
    DataInputStream dataIn = new DataInputStream(in);
    
    buf.reset();
    IOUtilities.pump(conn.getInputStream(), buf);
    String result = new String(buf.toByteArray());
    return result;
  }
  
  
  
  /**
   * Writes (for storage purposes) the information about the specified user into
   * the specified output stream.
   */
   
  private void writeUser(DataOutputStream out, User user) throws IOException{
    out.writeUTF(user.getUsername());
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    user.getPrefs().save(buf);
    out.writeInt(buf.size());
    buf.writeTo(out);
    
    // Todo: add storing user files
  }
  
  
  
  /**
   * Returns an array containing the server we're connecting to.
   */
   
  public Server [] getServers(){
    return new Server[]{server};
  }
  
  
  
  /**
   * Returns the list of known user's accounts on the server.  
   */
   
  public User [] getUsers(){
    return users;
  }
  
  
  
  /**
   * Sets the list of known user accounts, so that they can be uploaded as a
   * part of the preferences.
   */
   
  public void setUsers(User [] users){
    this.users = users;
  }
  
  
  
  /**
   * Returns the descriptions of actions for the specified server.
   */
   
  public ActionInfo [] getActions(Server server){
    if (server != this.server)
      throw new IllegalArgumentException("Unknown server: " + server);
    
    return actions;
  }
  
  
  
  /**
   * Returns the descriptions of plugins for the specified server.
   */
   
  public PluginInfo [] getPlugins(Server server){
    if (server != this.server)
      throw new IllegalArgumentException("Unknown server: " + server);
    
    return plugins;
  }
  
  
  
  /**
   * Returns <code>true</code>.
   */
   
  public boolean isSavePrefsCapable(){
    return true;
  }



  /**
   * Returns text warning the user about saving his password and asking him to
   * confirm it.
   */
   
  public String getPasswordSaveWarning(){
    boolean isSecure = getDocumentBase().getProtocol().equals("https");
    
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
   * Creates UI which informs the user that the specified error has occurred.
   */
   
  private void createErrorUI(Throwable t){
    removeAll();
    
    setLayout(new BorderLayout());
    
    add(new Label("An error has occurred:"), BorderLayout.NORTH);
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(buf));
    TextArea stackTraceArea = new TextArea(buf.toString());

    add(stackTraceArea, BorderLayout.CENTER);
    doLayout();
  }


  
  /**
   * A panel which asks the user to specify his username and password. When
   * the user submits those, the settings for that user are retrieved and
   * the <code>start(Preferences, User, User [], String, String)</code> method
   * of <code>JinApplet</code> is invoked. If the user chooses to continue as
   * guest, the <code>startAsGuest</code> method of <code>JinApplet</code> is
   * invoked.
   */
   
  private class UserAuthPanel extends Panel implements Runnable{
    
    
    
    /**
     * The username text field.
     */
     
    private final TextField usernameField;
    
    
    
    /**
     * The password field.
     */
     
    private final TextField passwordField;
    
    
    
    /**
     * The status label.
     */
     
    private final Label statusLabel;
    
    
    
    /**
     * The login button.
     */
     
    private Button loginButton;
    
    
    
    /**
     * The "login as guest" button.
     */
     
    private Button guestButton;
    
    
    
    /**
     * The thread authenticating the user and retrieving his settings.
     */
     
    private Thread authThread = null;
    
    
    
    /**
     * Creates a new <code>UserAuthPanel</code>.
     */
     
    public UserAuthPanel(){
      usernameField = new TextField(20);
      passwordField = new TextField(20);
      statusLabel = new Label();
      
      passwordField.setEchoChar('*');
      
      createUI();
    }
    
    
    
    /**
     * Sets the status to the specified value (sets the status label). 
     */
     
    private void setStatus(String status, Color color){
      statusLabel.setForeground(color);
      statusLabel.setText(status);
    }
    
    
    
    /**
     * Builds the ui of this panel.
     */
     
    private void createUI(){
      this.setLayout(new GridLayout(5, 1));
      this.add(new Label("Enter your username and password or continue as guest"));
      
      Panel usernamePanel = new Panel(new FlowLayout(FlowLayout.LEFT));
      Label usernameLabel = new Label("Username: ");
      usernamePanel.add(new Label("Username: "));
      usernamePanel.add(usernameField);
      this.add(usernamePanel);
      
      Panel passwordPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
      passwordPanel.add(new Label("Password: "));
      passwordPanel.add(passwordField);
      this.add(passwordPanel);
      
      this.add(statusLabel);
      
      loginButton = new Button("Login");
      guestButton = new Button("Login as Guest");
      
      Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
      buttonsPanel.add(loginButton);
      buttonsPanel.add(guestButton);
      
      this.add(buttonsPanel);
      
      loginButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          String username = usernameField.getText();
          String password = passwordField.getText();
          
          if ((username == null) || "".equals(username)){
            setStatus("Please specify a username", Color.red);
            usernameField.requestFocus();
          }
          else if ((password == null) || "".equals(password)){
            setStatus("Please specify the password", Color.red);
            passwordField.requestFocus();
          }
          else{
            loginButton.setEnabled(false);
            guestButton.setEnabled(false);

            retrievePrefs(); 
          }
        }
      });
      
      guestButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          setStatus("Starting Jin, this may take a few moments.", Color.black);
          loginButton.setEnabled(false);
          guestButton.setEnabled(false);
          startAsGuest();
          setStatus("Do not leave this page while Jin is running!", Color.black);
        }
      });
    }
    
    
    
    /**
     * Authenticates the user and retrieves the preferences from the server.
     */
     
    private synchronized void retrievePrefs(){
      if (authThread == null){
        authThread = new Thread(this);
        authThread.start();
      }
    }
    
    
    
    /**
     * Connects to the server and retrieves the preferences. 
     */
     
    public void run(){
      try{
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        setStatus("Connecting", Color.black);
        
        URL loadPrefsUrl = new URL(getDocumentBase(), getParameter("loadPrefsURL"));
        URLConnection conn = loadPrefsUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-type", "application/binary");
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.writeBytes(username + "\n");
        out.writeBytes(password + "\n");
        
        out.close();
        
        conn.connect();
        
        setStatus("Authenticating", Color.black);
        
        InputStream in = new BufferedInputStream(conn.getInputStream());
        DataInputStream dataIn = new DataInputStream(in);
        
        Preferences prefs;
        User guest;
        User [] users;
        
        // Read return code
        String returnCode = dataIn.readLine();
        
        if ("OK".equals(returnCode)){
          setStatus("Retrieving preferences", Color.black);
          
          // Read application preferences
          int appPrefsLength = dataIn.readInt();
          prefs = Preferences.load(new ByteArrayInputStream(IOUtilities.read(in, appPrefsLength)));
          
          // Read guest user
          guest = loadUser(dataIn);
  
          // Number of known users
          int usersCount = dataIn.readInt();
          users = new User[usersCount];
                  
          for (int i = 0; i < users.length; i++)
            users[i] = loadUser(dataIn);
        } else if ("NOPREFS".equals(returnCode)){ // A new user
          prefs = Preferences.createNew();
          guest = null;
          users = new User[0];
        }
        else{ // An error
          setStatus(returnCode, Color.red);
          System.out.println(returnCode);
          IOUtilities.pump(in, System.out);
          
          loginButton.setEnabled(true);
          guestButton.setEnabled(true);
          return;
        }

        setStatus("Starting Jin, this may take a few moments.", Color.black);
        
        start(prefs, guest, users, username, password);
        
        setStatus("Do not leave this page while Jin is running!", Color.black);
      } catch (IOException e){
          e.printStackTrace();
          createErrorUI(e);
        }
        catch (RuntimeException e){
          e.printStackTrace();
          createErrorUI(e);
        }
        finally{
          synchronized(this){
            authThread = null;
          }
        }
    }
    
    
    
    /**
     * Creates a User from the specified <code>InputStream</code>.
     */
     
    private User loadUser(DataInputStream in) throws IOException{
      String username = in.readUTF();
      int prefsLength = in.readInt();
      Preferences prefs = Preferences.load(new ByteArrayInputStream(IOUtilities.read(in, prefsLength)));
      Hashtable files = new Hashtable(); // TODO: Add loading/saving files 
       
      return new User(server, username, prefs, files);
    }
    

    
  }
  
  
  
  /**
   * A dialog which is displayed while the user settings are uploaded.
   */
   
  private abstract class SettingsUploadDialog extends Dialog{
    
    
    
    /**
     * Creates a new <code>SettingsUploadDialog</code> with the specified
     * parent Frame.
     */
     
    public SettingsUploadDialog(Frame parent){
      super(parent, "Settings Upload", true);
     
      this.setLayout(new GridLayout(2, 1));
      
      this.add(new Label("Your settings are being uploaded to the server, please wait."));
      
      Button button = new Button("Cancel");
      Panel buttonPanel = new Panel(new FlowLayout());
      buttonPanel.add(button);
      this.add(buttonPanel);
      
      button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          canceled();
        }
      });
    }
    
    
    
    /**
     * Gets called when the user pressed the "cancel" button.
     */
     
    public abstract void canceled();
    
    
  }
  
  
  
  /**
   * A dialog for displaying the error that occurred while uploading user
   * settings.
   */
   
  private class SettingsUploadErrorDialog extends Dialog{
    
    
    
    /**
     * Creates a new <code>SettingsUploadErrorDialog</code> with the specified
     * parent frame and the specified error text.
     */
     
    public SettingsUploadErrorDialog(Frame parent, String errorMessage){
      super(parent, "Settings Upload Error", true);
      
      createUI(errorMessage);
    }
    
    
    
    /**
     * Creates the UI of this dialog.
     */
     
    private void createUI(String errorMessage){
      this.setLayout(new BorderLayout(5, 5));
      
      this.add(BorderLayout.NORTH, new Label("An error has occurred while uploading your settings:"));
      
      TextArea errorArea = new TextArea(errorMessage);
      errorArea.setEditable(false);
      this.add(BorderLayout.CENTER, errorArea);
      
      Button closeButton = new Button("Close");
      Panel buttonPanel = new Panel(new FlowLayout());
      buttonPanel.add(closeButton);
      this.add(BorderLayout.SOUTH, buttonPanel);
      
      closeButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          dispose(); 
        }
      });
    }
     
     
     
  }
   
}