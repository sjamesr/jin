/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Implements registering, unregistering change listeners and and firing change events. */
public class ChangeSupport {

  /** Our list of change listeners. */
  private final List listeners = new LinkedList();

  /** The change event we fire. */
  private final ChangeEvent changeEvent;

  /** Creates a new <code>ChangeSupport</code> with the specified source of change events. */
  public ChangeSupport(Object source) {
    changeEvent = new ChangeEvent(source);
  }

  /** Adds a change listener. */
  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  /** Removes a change listener. */
  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  /** Fires a state change event. */
  public void fireStateChanged() {
    for (Iterator i = listeners.iterator(); i.hasNext(); ) {
      ChangeListener listener = (ChangeListener) i.next();
      listener.stateChanged(changeEvent);
    }
  }
}
