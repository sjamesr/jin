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

package free.jin.chessclub.console;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import free.util.swing.ColorChooserButton;
import free.util.swing.FontSelectorPanel;


/**
 * A common panel allowing to choose the font, foreground and optionally, 
 * background color of text.
 */

public class TextStyleChooserPanel extends JPanel{


  /**
   * The FontSelectorPanel we're using to select the font.
   */

  private final FontSelectorPanel fontSelector;




  /**
   * The ColorChooserButton used to select the foreground color.
   */

  private final ColorChooserButton foregroundChooser;




  /**
   * The ColorChooserButton used to select the background color.
   */

  private final ColorChooserButton backgroundChooser;




  /**
   * Creates a new TextStyleChooserPanel with the given initial font,
   * foreground and background colors. The <code>allowBackgroundSelection</code>
   * argument specifies whether the user should be allowed to change the
   * background.
   */

  public TextStyleChooserPanel(Font initialFont, Color initForegroundColor, Color initBackgroundColor, boolean allowBackgroundSelection){
    fontSelector = new FontSelectorPanel();
    foregroundChooser = new ColorChooserButton("Foreground", initForegroundColor);
    if (allowBackgroundSelection)
      backgroundChooser = new ColorChooserButton("Background", initBackgroundColor);
    else
      backgroundChooser = null;

    fontSelector.setSelectedFont(initialFont);
    fontSelector.getPreviewPanel().setOpaque(true);
    fontSelector.getPreviewPanel().setBackground(initBackgroundColor);
    fontSelector.getPreviewPanel().setForeground(initForegroundColor);

    createUI();

    ChangeListener colorChangeListener = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        fontSelector.getPreviewPanel().setForeground(foregroundChooser.getColor());
        if (backgroundChooser != null)
          fontSelector.getPreviewPanel().setBackground(backgroundChooser.getColor());
      }
    };

    foregroundChooser.addChangeListener(colorChangeListener);
    if (backgroundChooser != null)
      backgroundChooser.addChangeListener(colorChangeListener);
  }




  /**
   * Creates the UI.
   */

  private void createUI(){
    setLayout(new BorderLayout(5,5));

    JPanel colorChooserPanel = new JPanel(new GridLayout(1, (foregroundChooser == null) ? 1 : 2, 5, 5));
    colorChooserPanel.add(foregroundChooser);
    foregroundChooser.setDefaultCapable(false);

    if (backgroundChooser != null){
      colorChooserPanel.add(backgroundChooser);
      backgroundChooser.setDefaultCapable(false);
    }

    add(BorderLayout.CENTER, fontSelector);
    add(BorderLayout.SOUTH, colorChooserPanel);
  }




  /**
   * Sets the currently selected foreground color.
   */

  public void setSelectedForeground(Color color){
    foregroundChooser.setColor(color);
  }




  /**
   * Returns the currently selected foreground color.
   */

  public Color getSelectedForeground(){
    return foregroundChooser.getColor();
  }




  /**
   * Sets the currently selected font.
   */

  public void setSelectedFont(Font font){
    fontSelector.setSelectedFont(font);
  }




  /**
   * Returns the currently selected font.
   */

  public Font getSelectedFont(){
    return fontSelector.getSelectedFont();
  }




  /**
   * Returns the background color.
   */

  public void setSelectedBackground(Color color){
    fontSelector.getPreviewPanel().setBackground(color);
    if (backgroundChooser != null)
      backgroundChooser.setColor(color);
  }




  /**
   * Returns the selected background color.
   */

  public Color getSelectedBackground(){
    if (backgroundChooser == null)
      return fontSelector.getPreviewPanel().getBackground();
    else
      return backgroundChooser.getColor();
  }

}