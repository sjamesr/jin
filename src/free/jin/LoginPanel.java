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

import javax.swing.*;
import java.awt.*;
import free.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;
import javax.swing.border.TitledBorder;
import free.workarounds.FixedJTextField;
import free.workarounds.FixedJPasswordField;
import free.workarounds.FixedJComboBox;


/**
 * The login panel.
 */

public class LoginPanel extends DialogPanel{



  /**
   * The context in which we're running.
   */

  private final JinContext context;



  /**
   * The server we're to connect to.
   */

  private final Server server;



  /**
   * The initially filled in connection details. May be null.
   */

  private final ConnectionDetails connDetails;



  /**
   * The component focused by default.
   */

  private JComponent defaultComponent = null;



  /**
   * Creates a new <code>LoginPanel</code> with the specified initial
   * connection details.
   */

  public LoginPanel(JinContext context, Server server, ConnectionDetails connDetails){
    this.context = context;
    this.server = server;
    this.connDetails = connDetails;

    createUI();
  }



  /**
   * Displays this panel using the specified <code>UIProvider</code> and returns
   * the connection details specified by the user. Returns <code>null</code> if
   * the user closes the panel or otherwise cancels the operation.
   */

  public ConnectionDetails askConnectionDetails(UIProvider uiProvider){
    return (ConnectionDetails)super.askResult(uiProvider);
  }



  /**
   * Returns the title for this panel.
   */

  protected String getTitle(){
    return server.getLongName() + " Login";
  }



  /**
   * Displays an error panel with the specified title and message.
   */

  private void showError(String title, String message){
    OptionPanel panel = new OptionPanel(OptionPanel.ERROR, title,
      new Object[]{OptionPanel.OK}, OptionPanel.OK, message);
    panel.setHintParent(this);
    panel.show(context.getUIProvider());
  }



  /**
   * Creates and adds the user interface of this panel.
   */

  private void createUI(){
    boolean noConnDetailsOrGuest = (connDetails == null) || connDetails.isGuest();
    String username = noConnDetailsOrGuest ? "" : connDetails.getUsername();
    String password = noConnDetailsOrGuest ? "" : connDetails.getPassword();
    boolean savePassword = noConnDetailsOrGuest ? false : connDetails.isSavePassword();
    String [] hostnames = server.getHosts();
    String hostname = connDetails == null ? server.getDefaultHost() : connDetails.getHost();
    int [] ports = connDetails == null ? server.getPorts() : connDetails.getPorts();

    final JTextField usernameField = new FixedJTextField(username);
    final JPasswordField passwordField = new FixedJPasswordField(password);
    final JComboBox hostnameBox = new FixedJComboBox(hostnames);
    hostnameBox.setSelectedItem(hostname);
    hostnameBox.setEditable(true);
    final JTextField portsField = new FixedJTextField(StringEncoder.encodeIntList(ports), 7);
    final JCheckBox savePasswordCheckBox = new JCheckBox("Save password", savePassword);
    savePasswordCheckBox.setMnemonic('S');

    
    JButton connectButton = new JButton("Connect");
    connectButton.setDefaultCapable(true);
    setDefaultButton(connectButton);
    connectButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String username = usernameField.getText();
        UsernamePolicy policy = server.getUsernamePolicy();
        String invalidityReason = policy.invalidityReason(username);
        if (invalidityReason != null){
          showError("Bad Username", invalidityReason);
          return;
        }

        int [] ports = parsePorts(portsField.getText());
        if (ports == null){
          showError("Bad Ports", "The ports text field must contain a\n" +
                                 "nonempty space separated list of ports.\n" +
                                 "A valid port is between 0 and 65535.");
          return;
        }

        String password = new String(passwordField.getPassword());
        if ("".equals(password)) // An empty string indicates there is no password
          password = null;       // but we want to let the user input it himself

        ConnectionDetails result = policy.isSame(username, policy.getGuestUsername()) ?
          ConnectionDetails.createGuest(username, (String)hostnameBox.getSelectedItem(), ports) :
          ConnectionDetails.create(username, password, savePasswordCheckBox.isSelected(),
            (String)hostnameBox.getSelectedItem(), ports);

        close(result);
      }
    });

    JButton retrievePasswordButton = null;
    if (server.getPasswordRetrievalPage() != null){
      retrievePasswordButton = new JButton("Retrieve Password");
      retrievePasswordButton.setMnemonic('t');
      retrievePasswordButton.setDefaultCapable(false);
      retrievePasswordButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          String url = server.getPasswordRetrievalPage();
          if (!BrowserControl.displayURL(url))
            BrowserControl.showDisplayBrowserFailedDialog(url, LoginPanel.this, true);
        }
      });
    }

    JButton loginAsGuestButton = new JButton("Login as Guest");
    loginAsGuestButton.setMnemonic('G');
    loginAsGuestButton.setDefaultCapable(false);
    loginAsGuestButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        int [] ports = parsePorts(portsField.getText());
        if (ports == null){
          showError("Bad Ports", "The ports text field must contain a\n" +
                                 "nonempty space separated list of ports.\n" +
                                 "A valid port is between 0 and 65535.");
          return;
        }

        String username = server.getUsernamePolicy().getGuestUsername();

        close(
          ConnectionDetails.createGuest(username, (String)hostnameBox.getSelectedItem(), ports));
      }
    });

    JButton registerButton = new JButton("Register");
    registerButton.setMnemonic('R');
    registerButton.setDefaultCapable(false);
    registerButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String url = server.getRegistrationPage();
        if (!BrowserControl.displayURL(url))
          BrowserControl.showDisplayBrowserFailedDialog(url, LoginPanel.this, true);
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setDefaultCapable(false);
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        close(null);
      }
    });


    // Set default focused component
    if (username.length() == 0)
      defaultFocusedComponent = usernameField;
    else if (password.length() == 0)
      defaultFocusedComponent = passwordField;
    else
      defaultFocusedComponent = connectButton;



    // Create the main panel parts

    Component guestPanel = createGuestPanel(loginAsGuestButton, registerButton);
    Component advancedPanel = createAdvancedPanel(hostnameBox, portsField);
    Component membersPanel = createMembersPanel(usernameField, passwordField,
      savePasswordCheckBox, retrievePasswordButton, connectButton);
    Component cancelPanel = createCancelPanel(cancelButton);

    // Add the main panels and layout
    setLayout(new BorderLayout());

    Box upperPanel = new Box(BoxLayout.X_AXIS);
    Box lowerPanel = new Box(BoxLayout.X_AXIS);

    upperPanel.add(guestPanel);
    upperPanel.add(Box.createHorizontalStrut(10));
    upperPanel.add(advancedPanel);

    lowerPanel.add(membersPanel);
    lowerPanel.add(Box.createHorizontalStrut(10));
    lowerPanel.add(cancelPanel);

    Box vpanel = new Box(BoxLayout.Y_AXIS);
    vpanel.add(upperPanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(lowerPanel);

    add(vpanel, BorderLayout.CENTER);
  }



  /**
   * Creates the guest panel.
   */

  private Component createGuestPanel(JButton loginAsGuestButton, JButton registerButton){
    Box vpanel = new Box(BoxLayout.Y_AXIS);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(loginAsGuestButton);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(registerButton);
    vpanel.add(Box.createVerticalStrut(10));

    Box hpanel = new Box(BoxLayout.X_AXIS);
    hpanel.add(Box.createHorizontalStrut(10));
    hpanel.add(vpanel);
    hpanel.add(Box.createHorizontalStrut(10));

    JPanel outerPanel = new JPanel(new BorderLayout());
    outerPanel.add(hpanel,BorderLayout.CENTER);
    outerPanel.setBorder(new TitledBorder(" Guests "));

    return outerPanel;
  }



  /**
   * Creates the advanced options panel.
   */

  private Component createAdvancedPanel(JComboBox hostnameBox, JTextField portsField){
    JPanel hostnameLabelPanel = new JPanel(new GridLayout(3,1));
    hostnameLabelPanel.add(new JLabel("Server"));
    hostnameLabelPanel.add(new JLabel("Hostname or"));
    JLabel hostnameLabel = new JLabel("IP Number");
    hostnameLabel.setDisplayedMnemonic('I');
    hostnameLabelPanel.add(hostnameLabel);

    Box hostnamePanel = new Box(BoxLayout.X_AXIS);

    hostnameBox.setFont(UIManager.getFont("TextField.font"));
    hostnameBox.setEditable(true);
    hostnameLabel.setLabelFor(hostnameBox);

    hostnamePanel.add(hostnameBox);
    hostnamePanel.add(Box.createHorizontalStrut(10));
    hostnamePanel.add(hostnameLabelPanel);

    
    Box portPanel = new Box(BoxLayout.X_AXIS);

    portsField.setMaximumSize(portsField.getPreferredSize());

    portPanel.add(portsField);
    portPanel.add(Box.createHorizontalStrut(10));
    JLabel portLabel = new JLabel("Ports");
    portLabel.setDisplayedMnemonic('o');
    portLabel.setLabelFor(portsField);
    portPanel.add(portLabel);
    portPanel.add(Box.createHorizontalGlue());
   
    
    Box vpanel = new Box(BoxLayout.Y_AXIS);
    vpanel.add(hostnamePanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(portPanel);
    vpanel.add(Box.createVerticalStrut(10));

    Box hpanel = new Box(BoxLayout.X_AXIS);
    hpanel.add(Box.createHorizontalStrut(10));
    hpanel.add(vpanel);
    hpanel.add(Box.createHorizontalStrut(10));

    JPanel outerPanel = new JPanel(new BorderLayout());
    outerPanel.setBorder(new TitledBorder(" Advanced Options "));
    outerPanel.add(hpanel, BorderLayout.CENTER);

    return outerPanel;
  }



  /**
   * Creates the members panel.
   */

  private Component createMembersPanel(JTextField usernameField, JPasswordField passwordField,
    JCheckBox savePasswordCheckBox, JButton retrievePasswordButton, JButton connectButton){

    Box usernamePanel = new Box(BoxLayout.X_AXIS);

    usernameField.setPreferredSize(new Dimension(130, 20));
    usernamePanel.add(usernameField);
    usernamePanel.add(Box.createHorizontalStrut(10));
    JLabel handleLabel = new JLabel("Handle (your login name)");
    handleLabel.setDisplayedMnemonic('H');
    handleLabel.setLabelFor(usernameField);
    usernamePanel.add(handleLabel);
    usernamePanel.add(Box.createHorizontalGlue());
    usernamePanel.add(Box.createHorizontalStrut(10));


    Box passwordInputPanel = new Box(BoxLayout.X_AXIS);

    passwordField.setPreferredSize(new Dimension(130, 20));
    passwordInputPanel.add(passwordField);
    passwordInputPanel.add(Box.createHorizontalStrut(10));
    JLabel passwordLabel = new JLabel("Password");
    passwordLabel.setDisplayedMnemonic('P');
    passwordLabel.setLabelFor(passwordField);
    passwordInputPanel.add(passwordLabel);
    passwordInputPanel.add(Box.createHorizontalGlue());
    passwordInputPanel.add(Box.createHorizontalStrut(10));

    int fieldsWidth = Math.max(usernameField.getPreferredSize().width, passwordField.getPreferredSize().width);
    usernameField.setMaximumSize(new Dimension(fieldsWidth, usernameField.getPreferredSize().height));
    passwordField.setMaximumSize(new Dimension(fieldsWidth, passwordField.getPreferredSize().height));
    usernameField.setPreferredSize(new Dimension(fieldsWidth, usernameField.getPreferredSize().height));
    passwordField.setPreferredSize(new Dimension(fieldsWidth, passwordField.getPreferredSize().height));

    usernameField.setColumns(0); // Otherwise setPreferredSize is ignored and it will still
    passwordField.setColumns(0); // use the amount of columns to calculate preferred size.

    Box passwordOptionsPanel = new Box(BoxLayout.X_AXIS);

    passwordOptionsPanel.add(savePasswordCheckBox);
    if (retrievePasswordButton != null){
      passwordOptionsPanel.add(Box.createHorizontalStrut(10));
      passwordOptionsPanel.add(retrievePasswordButton);
    }
    passwordOptionsPanel.add(Box.createHorizontalGlue());


    Box decisionPanel = new Box(BoxLayout.X_AXIS);
    
    decisionPanel.add(connectButton);
    decisionPanel.add(Box.createHorizontalGlue());

    Box vpanel = new Box(BoxLayout.Y_AXIS);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(usernamePanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(passwordInputPanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(passwordOptionsPanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(decisionPanel);
    vpanel.add(Box.createVerticalStrut(10));

    Box hpanel = new Box(BoxLayout.X_AXIS);
    hpanel.add(Box.createHorizontalStrut(10));
    hpanel.add(vpanel);
    hpanel.add(Box.createHorizontalGlue());


    JPanel outerPanel = new JPanel(new BorderLayout());
    outerPanel.setBorder(new TitledBorder(" Members "));
    outerPanel.add(hpanel,BorderLayout.CENTER);

    return outerPanel;
  }



  /**
   * Creates the panel for the cancel button.
   */
  private Component createCancelPanel(JButton cancelButton){
    Box panel = new Box(BoxLayout.Y_AXIS);
    panel.add(Box.createVerticalGlue());
    panel.add(cancelButton);

    return panel;
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

  private JComponent defaultFocusedComponent = null;



  // Hack to set the focus where we want it.
  public void paint(Graphics g){ 
                                 
    if (defaultFocusedComponent != null){
      defaultFocusedComponent.requestFocus();
      defaultFocusedComponent = null;
    }
    super.paint(g);
  }



}