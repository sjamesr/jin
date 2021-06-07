/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.console;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.Preferences;
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.FriendsEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;
import free.jin.ui.SdiUiProvider;
import free.util.BrowserControl;
import free.util.PlatformUtils;
import free.util.swing.MultiButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jdesktop.layout.Baseline;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * A Component which implements a text console in which the user can see the output of the server
 * and write/send arbitrary commands to the server. This is a component that can be used by various
 * plugins - it's mainly used by free.jin.console.ConsoleManager.
 */
public class Console extends JPanel implements KeyListener {

  /** The <code>ConsoleManager</code> we're a part of. */
  private final ConsoleManager consoleManager;

  /** This console's designation. */
  private final ConsoleDesignation designation;

  /** The listener list. */
  protected final EventListenerList listenerList = new EventListenerList();

  /** The ConsoleTextPane where the output is displayed. */
  private final ConsoleTextPane outputComponent;

  /** The JScrollPane wrapping the output component. */
  private final JScrollPane outputScrollPane;

  /**
   * The command type component, which is either a JLabel (if there's only one command type) or a
   * JComboBox (if there are multiple command types).
   */
  private final JComponent commandTypeComponent;

  /** The ConsoleTextField which takes the input from the user. */
  private final ConsoleTextField inputComponent;

  /** The preferences of this console. */
  private final Preferences prefs;

  /** The regular expressions against which we match the text to find links. */
  private Pattern[] linkREs;

  /** The commands executed for the matched links. */
  private String[] linkCommands;

  /** The indices of the subexpression to make a link out of. */
  private int[] linkSubexpressionIndices;

  /** The regular expression we use for detecting URLs. */
  private static final Pattern URL_REGEX =
      Pattern.compile(
          "((([Ff][Tt][Pp]|[Hh][Tt][Tt][Pp]([Ss])?)://)|([Ww][Ww][Ww]\\.))([^\\s()<>\"])*[^\\s.,()<>\"'!?]");

  /** The regular expression we use for detecting emails. */
  private static final Pattern EMAIL_REGEX =
      Pattern.compile("[^\\s()<>\"\']+@[^\\s()<>\"]+\\.[^\\s.,()<>\"'?]+");

  /** Maps text types that were actually looked up to the resulting AttributeSets. */
  private final Hashtable attributesCache = new Hashtable();

  /** A history of people who have told us anything. */
  private final Vector tellers = new Vector();

  /**
   * The amount of times addToOutput was called. See {@see #addToOutput(String, String)} for the
   * hack involved.
   */
  private int numAddToOutputCalls = 0;

  /**
   * Whether the runnable that is supposed to scroll the scrollpane to the bottom already executed.
   * See {@see #addToOutput(String, String)} for the hack involved.
   */
  private boolean didScrollToBottom = true;

  /** An action which clears the console. */
  private final Action clearAction =
      new AbstractAction(I18n.get(Console.class).getString("clearAction.name")) {
        @Override
        public void actionPerformed(ActionEvent e) {
          clear();
        }
      };

  /** An action which closes the console. */
  private final Action closeAction =
      new AbstractAction(I18n.get(Console.class).getString("closeAction.name")) {
        @Override
        public void actionPerformed(ActionEvent e) {
          consoleManager.removeConsole(Console.this);
        }
      };

  /**
   * Creates a new <code>Console</code> with the specified designation, to be used in the specified
   * <code>ConsoleManager</code>.
   */
  public Console(ConsoleManager consoleManager, ConsoleDesignation designation) {
    this.consoleManager = consoleManager;
    this.designation = designation;

    this.prefs = consoleManager.getPrefs();

    this.outputComponent = createOutputComponent();
    configureOutputComponent(outputComponent);
    this.outputScrollPane = createOutputScrollPane(outputComponent);
    this.commandTypeComponent = createCommandTypeComponent();
    this.inputComponent = createInputComponent();

    KeyStroke clearAccelerator =
        KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    registerKeyboardAction(clearAction, clearAccelerator, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    clearAction.putValue(Action.ACCELERATOR_KEY, clearAccelerator);

    KeyStroke closeAccelerator =
        KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    registerKeyboardAction(closeAction, closeAccelerator, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    closeAction.putValue(Action.ACCELERATOR_KEY, closeAccelerator);

    closeAction.setEnabled(designation.isConsoleCloseable());

    createUI();

    outputComponent.addKeyListener(this);
    inputComponent.addKeyListener(this);

    if (designation.getCommandTypes().size() == 0) {
      inputComponent.setEnabled(false);
      inputComponent.setEditable(false);
    }

    setFocusable(false);

    init();
  }

  /** Returns this console's designation. */
  public ConsoleDesignation getDesignation() {
    return designation;
  }

  /** Creates the UI (layout) of this console. */
  private void createUI() {
    JComponent actionsComponent = createActionsComponent();

    JLabel colonLabel = new JLabel(":");

    JPanel bottomPanel = new JPanel();
    GroupLayout bottomPanelLayout = new GroupLayout(bottomPanel);
    bottomPanel.setLayout(bottomPanelLayout);

    GroupLayout.SequentialGroup hGroup = bottomPanelLayout.createSequentialGroup();
    GroupLayout.ParallelGroup vGroup = bottomPanelLayout.createParallelGroup(GroupLayout.BASELINE);

    // We add the command type component anyway to make the text field appear at
    // the same position (vertically) in different tabs.
    hGroup.add(
        commandTypeComponent,
        GroupLayout.PREFERRED_SIZE,
        GroupLayout.DEFAULT_SIZE,
        GroupLayout.PREFERRED_SIZE);
    if (designation.getCommandTypes().size() > 0) {
      hGroup
          .add(
              colonLabel,
              GroupLayout.PREFERRED_SIZE,
              GroupLayout.DEFAULT_SIZE,
              GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(LayoutStyle.RELATED);
    }
    hGroup
        .add(inputComponent, 0, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
        .addPreferredGap(LayoutStyle.UNRELATED)
        .add(
            actionsComponent,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE);

    vGroup.add(commandTypeComponent);
    if (designation.getCommandTypes().size() > 0) {
      vGroup.add(colonLabel);
    }
    vGroup.add(inputComponent).add(actionsComponent);

    bottomPanelLayout.setHorizontalGroup(hGroup);
    bottomPanelLayout.setVerticalGroup(vGroup);

    bottomPanel.setSize(bottomPanel.getPreferredSize());
    bottomPanel.doLayout();
    Rectangle inputBounds = inputComponent.getBounds();
    int topGap = inputBounds.y;
    int bottomGap = bottomPanel.getHeight() - (inputBounds.y + inputBounds.height);
    int vgap = Math.max(2, Math.max(topGap, bottomGap));

    // Hack to make room for the window resize handle
    if (PlatformUtils.isMacOSX() && (Jin.getInstance().getUIProvider() instanceof SdiUiProvider))
      bottomPanel.setBorder(
          BorderFactory.createEmptyBorder(vgap - topGap, 5, vgap - bottomGap, 18));
    else
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(vgap - topGap, 5, vgap - bottomGap, 5));

    setLayout(new BorderLayout());
    add(outputScrollPane, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);
  }

  /** Transfers the focus to the input component. */
  public void obtainFocus() {
    inputComponent.requestFocusInWindow();
  }

  /** Flashes the input field, attracting attention to it. */
  public void flashInputField() {
    inputComponent.setBackground(Color.red);

    Timer timer =
        new Timer(
            300,
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                inputComponent.setBackground(UIManager.getColor("TextField.background"));
              }
            });
    timer.setRepeats(false);
    timer.start();
  }

  /** Returns the preferences. */
  public Preferences getPrefs() {
    return prefs;
  }

  /** Returns the <code>ConsoleManager</code> a part of which we are. */
  public ConsoleManager getConsoleManager() {
    return consoleManager;
  }

  /**
   * Creates the component which allows the user to perform certain actions on the console (such as
   * close, clear...).
   */
  protected JComponent createActionsComponent() {
    MultiButton button = new MultiButton(I18n.get(Console.class).getString("actionsButton.text"));

    button.add(inputComponent.getSendAction());
    button.add(clearAction);
    button.add(closeAction);

    return button;
  }

  /** Creates the <code>ConsoleTextPane</code> to which the server's textual output goes. */
  protected ConsoleTextPane createOutputComponent() {
    return new ConsoleTextPane(this);
  }

  /** Configures the output component to be used with this console. */
  protected void configureOutputComponent(final ConsoleTextPane textPane) {
    // Seriously hack the caret for our own purposes (desired scrolling and selecting).
    Caret caret =
        new DefaultCaret() {

          @Override
          public void focusGained(FocusEvent evt) {
            super.focusGained(evt);
            if (!dragging) obtainFocus();
          }

          @Override
          public void focusLost(FocusEvent e) {
            this.setVisible(false);
          }

          @Override
          protected void adjustVisibility(Rectangle nloc) {
            if (!dragging) return;

            if (SwingUtilities.isEventDispatchThread()) {
              textPane.scrollRectToVisible(nloc);
              if (nloc.y + nloc.height > textPane.getSize().height - nloc.height / 2) {
                BoundedRangeModel scrollModel = outputScrollPane.getVerticalScrollBar().getModel();
                scrollModel.setValue(scrollModel.getMaximum());
              }
            } else {
              super.adjustVisibility(nloc); // Just in case... shouldn't happen.
            }
          }

          private boolean dragging = false;

          @Override
          public void mousePressed(MouseEvent e) {
            dragging = true;
            super.mousePressed(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            dragging = false;
            super.mouseReleased(e);
            if (isCopyOnSelect()) {
              SwingUtilities.invokeLater(
                  new Runnable() {
                    @Override
                    public void run() {
                      obtainFocus();
                    }
                  });
            }
          }

          @Override
          protected void moveCaret(MouseEvent e) {
            Point pt = new Point(e.getX(), e.getY());
            Position.Bias[] biasRet = new Position.Bias[1];
            int pos = textPane.getUI().viewToModel(textPane, pt, biasRet);
            if (pos >= 0) {
              int maxPos = textPane.getDocument().getEndPosition().getOffset();
              if ((maxPos == pos + 1) && (pos > 0)) {
                pos--;
                moveDot(pos);
                if (dragging) {
                  BoundedRangeModel scrollModel =
                      outputScrollPane.getVerticalScrollBar().getModel();
                  scrollModel.setValue(scrollModel.getMaximum());
                }
              } else moveDot(pos);
            }
          }

          @Override
          protected void positionCaret(MouseEvent e) {
            Point pt = new Point(e.getX(), e.getY());
            Position.Bias[] biasRet = new Position.Bias[1];
            int pos = textPane.getUI().viewToModel(textPane, pt, biasRet);
            if (pos >= 0) {
              int maxPos = textPane.getDocument().getEndPosition().getOffset();
              if ((maxPos == pos + 1) && (pos > 0)) {
                pos--;
                setDot(pos);
                if (dragging) {
                  BoundedRangeModel scrollModel =
                      outputScrollPane.getVerticalScrollBar().getModel();
                  scrollModel.setValue(scrollModel.getMaximum());
                }
              } else setDot(pos);
            }
          }
        };

    caret.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent evt) {
            if (isCopyOnSelect()) textPane.copy(); // CDE/Motif style copy/paste
          }
        });

    textPane.setCaret(caret);
  }

  /**
   * The JViewport we use as the viewport for the scrollpane of the output component. This class
   * being the viewport makes sure that when a console is resized, the currently displayed text
   * remains such. The anchor is the last currently visible character.
   */
  protected class OutputComponentViewport extends JViewport {

    // Used to avoid endless recursion
    private boolean settingViewSize = false;

    // This makes sure that when the viewport is resized, the last visible line
    // (or character) remains the same after the resize.
    @Override
    public void reshape(int x, int y, int width, int height) {
      Dimension viewSize = getViewSize();
      Dimension viewportSize = getExtentSize();
      JTextComponent view = (JTextComponent) getView();

      if ((viewSize.height <= height)
          || (viewportSize.height < 0)
          || settingViewSize
          || ((width == this.getWidth()) && (height == this.getHeight()))
          || (view.getDocument().getLength() == 0)) {
        super.reshape(x, y, width, height);
        return;
      }

      Point viewPosition = getViewPosition();
      Point viewCoords =
          new Point(viewportSize.width + viewPosition.x, viewportSize.height + viewPosition.y);
      int lastVisibleIndex = view.viewToModel(viewCoords);

      super.reshape(x, y, width, height);

      settingViewSize = true;
      this.doLayout();
      this.validate();
      settingViewSize = false;
      // Otherwise the viewport doesn't update what it thinks about the size of
      // the view and may thus scroll to the wrong location.

      try {
        Dimension newViewportSize = getExtentSize();
        Rectangle lastVisibleIndexPosition = view.modelToView(lastVisibleIndex);
        if (lastVisibleIndexPosition != null) {
          setViewPosition(
              new Point(
                  0,
                  Math.max(
                      0,
                      lastVisibleIndexPosition.y
                          + lastVisibleIndexPosition.height
                          - 1
                          - newViewportSize.height)));
        }
      } catch (BadLocationException e) {
      }
    }
  }

  /** Creates the JScrollPane in which the output component will be put. */
  protected JScrollPane createOutputScrollPane(JTextPane outputComponent) {
    JViewport viewport = new OutputComponentViewport();
    viewport.setView(outputComponent);

    JScrollPane scrollPane =
        new JScrollPane(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setViewport(viewport);

    viewport.putClientProperty("EnableWindowBlit", Boolean.TRUE);

    scrollPane.setBorder(null);

    return scrollPane;
  }

  /** Creates the component which lets the user choose the command type to execute his command. */
  private JComponent createCommandTypeComponent() {
    java.util.List commandTypes = designation.getCommandTypes();

    JComboBox box = new JComboBox(commandTypes.toArray());
    box.setEditable(false);
    box.setFocusable(false);
    Dimension boxPrefSize = box.getPreferredSize();

    JLabel label =
        (commandTypes.size() > 0) ? new JLabel(commandTypes.get(0).toString()) : new JLabel(" ");
    if (commandTypes.size() > 0) label.putClientProperty("commandType", commandTypes.get(0));
    Dimension labelPrefSize = label.getPreferredSize();

    // All the stuff below is to make sure that the various components align
    // properly and the console's input field doesn't "move" when switching
    // between tabs
    int height = Math.max(boxPrefSize.height, labelPrefSize.height);

    int boxBaseline = Baseline.getBaseline(box, 50, height);
    int labelBaseline = Baseline.getBaseline(label, 50, height);
    int maxBaseline = Math.max(boxBaseline, labelBaseline);

    if (commandTypes.size() <= 1) {
      label.setPreferredSize(new Dimension(labelPrefSize.width, height));
      label.setMinimumSize(label.getPreferredSize());
      if (maxBaseline > labelBaseline)
        label.setBorder(BorderFactory.createEmptyBorder(maxBaseline - labelBaseline, 0, 0, 0));
      return label;
    } else {
      box.setPreferredSize(new Dimension(boxPrefSize.width, height));
      box.setMinimumSize(box.getPreferredSize());
      if (maxBaseline > boxBaseline)
        box.setBorder(BorderFactory.createEmptyBorder(maxBaseline - boxBaseline, 0, 0, 0));
      return box;
    }
  }

  /** Returns the currently selected command type; <code>null</code> if none. */
  private ConsoleDesignation.CommandType getSelectedCommandType() {
    if (commandTypeComponent instanceof JLabel) {
      JLabel label = (JLabel) commandTypeComponent;
      return (ConsoleDesignation.CommandType) label.getClientProperty("commandType");
    } else if (commandTypeComponent instanceof JComboBox) {
      JComboBox box = (JComboBox) commandTypeComponent;
      return (ConsoleDesignation.CommandType) box.getSelectedItem();
    } else return null;
  }

  /**
   * Creates the text field in which the user can input commands to be sent to the server. This
   * method may be overridden by server-specific subclasses to return their own version of a console
   * text field.
   */
  protected ConsoleTextField createInputComponent() {
    return new ConsoleTextField(this);
  }

  /**
   * Initializes this console, loading all the properties from the plugin, etc. The Console uses the
   * Plugin's (and the User's properties) to determine its various properties (text color etc.)
   */
  private void init() {
    attributesCache.clear(); // Clear the cache

    /********************* OUTPUT COMPONENT ***********************/
    String backgroundType = prefs.getString("background.type", "color");

    if ("color".equals(backgroundType)) {
      // We set it here because of a Swing bug which causes the background to be
      // drawn with the foreground color if you set the background as an attribute.
      Color outputBg = prefs.getColor("background", null);
      if (outputBg != null) outputComponent.setBackground(outputBg);
    } else if ("pattern".equals(backgroundType)) {
      Image image = getToolkit().getImage(Console.class.getResource("background.png"));
      outputComponent.setBackgroundPattern(image);
    }

    Color outputSelection = prefs.getColor("output-selection", null);
    if (outputSelection != null) outputComponent.setSelectionColor(outputSelection);

    Color outputSelected = prefs.getColor("output-selected", null);
    if (outputSelected != null) outputComponent.setSelectedTextColor(outputSelected);

    /********************* INPUT COMPONENT *************************/
    Color inputBg = prefs.getColor("input-background", null);
    if (inputBg != null) inputComponent.setBackground(inputBg);

    Color inputFg = prefs.getColor("input-foreground", null);
    if (inputFg != null) inputComponent.setForeground(inputFg);

    Color inputSelection = prefs.getColor("input-selection", null);
    if (inputSelection != null) inputComponent.setSelectionColor(inputSelection);

    Color inputSelected = prefs.getColor("input-selected", null);
    if (inputSelected != null) inputComponent.setSelectedTextColor(inputSelected);

    int numLinkPatterns = prefs.getInt("output-link.num-patterns", 0);
    linkREs = new Pattern[numLinkPatterns];
    linkCommands = new String[numLinkPatterns];
    linkSubexpressionIndices = new int[numLinkPatterns];
    for (int i = 0; i < numLinkPatterns; i++) {
      try {
        String linkPattern = prefs.getString("output-link.pattern-" + i);
        String linkCommand = prefs.getString("output-link.command-" + i);
        int subexpressionIndex = prefs.getInt("output-link.index-" + i);

        linkSubexpressionIndices[i] = subexpressionIndex;
        Pattern regex = Pattern.compile(linkPattern);
        linkREs[i] = regex;
        linkCommands[i] = linkCommand;
      } catch (PatternSyntaxException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Refreshes the console by re-reading the plugin/user properties and adjusting the assosiated
   * console properties accordingly. This is useful to call after a user changes the preferences.
   */
  public void refreshFromProperties() {
    init();
    outputComponent.refreshFromProperties();
    inputComponent.refreshFromProperties();
  }

  /** Returns whether text will be copied into the clipboard on selection. */
  protected boolean isCopyOnSelect() {
    return prefs.getBool("copyOnSelect", true);
  }

  /**
   * This method <B>must</B> be called before adding anything to the output component. This method
   * works together with the <code>assureScrolling</code> method.
   *
   * @returns whether the <code>assureScrolling</code> method should scroll the scrollpane of the
   *     output component to the bottom. This needs to be passed to the <code>assureScrolling</code>
   *     method.
   */
  protected final boolean prepareAdding() {
    // Seriously hack the scrolling to make sure if we're at the bottom, we stay there,
    // and if not, we stay there too :-)
    numAddToOutputCalls++;
    outputScrollPane
        .getViewport()
        .putClientProperty(
            "EnableWindowBlit", Boolean.FALSE); // Adding a lot of text is slow with blitting
    BoundedRangeModel verticalScroll = outputScrollPane.getVerticalScrollBar().getModel();

    return (verticalScroll.getMaximum()
        <= verticalScroll.getValue() + verticalScroll.getExtent() + 5);
    // The +5 is to scroll it to the bottom even if it's a couple of pixels away.
    // This can happen if you try to scroll to the bottom programmatically
    // (a bug probably) using scrollRectToVisible(Rectangle).
  }

  /**
   * This method <B>must</B> be called after adding anything to the output component. This method
   * works together with the <code>prepareAdding</code> method. Pass the value returned by <code>
   * prepareAdding</code> as the argument of this method.
   */
  protected final void assureScrolling(boolean scrollToBottom) {
    class BottomScroller implements Runnable {

      private int curNumCalls;

      BottomScroller(int curNumCalls) {
        this.curNumCalls = curNumCalls;
      }

      @Override
      public void run() {
        if (numAddToOutputCalls == curNumCalls) {
          try {
            int lastOffset = outputComponent.getDocument().getEndPosition().getOffset();
            Rectangle lastCharRect = outputComponent.modelToView(lastOffset - 1);
            if (lastCharRect != null) outputComponent.scrollRectToVisible(lastCharRect);
          } catch (BadLocationException e) {
            e.printStackTrace();
          }

          didScrollToBottom = true;

          // Enable blitting again
          outputScrollPane.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);

          outputComponent.repaint();
        } else {
          curNumCalls = numAddToOutputCalls;
          SwingUtilities.invokeLater(this);
        }
      }
    }

    if (scrollToBottom && didScrollToBottom) {
      // This may be false if the frame containing us (for example), is iconified
      if (isDisplayable()) {
        didScrollToBottom = false;
        SwingUtilities.invokeLater(new BottomScroller(numAddToOutputCalls));
      }
    }
  }

  /** Adds the given component to the output. */
  public void addToOutput(JComponent component) {
    boolean shouldScroll = prepareAdding();

    boolean wasEditable = outputComponent.isEditable();
    outputComponent.setEditable(true);
    outputComponent.setCaretPosition(outputComponent.getDocument().getLength());
    StyledDocument document = outputComponent.getStyledDocument();
    outputComponent.insertComponent(component);

    // See http://developer.java.sun.com/developer/bugParade/bugs/4353673.html
    LayoutManager layout = component.getParent().getLayout();
    if (layout instanceof OverlayLayout)
      ((OverlayLayout) layout).invalidateLayout(component.getParent());

    try {
      document.insertString(document.getLength(), "\n", null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    outputComponent.setEditable(wasEditable);

    assureScrolling(shouldScroll);
  }

  /**
   * Adds the specified text of the specified type to the console. The event type is a string which
   * is used to look up (in the preferences) the properties (font, color etc.) of the text when
   * displayed in the console.
   *
   * @see #textTypeForEvent(JinEvent)
   */
  public void addToOutput(String text, String textType) {
    try {
      boolean shouldScroll = prepareAdding();
      addToOutputImpl(text, textType);
      assureScrolling(shouldScroll);
    } catch (BadLocationException e) {
      e.printStackTrace(); // Why the heck is this checked?
    }
  }

  /** Adds the default representation of the specified <code>ChatEvent</code> to the console. */
  public void addToOutput(ChatEvent evt, String encoding) {
    addToOutput(consoleManager.getDefaultTextForChat(evt, encoding), textTypeForEvent(evt));
  }

  /**
   * Returns the text type which should be used when adding text for the specified event to the
   * console. The returned text type is a recommendation - if there is a good reason to use a
   * different type, you may do so. For <code>PlainTextEvent</code>, the method returns the string
   * "plain". For <code>ChatEvent</code>s, it returns the concatenation of the chat type, chat forum
   * and sender name, separated by dots (using an empty string if one of the properties is missing).
   * Other events are unsupported.
   */
  public String textTypeForEvent(JinEvent evt) {
    if (evt == null) throw new NullPointerException();

    if (evt instanceof PlainTextEvent) return "plain";
    else if (evt instanceof ChatEvent) {
      ChatEvent chatEvent = (ChatEvent) evt;

      String type = chatEvent.getType();
      Object forum = chatEvent.getForum();
      ServerUser sender = chatEvent.getSender();

      return type
          + "."
          + (forum == null ? "" : forum.toString())
          + "."
          + (sender == null ? "" : sender.getName());
    } else if (evt instanceof FriendsEvent) {
      return "friendsNotification";
    } else throw new IllegalArgumentException("Unsupported event: " + evt);
  }

  /** Returns the text type for user-typed text. */
  public String getUserTextType() {
    return "user";
  }

  /** Actually does the work of adding the given text to the output component's Document. */
  protected void addToOutputImpl(String text, String textType) throws BadLocationException {
    StyledDocument document = outputComponent.getStyledDocument();
    int oldTextLength = document.getLength();

    document.insertString(document.getLength(), text + "\n", attributesForTextType(textType));

    AttributeSet urlAttributes = attributesForTextType("link.url");
    AttributeSet emailAttributes = attributesForTextType("link.email");
    AttributeSet commandAttributes = attributesForTextType("link.command");

    Matcher urlMatcher = URL_REGEX.matcher(text);
    while (urlMatcher.find()) {
      int matchStart = urlMatcher.start();
      int matchEnd = urlMatcher.end();

      Command command =
          new Command(
              "url " + text.substring(matchStart, matchEnd),
              Command.SPECIAL_MASK | Command.BLANKED_MASK);
      Position linkStart = document.createPosition(matchStart + oldTextLength);
      Position linkEnd = document.createPosition(matchEnd + oldTextLength);
      Link link = new Link(linkStart, linkEnd, command);
      document.setCharacterAttributes(
          matchStart + oldTextLength, matchEnd - matchStart, urlAttributes, false);
      outputComponent.addLink(link);
    }

    Matcher emailMatcher = EMAIL_REGEX.matcher(text);
    while (emailMatcher.find()) {
      int matchStart = emailMatcher.start();
      int matchEnd = emailMatcher.end();

      Command command =
          new Command(
              "email " + text.substring(matchStart, matchEnd),
              Command.SPECIAL_MASK | Command.BLANKED_MASK);
      Position linkStart = document.createPosition(matchStart + oldTextLength);
      Position linkEnd = document.createPosition(matchEnd + oldTextLength);
      Link link = new Link(linkStart, linkEnd, command);
      document.setCharacterAttributes(
          matchStart + oldTextLength, matchEnd - matchStart, emailAttributes, false);
      outputComponent.addLink(link);
    }

    for (int i = 0; i < linkREs.length; i++) {
      Pattern linkRE = linkREs[i];

      if (linkRE == null) // Bad pattern was given in properties.
      continue;

      Matcher linkMatcher = linkRE.matcher(text);
      while (linkMatcher.find()) {
        String linkCommand = linkCommands[i];

        int index = -1;
        while ((index = linkCommand.indexOf("$", index + 1)) != -1) {
          if ((index < linkCommand.length() - 1)
              && Character.isDigit(linkCommand.charAt(index + 1))) {
            int subexpressionIndex = Character.digit(linkCommand.charAt(index + 1), 10);
            linkCommand =
                linkCommand.substring(0, index)
                    + linkMatcher.group(subexpressionIndex)
                    + linkCommand.substring(index + 2);
          }
        }

        int linkSubexpressionIndex = linkSubexpressionIndices[i];
        int matchStart = linkMatcher.start(linkSubexpressionIndex);
        int matchEnd = linkMatcher.end(linkSubexpressionIndex);

        document.setCharacterAttributes(
            matchStart + oldTextLength, matchEnd - matchStart, commandAttributes, false);

        Position linkStart = document.createPosition(matchStart + oldTextLength);
        Position linkEnd = document.createPosition(matchEnd + oldTextLength);
        Link link = new Link(linkStart, linkEnd, new Command("/" + linkCommand, 0));
        outputComponent.addLink(link);
      }
    }
  }

  /**
   * Executes a special command. The following commands are recognized by this method:
   *
   * <UL>
   *   <LI>cls - Removes all text from the console.
   *   <LI>"url <url>" - Displays the URL (the '<' and '>' don't actually appear in the string).
   *   <LI>"email <email address>" - Displays the mailer with the "To" field set to the given email
   *       address.
   * </UL>
   */
  protected void executeSpecialCommand(String command) {
    command = command.trim();
    if (command.equalsIgnoreCase("cls")) {
      clear();
    } else if (command.startsWith("url ")) {
      String urlString = command.substring("url ".length());

      // A www. string
      if (urlString.substring(0, Math.min(4, urlString.length())).equalsIgnoreCase("www."))
        urlString = "http://" + urlString; // Assume http

      if (!BrowserControl.displayURL(urlString))
        BrowserControl.showDisplayBrowserFailedDialog(urlString, this, true);
    } else if (command.startsWith("email ")) {
      String emailString = command.substring("email ".length());
      if (!BrowserControl.displayMailer(emailString))
        BrowserControl.showDisplayMailerFailedDialog(emailString, this, true);
    } else {
      String message =
          I18n.get(Console.class)
              .getFormattedString("unknownSpecialCommandMessage", new Object[] {command});
      addToOutput(message, "info");
    }
  }

  /** Executes the given command. */
  void issueCommand(Command command) {
    String commandString = command.getCommandString();

    if (command.isSpecial()) {
      executeSpecialCommand(commandString);
      if (!command.isBlanked()) addToOutput(commandString, "user");
    } else {
      Connection conn = consoleManager.getConn();
      if (!conn.isConnected())
        addToOutput(I18n.get(Console.class).getString("unconnectedWarningMessage"), "info");
      else {
        ConsoleDesignation.CommandType commandType = getSelectedCommandType();
        if (commandType != null) commandType.handleCommand(commandString, command.isBlanked());
        else getToolkit().beep();
      }
    }
  }

  /** Removes all text from the console. */
  public void clear() {
    outputComponent.setText("");
    outputComponent.removeAll();
    outputComponent.removeLinks();
  }

  /**
   * Gets called when a tell by the given player is received. This method saves the name of the
   * sender so it can be later retrieved when the keyboard shortcut to reply is activated.
   */
  public void personalTellReceived(ServerUser sender) {
    tellers.removeElement(sender);
    tellers.insertElementAt(sender, 0);
    if (tellers.size() > getTellerRingSize()) tellers.removeElementAt(tellers.size() - 1);
  }

  /**
   * Returns the size of the teller ring, the amount of last players who told us something we
   * traverse.
   */
  public int getTellerRingSize() {
    return prefs.getInt("teller-ring-size", 5);
  }

  /**
   * Returns the nth (from the end) person who told us something via "tell", "say" or "atell" which
   * went into this console. Returns <code>null</code> if no such person exists. The index is 0
   * based. Sorry about the name of the method but I didn't think getColocutor() was much better :-)
   */
  public ServerUser getTeller(int n) {
    if ((n < 0) || (n >= tellers.size())) return null;

    return (ServerUser) tellers.elementAt(n);
  }

  /** Returns the amount of people who have told us anything so far. */
  public int getTellerCount() {
    return tellers.size();
  }

  /**
   * Returns the AttributeSet for the given type of output text. Due to a bug in Swing, this method
   * does not address the background color.
   */
  protected AttributeSet attributesForTextType(String textType) {
    AttributeSet attributes = (AttributeSet) attributesCache.get(textType);
    if (attributes != null) return attributes;

    String fontFamily = (String) prefs.lookup("font-family." + textType, "Monospaced");
    Integer fontSize = (Integer) prefs.lookup("font-size." + textType, new Integer(14));
    Boolean bold = (Boolean) prefs.lookup("font-bold." + textType, Boolean.FALSE);
    Boolean italic = (Boolean) prefs.lookup("font-italic." + textType, Boolean.FALSE);
    Boolean underline = (Boolean) prefs.lookup("font-underlined." + textType, Boolean.FALSE);
    Color foreground = (Color) prefs.lookup("foreground." + textType, Color.white);

    SimpleAttributeSet mAttributes = new SimpleAttributeSet();
    mAttributes.addAttribute(StyleConstants.FontFamily, fontFamily);
    mAttributes.addAttribute(StyleConstants.FontSize, fontSize);
    mAttributes.addAttribute(StyleConstants.Bold, bold);
    mAttributes.addAttribute(StyleConstants.Italic, italic);
    mAttributes.addAttribute(StyleConstants.Underline, underline);
    mAttributes.addAttribute(StyleConstants.Foreground, foreground);
    //    StyleConstants.setFontFamily(mAttributes, fontFamily);
    //    StyleConstants.setFontSize(mAttributes, fontSize);
    //    StyleConstants.setBold(mAttributes, bold);
    //    StyleConstants.setItalic(mAttributes, italic);
    //    StyleConstants.setUnderline(mAttributes, underlined);
    //    StyleConstants.setForeground(mAttributes, foreground);
    attributesCache.put(textType, mAttributes);

    return mAttributes;
  }

  /**
   * Processes Key pressed events from the components we're registered as listeners for. The default
   * implementation is registered to listen to the input component.
   */
  @Override
  public void keyPressed(KeyEvent evt) {
    int keyCode = evt.getKeyCode();
    if ((evt.getSource() == inputComponent)) {
      if (evt.getID() == KeyEvent.KEY_PRESSED) {
        JScrollBar vscrollbar = outputScrollPane.getVerticalScrollBar();
        Rectangle viewRect = outputScrollPane.getViewport().getViewRect();
        int value = vscrollbar.getValue();

        if (evt.getModifiers() == 0) {
          switch (keyCode) {
            case KeyEvent.VK_PAGE_UP: // Page Up
              vscrollbar.setValue(
                  value
                      - outputComponent.getScrollableBlockIncrement(
                          viewRect, SwingConstants.VERTICAL, -1));
              break;
            case KeyEvent.VK_PAGE_DOWN: // Page Down
              vscrollbar.setValue(
                  value
                      + outputComponent.getScrollableBlockIncrement(
                          viewRect, SwingConstants.VERTICAL, +1));
              break;
          }
        }

        if (evt.isControlDown()) {
          switch (keyCode) {
            case KeyEvent.VK_UP: // Ctrl-Up
              vscrollbar.setValue(
                  value
                      - outputComponent.getScrollableUnitIncrement(
                          viewRect, SwingConstants.VERTICAL, -1));
              break;
            case KeyEvent.VK_DOWN: // Ctrl-Down
              vscrollbar.setValue(
                  value
                      + outputComponent.getScrollableUnitIncrement(
                          viewRect, SwingConstants.VERTICAL, +1));
              break;
              // These get in the way of the examined board shortcuts
              //            case KeyEvent.VK_HOME: // Ctrl-Home
              //              vscrollbar.setValue(vscrollbar.getMinimum());
              //              break;
              //            case KeyEvent.VK_END: // Ctrl-End
              //              vscrollbar.setValue(vscrollbar.getMaximum() -
              // vscrollbar.getVisibleAmount());
              //              break;
          }
        }
      }
    }
  }

  /**
   * Processes Key released events from the components we're registered as listeners for. The
   * default implementation is registered to listen to the output and to the input component.
   */
  @Override
  public void keyReleased(KeyEvent evt) {}

  /**
   * Processes Key typed events from the components we're registered as listeners for. The default
   * implementation is registered to listen to the output and to the input component.
   */
  @Override
  public void keyTyped(KeyEvent evt) {}
}
