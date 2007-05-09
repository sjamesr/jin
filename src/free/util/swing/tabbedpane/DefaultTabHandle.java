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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import free.util.swing.SwingUtils;



/**
 * The default implementation of <code>TabHandle</code>.
 */

public class DefaultTabHandle implements TabHandle{
  
  
  
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
   * Creates a new <code>DefaultTabHandle</code> for the specified tab to be
   * used in the specified tabbed pane.
   */
  
  public DefaultTabHandle(TabbedPane tabbedPane, Tab tab){
    this.tabbedPane = tabbedPane;
    this.tab = tab;
    this.component = new JPanel(new BorderLayout(2, 2));
    
    MouseListener pressListener = new MouseAdapter(){
      public void mousePressed(MouseEvent e){
        TabbedPaneModel model = DefaultTabHandle.this.tabbedPane.getModel(); 
        model.setSelectedIndex(model.indexOfTab(DefaultTabHandle.this.tab));
        e.consume();
      }
    }; 
    
    component.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 10));
    component.setOpaque(false);
    component.setMinimumSize(new Dimension(20, component.getMinimumSize().height));
    component.addMouseListener(pressListener);
    
    JLabel label = makeLabel();
    label.addMouseListener(pressListener);
    component.add(label, BorderLayout.CENTER);
    
    if (tab.isCloseable()){
      JButton closeButton = makeCloseButton();
      
      closeButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          TabbedPaneModel model = DefaultTabHandle.this.tabbedPane.getModel(); 
          model.removeTab(model.indexOfTab(DefaultTabHandle.this.tab));
        }
      });
      
      if (SwingUtils.isMacLnF())
        component.add(closeButton, BorderLayout.LINE_START);
      else
        component.add(closeButton, BorderLayout.LINE_END);
    }
    
    TabbedPaneModel model = tabbedPane.getModel();
    setSelected(model.getSelectedIndex() == model.indexOfTab(tab));
  }
  
  
  
  /**
   * Creates the label part of the tab handle.
   */
  
  protected JLabel makeLabel(){
    JLabel label = new JLabel();
    label.setText(tab.getTitle());
    label.setToolTipText(tab.getTitle());
    label.setIcon(tab.getIcon());
    label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    label.setOpaque(false);
    
    return label;
  }
  
  
  
  /**
   * Creates the button that closes the tab.
   */
  
  protected JButton makeCloseButton(){
    JButton closeButton = new JButton();
    
    closeButton.setIcon(new ImageIcon(DefaultTabHandle.class.getResource("close.png")));
    closeButton.setRolloverIcon(new ImageIcon(DefaultTabHandle.class.getResource("close_rollover.png")));
    closeButton.setPressedIcon(new ImageIcon(DefaultTabHandle.class.getResource("close_pressed.png")));
    
    closeButton.setBorderPainted(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setFocusable(false);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setOpaque(false);
    
    return closeButton;
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
