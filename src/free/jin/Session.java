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

import free.jin.plugin.*;
import free.jin.action.*;
import java.io.IOException;


/**
 * Represents a session with a chess server.
 */

public class Session{



  /**
   * The context.
   */

  private final JinContext context;



  /**
   * The <code>User</code> object representing the account we're logging in for.
   */

  private final User user;



  /**
   * The <code>ConnectionDetails</code> which we use to connect and log in.
   */

  private final ConnectionDetails connDetails;



  /**
   * The connection to the server.
   */

  private final Connection conn;


  
  /**
   * True when this session has been closed.
   */

  private boolean isClosed = false;


  
  /**
   * An array of the standalone actions in this session.
   */
   
  private final JinAction [] actions; 
  
  

  /**
   * An array of the plugins in this session.
   */

  private final Plugin [] plugins;



  /**
   * The port on which we've actually connected.
   */

  private int port = -1;



  /**
   * Creates a new <code>Session</code> for the specified account with the
   * specified <code>ConnectionDetails</code>. The <code>Session</code> is not
   * initially connected - you must invoke the {@link #login()} method to
   * connect and log in.
   *
   * @throws PluginStartException creating, initializing or starting one of the
   * plugins fails.
   */

  public Session(JinContext context, User user, ConnectionDetails connDetails)
      throws PluginStartException{
    if (context == null)
      throw new IllegalArgumentException("context may not be null");
    if (user == null)
      throw new IllegalArgumentException("user may not be null");
    if (connDetails == null)
      throw new IllegalArgumentException("connDetails may not be null");
   
    Server server = user.getServer();

    this.context = context;
    this.user = user;
    this.connDetails = connDetails; 
    this.conn =
      server.createConnection(context, connDetails.getUsername(), connDetails.getPassword());

    this.actions = createActions();
    this.plugins = createPlugins();
  }
  
  
  
  /**
   * Creates the standalone actions for this session and returns an array of
   * them. Yes, I shouldn't reuse PluginStartException here, but so what :-p.
   */
   
  private JinAction [] createActions() throws PluginStartException{
    ActionInfo [] actionsInfo = context.getActions(getServer());
    JinAction [] actions = new JinAction[actionsInfo.length];
    
    for (int i = 0; i < actions.length; i++){
      ActionInfo info = actionsInfo[i];
      
      Class actionClass = info.getActionClass();
      Preferences prefs = info.getActionPreferences();
      
      JinAction action;
      
      try{
        action = (JinAction)actionClass.newInstance();
      } catch (InstantiationException e){
          throw new PluginStartException(e, "Unable to instantiate " + actionClass);
        }
        catch (IllegalAccessException e){
          throw new PluginStartException(e, "Unable to instantiate " + actionClass);
        }
      
      ActionContext actionContext = new ActionContext(context, conn, user, prefs);
      action.setContext(actionContext);
      
      actions[i] = action;
    }
    
    return actions;
  }



  /**
   * Creates the plugins for this session and returns an array of them.
   */

  private Plugin [] createPlugins() throws PluginStartException{
    PluginInfo [] pluginsInfo = context.getPlugins(getServer());
    Plugin [] plugins = new Plugin[pluginsInfo.length];

    Preferences [] pluginPrefs = new Preferences[pluginsInfo.length];
    for (int i = 0; i < pluginPrefs.length; i++)
      pluginPrefs[i] = pluginsInfo[i].getPluginPreferences();

    // Instantiate plugins
    for (int i = 0; i < plugins.length; i++){
      Class pluginClass = pluginsInfo[i].getPluginClass();
      try{
        plugins[i] = (Plugin)pluginClass.newInstance();
      } catch (InstantiationException e){
          throw new PluginStartException(e, "Unable to instantiate " + pluginClass);
        }
        catch (IllegalAccessException e){
          throw new PluginStartException(e, "Unable to instantiate " + pluginClass);
        }
    }

    // Create plugin context
    PluginContext pluginContext = new PluginContext(context, conn, user, plugins, pluginPrefs, actions);

    // Set context on plugins
    for (int i = 0; i < plugins.length; i++)
      plugins[i].setContext(pluginContext);
    
    // Start the plugins
    for (int i = 0; i < plugins.length; i++){
      Plugin plugin = plugins[i];
      try{
        plugin.start();
      } catch (Exception e){
          throw new PluginStartException(e, "Failed to start plugin: " + plugin);
        }
    }

    return plugins;
  }



  /**
   * Returns the connection details of this <code>Session</code>.
   */

  public ConnectionDetails getConnDetails(){
    return connDetails;
  }



  /**
   * Returns the port on which we've actually connected, or -1 if we haven't
   * connected yet.
   */

  public synchronized int getPort(){
    return port;
  }



  /**
   * Returns the server to which this session is.
   */

  public Server getServer(){
    return user.getServer();
  }



  /**
   * Returns the <code>User</code> object representing the account for this
   * session.
   */

  public User getUser(){
    return user;
  }



  /**
   * Returns an array containing the plugins in this session.
   */

  public synchronized Plugin [] getPlugins(){
    return (plugins == null) ? null : (Plugin [])plugins.clone();
  }



  /**
   * Creates and starts the plugins, then connects and logs on to the server.
   * Note that this method blocks until the connection is established (or until
   * it fails to connect) and the account is logged in.
   *
   * @throws LoginException if login fails for some reason.
   */

  public void login() throws LoginException{
    synchronized(this){
      if (conn.isConnected())
        throw new IllegalArgumentException("Session already logged in");
      if (isClosed)
        throw new IllegalArgumentException("Session already closed - can't reuse session");
    }

    String hostname = connDetails.getHost();
    int [] ports = connDetails.getPorts();

    // The error message for trying to connect to the corresponding port.
    String [] errorMessages = new String[ports.length];

    // Try the ports until one works
    for (int portIndex = 0; portIndex < ports.length; portIndex++){
      try{
        boolean result = conn.connectAndLogin(hostname, ports[portIndex]);
        synchronized(this){
          if (!result)
            throw new LoginException(conn.getLoginErrorMessage());
          this.port = ports[portIndex];
          break;
        }
      } catch (IOException e){
          synchronized(this){
            if (isClosed)
              throw new LoginException("Session closed while connecting");
            errorMessages[portIndex] = e.getMessage();
          }
        }
    }

    synchronized(this){
      // Failed to establish connection on all ports
      if (!conn.isConnected()){
        isClosed = true;

        // Stop plugins
        for (int i = 0; i < plugins.length; i++)
          plugins[i].stop();

        // Create and throw a login exception
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < errorMessages.length; i++)
          buf.append("Port " + ports[i] + ": " + errorMessages[i] + "\n");

        buf.setLength(buf.length() - 1);
        throw new LoginException(buf.toString());
      }
    }
  }



  /**
   * Returns whether we are currently actually connected to the server. Note
   * that we may be disconnected but the session isn't closed yet.
   */

  public boolean isConnected(){
    return conn.isConnected();
  }



  /**
   * Closes this session, disconnecting from the server (if necessary) and
   * stopping all the plugins.
   */

  public synchronized void close(){
    for (int i = 0; i < plugins.length; i++)
      plugins[i].saveState();

    for (int i = 0; i < plugins.length; i++)
      plugins[i].stop();

    try{
      if (conn.isConnected()){
        conn.exit();
        conn.disconnect();
      }
    } catch (IOException e){
        System.err.println("Failed to disconnect connection: " + conn);
        e.printStackTrace();
      } 

    isClosed = true;
  }



}