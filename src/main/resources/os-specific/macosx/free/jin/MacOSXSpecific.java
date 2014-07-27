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
 
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
 
 
/**
 * This class does various Mac OS X specific things for Jin. It is only compiled
 * and used when running under OS X. Using it involves simply loading it via
 * <code>Class.forName</code>
 */
 
public class MacOSXSpecific{
  


  /**
   * Invokes the go() method.
   */
  
  static{
    try{
      go();
    } catch (RuntimeException e){
        e.printStackTrace();
        throw e;
      }
  }
  
  
  
  /**
   * Performs various OS X specific things.
   */
   
  private static void go(){
    registerApplicationListener();
  }
  
  
  
  /**
   * Registers an <code>ApplicationListener</code> with the
   * <code>Application</code>. 
   */
   
  private static void registerApplicationListener(){
    Application.getApplication().addApplicationListener(new ApplicationAdapter(){
      
      public void handleQuit(ApplicationEvent evt){
        evt.setHandled(true);
        Jin jin = Jin.getInstance();
        if (jin != null)
          jin.quit(false);
      }
      
      public void handleAbout(ApplicationEvent evt){
        evt.setHandled(true);
        Jin jin = Jin.getInstance();
        if (jin != null)
          jin.showAboutDialog();
      }
      
    });
  }
  
  
}
 
