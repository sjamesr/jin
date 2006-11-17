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
   * The bounds the frame should have, for bug 4424690.
   */
  
  private volatile Rectangle expectedBounds = null;
  
  
  
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
   * Remembers a user-driven change to the bounds during a call to
   * <code>show()</code>.
   */
  
  public void reshape(int x, int y, int width, int height){
    super.reshape(x, y, width, height);
    expectedBounds = new Rectangle(x, y, width, height);
  }
  
  
  
  /**
   * Works around bug 4424690 by resetting the frame's bounds after showing it.
   */
  
  public void show(){
    if (isVisible()){
      super.show();
      return;
    }
    
    expectedBounds = getBounds();
    
    super.show();
    
    setBounds(expectedBounds);
  }
  


  // </4424690>
  
  
  
}
