/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.board;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import free.jin.plugin.PreferencesPanel;
import free.util.swing.ColorChooserButton;
import free.util.StringEncoder;
import free.util.StringParser;


/**
 * The preferences panel for the BoardManager plugin.
 */

public class BoardPreferencesPanel extends PreferencesPanel{


  
  /**
   * The BoardManager whose preferences panel this BoardPreferencesPanel is.
   */

  private final BoardManager boardManager;




  /**
   * The ColorChooserButton for selecting the color of the white pieces.
   */

  private final ColorChooserButton whiteColorChooser;




  /**
   * The ColorChooserButton for selecting the color of the black pieces.
   */

  private final ColorChooserButton blackColorChooser;




  /**
   * The ColorChooserButton for selecting the color of the outline for white
   * pieces.
   */

  private final ColorChooserButton whiteOutlineChooser;




  /**
   * The ColorChooserButton for selecting the color of the outline for black
   * pieces.
   */

  private final ColorChooserButton blackOutlineChooser;




  /**
   * The ColorChooserButton for selecting the color of the light squares.
   */

  private final ColorChooserButton lightColorChooser;




  /**
   * The ColorChooserButton for selecting the color of the dark squares.
   */

  private final ColorChooserButton darkColorChooser;




  /**
   * Creates a new BoardPreferencesPanel with the given BoardManager.
   */

  public BoardPreferencesPanel(BoardManager boardManager){
    this.boardManager = boardManager;

    whiteColorChooser = new ColorChooserButton("White pieces", getColorProperty("white-piece-color", Color.white));
    blackColorChooser = new ColorChooserButton("Black pieces", getColorProperty("black-piece-color", Color.black));
    whiteOutlineChooser = new ColorChooserButton("White pieces' outline", getColorProperty("white-outline-color", Color.black));
    blackOutlineChooser = new ColorChooserButton("Black pieces' outline", getColorProperty("black-outline-color", Color.white));

    lightColorChooser = new ColorChooserButton("Light squares", getColorProperty("light-square-color", new Color(255,207,144)));
    darkColorChooser = new ColorChooserButton("Dark squares", getColorProperty("dark-square-color", new Color(143,96,79)));

    createUI();
  }




  /**
   * Returns the given color property. If there is no such property, returns the
   * given default.
   */

  private Color getColorProperty(String name, Color defaultValue){
    String value = boardManager.getProperty(name);
    if (value == null)
      return defaultValue;
    else
      return StringParser.parseColor(value);
  }




  /**
   * Creates the UI.
   */

  private void createUI(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel piecesPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    piecesPanel.add(whiteColorChooser);
    piecesPanel.add(blackColorChooser);
    piecesPanel.add(whiteOutlineChooser);
    piecesPanel.add(blackOutlineChooser);
    Border innerBorder = new EmptyBorder(5, 5, 5, 5);
    Border outerBorder = new TitledBorder("Piece preferences");
    piecesPanel.setBorder(new CompoundBorder(outerBorder, innerBorder));

    JPanel boardPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    boardPanel.add(lightColorChooser);
    boardPanel.add(darkColorChooser);
    innerBorder = new EmptyBorder(5, 5, 5, 5);
    outerBorder = new TitledBorder("Board preferences");
    boardPanel.setBorder(new CompoundBorder(outerBorder, innerBorder));

    add(piecesPanel);
    add(Box.createVerticalStrut(10));
    add(boardPanel);
  }




  /**
   * Applies the changes done by the user.
   */

  public void applyChanges(){
    maybeSetColorProperty("white-piece-color", whiteColorChooser.getColor());
    maybeSetColorProperty("black-piece-color", blackColorChooser.getColor());
    maybeSetColorProperty("white-outline-color", whiteOutlineChooser.getColor());
    maybeSetColorProperty("black-outline-color", blackOutlineChooser.getColor());

    maybeSetColorProperty("light-square-color", lightColorChooser.getColor());
    maybeSetColorProperty("dark-square-color", darkColorChooser.getColor());

    boardManager.refreshFromProperties();
  }




  /**
   * If the current value of the color property is the same as the new one, does
   * nothing. Otherwise, sets the user property to the given color.
   */

  private void maybeSetColorProperty(String name, Color newValue){
    if (newValue.equals(getColorProperty(name, null)))
      return;

    boardManager.setProperty(name, StringEncoder.encodeColor(newValue), true);
  }

}
