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
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;
import free.jin.action.ActionInfo;
import free.util.IOUtilities;
import free.util.PlatformUtils;
import free.util.AWTUtilities;



/**
 * The class responsible for starting up Jin.
 */
 
public class Jin{
  
  
  
  /**
   * The sole Jin instance.
   */
   
  private static Jin instance = null;
  
  
  
  /**
   * The context.
   */
   
  private final JinContext context;
  
  
  
  /**
   * Application (Jin) properties.
   */

  private final Properties appProps;
  
  
  
  /**
   * A list of known users (accounts on various servers).
   */
   
  private final DefaultListModel users;
  
  
  
  /**
   * The main Jin frame.
   */
   
  private final JFrame mainFrame;
  
  
  
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
      appProps = IOUtilities.loadProperties(Jin.class.getResourceAsStream("resources/app.props"));
    } catch (IOException e){
        e.printStackTrace();
        throw new IllegalStateException("Unable to load application properties from resources/app.props");
      }
      
    // Get known users (accounts on various servers);
    users = new DefaultListModel();
    User [] usersArr = context.getUsers();
    for (int i = 0; i < usersArr.length; i++)
      users.addElement(usersArr[i]);
      
    // Install any extra look and feels we're using
    installExtraLookAndFeels();

    // Restore the look and feel
    restoreLookAndFeel();
    
    // Apply Swing fixes
    fixSwing();

    // Create the main frame
    mainFrame = createMainFrame();
    
    // Restore the main frame geometry
    restoreFrameGeometry(mainFrame, "frame");

    // Create the UI manager
    uiProvider = new MdiUiProvider(this, TopLevelContainer.getFor(mainFrame, mainFrame.getTitle()));

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
    mainFrame.addWindowListener(new WindowAdapter(){
      public void windowOpened(WindowEvent evt){
        mainFrame.removeWindowListener(this);
        
        // Workaround - otherwise menu activation shortcuts don't work
        // immediately. Under OS X, in native menubar mode, this actually breaks things.
        if ((mainFrame.getJMenuBar() != null) &&
            (mainFrame.getFocusOwner() != null) &&
            !PlatformUtils.isMacOSX()){
          mainFrame.getJMenuBar().requestFocus();
        }

        connManager.start();
      }
    });
    mainFrame.setVisible(true);
  }
  
  
  
  
  /**
   * Installs any extra look and feels Jin is using.
   */

  private void installExtraLookAndFeels(){
    int extraLooksCount = Integer.parseInt(appProps.getProperty("lf.extra.count", "0"));
    for (int i = 0; i < extraLooksCount; i++){
      String name = appProps.getProperty("lf.extra." + i + ".name");
      String className = appProps.getProperty("lf.extra." + i + ".class");
      String minRequiredJavaVer = appProps.getProperty("lf.extra." + i + ".minRequiredJava", "0");
      if (PlatformUtils.isJavaBetterThan(minRequiredJavaVer)){
        try{
          Class.forName(className);
          UIManager.installLookAndFeel(name, className);
        } catch (ClassNotFoundException e){
            System.err.println("Unable to load class " + className + " for the " + name + " look and feel");
          }
      }
    }
  }



  /**
   * Sets the current look and feel to the one specified in user preferences.
   */

  private void restoreLookAndFeel(){
    String lfClassName = context.getPrefs().getString("lf.default", UIManager.getSystemLookAndFeelClassName());
    try{
      UIManager.setLookAndFeel(lfClassName);
    } catch (Exception e){}
  }
  


  /**
   * Saves the currently used look and feel into user preferences.
   */

  private void saveLookAndFeel(){
    context.getPrefs().setString("lf.default", UIManager.getLookAndFeel().getClass().getName());
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
   * Restores the geometry of the specified frame from the preferences.
   */
   
  private void restoreFrameGeometry(JFrame frame, String prefNamePrefix){
    Preferences prefs = context.getPrefs();
    
    Dimension screenSize = frame.getToolkit().getScreenSize();
    Rectangle defaultFrameBounds = new Rectangle(
      screenSize.width/16, screenSize.height/16, screenSize.width*7/8, screenSize.height*7/8);
    
      
    // Restore bounds      
    Rectangle frameBounds = prefs.getRect(prefNamePrefix + ".bounds", defaultFrameBounds);
    frameBounds = frameBoundsOk(screenSize, frameBounds) ? frameBounds : defaultFrameBounds;
    frame.setBounds(frameBounds);

    
    // Restore maximized state 
    boolean vertMaximized = prefs.getBool(prefNamePrefix + ".maximized.vert", false);
    boolean horizMaximized = prefs.getBool(prefNamePrefix + ".maximized.horiz", false);

    // Bugfix for Java bug 4464714 - setExtendedState only works once the
    // the window is realized.
    if (frame.getPeer() == null)
      frame.addNotify();
   
    int state = ((vertMaximized ? Frame.MAXIMIZED_VERT : 0) | (horizMaximized ? Frame.MAXIMIZED_HORIZ : 0));
    AWTUtilities.setExtendedFrameState(frame, state);
  }
  
  
  
  
  /**
   * Saves the geometry of the specified frame into the preferences
   * with preference names prefixed with the specified string.
   */

  private void saveFrameGeometry(JFrame frame, String prefNamePrefix){
    Preferences prefs = context.getPrefs();
    
    // Save bounds on screen
    Point frameLocation = frame.isVisible() ? frame.getLocationOnScreen() : frame.getLocation();
    Dimension frameSize = frame.getSize();
    prefs.setRect(prefNamePrefix + ".bounds", new Rectangle(frameLocation, frameSize));
    
    // Save maximized state
    int state = AWTUtilities.getExtendedFrameState(frame);
    prefs.setBool(prefNamePrefix + ".maximized.vert", (state & Frame.MAXIMIZED_VERT) != 0);
    prefs.setBool(prefNamePrefix + ".maximized.horiz", (state & Frame.MAXIMIZED_HORIZ) != 0);
  }
  
  

  /**
   * Returns whether the specified frame bounds are reasonably placed on a
   * screen of the specified dimensions. This is used to avoid situations where
   * a frame is displayed outside of the screen where the user can't change its
   * size and/or move it (can happen for example if the resolution is changed
   * between runs).
   */

  private static boolean frameBoundsOk(Dimension screenSize, Rectangle frameBounds){
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
   * plugin. Resources are typically used when there is a need to allow the user
   * (or some other 3rd party) to add his own customizations to Jin
   * (or a plugin). For example, this mechanism is used for loading piece sets
   * and boards by the board manager plugin. A <code>JinContext</code>
   * implementation may then look for piece set "packs" in some predefined
   * directories, allowing the user to add/remove piece sets simply by
   * adding/deleting files from those directories.
   */
   
  public Resource [] getResources(String resourceType, Plugin plugin){
    return (Resource [])context.getResources(resourceType, plugin).clone();
  }
  
  
  
  /**
   * Returns the resource with the specified type and id.
   */
  
  public Resource getResource(String resourceType, String id, Plugin plugin){
    return context.getResource(resourceType, id, plugin);
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
   * Quits the application, possibly asking the user to confirm quitting first.
   * This method doesn't necessarily return.
   */
   
  public void quit(boolean askToConfirm){
    Object result = askToConfirm ? 
      OptionPanel.confirm("Quit", "Quit Jin?", OptionPanel.OK) : OptionPanel.OK;
    
    if (result == OptionPanel.OK){
      connManager.closeSession();
      saveFrameGeometry(mainFrame, "frame");
      saveLookAndFeel();
      
      User [] usersArr = new User[users.size()];
      users.copyInto(usersArr);
      context.setUsers(usersArr);
  
      mainFrame.dispose();

      instance = null;      
      
      context.shutdown();
    }
  }
  
  
  
}
