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

import free.jin.plugin.PluginStartException;
import free.util.Utilities;
import free.util.TextUtilities;
import free.util.PlatformUtils;


/**
 * Manages the creation, initiation and closing of the connection to the server.
 */

public class ConnectionManager{



  /**
   * The current Session. Null when none.
   */

  private Session session = null;



  /**
   * Creates a new <code>ConnectionManager</code>.
   */

  public ConnectionManager(){
    
  }



  /**
   * Displays UI which allows the user to login to one of the supported servers.
   * This method is invoked (usually by the class responsible for the start-up
   * of the application) when all the start up initialization (preferences,
   * ui factory etc.) has finished and it's ok to start the normal flow of the
   * application.
   */

  public void start(){
    Server server = findLoginServer();
    if (server == null)
      return;
    
    User user = findLoginUser(server);
    
    ConnectionDetails connDetails = findConnDetails(server, user);
    if (connDetails == null)
      return;
    
    user = connDetails.isGuest() ? 
      server.getGuest() : Jin.getInstance().getUser(server, connDetails.getUsername());
    if (user == null) // New user
      user = new User(server, connDetails.getUsername());

    login(user, connDetails);
  }
  
  
  
  /**
   * Determines and returns the server we should login to, based on
   * (in that order):
   * <ol>
   *   <li> Application parameters.
   *   <li> Last logged in user.
   *   <li> Sole supported server.
   *   <li> Server choice panel displayed to the user.
   * </ol>
   * Returns <code>null</code> if all the above methods fail (implies that 
   * the user canceled the server choice panel).
   */
   
  private Server findLoginServer(){
    Jin jin = Jin.getInstance();
    
    // Application parameters
    String serverId = jin.getParameter("login.server");
    if (serverId != null){
      Server server = jin.getServerById(serverId);
      if (server == null)
        OptionPanel.error("Login Parameters Error", "Unknown server ID: " + serverId);
      else
        return server;
    }
    
    // Last logged in user
    User lastUser = getLastUser();
    if (lastUser != null)
      return lastUser.getServer();
    
    // Sole supported server
    if (jin.getServers().length == 1)
      return jin.getServers()[0];
    
    // Server choice panel
    Server askedServer = new ServerChoicePanel().askServer();
    if (askedServer != null)
      return askedServer;
    
    return null;
  }
  
  
  
  /**
   * Determines and returns the user account we should login with, based on
   * (in that order):
   * <ol>
   *   <li> Application parameters.
   *   <li> Last logged in user.
   * </ol>
   * Returns <code>null</code> if all the above methods fail.
   */
   
  private User findLoginUser(Server server){
    Jin jin = Jin.getInstance();
    
    // Application parameters
    boolean isGuest = new Boolean(jin.getParameter("login.guest")).booleanValue();
    String username = jin.getParameter("login.username");
    if (isGuest)
      return server.getGuest();
    else if (username != null){
      User user = jin.getUser(server, username);
      if (user == null) // New user
        return new User(server, username);
      else
        return user;
    }
    
    // Last logged in user
    User lastUser = getLastUser();
    if (lastUser != null)
      return lastUser;
    
    return null;
  }
  
  
  
  /**
   * Determines the connection details we should use when logging in, based on
   * (in that order, for each connection detail):
   * <ol>
   *   <li> Application parameters.
   *   <li> Specified user.
   *   <li> Specified server.
   * </ol>
   * Additionally, if the applications parameters allow displaying the login
   * panel, any connection details modified by the user there override all
   * others.
   * Returns <code>null</code> if all the above methods fail (implies that the
   * user canceled the login panel).
   * The specified user may be <code>null</code>, but not the server.
   */
   
  private ConnectionDetails findConnDetails(Server server, User user){
    Jin jin = Jin.getInstance();
    ConnectionDetails connDetails = user == null ? null : user.getPreferredConnDetails();
    
    String password = jin.getParameter("login.password");
    if ((password == null) && (connDetails != null))
      password = connDetails.getPassword();
    
    String savePassString = jin.getParameter("login.savepassword");
    boolean savePassword = new Boolean(savePassString).booleanValue();
    if ((savePassString == null) && (connDetails != null) && !connDetails.isGuest())
      savePassword = connDetails.isSavePassword();
    
    String hostname = jin.getParameter("login.hostname");
    if (hostname == null){
      if (connDetails != null)
        hostname = connDetails.getHost();
      else
        hostname = server.getDefaultHost();
    }
    
    String portsString = jin.getParameter("login.ports");
    int [] ports;
    if (portsString != null)
      ports = TextUtilities.parseIntList(portsString, ",");
    else if (connDetails != null)
        ports = connDetails.getPorts();
    else
      ports = server.getPorts();
    
    
    if (user == null)
      connDetails = null;
    else if (user.isGuest())
      connDetails = ConnectionDetails.createGuest(user.getUsername(), hostname, ports);
    else
      connDetails = ConnectionDetails.create(user.getUsername(), password, savePassword, hostname, ports);
      
    
    // Must show the login dialog if the user is null
    if ((user == null) || !(new Boolean(jin.getParameter("autologin")).booleanValue()))
      connDetails = new LoginPanel(server, connDetails).askConnectionDetails();
    
    return connDetails; 
  }
  
  
  
  /**
   * Displays UI for creating a new connection.
   */
   
  public void displayNewConnUI(){
    Server server;
    
    Server [] servers = Jin.getInstance().getServers();
    // Sole supported server
    if (servers.length == 1)
      server = servers[0];
    else{  // Server choice panel
      server = new ServerChoicePanel().askServer();
      if (server == null)
        return;
    }
    
    displayNewConnUI(server, null);
  }
  
  
  
  /**
   * Displays UI for creating a new connection to the specified server and
   * with the specified default connection details. The default connection
   * details may be <code>null</code>.
   */
   
  public void displayNewConnUI(Server server, ConnectionDetails defaultConnDetails){
    ConnectionDetails connDetails = new LoginPanel(server, defaultConnDetails).askConnectionDetails();
    if (connDetails == null)
      return;
    
    User user = connDetails.isGuest() ? 
      server.getGuest() : Jin.getInstance().getUser(server, connDetails.getUsername());
    if (user == null) // New user
      user = new User(server, connDetails.getUsername());
    
    login(user, connDetails);
  }
  
  
  
  
  /**
   * Initiates login for the specified user with the specified connection
   * details. Neither value may be <code>null</code>.
   */
   
  private void login(User user, ConnectionDetails connDetails){
    try{
      session = new Session(user, connDetails);
      Jin.getInstance().getUIProvider().setConnected(true, session);
      new LoginThread(session).start();
    } catch (PluginStartException e){
        e.printStackTrace();
        Exception reason = e.getReason();

        String errorMessage = e.getMessage() + "\n" + 
          (reason == null ? "" : reason.getClass().getName() + ": " + reason.getMessage());
        OptionPanel.error("Error", errorMessage);
      }
  }
  
  
  
  /**
   * Closes the current session. The call is ignored if there is no current
   * session.
   */

  public void closeSession(){
    if (session == null)
      return;

    // Close the session
    session.close();

    User user = session.getUser();
    int connPort = session.getPort();

    if (connPort != -1){ // Actually connected
      
      // Set preferred connection details for this account
      ConnectionDetails connDetails = session.getConnDetails().usePort(connPort);
      user.setPreferredConnDetails(connDetails);

      // Add the user to the known users list
      if (!user.isGuest() && !Jin.getInstance().isKnownUser(user)){
        boolean rememberUser =
          !Jin.getInstance().isSavePrefsCapable() ||
          OptionPanel.question("Remember Account?", "Would you like Jin to remember the " 
          + user.getUsername() + " account?", OptionPanel.YES) == OptionPanel.YES;
          
        if (rememberUser){
          Jin.getInstance().addUser(user);
          saveLastUser(user);
        }
      }
      else{
        user.markDirty();
      }
    }

    Jin.getInstance().getUIProvider().setConnected(false, session);

    session = null;
  }



  /**
   * Returns the current <code>Session</code>, or <code>null</code> if none.
   */

  public Session getSession(){
    return session;
  }



  /**
   * Returns the <code>User</code> object of the last account that logged on.
   * Returns <code>null</code> if none.
   */

  private User getLastUser(){
    Preferences prefs = Jin.getInstance().getPrefs();
    String serverId = prefs.getString("last-login.serverId", null);
    String username = prefs.getString("last-login.username", null);

    if (serverId == null)
      return null;

    Server server = Jin.getInstance().getServerById(serverId);
    if (username == null)
      return server.getGuest();
    else
      return Jin.getInstance().getUser(server, username);
  }



  /**
   * Saves the information about the last account that logged in into
   * preferences.
   */

  private void saveLastUser(User user){
    Preferences prefs = Jin.getInstance().getPrefs();
    String serverId = user.getServer().getId();
    String oldServerId = prefs.getString("last-login.serverId", null);

    prefs.setString("last-login.serverId", serverId);
    if (!Utilities.areEqual(serverId, oldServerId))
      prefs.setString("last-login.username",
        user.isGuest() || !Jin.getInstance().isKnownUser(user) ? null : user.getUsername());
    else if (!user.isGuest())
      prefs.setString("last-login.username", user.getUsername());
  }



  /**
   * The thread that connects and logs in to the server.
   */

  private class LoginThread extends Thread{



    /**
     * The session.
     */

    private final Session session;



    /**
     * Creates a new <code>LoginThread</code> with the specified session.
     */

    public LoginThread(Session session){
      super("LoginThread-" + session.getServer().getId());

      this.session = session;
    }



    /**
     * Invoked {@link Session#login()} on the <code>Session</code> object
     * specified in the constructor.
     */

    public void run(){
      try{
        session.login();
      } catch (LoginException e){
          String errorMessage = "Error logging in:\n" + e.getMessage();
          OptionPanel.error("Login Error", errorMessage);
        }
    }


  }



}