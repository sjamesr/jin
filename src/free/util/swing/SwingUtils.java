/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import free.util.WindowDisposingListener;
import free.util.imagefilters.IconImageFilters;


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
   * Configures the specified button to be an icon button. The button is made
   * unfocusable, non default capable, non painting its border or content area
   * and marginless. Additionally, unless the specified image is
   * <code>null</code>, variants of the image to be used as rollover, pressed
   * and disabled icons are generated and set on the button.
   */
  
  public static void confIconButton(AbstractButton button, Image image){
    if (image != null){
      button.setIcon(new ImageIcon(image));
      button.setRolloverIcon(new ImageIcon(IconImageFilters.getRollover(image)));
      button.setPressedIcon(new ImageIcon(IconImageFilters.getPressed(image)));
      button.setDisabledIcon(new ImageIcon(IconImageFilters.getDisabled(image)));
    }
    
    button.setFocusable(false);
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
    if (labelSpec == null)
      return -1;
    
    return labelSpec.indexOf('&'); 
  }
  
  
  
  /**
   * Returns the text for the specified label specification.
   * A label specification is a string which possibly includes an ampersand;
   * the character following the first ampersand is the mnemonic. 
   */
  
  public static String getLabelText(String labelSpec){
    if (labelSpec == null)
      return null;
    
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
    
    if (!isMacLnF()){
      int mnemonicIndex = getLabelMnemonicIndex(labelSpec);
      if (mnemonicIndex != -1){
        int mnemonic = Character.toUpperCase(labelSpec.charAt(mnemonicIndex + 1));
        button.setMnemonic(mnemonic);
        button.setDisplayedMnemonicIndex(mnemonicIndex);
      }
    }
  }
  
  
  
  /**
   * Applies the label specification on the specified label.
   */
  
  public static void applyLabelSpec(JLabel label, String labelSpec){
    label.setText(getLabelText(labelSpec));
    
    if (!isMacLnF()){
      int mnemonicIndex = getLabelMnemonicIndex(labelSpec);
      if (mnemonicIndex != -1){
        int mnemonic = Character.toUpperCase(labelSpec.charAt(mnemonicIndex + 1));
        label.setDisplayedMnemonic(mnemonic);
        label.setDisplayedMnemonicIndex(mnemonicIndex);
      }
    }
  }
  
  
  
  /**
   * Applies the label specification on the specified mnemonicable component.
   */
  
  public static void applyLabelSpec(Mnemonicable mnemonicable, String labelSpec){
    mnemonicable.setText(getLabelText(labelSpec));
    
    if (!isMacLnF()){
      int mnemonicIndex = getLabelMnemonicIndex(labelSpec);
      if (mnemonicIndex != -1){
        int mnemonic = Character.toUpperCase(labelSpec.charAt(mnemonicIndex + 1));
        mnemonicable.setMnemonic(mnemonic);
        mnemonicable.setDisplayedMnemonicIndex(mnemonicIndex);
      }
    }
  }
  
  
  
  /**
   * Creates a label for each line in the specified string.
   */
  
  public static JLabel [] labelsForLines(String text){
    StringTokenizer tokenizer = new StringTokenizer(text, "\r\n");
    JLabel [] labels = new JLabel[tokenizer.countTokens()];
    for (int i = 0; i < labels.length; i++)
      labels[i] = new JLabel(tokenizer.nextToken());
    
    return labels;
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
  
  
  
  /**
   * Returns whether we're currently running the GKT Look&Feel.
   */
  
  public static boolean isGtkLnF(){
    return UIManager.getLookAndFeel().getID().equals("GTK");
  }
  
  
  
  /**
   * Links the enabled state of the specified component to the selected state of
   * the specified <code>ListSelectionModel</code>.
   */
  
  public static void linkEnabledToSelected(final JComponent component, final ListSelectionModel model){
    component.setEnabled(!model.isSelectionEmpty());
    model.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        if (!e.getValueIsAdjusting())
          component.setEnabled(!model.isSelectionEmpty());
      }
    });
  }
  
  
  
}
