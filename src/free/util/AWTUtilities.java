/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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


package free.util;

import java.awt.*;
import java.lang.reflect.*;


/**
 * A collection of AWT related utilities.
 */

public class AWTUtilities{



  /**
   * Packs and centers the given window relative to the given component. The
   * specified component may be <code>null</code>, in which case the window will
   * be centered on the screen.
   */

  public static void centerWindow(Window target, Component parent){
    target.pack();

    Dimension size = target.getSize();
    Rectangle parentBounds = parent == null || !parent.isShowing() ? 
      new Rectangle(target.getToolkit().getScreenSize()) :
      new Rectangle(parent.getLocationOnScreen(), parent.getSize());

    target.setLocation(parentBounds.x + (parentBounds.width - size.width)/2, parentBounds.y + (parentBounds.height - size.height)/2);
  }




  /**
   * Returns the parent Frame of the specified <code>Component</code> or
   * <code>null</code> if none exists.
   */

  public static Frame frameForComponent(Component component){
    while (component != null){
      if (component instanceof Frame)
        return (Frame)component;
      component = component.getParent();
    }

    return null;
  }




  /**
   * Returns a list of available font names. Under JDK1.1 it uses
   * <code>Toolkit.getFontList()</code> while under JDK1.2 (via reflection),
   * <code>GraphicsEnvironment.getAvailableFontFamilyNames()</code>
   */

  public static String [] getAvailableFontNames(){
    if (System.getProperty("java.version").compareTo("1.3") >= 0){
      try{
        // The equivalent of "return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();"
        Class geClass = Class.forName("java.awt.GraphicsEnvironment");
        Method getLocalGraphicsEnvironmentMethod = geClass.getMethod("getLocalGraphicsEnvironment", null);
        Object localGE = getLocalGraphicsEnvironmentMethod.invoke(null, new Object[0]);
        Method getAvailableFontFamilyNamesMethod = geClass.getMethod("getAvailableFontFamilyNames", null);
        String [] fontNames = (String [])getAvailableFontFamilyNamesMethod.invoke(localGE, new Object[0]);
        return fontNames;
      } catch (ClassNotFoundException e){e.printStackTrace();}
        catch (NoSuchMethodException e){e.printStackTrace();}
        catch (IllegalAccessException e){e.printStackTrace();}
        catch (InvocationTargetException e){e.printStackTrace();}
      return null;
    }
    else
      return Toolkit.getDefaultToolkit().getFontList();
  }
  
  
  
  /**
   * Returns the state of the specified frame, as specified by
   * <code>Frame.getExtendedState()</code> if running under JDK 1.4 or later,
   * otherwise returns 0. The call to <code>Frame.getExtendedState()</code> is
   * done via reflection to avoid runtime errors.
   */
   
  public static int getExtendedFrameState(Frame frame){
    if (System.getProperty("java.version").compareTo("1.4") >= 0){
      try{
        Class frameClass = Class.forName("java.awt.Frame");
        Method getExtendedStateMethod = frameClass.getMethod("getExtendedState", null);
        Integer state = (Integer)getExtendedStateMethod.invoke(frame, new Object[0]);
        return state.intValue();
      } catch (ClassNotFoundException e){e.printStackTrace();}
        catch (NoSuchMethodException e){e.printStackTrace();}
        catch (IllegalAccessException e){e.printStackTrace();}
        catch (InvocationTargetException e){e.printStackTrace();}
    }
    
    return 0;
  }
  
  
  
  /**
   * Sets the state of the specified frame, as specified by
   * <code>Frame.setExtendedState</code> if running in JDK 1.4 or later,
   * otherwise does nothing. The call to <code>Frame.setExtendedState()</code>
   * is done via reflection to avoid runtime errors. 
   */
   
  public static void setExtendedFrameState(Frame frame, int state){
    if (System.getProperty("java.version").compareTo("1.4") >= 0){
      try{
        Class frameClass = Class.forName("java.awt.Frame");
        Method setExtendedStateMethod = frameClass.getMethod("setExtendedState", new Class[]{int.class});
        setExtendedStateMethod.invoke(frame, new Object[]{new Integer(state)});
      } catch (ClassNotFoundException e){e.printStackTrace();}
        catch (NoSuchMethodException e){e.printStackTrace();}
        catch (IllegalAccessException e){e.printStackTrace();}
        catch (InvocationTargetException e){e.printStackTrace();}
    }
  }
  
  
  
  /**
   * Enables or disables all the components within the specified container.
   * 
   * This is a rather hacky method - it doesn't work well if there are both
   * enabled and disabled components in the container.
   */

  public static void setContainerEnabled(Container container, boolean enabled){
    Component [] children = container.getComponents();
    for (int i = 0; i < children.length; i++){
      Component child = children[i];
      child.setEnabled(enabled);
      if (child instanceof Container)
        setContainerEnabled((Container)child, enabled);
    }
  }
  


}
