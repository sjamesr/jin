/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import free.workarounds.FixedJPanel;
import free.util.AWTUtilities;
import free.util.PlatformUtils;


/**
 * A <code>JPanel</code> which allows its content (always a single component)
 * to be displayed in fullscreen mode. 
 */

public class FullscreenPanel extends FixedJPanel{



  /**
   * Are we running under JDK 1.3 or earlier?
   */

  private static final boolean IS_FULLSCREEN_API_AVAILABLE = PlatformUtils.isJavaBetterThan("1.4");



  /**
   * The target component.
   */

  private final Component target;



  /**
   * Are we in the process of entering/exiting fullscreen mode.
   */

  private boolean beingModified = false;



  /**
   * The window which contains this panel when it's in fullscreen mode.
   */

  private RootPaneContainer fullscreenWindow = null;
  
  
  
  /**
   * The extended state of the original parent frame. This is needed as a
   * workaround for the maximized state sometimes being reset when coming back
   * from fullscreen mode.
   */
   
  private Object originalFrameState;
  
  
  
  /**
   * The original parent frame. We need to keep this because the fullscreen
   * panel might get removed from its parent, but we still need to make the
   * original parent frame visible when the target panel is made "normal" again.
   */
   
  private Frame originalFrame;
  
  
  
  /**
   * Creates a new <code>FullscreenPanel</code> with the specified target
   * component.
   */

  public FullscreenPanel(Component target){
    super(new BorderLayout());

    this.target = target;

    beingModified = true;
    add(target, BorderLayout.CENTER);
    beingModified = false;
  }



  /**
   * Overrides the superclass method to disallow adding any components.
   */

  public void add(Component comp, Object constraints, int index){
    if (beingModified)
      super.add(comp, constraints, index);
    else
      throw new IllegalStateException("You may not add components to FullscreenPanel");
  }



  /**
   * Overrides the superclass method to disallow removing any components.
   */

  public void remove(int index){
    if (beingModified)
      super.remove(index);
    else
      throw new IllegalStateException("You may not remove components from FullscreenPanel");
  }



  /**
   * Sets this panel's fullscreen mode.
   */

  public void setFullscreen(boolean on){
    if (on == isFullscreen())
      return;

    beingModified = true;
    if (on)
      makeFullscreen();
    else
      makeNormal();
    beingModified = false;
  }




  /**
   * Returns true if this panel is already in fullscreen mode.
   */

  public boolean isFullscreen(){
    return fullscreenWindow != null;
  }



  /**
   * Puts this panel into fullscreen mode.
   */

  private void makeFullscreen(){
    remove(target);

    if (IS_FULLSCREEN_API_AVAILABLE && securityManagerAllowsFullscreen())
      fullscreenWindow = setRealFullscreen();
    else
      fullscreenWindow = setFakeFullscreen();

    fullscreenWindow.getContentPane().setLayout(new BorderLayout());
    fullscreenWindow.getContentPane().add(target, BorderLayout.CENTER);
    fullscreenWindow.getContentPane().validate();
  }
  
  
  
  /**
   * Returns whether the security manager allows fullscreen mode.
   */
   
  private static boolean securityManagerAllowsFullscreen(){
    SecurityManager sm = System.getSecurityManager();
    if (sm == null)
      return true;
    
    try{
      Class awtPermissionClass = Class.forName("java.awt.AWTPermission");
      Constructor awtPermissionCtor = awtPermissionClass.getConstructor(new Class[]{String.class});
      Object fullscreenExclusivePermission = awtPermissionCtor.newInstance(new Object[]{"fullScreenExclusive"});
      Class permissionClass = Class.forName("java.security.Permission");
      Method checkPermissionMethod = SecurityManager.class.getMethod("checkPermission", new Class[]{permissionClass});
      try{
        checkPermissionMethod.invoke(sm, new Object[]{fullscreenExclusivePermission});
      } catch (InvocationTargetException e){
          if (e.getTargetException() instanceof SecurityException)
            return false;
          else
            e.printStackTrace();
        }
    } catch (ClassNotFoundException e){e.printStackTrace();}
      catch (NoSuchMethodException e){e.printStackTrace();}
      catch (InstantiationException e){e.printStackTrace();}
      catch (IllegalAccessException e){e.printStackTrace();}
      catch (InvocationTargetException e){e.printStackTrace();}
      
    return true;
  }



  /**
   * Attempts to set the specified window in real fullscreen mode via the
   * JDK 1.4 fullscreen API.
   */

  private RootPaneContainer setRealFullscreen(){
    try{
      originalFrame = SwingUtils.frameForComponent(this);
      JFrame frame = new JFrame(originalFrame == null ? "" : originalFrame.getTitle());
      if (originalFrame != null)
        frame.setIconImage(originalFrame.getIconImage());        

      Method setUndecorated = 
        Frame.class.getMethod("setUndecorated", new Class[]{boolean.class});
      Method setResizable = 
        Frame.class.getMethod("setResizable", new Class[]{boolean.class});

      setUndecorated.invoke(frame, new Object[]{Boolean.TRUE});
      setResizable.invoke(frame, new Object[]{Boolean.FALSE});

      Class windowClass = Window.class;
      Class graphicsConfigurationClass = Class.forName("java.awt.GraphicsConfiguration");
      Class graphicsDeviceClass = Class.forName("java.awt.GraphicsDevice");
      Method getGraphicsConfiguration = 
        windowClass.getMethod("getGraphicsConfiguration", (Class[])null);
      Method getDevice = graphicsConfigurationClass.getMethod("getDevice", new Class[0]);
      Method setFullScreenWindow =
        graphicsDeviceClass.getMethod("setFullScreenWindow", new Class[]{Window.class});

      Object graphicsConfiguration = 
        getGraphicsConfiguration.invoke(SwingUtils.frameForComponent(this), new Object[0]);
      Object graphicsDevice = getDevice.invoke(graphicsConfiguration, new Object[0]);
      
      if (originalFrame != null){
        Method getExtState = Frame.class.getMethod("getExtendedState", new Class[0]);
        originalFrameState = getExtState.invoke(originalFrame, new Object[0]);
  
        originalFrame.setVisible(false);
        setFullScreenWindow.invoke(graphicsDevice, new Object[]{frame});
      }

      return frame;
    } catch (Exception e){e.printStackTrace();}

    return null;
  }




  /**
   * Fakes a fullscreen mode, merely setting the specified window's size to the
   * size of the screen.
   */

  private RootPaneContainer setFakeFullscreen(){
    JWindow window = new JWindow(SwingUtils.frameForComponent(this));

    Dimension screenSize = AWTUtilities.getUsableScreenBounds().getSize();
    window.setBounds(0, 0, screenSize.width, screenSize.height);
    window.setVisible(true);
    window.toFront();

    return window;
  }


  

  /**
   * Puts this panel into normal (non-fullscreen) mode.
   */

  private void makeNormal(){
    fullscreenWindow.getContentPane().remove(target);

    if (IS_FULLSCREEN_API_AVAILABLE && securityManagerAllowsFullscreen())
      makeRealNormal();
    else
      makeFakeNormal();

    add(target, BorderLayout.CENTER);
    validate();

    fullscreenWindow = null;
  }



  /**
   * Restores a real fullscreen window.
   */

  private void makeRealNormal(){
    try{
      JFrame frame = (JFrame)fullscreenWindow;

      Class windowClass = Window.class;
      Class graphicsConfigurationClass = Class.forName("java.awt.GraphicsConfiguration");
      Class graphicsDeviceClass = Class.forName("java.awt.GraphicsDevice");
      Method getGraphicsConfiguration = 
        windowClass.getMethod("getGraphicsConfiguration", new Class[0]);
      Method getDevice = graphicsConfigurationClass.getMethod("getDevice", new Class[0]);
      Method setFullScreenWindow =
        graphicsDeviceClass.getMethod("setFullScreenWindow", new Class[]{Window.class});

      Object graphicsConfiguration = getGraphicsConfiguration.invoke(frame, new Object[0]);
      Object graphicsDevice = getDevice.invoke(graphicsConfiguration, new Object[0]);
      setFullScreenWindow.invoke(graphicsDevice, new Object[]{null});

      frame.dispose();

      
      if (originalFrame != null){
        Method setExtState =
          Frame.class.getMethod("setExtendedState", new Class[]{int.class});
        setExtState.invoke(originalFrame, new Object[]{originalFrameState});
        originalFrame.setVisible(true);
      }
    } catch (Exception e){e.printStackTrace();}
  }



  /**
   * Restores a fake fullscreen window.
   */

  private void makeFakeNormal(){
    JWindow window = (JWindow)fullscreenWindow;
    window.dispose();
  }



}