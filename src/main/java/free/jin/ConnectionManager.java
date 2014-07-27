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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.google.common.collect.Iterables;

import free.jin.event.ConnectionListener;
import free.jin.plugin.PluginStartException;
import free.jin.ui.DialogPanel;
import free.jin.ui.LoginPanel;
import free.jin.ui.OptionPanel;
import free.jin.ui.ServerChoicePanel;
import free.util.EventListenerList;
import free.util.TextUtilities;
import free.util.swing.SwingUtils;


/**
 * Manages the creation, initiation and closing of the connection to the server.
 */

public class ConnectionManager{



  /**
   * The current Session. Null when none.
   */

  private Session session = null;
  
  
  
  /**
   * The model whose state matches whether a session is currently established.
   */
  
  private final EventListenerList listenerList = new EventListenerList();



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
    if (connDetails == null){
      Jin.getInstance().quitIfNoUiVisible();
      return;
    }
    
    login(connDetails);
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
      if (server == null){
        I18n i18n = I18n.get(getClass());
        i18n.error("unknownServerParam", new Object[]{serverId});
      }
      else
        return server;
    }
    
    // Last logged in user
    User lastUser = loadLastUser();
    if (lastUser != null)
      return lastUser.getServer();
    
    // Sole supported server
    if (jin.getServers().size() == 1)
      return Iterables.getOnlyElement(jin.getServers());
    
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
   *   <li> Sole known user for the server.
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
    User lastUser = loadLastUser();
    if ((lastUser != null) && (lastUser.getServer() == server))
      return lastUser;
    
    // Only one known user on the server
    List serverUsers = jin.getUsers(server);
    if (serverUsers.size() == 1)
      return (User)serverUsers.get(0);
    
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
    if (user == null)
      return new LoginPanel(server).askConnectionDetails();
      
    Jin jin = Jin.getInstance();
    ConnectionDetails connDetails = user.getPreferredConnDetails();
    
    String password = jin.getParameter("login.password");
    if (password == null)
      password = connDetails.getPassword();
    
    String savePassString = jin.getParameter("login.savepassword");
    boolean savePassword = new Boolean(savePassString).booleanValue();
    if ((savePassString == null) && !connDetails.isGuest())
      savePassword = connDetails.isSavePassword();
    
    String hostname = jin.getParameter("login.hostname");
    if (hostname == null){
      hostname = connDetails.getHost();
    }
    
    String portsString = jin.getParameter("login.ports");
    int [] ports;
    if (portsString != null)
      ports = TextUtilities.parseIntList(portsString, ",");
    else
      ports = connDetails.getPorts();
    
    
    if (user.isGuest())
      connDetails = ConnectionDetails.createGuest(server, user.getUsername(), hostname, ports);
    else
      connDetails = ConnectionDetails.create(server, user, user.getUsername(), password, savePassword, hostname, ports);
      
    if (!(new Boolean(jin.getParameter("autologin")).booleanValue()))
      connDetails = new LoginPanel(connDetails).askConnectionDetails();
    
    return connDetails; 
  }
  
  
  
  /**
   * Displays UI for creating a new connection (from scratch).
   */
   
  public void displayNewConnUI(){
    Server server;
    
    Set<Server> servers = Jin.getInstance().getServers();
    // Sole supported server
    if (servers.size() == 1)
      server = Iterables.getOnlyElement(servers);
    else{  // Server choice panel
      server = new ServerChoicePanel().askServer();
      if (server == null){ // user canceled the dialog
        Jin.getInstance().quitIfNoUiVisible();
        return;
      }
    }
    
    displayNewConnUI(server);
  }
  
  
  
  /**
   * Displays UI for connecting to the specified server.
   */
  
  public void displayNewConnUI(Server server){
    ConnectionDetails connDetails = new LoginPanel(server).askConnectionDetails();
    
    if (connDetails == null){ // user canceled the dialog
      Jin.getInstance().quitIfNoUiVisible();
      return;
    }
    
    login(connDetails);
  }
  
  
  
  /**
   * Displays UI for connecting with the specified account.
   */
   
  public void displayNewConnUI(User user){
    ConnectionDetails connDetails = 
      new LoginPanel(user.getPreferredConnDetails()).askConnectionDetails();
      
    if (connDetails == null){ // user canceled the dialog
      Jin.getInstance().quitIfNoUiVisible();
      return;
    }
      
    login(connDetails);
  }
  
  
  
  /**
   * Initiates login with the specified connection details.
   */
   
  private void login(ConnectionDetails connDetails){
    try{
      fireSessionEvent(new SessionEvent(this, SessionEvent.SESSION_STARTING, null));
      session = new Session(connDetails);
      fireSessionEvent(new SessionEvent(this, SessionEvent.SESSION_ESTABLISHED, session));
      
      new ReconnectDialogPanel(session);
      
      session.initiateLogin();
    } catch (PluginStartException e){
        e.printStackTrace();
        Exception reason = e.getReason();

        String errorMessage = e.getMessage() + "\n" + 
          (reason == null ? "" : reason.getClass().getName() + ": " + reason.getMessage());
        OptionPanel.error("Error", errorMessage);
      }
  }
  
  
  
  /**
   * Invoked by Session if login fails.
   * 
   * @param message The message with which login failed.
   */
  
  void loginFailed(String message){
    I18n i18n = I18n.get(getClass());
    i18n.error("loginErrorDialog", new Object[]{message});
    
    // Reopen the connection UI
    User user = session.getUser();
    closeSession();
    
    displayNewConnUI(user);
  }
  
  
  
  /**
   * Closes the current session, if any.
   */

  public void closeSession(){
    closeSession(false);
  }
  
  
  
  /**
   * Closes the current session, if any, and optionally reconnects using the
   * connection details of the current session.
   */
  
  public void closeSession(boolean reconnect){
    if (session == null)
      return;
    
    User user = session.getUser();
    int connPort = session.getPort();

    if (connPort != -1){ // A connection really was established
      
      // Set preferred connection details for this account
      ConnectionDetails connDetails = session.getConnDetails().usePort(connPort);
      user.setPreferredConnDetails(connDetails);

      // Add the user to the known users list
      if (!user.isGuest() && !Jin.getInstance().isKnownUser(user)){
        Jin.getInstance().addUser(user);
        saveLastUser(user);
      }
      else{
        user.markDirty();
        saveLastUser(user);
      }
    }
    
    
    fireSessionEvent(new SessionEvent(this, SessionEvent.SESSION_CLOSING, session));

    // Close the session
    session.close();

    Session tempSession = session;
    session = null;
    
    fireSessionEvent(new SessionEvent(this, SessionEvent.SESSION_CLOSED, tempSession));
    
    if (reconnect)
      login(tempSession.getConnDetails());
  }



  /**
   * Returns the current <code>Session</code>, or <code>null</code> if none.
   */

  public Session getSession(){
    return session;
  }
  
  
  
  /**
   * Adds a session listener.
   */
  
  public void addSessionListener(SessionListener l){
    listenerList.add(SessionListener.class, l);
  }
  
  
  
  /**
   * Removes a session listener.
   */
  
  public void removeSessionListener(SessionListener l){
    listenerList.remove(SessionListener.class, l);
  }
  
  
  
  /**
   * Fires a session event.
   */
  
  private void fireSessionEvent(SessionEvent evt){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == SessionListener.class){
        SessionListener listener = (SessionListener)listeners[i+1];
        switch (evt.getId()){
          case SessionEvent.SESSION_STARTING:
            listener.sessionStarting(evt);
            break;
          case SessionEvent.SESSION_ESTABLISHED:
            listener.sessionEstablished(evt);
            break;
          case SessionEvent.SESSION_CLOSING:
            listener.sessionClosing(evt);
            break;
          case SessionEvent.SESSION_CLOSED:
            listener.sessionClosed(evt);
            break;
        }
      }
    }
    
  }



  /**
   * Returns the <code>User</code> object of the last account that logged on.
   * Returns <code>null</code> if none.
   */

  private User loadLastUser(){
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

    prefs.setString("last-login.serverId", serverId);
    prefs.setString("last-login.username",
        user.isGuest() || !Jin.getInstance().isKnownUser(user) ? null : user.getUsername());
  }
  
  
  
  /**
   * A dialog panel we display to the user when he gets disconnected.
   */
  
  private class ReconnectDialogPanel extends DialogPanel implements ConnectionListener{
    
    
    
    /**
     * The session we are used for.
     */
    
    private final Session session;
    
    
    
    /**
     * Whether the connection actually ever logged in. If not, we don't show
     * ourselves.
     */
    
    private boolean loggedIn = false;
    
    
    
    /**
     * Whether the session is closing. If a SESSION_CLOSING event is fired
     * before the CONNECTION_LOST event, it means the connection was lost due
     * to a user closing the session, so we shouldn't show ourselves.
     */
    
    private boolean sessionClosing = false;
    
    
    
    /**
     * Creates a new <code>ReconnectDialogPanel</code> for the specified
     * session. The dialog will listen to session events and display itself when
     * appropriate.
     */
    
    public ReconnectDialogPanel(Session session){
      super(I18n.get(ReconnectDialogPanel.class).getString("title"));
      
      this.session = session;
      
      addSessionListener(new SessionListener(){
        @Override
        public void sessionClosing(SessionEvent evt){
          removeSessionListener(this);
          if (isShowing())
            close(null);
          else
            sessionClosing = true;
        }
        @Override
        public void sessionClosed(SessionEvent evt){}
        @Override
        public void sessionEstablished(SessionEvent session){}
        @Override
        public void sessionStarting(SessionEvent evt){}
      });
      
      session.getConnection().getListenerManager().addConnectionListener(this);
    }
    
    
    
    /**
     * Creates the UI of this dialog.
     */
    
    private void createUi(){
      setLayout(new BorderLayout(10, 10));
      
      I18n i18n = I18n.get(ReconnectDialogPanel.class);
      
      JButton reconnectButton = i18n.createButton("reconnectButton");
      JButton quitButton = i18n.createButton("quitButton", new Object[]{Jin.getAppName()});
      
      reconnectButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent evt){
          closeSession(true);
        }
      });
      
      quitButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent evt){
          Jin.getInstance().quit(false);
        }
      });
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      buttonPanel.add(quitButton);
      buttonPanel.add(reconnectButton);
      
      setDefaultButton(reconnectButton);
      
      JComponent labelPanel = Box.createVerticalBox();
      JLabel [] labels = SwingUtils.labelsForLines(i18n.getFormattedString("message",
          new Object[]{session.getServer().getLongName(),
                       session.getConnDetails().getUser().getUsername(),
                       session.getConnDetails().getHost(),
                       String.valueOf(session.getPort())}));
      for (int i = 0; i < labels.length; i++)
        labelPanel.add(labels[i]);
      
      add(BorderLayout.WEST, new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
      add(BorderLayout.CENTER, labelPanel);
      add(BorderLayout.SOUTH, buttonPanel);
    }
    
    
    
    /**
     * Configures the dialog not to be modal.
     */
    
    @Override
    protected void configureDialog(JDialog dialog){
      super.configureDialog(dialog);
      
      dialog.setModal(false);
    }
    
    
    
    /**
     * Remember that the user got logged in, because otherwise we don't show
     * ourselves.
     */
    
    @Override
    public void loginSucceeded(Connection conn){
      loggedIn = true;
      
      // We create the UI here and not in the constructor because to create
      // the message we need the port on which we succeeded to log in, which
      // is not known in the constructor.
      createUi();
    }
    
    
    
    /**
     * If the connection was lost but the session is still open, show ourselves.
     */
    
    @Override
    public void connectionLost(Connection conn){
      if (loggedIn && !sessionClosing)
        Jin.getInstance().getUIProvider().showDialog(this, null);
    }
    
    
    
    @Override
    public void connectingFailed(Connection conn, String reason){}
    @Override
    public void connectionAttempted(Connection conn, String hostname, int port){}
    @Override
    public void connectionEstablished(Connection conn){}
    @Override
    public void loginFailed(Connection conn, String reason){}
    
    
    
  }



}