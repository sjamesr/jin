/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import free.util.WindowDisposingListener;


/**
 * Provides various swing related utilities.
 */

public class SwingUtils{


  /**
   * The ESCAPE keystroke we use to close a dialog/window.
   */

  private static final KeyStroke CLOSE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);  



  /**
   * Adds a keyboard action to the specified <code>RootPaneContainer</code> so
   * that it is disposed when the ESCAPE key is hit. <B>Note that this only
   * works for subclasses of <code>Window</code>.</B>
   */

  public static void registerEscapeCloser(RootPaneContainer container){
    JRootPane rootPane = container.getRootPane();
    Window window = SwingUtilities.windowForComponent(rootPane);

    ActionListener closer = new WindowDisposingListener(window);
    rootPane.registerKeyboardAction(closer, CLOSE_KEYSTROKE, JComponent.WHEN_IN_FOCUSED_WINDOW);
  }



  /**
   * Returns the parent Frame of the specified <code>Component</code> or
   * <code>null</code> if none exists. This does the same as
   * <code>free.util.AWTUtilities.frameForComponent</code> but also takes care
   * of various swing quirks.
   */

  public static Frame frameForComponent(Component component){
    while (component != null){
      if (component instanceof Frame)
        return (Frame)component;

      if (component instanceof JPopupMenu) // The parent of a popup menu seems to be null
        component = ((JPopupMenu)component).getInvoker();
      else
        component = component.getParent();
    }

    return null;
  }
  
  
  
  /**
   * Creates and returns a <code>JPanel</code> with x-axis
   * <code>BoxLayout</code>. 
   */
   
  public static JPanel createHorizontalBox(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    return panel;
  }



  /**
   * Creates and returns a <code>JPanel</code> with y-axis
   * <code>BoxLayout</code>. 
   */
   
  public static JPanel createVerticalBox(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    return panel;
  }
  
  
  
  /**
   * Makes an icon button from the specified button by setting its icon to the
   * specified one, setting it not to draw its border or fill its content area
   * and setting its margins to 0.
   */
  
  public static void makeIconButton(AbstractButton button, 
      Icon icon, Icon rolloverIcon, Icon pressedIcon){
    
    button.setIcon(icon);
    button.setRolloverIcon(rolloverIcon);
    button.setPressedIcon(pressedIcon);
    
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(0, 0, 0, 0));
  }



  /**
   * Returns the displayed mnemonic index for the specified label specification.
   * A label specification is a string which possibly includes an ampersand;
   * the character following the first ampersand is the mnemonic. 
   */
  
  public static int getLabelMnemonicIndex(String labelSpec){
    return labelSpec.indexOf('&'); 
  }



  /**
   * Returns the text for the specified label specification.
   * A label specification is a string which possibly includes an ampersand;
   * the character following the first ampersand is the mnemonic. 
   */
  
  public static String getLabelText(String labelSpec){
    int ampIndex = labelSpec.indexOf('&');
    if (ampIndex == -1)
      return labelSpec;
    else
      return labelSpec.substring(0, ampIndex) + labelSpec.substring(ampIndex + 1);
  }
  
  
  
  /**
   * Applies the label specification on the specified button.
   */
  
  public static void applyLabelSpec(AbstractButton button, String labelSpec){
    button.setText(getLabelText(labelSpec));
    
    if (!isMacLnF())
      button.setDisplayedMnemonicIndex(getLabelMnemonicIndex(labelSpec));
  }
  
  
  
  /**
   * Applies the label specification on the specified label.
   */
  
  public static void applyLabelSpec(JLabel label, String labelSpec){
    label.setText(getLabelText(labelSpec));
    
    if (!isMacLnF())
      label.setDisplayedMnemonicIndex(getLabelMnemonicIndex(labelSpec));
  }
  
  
  
  /**
   * Applies the label specification on the specified mnemonicable component.
   */
  
  public static void applyLabelSpec(Mnemonicable mnemonicable, String labelSpec){
    mnemonicable.setText(getLabelText(labelSpec));
    
    if (!isMacLnF())
      mnemonicable.setDisplayedMnemonicIndex(getLabelMnemonicIndex(labelSpec));
  }
  
  
  
  /**
   * Returns whether we're currently running the Mac OS X Look&Feel.
   */
  
  public static boolean isMacLnF(){
    String id = UIManager.getLookAndFeel().getID(); 
    return id.equals("Mac") || id.equals("Aqua");
  }
  
  
  
  /**
   * Returns whether we're currently running the Metal Look&Feel.
   */
  
  public static boolean isMetalLnF(){
    return UIManager.getLookAndFeel().getID().equals("Metal");
  }
  
  
  
  /**
   * Returns whether we're currently running the Motif Look&Feel.
   */
  
  public static boolean isMotiflLnF(){
    return UIManager.getLookAndFeel().getID().equals("Motif");
  }
  
  
  
  /**
   * Returns whether we're currently running the Windows Look&Feel.
   */
  
  public static boolean isWindowsLnF(){
    return UIManager.getLookAndFeel().getID().equals("Windows");
  }
  
  
  
}
