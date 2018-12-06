/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002, 2003 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.console;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Keymap;
import javax.swing.text.Utilities;

import free.jin.I18n;
import free.jin.Preferences;
import free.util.GraphicsUtilities;
import free.util.ImageUtilities;
import free.util.PlatformUtils;
import free.workarounds.FixedJTextPane;

/**
 * An extension of JTextPane which is used as the output component in a Console. Implements the
 * following features:
 * <UL>
 * <LI>On right click, displays a popup menu which lets the user execute various commands with the
 * current selection as the command argument.
 * <LI>Allows marking parts of the text as Links. This allows a certain action to be performed
 * </UL>
 */
public class ConsoleTextPane extends FixedJTextPane {

  /**
   * The Console we're a part of.
   */
  protected final Console console;

  /**
   * The paint we use for the background; <code>null</code> if none.
   */
  private Paint backgroundPaint = null;

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
  public ConsoleTextPane(Console console) {
    this.console = console;

    setEditable(false);

    ToolTipManager tooltipManager = ToolTipManager.sharedInstance();
    if (!tooltipManager.isEnabled()) tooltipManager.setEnabled(true);

    tooltipManager.registerComponent(this);

    Action copyAction = new DefaultEditorKit.CopyAction();
    Keymap keymap = getKeymap();
    keymap.removeBindings();
    keymap.setResolveParent(null);
    keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0), copyAction);
    keymap.addActionForKeyStroke(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
        copyAction);

    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
  }

  /**
   * Re-reads all the plugin properties used by this instance and/or clears any cached values of
   * such properties.
   */
  public void refreshFromProperties() {
    defaultPopupMenu = null;
    repaint();
  }

  /**
   * Sets the image we use for the background pattern.
   */
  public void setBackgroundPattern(Image image) {
    setBackground(
        new Color(0, 0, 0, 0)); // Otherwise the background color is painted over the pattern

    ImageUtilities.preload(image);
    BufferedImage bi =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
    bi.getGraphics().drawImage(image, 0, 0, null);
    backgroundPaint = new TexturePaint(bi, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
  }

  /**
   * Overrides <code>paintComponent</code> to enable/disable antialiasing and perhaps other
   * expensive rendering hints.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    Paint standardPaint = g2d.getPaint();

    g2d.setPaint(backgroundPaint);
    g2d.fill(g2d.getClip());

    g2d.setPaint(standardPaint);

    boolean antialias = console.getPrefs().getBool("output-text.antialias", false);
    Object textAntialiasValue =
        antialias
            ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAntialiasValue);

    super.paintComponent(g);
  }

  /**
   * Should we use the hack that prevents the text pane from becoming taller than Short.MAX_VALUE
   * pixels?
   */
  private static final boolean SHOULD_USE_16_BIT_GRAPHICS_HACK =
      !PlatformUtils.isJavaBetterThan("1.2")
          && (PlatformUtils.isOldWindows() || PlatformUtils.isSolaris());

  /**
   * Overrides <code>reshape(int, int, int, int)</code> to possibly prevent the text pane from
   * becoming taller than Short.MAX_VALUE pixels tall. See bug
   * http://developer.java.sun.com/developer/bugParade/bugs/4138673.html for details.
   */
  @Override
  public void reshape(int x, int y, int width, int height) {
    if ((height > Short.MAX_VALUE) && SHOULD_USE_16_BIT_GRAPHICS_HACK) {
      try {
        // Remove lines until our preferred height is less than Short.MAX_VALUE
        Document document = getDocument();
        while (getPreferredSize().height >= Short.MAX_VALUE) {

          // Find the first newline
          int documentLength = document.getLength();
          String text = document.getText(0, Math.min(200, documentLength));
          int newlineIndex;
          while ((newlineIndex = text.indexOf('\n')) == -1)
            text = document.getText(0, Math.min(text.length() * 2, documentLength));

          // Remove the first line
          document.remove(0, newlineIndex + 1);
        }
      } catch (BadLocationException e) {
        e.printStackTrace();
      }

      Container parent = getParent();
      if (parent != null) {
        parent.invalidate();
        parent.validate();
        parent.doLayout();
      }
    } else super.reshape(x, y, width, height);
  }

  /**
   * We override this to prevent unnecessary Toolkit.beep()s which are caused when you press a key
   * when the text pane has focus because it's not editable. The reason why we don't want beeps to
   * be generated is because we're relaying the events to the console's input field. See
   * {@link free.jin.console.Console#keyPressed(java.awt.event.KeyEvent)} for more details.
   */
  @Override
  protected void processComponentKeyEvent(KeyEvent evt) {
    if ((!isEditable()) && (getKeymap().getAction(KeyStroke.getKeyStrokeForEvent(evt)) == null)) {
      evt.consume(); // Otherwise the parent scrollpane gets it and starts doing
      // things we already handle, like scrolling on PAGE_UP
      return;
    }

    super.processComponentKeyEvent(evt);
  }

  /**
   * Returns a JPopupMenu for the given mouse event. This method is called each time the right mouse
   * button is clicked over the ConsoleTextPane, if it returns null, no popup is shown. The default
   * implementation returns obtains the popup items from the plugin properties.
   */
  protected JPopupMenu getPopupMenu(MouseEvent evt) {
    String selection = getSelectedText();
    int selectionStart = getSelectionStart();
    int selectionEnd = getSelectionEnd();

    if ((selection == null)
        || (selection.length() == 0)
        || !isOverText(evt.getX(), evt.getY(), selectionStart, selectionEnd)) {
      int pressedLocation = viewToModel(evt.getPoint());
      if (pressedLocation == -1) return null;

      try {
        int wordStart = getWordStart(pressedLocation);
        int wordEnd = getWordEnd(pressedLocation);
        if (isOverText(evt.getX(), evt.getY(), wordStart, wordEnd)) {
          selection = getDocument().getText(wordStart, wordEnd - wordStart);
          if (selection.trim().length()
              != 0) // Don't override current selection with whitespace selection
          select(wordStart, wordEnd);

          selection = getSelectedText();
        }
      } catch (BadLocationException e) {
        e.printStackTrace();
        return null;
      }
    }

    if ((selection == null) || (selection.length() == 0)) return null;

    if (defaultPopupMenu == null) defaultPopupMenu = createPopupMenu();

    return defaultPopupMenu;
  }

  /**
   * Creates the popup menu for this <code>ConsoleTextPane</code>.
   */
  protected JPopupMenu createPopupMenu() {
    Preferences prefs = console.getPrefs();

    // We need this one because the categories (and thus the localization)
    // are defined in a server specified package
    I18n consoleManagerI18n = console.getConsoleManager().getI18n();

    String popupPrefix = "consolePopup.";
    int itemCount = prefs.getInt(popupPrefix + "itemCount", 0);
    if (itemCount == 0) return null;

    JPopupMenu popupMenu = new JPopupMenu();

    for (int i = 0; i < itemCount; i++) {
      String itemPrefix = popupPrefix + i + ".";

      String itemType = prefs.getString(itemPrefix + "type", "serverCommand");
      String labelKey = prefs.getString(itemPrefix + "labelKey", null);
      String label = labelKey == null ? null : consoleManagerI18n.getString(labelKey);

      if ("serverCommand".equals(itemType)) {
        String command = "/" + prefs.getString(itemPrefix + "command");
        popupMenu.add(new IssueCommandMenuItem(label, command));
      } else if ("separator".equals(itemType)) popupMenu.addSeparator();
      else if ("copy".equals(itemType)) popupMenu.add(new CopyMenuItem(label));
      else if ("execute".equals(itemType)) popupMenu.add(new ExecuteMenuItem(label));
      else if ("expurgate".equals(itemType)) popupMenu.add(new ExpurgateMenuItem(label));
      else throw new IllegalStateException("Unknown console popup item type: " + itemType);
    }

    popupMenu.setSize(popupMenu.getPreferredSize());

    return popupMenu;
  }

  /**
   * Returns the start of the word at the specified location.
   */
  private int getWordStart(int location) throws BadLocationException {
    Document document = getDocument();
    Element lineElement = Utilities.getParagraphElement(this, location);
    int lineStart = lineElement.getStartOffset();
    int lineEnd = Math.min(lineElement.getEndOffset(), document.getLength());

    String text = document.getText(lineStart, lineEnd - lineStart);
    if ((text == null) || (text.length() == 0)) return location;

    location -= lineStart;

    char locationChar = text.charAt(location);
    boolean isWhitespaceWord;
    if (isWhitespaceChar(locationChar)) isWhitespaceWord = true;
    else if (isWordChar(locationChar)) isWhitespaceWord = false;
    else return location + lineStart;

    for (int i = location; i >= 0; i--) {
      char c = text.charAt(i);
      if ((isWhitespaceWord && !isWhitespaceChar(c)) || !isWhitespaceWord && !isWordChar(c))
        return lineStart + i + 1;
    }

    return lineStart;
  }

  /**
   * Returns the end of the word at the specified location.
   */
  private int getWordEnd(int location) throws BadLocationException {
    Document document = getDocument();
    Element lineElement = Utilities.getParagraphElement(this, location);
    int lineStart = lineElement.getStartOffset();
    int lineEnd = Math.min(lineElement.getEndOffset(), document.getLength());

    String text = document.getText(lineStart, lineEnd - lineStart);
    if ((text == null) || (text.length() == 0)) return location;

    location -= lineStart;

    int textLength = text.length();

    char locationChar = text.charAt(location);
    boolean isWhitespaceWord;
    if (isWhitespaceChar(locationChar)) isWhitespaceWord = true;
    else if (isWordChar(locationChar)) isWhitespaceWord = false;
    else return location + lineStart + 1;

    for (int i = location; i < textLength; i++) {
      char c = text.charAt(i);
      if ((isWhitespaceWord && !isWhitespaceChar(c)) || !isWhitespaceWord && !isWordChar(c))
        return lineStart + i;
    }

    return lineEnd;
  }

  /**
   * Returns <code>true</code> if the specified character should be considered a word character by
   * the text selection mechanism, <code>false</code> otherwise.
   */
  protected boolean isWordChar(char c) {
    return Character.isLetter(c) || Character.isDigit(c);
  }

  /**
   * Returns <code>true</code> if the specified character should be considered a whitespace
   * character by the text selection mechanism, <code>false</code> otherwise.
   */
  protected boolean isWhitespaceChar(char c) {
    return Character.isWhitespace(c);
  }

  /**
   * Expurgates the current selection by replacing all non-whitespace characters with asterisks.
   */
  protected void expurgateSelection() {
    String selection = getSelectedText();
    int selectionLength = selection.length();
    StringBuffer asterisks = new StringBuffer(selectionLength);
    for (int i = 0; i < selectionLength; i++) {
      char c = selection.charAt(i);
      if (Character.isWhitespace(c)) asterisks.append(c);
      else asterisks.append('*');
    }

    boolean isEditable = isEditable();
    setEditable(true);
    replaceSelection(asterisks.toString());
    setEditable(isEditable);
  }

  /**
   * Adds the given Link to this ConsoleTextPane, making the text between the starting index and the
   * ending index clickable.
   */
  public void addLink(Link link) {
    links.addElement(link);
  }

  /**
   * Removes all the links.
   */
  public void removeLinks() {
    links.removeAllElements();
  }

  /**
   * Due to a bug in MS VM, which never sends mouse events with clickCount more than 2, we're forced
   * to count clicks ourselves.
   */
  private int clickCount = 0;
  private long lastReleaseTime;

  /**
   * Updates the click count, if necessary.
   */
  private void updateClickCount(MouseEvent evt) {
    if (PlatformUtils.isOldMicrosoftVM()) {
      switch (evt.getID()) {
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_ENTERED:
          clickCount = 0;
          break;
        case MouseEvent.MOUSE_PRESSED:
          if (evt.getWhen() - lastReleaseTime < 500) clickCount++;
          else clickCount = 1;
          break;
        case MouseEvent.MOUSE_RELEASED:
          lastReleaseTime = evt.getWhen();
          break;
      }
    }
  }

  /**
   * Returns the click count for the specified mouse event.
   */
  private int getClickCount(MouseEvent evt) {
    return PlatformUtils.isOldMicrosoftVM() ? clickCount : evt.getClickCount();
  }

  /**
   * Processes the given MouseEvent.
   */
  @Override
  protected void processMouseEvent(MouseEvent evt) {
    updateClickCount(evt);

    // We want to handle events where clickCount >= 2 ourselves
    if (evt.getClickCount() < 2) super.processMouseEvent(evt);

    int pressedLoc = viewToModel(evt.getPoint());

    if (pressedLoc != -1) {
      try {
        if ((getClickCount(evt) >= 2)
            && (evt.getID() == MouseEvent.MOUSE_PRESSED)
            && SwingUtilities.isLeftMouseButton(evt)) {
          if ((getClickCount(evt) % 2) == 0) {
            int start = getWordStart(pressedLoc);
            int end = getWordEnd(pressedLoc);

            select(start, end);
          } else {
            Document document = getDocument();
            Element paragraphElement = Utilities.getParagraphElement(this, pressedLoc);
            int start = paragraphElement.getStartOffset();
            int end = Math.min(paragraphElement.getEndOffset(), document.getLength());

            select(start, end);
          }
        }
      } catch (BadLocationException e) {
      }
    }

    if (evt.isPopupTrigger()) {
      JPopupMenu popup = getPopupMenu(evt);
      if (popup != null) {
        Component rootPane = SwingUtilities.getRootPane(this);
        Dimension rootPaneSize = rootPane.getSize();
        Point clickPointOnRootPane = SwingUtilities.convertPoint(this, evt.getPoint(), rootPane);
        int x = evt.getX();
        int y = evt.getY();
        int width = popup.getWidth();
        int height = popup.getHeight();

        // The extra 1 pixel works around a bug where a mouse release event is
        // dispatched immediately (on the mouse release corresponding to the
        // mouse press which activated the popup) on the popup, activating an
        // item which wasn't really selected.
        if (clickPointOnRootPane.x + width + 1 > rootPaneSize.width) x = x - width - 1;
        else x += 1;

        if (clickPointOnRootPane.y + height - 5 > rootPaneSize.height) y = y - height + 5;
        else y -= 5;

        popup.setSelected(null);
        popup.show(this, x, y);
      }
    }

    if (evt.getID() == MouseEvent.MOUSE_EXITED) {
      curLink = null;
      setCursor(regCursor);
    }

    // We're not doing anything on a MOUSE_ENTERED event (although we should)
    // because if we do, and the user clicks in a popup directly over a link
    // we get a MOUSE_ENTERED event immediately followed by a MOUSE_CLICK
    // event (bug http://developer.java.sun.com/developer/bugParade/bugs/4119993.html probably)
    // and so we run the link.
    if (
    /*(evt.getID() == MouseEvent.MOUSE_ENTERED) || */
    (evt.getID() == MouseEvent.MOUSE_RELEASED)) {
      processPossibleLinkUpdate(evt);
    }

    if ((evt.getID() == MouseEvent.MOUSE_CLICKED) && SwingUtilities.isLeftMouseButton(evt)) {
      if (curLink != null) console.issueCommand(curLink.getCommand());
    }
  }

  /**
   * Processes the given Mouse(Motion)Event.
   */
  @Override
  protected void processMouseMotionEvent(MouseEvent evt) {
    updateClickCount(evt);

    super.processMouseMotionEvent(evt);

    if (evt.getID() == MouseEvent.MOUSE_MOVED) {
      processPossibleLinkUpdate(evt);
    }
  }

  /**
   * This method is called when a mouse event occurs that might change whether we're over a link.
   * This method determines whether it changed and updates all the variables.
   */
  private void processPossibleLinkUpdate(MouseEvent evt) {
    Link newLink = getLink(evt.getX(), evt.getY());

    if (newLink == null) {
      if (curLink != null) {
        curLink = null;
        setCursor(regCursor, false);
      }
    } else if (!newLink.equals(curLink)) {
      curLink = newLink;
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), false);
    }
  }

  /**
   * Returns the link at the given location, or null if none.
   */
  protected Link getLink(int x, int y) {
    int numLinks = links.size();
    for (int i = 0; i < numLinks; i++) {
      Link link = (Link) links.elementAt(i);
      int linkStart = link.getStartPosition().getOffset();
      int linkEnd = link.getEndPosition().getOffset();

      if (isOverText(x, y, linkStart, linkEnd)) return link;
    }

    return null;
  }

  /**
   * Returns <code>true</code> if the specified location (in pixels) is on a character between the
   * two specified positions in the text.
   */
  protected boolean isOverText(int x, int y, int startPos, int endPos) {
    Rectangle startCharRect, endCharRect;
    try {
      startCharRect = modelToView(startPos);
      endCharRect = modelToView(endPos);
    } catch (BadLocationException e) { // Shouldn't happen
      e.printStackTrace();
      return false;
    }

    // Sometimes modelToView returns null, not sure why.
    if ((startCharRect == null) || (endCharRect == null)) return false;

    if (startCharRect.y + startCharRect.height <= endCharRect.y) { // Separate lines.
      if (y > startCharRect.y) {
        if (y <= startCharRect.y + startCharRect.height) {
          if (x > startCharRect.x) {
            try {
              int lineEnd = Utilities.getRowEnd(this, startPos);
              if (lineEnd == -1) return false;

              Rectangle lineEndCharRect = modelToView(lineEnd);
              if (x <= lineEndCharRect.x + lineEndCharRect.width) return true;
            } catch (BadLocationException e) { // Shouldn't happen
              e.printStackTrace();
              return false;
            }
          }
        } else if (y < endCharRect.y) return true;
        else if ((y < endCharRect.y + endCharRect.height)
            && (x < endCharRect.x + endCharRect.width)) return true;
      }
      if ((y > startCharRect.y + startCharRect.height) && (y < endCharRect.y)) return true;
    } else { // Same line
      if (startCharRect.union(endCharRect).contains(x, y)) return true;
    }

    return false;
  }

  /**
   * Returns the tooltip to display.
   */
  @Override
  public String getToolTipText(MouseEvent evt) {
    Link link = getLink(evt.getX(), evt.getY());
    if (link != null) {
      Command command = link.getCommand();
      if (!command.isSpecial()) {
        String commandString = command.getCommandString();

        try {
          int startIndex = link.getStartPosition().getOffset();
          int length = link.getEndPosition().getOffset() - startIndex;
          String linkText = getText(startIndex, length);

          if (linkText.equals(commandString)) return null;
          else return commandString;
        } catch (BadLocationException e) {
        }
      }
    }

    return null;
  }

  /**
   * Overrides setCursor to save information about the regular cursor.
   */
  @Override
  public void setCursor(Cursor cursor) {
    setCursor(cursor, true);
  }

  /**
   * Sets the cursor to the given Cursor, if the given boolean value is true, saves the given cursor
   * into regCursor.
   */
  private void setCursor(Cursor cursor, boolean save) {
    super.setCursor(cursor);
    if (save) regCursor = cursor;
  }

  /**
   * Returns the block scroll amount.
   */
  @Override
  public int getScrollableBlockIncrement(Rectangle viewRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
      return super.getScrollableBlockIncrement(viewRect, orientation, direction);

    int scroll = viewRect.height - 3 * getScrollableUnitIncrement(viewRect, orientation, direction);
    return scroll <= 0 ? viewRect.height : scroll;
  }

  /**
   * Returns the unit scroll amount.
   */
  @Override
  public int getScrollableUnitIncrement(Rectangle viewRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
      return super.getScrollableUnitIncrement(viewRect, orientation, direction);

    FontMetrics metrics = GraphicsUtilities.getFontMetrics(getFont());
    return metrics.getHeight();
  }

  /**
   * A <code>JMenuItem</code> which, when activated, sends a command to the server consisting of the
   * current selection in the console appended to the specified string. Useful for the console
   * popup.
   */
  protected class IssueCommandMenuItem extends JMenuItem {

    /**
     * The server command to which the selection is appended.
     */
    private final String serverCommand;

    /**
     * Creates a new <code>IssueCommandMenuItem</code> with the specified label and server command.
     */
    public IssueCommandMenuItem(String label, String serverCommand) {
      super(label);

      this.serverCommand = serverCommand;
    }

    /**
     * Sends the command to the server.
     */
    @Override
    protected void fireActionPerformed(ActionEvent evt) {
      console.issueCommand(new Command(serverCommand + " " + getSelectedText(), 0));
    }
  }

  /**
   * A <code>JMenuItem</code> which, when activated, copies the current console selection to the
   * clipboard. Useful for the console popup.
   */
  protected class CopyMenuItem extends JMenuItem {

    /**
     * Creates a new <code>CopyMenuItem</code> with the specified label.
     */
    public CopyMenuItem(String label) {
      super(label);
    }

    /**
     * Copies the currently selected text in the console to the clipboard.
     */
    @Override
    protected void fireActionPerformed(ActionEvent evt) {
      copy();
    }
  }

  /**
   * A <code>JMenuItem</code> which sends the current console selection to the server, as a command.
   * Useful for the console popup.
   */
  protected class ExecuteMenuItem extends JMenuItem {

    /**
     * Creates a new <code>ExecuteMenuItem</code> with the specified label.
     */
    public ExecuteMenuItem(String label) {
      super(label);
    }

    /**
     * Sends the current console selection to the server, as a command.
     */
    @Override
    protected void fireActionPerformed(ActionEvent evt) {
      console.issueCommand(new Command("/" + getSelectedText(), 0));
    }
  }

  /**
   * A <code>JMenuItem</code> which expurgates the current selection in the console. Useful for the
   * console popup.
   */
  protected class ExpurgateMenuItem extends JMenuItem {

    /**
     * Creates a new <code>ExpurgateMenuItem</code> with the specified label.
     */
    public ExpurgateMenuItem(String label) {
      super(label);
    }

    /**
     * Expurgates the current selection.
     */
    @Override
    protected void fireActionPerformed(ActionEvent evt) {
      expurgateSelection();
    }
  }
}
