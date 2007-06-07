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

package free.jin.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.jin.ConnectionDetails;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.Server;
import free.jin.User;
import free.jin.UsernamePolicy;
import free.util.StringEncoder;
import free.util.Utilities;
import free.util.swing.LinkLabel;
import free.util.swing.MoreLessOptionsButton;
import free.util.swing.UrlDisplayingAction;
import free.workarounds.FixedJComboBox;
import free.workarounds.FixedJPasswordField;
import free.workarounds.FixedJTextField;


/**
 * The login panel.
 */

public class LoginPanel extends DialogPanel{
  
  
  
  /**
   * The <code>I18n</code> for this login panel.
   */
  
  private final I18n i18n = I18n.get(LoginPanel.class);
  
  
  
  /**
   * The list of servers, in the order we put them in the serverBox.
   */
   
  private Server [] servers;
  
  
  
  /**
   * The list of users, in the order we put them in the accountBox. 
   */
   
  private User [] users;
  
  
  
  /**
   * The JComboBox displaying the current server. 
   */
   
  private final JComboBox serverBox;
  
  
  
  /**
   * The server website link.
   */
  
  private final LinkLabel serverWebsiteLink;
  
  
  
  /**
   * The action invoked when the server website link is clicked.
   */
  
  private final UrlDisplayingAction serverWebsiteAction;
  
  
  
  /**
   * The username label
   */
  
  private final JLabel usernameLabel;
  
  
  
  /**
   * The username box.
   */
   
  private final JComboBox usernameBox;
  
  
  
  /**
   * The password label.
   */
  
  private final JLabel passwordLabel;
  
  
  
  /**
   * The password field.
   */
   
  private final JPasswordField passwordField;
  
  
  
  /**
   * The save password checkbox.
   */
   
  private final JCheckBox savePasswordCheckBox;



  /**
   * The retrieve password link label.
   */
  
  private final LinkLabel retrievePasswordLink;
  
  
  
  /**
   * The action invoked when the retrieve password link is clicked.
   */
  
  private final UrlDisplayingAction retrievePasswordAction;



  /**
   * The hostname box.
   */
   
  private final JComboBox hostnameBox;
  
  
  
  /**
   * The ports field.
   */
   
  private final JTextField portsField;
  
  
  
  /**
   * The "connect" button.
   */
   
  private final JButton connectButton;
  
  
  
  /**
   * The "register" link label.
   */
   
  private final LinkLabel registerLink;
  
  
  
  /**
   * The action invoked when the register link is clicked.
   */
  
  private final UrlDisplayingAction registerAction;
  
  
  
  /**
   * Creates a new <code>LoginPanel</code> with the specified initial connection
   * details.
   */

  public LoginPanel(ConnectionDetails connDetails){
    this(connDetails.getServer(), connDetails);
  }
  
  
  
  /**
   * Creates a new <code>LoginPanel</code> with the specified initial server.
   */
   
  public LoginPanel(Server server){
    this(server, null);
  }
  
  
  
  /**
   * Creates a new <code>LoginPanel</code> with the specified server and
   * connection details.
   */
   
  private LoginPanel(Server server, ConnectionDetails connDetails){
    this.serverBox = new FixedJComboBox();
    this.serverWebsiteLink = new LinkLabel(i18n.getString("serverWebsiteLink.text"));
    this.serverWebsiteAction = new UrlDisplayingAction(null);
    this.usernameLabel = i18n.createLabel("handleLabel");
    this.usernameBox = new FixedJComboBox();
    this.passwordLabel = i18n.createLabel("passwordLabel");
    this.passwordField = new FixedJPasswordField();
    this.savePasswordCheckBox = i18n.createCheckBox("savePasswordCheckBox");
    this.retrievePasswordLink = new LinkLabel(i18n.getString("retrievePasswordLink.text"));
    this.retrievePasswordAction = new UrlDisplayingAction(null);
    this.hostnameBox = new FixedJComboBox();
    this.portsField = new FixedJTextField(7);
    this.connectButton = i18n.createButton("connectButton");
    this.registerLink = new LinkLabel(i18n.getString("registerLink.text"));
    this.registerAction = new UrlDisplayingAction(null);
    
    this.servers = Jin.getInstance().getServers();
    
    serverBox.setEditable(false);
    usernameBox.setEditable(true);
    
    usernameLabel.setLabelFor(usernameBox);
    passwordLabel.setLabelFor(passwordField);
    
    serverWebsiteLink.addActionListener(serverWebsiteAction);
    retrievePasswordLink.addActionListener(retrievePasswordAction);
    registerLink.addActionListener(registerAction);
    
    hostnameBox.setEditable(true);
    
    connectButton.setDefaultCapable(true);
    setDefaultButton(connectButton);
    connectButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Server server = getServer();
        User user;
        if (usernameBox.getSelectedItem() instanceof User)
          user = (User)usernameBox.getSelectedItem();
        else{
          String username = (String)usernameBox.getSelectedItem();
          UsernamePolicy policy = server.getUsernamePolicy();
          String invalidityReason = policy.invalidityReason(username);
          if (invalidityReason != null){
            OptionPanel.error(i18n.getString("invalidUsernameDialog.title"), invalidityReason, LoginPanel.this);
            return;
          }
          
          user = Jin.getInstance().getUser(server, username);
          if (user == null){
            if (policy.isSame(username, policy.getGuestUsername()))
              user = server.getGuest();
            else
              user = new User(server, username);
          }
        }

        int [] ports = parsePorts(portsField.getText());
        if (ports == null){
          showIllegalPortsError();
          return;
        }

        String password = new String(passwordField.getPassword());
        if ("".equals(password)) // An empty string indicates there is no password
          password = null;       // but we want to let the user input it himself

        boolean savePassword = (savePasswordCheckBox != null) && savePasswordCheckBox.isSelected();
        
        
        ConnectionDetails result =  user.isGuest() ?
          ConnectionDetails.createGuest(server, user.getUsername(), (String)hostnameBox.getSelectedItem(),
            ports) :
          ConnectionDetails.create(server, user, user.getUsername(), password, savePassword,
            (String)hostnameBox.getSelectedItem(), ports);

        close(result);
      }
    });

    serverBox.setModel(createServerBoxModel());
    
    setData(server, connDetails, true, true);
    
    serverBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Server server = servers[serverBox.getSelectedIndex()];
        User [] users = getServerUsers(server);
        
        if ((users.length != 0) && !users[0].isGuest())   
          setData(server, users[0].getPreferredConnDetails(), false, true);
        else
          setData(server, null, false, true);
      }
    });
    
    usernameBox.addActionListener(new ActionListener(){
      private boolean ignoreCalls = false;
      public void actionPerformed(ActionEvent evt){
        if (ignoreCalls)
          return;
        
        Server server = getServer();
        int selectedIndex = usernameBox.getSelectedIndex();
        
        if (selectedIndex == -1)
          setData(server, null, false, false);
        else if (selectedIndex == 0){
          ignoreCalls = true;
          usernameBox.setSelectedItem("");
          ignoreCalls = false;
          setData(server, null, false, false);
        }
        else
          setData(server, users[selectedIndex - 1].getPreferredConnDetails(), false, false);
      }
    });
    
    createUI();
  }
  
  
  
  /**
   * Sets the data currently displayed by the dialog. If the connection details
   * are <code>null</code>, it means that the user is creating a new account
   * from scratch (only the server is known).
   */
   
  private void setData(Server server, ConnectionDetails connDetails, boolean updateServer, boolean updateUsername){
    if (updateServer)
      serverBox.setSelectedIndex(Utilities.indexOf(servers, server));
    
    if (updateUsername){
      users = getServerUsers(server);
      usernameBox.setModel(createAccountBoxModel());
      
      User user;
      if (connDetails == null)
        user = null;
      else if (connDetails.isGuest())
        user = server.getGuest();
      else
        user = Jin.getInstance().getUser(server, connDetails.getUsername());
      
      if (user == null)
        usernameBox.setSelectedItem("");
      else
        usernameBox.setSelectedIndex(Utilities.indexOf(users, user) + 1);
    }
    
    if (connDetails == null){
      passwordField.setText("");
      savePasswordCheckBox.setSelected(false);
    }
    else if (connDetails.isGuest()){
      passwordField.setText("");
      savePasswordCheckBox.setSelected(false);
    }
    else{
      passwordField.setText(connDetails.getPassword());
      savePasswordCheckBox.setSelected(connDetails.isSavePassword());
    }
    
    JComponent [] guestDependentComponents = new JComponent[]{
        passwordLabel, passwordField,
        savePasswordCheckBox, retrievePasswordLink
    };
    
    for (int i = 0; i < guestDependentComponents.length; i++)
      guestDependentComponents[i].setEnabled((connDetails == null) || !connDetails.isGuest());

    
    hostnameBox.setModel(new DefaultComboBoxModel(server.getHosts()));
    hostnameBox.setSelectedItem(connDetails == null ? server.getDefaultHost() : connDetails.getHost());
    
    int [] ports = connDetails == null ? server.getPorts() : connDetails.getPorts();
    portsField.setText(StringEncoder.encodeIntList(ports));
    
    String serverWebsiteUrl = server.getWebsite();
    serverWebsiteAction.setUrl(serverWebsiteUrl);
    serverWebsiteLink.setVisible(serverWebsiteUrl != null);
    
    String passwordRetrievalPageUrl = server.getPasswordRetrievalPage();
    retrievePasswordAction.setUrl(passwordRetrievalPageUrl);
    retrievePasswordLink.setVisible(passwordRetrievalPageUrl != null);
    
    String registrationPageUrl = server.getRegistrationPage();
    registerAction.setUrl(registrationPageUrl);
    registerLink.setVisible(registrationPageUrl != null);
    
    Component focusedComponent;
    if ("".equals(usernameBox.getSelectedItem()))
      focusedComponent = usernameBox;
    else if ((passwordField.getPassword().length == 0) && passwordField.isEnabled())
      focusedComponent = passwordField;
    else
      focusedComponent = connectButton;
    
    if (isShowing())
      focusedComponent.requestFocusInWindow();
    else
      defaultFocusedComponent = focusedComponent;
  }
  
  
  
  /**
   * Creates the ComboBoxModel for the servers combo box.
   */
   
  private ComboBoxModel createServerBoxModel(){
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    for (int i = 0; i < servers.length; i++)
      model.addElement(servers[i].getLongName());
    
    return model;
  }
  
  
  
  /**
   * Returns a list of known users on the specified server.
   */
   
  private User [] getServerUsers(Server server){
    Vector usersVec = new Vector();
    ListModel usersList = Jin.getInstance().getUsers();
    
    for (int i = 0; i < usersList.getSize(); i++){
      User user = (User)usersList.getElementAt(i);
      if (user.getServer() == server)
        usersVec.addElement(user);
    }
    usersVec.add(server.getGuest());
    
    User [] usersArr = new User[usersVec.size()];
    usersVec.copyInto(usersArr);
    return usersArr;
  }
  
  
  
  /**
   * Creates the ComboBoxModel for the users combo box.
   */
   
  private ComboBoxModel createAccountBoxModel(){
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    model.addElement(i18n.getString("accountComboBox.newAccount"));
    
    for (int i = 0; i < users.length; i++)
      model.addElement(users[i].getUsername());
    
    return model;
  }



  /**
   * Displays this panel and returns the connection details specified by the
   * user. Returns <code>null</code> if the user closes the panel or otherwise
   * cancels the operation.
   */

  public ConnectionDetails askConnectionDetails(){
    return (ConnectionDetails)super.askResult();
  }



  /**
   * Returns the title for this panel.
   */

  protected String getTitle(){
    return i18n.getString("title");
  }



  /**
   * Shows an error regarding telling the user that the ports list he selected is invalid.
   */
  
  private void showIllegalPortsError(){
    i18n.error("invalidPortsDialog", this);
  }
  
  
  
  /**
   * Returns the currently selected server. Helper function.
   */
   
  private Server getServer(){
    return servers[serverBox.getSelectedIndex()];     
  }



  /**
   * Creates and adds the user interface of this panel.
   */

  private void createUI(){
    JLabel serverLabel = i18n.createLabel("serverLabel");
    serverLabel.setLabelFor(serverBox);
    
    JLabel hostnameLabel = i18n.createLabel("hostnameLabel");
    hostnameLabel.setLabelFor(hostnameBox);
    
    JLabel portsLabel = i18n.createLabel("portsLabel");
    portsLabel.setLabelFor(portsField);
    
    MoreLessOptionsButton moreLessButton = new MoreLessOptionsButton(false, new Component[]{
       hostnameLabel, hostnameBox,
       portsLabel, portsField
    });
    
    moreLessButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        resizeContainerToFit();
      }
    });

    // Layout
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    
    
    layout.setHonorsVisibility(serverWebsiteLink, Boolean.FALSE);
    layout.setHonorsVisibility(registerLink, Boolean.FALSE);
    layout.setHonorsVisibility(retrievePasswordLink, Boolean.FALSE);
    
    layout.setHorizontalGroup(
        layout.createParallelGroup()
        .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(GroupLayout.TRAILING, false)
                .add(serverLabel)
                .add(usernameLabel)
                .add(passwordLabel)
                .add(hostnameLabel)
                .add(portsLabel)
            )
            .addPreferredGap(LayoutStyle.RELATED)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(serverBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                    .add(usernameBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                    .add(passwordField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                    .add(savePasswordCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                    .add(hostnameBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                    .add(portsField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
                )
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(serverWebsiteLink, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(registerLink, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(retrievePasswordLink, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
            )
        )
        .add(layout.createSequentialGroup()
            .addPreferredGap(LayoutStyle.RELATED, 1, Integer.MAX_VALUE)
            .add(moreLessButton)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(connectButton)
        )
    );
    
    layout.setVerticalGroup(
        layout.createSequentialGroup()
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(serverLabel).add(serverBox).add(serverWebsiteLink)
        )
        .addPreferredGap(LayoutStyle.RELATED)
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(usernameLabel).add(usernameBox).add(registerLink)
        )
        .addPreferredGap(LayoutStyle.RELATED)
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(passwordLabel).add(passwordField).add(retrievePasswordLink)
        )
        .addPreferredGap(LayoutStyle.RELATED)
        .add(savePasswordCheckBox)
        .addPreferredGap(LayoutStyle.UNRELATED)
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(hostnameLabel).add(hostnameBox)
        )
        .addPreferredGap(LayoutStyle.RELATED)
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(portsLabel).add(portsField)
        )
        .addPreferredGap(LayoutStyle.UNRELATED, 20, Integer.MAX_VALUE)
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(moreLessButton)
            .add(connectButton)
        )
    );
  }
  
  
  
  /**
   * Parses the specified string as a list of space delimited ports. Returns
   * <code>null</code> if the string is invalid.
   */

  private static int [] parsePorts(String s){
    StringTokenizer tokenizer = new StringTokenizer(s, " ");
    int [] ports = new int[tokenizer.countTokens()];
    if (ports.length == 0)
      return null;

    try{
      for (int i = 0; i < ports.length; i++){
        int port = Integer.parseInt(tokenizer.nextToken());
        if ((port < 0) || (port > Short.MAX_VALUE))
          return null;
        ports[i] = port;
      }
    } catch (NumberFormatException e){
        return null;
      }

    return ports;
  }



  /**
   * The component focused by default.
   */

  private Component defaultFocusedComponent = null;



  // Hack to set the focus where we want it.
  public void paint(Graphics g){ 
                                 
    if (defaultFocusedComponent != null){
      defaultFocusedComponent.requestFocusInWindow();
      defaultFocusedComponent = null;
    }
    super.paint(g);
  }



}