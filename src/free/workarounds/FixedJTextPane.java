/**
 * The workarounds library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The workarounds library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The workarounds library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the workarounds library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package free.workarounds;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;


/**
 * A fix of JTextPane. Fixes the following bugs:
 * <UL>
 *   <LI> copy()/paste()/cut() throws exceptions under MS VM when run as an
 *        applet.
 * </UL>
 */
 
public class FixedJTextPane extends JTextPane{
  
  
  
  public FixedJTextPane(){
     
  }
  
  
  
  public FixedJTextPane(StyledDocument doc){
    super(doc); 
  }
  
  
  
  public void paste(){
    try{
      super.paste();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakePaste(this);
        else
          throw e;
      }
  }
  
  
  
  public void copy(){
    try{
      super.copy();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakeCopy(this);
        else
          throw e;
      }
  }


  
  public void cut(){
    try{
      super.cut();
    } catch (RuntimeException e){ // MS VM throws a com.ms.security.SecurityExceptionEx
        if (e.getClass().getName().equals("com.ms.security.SecurityExceptionEx"))
          FixUtils.fakeCut(this);
        else
          throw e;
      }
  }
  
 
   
}

