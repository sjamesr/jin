/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Keymap;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Utilities;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import free.util.BrowserControl;
import java.util.Properties;
import java.awt.peer.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.datatransfer.Clipboard;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


/**
 * An extension of JTextPane which is used as the output component in a Console.
 * Implements the following features:
 * <UL>
 *   <LI> On right click, displays a popup menu which lets the user execute
 *        various commands with the current selection as the command argument.
 *   <LI> Allows marking parts of the text as Links. This allows a certain action
 *        to be performed 
 * </UL>
 */

public class ConsoleTextPane extends JTextPane{

  
  /**
   * The Console we're a part of.
   */

  protected final Console console;




  /**
   * The default Popup.
   */

  protected JPopupMenu defaultPopupMenu = null;




  /**
   * We keep the links here.
   */

  private Vector links = new Vector();





  /**
   * Our regular cursor (our real cursor might be hand while over a link).
   */

  private Cursor regCursor = Cursor.getDefaultCursor();




  /**
   * What is the link we're currently over? Null if none.
   */

  private Link curLink = null;





  /**
   * Creates a new ConsoleTextPane which will be a part of the given Console.
   */

  public ConsoleTextPane(Console console){
    this.console = console;

    setEditable(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    ToolTipManager tooltipManager = ToolTipManager.sharedInstance();
    if (!tooltipManager.isEnabled())
      tooltipManager.setEnabled(true);

    tooltipManager.registerComponent(this);

    Action copyAction = new DefaultEditorKit.CopyAction();
    Keymap keymap = getKeymap();
    keymap.removeBindings();
    keymap.setResolveParent(null);
    keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(0xFFCD, 0), copyAction);
    keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK), copyAction);

    enableEvents(MouseEvent.MOUSE_EVENT_MASK|MouseEvent.MOUSE_MOTION_EVENT_MASK);
  }


    
  /**
   * We override this to prevent unnecessary Toolkit.beep()s which are caused
   * when you press a key when the text pane has focus because it's not
   * editable. The reason why we don't want beeps to be generated is because
   * we're relaying the events to the console's input field. See
   * {@link free.jin.console.Console#keyPressed(java.awt.event.KeyEvent)} for
   * more details.
   */

  protected void processComponentKeyEvent(KeyEvent evt){
    if ((!isEditable()) && (getKeymap().getAction(KeyStroke.getKeyStrokeForEvent(evt)) == null)){
      evt.consume(); // Otherwise the parent scrollpane gets it and starts doing
                     // things we already handle, like scrolling on PAGE_UP
      return;
    }

    super.processComponentKeyEvent(evt);
  }




  /**
   * Returns a JPopupMenu for the given mouse event. This method is called each
   * time the right mouse button is clicked over the ConsoleTextPane, if it
   * returns null, no popup is shown. The default implementation returns obtains
   * the popup items from the plugin properties.
   */

  protected JPopupMenu getPopupMenu(MouseEvent evt){
    String selection = getSelectedText();
    int selectionStart = getSelectionStart();
    int selectionEnd = getSelectionEnd();

    if ((selection == null) || (selection.length() == 0) ||
        !isAboveText(evt.getX(), evt.getY(), selectionStart, selectionEnd)){
      int pressedLocation = viewToModel(evt.getPoint());
      if (pressedLocation == -1)
        return null;

      try{
        int wordStart = Utilities.getWordStart(this, pressedLocation);
        int wordEnd = Utilities.getWordEnd(this, pressedLocation);
        if (isAboveText(evt.getX(), evt.getY(), wordStart, wordEnd)){
          selection = getDocument().getText(wordStart, wordEnd - wordStart);
          if (selection.trim().length() != 0) // Don't override current selection with whitespace selection
            select(wordStart, wordEnd);

          selection = getSelectedText();
        }
      } catch (BadLocationException e){
          e.printStackTrace();
          return null;
        }
    }

    if ((selection == null) || (selection.length() == 0))
      return null;

    if (defaultPopupMenu == null){
      int numCommands = Integer.parseInt(console.getProperty("output-popup.num-commands", "0"));
      if (numCommands == 0)
        return null;

      defaultPopupMenu = new JPopupMenu();

      for (int i=0;i<numCommands;i++){
        String command = console.getProperty("output-popup.command-"+i);

        if (command.equalsIgnoreCase("separator")){
          defaultPopupMenu.addSeparator();
          continue;
        }

        String commandName = console.getProperty("output-popup.command-"+i+"-name");

        JMenuItem menuItem = new JMenuItem(commandName);
        menuItem.setActionCommand(command);
        menuItem.addActionListener(new ActionListener(){

          public void actionPerformed(ActionEvent evt){
            String actionCommand = ((AbstractButton)evt.getSource()).getActionCommand();
            String curSelection = getSelectedText();
            if (actionCommand.startsWith("$")){
              executePopupCommand(actionCommand.substring(1), curSelection);
              return;
            }
            String actualCommand = actionCommand;
            int index;
            while ((index = actualCommand.indexOf("!@"))!=-1)
              actualCommand = actualCommand.substring(0,index) + curSelection + actualCommand.substring(index+"!@".length());

            console.issueCommand(new Command(actualCommand,0));
          }
          
        });
        defaultPopupMenu.add(menuItem);
      }

      defaultPopupMenu.setSize(defaultPopupMenu.getPreferredSize());
    }
    
    return defaultPopupMenu;
  }




  /**
   * Executes a popup command, these are commands that start with '$' in
   * the properties. The default recognized commands:
   * <UL>
   *   <LI> copy - copies the current ConsoleTextPane's selection to the clipboard.
   *   <LI> execute - executes the currently selected text as a command.
   *   <LI> url - Treats the currently selected text as a URL and displays the URL
   *        in a browser.
   *   <LI> expurgate - Replaces all none-whitespace characters in the current
   *        selection with asterisks.
   * </UL>
   */

  protected void executePopupCommand(String command, String selection){
    if (command.equalsIgnoreCase("copy")){
      copy();
    }
    else if (command.equalsIgnoreCase("execute")){
      console.issueCommand(new Command(selection, 0));
    }
    else if (command.equalsIgnoreCase("expurgate"))
      expurgateSelection();
    else{
      console.addToOutput("Unknown popup command: "+command, "regular");
    }
  }

  



  /**
   * Expurgates the current selection by replacing all non-whitespace characters
   * with asterisks.
   */

  protected void expurgateSelection(){
    String selection = getSelectedText();
    int selectionLength = selection.length();
    StringBuffer asterisks = new StringBuffer(selectionLength);
    for (int i=0;i<selectionLength;i++){
      char c = selection.charAt(i);
      if (Character.isWhitespace(c))
        asterisks.append(c);
      else
        asterisks.append('*');
    }

    boolean isEditable = isEditable();
    setEditable(true);
    replaceSelection(asterisks.toString());
    setEditable(isEditable);
  }




  
  /**
   * Adds the given Link to this ConsoleTextPane, making the text between the
   * starting index and the ending index clickable.
   */

  public void addLink(Link link){
    links.addElement(link);
  }




  /**
   * Removes all the links.
   */

  public void removeLinks(){
    links.removeAllElements();
  }



  /**
   * Processes the given MouseEvent.
   */

  protected void processMouseEvent(MouseEvent evt){
    super.processMouseEvent(evt);

    if (evt.isPopupTrigger()){
      JPopupMenu popup = getPopupMenu(evt);
      if (popup != null){ 
        Component rootPane = SwingUtilities.getRootPane(this);
        Dimension rootPaneSize = rootPane.getSize();
        Point clickPointOnRootPane = SwingUtilities.convertPoint(this, evt.getPoint(), rootPane);
        int x = evt.getX();
        int y = evt.getY();
        int width = popup.getWidth();
        int height = popup.getHeight();
        if (clickPointOnRootPane.x + width > rootPaneSize.width)
          x = x - width;
        if (clickPointOnRootPane.y + height > rootPaneSize.height)
          y = y - height;
        
        popup.setSelected(null);
        popup.show(this,x,y);
      }
    }

    if (evt.getID() == MouseEvent.MOUSE_EXITED){
      curLink = null;
      setCursor(regCursor);
    }

    if ((evt.getID() == MouseEvent.MOUSE_ENTERED) || (evt.getID() == MouseEvent.MOUSE_RELEASED)){
      processPossibleLinkUpdate(evt);
    } 

    if (evt.getID()==MouseEvent.MOUSE_CLICKED){
      if (curLink!=null){
        console.issueCommand(curLink.getCommand());
//        evt.consume(); Not sure what's it for, so commenting out until I find out.
      }
    } 

  }




  /** 
   * Processes the given Mouse(Motion)Event.
   */

  protected void processMouseMotionEvent(MouseEvent evt){
    super.processMouseMotionEvent(evt);

    if (evt.getID()==MouseEvent.MOUSE_MOVED){
      processPossibleLinkUpdate(evt);
    }
  }




  /**
   * This method is called when a mouse event occurs that might change whether
   * we're over a link. This method determines whether it changed and updates
   * all the variables.
   */

  private void processPossibleLinkUpdate(MouseEvent evt){
    Link newLink = getLink(evt.getX(), evt.getY());

    if (newLink == null){
      if (curLink != null){
        curLink = null;
        setCursor(regCursor, false);
      }
    }
    else if (!newLink.equals(curLink)){
      curLink = newLink;
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), false);
    }
  }




  /**
   * Returns the link at the given location, or null if none.
   */

  protected Link getLink(int x, int y){
    int numLinks = links.size();
    for (int i = 0; i < numLinks; i++){
      Link link = (Link)links.elementAt(i);
      int linkStart = link.getStartIndex();
      int linkEnd = link.getEndIndex();

      if (isAboveText(x, y, linkStart, linkEnd))
        return link;
    }

    return null;
  }




  /**
   * Returns <code>true</code> if the specified location (in pixels) is on a
   * character between the two specified positions in the text.
   */

  protected boolean isAboveText(int x, int y, int startPos, int endPos){
    Rectangle startCharRect, endCharRect;
    try{
      startCharRect = modelToView(startPos);
      endCharRect = modelToView(endPos);
    } catch (BadLocationException e){ // Shouldn't happen
        e.printStackTrace();
        return false;
      }

    // Sometimes modelToView returns null, not sure why.
    if ((startCharRect == null) || (endCharRect == null)) 
      return false;

    if (startCharRect.y + startCharRect.height <= endCharRect.y){ // Separate lines.
      if (y > startCharRect.y){
        if (y <= startCharRect.y + startCharRect.height){
          if (x > startCharRect.x){
            try{
              int lineEnd = Utilities.getRowEnd(this, startPos);
              if (lineEnd == -1)
                return false;

              Rectangle lineEndCharRect = modelToView(lineEnd);
              if (x <= lineEndCharRect.x + lineEndCharRect.width)
                return true;
            } catch (BadLocationException e){ // Shouldn't happen
                e.printStackTrace();
                return false;
              }
          }
        }
        else if (y < endCharRect.y)
          return true;
        else if ((y < endCharRect.y+endCharRect.height) && (x < endCharRect.x+endCharRect.width))
          return true;
      }
      if ((y > startCharRect.y+startCharRect.height) && (y < endCharRect.y))
        return true;
    }
    else{ // Same line
      if (startCharRect.union(endCharRect).contains(x, y))
        return true;
    }

    return false;
  }




  /**
   * Returns the tooltip to display.
   */

  public String getToolTipText(MouseEvent evt){
    Link link = getLink(evt.getX(), evt.getY());
    if (link != null){
      Command command = link.getCommand();
      if (!command.isSpecial()){
        String commandString = command.getCommandString();

        try{
          int startIndex = link.getStartIndex();
          int length = link.getEndIndex()-startIndex;
          String linkText = getText(startIndex, length);

          if (linkText.equals(commandString))
            return null;
          else
            return commandString;
        } catch (BadLocationException e){}
      }
    }
    
    return null;
  }




  /**
   * Overrides setCursor to save information about the regular cursor.
   */

  public void setCursor(Cursor cursor){
    setCursor(cursor, true);
  }




  /**
   * Sets the cursor to the given Cursor, if the given boolean value is true,
   * saves the given cursor into regCursor.
   */

  private void setCursor(Cursor cursor, boolean save){
    super.setCursor(cursor);
    if (save)
      regCursor = cursor;
  }




  /**
   * Returns the unit scroll amount.
   */

  public int getScrollableUnitIncrement(Rectangle viewRect, int orientation, int direction){
    if (orientation==SwingConstants.HORIZONTAL)
      return super.getScrollableUnitIncrement(viewRect, orientation, direction);

    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
    return metrics.getHeight();
  }

}