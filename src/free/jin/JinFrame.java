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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import free.jin.plugin.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;


/**
 * Jin's main frame.
 */

public class JinFrame extends JFrame{


  
  /**
   * An array containing all the servers known by Jin.
   */

  private final Server [] knownServers;




  /**
   * Maps Connections to arrays of plugins for those connections.
   */

  private final Hashtable connsToPlugins = new Hashtable();




  /**
   * Maps Connections to Users assosiated with those connections.
   */

  private final Hashtable connsToUsers = new Hashtable();



  /**
   * Creates a new JinFrame.
   */

  public JinFrame(){
    Vector servers = new Vector();
    Enumeration serverEnum = Jin.getServers();
    while (serverEnum.hasMoreElements())
      servers.addElement(serverEnum.nextElement());
    knownServers = new Server[servers.size()];
    servers.copyInto(knownServers);

    setJMenuBar(createJMenuBar());

//    getToolkit().getSystemEventQueue().push(new EventQueue(){
//
//      protected void dispatchEvent(AWTEvent evt){
//        super.dispatchEvent(evt);
//
//        if (evt instanceof KeyEvent){
//          System.out.println("KeyEvent: "+evt);
//          System.out.println("Focus owner: "+getFocusOwner());
//          System.out.println();
//        }
//      }
//
//    });

    enableEvents(AWTEvent.WINDOW_EVENT_MASK|AWTEvent.FOCUS_EVENT_MASK);
  }




  /**
   * Creates the RootPane pane for Jin's main frame.
   */

  protected JRootPane createRootPane(){
    return new JinRootPane(this);
  }




  /**
   * We set this to true if turning off the expensive RenderingHints failed
   * (because it's a JDK1.1 system for example) so that we know not to try it
   * again.
   */

  private static boolean clearingExpensiveRenderingHintsFailed = false;



  
  /**
   * Disables all antialiasing, dithering and other expensive settings from the
   * returned Graphics object. This is hackish as it obviously doesn't work with
   * double buffering (since then the Graphics object is that of the offscreen
   * Image), but it's only (currently) useful on Mac OS X where we disable swing
   * double buffering anyway, because OS X does double buffering by itself.
   */

  public Graphics getGraphics(){
    Graphics g = super.getGraphics();

    if (clearingExpensiveRenderingHintsFailed)
      return g;

    try{
      Class graphics2DClass = Class.forName("java.awt.Graphics2D");
      Class rhClass = Class.forName("java.awt.RenderingHints");
      Class rhKeyClass = Class.forName("java.awt.RenderingHints$Key");
      Method setRenderingHintMethod = graphics2DClass.getMethod("setRenderingHint", new Class[]{rhKeyClass, Object.class});
      Object [][] renderingHintsKeyValue = new Object[][]{
        {rhClass.getField("KEY_ALPHA_INTERPOLATION").get(null), rhClass.getField("VALUE_ALPHA_INTERPOLATION_SPEED").get(null)},
        {rhClass.getField("KEY_ANTIALIASING").get(null), rhClass.getField("VALUE_ANTIALIAS_OFF").get(null)},
        {rhClass.getField("KEY_COLOR_RENDERING").get(null), rhClass.getField("VALUE_COLOR_RENDER_SPEED").get(null)},
        {rhClass.getField("KEY_DITHERING").get(null), rhClass.getField("VALUE_DITHER_DISABLE").get(null)},
        {rhClass.getField("KEY_INTERPOLATION").get(null), rhClass.getField("VALUE_INTERPOLATION_NEAREST_NEIGHBOR").get(null)},
        {rhClass.getField("KEY_RENDERING").get(null), rhClass.getField("VALUE_RENDER_SPEED").get(null)},
        {rhClass.getField("KEY_TEXT_ANTIALIASING").get(null), rhClass.getField("VALUE_TEXT_ANTIALIAS_OFF").get(null)},
        {rhClass.getField("KEY_FRACTIONALMETRICS").get(null), rhClass.getField("VALUE_FRACTIONALMETRICS_OFF").get(null)}
      };

      for (int i = 0; i < renderingHintsKeyValue.length; i++)
        setRenderingHintMethod.invoke(g, renderingHintsKeyValue[i]);
    } catch (Exception e){
        if (e instanceof RuntimeException)
          throw (RuntimeException)e;
        System.err.println("Turning expensive rendering hints off failed due to: "+e.getClass().getName()+": "+e.getMessage());
        clearingExpensiveRenderingHintsFailed = true;
      }

    return g;
  }




  /**
   * Creates the initial JMenuBar used by Jin's main frame.
   */

  protected JMenuBar createJMenuBar(){
    return new JinFrameMenuBar(this);
  }





  /**
   * Returns Jin's desktop, where all the JInternalFrames reside.
   */

  public JDesktopPane getDesktop(){
    return ((JinRootPane)getRootPane()).getDesktop();
  }




  /**
   * Returns Jin's status bar.
   */

  public JPanel getStatusbar(){
    return ((JinRootPane)getRootPane()).getStatusbar();
  }




  /**
   * Returns the menubar of this JinFrame.
   */

  public JinFrameMenuBar getJinFrameMenuBar(){
    return (JinFrameMenuBar)super.getJMenuBar();
  }






  /**
   * Shows some UI allowing the user to create a new connection.
   */

  public void showConnectionCreationUI(){
    Server chosenServer;

    if (knownServers.length>1){
      Object result = JOptionPane.showInputDialog(this,"Choose a server to connect to",
        "Choose server",JOptionPane.QUESTION_MESSAGE,null,knownServers,null);

      if (result==null)
        return;

      chosenServer = (Server)result;
    }
    else if (knownServers.length==0){
      JOptionPane.showMessageDialog(this, "No defined servers, will exit.");
      System.exit(0);
      return; // Silly compiler
    }
    else
      chosenServer = knownServers[0];

    User defaultUser = chosenServer.createDefaultUser();
    showLoginDialog(defaultUser);
  }





  /**
   * Shows the LoginDialog for the given User.
   */

  public void showLoginDialog(User user){
    Server server = user.getServer();
    LoginDialog loginDialog = server.createLoginDialog();
    loginDialog.setHintUser(user);
    loginDialog.show(this);
    if (loginDialog.isCanceled())
      return;

    JinConnection conn = loginDialog.createConnection();
    user = loginDialog.getUser();
    startConnection(conn, user);
  }





  /**
   * Creates and starts all the necessary plugins that should be created for the
   * given user, then connects the given JinConnection to the server. The connecting
   * part is done asynchronously because JinConnection.connect() blocks.
   */

  private void startConnection(final JinConnection conn, User user){

    // Phase 1 - initialize the plugins
    String pluginsString = user.getProperty("plugins","");
    Hashtable plugins = new Hashtable();
    Hashtable pluginsToProperties = new Hashtable();
    StringTokenizer pluginsTokenizer = new StringTokenizer(pluginsString,";");
    if (pluginsTokenizer.countTokens()==0){ // No plugins
      System.err.println("There are no plugins assosiated with "+user+" - will not be connecting");
      return;
    }
    Plugin [] pluginsArr = new Plugin[pluginsTokenizer.countTokens()];
    while (pluginsTokenizer.hasMoreTokens()){
      String pluginName = pluginsTokenizer.nextToken();
      try{
        InputStream in = getClass().getResourceAsStream("resources/plugins/"+pluginName+".properties");
        Properties pluginProperties = new Properties();
        pluginProperties.load(in);
        in.close();

        Class pluginClass = Class.forName(pluginProperties.getProperty("class"));
        Plugin plugin = (Plugin)pluginClass.newInstance();
        plugins.put(pluginName,plugin);
        pluginsToProperties.put(plugin,pluginProperties);
        pluginsArr[plugins.size()-1] = plugin;
      } catch (Exception e){                                         
          synchronized(System.err){
            System.err.println("Unable to load plugin "+pluginName);
            e.printStackTrace();
          }
        }
    }


    // Phase 2 - set the plugins' context
    Enumeration pluginsEnum = plugins.elements();
    while (pluginsEnum.hasMoreElements()){
      Plugin plugin = (Plugin)pluginsEnum.nextElement();
      PluginContext context = new PluginContext(user,conn,this,(Properties)pluginsToProperties.get(plugin),plugins);
      try{
        plugin.setContext(context);
      } catch (UnsupportedContextException e){
          System.out.println("The plugin \""+plugin.getID()+"\" doesn't support the given server or connection implementation, reason: "+e.getMessage());
        }
        catch (RuntimeException e){ // Make sure that one bad plugin doesn't spoil for the rest.
          e.printStackTrace();
        }
    }



    // Phase 3 - start the plugins
    pluginsEnum = plugins.elements();
    while (pluginsEnum.hasMoreElements()){
      Plugin plugin = (Plugin)pluginsEnum.nextElement();
      try{
        plugin.start();
      } catch (RuntimeException e){ // Make sure that one bad plugin doesn't spoil for the rest.
          e.printStackTrace();
        }
    }



    // Phase 4 - Tell JinFrameMenuBar to add the required menus on-connect.
    JinFrameMenuBar menubar = getJinFrameMenuBar();
    pluginsEnum = plugins.elements();
    menubar.connecting(conn, pluginsEnum);


    // Phase 5 - connect.
    new Thread(){

      public void run(){
        try{
          conn.connect();
        } catch (IOException e){
            synchronized(System.err){
              System.err.println("Unable to connect to server");
              e.printStackTrace();
            }
          }
      }

    }.start();


    // Map the connection to the plugins.
    connsToPlugins.put(conn, pluginsArr);

    // Map the connection to the user.
    connsToUsers.put(conn, user);
  }





  /**
   * Stops the given JinConnection by stopping all its plugins and closing the
   * JinConnection.
   */

  public void closeConnection(JinConnection conn){
    Plugin [] plugins = (Plugin [])connsToPlugins.get(conn);
    for (int i=0;i<plugins.length;i++)
      try{
        Plugin plugin = plugins[i];
        if (plugin!=null){
          System.out.println("Stopping "+plugin.getName());
          plugin.stop();  
        }
      } catch (Throwable e){ // Make sure plugins don't bother other plugins.
          synchronized(System.err){
            System.err.println("An exception occurred while stopping a plugin:");
            e.printStackTrace();
          }
        }

    System.out.println("Closing connection");
    try{
      if (conn.isConnected()){
        conn.exit();
        conn.disconnect();
      }
    } catch (IOException e){
        synchronized(System.err){
          System.err.println("Failed to disconnect connection: "+conn);
          e.printStackTrace();
        }
      } 

    User user = (User)connsToUsers.get(conn);

    if (Jin.isKnownUser(user)){
      /*
      if (user.isUserModified()){
        System.out.println("Querying user about saving settings");
        int result = JOptionPane.showConfirmDialog(this,"Save "+user.getUsername()+"'s settings into "+user.getFilename()+"?","Save settings?",JOptionPane.YES_NO_OPTION);
        if (result==JOptionPane.YES_OPTION){
          System.out.println("Saving user settings");
          Jin.save(user);
        }
        else{
          System.out.println("Restoring user's original settings");
          user.restore();
        }
      }
      else{
        System.out.println("Saving user's implicit settings");
        Jin.save(user);
      }
      */

      // Isn't it stupid to ask the user whether he wants to save his settings?
      System.out.println("Saving user settings");
      Jin.save(user);
    }
    else{
      if (user.getUsername()!=null){ // Null means a guest.
        System.out.println("Querying user about creating a new account");
        int result = JOptionPane.showConfirmDialog(this,"Would you like to create a new user named \""+user.getUsername()+"\"?","Create new user?",JOptionPane.YES_NO_OPTION);
        if (result==JOptionPane.YES_OPTION){
          System.out.println("Creating new user, named "+user.getUsername());
          Jin.save(user);
        }
      }
    }

    String userFilename = user.getFilename();
    if (userFilename!=null){
      System.out.println("Saving last user information");
      Jin.setProperty("last.user", userFilename);
    }
    
    System.out.println("Removing connection from mappings");
    connsToPlugins.remove(conn);
    connsToUsers.remove(conn);

    System.out.println("Modifying menubar");
    JinFrameMenuBar menubar = getJinFrameMenuBar();
    menubar.disconnected(conn);

    // Bugfix
    menubar.repaint();
  }





  /**
   * This method stops all the plugins and closes all the connections. It's
   * called just before Jin exits.
   */

  public void exiting(){
    Enumeration conns = connsToPlugins.keys();
    while (conns.hasMoreElements()){
      JinConnection conn = (JinConnection)conns.nextElement();
      closeConnection(conn);
    }
  } 






  /**
   * Shows the login dialog to the user on WINDOW_OPENED event.
   */

  protected void processWindowEvent(WindowEvent evt){
    super.processWindowEvent(evt);
    if (evt.getID()==WindowEvent.WINDOW_OPENED){
      String lastUserFilename = Jin.getProperty("last.user");
      if (lastUserFilename!=null){
        User user = Jin.getUserByFilename(lastUserFilename);
        if (user!=null){
          showLoginDialog(user);
          return;
        }
      }
      showConnectionCreationUI();
    }
  }




  /**
   * Calls requestDefaultFocus() on the Desktop to make sure the focus goes to
   * the component that wants it.
   */

  protected void processFocusEvent(FocusEvent evt){
    super.processFocusEvent(evt);
    if (evt.getID()==FocusEvent.FOCUS_GAINED){
      getDesktop().requestDefaultFocus();
    }
  }

}
