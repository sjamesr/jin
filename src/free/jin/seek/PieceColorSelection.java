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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import free.chess.Player;
import free.jin.I18n;
import free.util.NamedWrapper;
import free.util.TableLayout;
import free.util.swing.PreferredSizedPanel;
import free.util.swing.WrapperComponent;



/**
 * A UI element which allows the user to select the color of the pieces with
 * which he wants to play.
 */

public final class PieceColorSelection extends WrapperComponent{
  
  
  
  /**
   * The choice allowing the user to select the color.
   */
  
  private final JComboBox box;
  
  
  
  /**
   * Creates a new <code>PieceColorSelection</code> with the specified initially
   * selected value. <code>null</code> signifies that the color be selected
   * automatically.
   */
  
  public PieceColorSelection(Player initialColor){
    I18n i18n = I18n.get(PieceColorSelection.class);
    
    Object automatic = new NamedWrapper(null, i18n.getString("automatic"));
    Object white = new NamedWrapper(Player.WHITE_PLAYER, i18n.getString("white"));
    Object black = new NamedWrapper(Player.BLACK_PLAYER, i18n.getString("black"));
    Object selected = initialColor == null ? automatic : (initialColor.isWhite() ? white : black);
    Object [] pieceColorSelections = new Object[]{automatic, white, black};
    
    this.box = new JComboBox(pieceColorSelections);
    box.setEditable(false);
    box.setSelectedItem(selected);
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this component.
   */
  
  private void createUI(){
    I18n i18n = I18n.get(PieceColorSelection.class);
    
    JLabel label = i18n.createLabel("");
    label.setLabelFor(box);
    
    JPanel content = new PreferredSizedPanel(new TableLayout(2, 4, 6));
    content.add(label);
    content.add(box);
    
    add(content);
  }
  
  
  
  /**
   * Returns the selected color, <code>null</code> for automatic color
   * selection.
   */
  
  public Player getColor(){
    return (Player)((NamedWrapper)box.getSelectedItem()).getTarget();
  }
  
  
  
}
