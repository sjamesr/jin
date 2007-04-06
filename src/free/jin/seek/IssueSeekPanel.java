/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.seek;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.SeekConnection;
import free.jin.UserSeek;
import free.jin.plugin.Plugin;
import free.util.NamedObject;
import free.util.TableLayout;
import free.util.swing.IntegerStrictPlainDocument;
import free.util.swing.SwingUtils;
import free.workarounds.FixedJComboBox;
import free.workarounds.FixedJTextField;



/**
 * A panel which lets the user select the seek options.
 */
 
public class IssueSeekPanel extends JPanel{
  
  
  
  /**
   * The plugin we're part of.
   */
  
  private final Plugin plugin;
  
  
  
  /**
   * Our preferences.
   */
  
  private final Preferences prefs;
  
  
  
  // The various UI elements.
  private final JTextField timeField;
  private final JTextField incField;
  private final JCheckBox isRatedBox;
  private final JComboBox variantChoice;
  private final JCheckBox isColorManualBox;
  private final JComboBox pieceColorChoice;
  private final JCheckBox limitRatingBox;
  private final JTextField minRatingField, maxRatingField;
  private final JCheckBox manualAcceptBox;
  private final JCheckBox useFormulaBox;
  
  private final JLabel errorLabel;
  private final JButton issueSeekButton;
  
  
  
  /**
   * Creates a new <code>IssueSeekPanel</code> for the specified plugin and with
   * the specified <code>Preferences</code> object to load settings from.
   */
   
  public IssueSeekPanel(Plugin plugin, Preferences prefs){
    if (plugin == null)
      throw new IllegalArgumentException("plugin may not be null");
    if (prefs == null)
      throw new IllegalArgumentException("prefs may not be null");
    if (!(plugin.getConn() instanceof SeekConnection))
      throw new IllegalArgumentException("Connection must be an instance of SeekConnection");
    
    this.plugin = plugin;
    this.prefs = prefs;
    
    I18n i18n = getI18n();
    
    WildVariant [] variants = plugin.getConn().getSupportedVariants();
    
    Object whitePiecesSelection = new NamedObject(Player.WHITE_PLAYER,
        i18n.getString("pieceColorChoice.white"));
    Object blackPiecesSelection = new NamedObject(Player.BLACK_PLAYER,
        i18n.getString("pieceColorChoice.black"));
    Object [] pieceColorSelections = 
      new Object[]{whitePiecesSelection, blackPiecesSelection};
    
    // Create ui elements
    timeField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 3);
    incField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 3);
    isRatedBox = i18n.createCheckBox("ratedBox");
    variantChoice = new FixedJComboBox(variants);
    variantChoice.setEditable(false);
    isColorManualBox = i18n.createCheckBox("manualColorBox");
    pieceColorChoice = new FixedJComboBox(pieceColorSelections);
    limitRatingBox = i18n.createCheckBox("limitRatingCheckBox");
    minRatingField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 4);
    maxRatingField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 4);
    manualAcceptBox = i18n.createCheckBox("manualAcceptCheckBox");
    useFormulaBox = i18n.createCheckBox("useFormulaCheckBox");
    
    errorLabel = new JLabel();
    errorLabel.setForeground(Color.red);
    errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD));
    
    issueSeekButton = i18n.createButton("issueSeekButton");
    
    
    String color = prefs.getString("color", "auto");
    int minRating = prefs.getInt("minRating", Integer.MIN_VALUE);
    int maxRating = prefs.getInt("maxRating", Integer.MAX_VALUE);
    
    // Set initial values of ui elements
    timeField.setText(String.valueOf(prefs.getInt("time", 10)));
    incField.setText(String.valueOf(prefs.getInt("inc", 0)));
    isRatedBox.setSelected(prefs.getBool("isRated", true));
    variantChoice.setSelectedIndex(findVariantIndex(variants, prefs.getString("variant", "Chess")));
    if ("auto".equals(color))
      isColorManualBox.setSelected(false);
    else if ("white".equals(color))
      pieceColorChoice.setSelectedItem(whitePiecesSelection);
    else if ("black".equals(color))
      pieceColorChoice.setSelectedItem(blackPiecesSelection);
    limitRatingBox.setSelected(prefs.getBool("limitRating", false));
    minRatingField.setText(minRating == Integer.MIN_VALUE ? "" : String.valueOf(minRating));
    maxRatingField.setText(minRating == Integer.MAX_VALUE ? "" : String.valueOf(maxRating));
    manualAcceptBox.setSelected(prefs.getBool("manualAccept", false));
    useFormulaBox.setSelected(prefs.getBool("useFormula", true));
    
    // Disable isRated for guests
    if (plugin.getUser().isGuest()){
      isRatedBox.setSelected(false);
      isRatedBox.setEnabled(false);
    }
    
    createUI();
  }
  
  
  
  
  
  
  
  /**
   * Returns the <code>I18n</code> for this class.
   */
  
  private I18n getI18n(){
    return I18n.get(getClass());
  }
  
  
  
  /**
   * Saves the panel's preferences.
   */
  
  public void saveSeekOptions(){
    // Do not save invalid data
    if (validateInput() != null)
      return;
    
    UserSeek seek = getSeek();
    String minRatingText = minRatingField.getText();
    String maxRatingText = maxRatingField.getText();
    
    prefs.setInt("time", seek.getTime());
    prefs.setInt("inc", seek.getInc());
    prefs.setBool("isRated", seek.isRated());
    prefs.setString("variant", seek.getVariant().getName());
    Player color = seek.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    prefs.setBool("limitRating", limitRatingBox.isSelected());
    prefs.setInt("minRating", "".equals(minRatingText) ? 
        Integer.MIN_VALUE : Integer.parseInt(minRatingText));
    prefs.setInt("maxRating", "".equals(maxRatingText) ? 
        Integer.MAX_VALUE : Integer.parseInt(maxRatingText));
    prefs.setBool("manualAccept", seek.isManualAccept());
    prefs.setBool("useFormula", seek.isFormula());
  }

  
  
  
  /**
   * Creates the ui of this panel, laying out all the ui elements.
   */
   
  private void createUI(){
    I18n i18n = getI18n();
    
    int xGap = 4; // The standard horizontal gap
    int yGap = 6; // The standard verical gap
    
    // Time controls
    JPanel timeControlsPanel = new JPanel(new TableLayout(3, xGap, yGap));
    
    JLabel timeLabel = i18n.createLabel("timeLabel");
    timeLabel.setLabelFor(timeField);
    timeLabel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    JLabel incLabel = i18n.createLabel("incrementLabel");
    incLabel.setLabelFor(incField);
    incLabel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    JLabel secondsLabel = i18n.createLabel("secondsLabel");
    secondsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    JLabel minutesLabel = i18n.createLabel("minutesLabel");
    minutesLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    timeField.setMaximumSize(timeField.getPreferredSize());
    incField.setMaximumSize(incField.getPreferredSize());
    
    // First row
    timeControlsPanel.add(timeLabel);
    timeControlsPanel.add(timeField);
    timeControlsPanel.add(minutesLabel);
    
    // Second row
    timeControlsPanel.add(incLabel);
    timeControlsPanel.add(incField);
    timeControlsPanel.add(secondsLabel);
    
    
    // Ratedness and variant
    JPanel gameTypePanel = new JPanel(new TableLayout(2, xGap, yGap));
    isRatedBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
    variantChoice.setAlignmentY(JComponent.TOP_ALIGNMENT);
    gameTypePanel.add(isRatedBox);
    gameTypePanel.add(variantChoice);
    
    
    // Color
    JPanel colorPanel = new JPanel(new TableLayout(2, xGap, yGap));
    isColorManualBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
    pieceColorChoice.setAlignmentY(JComponent.TOP_ALIGNMENT);
    colorPanel.add(isColorManualBox);
    colorPanel.add(pieceColorChoice);
    
    
    // Limit opponent rating
    Font normalFont = UIManager.getFont("Label.font");
    int minMaxLabelsFontSize = Math.max(9, normalFont.getSize() - 4);
    Font minMaxLabelsFont = 
      normalFont.deriveFont((float)minMaxLabelsFontSize);
    
    JLabel minLabel = i18n.createLabel("minRatingLabel");
    minLabel.setFont(minMaxLabelsFont);
    minLabel.setLabelFor(minRatingField);
    minLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    JLabel maxLabel = i18n.createLabel("maxRatingLabel");
    maxLabel.setLabelFor(maxRatingField);
    maxLabel.setFont(minMaxLabelsFont);
    maxLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    minRatingField.setMaximumSize(minRatingField.getPreferredSize());
    maxRatingField.setMaximumSize(minRatingField.getPreferredSize());

    
    JPanel oppRatingLimitPanel = new JPanel(new TableLayout(4, xGap, 1));
    oppRatingLimitPanel.add(limitRatingBox);
    oppRatingLimitPanel.add(minRatingField);
    oppRatingLimitPanel.add(new JLabel("-"));
    oppRatingLimitPanel.add(maxRatingField);
    oppRatingLimitPanel.add(new JPanel());
    oppRatingLimitPanel.add(minLabel);
    oppRatingLimitPanel.add(new JPanel());
    oppRatingLimitPanel.add(maxLabel);
    
    
    // Buttons panel
    JPanel buttonsPanel = new JPanel(new TableLayout(2, xGap, yGap));
    buttonsPanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    JButton moreLessButton = i18n.createButton("moreOptionsButton");
    moreLessButton.setDefaultCapable(false);
    moreLessButton.setActionCommand("more");
    
    buttonsPanel.add(moreLessButton);
    buttonsPanel.add(issueSeekButton);
    
    
    // Holds the panel displayed when "More Options" is clicked
    final JPanel advancedPanelHolder = new JPanel(new BorderLayout());
    
    
    // Layout the subcontainers in the main container
    setLayout(new TableLayout(1, xGap, yGap));
    setAlignmentX(JComponent.LEFT_ALIGNMENT);
    timeControlsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    advancedPanelHolder.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    buttonsPanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    add(timeControlsPanel);
    add(Box.createVerticalStrut(yGap/2));
    add(advancedPanelHolder);
    add(errorLabel);
    add(buttonsPanel);
    
    
    // Advanced options panel
    final JPanel advancedPanel = SwingUtils.createVerticalBox();
    
    gameTypePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    colorPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    oppRatingLimitPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    manualAcceptBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    useFormulaBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    advancedPanel.add(gameTypePanel);
    advancedPanel.add(Box.createVerticalStrut(yGap));
    advancedPanel.add(colorPanel);
    advancedPanel.add(Box.createVerticalStrut(yGap));
    advancedPanel.add(oppRatingLimitPanel);
    advancedPanel.add(Box.createVerticalStrut(yGap - minMaxLabelsFontSize - 1));
    advancedPanel.add(manualAcceptBox);
    advancedPanel.add(Box.createVerticalStrut(yGap));
    advancedPanel.add(useFormulaBox);
    
    
    DocumentListener validatingListener = new DocumentListener(){
      public void changedUpdate(DocumentEvent e){
        updateErrorState();
      }
      public void insertUpdate(DocumentEvent e){
        updateErrorState();
      }
      public void removeUpdate(DocumentEvent e){
        updateErrorState();
      }
    };
    
    timeField.getDocument().addDocumentListener(validatingListener);
    incField.getDocument().addDocumentListener(validatingListener);
    minRatingField.getDocument().addDocumentListener(validatingListener);
    maxRatingField.getDocument().addDocumentListener(validatingListener);
    
    limitRatingBox.getModel().addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e){
        updateErrorState();
      }
    });
    
    
    boolean isColorManual = isColorManualBox.isSelected();
    pieceColorChoice.setEnabled(isColorManual);
    isColorManualBox.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent evt){
        boolean isColorManual = isColorManualBox.isSelected();
        pieceColorChoice.setEnabled(isColorManual);
      }
    });
    
    
    boolean isRatingLimited = limitRatingBox.isSelected();
    minRatingField.setEnabled(isRatingLimited);
    maxRatingField.setEnabled(isRatingLimited);
    limitRatingBox.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent evt){
          boolean isRatingLimited = limitRatingBox.isSelected();
          minRatingField.setEnabled(isRatingLimited);
          maxRatingField.setEnabled(isRatingLimited);
      }
    });
    
    
    moreLessButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (evt.getActionCommand().equals("more")){
          JButton moreLessButton = (JButton)evt.getSource();
          moreLessButton.setText(getI18n().getString("lessOptionsButton.text"));
          moreLessButton.setActionCommand("less");
          advancedPanelHolder.add(advancedPanel, BorderLayout.CENTER);
          //IssueSeekPanel.this.resizeContainerToFit();
        }
        else{
          JButton moreLessButton = (JButton)evt.getSource();
          moreLessButton.setText(getI18n().getString("moreOptionsButton.text"));
          moreLessButton.setActionCommand("more");
          advancedPanelHolder.remove(advancedPanel);
          //IssueSeekPanel.this.resizeContainerToFit();
        }
      }
    });
    
    
    issueSeekButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        SeekConnection conn = (SeekConnection)plugin.getConn();
        conn.issue(getSeek());
      }
    });
  }
  
  
  
  /**
   * Updates the error state based on the current state of the input.
   */
  
  private void updateErrorState(){
    String errorMessage = validateInput();
    errorLabel.setText(errorMessage);
    issueSeekButton.setEnabled(errorMessage == null);
  }
  
  
  
  /**
   * Validates the current user input and returns an error description if it's
   * invalid. Returns <code>null</code> if the input is valid.
   */
  
  private String validateInput(){
    I18n i18n = I18n.get(IssueSeekPanel.class);
    
    try{
      Integer.parseInt(timeField.getText());
    } catch (NumberFormatException e){
        return i18n.getString("timeErrorMessage");
      }
    
    try{
      Integer.parseInt(incField.getText());
    } catch (NumberFormatException e){
        return i18n.getString("incErrorMessage");
      }
    
    if (limitRatingBox.isSelected()){
      String minRatingText = minRatingField.getText();
      String maxRatingText = maxRatingField.getText();
      int minRating, maxRating;
      
      try{
        minRating = "".equals(minRatingText) ? 
            Integer.MIN_VALUE : Integer.parseInt(minRatingText);
      } catch (NumberFormatException e){
          return i18n.getString("minRatingErrorMessage");
        }
      
      try{
        maxRating = "".equals(maxRatingText) ?
            Integer.MAX_VALUE : Integer.parseInt(maxRatingText);
      } catch (NumberFormatException e){
          return i18n.getString("maxRatingErrorMessage");
        }
      
      if (minRating > maxRating)
        return i18n.getString("ratingRangeError");
    }
    
    return null;
  }
  
  
  
  /**
   * Returns the currently specified <code>UserSeek</code>. Throws an
   * <code>IllegalStateException</code> if the currently specified information
   * does not translate to a legal <code>UserSeek</code>.
   */
  
  private UserSeek getSeek() throws IllegalStateException{
    String error = validateInput();
    if (error != null)
      throw new IllegalStateException(error);
    
    // Time
    int time = Integer.parseInt(timeField.getText());
    
    // Increment
    int inc = Integer.parseInt(incField.getText());
    
    // Ratedness
    boolean isRated = isRatedBox.isSelected();
    
    // Variant
    WildVariant variant = (WildVariant)variantChoice.getSelectedItem();
    
    // Color
    NamedObject pieceColorSelection = 
      (NamedObject)pieceColorChoice.getSelectedItem(); 
    Player color = (Player)pieceColorSelection.getTarget();
    if (!isColorManualBox.isSelected())
      color = null;
    
    // Opponent rating range
    int minRating, maxRating;
    if (limitRatingBox.isSelected()){
      String minRatingText = minRatingField.getText();
      minRating = "".equals(minRatingText) ?
        Integer.MIN_VALUE : Integer.parseInt(minRatingText);
      
      String maxRatingText = maxRatingField.getText();
      maxRating = "".equals(maxRatingText) ? 
        Integer.MAX_VALUE : Integer.parseInt(maxRatingText);
    }
    else{
      minRating = Integer.MIN_VALUE;
      maxRating = Integer.MAX_VALUE;
    }
    
    boolean manualAccept = manualAcceptBox.isSelected();
    boolean useFormula = useFormulaBox.isSelected();
    
    return new UserSeek(time, inc, isRated, variant, color, minRating,
      maxRating, manualAccept, useFormula);
  }
  
  
  
  /**
   * Returns the index of the variant with the specified name among the
   * specified list of variants. 
   */
   
  private int findVariantIndex(WildVariant [] variants, String name){
    for (int i = 0; i < variants.length; i++)
      if (variants[i].getName().equals(name))
        return i;
    
    return -1;
  }
  
  
  
}
