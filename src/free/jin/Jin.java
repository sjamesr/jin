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
import free.jin.ui.MdiUiProvider;
import free.jin.ui.OptionPanel;
import free.jin.ui.SdiUiProvider;
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
   * The sole Jin instance.
   */
   
  private static volatile Jin instance = null;
  
  
  
  /**
   * The context.
   */
   
  private final JinContext context;
  
  
  
  /**
   * Application (Jin) properties.
   */

  private final Properties appProps;
  
  
  
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
    
    // Load application properties
    try{
      appProps = IOUtilities.loadPropertiesAndClose(Jin.class.getResourceAsStream("resources/app.props"));
    } catch (IOException e){
        e.printStackTrace();
        throw new IllegalStateException("Unable to load application properties from resources/app.props");
      }
    
    // Get known users (accounts on various servers);
    users = new DefaultListModel();
    User [] usersArr = context.getUsers();
    for (int i = 0; i < usersArr.length; i++)
      users.addElement(usersArr[i]);
      
    // Restore the look and feel
    restoreLookAndFeel();
    
    // Apply Swing fixes
    fixSwing();

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
    
    String lfClassName = getPrefs().getString("lookAndFeel.classname", null);
    
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
    
    // lnf selection UI needs to know this, if we're using the default one 
    getPrefs().setString("lookAndFeel.classname", UIManager.getLookAndFeel().getClass().getName()); 
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
   * Creates the UIProvider based on user preferences.
   */
  
  private UIProvider createUiProvider(){
    String uiProviderClassname = PlatformUtils.isMacOS() ? 
        SdiUiProvider.class.getName() : MdiUiProvider.class.getName();
    uiProviderClassname = getPrefs().getString("uiProvider.classname", uiProviderClassname);
    
    // ui provider selection UI needs to know this, if we're using the default one
    getPrefs().setString("uiProvider.classname", uiProviderClassname);
    
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
   * Returns the value of the specified application property, or
   * the specified default value if none exists.
   */
  
  public String getAppProperty(String propName, String defaultValue){
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
   * If no UI is currently visible, quits the application.
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
      i18n.confirm(OptionPanel.OK, "quitConfirmationDialog") : OptionPanel.OK;
    
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
