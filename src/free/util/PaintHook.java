package free.util;

import java.awt.Graphics;
import java.awt.Component;


/**
 * An interface suitable for classes which want to allow extending their
 * (complex) painting behaviour. The interface has one method - 
 * <code>paint(Component, Graphics)</code> which should be called by the
 * service component at some point during its painting.
 */

public interface PaintHook{



  /**
   * This method should be called by the service component sometime during its
   * painting.
   *
   * @param component The Component which is painting.
   * @param g The Graphics object which is doing the painting.
   */

  void paint(Component component, Graphics g);


}