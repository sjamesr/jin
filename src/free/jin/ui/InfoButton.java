package free.jin.ui;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;



/**
 * A button with a question mark icon. This should be used as the standard
 * "informational" button in Jin.
 * 
 * @author Maryanovsky Alexander
 */

public class InfoButton extends JButton{
  
  
  
  
  /**
   * The info button icon.
   */
  
  private static final Icon ICON = new ImageIcon(InfoButton.class.getResource("info.png"));
  
  
  
  /**
   * Creates a new <code>InfoButton</code>.
   */
  
  public InfoButton(){
    super(ICON);
    
    setFocusable(false);
    setMargin(new Insets(0, 0, 0, 0));
    
    // setMargin isn't enough for Ocean or Windows L&Fs
    Dimension legendButtonSize = new Dimension(ICON.getIconWidth() + 4, ICON.getIconHeight() + 4);
    setMinimumSize(legendButtonSize);
    setPreferredSize(legendButtonSize);
    setMaximumSize(legendButtonSize);
  }
  
  
  
}
