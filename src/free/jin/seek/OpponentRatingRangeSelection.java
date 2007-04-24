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

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.jin.I18n;
import free.util.AWTUtilities;
import free.util.TableLayout;
import free.util.swing.PreferredSizedPanel;
import free.util.swing.WrapperComponent;



/**
 * A UI element which allows the user to limit the range of the rating of his
 * opponent(s). 
 */

public final class OpponentRatingRangeSelection extends WrapperComponent{
  
  
  
  /**
   * The checkbox specifying whether the range is limited.
   */
  
  private final JCheckBox isLimited;
  
  
  
  /**
   * The minimum rating model.
   */
  
  private final SpinnerNumberModel minimum;
  
  
  
  /**
   * The maximum rating model.
   */
  
  private final SpinnerNumberModel maximum;
  
  
  
  /**
   * Creates a new <code>OpponentRatingRangeSelection</code> with the specified
   * initial state.
   * 
   * @isInitiallyLimited Whether the range is initially limited.
   * @initialMinimum The initial minimum rating value.
   * @initialMaximum The initial maximum rating value.
   */
  
  public OpponentRatingRangeSelection(boolean isInitiallyLimited, int initialMinimum, int initialMaximum){
    I18n i18n = I18n.get(OpponentRatingRangeSelection.class);
    
    isLimited = i18n.createCheckBox("isLimitedBox"); 
    minimum = new SpinnerNumberModel(initialMinimum, 0, 9999, 50);
    maximum = new SpinnerNumberModel(initialMaximum, 0, 9999, 50);
    
    isLimited.setSelected(isInitiallyLimited);
    
    minimum.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        if (minimum.getNumber().intValue() > maximum.getNumber().intValue())
          maximum.setValue(minimum.getNumber());
      }
    });
    
    maximum.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        if (minimum.getNumber().intValue() > maximum.getNumber().intValue())
          minimum.setValue(maximum.getNumber());
      }
    });
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this component.
   */
  
  private void createUI(){
    I18n i18n = I18n.get(OpponentRatingRangeSelection.class);
    
    JSpinner minSpinner = new JSpinner(minimum);
    JSpinner maxSpinner = new JSpinner(maximum);
    
    Font normalFont = UIManager.getFont("Label.font");
    int minMaxLabelsFontSize = Math.max(9, normalFont.getSize() - 4);
    Font minMaxLabelsFont = normalFont.deriveFont((float)minMaxLabelsFontSize);
    
    JLabel minLabel = i18n.createLabel("minLabel");
    minLabel.setFont(minMaxLabelsFont);
    minLabel.setLabelFor(minSpinner);
    minLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    JLabel maxLabel = i18n.createLabel("maxLabel");
    maxLabel.setFont(minMaxLabelsFont);
    maxLabel.setLabelFor(maxSpinner);
    maxLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    final JPanel rangePanel = new JPanel(new TableLayout(3, 4, 1));
    rangePanel.add(minSpinner);
    rangePanel.add(new JLabel("-"));
    rangePanel.add(maxSpinner);
    rangePanel.add(minLabel);
    rangePanel.add(new JPanel());
    rangePanel.add(maxLabel);
    
    AWTUtilities.setContainerEnabled(rangePanel, isLimited.isSelected());
    isLimited.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent evt){
        AWTUtilities.setContainerEnabled(rangePanel, isLimited.isSelected());
      }
    });
    
    rangePanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
    
    JPanel content = new PreferredSizedPanel(new TableLayout(1, 4, 6));
    content.add(isLimited);
    content.add(rangePanel);

    add(content);
  }
  
  
  
  /**
   * Returns whether the rating range is limited.
   */
  
  public boolean isLimited(){
    return isLimited.isSelected();
  }
  
  
  
  /**
   * Returns the lower bound of the range. 
   */
  
  public int getMinimum(){
    return minimum.getNumber().intValue();
  }
  
  
  
  /**
   * Returns the upper bound of the range.
   */
  
  public int getMaximum(){
    return maximum.getNumber().intValue();
  }
  
  
  
}
