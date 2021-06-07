/**
 * The workarounds library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The workarounds library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * <p>The workarounds library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the
 * workarounds library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.workarounds;

import java.awt.AWTEvent;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A fix for JPanel. Fixes the following bugs:
 *
 * <UL>
 *   <LI><A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4221132.html"> setCursor
 *       only works if component is using a MouseListener</A>
 * </UL>
 */
public class FixedJPanel extends JPanel {

  public FixedJPanel() {
    fixBugs();
  }

  public FixedJPanel(boolean isDoubleBuffered) {
    super(isDoubleBuffered);
    fixBugs();
  }

  public FixedJPanel(LayoutManager layout) {
    super(layout);
    fixBugs();
  }

  public FixedJPanel(LayoutManager layout, boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
    fixBugs();
  }

  /**
   * Fixes various bugs that can be fixed in the constructor. This method is called from all the
   * constructors.
   */
  protected void fixBugs() {

    // http://developer.java.sun.com/developer/bugParade/bugs/4221132.html //

    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

    // http://developer.java.sun.com/developer/bugParade/bugs/4221132.html //
  }
}
