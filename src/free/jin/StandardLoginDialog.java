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

import free.workarounds.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.util.Properties;
import java.util.StringTokenizer;
import free.util.swing.IntegerStrictPlainDocument;
import free.util.BrowserControl;



/**
 * A partial, basic implementation of a LoginDialog.
 */

public abstract class StandardLoginDialog implements LoginDialog{



  /**
   * The title of the dialog.
   */

  private final String title;




  /**
   * The User that wants to log on using this LoginDialog.
   */

  protected User user;



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
   * Creates a new StandardLoginDialog with the specified title.
   */

  public StandardLoginDialog(String title){
    this.title = title;
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
  
  public void proceed(){
    isCanceled = false;
    dialog.dispose();
  }




  /**
   * Uses the following properties from the given user:
   * <UL>
   *   <LI> "login.hostname" - The hostname or the IP of the server.
   *   <LI> "login.port" - The port to connect on to the server.
   *   <LI> "login.username" - The username of the account to log on.
   *   <LI> "login.password" - The password of the account.
   *   <LI> "login.hosts" - A list of ICC servers' hostnames (or IPs), separated by ";". 
   *   <LI> "login.savepassword" - Whether to save the password entered by the user,
   *        either "true" or "false".
   * </UL>
   */

  public void setHintUser(User user){
    this.user = user;
  }




  /**
   * Shows the user the login dialog and waits for the user to either ok or
   * cancel.
   */

  public void show(Component parentComponent){
    Frame parentFrame = parentComponent instanceof Frame ?
      (Frame)parentComponent :
      (Frame)SwingUtilities.getAncestorOfClass(Frame.class,parentComponent);

    dialog = new JDialog(parentFrame, title ,true){

      private boolean painted = false;
    
      public void paint(Graphics g){ // Hack to set the focus and the default button to be where we want them,
                                     // because nothing else seems to work.
        if (!painted){
          painted = true;
          if (usernameField.getText().length()==0)
            usernameField.requestFocus();
          else if (passwordField.getPassword().length==0)
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
    JButton button = new JButton("Connect as guest");
    button.setMnemonic('g');
    return button;
  }




  /**
   * This method is called when the "connect as guest" button is pressed or an
   * equivalent action is taken by the user.
   */

  protected abstract void connectAsGuestActionPerformed();




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
   * @parentDialog The dialog to which the panel will be added.
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
   * @parentDialog The dialog to which the panel will be added.
   */

  public Component createAdvancedOptionsPanel(final JDialog parentDialog){
    JPanel hostnameLabelPanel = new JPanel(new GridLayout(3,1));
    hostnameLabelPanel.add(new JLabel("Server"));
    hostnameLabelPanel.add(new JLabel("hostname or"));
    JLabel hostnameLabel = new JLabel("IP number");
    hostnameLabel.setDisplayedMnemonic('I');
    hostnameLabelPanel.add(hostnameLabel);

    Box hostnamePanel = new Box(BoxLayout.X_AXIS);

    StringTokenizer hosts = new StringTokenizer(user.getProperty("login.hosts", ""), ";");
    Object [] items = new String[hosts.countTokens()];
    int i = 0;
    while (hosts.hasMoreTokens())
      items[i++] = hosts.nextToken();

    hostnameBox = new FixedJComboBox(items);
    hostnameBox.setFont(UIManager.getFont("TextField.font"));
    hostnameBox.setEditable(true);
    hostnameBox.setSelectedItem(user.getProperty("login.hostname"));
    hostnameLabel.setLabelFor(hostnameBox);

    hostnamePanel.add(hostnameBox);
    hostnamePanel.add(Box.createHorizontalStrut(10));
    hostnamePanel.add(hostnameLabelPanel);

    
    Box portPanel = new Box(BoxLayout.X_AXIS);

    Document portDocument = new IntegerStrictPlainDocument(0,65535,10);
    portField = new FixedJTextField(portDocument,String.valueOf(user.getProperty("login.port")),5);
    portField.setMaximumSize(portField.getPreferredSize());

    portPanel.add(portField);
    portPanel.add(Box.createHorizontalStrut(10));
    JLabel portLabel = new JLabel("Port number");
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
   * Creates and returns a "Forgot your password?" button. This method should
   * return null if there is no password retrieving functionality on the server.
   * The default implementation returns a simple JButton with the label
   * "Forgot your password?".
   */

  protected JButton createForgotPasswordButton(){
    JButton button = new JButton("Forgot your password?");
    button.setMnemonic('F');
    return button;
  }




  /**
   * This method is called when the "Forgot your password?" button is pressed
   * or an equivalent action is taken by the user.
   */

  protected abstract void forgotPasswordActionPerformed();





  /**
   * Creates and returns a "Connect" button. The default implementation returns
   * a simple JButton with the label "Connect". This method may not return null.
   */

  protected JButton createConnectButton(){
    JButton button = new JButton("Connect");
    button.setMnemonic('C');
    return button;
  }




  /**
   * This method is called when the connect button is pressed or an equivalent
   * action is taken by the user. The default implementation consults with
   * <code>shouldCreateNewUser</code>, then fills the user's details and
   * disposes of the dialog.
   */

  protected void connectActionPerformed(){
    if (shouldCreateNewUser())
      user = new User(user);

    user.setProperty("login.username", usernameField.getText(), true);
    boolean savePassword = savePasswordCheckBox.isSelected();
    if (savePassword)
      user.setProperty("login.password", new String(passwordField.getPassword()), true);
    else
      user.setProperty("login.password", "", true);
    user.setProperty("login.hostname", (String)hostnameBox.getSelectedItem(), true);
    user.setProperty("login.port", portField.getText(),true);
    user.setProperty("login.savepassword", String.valueOf(savePassword), true);

    proceed();
  }




  /**
   * This method determines whether the currently specified user details are
   * sufficiently different from the details of the hint user so as to warrant
   * the creation of a new User object. The default implementation checks
   * whether the usernames are different (case insensitively).
   */

  protected boolean shouldCreateNewUser(){
    return !usernameField.getText().equalsIgnoreCase(user.getProperty("login.username")); 
  }





  /**
   * Creates the "Members" panel, the panel with options for members.
   *
   * @parentDialog The dialog to which the panel will be added.
   */

  public Component createMembersPanel(final JDialog parentDialog){
    Box usernamePanel = new Box(BoxLayout.X_AXIS);

    usernameField = new FixedJTextField(15);
    usernameField.setText(user.getProperty("login.username"));

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
    passwordField.setText(user.getProperty("login.password"));

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

    boolean savePassword = Boolean.valueOf(user.getProperty("login.savepassword","false")).booleanValue();
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
        String inputIllegalityReason = findInputIllegalityReason();
        if (inputIllegalityReason!=null){
          JOptionPane.showMessageDialog(parentDialog,inputIllegalityReason,
            "Wrong Connection Settings",JOptionPane.ERROR_MESSAGE);
          return;
        }

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
   * @parentDialog The dialog to which the panel will be added.
   */

  public Component createCancelButtonPanel(final JDialog parentDialog){
    Box panel = new Box(BoxLayout.Y_AXIS);

    panel.add(Box.createVerticalGlue());

    JButton button = new JButton("Cancel");
    button.setMnemonic('a');
    button.setDefaultCapable(false);
    button.addActionListener(closeActionListener);

    panel.add(button);

    return panel;
  }



  /**
   * Determines whether the currently specified user details are valid. If so,
   * returns <code>null</code>. Otherwise returns a string specifying what's
   * invalid about them.
   * The default implementation checks that the port value is a valid port.
   */

  public String findInputIllegalityReason(){
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
    return user;
  }


}