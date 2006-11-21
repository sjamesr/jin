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
 
import java.applet.Applet;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * A small applet which redirects to a different page based on the version of
 * Java in which it is run. The applet takes 3 parameters:
 * <ul>
 *   <li>minRequiredJavaVersion - the minimum required version of Java.
 *   <li>goodJavaURL - the URL to redirect to if the version of Java is equal to
 *       or higher than minRequiredJavaVersion.
 *   <li>badJavaURL - The URL to redirect to the if the version of Java is lower
 *       than minRequiredJavaVersion.
 * </ul>
 */
 
public class DetectJava extends Applet{
  
  
  
  
  /**
   * Invokes </code>getAppletContext().showDocument()</code> on the appropriate
   * URL.
   */
   
  public void start(){
    String minRequiredJavaVersion = getParameter("minRequiredJavaVersion");
    String javaVersion = System.getProperty("java.version");
    String url = (javaVersion.compareTo(minRequiredJavaVersion) < 0) ?  
      getParameter("badJavaURL") : getParameter("goodJavaURL");
    
    try{
      getAppletContext().showDocument(new URL(getDocumentBase(), url));
    } catch (MalformedURLException e){
        e.printStackTrace();
      }
  }
  
  
   
}