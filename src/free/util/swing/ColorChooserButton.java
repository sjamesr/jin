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

package free.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


/**
 * A button which displays a color and when pressed, opens a JColorChooser and
 * allows the user to select a different color.
 */

public class ColorChooserButton extends JButton implements ActionListener{



  /**
   * The icon's size.
   */

  private static final Dimension iconSize = new Dimension(30, 20);




  /**
   * The current color.
   */

  private Color color;




  /**
   * The sole ChangeEvent we need.
   */

  private final ChangeEvent changeEvent = new ChangeEvent(this);




  /**
   * Creates a new ColorChooserButton with no text and the given initial color.
   */

  public ColorChooserButton(Color initialColor){
    this(null, initialColor);
  }





  /**
   * Creates a new ColorChooserButton with the given text and initial color.
   */

  public ColorChooserButton(String text, Color initialColor){
    super(text, new SolidColorIcon(iconSize, initialColor));

    setHorizontalTextPosition(JButton.LEADING);

    color = initialColor;

    addActionListener(this);
  }




  /**
   * Adds a ChangeListener to the list of listeners receiving notifications when
   * the selected color changes.
   */

  public void addChangeListener(ChangeListener listener){
    listenerList.add(ChangeListener.class, listener);
  }




  /**
   * Removes the given Changelistener from the list of listeners receiving
   * notifications when the selected color changes.
   */

  public void removeChangeListener(ChangeListener listener){
    listenerList.remove(ChangeListener.class, listener);
  }




  /**
   * Fires a ChangeEvent to all interested listeners.
   */

  protected void fireChangeEvent(){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChangeListener.class){
        ChangeListener listener = (ChangeListener)listeners[i+1];
        listener.stateChanged(changeEvent);
      }
    }

  }



  /**
   * Returns the currently selected color.
   */

  public Color getColor(){
    return color;
  }




  /**
   * Sets the current color.
   */

  public void setColor(Color color){
    this.color = color;
    setIcon(new SolidColorIcon(iconSize, color));
    fireChangeEvent();
  }




  /**
   * Shows the color chooser.
   */

  public void actionPerformed(ActionEvent evt){
    Color newColor = JColorChooser.showDialog(SwingUtilities.windowForComponent(this), "Chooser a color", color);
    if (newColor != null)
      setColor(newColor);
  }

}