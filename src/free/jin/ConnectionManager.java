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
import free.util.PlatformUtils;


/**
 * Manages the creation, initiation and closing of the connection to the server.
 */

public class ConnectionManager{



  /**
   * The context in which we're running.
   */

  private final JinContext context;



  /**
   * The current Session. Null when none.
   */

  private Session session = null;



  /**
   * Creates a new <code>ConnectionManager</code> with the specified context.
   */

  public ConnectionManager(JinContext context){
    this.context = context;
  }



  /**
   * This method is invoked (usually by the class responsible for the start-up
   * of the application) when all the start up initialization (preferences,
   * ui factory etc.) has finished and it's ok to start the normal flow of the
   * application.
   */

  public void start(){
    User lastUser = getLastUser();

    if (lastUser == null)
      showNewConnectionUI(); 
    else      // Get server and connection details from last logged in user
      showLoginInformationUI(lastUser.getServer(), lastUser.getPreferredConnDetails());
  }



  /**
   * Displays a server choice dialog and calls
   * {@link #showLoginInformationUI(Server, ConnectionDetails)} with the result.
   */

  public void showNewConnectionUI(){
    Server [] servers = context.getServers();
    if (context.getServers().length == 1) // Only one server available - use it
      showLoginInformationUI(servers[0], null);
    else{                                 // Ask the user which server to connect to
      ServerChoicePanel serverChoicePanel = new ServerChoicePanel(servers);
      Server server = serverChoicePanel.askServer(context.getUIProvider());
      if (server == null)
        return;

      showLoginInformationUI(server, null);
    }
  }



  /**
   * Displays the user a login dialog and logs in with the connection details
   * he specifies by invoking {@link #login(Server, ConnectionDetails)}.
   */

  public void showLoginInformationUI(Server server, ConnectionDetails defaultConnDetails){

    // show login dialog
    LoginPanel loginPanel = new LoginPanel(context, server, defaultConnDetails);
    ConnectionDetails connDetails = loginPanel.askConnectionDetails(context.getUIProvider());

    if (connDetails == null) // user canceled the dialog
      return;

    if (connDetails.isGuest())
      login(server.getGuest(), connDetails);
    else{
      User user = JinUtilities.getUser(context, server, connDetails.getUsername());
      if (user == null)
        user = new User(server, connDetails.getUsername());

      login(user, connDetails);
    }
  }



  /**
   * Logs on the specified account with the specified connection details.
   */

  public void login(User user, ConnectionDetails connDetails){
    try{

      session = new Session(context, user, connDetails);

      context.getUIProvider().setConnected(true, session);

      new LoginThread(session).start();

    } catch (PluginStartException e){
        e.printStackTrace();
        Exception reason = e.getReason();

        String errorMessage = e.getMessage() + "\n" + 
          (reason == null ? "" : reason.getClass().getName() + ": " + reason.getMessage());
        OptionPanel.error(context.getUIProvider(), "Plugin Error", errorMessage);
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
    boolean storeUser = true;
    int connPort = session.getPort();

    if (connPort != -1){ // Actually connected
      // Set preferred connection details for this account
      ConnectionDetails connDetails = session.getConnDetails().usePort(connPort);
      user.setPreferredConnDetails(connDetails);

      if (user.isGuest()){
        // Microsoft VM returns "C:\windows\java" as the user.home directory.
        // Since this is shared between all users of the computer, we warn the
        // user about it.
        if (PlatformUtils.isOldMicrosoftVM()){
          Object result = OptionPanel.question(context.getUIProvider(), "Save Preferences?",
            "Would you like to save the preferences for the guest account?\n" +
            "Note that they will be shared with other users of this computer", OptionPanel.YES);
          if (result != OptionPanel.YES)
            storeUser = false;
        }
      }
      else{
        // Check whether it's a new account. If not, and the user wishes so, add it.
        if (!JinUtilities.isKnownUser(context, user)){
          Object result = OptionPanel.question(context.getUIProvider(), "Save Account?",
            "Would you like to save the preferences for this account?", OptionPanel.YES);
          if (result == OptionPanel.YES)
            context.addUser(user);
          else
            storeUser = false;
        }
      }
    }
    else
      storeUser = false;

    // This needs to be called after the user has been added, but before
    // his preference were saved, because it needs to know whether the user has
    // been kept but may set its own preferences
    context.getUIProvider().setConnected(false, session);

    if (storeUser){
      context.storeUser(user);
      saveLastUser(user);
    }


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
    Preferences prefs = context.getPrefs();
    String serverId = prefs.getString("last-login.serverId", null);
    String username = prefs.getString("last-login.username", null);

    if (serverId == null)
      return null;

    Server server = JinUtilities.getServerById(context, serverId);
    if (username == null)
      return server.getGuest();
    else
      return JinUtilities.getUser(context, server, username);
  }



  /**
   * Saves the information about the last account that logged in into
   * preferences.
   */

  private void saveLastUser(User user){
    Preferences prefs = context.getPrefs();
    String serverId = user.getServer().getId();
    String oldServerId = prefs.getString("last-login.serverId", null);

    prefs.setString("last-login.serverId", serverId);
    if (!Utilities.areEqual(serverId, oldServerId))
      prefs.setString("last-login.username",
        user.isGuest() || !JinUtilities.isKnownUser(context, user) ? null : user.getUsername());
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
          OptionPanel.error(context.getUIProvider(), "Login Error", errorMessage);
        }
    }


  }



}