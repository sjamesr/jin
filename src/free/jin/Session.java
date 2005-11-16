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

import java.io.IOException;
import java.util.Vector;

import free.jin.action.ActionContext;
import free.jin.action.ActionInfo;
import free.jin.action.JinAction;
import free.jin.event.ConnectionListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginContext;
import free.jin.plugin.PluginInfo;
import free.jin.plugin.PluginStartException;


/**
 * Represents a session with a chess server.
 */

public class Session{



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
   * The plugin context in this session.
   */
  
  private PluginContext pluginContext;



  /**
   * The port on which we've actually connected.
   */

  private int port = -1;



  /**
   * Creates a new <code>Session</code> with the specified
   * <code>ConnectionDetails</code>. The <code>Session</code> is not initially
   * connected - you must invoke the {@link #login()} method to connect and log
   * in.
   *
   * @throws PluginStartException creating, initializing or starting one of the
   * plugins fails.
   */

  public Session(ConnectionDetails connDetails)
      throws PluginStartException{
    if (connDetails == null)
      throw new IllegalArgumentException("connDetails may not be null");
   
    this.connDetails = connDetails; 
    this.conn = connDetails.getServer().createConnection(connDetails);

    this.actions = createActions();
    this.plugins = createPlugins();
  }
  
  
  
  /**
   * Creates the standalone actions for this session and returns an array of
   * them. Yes, I shouldn't reuse PluginStartException here, but so what :-p.
   */
   
  private JinAction [] createActions() throws PluginStartException{
    ActionInfo [] actionsInfo = Jin.getInstance().getActions(getServer());
    Vector actions = new Vector(actionsInfo.length);
    
    for (int i = 0; i < actionsInfo.length; i++){
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
      
      ActionContext actionContext = new ActionContext(conn, getUser(), prefs);
      
      if (action.setContext(actionContext))
        actions.addElement(action);
      else
        System.err.println("Warning: unsupported action not created - " + action.getId());
    }
    
    JinAction [] actionsArr = new JinAction[actions.size()];
    actions.copyInto(actionsArr);
    return actionsArr;
  }



  /**
   * Creates the plugins for this session and returns an array of them.
   */

  private Plugin [] createPlugins() throws PluginStartException{
    PluginInfo [] pluginsInfo = Jin.getInstance().getPlugins(getServer());
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
    pluginContext = 
      new PluginContext(conn, getUser(), plugins, pluginPrefs, actions);

    // Set context on plugins and start them
    pluginContext.setAndStart();
    
    return pluginContext.getPlugins();
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
    return getUser().getServer();
  }



  /**
   * Returns the <code>User</code> object representing the account for this
   * session.
   */

  public User getUser(){
    return connDetails.getUser();
  }



  /**
   * Returns an array containing the plugins in this session.
   */

  public synchronized Plugin [] getPlugins(){
    return (plugins == null) ? null : (Plugin [])plugins.clone();
  }
  
  
  
  /**
   * Returns the <code>PluginContext</code> for this session.
   */
  
  public PluginContext getPluginContext(){
    return pluginContext;
  }



  /**
   * Initiates connecting and logging in on the server.
   * The method itself returns without waiting for a connection to be established or for login to finish, but it causes
   * {@link ConnectionManager#loginFailed(String)} to be invoked if login fails.
   */

  synchronized void initiateLogin(){
    if (conn.isConnected())
      throw new IllegalArgumentException("Session already logged in");
    if (isClosed)
      throw new IllegalArgumentException("Session already closed - can't reuse session");
    
    new ConnectionInitializer(connDetails.getHost(), connDetails.getPorts()).go();
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
      if (conn.isConnected())
        conn.exit();
      
      conn.close();
    } catch (IOException e){
        System.err.println("Failed to disconnect connection: " + conn);
        e.printStackTrace();
      } 

    isClosed = true;
  }
  
  
  
  /**
   * Initiates connection and login and then listens for results. If login fails, informs ConnectionManager.
   */
  
  private class ConnectionInitializer implements ConnectionListener{
    
    
    
    /**
     * The hostname to connect to.
     */
    
    private final String hostname;
    
    
    
    /**
     * The ports to connect on.
     */
    
    private final int [] ports;
    
    
    
    /**
     * The error messages we receive when logging in on each port.
     */
    
    private final String [] errorMessages;
    
    
    
    /**
     * The index of the current port on which we're trying to connect.
     */
    
    private int portIndex = 0;
    
    
    
    /**
     * Creates a new ConnectionInitializer with the specified hostname to connect to and the specified ports to connect
     * on.
     */
    
    public ConnectionInitializer(String hostname, int [] ports){
      this.hostname = hostname;
      this.ports = ports;
      this.errorMessages = new String[ports.length];
    }
    
    
    
    /**
     * Initiates connection and login.
     */
    
    public void go(){
      conn.getListenerManager().addConnectionListener(this);
      conn.initiateConnectAndLogin(hostname, ports[portIndex]);
    }
    
    
    
    /**
     * Tries to connect on the next port, or, if all ports have been tried, notifies the ConnectionManager.
     */
    
    public void connectingFailed(Connection conn, String reason){
      errorMessages[portIndex] = reason;
      if (portIndex == ports.length - 1){ // All ports failed
        
        // Stop plugins
        for (int i = 0; i < plugins.length; i++)
          plugins[i].stop();

        // Create the error message
        StringBuffer errorMessage = new StringBuffer();
        for (int i = 0; i < errorMessages.length; i++)
          errorMessage.append("Port " + ports[i] + ": " + errorMessages[i] + "\n");
        errorMessage.setLength(errorMessage.length() - 1);
        
        Jin.getInstance().getConnManager().loginFailed(errorMessage.toString());
      }
      else{
        portIndex++;
        conn.initiateConnectAndLogin(hostname, ports[portIndex]);
      }
    }
    
    
    
    
    /**
     * Notifies the connection manager that login failed.
     */

    public void loginFailed(Connection conn, String reason){
      // Stop plugins
      for (int i = 0; i < plugins.length; i++)
        plugins[i].stop();
      
      Jin.getInstance().getConnManager().loginFailed(reason);
    }
    
    
    
    /**
     * Unregisters us as a connection listener, since connection succeeded.
     */
    
    public void loginSucceeded(Connection conn){
      conn.getListenerManager().removeConnectionListener(this);
    }
    
    
    
    /**
     * Sets the connection port in Session.
     */
    
    public void connectionEstablished(Connection conn){
      synchronized(Session.this){
        Session.this.port = ports[portIndex];
      }
    }

    
    
    // The rest of ConnectionListener's methods.
    public void connectionAttempted(Connection conn, String hostname, int port){}
    public void connectionLost(Connection conn){}
    
    
    
  }



}