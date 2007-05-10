/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing.tabbedpane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.util.swing.SwingUtils;



/**
 * The default implementation of <code>TabHandle</code>.
 */

public class DefaultTabHandle implements TabHandle{
  
  
  
  /**
   * The close button icon. 
   */
  
  private static final Icon CLOSE_ICON_NORMAL = new ImageIcon(DefaultTabHandle.class.getResource("close.png"));
  
  
  
  /**
   * The rollover version of the close button icon.
   */
  
  private static final Icon CLOSE_ICON_ROLLOVER = new ImageIcon(DefaultTabHandle.class.getResource("close_rollover.png"));
  
  
  
  /**
   * The pressed version of the close button icon.
   */
  
  private static final Icon CLOSE_ICON_PRESSED = new ImageIcon(DefaultTabHandle.class.getResource("close_pressed.png"));
  
  
  
  /**
   * The tabbed pane we're part of.
   */
  
  private final TabbedPane tabbedPane;
  
  
  
  /**
   * The tab we're representing.
   */
  
  private final Tab tab;
  
  
  
  /**
   * The panel holding the actual tab components.
   */
  
  private final JPanel component;
  
  
  
  /**
   * The label we employ for the title and icon.
   */
  
  private final JLabel label;
  
  
  
  /**
   * The close button.
   */
  
  private final JButton closeButton;
  
  
  
  /**
   * Creates a new <code>DefaultTabHandle</code> for the specified tab to be
   * used in the specified tabbed pane.
   */
  
  public DefaultTabHandle(TabbedPane tabbedPane, Tab tab){
    this.tabbedPane = tabbedPane;
    this.tab = tab;
    
    MouseListener pressListener = new MouseAdapter(){
      public void mousePressed(MouseEvent e){
        selectInvoked();
        e.consume();
      }
    };
    
    
    // Create the component
    this.component = new JPanel(new BorderLayout(2, 2));
    component.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 10));
    component.setOpaque(false);
    component.setMinimumSize(new Dimension(20, component.getMinimumSize().height));
    component.addMouseListener(pressListener);
    
    
    // Create the label
    this.label = makeLabel();
    confLabelFromTab();
    label.addMouseListener(pressListener);
    
    
    // Create the close button
    this.closeButton = makeCloseButton();
    closeButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        closeInvoked();
      }
    });
    
    
    // Add label and button to the component
    component.add(label, BorderLayout.CENTER);
    if (tab.isCloseable()){
      if (SwingUtils.isMacLnF())
        component.add(closeButton, BorderLayout.LINE_START);
      else
        component.add(closeButton, BorderLayout.LINE_END);
    }
    
    
    TabbedPaneModel model = tabbedPane.getModel();
    setSelected(model.getSelectedIndex() == model.indexOfTab(tab));
    
    tab.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        confLabelFromTab();
      }
    });
  }
  
  
  
  /**
   * Creates the label part of the tab handle.
   */
  
  private JLabel makeLabel(){
    JLabel label = new JLabel();
    label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    label.setOpaque(false);
    
    return label;
  }
  
  
  
  /**
   * Sets the label's properties from the tab's properties.
   */
  
  private void confLabelFromTab(){
    label.setText(tab.getTitle());
    label.setToolTipText(tab.getTitle());
    label.setIcon(tab.getIcon());
  }
  
  
  
  /**
   * Creates the button that closes the tab.
   */
  
  private JButton makeCloseButton(){
    JButton closeButton = new JButton();
    
    closeButton.setIcon(CLOSE_ICON_NORMAL);
    closeButton.setRolloverIcon(CLOSE_ICON_ROLLOVER);
    closeButton.setPressedIcon(CLOSE_ICON_PRESSED);
    
    closeButton.setBorderPainted(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setFocusable(false);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setOpaque(false);
    
    return closeButton;
  }
  
  
  
  /**
   * Invoked when the user presses the tab handle. Makes the tab we're
   * representing selected.
   */
  
  protected void selectInvoked(){
    TabbedPaneModel model = tabbedPane.getModel(); 
    model.setSelectedIndex(model.indexOfTab(tab));
  }
  
  
  
  /**
   * Invoked when the user presses the close button. Consults the
   * <code>TabCloseApprover</code> of the tabbed pane and closes the tab.
   */
  
  protected void closeInvoked(){
    TabbedPaneModel model = DefaultTabHandle.this.tabbedPane.getModel();
    int tabIndex = model.indexOfTab(DefaultTabHandle.this.tab);
    
    TabCloseApprover mainApprover = tabbedPane.getTabCloseApprover();
    TabCloseApprover tabApprover = tab.getTabCloseApprover();
    
    if (((mainApprover == null) || mainApprover.mayClose(tabbedPane, tab, tabIndex)) &&
        ((tabApprover == null) || tabApprover.mayClose(tabbedPane, tab, tabIndex)))
      model.removeTab(tabIndex);
  }
  
  
  
  /**
   * Returns the tab component.
   */
  
  public Component getComponent(){
    return component;
  }
  
  
  
  /**
   * Sets the selected state of the tab component.
   */
  
  public void setSelected(boolean isSelected){
    
  }
  
  
  
}
