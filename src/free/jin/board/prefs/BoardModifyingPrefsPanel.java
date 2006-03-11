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

package free.jin.board.prefs;

import java.awt.Color;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import free.jin.I18n;
import free.jin.Jin;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.jin.ui.PreferencesPanel;
import free.util.swing.ColorChooser;


/**
 * A common superclass for preference panels which modify the board. 
 */
 
public abstract class BoardModifyingPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The board manager.
   */
   
  protected final BoardManager boardManager;
  
  
  
  /**
   * The preview board.
   */
   
  protected final JinBoard previewBoard;
  
  
  
  /**
   * The <code>I18n</code> for this preferences panel.
   */
  
  private final I18n i18n;
  
  
  
  /**
   * Creates a new BoardModifyingPrefsPanel for the specified
   * <code>BoardManager</code> and with the specified preview board.
   */
   
  public BoardModifyingPrefsPanel(BoardManager boardManager, JinBoard previewBoard){
    this.boardManager = boardManager;
    this.previewBoard = previewBoard;
    this.i18n = I18n.getInstance(getClass(), Jin.getInstance().getLocale());
  }
  
  
  
  /**
   * Sets the initial properties of the preview board. This method is intended
   * to be overriden by subclasses to apply the initial properties of the board
   * that the particular subclass is responsible for.
   */
   
  public abstract void initPreviewBoard();
  
  
  
  /**
   * Returns the <code>I18n</code> for this preferences panel.
   */
  
  public I18n getI18n(){
    return i18n;
  }
  
  
  
  /**
   * A helper method which creates a <code>JRadioButton</code> from the
   * specified i18n key and with the specified initial state.
   */
  
  protected JRadioButton createRadioButton(String i18nKey, boolean initialState){
    return (JRadioButton)init(new JRadioButton(), i18nKey, initialState);
  }

  
  
  /**
   * A helper method which creates a <code>JCheckBox</code> from the
   * specified i18n key and with the specified initial state.
   */
  
  protected JCheckBox createCheckBox(String i18nKey, boolean initialState){
    return (JCheckBox)init(new JCheckBox(), i18nKey, initialState);
  }
  
  
  
  /**
   * A helper method which creates a <code>ColorChooser</code> from the
   * specified i18n key and with the specified initial value. 
   */
  
  protected ColorChooser createColorChooser(String i18nKey, Color initialValue){
    I18n i18n = getI18n();
    
    ColorChooser colorChooser = new ColorChooser(i18n.getString(i18nKey + ".text"), initialValue);
    colorChooser.setDisplayedMnemonicIndex(i18n.getInt(i18nKey + ".displayedMnemonicIndex"));
    colorChooser.setToolTipText(i18n.getString(i18nKey + ".tooltip"));
    
    return colorChooser;
  }

  
  
  /**
   * Initializes the specified <code>AbstractButton</code> from the specified
   * i18n key and with the specified initial state.
   */
  
  private AbstractButton init(AbstractButton button, String i18nKey, boolean initialState){
    I18n i18n = getI18n();
    
    button.setText(i18n.getString(i18nKey + ".text"));
    button.setDisplayedMnemonicIndex(i18n.getInt(i18nKey + ".displayedMnemonicIndex"));
    button.setToolTipText(i18n.getString(i18nKey + ".tooltip"));
    button.setSelected(initialState);
    
    return button;
  }
  
  
  
}