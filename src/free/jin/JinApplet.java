/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin;

import javax.swing.JApplet;
import free.util.BrowserControl;



/**
 * An applet which runs Jin, via a <code>AppletJinContext</code>. This is just
 * a small class responsible for creating an <code>AppletJinContext</code> and
 * passing it the various applet events (start, stop, destroy).
 */

public class JinApplet extends JApplet{
  
  
  
  /**
   * The <code>AppletJinContext</code> we're running Jin with.
   */
   
  private AppletJinContext context;
  
  
  
  /**
   * The exception (Throwable, really) we got when trying to create the context,
   * <code>null</code> if none.
   */
   
  private Throwable contextCreationError;

  

  /**
   * Creates an AppletJinContext.
   */
   
  public void init(){
    super.init();
    
    try{
      BrowserControl.setAppletContext(getAppletContext());
      context = new AppletJinContext(this);
    } catch (Throwable t){
        contextCreationError = t;
      }
  }
  
  
  
  /**
   * Invokes the context's <code>start</code> method.
   */
   
  public void start(){
    super.start();
    
    if (context != null)
      context.start();  
    else{
      contextCreationError.printStackTrace();
      // TODO: Show error dialog
    }
  }
  
  
  
  /**
   * Invokes the context's <code>quit</code> method with a <code>false</code>
   * argument.
   */
   
  public void stop(){
    if (context != null)
      context.stop();
  }
  
  
  
}
