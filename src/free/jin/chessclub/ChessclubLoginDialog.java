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

package free.jin.chessclub;

import free.jin.*;
import free.workarounds.*;
import free.util.swing.IntegerStrictPlainDocument;
import free.util.BrowserControl;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * The login dialog for the chessclub.com server.
 */

public class ChessclubLoginDialog implements LoginDialog{



  /**
   * The User that wants to log on using this LoginDialog.
   */

  private User user;




  /**
   * The JTextField containing the user's username.
   */

  private JTextField usernameField;




  /**
   * The JPasswordField containing the user's password.
   */

  private JPasswordField passwordField;



  /**
   * The JComboBox containing the hostname of the server to connect to.
   */

  private JComboBox hostnameBox;



  /**
   * The JTextField containing the port to connect on.
   */

  private JTextField portField;



  /**
   * The JCheckBox saying whether to save the user's password.
   */

  private JCheckBox savePasswordCheckBox;



  /**
   * The default button.
   */

  private JButton defaultButton = null;



  /**
   * True if the user canceled the connection, false otherwise.
   */

  private boolean isCanceled = true;



  /**
   * The dialog we open.
   */

  private JDialog dialog;




  /**
   * An action listener which cancels the dialog. It is attached to a "Cancel"
   * button and registered as the ActionListener for an ESCAPE keystroke.
   */

  private final ActionListener closeActionListener = new ActionListener(){

    public void actionPerformed(ActionEvent evt){
      isCanceled = true;
      dialog.dispose();
    }
    
  };




  /**
   * Creates a new ChessclubLoginDialog.
   */

  public ChessclubLoginDialog(){

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
    Frame parentFrame = 
      parentComponent instanceof Frame ?
      (Frame)parentComponent :
      (Frame)SwingUtilities.getAncestorOfClass(Frame.class,parentComponent);

    dialog = new JDialog(parentFrame,"Chessclub.com Login Information",true){

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
   * Creates the "Guests" panel, the panel with options for guests.
   *
   * @parentDialog The dialog to which the panel will be added.
   */

  public Component createGuestsPanel(final JDialog parentDialog){
    Box vpanel = new Box(BoxLayout.Y_AXIS);

    vpanel.add(Box.createVerticalStrut(10));

    JButton connectAsGuestButton = new JButton("Connect as guest");
    connectAsGuestButton.setDefaultCapable(false);
    connectAsGuestButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        usernameField.setText("guest");
        passwordField.setText("");

        String inputIllegalityReason = findInputIllegalityReason();
        if (inputIllegalityReason!=null){
          JOptionPane.showMessageDialog(parentDialog,inputIllegalityReason,
            "Wrong Connection Settings",JOptionPane.ERROR_MESSAGE);
          return;
        }

        isCanceled = false;
        updateUser();
        parentDialog.dispose();
      }

    });
    vpanel.add(connectAsGuestButton);

    vpanel.add(Box.createVerticalStrut(10));

    JButton joinICCButton = new JButton("Join ICC");
    joinICCButton.setDefaultCapable(false);
    joinICCButton.addActionListener(new ActionListener(){
      
      public void actionPerformed(ActionEvent evt){
        try{
          BrowserControl.displayURL("http://www.chessclub.com/from/AlexTheGreat");
        } catch (java.io.IOException e){
            JOptionPane.showMessageDialog(parentDialog,e);
          }
      }

    });
    vpanel.add(joinICCButton);

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
    hostnameLabelPanel.add(new JLabel("ICC server"));
    hostnameLabelPanel.add(new JLabel("hostname or"));
    hostnameLabelPanel.add(new JLabel("IP number"));

    Box hostnamePanel = new Box(BoxLayout.X_AXIS);

    StringTokenizer hosts = new StringTokenizer(user.getProperty("login.hosts",""),";");
    Object [] items = new String[hosts.countTokens()];
    int i = 0;
    while (hosts.hasMoreTokens())
      items[i++] = hosts.nextToken();

    hostnameBox = new FixedJComboBox(items);
    hostnameBox.setFont(UIManager.getFont("TextField.font"));
    hostnameBox.setEditable(true);
    hostnameBox.setSelectedItem(user.getProperty("login.hostname"));

    hostnamePanel.add(hostnameBox);
    hostnamePanel.add(Box.createHorizontalStrut(10));
    hostnamePanel.add(hostnameLabelPanel);

    
    Box portPanel = new Box(BoxLayout.X_AXIS);

    Document portDocument = new IntegerStrictPlainDocument(0,65535,10);
    portField = new FixedJTextField(portDocument,String.valueOf(user.getProperty("login.port")),5);
    portField.setMaximumSize(portField.getPreferredSize());

    portPanel.add(portField);
    portPanel.add(Box.createHorizontalStrut(10));
    portPanel.add(new JLabel("Port number"));
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
   * Creates the "Members" panel, the panel with options for members.
   *
   * @parentDialog The dialog to which the panel will be added.
   */

  public Component createMembersPanel(final JDialog parentDialog){
    Box usernamePanel = new Box(BoxLayout.X_AXIS);

    usernameField = new FixedJTextField(user.getProperty("login.username"),15);
    usernameField.setMaximumSize(usernameField.getPreferredSize());

    usernamePanel.add(usernameField);
    usernamePanel.add(Box.createHorizontalStrut(10));
    usernamePanel.add(new JLabel("Handle (your login name)"));
    usernamePanel.add(Box.createHorizontalGlue());

    
    Box passwordInputPanel = new Box(BoxLayout.X_AXIS);

    passwordField = new FixedJPasswordField(user.getProperty("login.password"),15);
    passwordField.setMaximumSize(passwordField.getPreferredSize());

    passwordInputPanel.add(passwordField);
    passwordInputPanel.add(Box.createHorizontalStrut(10));
    passwordInputPanel.add(new JLabel("Password"));
    passwordInputPanel.add(Box.createHorizontalGlue());


    Box passwordOptionsPanel = new Box(BoxLayout.X_AXIS);

    boolean savePassword = Boolean.valueOf(user.getProperty("login.savepassword","false")).booleanValue();
    savePasswordCheckBox = new JCheckBox("Save password",savePassword);

    JButton forgotPasswordButton = new JButton("Forgot your password?");
    forgotPasswordButton.setDefaultCapable(false);
    forgotPasswordButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        try{
          BrowserControl.displayURL("http://www.chessclub.com/mailpassword");
        } catch (java.io.IOException e){
            JOptionPane.showMessageDialog(parentDialog,e);
          }
      }

    });

    passwordOptionsPanel.add(savePasswordCheckBox);
    passwordOptionsPanel.add(Box.createHorizontalStrut(10));
    passwordOptionsPanel.add(forgotPasswordButton);
    passwordOptionsPanel.add(Box.createHorizontalGlue());


    Box decisionPanel = new Box(BoxLayout.X_AXIS);
    
    final JButton connectButton = new JButton("Connect");
    connectButton.setDefaultCapable(true);
    connectButton.addActionListener(new ActionListener(){
      
      public void actionPerformed(ActionEvent evt){
        String inputIllegalityReason = findInputIllegalityReason();
        if (inputIllegalityReason!=null){
          JOptionPane.showMessageDialog(parentDialog,inputIllegalityReason,
            "Wrong Connection Settings",JOptionPane.ERROR_MESSAGE);
          return;
        }
          
        isCanceled = false;
        updateUser();
        parentDialog.dispose();
      }

    });

    defaultButton = connectButton;


    JButton saveButton = new JButton("Save settings");
    saveButton.setDefaultCapable(false);
    saveButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        String inputIllegalityReason = findInputIllegalityReason();
        if (inputIllegalityReason!=null){
          JOptionPane.showMessageDialog(parentDialog,inputIllegalityReason,
            "Wrong Connection Settings",JOptionPane.ERROR_MESSAGE);
          return;
        }

        updateUser();
        Jin.save(user);
      }

    });

    decisionPanel.add(connectButton);
    decisionPanel.add(Box.createHorizontalStrut(10));
    decisionPanel.add(saveButton);
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
    button.setDefaultCapable(false);
    button.addActionListener(closeActionListener);

    panel.add(button);

    return panel;
  }



  /**
   * Checks whether the current input is legal and returns null if it is. Returns
   * a String specifying the reason for the illegality of the input if the input
   * is illegal.
   */

  private String findInputIllegalityReason(){
    String portString = portField.getText();
    try{
      Integer.parseInt(portString);
    } catch (NumberFormatException e){
        return "Bad port value: "+portString;
      }

    String username = usernameField.getText();
    int usernameLength = username.length();
    if ((usernameLength<2)||(usernameLength>15))
      return "Usernames must be between 2 and 15 characters long";

    boolean hasHyphenAppeared = false;
    for (int i=0;i<usernameLength;i++){
      char c = username.charAt(i);
      if (c=='-')
        if (hasHyphenAppeared)
          return "A username must contain at most one hyphen ('-')";
        else
          hasHyphenAppeared = true;
      if (!isValidUsernameCharacter(c))
        return "Your username contains at least one illegal character: "+c;
    }

    return null;
  }




  /**
   * Returns true if the given character is a valid character for a username.
   */

  private boolean isValidUsernameCharacter(char c){
    int val = c;
    if ((val>=97)&&(val<=122)) // Lowercase characters.
      return true;
    if ((val>=65)&&(val<=90)) // Uppercase characters.
      return true;
    if ((val>=48)&&(val<=57)) // Digits
      return true;
    if (c=='-') // Hyphen
      return true;
    return false;
  }




  /**
   * Updates the user properties with the information filled in by the user. If
   * the currently selected username differs (case insensitively) from the
   * current username, this method creates a new User with the same properties.
   */

  private void updateUser(){
    if (usernameField.getText().equals("guest")){ // Create a new, default user.
      user = user.getServer().createDefaultUser();
    }
    else{
      if (!usernameField.getText().equalsIgnoreCase(user.getProperty("login.username")))
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
    }
  }




  /**
   * Returns true if the user canceled the connection, false otherwise.
   */

  public boolean isCanceled(){
    return isCanceled;
  }




  /**
   * Returns a JinChessclubConnection based on the hostname, port, username and
   * password chosen by the user.
   */

  public JinConnection createConnection(){
    String hostname = (String)hostnameBox.getSelectedItem();
    int port = Integer.parseInt(portField.getText());
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());
    return new JinChessclubConnection(hostname, port, username, password);
  }




  /**
   * Returns the User object filled with the information supplied by the user.
   */

  public User getUser(){
    return user;
  }

  
}