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

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.util.AWTUtilities;


/**
 * A base class for components which wrap some UI and present a convenient API
 * for it. This class is a convenience base class for such implementations. It
 * provides the following features:
 * <ul>
 *   <li>The default layout manager is {@link WrapLayout}.
 *   <li>It supports change listeners. The subclass is responsible only for
 *       invoking fireStateChanged() when its state actually changed.
 *       Note that the <code>addChangeListener/removeChangeListener</code>
 *       methods are <code>protected</code> by default - it's up to the subclass
 *       to allow access to them, by overriding them with a <code>public</code>
 *       modifier.
 *   <li>Invoking <code>setEnabled()</code> on the component will set the
 *       enabled state of all its children, recursively.
 * </ul>
 */

public abstract class WrapperComponent extends JComponent{
  
  
  
  /**
   * Creates a new <code>WrapperComponent</code>.
   */
  
  public WrapperComponent(){
    setLayout(WrapLayout.getInstance());
  }
  
  
  
  /**
   * Adds the specified change listener to receive notifications when the state
   * of this component changes. 
   */
  
  protected void addChangeListener(ChangeListener listener){
    listenerList.add(ChangeListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified change listener from receiving notifications when the
   * state of this component changes.
   */
  
  protected void removeChangeListener(ChangeListener listener){
    listenerList.remove(ChangeListener.class, listener);
  }
  
  
  
  /**
   * Fires the state change event to interested listeners.
   */
  
  protected void fireStateChanged(){
    ChangeEvent event = new ChangeEvent(this);
    
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChangeListener.class)
        ((ChangeListener)listeners[i+1]).stateChanged(event);
    }
  }
  
  
  
  /**
   * Sets the enabled state of this component, and all its children,
   * recursively.
   */
  
  @Override
  public void setEnabled(boolean isEnabled){
    super.setEnabled(isEnabled);
    AWTUtilities.setContainerEnabled(this, isEnabled);
  }
  
  
  
}
