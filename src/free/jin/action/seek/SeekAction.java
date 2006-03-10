/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.action.seek;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.SeekConnection;
import free.jin.UserSeek;
import free.jin.action.ActionContext;
import free.jin.action.JinAction;
import free.jin.ui.DialogPanel;
import free.jin.ui.OptionPanel;
import free.util.AWTUtilities;
import free.util.TableLayout;
import free.util.swing.IntegerStrictPlainDocument;
import free.util.swing.SwingUtils;
import free.workarounds.FixedJComboBox;
import free.workarounds.FixedJTextField;


/**
 * An action which displays a panel where the user can issue a game seek.
 */
 
public class SeekAction extends JinAction{
  
  
  
  /**
   * Returns the id of the action - "seek".
   */
   
  public String getId(){
    return "seek";
  }
  
  
  
  /**
   * Returns the name of the action.
   */
   
  public String getName(){
    return getI18n().getString("actionName"); 
  }
  
  
  
  /**
   * Returns <code>true</code> if the connection to the server is an instance
   * of <code>SeekConnection</code>.
   */
   
  protected boolean isContextSupported(ActionContext context){
    return context.getConnection() instanceof SeekConnection;
  }
  
  
  
  /**
   * Displays a panel which lets the user specify the seek details and then
   * issues the seek.
   */
  
  public void go(){
    UserSeek seek = new SeekPanel().getSeek();
    
    if (seek != null){
      ((SeekConnection)getConn()).issueSeek(seek);
      saveSeekOptions(seek);
    }
  }
  
  
  
  /**
   * Saves the options of the specified seek to be used for the next time when
   * this action is run.
   */
  
  private void saveSeekOptions(UserSeek seek){
    Preferences prefs = getPrefs();
    
    prefs.setInt("time", seek.getTime());
    prefs.setInt("inc", seek.getInc());
    prefs.setBool("isRated", seek.isRated());
    prefs.setString("variant", seek.getVariant().getName());
    Player color = seek.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    int minRating = seek.getMinRating();
    int maxRating = seek.getMaxRating();
    prefs.setBool("limitRating", (minRating != Integer.MIN_VALUE) || (maxRating != Integer.MAX_VALUE));
    prefs.setInt("minRating", minRating == Integer.MIN_VALUE ? 0 : minRating);
    prefs.setInt("maxRating", maxRating == Integer.MAX_VALUE ? 9999 : maxRating);
    prefs.setBool("manualAccept", seek.isManualAccept());
    prefs.setBool("useFormula", seek.isFormula());
  }
  
  
  
  /**
   * A panel which lets the user select the seek options.
   */
   
  private class SeekPanel extends DialogPanel{
    
    
    
    // The various UI elements.
    private final JTextField timeField;
    private final JTextField incField;
    private final JCheckBox isRatedBox;
    private final JComboBox variantChoice;
    private final JRadioButton autoColor, whiteColor, blackColor;
    private final JCheckBox limitRatingBox;
    private final JTextField minRatingField, maxRatingField;
    private final JCheckBox manualAcceptBox;
    private final JCheckBox useFormulaBox;
    
    
    
    /**
     * Creates a new <code>SeekPanel</code>.
     */
     
    public SeekPanel(){
      Preferences prefs = getPrefs();
      
      WildVariant [] variants = getConn().getSupportedVariants();
      
      // Create ui elements
      timeField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 3);
      incField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 3);
      isRatedBox = new JCheckBox();
      variantChoice = new FixedJComboBox(variants);
      variantChoice.setEditable(false);
      autoColor = new JRadioButton();
      whiteColor = new JRadioButton();
      blackColor = new JRadioButton();
      limitRatingBox = new JCheckBox();
      minRatingField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 4);
      maxRatingField = new FixedJTextField(new IntegerStrictPlainDocument(0, 9999), "", 4);
      manualAcceptBox = new JCheckBox();
      useFormulaBox = new JCheckBox();
      
      
      ButtonGroup colorButtonGroup = new ButtonGroup();
      colorButtonGroup.add(autoColor);
      colorButtonGroup.add(whiteColor);
      colorButtonGroup.add(blackColor);
     
      
      String color = prefs.getString("color", "auto");
      
      // Set initial values of ui elements
      timeField.setText(String.valueOf(prefs.getInt("time", 10)));
      incField.setText(String.valueOf(prefs.getInt("inc", 0)));
      isRatedBox.setSelected(prefs.getBool("isRated", true));
      variantChoice.setSelectedIndex(findVariantIndex(variants, prefs.getString("variant", "Chess")));
      autoColor.setSelected("auto".equals(color));
      whiteColor.setSelected("white".equals(color));
      blackColor.setSelected("black".equals(color));
      limitRatingBox.setSelected(prefs.getBool("limitRating", false));
      minRatingField.setText(String.valueOf(prefs.getInt("minRating", 0)));
      maxRatingField.setText(String.valueOf(prefs.getInt("maxRating", 9999)));
      manualAcceptBox.setSelected(prefs.getBool("manualAccept", false));
      useFormulaBox.setSelected(prefs.getBool("useFormula", true));
      
      // Disable isRated for guests
      if (getUser().isGuest()){
        isRatedBox.setSelected(false);
        isRatedBox.setEnabled(false);
      }
      
      createUI();
    }
    
    
    
    /**
     * Creates the ui of this panel, laying out all the ui elements.
     */
     
    private void createUI(){
      I18n i18n = getI18n();
      
      final int labelPad = 4; // To align labels with checkboxes
      
      
      // Time controls
      JLabel timeLabel = new JLabel(i18n.getString("timeLabel"));
      timeLabel.setDisplayedMnemonicIndex(i18n.getInt("timeLabel.displayedMnemonicIndex"));
      timeLabel.setLabelFor(timeField);
      JLabel minutesLabel = new JLabel(i18n.getString("minutesLabel"));
      JLabel incLabel = new JLabel(i18n.getString("incrementLabel"));
      incLabel.setDisplayedMnemonicIndex(i18n.getInt("incrementLabel.displayedMnemonicIndex"));
      incLabel.setLabelFor(incField);
      JLabel secondsLabel = new JLabel(i18n.getString("secondsLabel"));
      timeField.setMaximumSize(timeField.getPreferredSize());
      incField.setMaximumSize(incField.getPreferredSize());
      
      JComponent timeContainer = new JPanel(new TableLayout(5, labelPad, 2));
      timeContainer.add(Box.createHorizontalStrut(0));
      timeContainer.add(timeLabel);
      timeContainer.add(Box.createHorizontalStrut(10));
      timeContainer.add(timeField);
      timeContainer.add(minutesLabel);
      timeContainer.add(Box.createHorizontalStrut(0));
      timeContainer.add(incLabel);
      timeContainer.add(Box.createHorizontalStrut(10));
      timeContainer.add(incField);
      timeContainer.add(secondsLabel);
      

      // Game ratedness
      isRatedBox.setText(i18n.getString("ratedBoxLabel"));
      isRatedBox.setDisplayedMnemonicIndex(i18n.getInt("ratedBoxLabel.displayedMnemonicIndex"));
      
      
      // Variant
      JLabel variantLabel = new JLabel(i18n.getString("variantLabel"));
      variantLabel.setDisplayedMnemonicIndex(i18n.getInt("variantLabel.displayedMnemonicIndex"));
      variantLabel.setLabelFor(variantChoice);
      variantChoice.setMaximumSize(variantChoice.getPreferredSize());
      
      JComponent variantContainer = SwingUtils.createHorizontalBox();
      variantContainer.add(Box.createHorizontalStrut(labelPad));
      variantContainer.add(variantLabel);
      variantContainer.add(Box.createHorizontalStrut(10));
      variantContainer.add(variantChoice);
      variantContainer.add(Box.createHorizontalGlue());
      

      // Color
      autoColor.setText(i18n.getString("autoColorChoice"));
      autoColor.setDisplayedMnemonicIndex(i18n.getInt("autoColorChoice.displayedMnemonicIndex"));
      whiteColor.setText(i18n.getString("whiteColorChoice"));
      whiteColor.setDisplayedMnemonicIndex(i18n.getInt("whiteColorChoice.displayedMnemonicIndex"));
      blackColor.setText(i18n.getString("blackColorChoice"));
      blackColor.setDisplayedMnemonicIndex(i18n.getInt("blackColorChoice.displayedMnemonicIndex"));
      
      JLabel colorLabel = new JLabel(i18n.getString("colorLabel"));
      
      JComponent colorContainer = SwingUtils.createHorizontalBox();
      colorContainer.add(Box.createHorizontalStrut(labelPad));
      colorContainer.add(colorLabel);
      colorContainer.add(Box.createHorizontalStrut(15));
      colorContainer.add(autoColor);
      colorContainer.add(Box.createHorizontalStrut(10));
      colorContainer.add(whiteColor);
      colorContainer.add(Box.createHorizontalStrut(10));
      colorContainer.add(blackColor);
      colorContainer.add(Box.createHorizontalGlue());
      
      
      // Limit opponent rating
      limitRatingBox.setText(i18n.getString("limitRatingLabel"));
      limitRatingBox.setDisplayedMnemonicIndex(i18n.getInt("limitRatingLabel.displayedMnemonicIndex"));
      
      JLabel minLabel = new JLabel(i18n.getString("minRatingLabel"));
      minLabel.setDisplayedMnemonicIndex(i18n.getInt("minRatingLabel.displayedMnemonicIndex"));
      minLabel.setLabelFor(minRatingField);
      JLabel maxLabel = new JLabel(i18n.getString("maxRatingLabel"));
      maxLabel.setDisplayedMnemonicIndex(i18n.getInt("maxRatingLabel.displayedMnemonicIndex"));
      maxLabel.setLabelFor(maxRatingField);
      minRatingField.setMaximumSize(minRatingField.getPreferredSize());
      maxRatingField.setMaximumSize(minRatingField.getPreferredSize());
      
      JComponent limitRatingBoxContainer = SwingUtils.createHorizontalBox();
      limitRatingBoxContainer.add(limitRatingBox);
      limitRatingBoxContainer.add(Box.createHorizontalGlue());
      
      final JComponent minMaxContainer = SwingUtils.createHorizontalBox();
      minMaxContainer.add(Box.createHorizontalStrut(40));
      minMaxContainer.add(minLabel);
      minMaxContainer.add(Box.createHorizontalStrut(10));
      minMaxContainer.add(minRatingField);
      minMaxContainer.add(Box.createHorizontalStrut(20));
      minMaxContainer.add(maxLabel);
      minMaxContainer.add(Box.createHorizontalStrut(10));
      minMaxContainer.add(maxRatingField);
      minMaxContainer.add(Box.createHorizontalGlue());
      
      
      JComponent limitRatingContainer = SwingUtils.createVerticalBox();
      limitRatingContainer.add(limitRatingBoxContainer);
      limitRatingContainer.add(Box.createVerticalStrut(3));
      limitRatingContainer.add(minMaxContainer);
      
      
      // Manual accept
      manualAcceptBox.setText(i18n.getString("manualAcceptLabel"));
      manualAcceptBox.setDisplayedMnemonicIndex(i18n.getInt("manualAcceptLabel.displayedMnemonicIndex"));
      
      
      // Use formula
      useFormulaBox.setText(i18n.getString("useFormulaLabel"));
      useFormulaBox.setMnemonic(i18n.getInt("useFormulaLabel.displayedMnemonicIndex"));
      

      // Buttons panel
      JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JButton issueSeekButton = new JButton(i18n.getString("issueSeekButtonText"));
      JButton cancelButton = new JButton(i18n.getString("cancelButtonText"));
      buttonsPanel.add(issueSeekButton);
      buttonsPanel.add(cancelButton);
      
      
      JButton moreLessButton = new JButton(i18n.getString("moreOptionsButtonText"));
      moreLessButton.setActionCommand("more");
      JPanel moreLessPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      moreLessPanel.add(moreLessButton);
      
      final JComponent advancedPanelHolder = new JPanel(new BorderLayout());
      
      
      
      // Layout the subcontainers in the main container
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      timeContainer.setAlignmentX(LEFT_ALIGNMENT);
      add(timeContainer);
      add(Box.createVerticalStrut(2));
      isRatedBox.setAlignmentX(LEFT_ALIGNMENT);
      add(isRatedBox);
      advancedPanelHolder.setAlignmentX(LEFT_ALIGNMENT);
      add(advancedPanelHolder);
      add(Box.createVerticalStrut(5));
      moreLessPanel.setAlignmentX(LEFT_ALIGNMENT);
      add(moreLessPanel);
      
      add(Box.createVerticalStrut(10));
      buttonsPanel.setAlignmentX(LEFT_ALIGNMENT);
      add(buttonsPanel);
      
      
      // Advanced options panel
      final JComponent advancedPanel = SwingUtils.createVerticalBox();
      advancedPanel.add(Box.createVerticalStrut(4));
      variantContainer.setAlignmentX(LEFT_ALIGNMENT);
      advancedPanel.add(variantContainer);
      advancedPanel.add(Box.createVerticalStrut(4));
      colorContainer.setAlignmentX(LEFT_ALIGNMENT);
      advancedPanel.add(colorContainer);
      advancedPanel.add(Box.createVerticalStrut(2));
      limitRatingContainer.setAlignmentX(LEFT_ALIGNMENT);
      advancedPanel.add(limitRatingContainer);
      advancedPanel.add(Box.createVerticalStrut(2));
      manualAcceptBox.setAlignmentX(LEFT_ALIGNMENT);
      advancedPanel.add(manualAcceptBox);
      advancedPanel.add(Box.createVerticalStrut(2));
      useFormulaBox.setAlignmentX(LEFT_ALIGNMENT);
      advancedPanel.add(useFormulaBox);
      
      
      AWTUtilities.setContainerEnabled(minMaxContainer, limitRatingBox.isSelected());
      limitRatingBox.addItemListener(new ItemListener(){
        public void itemStateChanged(ItemEvent evt){
          AWTUtilities.setContainerEnabled(minMaxContainer, limitRatingBox.isSelected());
        }
      });
      
      
      moreLessButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          if (evt.getActionCommand().equals("more")){
            JButton moreLessButton = (JButton)evt.getSource();
            moreLessButton.setText(getI18n().getString("lessOptionsButtonText"));
            moreLessButton.setActionCommand("less");
            advancedPanelHolder.add(advancedPanel, BorderLayout.CENTER);
            SeekPanel.this.resizeContainerToFit();
          }
          else{
            JButton moreLessButton = (JButton)evt.getSource();
            moreLessButton.setText(getI18n().getString("moreOptionsButtonText"));
            moreLessButton.setActionCommand("more");
            advancedPanelHolder.remove(advancedPanel);
            SeekPanel.this.resizeContainerToFit();
          }
        }
      });
      
      
      cancelButton.addActionListener(new ClosingListener(null));
      issueSeekButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          int time, inc;
          
          try{
            time = Integer.parseInt(timeField.getText());
          } catch (NumberFormatException e){
              OptionPanel.error(getI18n().getString("timeError.title"), getI18n().getString("timeError.message"));
              return;
            }
            
          try{
            inc = Integer.parseInt(incField.getText());
          } catch (NumberFormatException e){
              OptionPanel.error(getI18n().getString("incError.title"), getI18n().getString("incError.message"));
              return;
            }
            
          boolean isRated = isRatedBox.isSelected();
          
          WildVariant variant = (WildVariant)variantChoice.getSelectedItem();
          
          Player color = autoColor.isSelected() ? null :
            whiteColor.isSelected() ? Player.WHITE_PLAYER : Player.BLACK_PLAYER;
            
          int minRating, maxRating;
          if (limitRatingBox.isSelected()){
            try{
              minRating = Integer.parseInt(minRatingField.getText());
            } catch (NumberFormatException e){
                OptionPanel.error(getI18n().getString("minRatingError.title"), getI18n().getString("minRatingError.message"));
                return;
              }
            try{
              maxRating = Integer.parseInt(maxRatingField.getText());
            } catch (NumberFormatException e){
                OptionPanel.error(getI18n().getString("maxRatingError.title"), getI18n().getString("maxRatingError.message"));
                return;
              }
          }
          else{
            minRating = Integer.MIN_VALUE;
            maxRating = Integer.MAX_VALUE;
          }
          
          boolean manualAccept = manualAcceptBox.isSelected();
          boolean useFormula = useFormulaBox.isSelected();
          
          close(new UserSeek(time, inc, isRated, variant, color, minRating,
            maxRating, manualAccept, useFormula));
        }
      });
      
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
    
    

    /**
     * Returns the title of the panel.
     */
     
    public String getTitle(){
      return getI18n().getString("dialogTitle"); 
    }
    
    
    
    /**
     * Displays the panel to the user and returns a <code>UserSeek</code>
     * object with the selection options. Returns <code>null</code> if the user
     * canceled the dialog.
     */
     
    public UserSeek getSeek(){
      return (UserSeek)askResult();
    }
    


  }
  
  
   
}
