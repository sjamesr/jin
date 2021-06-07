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
package free.jin.scripter;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.ui.PreferencesPanel;
import free.util.AWTUtilities;
import free.util.Utilities;
import free.util.WindowDisposingListener;
import free.workarounds.FixedJTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** The preferences UI for the Scripter plugin. */
public class ScripterPreferencesPanel extends PreferencesPanel {

  /** The Scripter whose preferences we're displaying/modifying. */
  private final Scripter scripter;

  /** The <code>DefaultListModel</code> holding the scripts. */
  private final DefaultListModel scriptsListModel;

  /**
   * Creates a new <code>ScripterPreferencesPanel</code> to display/modify the preferences of the
   * specified <code>Scripter</code>.
   */
  public ScripterPreferencesPanel(Scripter scripter) {
    this.scripter = scripter;

    Script[] scripts = scripter.getScripts();
    scriptsListModel = new DefaultListModel();
    for (int i = 0; i < scripts.length; i++) scriptsListModel.addElement(scripts[i]);

    createUI();
  }

  /** Opens a dialog to allow the user to create a new script. */
  private void addScript() {
    try {
      ScriptTypeSelectionDialog scriptTypeChooser = new ScriptTypeSelectionDialog(this);
      AWTUtilities.centerWindow(scriptTypeChooser, this);
      String scriptType = scriptTypeChooser.askScriptType();
      if (scriptType == null) return;

      ScriptDialog dialog = scriptDialogForType(scriptType, null);
      if (dialog == null) throw new IllegalStateException("Unknown script type: " + scriptType);

      AWTUtilities.centerWindow(dialog, this);
      Script script = dialog.askForScript();
      if (script != null) {
        scriptsListModel.addElement(script);
        fireStateChanged();
      }
    } finally {
      SwingUtilities.windowForComponent(this).toFront();
      // Workaround for a fluxbox bug. See
      // http://sourceforge.net/tracker/index.php?func=detail&aid=803455&group_id=50386&atid=459537
    }
  }

  /** Opens a dialog to allow the user to edit the specified <code>Script</code>. */
  private void editScript(Script script) {
    try {
      String scriptType = script.getType();
      ScriptDialog dialog = scriptDialogForType(scriptType, script);
      if (dialog == null) throw new IllegalStateException("Unknown script type: " + scriptType);

      AWTUtilities.centerWindow(dialog, this);
      Script editedScript = dialog.askForScript();
      if (editedScript != null) {
        int index = scriptsListModel.indexOf(script);
        scriptsListModel.removeElementAt(index);
        scriptsListModel.insertElementAt(editedScript, index);
        fireStateChanged();
      }
    } finally {
      SwingUtilities.windowForComponent(this).toFront();
      // Workaround for a fluxbox bug. See
      // http://sourceforge.net/tracker/index.php?func=detail&aid=803455&group_id=50386&atid=459537
    }
  }

  /** Removes the specified <code>Script</code>. */
  private void deleteScript(Script script) {
    scriptsListModel.removeElement(script);
    fireStateChanged();
  }

  /** Returns an implementation of <code>ScriptDialog</code> for the specified script type. */
  private ScriptDialog scriptDialogForType(String scriptType, Script templateScript) {
    if ("commands".equals(scriptType))
      return new CommandsScriptDialog(this, scripter, (CommandScript) templateScript);
    else if ("beanshell".equals(scriptType))
      return new BeanShellScriptDialog(this, scripter, (BeanShellScript) templateScript);

    return null;
  }

  /** Creates the user interface of this preferences panel. */
  protected void createUI() {
    I18n i18n = I18n.get(ScripterPreferencesPanel.class);

    setLayout(new BorderLayout(10, 5));

    final JList list = new JList(scriptsListModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent evt) {
            if ((evt.getClickCount() == 2) && (evt.getModifiers() == InputEvent.BUTTON1_MASK)) {
              int selectedIndex = list.getSelectedIndex();
              if (selectedIndex != -1)
                editScript((Script) scriptsListModel.getElementAt(selectedIndex));
            }
          }
        });

    JScrollPane scrollPane =
        new JScrollPane(
            list,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(200, 70));

    JLabel listLabel = i18n.createLabel("scriptsLabel");
    listLabel.setLabelFor(listLabel);

    final JButton add = i18n.createButton("addScriptButton");
    final JButton edit = i18n.createButton("editScriptButton");
    final JButton remove = i18n.createButton("removeScriptButton");
    final JCheckBox enabled = i18n.createCheckBox("scriptEnabledCheckBox");

    edit.setEnabled(false);
    remove.setEnabled(false);
    enabled.setEnabled(false);

    list.addListSelectionListener(
        new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent evt) {
            if (evt.getValueIsAdjusting()) return;

            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex == -1) {
              edit.setEnabled(false);
              remove.setEnabled(false);
              enabled.setEnabled(false);
            } else {
              Script script = (Script) scriptsListModel.getElementAt(selectedIndex);
              edit.setEnabled(true);
              remove.setEnabled(true);
              enabled.setEnabled(true);
              enabled.setSelected(script.isEnabled());
            }
          }
        });

    add.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            addScript();
          }
        });

    edit.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            int selectedIndex = list.getSelectedIndex();
            editScript((Script) scriptsListModel.getElementAt(selectedIndex));
          }
        });

    remove.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            int selectedIndex = list.getSelectedIndex();
            deleteScript((Script) scriptsListModel.getElementAt(selectedIndex));

            if (selectedIndex < scriptsListModel.getSize()) list.setSelectedIndex(selectedIndex);
            else if (scriptsListModel.getSize() != 0)
              list.setSelectedIndex(scriptsListModel.getSize() - 1);
            else {
              edit.setEnabled(false);
              remove.setEnabled(false);
              enabled.setEnabled(false);
            }
          }
        });

    enabled.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            int selectedIndex = list.getSelectedIndex();
            Script script = (Script) scriptsListModel.getElementAt(selectedIndex);
            scriptsListModel.removeElementAt(selectedIndex);
            Script copy = script.createCopy();
            copy.setEnabled(enabled.isSelected());
            scriptsListModel.insertElementAt(copy, selectedIndex);
            list.setSelectedIndex(selectedIndex);
            fireStateChanged();
          }
        });

    add.setDefaultCapable(false);
    edit.setDefaultCapable(false);
    remove.setDefaultCapable(false);

    JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    buttonsPanel.add(add);
    buttonsPanel.add(edit);
    buttonsPanel.add(remove);
    buttonsPanel.setMaximumSize(buttonsPanel.getPreferredSize());

    Box buttonsWrapper = Box.createVerticalBox();
    buttonsWrapper.add(Box.createVerticalGlue());
    buttonsWrapper.add(buttonsPanel);
    buttonsWrapper.add(Box.createVerticalGlue());

    add(listLabel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(buttonsWrapper, BorderLayout.EAST);
    add(enabled, BorderLayout.SOUTH);
  }

  /** Applies the changes made by the user. */
  @Override
  public void applyChanges() throws BadChangesException {
    Script[] oldScripts = scripter.getScripts();
    Script[] newScripts = new Script[scriptsListModel.getSize()];
    scriptsListModel.copyInto(newScripts);

    for (int i = 0; i < oldScripts.length; i++) {
      Script script = oldScripts[i];
      int index = Utilities.indexOf(newScripts, script);
      if (index == -1) // This script was removed/edited
      scripter.removeScript(script);
      else newScripts[index] = null;
    }

    for (int i = 0; i < newScripts.length; i++) {
      Script script = newScripts[i];
      if (script != null) scripter.addScript(script);
    }
  }

  /** A dialog which queries the user about his choice of script type. */
  private class ScriptTypeSelectionDialog extends JDialog {

    /** The currently chosen script type. */
    private String scriptType;

    /** The listener that closes the dialog when the user cancels. */
    WindowDisposingListener closer =
        new WindowDisposingListener(this) {
          @Override
          public void beforeDisposing(EventObject evt) {
            scriptType = null;
          }
        };

    /** The constructor, duh. */
    public ScriptTypeSelectionDialog(Component parent) {
      super(AWTUtilities.frameForComponent(parent), "", true);

      setTitle(
          I18n.get(ScripterPreferencesPanel.class).getString("scriptTypeSelectionDialog.title"));

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

      KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      this.getRootPane()
          .registerKeyboardAction(closer, closeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

      addWindowListener(closer);

      createUI();
    }

    /** Creates the user interface for this dialog. */
    private void createUI() {
      I18n i18n = I18n.get(ScripterPreferencesPanel.class);

      JPanel content = new JPanel(new BorderLayout(10, 10));
      setContentPane(content);
      content.setBorder(new EmptyBorder(5, 5, 0, 5));

      JRadioButton commands = i18n.createRadioButton("serverCommandsScriptTypeRadioButton");
      JRadioButton beanshell = i18n.createRadioButton("beanshellScriptTypeRadioButton");

      commands.setActionCommand("commands");
      beanshell.setActionCommand("beanshell");

      final ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(commands);
      buttonGroup.add(beanshell);

      JPanel boxesPanel = new JPanel(new GridLayout(2, 1));
      boxesPanel.add(commands);
      boxesPanel.add(beanshell);

      final JTextArea typeExplanationTextArea =
          new FixedJTextArea() {

            @Override
            public boolean isFocusTraversable() {
              return false;
            }

            private Dimension prefSize = null;

            @Override
            public Dimension getPreferredSize() {
              if (prefSize == null) prefSize = calculatePreferredSize();

              return prefSize;
            }

            private Dimension calculatePreferredSize() {
              I18n i18n = I18n.get(ScripterPreferencesPanel.class);
              String[] expTexts =
                  new String[] {
                    i18n.getString("scriptExplanation.commands"),
                    i18n.getString("scriptExplanation.beanshell")
                  };
              FontMetrics metrics = this.getFontMetrics(this.getFont());
              int width = 0;
              int height = 0;
              for (int i = 0; i < expTexts.length; i++) {
                StringTokenizer tokenizer = new StringTokenizer(expTexts[i], "\n");
                if (tokenizer.countTokens() * metrics.getHeight() > height)
                  height = tokenizer.countTokens() * metrics.getHeight();
                while (tokenizer.hasMoreTokens()) {
                  String line = tokenizer.nextToken();
                  int lineWidth = metrics.stringWidth(line);
                  if (lineWidth > width) width = lineWidth;
                }
              }

              return new Dimension(width, height);
            }
          };
      typeExplanationTextArea.setEditable(false);
      typeExplanationTextArea.setOpaque(false);

      ActionListener selectionListener =
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              ButtonModel selectedModel = buttonGroup.getSelection();
              scriptType = selectedModel.getActionCommand();

              String text =
                  I18n.get(ScripterPreferencesPanel.class)
                      .getString("scriptExplanation." + scriptType);

              typeExplanationTextArea.setText(text);
            }
          };

      commands.addActionListener(selectionListener);
      beanshell.addActionListener(selectionListener);

      JButton next = i18n.createButton("scriptTypeSelectionDialog.nextButton");
      JButton cancel = i18n.createButton("scriptTypeSelectionDialog.cancelButton");

      JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonsPanel.add(next);
      buttonsPanel.add(cancel);

      next.addActionListener(new WindowDisposingListener(this));
      cancel.addActionListener(closer);

      content.add(boxesPanel, BorderLayout.NORTH);
      content.add(typeExplanationTextArea, BorderLayout.CENTER);
      content.add(buttonsPanel, BorderLayout.SOUTH);

      this.getRootPane().setDefaultButton(next);
    }

    /**
     * Displays the dialog, waits for the user to make his selection. Returns the selected script
     * type or <code>null</code> if none.
     */
    public String askScriptType() {
      this.setVisible(true);
      return scriptType;
    }
  }
}
