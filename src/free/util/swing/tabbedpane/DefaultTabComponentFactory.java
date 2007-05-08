package free.util.swing.tabbedpane;

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;



/**
 * The default <code>TabComponentFactory<code> implementation.
 */

public class DefaultTabComponentFactory implements TabComponentFactory{
  
  
  
  /**
   * The shared instance of this class.
   */
  
  private static final DefaultTabComponentFactory SHARED_INSTANCE = new DefaultTabComponentFactory();
  
  
  
  /**
   * Returns an instance of this class.
   */
  
  public static DefaultTabComponentFactory getInstance(){
    return SHARED_INSTANCE;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public JComponent makeCompactRowComponent(TabbedPane tabbedPane, Tab tab){
    JMenuItem menuItem = new JMenuItem();
    
    menuItem.setText(tab.getTitle());
    menuItem.setIcon(tab.getIcon());
    
    return menuItem;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public AbstractButton makeMainRowComponent(TabbedPane tabbedPane, Tab tab){
    JButton button = new JButton();
    
    button.setText(tab.getTitle());
    button.setIcon(tab.getIcon());
    button.setContentAreaFilled(false);
    button.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    
    button.setMinimumSize(new Dimension(20, button.getMinimumSize().height));
//    button.setPreferredSize(new Dimension(100, button.getPreferredSize().height));
    
    return button;
  }
  
  
  
}
