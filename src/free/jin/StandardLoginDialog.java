/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

import free.workarounds.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Enumeration;
import free.util.swing.IntegerStrictPlainDocument;
import free.util.BrowserControl;
import free.util.Utilities;



/**
 * A partial, basic implementation of a LoginDialog.
 */

public abstract class StandardLoginDialog implements LoginDialog{



  /**
   * The title of the dialog.
   */

  private final String title;




  /**
   * The <code>Properties</code> we use as default values for the various fields
   * in this dialog.
   */

  protected final Properties props;



  /**
   * The <code>Server</code> we're trying to login to.
   */

  protected final Server server;



  /**
   * The JTextField containing the user's username.
   */

  protected JTextField usernameField;




  /**
   * The JPasswordField containing the user's password.
   */

  protected JPasswordField passwordField;



  /**
   * The JComboBox containing the hostname of the server to connect to.
   */

  protected JComboBox hostnameBox;



  /**
   * The JTextField containing the port to connect on.
   */

  protected JTextField portField;



  /**
   * The JCheckBox saying whether to save the user's password.
   */

  protected JCheckBox savePasswordCheckBox;



  /**
   * The default button.
   */

  protected JButton defaultButton = null;



  /**
   * True if the user canceled the connection, false otherwise.
   */

  private boolean isCanceled = true;



  /**
   * The dialog we open.
   */

  protected JDialog dialog;




  /**
   * An action listener which cancels the dialog. It is attached to a "Cancel"
   * button and registered as the ActionListener for an ESCAPE keystroke.
   */

  private final ActionListener closeActionListener = new ActionListener(){

    public void actionPerformed(ActionEvent evt){
      cancel();
    }
    
  };



  /**
   * The "hint" user that was given to us.
   */

  protected final User hintUser;



  /**
   * The <code>User</code> we're going to return.
   */

  protected User resultUser = null;




  /**
   * Creates a new <code>StandardLoginDialog</code> with the specified title and
   * <code>User</code> whose properties will be used as default values for the
   * dialog fields.
   */

  public StandardLoginDialog(String title, User hintUser){
    this.title = title;
    this.props = getUserProperties(hintUser);
    this.hintUser = hintUser;
    this.server = hintUser.getServer();
  }




  /**
   * Creates a new <code>StandardLoginDialog</code> with the specified title and
   * <code>Server</code> whose properties will be used as default values for the
   * dialog fields.
   */

  public StandardLoginDialog(String title, Server server){
    this.title = title;
    this.props = getServerProperties(server);
    this.hintUser = null;
    this.server = server;
  }




  /**
   * Grabs the properties needed to be used as default values for this login
   * dialog from the specified <code>User</code>, puts them into a
   * <code>Properties</code> object and returns it.
   */

  protected Properties getUserProperties(User user){
    Properties props = new Properties();
    Utilities.put(props, "login.hostname", user.getProperty("login.hostname"));
    Utilities.put(props, "login.port", user.getProperty("login.port"));
    Utilities.put(props, "login.username", user.getProperty("login.username"));
    Utilities.put(props, "login.password", user.getProperty("login.password"));
    Utilities.put(props, "login.hosts", user.getProperty("login.hosts"));
    Utilities.put(props, "login.savepassword", user.getProperty("login.savepassword"));

    return props;
  }




  /**
   * Grabs the properties needed to be used as default values for this login
   * dialog from the specified <code>Server</code>, puts them into a
   * <code>Properties</code> object and returns it.
   */

  protected Properties getServerProperties(Server server){
    Properties props = new Properties();
    Utilities.put(props, "login.hostname", server.getProperty("login.hostname"));
    Utilities.put(props, "login.port", server.getProperty("login.port"));
    Utilities.put(props, "login.username", server.getProperty("login.username"));
    Utilities.put(props, "login.password", server.getProperty("login.password"));
    Utilities.put(props, "login.hosts", server.getProperty("login.hosts"));
    Utilities.put(props, "login.savepassword", server.getProperty("login.savepassword"));

    return props;
  }




  /**
   * Cancels the dialog, disposing of it with the <code>isCanceled</code> method
   * returning <code>true</code>.
   */

  protected void cancel(){
    isCanceled = true;
    dialog.dispose();
  }




  /**
   * Proceeds with the login process. Disposes of the dialog with the
   * <code>isCanceled</code> method returning <code>false</code>.
   */
  
  protected void proceed(){
    isCanceled = false;

    dialog.dispose();
  }




  /**
   * This method determines whether the currently specified user details are
   * sufficiently different from the details of the hint user so as to warrant
   * the creation of a new User object. The default implementation checks
   * whether the usernames are different (case insensitively).
   */

  protected boolean shouldCreateNewUser(){
    return (hintUser == null) || !usernameField.getText().equalsIgnoreCase(hintUser.getUsername()); 
  }



 

  /**
   * Shows the user the login dialog and waits for the user to either ok or
   * cancel.
   */

  public void show(Component parentComponent){
    Frame parentFrame = parentComponent instanceof Frame ?
      (Frame)parentComponent :
      (Frame)SwingUtilities.getAncestorOfClass(Frame.class, parentComponent);

    dialog = new JDialog(parentFrame, title ,true){

      private boolean painted = false;
    
      public void paint(Graphics g){ // Hack to set the focus and the default button to be where we want them,
                                     // because nothing else seems to work.
        if (!painted){
          painted = true;
          if (usernameField.getText().length() == 0)
            usernameField.requestFocus();
          else if (passwordField.getPassword().length == 0)
            passwordField.requestFocus();
          else
            defaultButton.requestFocus();
        }
        super.paint(g);
      }
    
    };

    dialog.setResizable(true); // Setting it to false causes problems on KDE.
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    JComponent content = new JPanel(new BorderLayout());
    dialog.setContentPane(content);

    KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    content.registerKeyboardAction(closeActionListener, closeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    Component guestPanel = createGuestsPanel(dialog);
    Component advancedOptionsPanel = createAdvancedOptionsPanel(dialog);
    Component membersPanel = createMembersPanel(dialog);
    Component cancelButtonPanel = createCancelButtonPanel(dialog);

    Box upperPanel = new Box(BoxLayout.X_AXIS);
    Box lowerPanel = new Box(BoxLayout.X_AXIS);

    upperPanel.add(guestPanel);
    upperPanel.add(Box.createHorizontalStrut(10));
    upperPanel.add(advancedOptionsPanel);

    lowerPanel.add(membersPanel);
    lowerPanel.add(Box.createHorizontalStrut(10));
    lowerPanel.add(cancelButtonPanel);

    Box vpanel = new Box(BoxLayout.Y_AXIS);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(upperPanel);
    vpanel.add(Box.createVerticalStrut(10));
    vpanel.add(lowerPanel);
    vpanel.add(Box.createVerticalStrut(10));

    Box hpanel = new Box(BoxLayout.X_AXIS);
    hpanel.add(Box.createHorizontalStrut(10));
    hpanel.add(vpanel);
    hpanel.add(Box.createHorizontalStrut(10));

    content.add(hpanel,BorderLayout.CENTER);

    dialog.getRootPane().setDefaultButton(defaultButton);

    dialog.pack();
    dialog.setLocationRelativeTo(parentComponent);
    dialog.setVisible(true);
  }




  /**
   * Creates and returns the "connect as guest" button. Should return null if
   * there is no such option on the server. The default implementation returns
   * a simple JButton with the label "Connect as guest".
   */

  protected JButton createConnectAsGuestButton(){
    JButton button = new JButton("Connect as Guest");
    button.setMnemonic('G');
    return button;
  }




  /**
   * This method is called when the "connect as guest" button is pressed or an
   * equivalent action is taken by the user. The default implementation sets the
   * username field to "guest", clears the password field, creates a guest
   * <code>User</code> and calls <code>proceed()</code>.
   */

  protected void connectAsGuestActionPerformed(){
    String inputIllegalityReason = findInputIllegalityReason(false);
    if (inputIllegalityReason != null){
      JOptionPane.showMessageDialog(dialog, inputIllegalityReason, "Wrong Connection Settings", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if ((hintUser == null) || !server.isGuest(hintUser)){
      System.out.println("Creating guest");
      resultUser = server.createGuest();
    }
    else{
      System.out.println("Reusing guest");
      resultUser = hintUser;
    }

    resultUser.setProperty("login.hostname", (String)hostnameBox.getSelectedItem());
    resultUser.setProperty("login.port", portField.getText());

    usernameField.setText(resultUser.getUsername());
    passwordField.setText(resultUser.getProperty("login.password"));

    proceed();
  }




  /**
   * Creates and returns a new "register" button. This button should take the
   * user to the registration page. This method may not return null. The
   * current implementation returns a simple JButton with the label "Register".
   */

  protected JButton createRegisterButton(){
    JButton button = new JButton("Register");
    button.setMnemonic('R');
    return button;
  }

 

  /**
   * This method is called when the "register" button is pressed or an
   * equivalent action is taken by the user.
   */

  protected abstract void registerActionPerformed();



  

  /**
   * Creates the "Guests" panel, the panel with options for guests.
   *
   * @param parentDialog The dialog to which the panel will be added.
   */

  public Component createGuestsPanel(final JDialog parentDialog){
    Box vpanel = new Box(BoxLayout.Y_AXIS);

    vpanel.add(Box.createVerticalStrut(10));

    JButton connectAsGuestButton = createConnectAsGuestButton();
    if (connectAsGuestButton != null){
      connectAsGuestButton.setDefaultCapable(false);
      connectAsGuestButton.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent evt){
          connectAsGuestActionPerformed();
        }

      });
      vpanel.add(connectAsGuestButton);

      vpanel.add(Box.createVerticalStrut(10));
    }

    JButton registerButton = createRegisterButton();
    registerButton.setDefaultCapable(false);
    registerButton.addActionListener(new ActionListener(){
      
      public void actionPerformed(ActionEvent evt){
        registerActionPerformed();
      }

    });
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
   * Creates the "Advanced Options" panel.
   *
   * @param parentDialog The dialog to which the panel will be added.
   */

  public Component createAdvancedOptionsPanel(final JDialog parentDialog){
    JPanel hostnameLabelPanel = new JPanel(new GridLayout(3,1));
    hostnameLabelPanel.add(new JLabel("Server"));
    hostnameLabelPanel.add(new JLabel("Hostname or"));
    JLabel hostnameLabel = new JLabel("IP Number");
    hostnameLabel.setDisplayedMnemonic('I');
    hostnameLabelPanel.add(hostnameLabel);

    Box hostnamePanel = new Box(BoxLayout.X_AXIS);

    StringTokenizer hosts = new StringTokenizer(props.getProperty("login.hosts", ""), ";");
    Object [] items = new String[hosts.countTokens()];
    int i = 0;
    while (hosts.hasMoreTokens())
      items[i++] = hosts.nextToken();

    hostnameBox = new FixedJComboBox(items);
    hostnameBox.setFont(UIManager.getFont("TextField.font"));
    hostnameBox.setEditable(true);
    hostnameBox.setSelectedItem(props.getProperty("login.hostname"));
    hostnameLabel.setLabelFor(hostnameBox);

    hostnamePanel.add(hostnameBox);
    hostnamePanel.add(Box.createHorizontalStrut(10));
    hostnamePanel.add(hostnameLabelPanel);

    
    Box portPanel = new Box(BoxLayout.X_AXIS);

    Document portDocument = new IntegerStrictPlainDocument(0,65535,10);
    portField = new FixedJTextField(portDocument,String.valueOf(props.getProperty("login.port")),5);
    portField.setMaximumSize(portField.getPreferredSize());

    portPanel.add(portField);
    portPanel.add(Box.createHorizontalStrut(10));
    JLabel portLabel = new JLabel("Port Number");
    portLabel.setDisplayedMnemonic('n');
    portLabel.setLabelFor(portField);
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
   * Creates and returns a button labeled "Forgot your password?". This method
   * should return null if there is no password retrieving functionality on the
   * server. The default implementation returns a simple JButton with the label
   * "Forgot your password?".
   */

  protected JButton createForgotPasswordButton(){
    JButton button = new JButton("Retrieve Password");
    button.setMnemonic('F');
    return button;
  }




  /**
   * This method is called when the button allowing the user to retrieve his
   * password is pressed or an equivalent action is taken by the user.
   */

  protected abstract void forgotPasswordActionPerformed();





  /**
   * Creates and returns a "Connect" button. The default implementation returns
   * a simple JButton with the label "Connect". This method may not return null.
   */

  protected JButton createConnectButton(){
    JButton button = new JButton("Connect");
    return button;
  }




  /**
   * This method is called when the connect button is pressed or an equivalent
   * action is taken by the user.
   */

  protected void connectActionPerformed(){
    String inputIllegalityReason = findInputIllegalityReason(true);
    if (inputIllegalityReason != null){
      JOptionPane.showMessageDialog(dialog, inputIllegalityReason,
        "Wrong Connection Settings", JOptionPane.ERROR_MESSAGE);
      return;
    }

    props.put("login.username", usernameField.getText());
    boolean savePassword = savePasswordCheckBox.isSelected();
    if (savePassword)
      props.put("login.password", new String(passwordField.getPassword()));
    else
      props.put("login.password", "");
    props.put("login.hostname", (String)hostnameBox.getSelectedItem());
    props.put("login.port", portField.getText());
    props.put("login.savepassword", String.valueOf(savePassword));

    if (shouldCreateNewUser())
      resultUser = server.createUser(props);
    else{
      resultUser = hintUser;

      Enumeration propsEnum = props.keys();
      while (propsEnum.hasMoreElements()){
        String key = (String)propsEnum.nextElement();
        String value = props.getProperty(key);
        resultUser.setProperty(key, value);
      }
    }

    proceed();
  }





  /**
   * Creates the "Members" panel, the panel with options for members.
   *
   * @param parentDialog The dialog to which the panel will be added.
   */

  public Component createMembersPanel(final JDialog parentDialog){
    Box usernamePanel = new Box(BoxLayout.X_AXIS);

    usernameField = new FixedJTextField(15);
    usernameField.setText(props.getProperty("login.username"));

    usernamePanel.add(usernameField);
    usernamePanel.add(Box.createHorizontalStrut(10));
    JLabel handleLabel = new JLabel("Handle (your login name)");
    handleLabel.setDisplayedMnemonic('H');
    handleLabel.setLabelFor(usernameField);
    usernamePanel.add(handleLabel);
    usernamePanel.add(Box.createHorizontalGlue());
    usernamePanel.add(Box.createHorizontalStrut(10));

    
    Box passwordInputPanel = new Box(BoxLayout.X_AXIS);

    passwordField = new FixedJPasswordField(15);
    passwordField.setText(props.getProperty("login.password"));

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

    boolean savePassword = Boolean.valueOf(props.getProperty("login.savepassword", "false")).booleanValue();
    savePasswordCheckBox = new JCheckBox("Save password", savePassword);
    savePasswordCheckBox.setMnemonic('S');

    JButton forgotPasswordButton = createForgotPasswordButton();
    if (forgotPasswordButton != null){
      forgotPasswordButton.setDefaultCapable(false);
      forgotPasswordButton.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent evt){
          forgotPasswordActionPerformed();
        }

      });
    }

    passwordOptionsPanel.add(savePasswordCheckBox);
    if (forgotPasswordButton != null){
      passwordOptionsPanel.add(Box.createHorizontalStrut(10));
      passwordOptionsPanel.add(forgotPasswordButton);
    }
    passwordOptionsPanel.add(Box.createHorizontalGlue());


    Box decisionPanel = new Box(BoxLayout.X_AXIS);
    
    final JButton connectButton = createConnectButton();
    connectButton.setDefaultCapable(true);
    connectButton.addActionListener(new ActionListener(){
      
      public void actionPerformed(ActionEvent evt){
        connectActionPerformed();
      }

    });

    defaultButton = connectButton;

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
   * Creates the panel with the "Cancel" button - the button that cancels the
   * connection.
   *
   * @param parentDialog The dialog to which the panel will be added.
   */

  public Component createCancelButtonPanel(final JDialog parentDialog){
    Box panel = new Box(BoxLayout.Y_AXIS);

    panel.add(Box.createVerticalGlue());

    JButton button = new JButton("Cancel");
    button.setDefaultCapable(false);
    button.addActionListener(closeActionListener);

    panel.add(button);

    return panel;
  }



  /**
   * Determines whether the currently specified user details are valid. If so,
   * returns <code>null</code>. Otherwise returns a string specifying what's
   * invalid about them. The <code>checkUsernameAndPassword</code> argument
   * specifies whether the username and password fields should be checked.
   * The default implementation checks that the port value is a valid port.
   */

  public String findInputIllegalityReason(boolean checkUsernameAndPassword){
    String portString = portField.getText();
    try{
      Integer.parseInt(portString);
    } catch (NumberFormatException e){
        return "Bad port value: "+portString;
      }

    return null;
  }





  /**
   * Returns true if the user canceled the connection, false otherwise.
   */

  public boolean isCanceled(){
    return isCanceled;
  }




  /**
   * Returns the User object filled with the information supplied by the user.
   */

  public User getUser(){
    return resultUser;
  }


}