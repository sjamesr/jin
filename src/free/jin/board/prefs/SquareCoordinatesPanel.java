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

import javax.swing.*;
import free.chess.JBoard;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.jin.plugin.BadChangesException;
import free.util.swing.ColorChooser;
import free.util.swing.PreferredSizedPanel;


/**
 * A preferences panel allowing the user to select the square coordinates
 * settings.
 */
 
public class SquareCoordinatesPanel extends BoardModifyingPrefsPanel{
  
  
  
  /**
   * The radio button for no move square coordinates.
   */
   
  private final JRadioButton none;
  
  
  
  /**
   * The radio button for square coordinates on the rim of the board
   */
   
  private final JRadioButton rim;
  
  
  
  /**
   * The radio button for square coordinates outside the board.
   */
   
  private final JRadioButton outside;
  
  
  
  /**
   * The radio button for square coordinates in every square.
   */
   
  private final JRadioButton everySquare;
  
  
  
  /**
   * The color chooser for color of the square coordinates' text.
   */
   
  private final ColorChooser coordsColor;
  
  
  
  /**
   * Creates a new SquareCoordinatesPanel for the specified BoardManager and
   * with the specified preview board.
   */
   
  public SquareCoordinatesPanel(BoardManager boardManager, JinBoard previewBoard){
    super(boardManager, previewBoard);
    
    int coordsDisplayStyle = boardManager.getCoordsDisplayStyle();
    none = new JRadioButton("None", coordsDisplayStyle == JBoard.NO_COORDS);
    rim = new JRadioButton("On the rim of the board (like winboard)", coordsDisplayStyle == JBoard.RIM_COORDS);
    outside = new JRadioButton("Outside the board", coordsDisplayStyle == JBoard.OUTSIDE_COORDS);
    everySquare = new JRadioButton("In every square", coordsDisplayStyle == JBoard.ARROW_MOVE_HIGHLIGHTING);
    
    coordsColor = new ColorChooser("Coordinates' text color:", boardManager.getCoordsDisplayColor());
    
    ButtonGroup group = new ButtonGroup();
    group.add(none);
    group.add(rim);
    group.add(outside);
    group.add(everySquare);
    
    none.setMnemonic('N');
    rim.setMnemonic('r');
    outside.setMnemonic('O');
    everySquare.setMnemonic('I');
    coordsColor.setMnemonic('C');
    
    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    JPanel contentPanel = new PreferredSizedPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Square Coordinates"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    none.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    rim.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    outside.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    everySquare.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    coordsColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    contentPanel.add(none);
    contentPanel.add(rim);
    contentPanel.add(outside);
    contentPanel.add(everySquare);
    contentPanel.add(coordsColor);
    contentPanel.add(Box.createVerticalGlue());
    
    contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    add(contentPanel);
    
    ActionListener styleListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        SquareCoordinatesPanel.this.previewBoard.setCoordsDisplayStyle(getCoordsDisplayStyle());
        
        fireStateChanged();
      }
    };
    none.addActionListener(styleListener);
    rim.addActionListener(styleListener);
    outside.addActionListener(styleListener);
    everySquare.addActionListener(styleListener);
    
    coordsColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        SquareCoordinatesPanel.this.previewBoard.setCoordsDisplayColor(coordsColor.getColor());
        
        fireStateChanged();
      }
    });
  }
  
  
  
  /**
   * Returns the currently selected coordinates display style.
   */
   
  private int getCoordsDisplayStyle(){
    if (none.isSelected())
      return JBoard.NO_COORDS;
    else if (rim.isSelected())
      return JBoard.RIM_COORDS;
    else if (outside.isSelected())
      return JBoard.OUTSIDE_COORDS;
    else if (everySquare.isSelected())
      return JBoard.EVERY_SQUARE_COORDS;
    else
      throw new IllegalStateException("None of the radio buttons are selected");
  }
  
  
  
  /**
   * Sets the initial properties of the preview board.
   */
   
  public void initPreviewBoard(){
    previewBoard.setCoordsDisplayStyle(getCoordsDisplayStyle());
    previewBoard.setCoordsDisplayColor(coordsColor.getColor());
  }
  
  

  /**
   * Applies any changes made by the user.
   */
   
  public void applyChanges() throws BadChangesException{
    boardManager.setCoordsDisplayStyle(getCoordsDisplayStyle());
    boardManager.setCoordsDisplayColor(coordsColor.getColor());
  }
  
   
}

