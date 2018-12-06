/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2008 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.console.prefs;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.text.JTextComponent;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.console.Channel;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.Encodings;
import free.util.Utilities;
import free.util.swing.AddRemoveButtons;

/**
 * A preferences panel allowing the user to specify the set of custom consoles he will be using.
 *
 * @author Maryanovsky Alexander
 */
public abstract class CustomConsolesPrefsPanel extends PreferencesPanel {

  /**
   * The name of the main console window (what we display as the first item) in the windows combo
   * box.
   */
  private static final String MAIN_WINDOW_NAME =
      I18n.get(CustomConsolesPrefsPanel.class).getString("mainWindowName");

  /**
   * The console manager we're part of.
   */
  protected final ConsoleManager consoleManager;

  /**
   * The list model of console specs.
   */
  protected final DefaultListModel consoles;

  /**
   * The <code>JList</code> displaying the console specs.
   */
  protected final JList consolesList;

  /**
   * The "Add Console" button.
   */
  protected final JButton addConsoleButton;

  /**
   * The "Remove Console" button.
   */
  protected final JButton removeConsoleButton;

  /**
   * The "Move Up" button.
   */
  protected final JButton moveUpButton;

  /**
   * The "Move Down" button.
   */
  protected final JButton moveDownButton;

  /**
   * The textfield for the console's title.
   */
  protected final JTextField titleField;

  /**
   * The <code>ComboBoxModel</code> of the window box.
   */
  private final DefaultComboBoxModel windowModel = new DefaultComboBoxModel();

  /**
   * The combo box for the console's window.
   */
  protected final JComboBox windowBox;

  /**
   * The <code>ComboBoxModel</code> of the encoding box.
   */
  private final DefaultComboBoxModel encodingModel = new DefaultComboBoxModel();

  /**
   * The combo box for the console's encoding.
   */
  protected final JComboBox encodingBox;

  /**
   * The text field for the console's channels.
   */
  protected final JTextField channelsField;

  /**
   * The "add/remove" widget for adding and removing channels.
   */
  protected final AddRemoveButtons addRemoveChannels;

  /**
   * The text field where the user can specify a regular expression such that any matching messages
   * are displayed in the console.
   */
  protected final JTextField messageRegexField;

  /**
   * The label for the consoles list.
   */
  protected final JLabel consolesListLabel;

  /**
   * The scrollpane for the console's list.
   */
  protected final JScrollPane consolesListScrollPane;

  /**
   * The label for the title field.
   */
  protected final JLabel titleLabel;

  /**
   * The label for the window box.
   */
  protected final JLabel windowLabel;

  /**
   * The label for the encoding box.
   */
  protected final JLabel encodingLabel;

  /**
   * The label for the channels field.
   */
  protected final JLabel channelsLabel;

  /**
   * The label for the message regex field.
   */
  protected final JLabel messageRegexLabel;

  /**
   * This flag is set to when the values of the console properties UI are being changed, but no
   * console properties should be updated (for example, because the selected console changed). This
   * is done so that the corresponding listener knows not to fire a <code>ChangeEvent</code>.
   */
  private boolean isIgnoreConsolePropertiesChange = false;

  /**
   * This flag is set when changes in the console selection are to be ignored. This happens, for
   * example, when a console is being deleted and the selection is changed programmatically.
   */
  private boolean isIgnoreConsoleListSelectionChange = false;

  /**
   * We set this to <code>true</code> after invoking {@link #createLayout()} to know not to invoke
   * it again.
   */
  private boolean layoutCreated = false;

  /**
   * Creates a new <code>CustomConsolesPrefsPanel</code> for the specified
   * <code>ConsoleManager</code>.
   */
  public CustomConsolesPrefsPanel(ConsoleManager consoleManager) {
    if (consoleManager == null)
      throw new IllegalArgumentException("consoleManager may not be null");

    I18n i18n = I18n.get(CustomConsolesPrefsPanel.class);

    this.consoleManager = consoleManager;
    this.consoles = new DefaultListModel();
    this.consolesList = new JList(consoles);
    this.addConsoleButton = i18n.createButton("addConsoleButton");
    this.removeConsoleButton = i18n.createButton("removeConsoleButton");
    this.moveUpButton = i18n.createButton("moveUpButton");
    this.moveDownButton = i18n.createButton("moveDownButton");
    this.titleField = new JTextField();
    this.windowBox = new JComboBox(windowModel);
    this.encodingBox = new JComboBox(encodingModel);
    this.channelsField = new JTextField();
    this.addRemoveChannels = new AddRemoveButtons();
    this.messageRegexField = new JTextField();

    this.consolesListLabel = i18n.createLabel("consolesListLabel");
    this.consolesListScrollPane = new JScrollPane(consolesList);
    this.titleLabel = i18n.createLabel("titleLabel");
    this.windowLabel = i18n.createLabel("windowLabel");
    this.encodingLabel = i18n.createLabel("encodingLabel");
    this.channelsLabel = i18n.createLabel("channelsLabel");
    this.messageRegexLabel = i18n.createLabel("messageRegexLabel");

    Preferences prefs = consoleManager.getPrefs();
    for (int i = prefs.getInt("consoles.count") - 1; i > 0; i--) {
      String prefix = "consoles." + i + ".";
      if (!"custom".equals(prefs.getString(prefix + "type"))) break;

      consoles.add(0, loadConsoleSpec(prefs, prefix));
    }

    DocumentListener changeFiringDocumentListener =
        new DocumentListener() {
          @Override
          public void changedUpdate(DocumentEvent e) {
            if (!isIgnoreConsolePropertiesChange()) fireStateChanged();
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            if (!isIgnoreConsolePropertiesChange()) fireStateChanged();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            if (!isIgnoreConsolePropertiesChange()) fireStateChanged();
          }
        };

    consoles.addListDataListener(
        new ListDataListener() {
          @Override
          public void contentsChanged(ListDataEvent e) {
            fireStateChanged();
          }

          @Override
          public void intervalAdded(ListDataEvent e) {
            fireStateChanged();
          }

          @Override
          public void intervalRemoved(ListDataEvent e) {
            fireStateChanged();
          }
        });

    consolesList.addListSelectionListener(
        new ListSelectionListener() {
          private int selectedIndex = -1;

          @Override
          public void valueChanged(ListSelectionEvent evt) {
            if (evt.getValueIsAdjusting()) return;

            if (isIgnoreConsoleListSelectionChange()) {
              selectedIndex = consolesList.getSelectedIndex();
              return;
            }

            if ((selectedIndex != -1) && (selectedIndex < consoles.size())) {
              try {
                updateConsoleFromUi((ConsoleSpec) consoles.get(selectedIndex));
              } catch (BadChangesException e) {
                try {
                  setIgnoreConsoleListSelectionChange(true);
                  consolesList.setSelectedIndex(selectedIndex);
                } finally {
                  setIgnoreConsoleListSelectionChange(false);
                }

                badChangeAttempted(e);
                return;
              }
            }

            selectedIndex = consolesList.getSelectedIndex();
            updateUiFromSelectedConsole();
          }
        });

    consolesList.addListSelectionListener(
        new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
            int selectedIndex = consolesList.getSelectedIndex();

            removeConsoleButton.setEnabled(selectedIndex != -1);
            moveUpButton.setEnabled((selectedIndex != -1) && (selectedIndex > 0));
            moveDownButton.setEnabled(
                (selectedIndex != -1) && (selectedIndex < consoles.size() - 1));
          }
        });

    consolesList.setVisibleRowCount(4);
    consolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    addConsoleButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            consoles.addElement(createNewConsoleSpec());
            consolesList.setSelectedIndex(consoles.size() - 1);
            consolesList.ensureIndexIsVisible(consolesList.getSelectedIndex());

            updateUiFromSelectedConsole();

            titleField.requestFocusInWindow();
          }
        });

    removeConsoleButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            int selectedIndex = consolesList.getSelectedIndex();
            if (selectedIndex == -1) return;

            try {
              setIgnoreConsoleListSelectionChange(true);
              consoles.removeElementAt(selectedIndex);
              if (selectedIndex < consoles.size()) consolesList.setSelectedIndex(selectedIndex);
              else if (consoles.size() != 0) consolesList.setSelectedIndex(selectedIndex - 1);
              consolesList.ensureIndexIsVisible(consolesList.getSelectedIndex());
            } finally {
              setIgnoreConsoleListSelectionChange(false);
            }

            updateUiFromSelectedConsole();
            fireStateChanged();
          }
        });

    moveUpButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            int selectedIndex = consolesList.getSelectedIndex();
            if ((selectedIndex == -1) || (selectedIndex == 0)) return;

            try {
              setIgnoreConsoleListSelectionChange(true);
              consoles.add(selectedIndex - 1, consoles.remove(selectedIndex));
              consolesList.setSelectedIndex(selectedIndex - 1);
              consolesList.ensureIndexIsVisible(consolesList.getSelectedIndex());
            } finally {
              setIgnoreConsoleListSelectionChange(false);
            }
          }
        });

    moveDownButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            int selectedIndex = consolesList.getSelectedIndex();
            if ((selectedIndex == -1) || (selectedIndex == consoles.size() - 1)) return;

            try {
              setIgnoreConsoleListSelectionChange(true);
              consoles.add(selectedIndex + 1, consoles.remove(selectedIndex));
              consolesList.setSelectedIndex(selectedIndex + 1);
              consolesList.ensureIndexIsVisible(consolesList.getSelectedIndex());
            } finally {
              setIgnoreConsoleListSelectionChange(false);
            }
          }
        });

    titleField.getDocument().addDocumentListener(changeFiringDocumentListener);
    titleField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void changedUpdate(DocumentEvent e) {
                updateTitle();
              }

              @Override
              public void insertUpdate(DocumentEvent e) {
                updateTitle();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                updateTitle();
              }

              private void updateTitle() {
                if (isIgnoreConsolePropertiesChange()) return;

                ConsoleSpec spec = getSelectedConsole();
                if (spec == null) return;

                spec.setTitle(titleField.getText());
                consolesList.repaint();
              }
            });

    windowModel.addElement(MAIN_WINDOW_NAME);
    int customWindowsCount = prefs.getInt("containers.custom.count", 0);
    for (int i = 0; i < customWindowsCount; i++) {
      String title = prefs.getString("containers.custom." + i + ".title", "");
      windowModel.addElement(title);
    }
    windowBox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (isIgnoreConsolePropertiesChange()) return;

            ConsoleSpec spec = getSelectedConsole();
            if (spec == null) return;

            String windowTitle = (String) windowBox.getSelectedItem();
            spec.setWindow(windowTitle);

            if (windowModel.getIndexOf(windowTitle) == -1) windowModel.addElement(windowTitle);

            fireStateChanged();
          }
        });

    Component windowEditorComponent = windowBox.getEditor().getEditorComponent();
    if (windowEditorComponent instanceof JTextComponent) {
      JTextComponent textComponent = (JTextComponent) windowEditorComponent;
      textComponent.getDocument().addDocumentListener(changeFiringDocumentListener);
    }
    windowBox.setEditable(true);

    encodingModel.addElement(null); // Default encoding
    Map categoriesToEncodings = Encodings.categoriesToEncodings();
    Map categoriesToNames = Encodings.categoriesToNames();
    for (Iterator i = Encodings.categories().iterator(); i.hasNext(); ) {
      String category = (String) i.next();
      List encodings = (List) categoriesToEncodings.get(category);
      String categoryName = (String) categoriesToNames.get(category);

      if (encodings.isEmpty()) continue;

      encodingModel.addElement(categoryName);
      for (Iterator j = encodings.iterator(); j.hasNext(); ) {
        Charset encoding = (Charset) j.next();
        encodingModel.addElement(encoding);
      }
    }

    // Allow selecting only charsets, not category names
    encodingBox.addActionListener(
        new ActionListener() {
          private Charset selectedEncoding;

          @Override
          public void actionPerformed(ActionEvent evt) {
            if (isIgnoreConsolePropertiesChange()) return;

            Object item = encodingBox.getSelectedItem();
            if ((item instanceof Charset) || (item == null)) {
              selectedEncoding = (Charset) item;

              ConsoleSpec spec = getSelectedConsole();
              if (spec == null) return;

              spec.setEncoding(selectedEncoding == null ? null : selectedEncoding.name());
              fireStateChanged();
            } else encodingBox.setSelectedItem(selectedEncoding);
          }
        });

    encodingBox.setRenderer(new EncodingBoxCellRenderer(encodingBox.getRenderer()));

    encodingBox.setEditable(false);
    if (consoleManager.getConn().getTextEncoding() == null) {
      encodingBox.setVisible(false);
      encodingLabel.setVisible(false);
    }

    channelsField.getDocument().addDocumentListener(changeFiringDocumentListener);

    addRemoveChannels
        .getAddButton()
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                final ConsoleSpec spec = (ConsoleSpec) consolesList.getSelectedValue();
                if (spec == null) return;

                ConsoleManager consoleManager = CustomConsolesPrefsPanel.this.consoleManager;
                SortedMap channels = new TreeMap(consoleManager.getChannels());
                channels.values().removeAll(spec.getChannels());
                Channel[] channelsArr =
                    (Channel[]) channels.values().toArray(new Channel[channels.size()]);
                final ChannelsPopup popup = new ChannelsPopup(channelsArr);

                popup.addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        popup.hide();

                        Channel channel = popup.getSelectedChannel();
                        spec.addChannel(channel);
                        updateUiFromSelectedConsole();
                        fireStateChanged();
                      }
                    });

                popup.show(addRemoveChannels, 0, addRemoveChannels.getHeight());
              }
            });

    addRemoveChannels
        .getRemoveButton()
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                final ConsoleSpec spec = (ConsoleSpec) consolesList.getSelectedValue();
                if (spec == null) return;

                Channel[] channelsArr = (Channel[]) spec.getChannels().toArray(new Channel[0]);
                final ChannelsPopup popup = new ChannelsPopup(channelsArr);

                popup.addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        popup.hide();

                        Channel channel = popup.getSelectedChannel();
                        spec.removeChannel(channel);
                        updateUiFromSelectedConsole();
                        fireStateChanged();
                      }
                    });

                popup.show(addRemoveChannels, 0, addRemoveChannels.getHeight());
              }
            });

    messageRegexField.getDocument().addDocumentListener(changeFiringDocumentListener);

    consolesListLabel.setLabelFor(consolesList);
    titleLabel.setLabelFor(titleField);
    windowLabel.setLabelFor(windowBox);
    encodingLabel.setLabelFor(encodingBox);
    channelsLabel.setLabelFor(channelsField);
    messageRegexLabel.setLabelFor(messageRegexField);

    consolesListScrollPane.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    consolesListScrollPane.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  }

  /**
   * Returns <code>true</code>.
   */
  @Override
  public boolean applyRequiresRestart() {
    return true;
  }

  /**
   * Invokes {@link #createLayout()} the first time we're added anywhere.
   */
  @Override
  public void addNotify() {
    super.addNotify();

    if (!layoutCreated) {
      layoutCreated = true;
      createLayout();

      int selectedIndex = consoles.size() - 1;
      consolesList.setSelectedIndex(selectedIndex);
      consolesList.ensureIndexIsVisible(selectedIndex);

      updateUiFromSelectedConsole();
    }
  }

  /**
   * Creates the layout of this panel.
   */
  protected abstract void createLayout();

  /**
   * Returns the currently selected console; <code>null</code> if none.
   */
  protected ConsoleSpec getSelectedConsole() {
    return (ConsoleSpec) consolesList.getSelectedValue();
  }

  /**
   * Updates the UI according to the currently selected console.
   */
  protected void updateUiFromSelectedConsole() {
    try {
      setIgnoreConsolePropertiesChange(true);

      ConsoleSpec spec = getSelectedConsole();
      updateConsoleTitleUi(spec);
      updateWindowUi(spec);
      updateEncodingUi(spec);
      updateConsoleChannelsUi(spec);
      updateRegexMatchUi(spec);
    } finally {
      setIgnoreConsolePropertiesChange(false);
    }
  }

  /**
   * Updates the console title UI from the specified selected spec (which may be <code>null</code>,
   * if there is no spec selected).
   */
  private void updateConsoleTitleUi(ConsoleSpec spec) {
    titleLabel.setEnabled(spec != null);
    titleField.setText(spec == null ? "" : spec.getTitle());
    titleField.setEnabled(spec != null);
    titleField.setEditable(spec != null);
  }

  /**
   * Updates the console window UI from the specified selected spec (which may be <code>null</code>,
   * if there is no spec selected).
   */
  private void updateWindowUi(ConsoleSpec spec) {
    windowLabel.setEnabled(spec != null);

    if (spec == null) windowBox.setSelectedIndex(-1);
    else windowBox.setSelectedItem(spec.getWindow());
    windowBox.setEnabled(spec != null);
  }

  /**
   * Updates the encoding UI from the specified selected spec (which may be <code>null</code>, if
   * there is no spec selected).
   */
  private void updateEncodingUi(ConsoleSpec spec) {
    encodingLabel.setEnabled(spec != null);
    encodingBox.setEnabled(spec != null);

    if (spec == null) encodingBox.setSelectedIndex(-1);
    else {
      String encoding = spec.getEncoding();
      Charset charset = encoding == null ? null : Charset.forName(encoding);
      int selectedIndex = -1;
      ListModel encodingsModel = encodingBox.getModel();
      for (int i = 0; i < encodingModel.getSize(); i++) {
        if (Utilities.areEqual(charset, encodingsModel.getElementAt(i))) {
          selectedIndex = i;
          break;
        }
      }
      encodingBox.setSelectedIndex(selectedIndex);
    }
  }

  /**
   * Upates the console channels UI from the specified selected spec (which may be <code>null</code>
   * , if there is no spec selected).
   */
  private void updateConsoleChannelsUi(ConsoleSpec spec) {
    channelsLabel.setEnabled(spec != null);
    channelsField.setText(spec == null ? "" : makeChannelListDisplayString(spec.getChannels()));
    channelsField.setEnabled(spec != null);
    channelsField.setEditable(spec != null);

    addRemoveChannels.getAddButton().setEnabled(spec != null);
    addRemoveChannels.getRemoveButton().setEnabled((spec != null) && !spec.getChannels().isEmpty());
  }

  /**
   * Updates the match regex UI from the specified selected spec (which may be <code>null</code>, if
   * there is no spec selected).
   */
  private void updateRegexMatchUi(ConsoleSpec spec) {
    messageRegexLabel.setEnabled(spec != null);
    messageRegexField.setText(spec == null ? "" : spec.getMessageRegex());
    messageRegexField.setEnabled(spec != null);
    messageRegexField.setEditable(spec != null);
  }

  /**
   * Creates a string to be displayed in the channels field for the specified channels.
   */
  protected abstract String makeChannelListDisplayString(List channels);

  /**
   * Parses the string displayed in the channels field and returns the corresponding list of
   * channels. Throws a {@link BadChangesException} if the value is badly formatted.
   */
  protected abstract List parseChannelsListDisplayString(String channels)
      throws BadChangesException;

  /**
   * Sets the properties of the specified console from the UI.
   *
   * @throws BadChangesException if the UI does not specify legal values.
   */
  protected void updateConsoleFromUi(ConsoleSpec spec) throws BadChangesException {
    I18n i18n = I18n.get(CustomConsolesPrefsPanel.class);

    String errorMessage = null;
    Component errorComponent = null;

    String title = titleField.getText();
    String window = (String) windowBox.getSelectedItem();
    Charset encoding = (Charset) encodingBox.getSelectedItem();
    List channels = parseChannelsListDisplayString(channelsField.getText());
    String matchRegex = messageRegexField.getText();

    if ((title == null) || "".equals(title)) {
      errorMessage = i18n.getString("consoleTitleUnspecifiedErrorMessage");
      errorComponent = titleField;
    }

    try {
      if ((matchRegex != null) && (matchRegex.trim().length() != 0)) Pattern.compile(matchRegex);
      else matchRegex = null;
    } catch (PatternSyntaxException e) {
      errorMessage = i18n.getString("badMessageRegexErrorMessage");
      errorComponent = messageRegexField;
    }

    if (errorMessage != null) throw new BadChangesException(errorMessage, errorComponent);

    spec.setTitle(title);
    spec.setWindow(window);
    spec.setEncoding(encoding == null ? null : encoding.name());
    spec.setChannels(channels);
    spec.setMessageRegex(matchRegex);
  }

  /**
   * Returns whether we are temporarily ignoring changes in the console properties UI.
   */
  protected final boolean isIgnoreConsolePropertiesChange() {
    return isIgnoreConsolePropertiesChange;
  }

  /**
   * Sets whether we are to temporarily ignore changes in the console properties UI. This is used
   * when the changes don't indicate a real change in the settings of a console, such as when the
   * selected console changes, and we update the UI.
   */
  protected final void setIgnoreConsolePropertiesChange(boolean ignore) {
    this.isIgnoreConsolePropertiesChange = ignore;
  }

  /**
   * Returns whether we are temporarily ignoring changes in the console selection.
   */
  protected final boolean isIgnoreConsoleListSelectionChange() {
    return isIgnoreConsoleListSelectionChange;
  }

  /**
   * Sets whether we are to temporarily ignore changes in the console selection. This is used when
   * such changes are done programmatically, in a controlled manner, and don't require the default
   * response (for example, when a console is being removed).
   */
  protected final void setIgnoreConsoleListSelectionChange(boolean ignore) {
    this.isIgnoreConsoleListSelectionChange = ignore;
  }

  /**
   * Applies the changes in the preferences panel, storing the preferences.
   */
  @Override
  public void applyChanges() throws BadChangesException {
    int selectedIndex = consolesList.getSelectedIndex();
    if (selectedIndex != -1) updateConsoleFromUi((ConsoleSpec) consoles.get(selectedIndex));

    Preferences prefs = consoleManager.getPrefs();

    // Store custom windows list
    Set customWindows = new TreeSet();
    for (int i = 0; i < consoles.size(); i++) {
      ConsoleSpec spec = (ConsoleSpec) consoles.get(i);
      String window = spec.getWindow();
      if (!MAIN_WINDOW_NAME.equals(window)) customWindows.add(window);
    }
    List customWindowsList = new ArrayList(customWindows);
    for (int i = 0; i < customWindowsList.size(); i++) {
      String title = (String) customWindowsList.get(i);
      prefs.setString("containers.custom." + i + ".title", title);
    }
    prefs.setInt("containers.custom.count", customWindows.size());

    // Store custom consoles
    int firstCustomIndex = prefs.getInt("consoles.count");
    while ("custom".equals(prefs.getString("consoles." + (firstCustomIndex - 1) + ".type")))
      firstCustomIndex--;
    for (int i = 0; i < consoles.size(); i++) {
      String prefix = "consoles." + (i + firstCustomIndex) + ".";
      prefs.setString(prefix + "type", "custom");
      ConsoleSpec spec = (ConsoleSpec) consoles.get(i);
      storeConsoleSpec(prefs, spec, prefix);
    }
    prefs.setInt("consoles.count", firstCustomIndex + consoles.size());
  }

  /**
   * Creates a <code>ConsoleSpec</code> for a new console. This method may be overridden to return a
   * server-specific type. This is the only method used to create new <code>ConsoleSpec</code>s, so
   * the subclass is guaranteed to ever only having to deal with the type it creates.
   */
  protected ConsoleSpec createNewConsoleSpec() {
    return new ConsoleSpec();
  }

  /**
   * Loads a <code>ConsoleSpec</code> with the specified prefix from preferences.
   */
  protected ConsoleSpec loadConsoleSpec(Preferences prefs, String prefix) {
    ConsoleSpec spec = createNewConsoleSpec();

    spec.setTitle(prefs.getString(prefix + "title"));
    spec.setEncoding(prefs.getString(prefix + "encoding", null));
    spec.setChannels(consoleManager.parseConsoleChannelsPref(prefs.get(prefix + "channels", null)));
    spec.setMessageRegex(prefs.getString(prefix + "messageRegex", null));

    String containerId = prefs.getString(prefix + "container.id", ConsoleManager.MAIN_CONTAINER_ID);
    if (containerId.equals(ConsoleManager.MAIN_CONTAINER_ID)) spec.setWindow(MAIN_WINDOW_NAME);
    else spec.setWindow(prefs.getString("containers." + containerId + ".title", ""));

    return spec;
  }

  /**
   * Stores the specified <code>ConsoleSpec</code> into preferences, using the specified prefix.
   */
  protected void storeConsoleSpec(Preferences prefs, ConsoleSpec spec, String prefix) {
    prefs.setString(prefix + "title", spec.getTitle());
    prefs.setString(prefix + "encoding", spec.getEncoding());
    prefs.set(prefix + "channels", consoleManager.encodeConsoleChannelsPref(spec.getChannels()));
    prefs.setString(prefix + "messageRegex", spec.getMessageRegex());

    String window = spec.getWindow();
    if (window.equals(MAIN_WINDOW_NAME))
      prefs.setString(prefix + "container.id", ConsoleManager.MAIN_CONTAINER_ID);
    else {
      int customWindowsCount = prefs.getInt("containers.custom.count", 0);
      for (int i = 0; i < customWindowsCount; i++) {
        String containerId = "custom." + i;
        String title = prefs.getString("containers." + containerId + ".title", "");
        System.out.println("Comparing " + window + " with " + title);
        if (window.equals(title)) {
          prefs.setString(prefix + "container.id", containerId);
          break;
        }
      }
    }
  }

  /**
   * Returns the string to be displayed in the channels add/remove popup for the specified channel.
   */
  protected abstract String makeChannelPopupString(Channel channel);

  /**
   * Encapsulates information about a custom console.
   */
  protected static class ConsoleSpec {

    /**
     * The default title we display when the title is empty.
     */
    private static final String DEFAULT_TITLE =
        I18n.get(CustomConsolesPrefsPanel.class).getString("defaultConsoleTitle");

    /**
     * The console's title.
     */
    private String title = "";

    /**
     * The console's window.
     */
    private String window = MAIN_WINDOW_NAME;

    /**
     * The console's encoding; <code>null</code> for default.
     */
    private String encoding;

    /**
     * The list of channels the console is showing.
     */
    private final List channels = new LinkedList();

    /**
     * The regular expression that messages are matched against to determine whether they are
     * displayed in the console; <code>null</code> if none.
     */
    private String messageRegex = null;

    /**
     * Returns the title.
     */
    public String getTitle() {
      return title;
    }

    /**
     * Sets the title.
     */
    public void setTitle(String title) {
      this.title = title;
    }

    /**
     * Returns the window.
     */
    public String getWindow() {
      return window;
    }

    /**
     * Sets the window.
     */
    public void setWindow(String window) {
      this.window = window;
    }

    /**
     * Returns the encoding; <code>null</code> for default.
     */
    public String getEncoding() {
      return encoding;
    }

    /**
     * Sets the encoding. Use <code>null</code> for the default encoding.
     */
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Returns the list of channels.
     */
    public List getChannels() {
      return Collections.unmodifiableList(channels);
    }

    /**
     * Sets the list of channels.
     */
    public void setChannels(List channels) {
      this.channels.clear();
      this.channels.addAll(channels);
    }

    /**
     * Adds a channel.
     */
    public void addChannel(Channel channel) {
      channels.add(channel);
    }

    /**
     * Removes a channel.
     */
    public void removeChannel(Channel channel) {
      channels.remove(channel);
    }

    /**
     * Returns the message regex.
     */
    public String getMessageRegex() {
      return messageRegex;
    }

    /**
     * Sets the match regex.
     */
    public void setMessageRegex(String messageRegex) {
      this.messageRegex = messageRegex;
    }

    /**
     * Returns the console's title, or a special string, if it's empty.
     */
    @Override
    public String toString() {
      String title = getTitle();
      return "".equals(title) ? DEFAULT_TITLE : title;
    }
  }

  /**
   * The popup we use to display the list of channels for the user to add/remove.
   */
  private class ChannelsPopup extends BasicComboPopup {

    /**
     * The list of channels.
     */
    private final Channel[] channels;

    /**
     * Creates a new <code>ChannelsPopup</code> with the specified list of channels.
     */
    public ChannelsPopup(Channel[] channels) {
      super(new JComboBox());

      this.channels = channels;

      for (int i = 0; i < channels.length; i++)
        comboBox.addItem(makeChannelPopupString(channels[i]));

      comboBox.setSelectedItem(null);
      list.setVisibleRowCount(Math.min(10, channels.length));

      setBorder(null);
    }

    /**
     * Adds an action listener to receive notifications when an item has been selected in the popup.
     */
    public void addActionListener(ActionListener listener) {
      comboBox.addActionListener(listener);
    }

    /**
     * Removes an action listener from receiving notifications when an item has been selected in the
     * popup.
     */
    public void removeActionListener(ActionListener listener) {
      comboBox.removeActionListener(listener);
    }

    /**
     * Returns the currently selected channel; <code>null</code> if none.
     */
    public Channel getSelectedChannel() {
      int selectedIndex = comboBox.getSelectedIndex();
      return selectedIndex == -1 ? null : channels[selectedIndex];
    }
  }

  /**
   * The list cell renderer we use to render the encodings combo box. Note that we subclass JLabel -
   * this is completely unnecessary, except for swing-layout (AquaBaseline, for example, finds a
   * combo box's baseline correctly only if the renderer is an instance of JLabel).
   */
  private class EncodingBoxCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * The delegate renderer (the box's original renderer).
     */
    private final ListCellRenderer delegateRenderer;

    /**
     * Creates a new {@link EncodingBoxCellRenderer} with the specified box's original renderer, to
     * whom we'll delegate the actual rendering.
     */
    public EncodingBoxCellRenderer(ListCellRenderer delegateRenderer) {
      this.delegateRenderer = delegateRenderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      boolean isDefaultCharset = (value == null);
      boolean isCharset = (value instanceof Charset) || isDefaultCharset;
      if (isCharset
          && !isDefaultCharset
          && (index != -1)) // -1 means drawing the box itself, not the popup
      value = "    " + value;

      if (!isCharset) isSelected = false;

      if (isDefaultCharset) {
        value =
            I18n.get(EncodingBoxCellRenderer.class)
                .getFormattedString(
                    "defaultEncoding.name", new Object[] {consoleManager.getEncoding().toString()});
      }

      Component renderer =
          delegateRenderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
      Font font = renderer.getFont();
      int bold = font.getStyle() | Font.BOLD;
      int nonBold = font.getStyle() & (~Font.BOLD);
      renderer.setFont(isCharset ? font.deriveFont(nonBold) : font.deriveFont(bold));

      return renderer;
    }
  }
}
