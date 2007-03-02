/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


/**
 * A component which allows the user to select a color.
 */

public class ColorChooser extends JComponent implements Mnemonicable{



  /**
   * The button we're using.
   */

  private final JButton button;
  
  
  
  /**
   * The label we're using.
   */
   
  private final JLabel label;



  /**
   * The icon's size.
   */

  private static final Dimension ICON_SIZE = new Dimension(25, 10);
  
  
  
  /**
   * The color of the icon's border.
   */
  
  private static final Color ICON_BORDER_COLOR = Color.GRAY;



  /**
   * The current color.
   */

  private Color color;



  /**
   * The sole ChangeEvent we need.
   */

  private final ChangeEvent changeEvent = new ChangeEvent(this);


  
  /**
   * Creates a new <code>ColorChooser</code> with no text and initial color of black.
   */
  
  public ColorChooser(){
    this(null, Color.black);
  }
  
  
  
  /**
   * Creates a new <code>ColorChooser</code> with the specified text and the
   * initial color of black.
   */
  
  public ColorChooser(String text){
    this(text, Color.black);
  }
  
  
  
  /**
   * Creates a new <code>ColorChooser</code> with no text and the given initial
   * color.
   */

  public ColorChooser(Color initialColor){
    this(null, initialColor);
  }



  /**
   * Creates a new <code>ColorChooser</code> with the given text and initial
   * color.
   */

  public ColorChooser(String text, Color initialColor){
    label = new JLabel();
    button = new JButton();
    
    setText(text);
    setColor(initialColor);
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(label);
    add(Box.createHorizontalStrut(20));
    add(Box.createHorizontalGlue());
    add(button);

    label.setLabelFor(button);
    button.setDefaultCapable(false);
    
    if (SwingUtils.isMacLnF())
      button.setMargin(new Insets(5, 5, 5, 5));
    else
      button.setMargin(new Insets(3, 3, 3, 3));
    

    color = initialColor;

    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Color newColor = JColorChooser.showDialog(SwingUtilities.windowForComponent(ColorChooser.this),
            LocalizationService.getForClass(ColorChooser.class).getString("dialogTitle"), color); //$NON-NLS-1$
        if (newColor != null)
          setColor(newColor);
      }
    });
  }
  
  
  
  /**
   * Sets the text of the label.
   */
  
  public void setText(String text){
    label.setText(text);
  }
  
  
  
  /**
   * Returns the text of the label.
   */
  
  public String getText(){
    return label.getText();
  }



  /**
   * Sets the displayed mnemonic index.
   */
  
  public void setDisplayedMnemonicIndex(int mnemonicIndex){
    label.setDisplayedMnemonicIndex(mnemonicIndex);
  }
  
  
  
  /**
   * Sets the enabled state of this color chooser.
   */
   
  public void setEnabled(boolean enabled){
    label.setEnabled(enabled);
    button.setEnabled(enabled);
    
    super.setEnabled(enabled);
  }
   



  /**
   * Adds a ChangeListener to the list of listeners receiving notifications when
   * one of the text properties changes.
   */

  public void addChangeListener(ChangeListener listener){
    listenerList.add(ChangeListener.class, listener);
  }



  /**
   * Removes the given Changelistener from the list of listeners receiving
   * notifications when one of the text properties changes.
   */

  public void removeChangeListener(ChangeListener listener){
    listenerList.remove(ChangeListener.class, listener);
  }



  /**
   * Fires a ChangeEvent to all interested listeners.
   */

  protected void fireStateChanged(){
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
    
    Icon colorIcon = new SolidColorRectangleIcon(ICON_SIZE, color);
    button.setIcon(new BorderIcon(colorIcon, ICON_BORDER_COLOR));
    
    fireStateChanged();
  }
  
  
  
}
