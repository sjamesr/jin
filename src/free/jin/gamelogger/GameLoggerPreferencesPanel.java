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

package free.jin.gamelogger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import bsh.EvalError;
import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.ui.OptionPanel;
import free.jin.ui.PreferencesPanel;
import free.util.swing.ExtensionFileFilter;
import free.workarounds.FixedJTextField;



/**
 * The preferences panel for the game logger.
 */

public class GameLoggerPreferencesPanel extends PreferencesPanel{



  /**
   * The GameLogger this panel shows preferences for.
   */

  protected final GameLogger gameLogger;




  /**
   * The radio button that specifies a LOG_NONE mode.
   */

  private JRadioButton logNoneButton;




  /**
   * The radio button that specifies a LOG_ALL mode.
   */

  private JRadioButton logAllButton;




  /**
   * The radio button that specifies a USE_RULES mode.
   */

  private JRadioButton useRulesButton;



  /**
   * The text field where the user can specify the name of the file to log all 
   * the games into.
   */

  private JTextField allGamesLogFileField;




  /**
   * The list holding all the logging rules.
   */

  private JList loggingRulesList;



  /**
   * The text field for the rule name.
   */

  private JTextField rulenameField;




  /**
   * The text field for the filename where the rule specific games will be
   * logged.
   */

  private JTextField filenameField;




  /**
   * The text field for the rule condition.
   */

  private JTextField conditionField;
  
  
  
  /**
   * The button for deleting rules.
   */
  
  private JButton deleteRuleButton;



  /**
   * The panel which holds the UI for the "log all" option.
   */
  
  private JPanel logAllPanel;
  
  
  
  /**
   * The panel which holds the UI for the "log via rules" option.
   */
  
  private JPanel useRulesPanel;
  
  
  
  /**
   * The panel which holds the UI for setting up a specific rule.
   */
  
  private JPanel rulePropertiesPanel;
  
  
  
  /**
   * This variable is set to <code>true</code> when the values of the rule
   * fields is being changed programmatically because a new rule is selected.
   * This is done so that the DocumentListener knows not to fire a ChangeEvent.
   */

  private boolean ignoreRuleFieldsDocumentChange = false;
  
  
  
  /**
   * This variable is set to <code>true</code> when the selection in the rules
   * list is being changed programmatically. This is done so that the corresponding
   * selection listened can act accordingly.
   */
  
  private boolean ignoreRulesListSelectionChange = false;



  /**
   * The currently selected index in the rules list. We need to keep this in
   * order to be able to roll back if the user tries to change the selection
   * when the current selection is incomplete.
   */

  private int rulesListSelectedIndex = -1;



  /**
   * A DocumentListener we register with all text components that require a
   * change event to be fired by this PreferencesPanel whenever their text
   * changes.
   */

  private final DocumentListener changeFiringDocumentListener = new DocumentListener(){
    public void changedUpdate(DocumentEvent e){if (!ignoreRuleFieldsDocumentChange) fireStateChanged();}
    public void insertUpdate(DocumentEvent e){if (!ignoreRuleFieldsDocumentChange) fireStateChanged();}
    public void removeUpdate(DocumentEvent e){if (!ignoreRuleFieldsDocumentChange) fireStateChanged();}
  };





  /**
   * Creates a new <code>GameLoggerPreferencesPanel</code> for the specified
   * GameLogger.
   */

  public GameLoggerPreferencesPanel(GameLogger gameLogger){
    this.gameLogger = gameLogger;

    createUI();
  }
  
  
  
  /**
   * Updates the UI according to the current user selections.
   */
  
  protected void syncUI(){
    try{
      ignoreRuleFieldsDocumentChange = true;

      if (logNoneButton.isSelected()){
        setContainerEnabled(logAllPanel, false);
        setContainerEnabled(useRulesPanel, false);
      }
      else if (logAllButton.isSelected()){
        setContainerEnabled(logAllPanel, true);
        setContainerEnabled(useRulesPanel, false);
      }
      else if (useRulesButton.isSelected()){
        setContainerEnabled(logAllPanel, false);
        setContainerEnabled(useRulesPanel, true);
      }
      
      if (loggingRulesList.getSelectedIndex() == -1){
        rulenameField.setText("");
        filenameField.setText("");
        conditionField.setText("");
        setContainerEnabled(rulePropertiesPanel, false);
        deleteRuleButton.setEnabled(false);
      }
      else{
        if (useRulesButton.isSelected()){
          setContainerEnabled(rulePropertiesPanel, true);
          deleteRuleButton.setEnabled(true);
        }

        Object selectedItem = loggingRulesList.getSelectedValue();
        if (selectedItem instanceof LoggingRule){ // When a new rule is created, this value is merely a String
          LoggingRule selectedRule = (LoggingRule)selectedItem;
          rulenameField.setText(selectedRule.getName());
          filenameField.setText(selectedRule.getFilename());
          conditionField.setText(selectedRule.getCondition());
        }
      }
    } finally{
      ignoreRuleFieldsDocumentChange = false;
    }
  }




  /**
   * Creates the user interface for this preferences panel.
   */

  protected void createUI(){
    I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
    
    int loggingMode = gameLogger.getLoggingMode();
    String allGamesLogFile = gameLogger.getLogFileForAll();
    Vector loggingRules = gameLogger.getLoggingRules();

    logNoneButton = i18n.createRadioButton("logNoGamesRadioButton");
    logAllButton = i18n.createRadioButton("logAllGamesRadioButton");
    useRulesButton = i18n.createRadioButton("ruleBasedGameLoggingRadioButton");

    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(logNoneButton);
    modeGroup.add(logAllButton);
    modeGroup.add(useRulesButton);

    logNoneButton.setActionCommand("none");
    logAllButton.setActionCommand("all");
    useRulesButton.setActionCommand("rules");
    
    logAllPanel = new JPanel();
    useRulesPanel = new JPanel();

    ActionListener loggingModeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        syncUI();
        fireStateChanged();
      }
    };

    switch(loggingMode){
      case GameLogger.LOG_NONE:  logNoneButton.setSelected(true); break;
      case GameLogger.LOG_ALL:   logAllButton.setSelected(true); break;
      case GameLogger.USE_RULES: useRulesButton.setSelected(true); break;
    }
    
    logNoneButton.addActionListener(loggingModeListener);
    logAllButton.addActionListener(loggingModeListener);
    useRulesButton.addActionListener(loggingModeListener);

    allGamesLogFileField = new FixedJTextField(10);
    allGamesLogFileField.setText(allGamesLogFile);
    allGamesLogFileField.getDocument().addDocumentListener(changeFiringDocumentListener);
    JButton browseAllGamesLogFileButton = i18n.createButton("browseLogFileButton");
    browseAllGamesLogFileButton.setDefaultCapable(false);
    browseAllGamesLogFileButton.addActionListener(new PGNFileSelectActionListener(this, allGamesLogFileField));

    logAllPanel.setLayout(new BorderLayout(5, 5));
    logAllPanel.add(allGamesLogFileField, BorderLayout.CENTER);
    logAllPanel.add(browseAllGamesLogFileButton, BorderLayout.EAST);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel logNoneHelpPanel = new JPanel(new BorderLayout(5, 5));
    logNoneHelpPanel.add(logNoneButton, BorderLayout.WEST);

    Box logAllHelpPanel = Box.createHorizontalBox();
    logAllHelpPanel.add(logAllButton);
    logAllHelpPanel.add(Box.createHorizontalStrut(10));
    logAllHelpPanel.add(logAllPanel);

    JPanel useRulesHelpPanel = new JPanel(new BorderLayout());
    useRulesHelpPanel.add(useRulesButton, BorderLayout.WEST);

    JPanel modeButtonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    modeButtonsPanel.add(logNoneHelpPanel);
    modeButtonsPanel.add(logAllHelpPanel);
    modeButtonsPanel.add(useRulesHelpPanel);


    final DefaultListModel rulesListModel = new DefaultListModel();
    for (int i = 0; i < loggingRules.size(); i++)
      rulesListModel.addElement(loggingRules.elementAt(i));
    loggingRulesList = new JList(rulesListModel);
    loggingRulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    loggingRulesList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        if (evt.getValueIsAdjusting())
          return;
        if (ignoreRulesListSelectionChange){
          rulesListSelectedIndex = loggingRulesList.getSelectedIndex();
          return;
        }
        if ((rulesListSelectedIndex != -1) && (rulesListSelectedIndex < rulesListModel.size())){
          try{
            updateRuleFromUI(rulesListSelectedIndex);
          } catch (BadChangesException e){
            ignoreRulesListSelectionChange = true;
              loggingRulesList.setSelectedIndex(rulesListSelectedIndex);
              ignoreRulesListSelectionChange = false;
              
              I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
              OptionPanel.error(i18n.getString("badChangesDialog.title"), e.getMessage(), GameLoggerPreferencesPanel.this);
              if (e.getErrorComponent() != null)
                e.getErrorComponent().requestFocus();
              return;
            }
        }
        rulesListSelectedIndex = loggingRulesList.getSelectedIndex();
        syncUI();
      }
    });

    JScrollPane loggingListRulesScrollPane = new JScrollPane(loggingRulesList); loggingListRulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    loggingListRulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    loggingListRulesScrollPane.setPreferredSize(new Dimension(80, 80));


    JLabel rulesLabel = i18n.createLabel("gameLoggingRulesLabel");
    rulesLabel.setLabelFor(loggingRulesList);
    rulesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JButton addRuleButton = i18n.createButton("addGameLoggingRuleButton");
    addRuleButton.setDefaultCapable(false);
    addRuleButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        int selectedIndex = loggingRulesList.getSelectedIndex();
        if (selectedIndex != -1){
          try{
            updateRuleFromUI(selectedIndex);
          } catch (BadChangesException e){
              I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
              OptionPanel.error(i18n.getString("badChangesDialog.title"), e.getMessage(), GameLoggerPreferencesPanel.this);
              if (e.getErrorComponent() != null)
                e.getErrorComponent().requestFocus();
              return;
            }
        }
        rulesListModel.addElement(I18n.get(GameLoggerPreferencesPanel.class).getString("initialNewGameLoggingRuleName"));
        loggingRulesList.setSelectedIndex(rulesListModel.size() - 1);
        loggingRulesList.ensureIndexIsVisible(loggingRulesList.getSelectedIndex());

        rulenameField.setText("");
        filenameField.setText("");
        conditionField.setText("");
        rulenameField.requestFocus();
      }
    });

    deleteRuleButton = i18n.createButton("deleteGameLoggingRuleButton");
    deleteRuleButton.setDefaultCapable(false);
    deleteRuleButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        int selectedIndex = loggingRulesList.getSelectedIndex();
        if (selectedIndex == -1)
          return;
        
        try{
          ignoreRulesListSelectionChange = true;
          rulesListModel.removeElementAt(selectedIndex);
          if (selectedIndex < rulesListModel.size())
            loggingRulesList.setSelectedIndex(selectedIndex);
          else if (rulesListModel.size() != 0)
            loggingRulesList.setSelectedIndex(selectedIndex - 1);
          loggingRulesList.ensureIndexIsVisible(loggingRulesList.getSelectedIndex());
        } finally{
          ignoreRulesListSelectionChange = false;
        }
        
        syncUI();  
        fireStateChanged();
      }
    });

    JPanel buttonHelpPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    buttonHelpPanel.add(addRuleButton);
    buttonHelpPanel.add(deleteRuleButton);
    buttonHelpPanel.setMaximumSize(buttonHelpPanel.getPreferredSize());

    Box buttonsBox = Box.createVerticalBox();
    buttonsBox.add(Box.createVerticalGlue());
    buttonsBox.add(buttonHelpPanel);
    buttonsBox.add(Box.createVerticalGlue());

    Box listAndButtonsPanel = Box.createHorizontalBox();
    listAndButtonsPanel.add(loggingListRulesScrollPane);
    listAndButtonsPanel.add(Box.createHorizontalStrut(10));
    listAndButtonsPanel.add(buttonsBox);
    listAndButtonsPanel.add(Box.createHorizontalGlue());

    JPanel rulesListPanel = new JPanel(new BorderLayout(5, 5));
    rulesListPanel.add(rulesLabel, BorderLayout.NORTH);
    rulesListPanel.add(listAndButtonsPanel, BorderLayout.CENTER);


    rulenameField = new FixedJTextField(10);
    filenameField = new FixedJTextField(10);
    conditionField = new FixedJTextField(20);

    rulenameField.getDocument().addDocumentListener(changeFiringDocumentListener);
    filenameField.getDocument().addDocumentListener(changeFiringDocumentListener);
    conditionField.getDocument().addDocumentListener(changeFiringDocumentListener);

    rulenameField.getDocument().addDocumentListener(new DocumentListener(){
      public void changedUpdate(DocumentEvent e){updateRulename();}
      public void insertUpdate(DocumentEvent e){updateRulename();}
      public void removeUpdate(DocumentEvent e){updateRulename();}

      private void updateRulename(){
        Object selectedItem = loggingRulesList.getSelectedValue();
        if (selectedItem instanceof String){
          String text = rulenameField.getText();
          if (text.length() == 0)
            rulesListModel.setElementAt(I18n.get(GameLoggerPreferencesPanel.class).getString("initialNewGameLoggingRuleName"), loggingRulesList.getSelectedIndex());
          else
            rulesListModel.setElementAt(text, loggingRulesList.getSelectedIndex());
        }
        else if (selectedItem instanceof LoggingRule){
          LoggingRule loggingRule = (LoggingRule)selectedItem;
          String text = rulenameField.getText();
          if ((text != null) && (text.length() != 0)){ // A temporary state when setting text?
            loggingRule.setName(text);
            loggingRulesList.repaint();
          }
        }
      }
    });

    JLabel rulenameLabel = i18n.createLabel("gameLoggingRuleNameLabel");
    JLabel filenameLabel = i18n.createLabel("gameLoggingFilenameLabel");
    JLabel conditionLabel = i18n.createLabel("gameLoggingConditionLabel");

    rulenameLabel.setLabelFor(rulenameField);
    filenameLabel.setLabelFor(filenameField);
    conditionLabel.setLabelFor(conditionField);

    JButton browseLogFileButton = i18n.createButton("browseLogFileButton");
    browseLogFileButton.setDefaultCapable(false);
    browseLogFileButton.addActionListener(new PGNFileSelectActionListener(this, filenameField));

    JButton helpConditionButton = i18n.createButton("conditionHelpButton");
    helpConditionButton.setDefaultCapable(false);
    helpConditionButton.setPreferredSize(browseLogFileButton.getPreferredSize());
    helpConditionButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
        i18n.showPlainTextDialog("conditionHelpDialog", GameLoggerPreferencesPanel.this);
      }
    });


    rulePropertiesPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    Box rulenameBox = Box.createHorizontalBox();
    Box filenameBox = Box.createHorizontalBox();
    Box conditionBox = Box.createHorizontalBox();

    int maxPrefWidth = Math.max(rulenameLabel.getPreferredSize().width,
      Math.max(filenameLabel.getPreferredSize().width, conditionLabel.getPreferredSize().width));

    rulenameLabel.setPreferredSize(new Dimension(maxPrefWidth + 7, 20));
    filenameLabel.setPreferredSize(new Dimension(maxPrefWidth + 7, 20));
    conditionLabel.setPreferredSize(new Dimension(maxPrefWidth + 7, 20));

    rulenameBox.add(rulenameLabel);
    rulenameBox.add(Box.createHorizontalStrut(5));
    rulenameBox.add(rulenameField);
    rulenameBox.add(Box.createHorizontalStrut(5+browseLogFileButton.getPreferredSize().width));


    filenameBox.add(filenameLabel);
    filenameBox.add(Box.createHorizontalStrut(5));
    filenameBox.add(filenameField);
    filenameBox.add(Box.createHorizontalStrut(5));
    filenameBox.add(browseLogFileButton);

    conditionBox.add(conditionLabel);
    conditionBox.add(Box.createHorizontalStrut(5));
    conditionBox.add(conditionField);
    conditionBox.add(Box.createHorizontalStrut(5));
    conditionBox.add(helpConditionButton);


    rulePropertiesPanel.add(rulenameBox);
    rulePropertiesPanel.add(filenameBox);
    rulePropertiesPanel.add(conditionBox);

    useRulesPanel.setBorder(new EmptyBorder(5, 16, 0, 0));
    useRulesPanel.setLayout(new BorderLayout(5, 20));
    useRulesPanel.add(rulesListPanel, BorderLayout.CENTER);
    useRulesPanel.add(rulePropertiesPanel, BorderLayout.SOUTH);
    
    add(modeButtonsPanel, BorderLayout.NORTH);
    add(useRulesPanel, BorderLayout.CENTER);
    
    int initialSelectedIndex = rulesListModel.getSize() - 1;
    loggingRulesList.setSelectedIndex(initialSelectedIndex);
    loggingRulesList.ensureIndexIsVisible(initialSelectedIndex);

    syncUI();
  }




  /**
   * Enables or disables all the components within the specified container.
   * This is a rather hacky method, which is very specific to this class, as
   * it's impossible to implement the desired functionality in a generic manner.
   */

  private static void setContainerEnabled(Container container, boolean enabled){
    Component [] children = container.getComponents();
    for (int i = 0; i < children.length; i++){
      Component child = children[i];
      
      if (child instanceof JTextComponent)
        ((JTextComponent)child).setEditable(enabled);
      child.setEnabled(enabled);
      
      if (child instanceof Container)
        setContainerEnabled((Container)child, enabled);
    }
  }




  /**
   * Updates the currently selected rule from the current rule properties in the
   * UI. Throws a <code>BadChangesException</code> if the properties entered by
   * the user were bad.
   */

  private void updateRuleFromUI(int ruleIndex) throws BadChangesException{
    I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
    
    DefaultListModel rulesModel = (DefaultListModel)loggingRulesList.getModel();
    Object item = rulesModel.getElementAt(ruleIndex);

    LoggingRule rule = (item instanceof LoggingRule) ? (LoggingRule)item : null;

    String rulename = rulenameField.getText();
    String filename = filenameField.getText();
    String ruleString = conditionField.getText();

    String errorMessage = null;
    Component errorComponent = null;

    if ((rulename == null) || (rulename.length() == 0)){
      errorMessage = i18n.getString("ruleNameUnspecifiedErrorMessage");
      errorComponent = rulenameField;
    }
    else if ((filename == null) || (filename.length() == 0)){
      errorMessage = i18n.getString("fileNameUnspecifiedErrorMessage");
      errorComponent = filenameField;
    }
    else if ((ruleString == null) || (ruleString.length() == 0)){
      ruleString = "true";
      conditionField.setText(ruleString);
    }
    else{
      errorMessage = checkLogFile(filename);
      if (errorMessage != null)
        errorComponent = filenameField;
    }

    if (errorMessage != null)
      throw new BadChangesException(errorMessage, errorComponent);

    try{
      if (rule == null){
        rule = new LoggingRule(rulename, ruleString, filename);
        rulesModel.setElementAt(rule, ruleIndex);
      }
      else{
        rule.setName(rulename);
        rule.setFilename(interpretLoggingTarget(filename));
        rule.setCondition(ruleString);
        loggingRulesList.repaint();        
      }
    } catch (EvalError e){
        throw new BadChangesException(i18n.getString("invalidGameLoggingConditionMessage"), conditionField);
      }
  }
  
  
  
  /**
   * Interprets the filename selected by the user by returning the filename
   * to be the actual logging target.
   */
  
  private static String interpretLoggingTarget(String filename){
    if (new File(filename).isAbsolute())
      return filename;
    else
      return new File(System.getProperty("user.home"), filename).getAbsolutePath();
  }
  
  
  
  /**
   * Checks whether the specified file is good as a target for logging games.
   * Returns <code>null</code> if so, otherwise, returns an error message. 
   */
  
  private String checkLogFile(String filename){
    I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
    
    File logFile = new File(interpretLoggingTarget(filename));
    File logDir = logFile.getParentFile();
    Object [] messageArgs = new Object[]{logFile.getPath(), logDir == null ? null : logDir.getPath()};
    if (logFile.isDirectory())
      return i18n.getFormattedString("logFileIsDirectoryErrorMessage", messageArgs);
    else if (logDir == null)
      return i18n.getFormattedString("badLogFileErrorMessage", messageArgs);
    else if (!logDir.exists()){
      Object result = 
        i18n.confirm(OptionPanel.OK, "createLogFileDirectoryDialog", this, messageArgs);
      if ((result == OptionPanel.OK) && !logDir.mkdirs())
        return i18n.getFormattedString("unableToCreateLogFileDirectory", messageArgs);
    }
    
    // Attempt to create the file.
    try{
      OutputStream out = new FileOutputStream(logFile, true);
      out.close();
    } catch (IOException e){
      return i18n.getFormattedString("unableToWriteLogFile", messageArgs);
    }
    
    return null;
  }


  /**
   * Applies the changed preferences done by the user.
   */

  public void applyChanges() throws BadChangesException{
    I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
    Preferences prefs = gameLogger.getPrefs();

    int selectedIndex = loggingRulesList.getSelectedIndex();
    if (selectedIndex != -1)
      updateRuleFromUI(selectedIndex);

    String loggingModeString;
    if (logNoneButton.isSelected())
      loggingModeString = "none";
    else if (logAllButton.isSelected())
      loggingModeString = "all";
    else if (useRulesButton.isSelected())
      loggingModeString = "rules";
    else
      throw new IllegalStateException("None of the mode radio buttons are selected");

    String allGamesLogFile = allGamesLogFileField.getText();
    if ("all".equals(loggingModeString)){
      String errMessage = null;
      
      if ((allGamesLogFile == null) || (allGamesLogFile.length() == 0))
        errMessage = i18n.getString("fileNameUnspecifiedErrorMessage");
      else
        errMessage = checkLogFile(allGamesLogFile);
      
      if (errMessage != null)
        throw new BadChangesException(errMessage, allGamesLogFileField);
    }
    

    prefs.setString("logging.mode", loggingModeString);

    prefs.setString("logging.all.filename", interpretLoggingTarget(allGamesLogFile));

    DefaultListModel loggingRulesModel = (DefaultListModel)loggingRulesList.getModel();
    int rulesCount = loggingRulesModel.size();
    prefs.setInt("logging.rules.count", rulesCount);
    for (int i = 0; i < rulesCount; i++){
      LoggingRule rule = (LoggingRule)loggingRulesModel.elementAt(i);
      prefs.setString("logging.rule-" + (i + 1) + ".name", rule.getName());
      prefs.setString("logging.rule-" + (i + 1) + ".condition", rule.getCondition());
      prefs.setString("logging.rule-" + (i + 1) + ".filename", interpretLoggingTarget(rule.getFilename()));
    }

    gameLogger.refreshFromProperties();
  }




  /**
   * A small ActionListener implementation which pops up a FileChooserDialog
   * when activated and lets the user choose a pgn file. It then sets the
   * selected filename to the specified JTextField.
   */

  private static class PGNFileSelectActionListener implements ActionListener{


    /**
     * The parent component.
     */

    private final Component parent;



    /**
     * The textfield we set the selected filename to.
     */

    private final JTextField textfield;



    /**
     * Creates a new PGNFileSelectActionListener with the specified parent
     * component and textfield.
     */

    public PGNFileSelectActionListener(Component parent, JTextField textfield){
      this.parent = parent;
      this.textfield = textfield;
    }




    public void actionPerformed(ActionEvent evt){
      I18n i18n = I18n.get(GameLoggerPreferencesPanel.class);
      
      File currentFile = new File(textfield.getText());
      File currentDir = null;
      if (currentFile.isFile()){
        currentDir = new File(currentFile.getParent());
        if (!currentDir.exists()){
          currentDir = null;
          currentFile = null;
        }
      }
      else if (currentFile.exists()){
        currentDir = currentFile;
        currentFile = null;
      }

      JFileChooser fileChooser = new JFileChooser(currentDir);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.addChoosableFileFilter(
        new ExtensionFileFilter(i18n.getString("pgnFileChooser.filterName"), ".pgn", false));
      fileChooser.setFileHidingEnabled(true);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int result = fileChooser.showDialog(parent, i18n.getString("pgnFileChooser.name"));
      if (result == JFileChooser.APPROVE_OPTION){
        String path = null;
        try{
          path = fileChooser.getSelectedFile().getCanonicalPath();
        } catch (IOException e){
            e.printStackTrace(); // Shouldn't happen
          }
        if (path == null)
          path = fileChooser.getSelectedFile().getAbsolutePath();

        if (!(path.endsWith(".pgn") || path.endsWith(".PGN")))
          path = path+".pgn";

        textfield.setText(path);
      }
    }

  }


}
