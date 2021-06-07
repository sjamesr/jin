/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.ui;

import free.jin.I18n;
import free.util.TableLayout;
import free.util.TextUtilities;
import free.util.Utilities;
import free.util.swing.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A panel for asking questions and showing information to the user. Similar to <code>JOptionPane
 * </code>. This panel may get more <code>JOptionPane</code>-like functionality in the future.
 */
public class OptionPanel extends DialogPanel {

  /** The constant for displaying an information panel. */
  public static final Object INFO = new String("information");

  /** The constant for displaying a warning panel. */
  public static final Object WARNING = new String("warning");

  /** The constant for displaying a question panel. */
  public static final Object QUESTION = new String("question");

  /** The constant for displaying an error panel. */
  public static final Object ERROR = new String("error");

  /** The "Yes" option. */
  public static final Object YES = new String("yes");

  /** The "No" option. */
  public static final Object NO = new String("no");

  /** The "OK" option. */
  public static final Object OK = new String("ok");

  /** The "Cancel" option. */
  public static final Object CANCEL = new String("cancel");

  /** The standard order of the predefined options. */
  private static final Object[] OPTION_ORDER =
      SwingUtils.isMacLnF() || SwingUtils.isGtkLnF()
          ? new Object[] {CANCEL, YES, NO, OK}
          : new Object[] {OK, YES, NO, CANCEL};

  /**
   * The type of this panel - possible values are {@link #INFO}, {@link #QUESTION}, {@link #WARNING}
   * and {@link #ERROR}.
   */
  private final Object panelType;

  /** The options displayed to the user. */
  private final Object[] options;

  /** The option that is selected by default. */
  private final Object defaultOption;

  /** The main message component. */
  private final Component messageComponent;

  /** The component which lets the user to specify his input. <code>null</code> if none. */
  private final Component queryComponent;

  /**
   * Creates an <code>OptionPanel</code> with the specified panel type, text and list of options to
   * display to the user,
   *
   * @param hintParent The hint parent component.
   * @param panelType The type of this panel - possible values are {@link #INFO}, {@link #QUESTION},
   *     {@link #WARNING} and {@link #ERROR}.
   * @param title The title of the panel.
   * @param options The list of options the user can choose from. Possible values for each element
   *     are {@link #YES}, {@link #NO}, {@link #OK} and {@link #CANCEL}.
   * @param defaultOption The option that is selected by default.
   * @param text The text to display to the user.
   * @param queryComponent A component which allows the user to specify his input. May be <code>null
   *     </code>.
   */
  public OptionPanel(
      Component hintParent,
      Object panelType,
      String title,
      Object[] options,
      Object defaultOption,
      String text,
      Component queryComponent) {

    super(title);

    this.panelType = panelType;
    this.options = options;
    this.defaultOption = defaultOption;
    this.messageComponent = createMessageComponent(text);
    this.queryComponent = queryComponent;

    setHintParent(hintParent);

    createUI();
  }

  /** Creates an <code>OptionPanel</code> with the specified arguments. */
  public OptionPanel(
      Component hintParent,
      Object panelType,
      String title,
      Object[] options,
      Object defaultOption,
      String text) {

    this(hintParent, panelType, title, options, defaultOption, text, null);
  }

  /** Creates a message component for the specified text message. */
  public Component createMessageComponent(String text) {
    String[] lines = TextUtilities.getTokens(text, "\r\n");
    Container messagePanel = Box.createVerticalBox();

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      JLabel label = new JLabel(line);
      messagePanel.add(label);
      messagePanel.add(Box.createVerticalStrut(2));
    }

    return messagePanel;
  }

  /**
   * Creates and shows an error panel with the specified arguments.
   *
   * <p><code>hintParent</code> specifies the component over which the error message should be
   * displayed. This is optional and may be <code>null</code>.
   */
  public static void error(String title, String message, Component hintParent) {
    OptionPanel panel =
        new OptionPanel(hintParent, OptionPanel.ERROR, title, new Object[] {OK}, OK, message);
    panel.display();
  }

  /** Creates and shows an error panel with the specified arguments. */
  public static void error(String title, String message) {
    error(title, message, null);
  }

  /**
   * Creates, displays a confirmation panel with the specified arguments and returns the result
   * value. Possible result values are {@link #OK} and {@link #CANCEL}.
   *
   * <p><code>hintParent</code> specifies the component over which the error message should be
   * displayed. This is optional and may be <code>null</code>.
   */
  public static Object confirm(
      Object defaultOption, String title, String message, Component hintParent) {
    OptionPanel panel =
        new OptionPanel(
            hintParent,
            OptionPanel.QUESTION,
            title,
            new Object[] {OK, CANCEL},
            defaultOption,
            message);
    return panel.display();
  }

  /**
   * Creates, displays a confirmation panel with the specified arguments and returns the result
   * value. Possible result values are {@link #OK} and {@link #CANCEL}.
   */
  public static Object confirm(String title, String message, Object defaultOption) {
    return confirm(defaultOption, title, message, null);
  }

  /**
   * Creates, displays a yes/no question dialog with the specified arguments and returns the result
   * value. The possible result options are {@link #YES}, {@link #NO} and {@link #CANCEL}. <code>
   * hintParent</code> specifies the component over which the error message should be displayed.
   * This is optional and may be <code>null</code>.
   */
  public static Object question(
      Object defaultOption, String title, String message, Component hintParent) {
    OptionPanel panel =
        new OptionPanel(
            hintParent,
            OptionPanel.QUESTION,
            title,
            new Object[] {YES, NO},
            defaultOption,
            message);

    return panel.display();
  }

  /**
   * Creates a yes/no question dialog with the specified arguments. The possible result options are
   * {@link #YES}, {@link #NO} and {@link #CANCEL}.
   */
  public static Object question(String title, String message, Object defaultOption) {
    return question(defaultOption, title, message, null);
  }

  /**
   * Queries the user for a text value by displaying him a text field. Returns the value specified
   * by the user, or <code>null</code> if he canceled the dialog.
   *
   * @param hintParent The hint parent component.
   * @param title The title of the panel.
   * @param text The text displayed in the panel.
   * @param inputFieldLabelText The text of the label displayed in front of the text field.
   */
  public static String queryText(
      Component hintParent, String title, String text, String inputFieldLabelText) {
    JTextField inputField = new JTextField(10);
    inputField.requestFocusInWindow();

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    if (inputFieldLabelText != null)
      panel.add(new JLabel(inputFieldLabelText), BorderLayout.LINE_START);
    panel.add(inputField, BorderLayout.CENTER);

    Object result =
        new OptionPanel(hintParent, QUESTION, title, new Object[] {OK, CANCEL}, OK, text, panel)
            .display();

    if (result == CANCEL) return null;
    else return inputField.getText();
  }

  /** Returns the {@link #CANCEL} option. */
  @Override
  protected Object getCancelResult() {
    return CANCEL;
  }

  /** If the icon for this panel. */
  private Icon getIcon() {
    if (panelType == INFO) return UIManager.getIcon("OptionPane.informationIcon");
    else if (panelType == WARNING) return UIManager.getIcon("OptionPane.warningIcon");
    else if (panelType == QUESTION) return UIManager.getIcon("OptionPane.questionIcon");
    else if (panelType == ERROR) return UIManager.getIcon("OptionPane.errorIcon");
    else throw new IllegalArgumentException("Bad panel type: " + panelType);
  }

  /** Creates the UI for this panel. */
  private void createUI() {
    I18n i18n = I18n.get(OptionPanel.class);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 2));
    buttonPanel.setOpaque(false);

    for (int i = 0; i < OPTION_ORDER.length; i++) {
      if (!Utilities.contains(options, OPTION_ORDER[i])) continue;

      Object option = OPTION_ORDER[i];
      JButton button = new JButton(i18n.getString(option.toString() + ".text"));
      button.addActionListener(new ClosingListener(option));
      if (option == defaultOption) setDefaultButton(button);

      buttonPanel.add(button);
    }

    Component mainUi = createMainUi();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(mainUi);
    add(Box.createVerticalStrut(15));
    add(buttonPanel);
  }

  /** Creates the main UI of this panel. */
  protected Component createMainUi() {
    Box mainPanel = Box.createHorizontalBox();

    JPanel innerPanel = new JPanel(new TableLayout(1, 10, 10));
    innerPanel.add(messageComponent);
    if (queryComponent != null) innerPanel.add(queryComponent);

    mainPanel.add(new JLabel(getIcon()));
    mainPanel.add(Box.createHorizontalStrut(10));
    mainPanel.add(innerPanel);

    return mainPanel;
  }

  /**
   * Displays this <code>OptionPanel</code> and returns the option chosen by the user. Note that the
   * return value may be {@link #CANCEL} even if you didn't specify it in the constructor, since
   * that is the value returned when the user cancels the panel without selecting any options.
   */
  public Object display() {
    return super.askResult();
  }
}
