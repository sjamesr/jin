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
import free.util.Utilities;
import free.util.TextUtilities;


/**
 * A panel for asking questions and showing information to the user. Similar
 * to <code>JOptionPane</code>. This panel may get more
 * <code>JOptionPane</code>-like functionality.
 */

public class OptionPanel extends DialogPanel{



  /**
   * The constant for displaying an information panel.
   */

  public static final Object INFO = new String("Information");



  /**
   * The constant for displaying a warning panel.
   */

  public static final Object WARNING = new String("Warning");



  /**
   * The constant for displaying a question panel.
   */

  public static final Object QUESTION = new String("Question");



  /**
   * The constant for displaying an error panel.
   */

  public static final Object ERROR = new String("Error");



  /**
   * The "Yes" option.
   */

  public static final Object YES = new String("Yes");



  /**
   * The "No" option.
   */

  public static final Object NO = new String("No");



  /**
   * The "OK" option.
   */

  public static final Object OK = new String("OK");



  /**
   * The "Cancel" option.
   */

  public static final Object CANCEL = new String("Cancel");



  /**
   * The standard order of the predefined options.
   */

  private static final Object [] optionOrder = new Object[]{OK, YES, NO, CANCEL};



  /**
   * The type of this panel - possible values are {@link #INFO},
   * {@link #QUESTION}, {@link #WARNING} and {@link #ERROR}.
   */

  private final Object panelType;



  /**
   * The title of the panel.
   */

  private final String title;



  /**
   * The options displayed to the user.
   */

  private final Object [] options;



  /**
   * The option that is selected by default.
   */

  private final Object defaultOption;



  /**
   * The main message component.
   */

  private final Component messageComponent;



  /**
   * Creates an <code>OptionPanel</code> with the specified panel type, text
   * and list of options to display to the user,
   *
   * @param panelType The type of this panel - possible values are
   * {@link #INFO}, {@link #QUESTION}, {@link #WARNING} and {@link #ERROR}.
   * @param title The title of the panel.
   * @param options The list of options the user can choose from. Possible
   * values for each element are {@link #YES}, {@link #NO}, {@link #OK} and
   * {@link #CANCEL}.
   * @param defaultOption The option that is selected by default.
   * @param text The text to display to the user.
   */

  public OptionPanel(Object panelType, String title, Object [] options, Object defaultOption, String text){
    this.panelType = panelType;
    this.title = title;
    this.options = options;
    this.defaultOption = defaultOption;
    this.messageComponent = createMessageComponent(text);

    createUI();
  }



  /**
   * Creates a message component for the specified text message.
   */

  public Component createMessageComponent(String text){
    String [] lines = TextUtilities.getTokens(text, "\r\n");
    Container messagePanel = Box.createVerticalBox();

    for (int i = 0; i < lines.length; i++){
      String line = lines[i];
      JLabel label = new JLabel(line);
      messagePanel.add(label);
      messagePanel.add(Box.createVerticalStrut(2));
    }

    return messagePanel;
  }



  /**
   * Creates, shows an error panel with the specified arguments.
   */

  public static void error(UIProvider ui, String title, String message){
    OptionPanel panel = new OptionPanel(OptionPanel.ERROR, title, new Object[]{OK}, OK, message);
    panel.show(ui);
  }



  /**
   * Creates, shows a confirmation panel with the specified arguments and
   * returns the result value. Possible result values are {@link #OK} and
   * {@link #CANCEL}.
   */

  public static Object confirm(UIProvider ui, String title, String message, Object defaultOption){
    OptionPanel panel = new OptionPanel(OptionPanel.QUESTION, title,
      new Object[]{OK, CANCEL}, defaultOption, message);
    return panel.show(ui);
  }



  /**
   * Creates a yes/no question dialog with the specified arguments. The possible
   * result options are {@link #YES}, {@link #NO} and {@link #CANCEL}.
   */

  public static Object question(UIProvider ui, String title, String message, Object defaultOption){
    OptionPanel panel = new OptionPanel(OptionPanel.QUESTION, title,
      new Object[]{YES, NO}, defaultOption, message);
    return panel.show(ui);
  }



  /**
   * Returns the title of this <code>DialogPanel</code>.
   */

  protected String getTitle(){
    return title;
  }



  /**
   * Returns the {@link #CANCEL} option.
   */

  protected Object getCancelResult(){
    return CANCEL;
  }



  /**
   * If the icon for this panel.
   */

  private Icon getIcon(){
    if (panelType == INFO)
      return UIManager.getIcon("OptionPane.informationIcon");
    else if (panelType == WARNING)
      return UIManager.getIcon("OptionPane.warningIcon");
    else if (panelType == QUESTION)
      return UIManager.getIcon("OptionPane.questionIcon");
    else if (panelType == ERROR)
      return UIManager.getIcon("OptionPane.errorIcon");
    else throw new IllegalArgumentException("Bad panel type: " + panelType);
  }



  /**
   * Creates the UI for this panel.
   */

  private void createUI(){
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
    buttonPanel.setOpaque(false);

    for (int i = 0; i < optionOrder.length; i++){
      if (!Utilities.contains(options, optionOrder[i]))
        continue;

      Object option = optionOrder[i];
      JButton button = new JButton(option.toString());
      button.addActionListener(new ClosingListener(option));
      if (option == defaultOption)
        setDefaultButton(button);

      buttonPanel.add(button);
    }

    Box mainPanel = Box.createHorizontalBox();
    mainPanel.add(new JLabel(getIcon()));
    mainPanel.add(Box.createHorizontalStrut(10));
    mainPanel.add(messageComponent);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(mainPanel);
    add(Box.createVerticalStrut(15));
    add(buttonPanel);
  }



  /**
   * Displays this <code>OptionPanel</code> using the specified
   * <code>UIProvider</code> and returns the option chosen by the user. Note
   * that the return value be {@link #CANCEL} even if you didn't specify it in
   * the constructor, since that is the value returned when the user cancels
   * the panel without selecting any options.
   */

  public Object show(UIProvider uiProvider){
    return super.askResult(uiProvider);
  }



}
