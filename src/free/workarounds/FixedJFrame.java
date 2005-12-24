package free.workarounds;

import java.awt.Rectangle;

import javax.swing.JFrame;



/**
 * A fix of JInternalFrame. Fixes the following bugs:
 * <UL> 
 *   <LI><A HREF="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4424690">
 *       JFrame size changes after setVisible(true) [Linux]</A>.
 * </UL>
 */

public class FixedJFrame extends JFrame{
  
  
  /**
   * Creates a new <code>FixedJFrame</code> per the contract of the respective
   * <code>JFrame</code> constructor.
   */

  public FixedJFrame(){
    super();
  }
  
  
  
  /**
   * Creates a new <code>FixedJFrame</code> per the contract of the respective
   * <code>JFrame</code> constructor.
   */

  public FixedJFrame(String title){
    super(title);
  }
  
  
  
  // <4424690>
  
  
  
  /**
   * Works around bug 4424690 by resetting the frame's bounds after showing it.
   */
  
  public void show(){
    if (isVisible()){
      super.show();
      return;
    }
    
    Rectangle bounds = getBounds();
    
    super.show();
    
    if (!bounds.equals(getBounds()))
      setBounds(bounds);
  }
  


  // </4424690>
  
  
  
}
