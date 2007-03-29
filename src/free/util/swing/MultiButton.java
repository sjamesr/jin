package free.util.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;



/**
 * Implements a button which allows the user to invoke multiple actions.
 */

public class MultiButton extends JComponent{
  
  
  
  /**
   * The button we employ for the action user interaction.
   */
  
  private final JButton button;
  
  
  
  /**
   * The popup we display to allow the user to create 
   */
  
  private final JPopupMenu popup = new JPopupMenu();
  
  
  
  /**
   * The action listener for menu items.
   */
  
  private final ActionListener menuItemActionListener = new ActionListener(){
    public void actionPerformed(ActionEvent e){
      JMenuItem source = (JMenuItem)e.getSource();
      setButtonAction(source.getAction());
    }
  };
  
  
  
  /**
   * Creates a new <code>MultiButton</code>.
   */
  
  public MultiButton(){
    button = new JButton();
    button.setHorizontalTextPosition(SwingConstants.LEADING);
    
    // We should support the keyboard, but we don't, yet
    button.setFocusable(false);
    
    createUI();
    
    button.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent evt){
        popup.show(MultiButton.this, 0, getHeight());
      }
      public void mouseReleased(MouseEvent evt){
        popup.setVisible(false);
      }
    });
  }
  
  
  
  /**
   * Creates the UI of this <code>MultiButton</code>.
   */
  
  private void createUI(){
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, button);
  }
  
  
  
  /**
   * Adds the specified action to this <code>MultiButton</code>.
   */
  
  public void add(Action action){
    if (popup.getComponentCount() == 0)
      setButtonAction(action);
    
    JMenuItem menuItem = popup.add(action);
    menuItem.addActionListener(menuItemActionListener);
  }
  
  
  
  /**
   * Sets the button's action.
   */
  
  private void setButtonAction(Action action){
    button.setAction(action);
    
    if (isDisplayable())
      button.setIcon(createArrowIcon()); // Override the action's icon
  }
  
  
  
  /**
   * Overrides <code>addNotify()</code> to add an arrow icon of the correct size
   * (which is not known until we are made displayable).
   */
  
  public void addNotify(){
    super.addNotify();
    
    button.setIcon(createArrowIcon());
  }
  
  
  
  /**
   * Creates the arrow icon used to indicate that we display a drop-down menu.
   */
  
  private Icon createArrowIcon(){
    int dotsPerInch = getToolkit().getScreenResolution();
    int fontSizePixels = (int)(getFont().getSize2D() * dotsPerInch / 72);
    return new ArrowIcon(fontSizePixels/2, getForeground());
  }
  
  
  
}
