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

import java.io.IOException;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.UIManager;

import free.jin.action.ActionInfo;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;
import free.jin.ui.AboutPanel;
import free.jin.ui.OptionPanel;
import free.jin.ui.UIProvider;
import free.util.IOUtilities;
import free.util.Pair;
import free.util.PlatformUtils;
import free.util.TextUtilities;



/**
 * The class responsible for starting up Jin.
 */
 
public class Jin{
  
  
  
  /**
   * Application (Jin) properties.
   */

  private final static Properties appProps;
  
  
  
  /**
   * Loads and returns the application properties.
   */
  
  static{
    try{
      appProps = IOUtilities.loadPropertiesAndClose(Jin.class.getResourceAsStream("resources/app.props"));
    } catch (IOException e){
        e.printStackTrace();
        throw new IllegalStateException("Unable to load application properties from resources/app.props");
      }
  }

  
  
  
  /**
   * The sole Jin instance.
   */
   
  private static volatile Jin instance = null;
  
  
  
  /**
   * The context.
   */
   
  private final JinContext context;
  
  
  
  /**
   * Cache of loaded resources. Maps a pair of (resourceType, plugin) to a
   * pair whose first value is a map from resourceId to the resource and whose
   * second value is a Boolean specifying whether the map consists of all the
   * resources of that type for that plugin (it may also be partially loaded).
   */
  
  private final Map resourceCache = new HashMap();
  
  
  
  /**
   * A list of known users (accounts on various servers).
   */
   
  private final DefaultListModel users;
  
  
  
  /**
   * The ui provider.
   */
   
  private final UIProvider uiProvider;
  
  
  
  /**
   * The connection manager.
   */

  private final ConnectionManager connManager;
   
  
  
  /**
   * Creates a new <code>Jin</code> instance, with the specified context.
   */
   
  private Jin(JinContext context){
    this.context = context;
    
    // Get known users (accounts on various servers);
    users = new DefaultListModel();
    User [] usersArr = context.getUsers();
    for (int i = 0; i < usersArr.length; i++)
      users.addElement(usersArr[i]);
      
    // Restore the look and feel
    restoreLookAndFeel();
    
    // Create the UI manager
    uiProvider = createUiProvider();

    // Create the connection manager
    connManager = new ConnectionManager();
  }
  
  
  
  /**
   * Creates the sole Jin instance, with the specified context.
   */
   
  public synchronized static void createInstance(JinContext context){
    if (instance != null)
      throw new IllegalStateException("Jin instance already exists");
    
    instance = new Jin(context);
  }
  
  
  
  /**
   * Returns the sole Jin instance.
   */
   
  public synchronized static Jin getInstance(){
    if (instance == null)
      throw new IllegalStateException("Jin instance doesn't yet exist");
    
    return instance;
  }
  
  
  
  /**
   * Starts Jin. This method is invoked by the context.
   */

  public void start(){
    uiProvider.init();
    
    UpgradeManager.start();
    
    uiProvider.start();
  }
  
  
  
  /**
   * Sets the current look and feel to the one specified in user preferences.
   */

  private void restoreLookAndFeel(){
    String defaultLnf = UIManager.getSystemLookAndFeelClassName();

    // GTK Look and Feel still sucks. Remove this when it doesn't.
    if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(defaultLnf))
      defaultLnf = UIManager.getCrossPlatformLookAndFeelClassName();
    
    Preferences prefs = getCustomizingPrefs() == null ? 
        getPrefs() : Preferences.createBackedUp(getPrefs(), getCustomizingPrefs());
    
    String lfClassName = (String)prefs.lookup("lookAndFeel.classname." + PlatformUtils.getOSName(), null);
    
    try{
      if (lfClassName != null)
        UIManager.setLookAndFeel(lfClassName);
      else
        UIManager.setLookAndFeel(defaultLnf);
    } catch (Exception e){
        if (lfClassName != null)
          JOptionPane.showMessageDialog(null, "Unable to use the specified look and feel: \n" +
              TextUtilities.breakIntoLines(e.getMessage(), 60), "Jin Error", JOptionPane.ERROR_MESSAGE);
      }
  }
  

  
  /**
   * Creates the UIProvider based on user preferences.
   */
  
  private UIProvider createUiProvider(){
    String defaultUiProviderClassname = getAppProperty("uiProvider.classname." + PlatformUtils.getOSName(), null);
    if (defaultUiProviderClassname == null)
      defaultUiProviderClassname = getAppProperty("uiProvider.classname", null);
    
    String uiProviderClassname = 
      getPrefs().getString("uiProvider.classname", defaultUiProviderClassname);
    
    try{
      return (UIProvider)Class.forName(uiProviderClassname).newInstance();
    } catch (Exception e){
        e.printStackTrace();
        throw new IllegalStateException("Unable to instantiate UIProvider");
      }
  }
  


  /**
   * Returns the application name.
   */

  public static String getAppName(){
    return getAppProperty("app.name", null);
  }



  /**
   * Returns the application version.
   */

  public static String getAppVersion(){
    return getAppProperty("app.version", null);
  }
  
  
  
  /**
   * Returns the value of the specified application property, or
   * the specified default value if none exists.
   */
  
  public static String getAppProperty(String propName, String defaultValue){
    return appProps.getProperty(propName, defaultValue);
  }
  
  
  
  /**
   * Returns the value of the specified parameter, as passed to the application
   * when it was run. Returns <code>null</code> if no parameter with the
   * specified name was passed.
   */
   
  public String getParameter(String paramName){
    return context.getParameter(paramName);
  }



  /**
   * Returns the application-wide preferences.
   */
   
  public Preferences getPrefs(){
    return context.getPrefs();
  }
  
  
  
  /**
   * Returns the application's customizing preferences. These are used as
   * default preferences for each user. They allow 3rd parties to customize Jin
   * to their liking. May return <code>null</code>.
   */
  
  public Preferences getCustomizingPrefs(){
    return context.getCustomizingPrefs();
  }
  
  
  
  
  /**
   * Returns the locale for this instance of Jin.
   */
  
  public Locale getLocale(){
    return context.getLocale();
  }
  
  
  
  /**
   * Returns a list of supported servers.
   */
   
  public Server [] getServers(){
    return (Server [])context.getServers().clone();
  }



  /**
   * Returns a list of <code>ActionInfo</code> objects describing the standalone
   * actions for the specified server.
   */
   
  public ActionInfo [] getActions(Server server){
    return (ActionInfo [])context.getActions(server).clone();
  }
  
  
  
  /**
   * Returns a list of <code>PluginInfo</code> objects describing the plugins
   * for the specified server.
   */

  public PluginInfo [] getPlugins(Server server){
    return (PluginInfo [])context.getPlugins(server).clone();
  }
  
  
  
  /**
   * Loads and returns the resources of the specified type for the specified
   * plugin. The returned map is from resource IDs to <code>Resource</code>s.
   * Resources are typically used when there is a need to allow the user
   * (or some other 3rd party) to add his own customizations to Jin
   * (or a plugin). For example, this mechanism is used for loading piece sets
   * and boards by the board manager plugin. A <code>JinContext</code>
   * implementation may then look for piece set "packs" in some predefined
   * directories, allowing the user to add/remove piece sets simply by
   * adding/deleting files from those directories.
   */
   
  public Map getResources(String resourceType, Plugin plugin){
    Pair key = new Pair(resourceType, plugin);
    Pair value = (Pair)resourceCache.get(key);
    Map resourceMap;
    if ((value == null) || !((Boolean)value.getSecond()).booleanValue()){
      resourceMap = context.getResources(resourceType, plugin);
      if (resourceMap == null)
        return null;
      resourceCache.put(key, new Pair(resourceMap, Boolean.TRUE));
    }
    else
      resourceMap = (Map)value.getFirst();
    
    return Collections.unmodifiableMap(resourceMap);
  }
  
  
  
  /**
   * Returns the resource with the specified type and id.
   */
  
  public Resource getResource(String resourceType, String id, Plugin plugin){
    Pair key = new Pair(resourceType, plugin);
    Pair value = (Pair)resourceCache.get(key);
    Resource resource;
    
    if ((value == null) || ((resource = (Resource)((Map)value.getFirst()).get(id)) == null))
      resource = context.getResource(resourceType, id, plugin);
    
    if (value == null){
      Map resourceMap = new HashMap();
      resourceMap.put(id, resource);
      resourceCache.put(key, new Pair(resourceMap, Boolean.FALSE));
    }
    else{
      Map resourceMap = (Map)value.getFirst();
      resourceMap.put(id, resource);
    }
    
    return resource;
  }
  
  
  
  /**
   * Returns the list of known users (accounts on various servers). This list
   * will be updated as users are added or removed, so you may register
   * listeners with it if you wish to be notified. The list does not include
   * guest users.
   */

  public ListModel getUsers(){
    return users; 
  }
  
  
  
  /**
   * Adds the specified user to the list of known users.
   */
   
  public void addUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (user.isGuest())
      throw new IllegalArgumentException("user may not be a guest");

    users.addElement(user);    
  }
  
  
  
  /**
   * Removes the specified user from the list of known users.
   */
   
  public void removeUser(User user){
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (user.isGuest())
      throw new IllegalArgumentException("user may not be a guest");

    users.removeElement(user);
  }
  
  
  
  /**
   * Returns whether the specified User represents a "known" account, that is,
   * it appears in the list returned by <code>getUsers</code>.
   */

  public boolean isKnownUser(User user){
    for (int i = 0; i < users.getSize(); i++)
      if (users.getElementAt(i).equals(user))
        return true;

    return false;
  }
  
  
  
  /**
   * Returns the ui provider.
   */
   
  public UIProvider getUIProvider(){
    return uiProvider;
  }
  
  
  
  /**
   * Returns the connection manager.
   */
   
  public ConnectionManager getConnManager(){
    return connManager;
  }
  
  
  
  /**
   * Returns the server with the specified id. Returns <code>null</code> if no
   * such server found.
   */

  public Server getServerById(String id){
    Server [] servers = context.getServers();
    for (int i = 0; i < servers.length; i++)
      if (servers[i].getId().equals(id))
        return servers[i];

    return null;
  }
  
  
  
  /**
   * Returns a list of the users on the specified server.
   */
  
  public List getUsers(Server server){
    List serverUsers = new LinkedList();
    for (int i = 0; i < users.getSize(); i++){
      User user = (User)users.getElementAt(i);
      if (user.getServer() == server)
        serverUsers.add(user);
    }
    return serverUsers;
  }
  
  
  
  /**
   * Returns the user with the specified username on the specified server or
   * <code>null</code> if no such user exists. Doesn't work for guest users.
   */

  public User getUser(Server server, String username){
    for (int i = 0; i < users.getSize(); i++){
      User user = (User)users.getElementAt(i);
      if ((user.getServer() == server) && 
          server.getUsernamePolicy().isSame(username, user.getUsername()))
        return user;
    }

    return null;
  }
  
  
  
  /**
   * Returns whether the context in which Jin is running is capable of saving
   * user preferences.
   */
   
  public boolean isSavePrefsCapable(){
    return context.isSavePrefsCapable();
  }
  
  
  
  /**
   * Returns text warning the user about saving his password and asking him to
   * confirm it.
   */
   
  public String getPasswordSaveWarning(){
    return context.getPasswordSaveWarning();
  }
  
  
  
  /**
   * Returns whether the context in which Jin is running allows the end-user to
   * extend Jin by running extra plugins, actions, resources etc.
   */
   
  public boolean isUserExtensible(){
    return context.isUserExtensible();
  }
  
  
  
  /**
   * Causes the "About" dialog to be displayed.
   */
  
  public void showAboutDialog(){
    new AboutPanel().display();
  }
  
  
  
  /**
   * If no UI is currently visible, quits the application. This was originally
   * needed because in Java 1.1 the AWT thread didn't quit when all UI was
   * closed. It is still needed, however, because nowadays (1.5 on Mac OS X,
   * at least) there's an audio related non-daemon thread, which also doesn't
   * quit.
   */
  
  public void quitIfNoUiVisible(){
    if (!getUIProvider().isUiVisible())
      quit(false);
  }
  
  
  
  
  /**
   * Quits the application, possibly asking the user to confirm quitting first.
   * This method doesn't necessarily return.
   */
   
  public void quit(boolean askToConfirm){
    I18n i18n = I18n.get(Jin.class);
    
    Object result = askToConfirm ? 
      i18n.confirm(OptionPanel.OK, "quitConfirmationDialog", new Object[]{getAppName()}) : OptionPanel.OK;
    
    if (result == OptionPanel.OK){
      connManager.closeSession();
      uiProvider.stop();
      
      User [] usersArr = new User[users.size()];
      users.copyInto(usersArr);
      context.setUsers(usersArr);

      context.shutdown();
      
      instance = null;
    }
  }
  
  
  
}
