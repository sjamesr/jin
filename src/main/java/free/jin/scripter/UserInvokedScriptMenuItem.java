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

import bsh.EvalError;
import bsh.Interpreter;
import free.jin.ui.DialogPanel;
import free.jin.ui.OptionPanel;
import free.util.PlatformUtils;
import free.workarounds.FixedJComboBox;
import free.workarounds.FixedJTable;
import free.workarounds.FixedTableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/** The menu item we use for user invoked scripts. */
class UserInvokedScriptMenuItem extends JMenuItem implements ActionListener {

  /** The script. */
  private final Script script;

  /** The constructor, duh. */
  public UserInvokedScriptMenuItem(Script script) {
    super(script.getName());

    this.script = script;

    setMnemonic(script.getName().charAt(0));
    addActionListener(this);
  }

  /** Runs the script. */
  @Override
  public void actionPerformed(ActionEvent evt) {
    VariablesPanel varsPanel = new VariablesPanel();

    Object[][] vars = varsPanel.askVars();
    if (vars == null) return;

    script.run(null, null, vars);
  }

  /** Returns the script. */
  public Script getScript() {
    return script;
  }

  /** A panel which allows the user to specify variables to the script. */
  private class VariablesPanel extends DialogPanel {

    /** The available variable types. */
    private final String[] varTypes = new String[] {"String", "Integer", "Boolean", "Real"};

    /** The table model. */
    private final DefaultTableModel tableModel;

    /** The column model. */
    private final DefaultTableColumnModel columnModel;

    /** The "Run Script" button. We need this because we want to set the focus to it. */
    private JButton runScriptButton;

    /** The constructor. */
    public VariablesPanel() {
      columnModel = new DefaultTableColumnModel();
      JComboBox typeChoice = new FixedJComboBox(varTypes);
      typeChoice.setEditable(false);
      TableColumn typeColumn =
          new FixedTableColumn(
              0, 100, new DefaultTableCellRenderer(), new DefaultCellEditor(typeChoice));
      TableColumn nameColumn = new FixedTableColumn(1, 100);
      TableColumn valueColumn = new FixedTableColumn(2, 150);

      typeColumn.setHeaderValue("Type");
      nameColumn.setHeaderValue("Name");
      valueColumn.setHeaderValue("Value");

      columnModel.addColumn(typeColumn);
      columnModel.addColumn(nameColumn);
      columnModel.addColumn(valueColumn);

      tableModel = new DefaultTableModel(1, 3);
      tableModel.setValueAt(varTypes[0], 0, 0);

      createUI();
    }

    /** Returns the title of this dialog panel. */
    @Override
    public String getTitle() {
      return "Specify Variables for the Script";
    }

    /** Displays the dialog and returns the variables once the user is done. */
    public Object[][] askVars() {
      return (Object[][]) super.askResult();
    }

    /** Creates the UI for this dialog. */
    private void createUI() {
      final JTable table = new FixedJTable(tableModel, columnModel);
      table.setCellSelectionEnabled(false);
      table.setColumnSelectionAllowed(false);
      table.setRowSelectionAllowed(true);
      table.setRowHeight(20);
      table.getTableHeader().setPreferredSize(new Dimension(350, 18));
      table.getTableHeader().setReorderingAllowed(false);

      JScrollPane scrollPane = new JScrollPane(table);
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setPreferredSize(new Dimension(350, 120));

      JLabel variablesLabel = new JLabel("Variables");
      variablesLabel.setDisplayedMnemonic('V');
      variablesLabel.setLabelFor(table);

      JButton add = new JButton("Add Variable");
      final JButton remove = new JButton("Remove Variable");

      add.setMnemonic('A');
      remove.setMnemonic('R');

      add.setDefaultCapable(false);
      remove.setDefaultCapable(false);

      remove.setEnabled(false);

      runScriptButton = new JButton("Run Script");
      JButton cancelButton = new JButton("Cancel");

      ListSelectionModel selectionModel = table.getSelectionModel();
      selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      selectionModel.addListSelectionListener(
          new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
              if (evt.getValueIsAdjusting()) return;

              int selectedRow = table.getSelectedRow();
              if (selectedRow == -1) remove.setEnabled(false);
              else remove.setEnabled(true);
            }
          });

      add.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              tableModel.addRow(new Object[] {varTypes[0], null, null});
            }
          });

      remove.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              int selectedRow = table.getSelectedRow();
              if (selectedRow == -1)
                throw new IllegalStateException("Remove clicked while no row is selected");

              tableModel.removeRow(selectedRow);
              if (tableModel.getRowCount() <= selectedRow) {
                if (tableModel.getRowCount() == 0) remove.setEnabled(false);
                table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
              } else table.setRowSelectionInterval(selectedRow, selectedRow);
            }
          });

      runScriptButton.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              if (table.isEditing()) {
                // Makes sure that the editing stops so that table.getValueAt() returns up-to-date
                // values.
                TableCellEditor editor = table.getCellEditor();
                editor.stopCellEditing();
              }

              int rowCount = tableModel.getRowCount();
              Vector varsVector = new Vector(rowCount);
              for (int i = 0; i < rowCount; i++) {
                String type = (String) tableModel.getValueAt(i, 0);
                String name = (String) tableModel.getValueAt(i, 1);
                String valueString = (String) tableModel.getValueAt(i, 2);
                if ((name == null) || (name.length() == 0)) continue;

                Object value;
                try {
                  if ("Integer".equals(type)) value = new Integer(valueString);
                  else if ("String".equals(type)) value = valueString;
                  else if ("Boolean".equals(type)) value = Boolean.valueOf(valueString);
                  else if ("Real".equals(type)) value = new Double(valueString);
                  else throw new IllegalStateException("Unknown variable type: " + type);
                } catch (IllegalArgumentException e) {
                  OptionPanel.error(
                      "Bad Variable Value",
                      "Inappropriate value specified for variable \""
                          + name
                          + "\", must be of type \""
                          + type
                          + "\"");
                  return;
                }
                varsVector.addElement(new Object[] {name, value});
              }

              Object[][] vars = new Object[varsVector.size()][];
              varsVector.copyInto(vars);

              close(vars);
            }
          });

      cancelButton.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              close(null);
            }
          });

      JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
      buttonsPanel.add(add);
      buttonsPanel.add(remove);
      buttonsPanel.setMaximumSize(buttonsPanel.getPreferredSize());

      Box buttonsWrapper = Box.createVerticalBox();
      buttonsWrapper.add(Box.createVerticalGlue());
      buttonsWrapper.add(buttonsPanel);
      buttonsWrapper.add(Box.createVerticalGlue());

      JPanel finishButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      finishButtonsPanel.add(runScriptButton);
      finishButtonsPanel.add(cancelButton);

      this.setLayout(new BorderLayout(10, 5));
      this.setBorder(new EmptyBorder(10, 10, 5, 10));
      this.add(variablesLabel, BorderLayout.NORTH);
      this.add(scrollPane, BorderLayout.CENTER);
      this.add(buttonsWrapper, BorderLayout.EAST);
      this.add(finishButtonsPanel, BorderLayout.SOUTH);

      setDefaultButton(runScriptButton);

      // So that the default button is activated on ENTER and the dialog closes on ESCAPE
      if (PlatformUtils.isJavaBetterThan("1.3")) {
        Action defaultButtonAction =
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent evt) {
                runScriptButton.doClick();
              }
            };
        Action closeDialogAction =
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent evt) {
                close(null);
              }
            };
        try {
          Interpreter bsh = new Interpreter();
          bsh.set("table", table);
          bsh.set("defaultButtonAction", defaultButtonAction);
          bsh.set("closeDialogAction", closeDialogAction);
          bsh.eval("ActionMap actionMap = table.getActionMap();");
          bsh.eval("InputMap inputMap = table.getInputMap();");
          bsh.eval("actionMap.put(\"defaultButtonAction\", defaultButtonAction);");
          bsh.eval(
              "inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), \"defaultButtonAction\");");
          bsh.eval("actionMap.put(\"closeDialogAction\", closeDialogAction);");
          bsh.eval(
              "inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), \"closeDialogAction\");");
        } catch (EvalError e) {
          e.printStackTrace();
        }
      } else {
        table.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        table.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
      }
    }
  }
}
