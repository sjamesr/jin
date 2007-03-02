package free.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;



/**
 * An icon which adds a border around another icon. 
 */

public class BorderIcon implements Icon{
  
  
  
  /**
   * The wrapped icon.
   */
  
  private final Icon wrappedIcon;
  
  
  
  /**
   * The border's color.
   */
  
  private final Color borderColor;
  
  
  
  /**
   * Creates a new <code>BorderIcon</code> with the specified wrapped icon
   * and border color.
   */
  
  public BorderIcon(Icon wrappedIcon, Color borderColor){
    if (wrappedIcon == null)
      throw new IllegalArgumentException("wrappedIcon may not be null");
    if (borderColor == null)
      throw new IllegalArgumentException("borderColor may not be null");
    
    this.wrappedIcon = wrappedIcon;
    this.borderColor = borderColor;
  }
  
  
  
  /**
   * Returns the wrapped icon's height plus the border size.
   */
  
  public int getIconHeight(){
    return wrappedIcon.getIconHeight() + 2;
  }
  
  
  
  /**
   * Returns the wrapped icon's width plus the border size.
   */

  public int getIconWidth(){
    return wrappedIcon.getIconWidth() + 2;
  }
  
  
  
  /**
   * Returns the wrapped icon.
   */
  
  public Icon getWrappedIcon(){
    return wrappedIcon;
  }
  
  
  
  /**
   * Returns the border's color.
   */
  
  public Color getBorderColor(){
    return borderColor;
  }
  
  
  
  /**
   * Paints the wrapped icon and the border.
   */

  public void paintIcon(Component component, Graphics g, int x, int y){
    wrappedIcon.paintIcon(component, g, x + 1, y + 1);
    
    g.setColor(borderColor);
    g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
  }
  
  
  
}
