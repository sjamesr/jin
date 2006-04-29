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

package free.jin.scripter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import free.jin.I18n;
import free.util.AWTUtilities;
import free.util.NamedObject;
import free.util.TableLayout;
import free.util.Utilities;
import free.util.swing.SwingUtils;
import free.workarounds.FixedJComboBox;
import free.workarounds.FixedJTextField;



/**
 * The superclass for dialogs allowing the user to create a <code>Script</code>.
 */

abstract class ScriptDialog extends JDialog{
  
  
  
  /**
   * The default event type we show in the event type choice.
   */

  private static final String SELECT_EVENT_TYPE = "selectEventType";



  /**
   * The scripter we're creating this <code>Script</code> for.
   */

  protected final Scripter scripter;



  /**
   * The template script.
   */

  protected final Script templateScript;



  /**
   * The resulting script.
   */

  private Script script = null;




  /**
   * Creates a new ScriptDialog with the specified parent component and title.
   */

  public ScriptDialog(Component parent, String title, Scripter scripter, Script templateScript){
    super(AWTUtilities.frameForComponent(parent), title, true);

    if (scripter == null)
      throw new IllegalArgumentException("The specified scripter is null");
    
    this.templateScript = templateScript;
    this.scripter = scripter;

    SwingUtils.registerEscapeCloser(this);
  }



  /**
   * Creates the script type specific UI and returns a <code>Container</code>
   * containing it.
   */

  protected abstract Container createScriptTypeSpecificUI();



  /**
   * This method is called when the user clicks the OK button. It is supposed
   * to collect the script type specific data from the UI and return a script.
   * If the user specified information is invalid, it should handle the problem
   * in some manner (displaying an error message to the user, for example) and
   * return null.
   */

  protected abstract Script createScriptOnOk(String scriptName, String eventType, String [] selectedSubtypes);



  /**
   * Creates the UI.
   */

  protected final void createUI(){
    I18n scripterI18n = scripter.getI18n();
    I18n i18n = I18n.get(ScriptDialog.class);
    
    String defaultScriptName = (templateScript == null ? "" : templateScript.getName());
    String defaultEventType = (templateScript == null ? SELECT_EVENT_TYPE : templateScript.getEventType());
    String [] defaultSelectedSubtypes = 
      (templateScript == null ? new String[0] : templateScript.getEventSubtypes());

    String [] eventTypes = scripter.getSupportedEventTypes();
    NamedObject [] eventTypeChoiceEntries = new NamedObject[eventTypes.length];
    for (int i = 0; i < eventTypes.length; i++)
      eventTypeChoiceEntries[i] = 
        new NamedObject(eventTypes[i], scripterI18n.getString("eventTypeNames." + eventTypes[i]));
    
    final JTextField scriptNameField = new FixedJTextField(defaultScriptName);

    final JComboBox eventTypeChoice = new FixedJComboBox(eventTypeChoiceEntries);
    eventTypeChoice.setEditable(false);
    eventTypeChoice.addItem(new NamedObject(SELECT_EVENT_TYPE, i18n.getString("selectEventTypeString")));
    
    for (int i = 0; i < eventTypeChoice.getItemCount(); i++){
      NamedObject item = (NamedObject)eventTypeChoice.getItemAt(i);
      if (item.getTarget().equals(defaultEventType)){
        eventTypeChoice.setSelectedIndex(i);
        break;
      }
    }

    final JPanel subtypesPanel = new JPanel(new TableLayout(2));
    JPanel subtypesHolderPanel = new JPanel(new BorderLayout());
    subtypesHolderPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
    subtypesHolderPanel.add(subtypesPanel, BorderLayout.NORTH);
    JScrollPane subtypesScrollPane = new JScrollPane(subtypesHolderPanel);
    subtypesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    subtypesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    subtypesScrollPane.setPreferredSize(new Dimension(300, 115));
    
    if (templateScript != null)
      updateSubtypesPanel(subtypesPanel, defaultEventType, defaultSelectedSubtypes);

    eventTypeChoice.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        NamedObject selectedItem = (NamedObject)eventTypeChoice.getSelectedItem();
        String eventType = (String)selectedItem.getTarget();
        String [] selectedSubtypes = (templateScript == null ? new String[0] : templateScript.getEventSubtypes());
        updateSubtypesPanel(subtypesPanel, eventType, selectedSubtypes);
      }
    });
    
    JLabel scriptNameLabel = i18n.createLabel("scriptNameLabel");
    JLabel eventTypeLabel = i18n.createLabel("eventTypeLabel");
    JLabel eventSubtypesLabel = i18n.createLabel("eventSubtypesLabel");

    scriptNameLabel.setLabelFor(scriptNameField);
    eventTypeLabel.setLabelFor(eventTypeChoice);
    eventSubtypesLabel.setLabelFor(subtypesPanel);

    eventSubtypesLabel.setAlignmentY(Component.TOP_ALIGNMENT);

    JButton eventTypeHelp = i18n.createButton("eventTypeHelpButton");
    JButton eventSubtypesHelp = i18n.createButton("eventSubtypesHelpButton");

    eventTypeHelp.setDefaultCapable(false);
    eventSubtypesHelp.setDefaultCapable(false);

    eventTypeHelp.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        I18n i18n = I18n.get(ScriptDialog.class);
        i18n.showPlainTextDialog("eventTypeHelpDialog", ScriptDialog.this);
      }
    });

    eventSubtypesHelp.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        I18n i18n = I18n.get(ScriptDialog.class);
        i18n.showPlainTextDialog("eventSubtypesHelpDialog", ScriptDialog.this);
      }
    });


    JPanel genericDataPanel = new JPanel(new TableLayout(3, 7, 5));

    genericDataPanel.add(scriptNameLabel);
    genericDataPanel.add(scriptNameField);
    genericDataPanel.add(Box.createVerticalStrut(eventTypeHelp.getPreferredSize().height));

    genericDataPanel.add(eventTypeLabel);
    genericDataPanel.add(eventTypeChoice);
    genericDataPanel.add(eventTypeHelp);

    genericDataPanel.add(eventSubtypesLabel);
    genericDataPanel.add(subtypesScrollPane);
    genericDataPanel.add(eventSubtypesHelp);

    Container scriptTypeSpecificDataPanel = createScriptTypeSpecificUI();

    JPanel dataPanel = new JPanel(null);
    dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
    dataPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

    dataPanel.add(genericDataPanel);
    dataPanel.add(Box.createVerticalStrut(10));
    dataPanel.add(scriptTypeSpecificDataPanel);

    JButton okButton = i18n.createButton("scriptDialogOkButton");
    JButton cancelButton = i18n.createButton("scriptDialogCancelButton");

    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String scriptName = scriptNameField.getText();
        NamedObject eventTypeSelectedItem = (NamedObject)eventTypeChoice.getSelectedItem();
        String eventType = (String)eventTypeSelectedItem.getTarget();

        Vector selectedSubtypesVector = new Vector();
        Component [] subtypeCheckboxes = subtypesPanel.getComponents();
        for (int i = 0; i < subtypeCheckboxes.length; i++){
          Component component = subtypeCheckboxes[i];
          if (component instanceof JCheckBox){
            JCheckBox checkbox = (JCheckBox)component;
            if (checkbox.isSelected())
              selectedSubtypesVector.addElement(checkbox.getActionCommand());
          }
        }
        String [] selectedSubtypes = new String[selectedSubtypesVector.size()];
        selectedSubtypesVector.copyInto(selectedSubtypes);

        if ((scriptName == null) || (scriptName.length() == 0)){
          I18n.get(ScriptDialog.class).error("missingScriptNameDialog", ScriptDialog.this);
          return;
        }

        if (SELECT_EVENT_TYPE.equals(eventType)){
          I18n.get(ScriptDialog.class).error("missingEventTypeDialog", ScriptDialog.this);
          return;
        }

        if ((selectedSubtypes.length == 0) && (scripter.getEventSubtypes(eventType) != null)){
          I18n.get(ScriptDialog.class).error("missingEventSubtypeDialog", ScriptDialog.this);
          return;
        }

        script = createScriptOnOk(scriptName, eventType, selectedSubtypes);
        if (script != null)
          dispose();
      }
    });

    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        script = null;
        dispose();
      }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    JPanel contentPane = new JPanel(new BorderLayout(5, 10));
    contentPane.add(dataPanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    setContentPane(contentPane);

    getRootPane().setDefaultButton(okButton);
  }



  /**
   * Puts the appropriate checkboxes in the subtypes panel.
   */

  private void updateSubtypesPanel(JPanel panel, String eventType, String [] selectedSubtypes){
    panel.removeAll();

    if (!SELECT_EVENT_TYPE.equals(eventType)){
      String [] subtypes = scripter.getEventSubtypes(eventType);
      if (subtypes != null){
        I18n scripterI18n = scripter.getI18n();
        for (int i = 0; i < subtypes.length; i++){
          String subtype = subtypes[i];
          JCheckBox checkbox = new JCheckBox(scripterI18n.getString("eventSubtypeNames." + eventType + "." + subtype));
          checkbox.setActionCommand(subtype);
          checkbox.setSelected(Utilities.contains(selectedSubtypes, subtype));
          panel.add(checkbox);
        }
      }
    }

    panel.revalidate();
  }



  /**
   * Displays the dialog until the user finishes creating the script and then
   * returns the resulting <code>Script</code>.
   */

  public Script askForScript(){
    setVisible(true);

    return script;
  }
  
  
  
}
