package free.workarounds;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JDialog;

import free.util.PlatformUtils;



/**
 * A fix of JDialog. Fixes the following bugs:
 * <ul>
 *   <li><a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5109571">
 *       REGRESSION: JDialog.setVisible(false) not honoured</a>
 * </ul>
 * 
 * @author Maryanovsky Alexander
 */

public class FixedJDialog extends JDialog{
  
  
  
  /**
   * Whether workaround for bug 5109571 should be applied.
   */
  
  private static final boolean WORKAROUND_5109571;
  static{
    boolean fix5109571;
    try{
      fix5109571 = 
        System.getProperty("java.awt.graphicsenv").equals("sun.awt.X11GraphicsEnvironment") &&
        PlatformUtils.isJavaBetterThan("1.5");
    } catch (SecurityException e){
        fix5109571 = (PlatformUtils.isLinux() || PlatformUtils.isSolaris()) && PlatformUtils.isJavaBetterThan("1.5");
    }
    
    WORKAROUND_5109571 = fix5109571;
  }
  
  
  
  /**
   * The time when the dialog was last shown. Needed for bug 5109571.
   */
  
  private long lastShownTime;

  
  
  
  public FixedJDialog() throws HeadlessException{
    super();
  }
  
  
  
  public FixedJDialog(Frame owner) throws HeadlessException{
    super(owner);
  }
  
  
  
  public FixedJDialog(Frame owner, boolean modal) throws HeadlessException{
    super(owner, modal);
  }
  
  
  
  public FixedJDialog(Frame owner, String title) throws HeadlessException{
    super(owner, title);     
  }
  
  
  
  public FixedJDialog(Frame owner, String title, boolean modal) throws HeadlessException{
    super(owner, title, modal);
  }
  
  
  
  public FixedJDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc){
    super(owner, title, modal, gc);
  }
  
  
  
  public FixedJDialog(Dialog owner) throws HeadlessException{
    super(owner);
  }
  
  
  
  public FixedJDialog(Dialog owner, boolean modal) throws HeadlessException{
    super(owner, modal);
  }
  
  
  
  public FixedJDialog(Dialog owner, String title) throws HeadlessException{
    super(owner, title);
  }
  
  
  
  public FixedJDialog(Dialog owner, String title, boolean modal) throws HeadlessException{
    super(owner, title, modal);
  }
  
  
  
  public FixedJDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException{
    super(owner, title, modal, gc);
  }
  
  
  
  // <5109571>
  
  /**
   * Remembers the time the dialog is shown.
   */
  
  public void show(){
    lastShownTime = System.currentTimeMillis();
    
    super.show();
  }
  
  
  
  /**
   * Works around bug 5109571, if needed, by delaying hiding the dialog.   
   */
  
  public void hide(){
    if (WORKAROUND_5109571){
      long remainingTimeout = 100 - (System.currentTimeMillis() - lastShownTime); 
      if (remainingTimeout > 0){
        try{
          Thread.sleep(remainingTimeout);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
      }
      
      super.hide();
    }
    else
      super.hide();
  }
  
  
  
  // </5109571>
  
  
  
}
