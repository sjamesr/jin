/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import free.chess.BoardImageBoardPainter;
import free.chess.ImagePiecePainter;
import free.chess.SquareImagesBoardPainter;
import free.jin.action.ActionInfo;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;
import free.util.*;
import free.util.audio.AppletContextAudioPlayer;


/**
 * A <code>JinContext</code> implementation for running Jin as an applet.
 */
 
public class JinApplet extends Applet implements JinContext{
  
  
  
  /**
   * The <code>Locale</code> for this instance of Jin.
   */
  
  private Locale locale;
  
  
  
  /**
   * The <code>Localization</code> for this class.
   */
  
  private Localization l10n;
  
  
  
  /**
   * Autologin parameters for Jin. They override any applet parameters in
   * <code>getParameter</code>.
   */
   
  private final Properties autologinParams = new Properties();
  
  
  
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
      // Determine the locale
      locale = determineLocale();
      
      // Configure the libraries the applet depends on
      configureLibraries();
      
      // Load localization
      l10n = Localization.load(JinApplet.class);
      
      // Load the server we'll be connecting to
      server = loadServer();
      
      // Load the actions we'll be using.
      actions = loadActions();
      
      // Load the plugins we'll be running.
      plugins = loadPlugins();
      
      
      // Set the background color
      String bgColorString = getParameter("bgcolor");
      if (bgColorString != null)
        setBackground(new Color(0xff000000 | Integer.parseInt(bgColorString, 16)));
      
      setLayout(new FlowLayout());
      add(new UserAuthPanel());
    } catch (Throwable t){
        createErrorUI(t);
      }
  }
  
  
  
  /**
   * Restarts JinApplet. This is called when the user closes Jin and is supposed
   * to bring the applet to its initial state - ready to accept a new username
   * and password.
   */
   
  private void restart(){
    try{
      username = null;
      password = null;
      
      removeAll();
      autologinParams.clear();
      
      // The server needs to be recreated because the user might use a different
      // account after restarting Jin, which means a different guest User.
      server = loadServer();
      
      add(new UserAuthPanel());
      validate();
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
    
    autologinParams.put("login.username", username);
    autologinParams.put("login.password", password);
    autologinParams.put("autologin", "true");
    
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
    
    autologinParams.put("login.guest", "true");
    autologinParams.put("autologin", "true");
    
    Jin.createInstance(this);
    Jin.getInstance().start();
  }
  
  
  
  /**
   * Determines the locale for this instance of Jin.
   */
  
  private Locale determineLocale(){
    String language = getParameter("locale.language");
    String country = getParameter("locale.country");
    String variant = getParameter("locale.variant");
    
    language = language == null ? "" : language;
    country = country == null ? "" : country;
    variant = variant == null ? "" : variant;
    
    return new Locale(language, country, variant);
  }
  
  
  
  /**
   * Configures various libraries the applet uses.
   */
  
  private void configureLibraries(){
    Localization.setAppLocale(locale);
    BrowserControl.setAppletContext(getAppletContext());
    AppletContextAudioPlayer.setAppletContext(getAppletContext());
    ImagePiecePainter.setAsyncImageLoad(true);
    BoardImageBoardPainter.setAsyncImageLoad(true);
    SquareImagesBoardPainter.setAsyncImageLoad(true);
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
    
    server.setHost(getDocumentBase().getHost());
      
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
   * Returns the locale for this instance of Jin.
   */
  
  public Locale getLocale(){
    return locale;
  }
  
  
  
  /**
   * Overrides the <code>getParameter</code> method to allow us to add our own
   * parameters, via the props Properties object.
   */
   
  public String getParameter(String paramName){
    String paramValue = autologinParams.getProperty(paramName);
    
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
   * See {@link JinContext#getResources(String, Plugin)} for more information.
   */

  public Map getResources(String resourceType, Plugin plugin){
    Map resourceMap = new HashMap();
    
    try{
      URL codeBase = getCodeBase();
      URL definitionsFileURL = new URL(codeBase, "resources/" + resourceType + "/definitions.zip");
      if (!IOUtilities.isURLCached(definitionsFileURL))
        IOUtilities.cacheURL(definitionsFileURL);
      ZipInputStream zip = new ZipInputStream(IOUtilities.inputStreamForURL(definitionsFileURL, true));
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null){
        String entryName = entry.getName();
        if (!entryName.endsWith("definition") || entry.isDirectory())
          continue;
        String resourcePath = entryName.substring(0, entryName.length() - "/definition".length());
        byte [] data = IOUtilities.readToEnd(zip);
        URL resourceURL = new URL(codeBase, "resources/" + resourceType + "/" + resourcePath + "/");
        IOUtilities.cacheData(new URL(resourceURL, "definition"), data);
        Resource resource = loadResource(resourceURL, plugin);
        if (resource != null)
          resourceMap.put(resource.getId(), resource);
      }
    } catch (IOException e){
        e.printStackTrace();
      }
    
    return resourceMap;
  }
  
  
  
  /**
   * Returns the resource with the specified type and id, or <code>null</code>
   * if the resource can't be loaded.
   */
   
  public Resource getResource(String type, String id, Plugin plugin){
    try{
      URL resourceURL = new URL(getCodeBase(), "resources/" + type + "/" + id + "/");
      return loadResource(resourceURL, plugin);
    } catch (IOException e){
        e.printStackTrace();
        return null;
      }
  }
  
  
  
  
  /**
   * Loads a single resource from the specified URL. Returns <code>null</code>
   * if unsuccessful.
   */
   
  private Resource loadResource(URL url, Plugin plugin) throws IOException{
    URL defURL = new URL(url, "definition");
    if (!IOUtilities.isURLCached(defURL))
      IOUtilities.cacheURL(defURL);
    
    Properties def = IOUtilities.loadProperties(defURL, true);
    String classname = def.getProperty("classname");
    if (classname == null)
      return null;
    
    try{
      // We need to load it with the plugin's classloader because the
      // resource may be of a type which is a part of the plugin.
      Class resourceClass = plugin.getClass().getClassLoader().loadClass(classname);
      Resource resource = (Resource)resourceClass.newInstance();
      if (resource.load(url, plugin))
        return resource;
      else
        return null;
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
            String result = null;
            while (!"OK".equals(result = uploadSettings())){
              // Hackish, I know
              if (result.toLowerCase().indexOf("password") != -1){
                if (!showPasswordDialog(result))
                  break;
              }
              else 
                showErrorDialog(result);
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
        
        /**
         * Shows a password dialog and returns whether preferences upload should
         * be retried. 
         */
        
        private boolean showPasswordDialog(String message){
          PasswordDialog passDialog =
            new PasswordDialog(AWTUtilities.frameForComponent(JinApplet.this), message, username);
          AWTUtilities.centerWindow(passDialog, JinApplet.this);
          
          boolean result = passDialog.shouldRetry();
          
          if (result){
            username = passDialog.getUsername();
            password = passDialog.getPassword();
          }
          return result;
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
    
    restart();
  }
  
  
  

  /**
   * Stores the user settings.
   */

  private String uploadSettings() throws IOException{
    URL savePrefsUrl = getPrefsUploadUrl();
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
    try{
      boolean isSecure = getPrefsDownloadUrl().getProtocol().equals("https") &&
                         getPrefsUploadUrl().getProtocol().equals("https");
      
      if (isSecure)
        return null;
      else
        return l10n.getString("passwordSaveWarning");
    } catch (MalformedURLException e){
        e.printStackTrace();
        return "Error: Your password will not be stored due to a configuration problem.";
      }
  }
  
  
  
  /**
   * Returns <code>false</code>. 
   */
  
  public boolean isUserExtensible(){
    return false;
  }
   
   

  /**
   * Creates UI which informs the user that the specified error has occurred.
   */
   
  private void createErrorUI(Throwable t){
    removeAll();
    
    setLayout(new BorderLayout());
    
    add(new Label(l10n.getString("errorLabel.text")), BorderLayout.NORTH);
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(buf));
    TextArea stackTraceArea = new TextArea(buf.toString());

    add(stackTraceArea, BorderLayout.CENTER);
    doLayout();
  }
  
  
  
  /**
   * Returns the URL from which we download user settings.
   */
   
  private URL getPrefsDownloadUrl() throws MalformedURLException{
    URL url = new URL(getDocumentBase(), getParameter("loadPrefsURL"));
    return new URL(getParameter("prefsProtocol"), url.getHost(), url.getPort(), url.getFile());
  }
  

  
  /**
   * Returns the URL to which we upload user settings.
   */
   
  private URL getPrefsUploadUrl() throws MalformedURLException{
    URL url = new URL(getDocumentBase(), getParameter("savePrefsURL"));
    return new URL(getParameter("prefsProtocol"), url.getHost(), url.getPort(), url.getFile());
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
      this.setLayout(new BorderLayout(15, 15));
      this.add(BorderLayout.NORTH,
        new Label(l10n.getString("instructionsLabel.text")));
      
      Panel userInfoPanel = new Panel(new BorderLayout());
      Panel labelsPanel = new Panel(new GridLayout(2, 1, 10, 10));
      Panel textFieldsPanel = new Panel(new GridLayout(2, 1, 10, 10));
      
      labelsPanel.add(new Label(l10n.getString("usernameLabel.text")));
      textFieldsPanel.add(usernameField);
            
      labelsPanel.add(new Label(l10n.getString("passwordLabel.text")));
      textFieldsPanel.add(passwordField);
      
      userInfoPanel.add(BorderLayout.WEST, labelsPanel);
      
      Panel textFieldsWrapperPanel = new Panel(new BorderLayout());
      textFieldsWrapperPanel.add(BorderLayout.WEST, textFieldsPanel);
      userInfoPanel.add(BorderLayout.CENTER, textFieldsWrapperPanel);
      
      this.add(BorderLayout.CENTER, userInfoPanel);
      
      Panel statusAndButtonsPanel = new Panel(new GridLayout(2, 1));
      
      statusAndButtonsPanel.add(statusLabel);
      
      loginButton = new Button(l10n.getString("loginButton.text"));
      guestButton = new Button(l10n.getString("loginAsGuestButton.text"));
      
      Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
      buttonsPanel.add(loginButton);
      buttonsPanel.add(guestButton);
      statusAndButtonsPanel.add(buttonsPanel);
      
      this.add(BorderLayout.SOUTH, statusAndButtonsPanel);
      
      ActionListener loginListener = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          String username = usernameField.getText();
          String password = passwordField.getText();
          
          if ((username == null) || "".equals(username)){
            setStatus(l10n.getString("usernameUnspecifiedError"), Color.red);
            usernameField.requestFocus();
          }
          else if ((password == null) || "".equals(password)){
            setStatus(l10n.getString("passwordUnspecifiedError"), Color.red);
            passwordField.requestFocus();
          }
          else{
            loginButton.setEnabled(false);
            guestButton.setEnabled(false);

            retrievePrefs(); 
          }
        }
      };
      loginButton.addActionListener(loginListener);
      // We do this to imitate a "default" button
      usernameField.addActionListener(loginListener);
      passwordField.addActionListener(loginListener);
      
      guestButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          setStatus(l10n.getString("startingJinStatus"), Color.black);
          loginButton.setEnabled(false);
          guestButton.setEnabled(false);
          startAsGuest();
          setStatus(l10n.getString("stayOnPageStatus"), Color.black);
        }
      });
    }
    
    
    
    /**
     * Have we been painted already?
     */
     
    private boolean isPainted = false;
    
    
    
    /**
     * Set focus to the username field on the first paint.
     */
     
    public void paint(Graphics g){
      super.paint(g);
      
      if (!isPainted){
        isPainted = true;
        usernameField.requestFocus();
      }
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
        
        setStatus(l10n.getString("connectingStatus"), Color.black);
        
        URL loadPrefsUrl = getPrefsDownloadUrl();
        URLConnection conn = loadPrefsUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-type", "application/binary");
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.writeBytes(username + "\n");
        out.writeBytes(password + "\n");
        
        out.close();
        
        conn.connect();
        
        setStatus(l10n.getString("authenticatingStatus"), Color.black);
        
        InputStream in = new BufferedInputStream(conn.getInputStream());
        DataInputStream dataIn = new DataInputStream(in);
        
        Preferences prefs;
        User guest;
        User [] users;
        
        // Read return code
        String returnCode = dataIn.readLine();
        
        if ("OK".equals(returnCode)){
          setStatus(l10n.getString("retrievingPreferencesStatus"), Color.black);
          
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

        setStatus(l10n.getString("startingJinStatus"), Color.black);
        
        start(prefs, guest, users, username, password);
        
        setStatus(l10n.getString("stayOnPageStatus"), Color.black);
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
      super(parent, "", true);
      
      setTitle(l10n.getString("prefsUploadDialog.title"));
     
      this.setLayout(new GridLayout(2, 1));
      
      this.add(new Label(l10n.getString("prefsUploadDialog.message")));
      
      Button button = new Button(l10n.getString("prefsUploadDialog.cancelButton.text"));
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
     * parent frame and error text.
     */
     
    public SettingsUploadErrorDialog(Frame parent, String errorMessage){
      super(parent, l10n.getString("prefsUploadErrorDialog.title"), true);
      
      createUI(errorMessage);
    }
    
    
    
    /**
     * Creates the UI of this dialog.
     */
     
    private void createUI(String errorMessage){
      this.setLayout(new BorderLayout(5, 5));
      
      this.add(BorderLayout.NORTH, new Label(l10n.getString("prefsUploadErrorDialog.message")));
      
      TextArea errorArea = new TextArea(errorMessage, 3, 40);
      
      errorArea.setEditable(false);
      this.add(BorderLayout.CENTER, errorArea);
      
      Button closeButton = new Button(l10n.getString("prefsUploadErrorDialog.closeButton.text"));
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
  
  
  
  /**
   * A dialog which asks the user to input his password and retry uploading the
   * preferences.
   */
  
  private class PasswordDialog extends Dialog{
    
    
    /**
     * The username field.
     */
    
    private final TextField usernameField;
    
    
    
    /**
     * The password field.
     */
    
    private final TextField passwordField;
    
    
    
    /**
     * Whether prefs upload should be retried.
     */
    
    private boolean shouldRetry;

    
    
    /**
     * Creates a new <code>PasswordDialog</code> with the specified
     * parent frame, error text and current username.
     */
     
    public PasswordDialog(Frame parent, String errorMessage, String username){
      super(parent, l10n.getString("passwordPrefsUploadErrorDialog.title"), true);
      
      usernameField = new TextField(username);
      passwordField = new TextField();
      passwordField.setEchoChar('*');
      
      createUI(errorMessage);
    }
    
    

    /**
     * Creates the UI of this dialog.
     */
     
    private void createUI(String errorMessage){
      this.setLayout(new BorderLayout(5, 5));
      
      this.add(BorderLayout.NORTH, new Label(l10n.getString("passwordPrefsUploadErrorDialog.message")));
      
      Panel centerPanel = new Panel(new BorderLayout(5, 5));
      
      TextArea errorArea = new TextArea(errorMessage, 3, 40);
      errorArea.setEditable(false);
      centerPanel.add(BorderLayout.CENTER, errorArea);
      
      Panel userInfoPanel = new Panel(new BorderLayout());
      Panel labelsPanel = new Panel(new GridLayout(2, 1, 10, 10));
      Panel textFieldsPanel = new Panel(new GridLayout(2, 1, 10, 10));
      
      labelsPanel.add(new Label(l10n.getString("usernameLabel.text")));
      textFieldsPanel.add(usernameField);
            
      labelsPanel.add(new Label(l10n.getString("passwordLabel.text")));
      textFieldsPanel.add(passwordField);
      
      userInfoPanel.add(BorderLayout.WEST, labelsPanel);
      userInfoPanel.add(BorderLayout.CENTER, textFieldsPanel);
      centerPanel.add(BorderLayout.SOUTH, userInfoPanel);
      this.add(BorderLayout.CENTER, centerPanel);
      
      
      Button retryButton = new Button(l10n.getString("passwordPrefsUploadErrorDialog.retryButton.text"));
      Button closeButton = new Button(l10n.getString("passwordPrefsUploadErrorDialog.closeButton.text"));
      
      Panel buttonPanel = new Panel(new FlowLayout());
      buttonPanel.add(retryButton);
      buttonPanel.add(closeButton);
      this.add(BorderLayout.SOUTH, buttonPanel);
      
      retryButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          shouldRetry = true;
          dispose();
        }
      });
      
      closeButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          shouldRetry = false;
          dispose(); 
        }
      });
    }
    
    
    
    /**
     * Displays the dialog and returns whether the user asked to retry
     * prefs upload.
     */
    
    public boolean shouldRetry(){
      this.setVisible(true);
      return shouldRetry;
    }
    
    
    
    /**
     * Returns the username specified by the user.
     */
    
    public String getUsername(){
      return usernameField.getText();
    }
    
    
    
    /**
     * Returns the password specified by the user.
     */
    
    public String getPassword(){
      return passwordField.getText();
    }
    
    
  }
   
}