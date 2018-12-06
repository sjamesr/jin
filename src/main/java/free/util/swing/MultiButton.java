package free.util.swing;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Implements a button which allows the user to invoke multiple actions.
 */
public class MultiButton extends JComponent {

  /**
   * The menu which is the actual component we're displaying.
   */
  private JMenu menu;

  /**
   * Whether we're using the unicode "BLACK DOWN-POINTING SMALL TRIANGLE" character. If
   * <code>false</code>, we're using <code>ArrowIcon</code>.
   */
  private boolean usingUnicodeArrow = false;

  /**
   * Creates a new <code>MultiButton</code> with the specified text and list of actions.
   */
  public MultiButton(String text, Action[] actions) {
    menu = new JMenu();
    menu.setBackground(UIManager.getColor("Panel.background"));
    menu.setBorder(null);

    setText(text);

    // We should support the keyboard, but we don't, yet
    menu.setFocusable(false);

    createUI();

    for (int i = 0; i < actions.length; i++) add(actions[i]);
  }

  /**
   * Creates a new <code>MultiButton</code> with the specified text and initially no actions.
   */
  public MultiButton(String text) {
    this(text, new Action[0]);
  }

  /**
   * Creates a new <code>MultiButton</code> with initially no text nor actions.
   */
  public MultiButton() {
    this(null, new Action[0]);
  }

  /**
   * Sets the text of this button.
   */
  public void setText(String text) {
    if (usingUnicodeArrow) menu.setText(text + " \u25BE");
    else menu.setText(text);
  }

  /**
   * Returns the text of this button.
   */
  public String getText() {
    String text = menu.getText();
    if (usingUnicodeArrow) return text.substring(0, text.length() - 2);
    else return text;
  }

  /**
   * Creates the UI of this <code>MultiButton</code>.
   */
  private void createUI() {
    JMenuBar menubar = new JMenuBar();
    menubar.setOpaque(false);
    menubar.setBorder(null);

    menubar.add(menu);

    setLayout(WrapLayout.getInstance());
    add(menubar);
  }

  /**
   * Adds the specified action to this <code>MultiButton</code>. A <code>null</code> action stands
   * for a separator.
   */
  public void add(Action action) {
    if (action == null) menu.addSeparator();
    else menu.add(action);
  }

  /**
   * Overrides <code>addNotify()</code> to add an arrow icon of the correct size (which is not known
   * until we are made displayable).
   */
  @Override
  public void addNotify() {
    super.addNotify();

    Font font = menu.getFont();
    if (font.canDisplay('\u25BE')) {
      String text = getText();
      usingUnicodeArrow = true;
      setText(text);
    } else {
      menu.setHorizontalTextPosition(SwingConstants.LEADING);
      menu.setIcon(createArrowIcon());
    }
  }

  /**
   * Creates the arrow icon used to indicate that we display a drop-down menu.
   */
  private Icon createArrowIcon() {
    int dotsPerInch = getToolkit().getScreenResolution();
    int fontSizePixels = (int) (getFont().getSize2D() * dotsPerInch / 72);
    return new ArrowIcon(fontSizePixels / 2, getForeground());
  }
}
