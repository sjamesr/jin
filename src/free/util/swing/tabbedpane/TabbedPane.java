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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import free.util.swing.WrapLayout;


/**
 * A better implementation of a tabbed pane.
 */

public class TabbedPane extends JComponent{
  
  
  
  /**
   * The panel where our tabs reside.
   */
  
  private final JPanel tabPanel = new JPanel(new TabRowLayout());
  
  
  
  /**
   * The panel holding the tab components.
   */
  
  private final JPanel componentPanel = new JPanel(WrapLayout.getInstance());
  
  
  
  /**
   * Our proxy listener with the model.
   */
  
  private final TabbedPaneListener proxyModelListener = new TabbedPaneListener(){

    public void tabAdded(TabbedPaneEvent evt){
      TabbedPane.this.tabAdded(evt);
    }

    public void tabRemoved(TabbedPaneEvent evt){
      TabbedPane.this.tabRemoved(evt);
    }
    
    public void tabSelected(TabbedPaneEvent evt){
      TabbedPane.this.tabSelected(evt);
    }
    
    public void tabDeselected(TabbedPaneEvent evt){
      TabbedPane.this.tabDeselected(evt);
    }
    
  };
  
  
  
  /**
   * Our tab factory.
   */
  
  private TabComponentFactory tabComponentFactory;
  
  
  
  /**
   * Our model.
   */
  
  private TabbedPaneModel model;
  
  
  
  /**
   * The placement of our tabs.
   */
  
  private int tabPlacement;
  
  
  
  /**
   * Creates a new <code>TabbedPane</code> with the specified tab placement -
   * one of:
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  
  public TabbedPane(int tabPlacement){
    switch (tabPlacement){
      case SwingConstants.TOP:
      case SwingConstants.BOTTOM:
      case SwingConstants.LEFT:
      case SwingConstants.RIGHT:
        break;
      default:
        throw new IllegalArgumentException("Unknown tab placement: " + tabPlacement);
    }
    
    this.tabComponentFactory = new DefaultTabComponentFactory();    
    this.model = new DefaultTabbedPaneModel();
    this.tabPlacement = tabPlacement;
    
    registerModelListeners(model);
    
    setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1));
    
    recreateUI();
  }
  
  
  
  /**
   * Returns the model of this tabbed pane.
   */
  
  public TabbedPaneModel getModel(){
    return model;
  }
  
  
  
  /**
   * Sets the model of this tabbed pane.
   */
  
  public void setModel(TabbedPaneModel model){
    if (model == null)
      throw new IllegalArgumentException("model may not be null");
    
    unregisterModelListeners(this.model);
    this.model = model;
    registerModelListeners(this.model);
    
    recreateUI();
  }
  
  
  
  /**
   * Registers any listeners we need with the model.
   */
  
  private void registerModelListeners(TabbedPaneModel model){
    model.addTabbedPaneListener(proxyModelListener);
  }
  
  
  
  /**
   * Unregisters any listeners we registered with the model.
   */
  
  private void unregisterModelListeners(TabbedPaneModel model){
    model.removeTabbedPaneListener(proxyModelListener);
  }
  
  
  
  /**
   * Returns the tab component factory of this tabbed pane.
   */
  
  public TabComponentFactory getTabComponentFactory(){
    return tabComponentFactory;
  }
  
  
  
  /**
   * Sets the tab component factory of this tabbed pane.
   */
  
  public void setTabComponentFactory(TabComponentFactory tabComponentFactory){
    if (tabComponentFactory == null)
      throw new IllegalArgumentException("tabComponentFactory may not be null");
    
    this.tabComponentFactory = tabComponentFactory;
    
    recreateUI();
  }
  
  
  
  /**
   * Returns the tab placement of this tabbed pane - one of:
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  
  public int getTabPlacement(){
    return tabPlacement;
  }
  
  
  
  /**
   * Sets the tab placement of this tabbed pane to one of:
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  
  public void setTabPlacement(int tabPlacement){
    this.tabPlacement = tabPlacement;
    
    recreateUI();
  }
  
  
  
  /**
   * Invoked when a new tab is added to the model.
   */
  
  private void tabAdded(TabbedPaneEvent evt){
    int index = evt.getTabIndex();
    TabbedPaneModel model = evt.getTabbedPaneModel(); 
    Tab tab = model.getTab(index);
    Component component = tab.getComponent();
    
    component.setVisible(index == model.getSelectedIndex());
    
    tabPanel.add(makeTabCell(getTabComponentFactory().makeMainRowComponent(this, tab)));
    componentPanel.add(tab.getComponent());
  }
  
  
  
  /**
   * Invoked when a tab is removed from the model.
   */
  
  private void tabRemoved(TabbedPaneEvent evt){
    int index = evt.getTabIndex();
    tabPanel.remove(index);
    componentPanel.remove(index);
  }
  
  
  
  /**
   * Invoked when a tab is selected in the model.
   */
  
  private void tabSelected(TabbedPaneEvent evt){
    getModel().getTab(evt.getTabIndex()).getComponent().setVisible(true);
  }
  
  
  
  /**
   * Invoked when a tab is deselected in the model.
   */
  
  private void tabDeselected(TabbedPaneEvent evt){
    getModel().getTab(evt.getTabIndex()).getComponent().setVisible(false);
  }
  
  
  
  /**
   * Creates and returns the component that contains the specified tab component
   * in the tab row.
   */
  
  private JComponent makeTabCell(JComponent titleComponent){
    return titleComponent;
  }
  
  
  
  /**
   * Creates (or re-creates, if already exists) the UI of this tabbed pane.
   */
  
  protected void recreateUI(){
    
    // Remove existing UI
    removeAll();
    tabPanel.removeAll();
    
    
    // Get data
    TabbedPaneModel model = getModel();
    TabComponentFactory factory = getTabComponentFactory();
    
    
    // Create new UI
    int selectedIndex = model.getSelectedIndex();
    int tabCount = model.getTabCount();
    for (int i = 0; i < tabCount; i++){
      Tab tab = model.getTab(i);
      Component component = tab.getComponent();
      
      component.setVisible(i == selectedIndex);
      
      tabPanel.add(makeTabCell(factory.makeMainRowComponent(this, tab)));
      componentPanel.add(component);
    }
    
    
    // Add new UI
    setLayout(new BorderLayout());
    
    Object tabRowPosition;
    switch(getTabPlacement()){
      case  SwingUtilities.TOP: tabRowPosition = BorderLayout.NORTH; break;
      case  SwingUtilities.BOTTOM: tabRowPosition = BorderLayout.SOUTH; break;
      case  SwingUtilities.LEFT: tabRowPosition = BorderLayout.WEST; break;
      case  SwingUtilities.RIGHT: tabRowPosition = BorderLayout.EAST; break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
    }
    
    add(tabPanel, tabRowPosition);
    add(componentPanel, BorderLayout.CENTER);
    
    
    // Configure some things
    tabPanel.setOpaque(false);
    componentPanel.setOpaque(false);

    int top, left, bottom, right;
    top = left = bottom = right = 5;
    switch(getTabPlacement()){
      case  SwingUtilities.TOP: bottom = 1; top = 2; break;
      case  SwingUtilities.BOTTOM: top = 1; bottom = 2; break;
      case  SwingUtilities.LEFT: right = 1; left = 2; break;
      case  SwingUtilities.RIGHT: left = 1; right = 2; break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
    }
    tabPanel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
  }
  
  
  
  /**
   * Paints the tabbed pane.
   */
  
  public void paintComponent(Graphics graphics){
    Graphics2D g = (Graphics2D)graphics;
    
    TabbedPaneModel model = getModel();
    
    Color borderColor = getBackground().darker();
    g.setColor(borderColor);
    
    // Draw the lines around the tabs and the lines to the side of the component
    Rectangle tabPanelBounds = tabPanel.getBounds();
    Insets tabPanelInsets = tabPanel.getInsets();
    
    // The coordinates of the "anchor" point of the current tab.
    // For TOP, it's the top-left, for RIGHT, it's the top-right etc. 
    int x, y;
    
    // The direction of the component from the anchor point
    int wc, hc; 
    
    // The direction in which we advance from tab to tab
    int cx, cy;
    
    // The offset of the selected tab, in the direction perpendicular to the
    // direction of the tab row
    int selectedOffset;
    
    switch (tabPlacement){
      case  SwingUtilities.TOP:
        x = tabPanelBounds.x + tabPanelInsets.left;
        y = tabPanelBounds.y;
        selectedOffset = tabPanelInsets.top;
        wc = 0;
        hc = 1;
        cx = 1;
        cy = 0;
        break;
      case  SwingUtilities.BOTTOM:
        x = tabPanelBounds.x + tabPanelInsets.left;
        y = tabPanelBounds.y + tabPanelBounds.height;
        selectedOffset = tabPanelInsets.bottom;
        wc = 0;
        hc = -1;
        cx = 1;
        cy = 0;
        break;
      case  SwingUtilities.LEFT:
        x = tabPanelBounds.x;
        y = tabPanelBounds.y + tabPanelInsets.top;
        selectedOffset = tabPanelInsets.left;
        wc = 1;
        hc = 0;
        cx = 0;
        cy = 1;
        break;
      case  SwingUtilities.RIGHT:
        x = tabPanelBounds.x + tabPanelBounds.width;
        y = tabPanelBounds.y + tabPanelInsets.top;
        selectedOffset = tabPanelInsets.right;
        wc = -1;
        hc = 0;
        cx = 0;
        cy = 1;
        break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
    }
    
    int w = wc*tabPanelBounds.width; // The x-axis offset from the anchor to the component
    int h = hc*tabPanelBounds.height; // The y-axis offset from the anchor to the component
    
    int dx, dy; // The offset to the end of the current tab
    
    // The small line from the side of the tab row to the first tab
    dx = -cx*tabPanelInsets.left;
    dy = -cy*tabPanelInsets.top;
    g.drawLine(x + w, y + h, x + w + dx, y + h + dy);
    
    int selectedIndex = model.getSelectedIndex();
    int tabCount = tabPanel.getComponentCount();
    
    Dimension tabSize = null;
    for (int i = 0; i < tabCount; i++){
      tabSize = tabPanel.getComponent(i).getSize(tabSize);
      
      dx = cx*(tabSize.width + 1);
      dy = cy*(tabSize.height + 1);
      
      if (selectedIndex == i){ // The selected tab
        final int cutSize = 2;
        
        int selectedX = x + wc*selectedOffset;
        int selectedY = y + hc*selectedOffset;
        // The line before the selected tab
        g.drawLine(selectedX + wc*cutSize, selectedY + hc*cutSize, x + w, y + h);
        
        // The line after the selected tab
        g.drawLine(selectedX + wc*cutSize + dx, selectedY + hc*cutSize + dy, x + w + dx, y + h + dy);
        
        // The line opposite to the component
        g.drawLine(selectedX + cx*cutSize, selectedY + cy*cutSize, 
            selectedX - cx*cutSize + dx, selectedY - cy*cutSize + dy); 
        
        // The corners
        g.drawLine(selectedX + wc*cutSize, selectedY + hc*cutSize, selectedX + cx*cutSize, selectedY + cy*cutSize);
        g.drawLine(selectedX + wc*cutSize + dx, selectedY + hc*cutSize + dy, selectedX - cx*cutSize + dx, selectedY - cy*cutSize + dy);
      }
      else{
        if (selectedIndex - 1 != i)
          g.drawLine(x + dx, y + dy, x + dx + w, y + dy + h); // The line between the tabs
        g.drawLine(x + w, y + h, x + w + dx, y + h + dy); // The line on the component's side
      }
      
      x += dx;
      y += dy;
    }
    
    // The line from the end of the tabs to the other side of the tab row 
    dx = cx * (tabPanelBounds.width - x);
    dy = cy * (tabPanelBounds.height - y);
    g.drawLine(x + w, y + h, x + w + dx, y + h + dy);
  }
  
  
  
  /**
   * The layout manager we use for the main tab row.
   */
  
  private class TabRowLayout implements LayoutManager{
    
    
    
    /**
     * The minimum sizes of the children.
     */
    
    private Dimension [] minSizes = null;
    
    
    
    /**
     * The preferred sizes of the children.
     */
    
    private Dimension [] prefSizes = null;
    
    
    
    /**
     * Returns the minimum sizes of the child tabs.
     */
    
    private Dimension [] getMinTabSizes(){
      if (minSizes != null)
        return minSizes;
      
      int tabCount = tabPanel.getComponentCount();
      minSizes = new Dimension[tabCount];
      
      for (int i = 0; i < tabCount; i++)
        minSizes[i] = tabPanel.getComponent(i).getMinimumSize();
      
      return minSizes;
    }
    
    
    
    /**
     * Returns the preferred sizes of the child tabs.
     */
    
    private Dimension [] getPrefTabSizes(){
      if (prefSizes != null)
        return prefSizes;
      
      int tabCount = tabPanel.getComponentCount();
      prefSizes = new Dimension[tabCount];
      
      for (int i = 0; i < tabCount; i++)
        prefSizes[i] = tabPanel.getComponent(i).getPreferredSize();
      
      return prefSizes;
    }

    
    
    
    /**
     * Lays out the tab row.
     */
    
    public void layoutContainer(Container parent){
      if (parent != tabPanel)
        throw new IllegalStateException("TabRowLayout may only be used to layout tabPanel");
      
      final int tabGap = 1;
      
      boolean isVertical = 
        (getTabPlacement() == SwingUtilities.LEFT) || 
        (getTabPlacement() == SwingUtilities.RIGHT);

      Dimension parentSize = parent.getSize();
      Insets insets = parent.getInsets();
      int d = isVertical ? insets.top : insets.left;
      int availableWidth = parentSize.width - insets.left - insets.right;
      int availableHeight = parentSize.height - insets.top - insets.bottom;
      
      int tabCount = parent.getComponentCount();
      
      // Get the preferred sizes of the tabs
      Dimension [] prefSizes = getPrefTabSizes();
      
      // Calculate the sizes of the tabs along the main axis
      int [] sizes = new int[tabCount];
      int totalSize = 0;
      for (int i = 0; i < tabCount; i++){
        sizes[i] = isVertical ? prefSizes[i].height : prefSizes[i].width;
        totalSize += sizes[i];
      }
      
      int availableTabSize = (isVertical ? availableHeight : availableWidth) - tabGap*(tabCount - 1);
      if (totalSize > availableTabSize){
        Dimension [] minDims = getMinTabSizes();
        int [] minSizes = new int[minDims.length];
        for (int i = 0; i < tabCount; i++)
          minSizes[i] = isVertical ? minDims[i].height : minDims[i].width;
          
        fitTabs(sizes, minSizes, totalSize, availableTabSize);
      }
      
      // Layout
      for (int i = 0; i < tabCount; i++){
         Component tab = parent.getComponent(i);
         
         int x = isVertical ? insets.left : d;
         int y = isVertical ? d : insets.top;
         
         int width = isVertical ? availableWidth : sizes[i];
         int height = isVertical ? sizes[i] : availableHeight;
         
         tab.setBounds(x, y, width, height);
         
         d += isVertical ? height : width;
         d += tabGap;
      }
    }
    
    
    
    /**
     * <p>Attempts to fit the tabs into the specified available size (isn't
     * successful if the sum of the minimum sizes is larger than the available
     * size).
     * <p>The algorithm is roughly as follows: each round, find the tabs that
     * have the maximum reduction capability (difference between current size
     * and minimum size) and find the second largest value. Reduce these tabs
     * to the second largest value and repeat. The algorithm ends when either
     * it is impossible to reduce further (all tabs at their minimum sizes) or
     * when the possible reduction in the current round places the total size
     * below the available size. 
     */
    
    private void fitTabs(int [] sizes, int [] minSizes, int totalSize, int availableSize){
      int count = sizes.length;
      int largestIndices [] = new int[count]; // The indices of the tabs which can be maximally reduced
      int largestCount; // The number of such tabs
      
      while (totalSize > availableSize){ 
        // Find the indices with the largest difference between size and minimum size
        int largestDiff = 0;
        int secondLargest = 0;
        largestCount = 0;
        for (int i = 0; i < sizes.length; i++){
          int diff = sizes[i] - minSizes[i];
          if (diff > largestDiff){
            largestCount = 1;
            largestIndices[0] = i;
            secondLargest = largestDiff;
            largestDiff = diff;
          }
          else if (diff == largestDiff)
            largestIndices[largestCount++] = i;
        }
        
        if (largestDiff == 0)
          return; // Nothing else we can do
        
        // Reduce them
        if (totalSize - availableSize < largestCount){
          // Less pixels needed than there are reducible tabs.
          // Reduce only the first totalSize - availableSize tabs, each by 1
          for (int i = 0; i < totalSize - availableSize; i++)
            sizes[largestIndices[i]]--;
          
          totalSize = availableSize;
        }
        else if (totalSize - availableSize < largestCount * (largestDiff - secondLargest)){
          // Less pixels needed than we reduce in this round
          // Reduce each tab by almost the same amount - some of the first tabs
          // will be reduced by 1 pixel more. 
          int baseReduction = (int)Math.floor((totalSize - availableSize) / (double)largestCount);
          int extraReductionCount = totalSize - availableSize - baseReduction*largestCount;
          
          for (int i = 0; i < extraReductionCount; i++)
            sizes[largestIndices[i]] -= (baseReduction + 1);
          
          for (int i = extraReductionCount; i < largestCount; i++)
            sizes[largestIndices[i]] -= baseReduction;
          
          totalSize = availableSize;
        }
        else{
          // More pixels needed than we can reduce in this round
          // Reduce each tab by largestDiff - secondLargest
          int reduction = largestDiff - secondLargest;
          for (int i = 0; i < largestCount; i++)
            sizes[largestIndices[i]] -= reduction;
          
          totalSize -= reduction*largestCount;
        }
      }
    }
    
    
    
    /**
     * Returns the minimum size for the tab row.
     */
    
    public Dimension minimumLayoutSize(Container parent){
      if (parent != tabPanel)
        throw new IllegalStateException("TabRowLayout may only be used to layout tabPanel");
      
      boolean isVertical = 
        (getTabPlacement() == SwingUtilities.LEFT) || 
        (getTabPlacement() == SwingUtilities.RIGHT);
      
      int size = 0;
      int componentCount = parent.getComponentCount();
      for (int i = 0; i < componentCount; i++){
        Dimension minSize = parent.getComponent(i).getMinimumSize();
        size = Math.max(size, isVertical ? minSize.width : minSize.height);
      }
      
      Insets insets = parent.getInsets();
      return new Dimension(
          (isVertical ? size : 0) + insets.left + insets.right,
          (isVertical ? 0 : size) + insets.top + insets.bottom);
    }
    
    
    
    /**
     * Returns the preferred size for the tab row.
     */
    
    public Dimension preferredLayoutSize(Container parent){
      if (parent != tabPanel)
        throw new IllegalStateException("TabRowLayout may only be used to layout tabPanel");
      
      boolean isVertical = 
        (getTabPlacement() == SwingUtilities.LEFT) || 
        (getTabPlacement() == SwingUtilities.RIGHT);
      
      int size = 0;
      int componentCount = parent.getComponentCount();
      for (int i = 0; i < componentCount; i++){
        Dimension prefSize = parent.getComponent(i).getPreferredSize();
        size = Math.max(size, isVertical ? prefSize.width : prefSize.height);
      }
      
      Insets insets = parent.getInsets();
      return new Dimension(
          (isVertical ? size : 0) + insets.left + insets.right,
          (isVertical ? 0 : size) + insets.top + insets.bottom);
    }
    
    
    
    public void addLayoutComponent(String name, Component comp){
      
    }
    
    
    
    public void removeLayoutComponent(Component comp){
      
    }
    
    
    
  }
  
  
  
  public static void main(String [] args){
    JFrame frame = new JFrame("Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    TabbedPane tabbedPane = new TabbedPane(SwingUtilities.BOTTOM);
    tabbedPane.setBorder(null);
    
    Tab [] tabs = new Tab[]{
        new Tab(new JLabel("Component "), "Short", null, false),
        new Tab(new JLabel("Component "), "Medium Size", null, false),
        new Tab(new JLabel("Component "), "Short", null, false),
        new Tab(new JLabel("Component "), "A very, very, very long tab", null, false),
        new Tab(new JLabel("Component "), "Short", null, false),
        new Tab(new JLabel("Component "), "Short", null, false),
        new Tab(new JLabel("Component "), "Medium Size", null, false),
    };
    
    for (int i = 0; i < tabs.length; i++)
      tabbedPane.getModel().addTab(tabs[i], i);
    
    
    
    tabbedPane.getModel().setSelectedIndex(1);
    
    JPanel contentPane = new JPanel(new BorderLayout());
//    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPane.add(tabbedPane);
    frame.setContentPane(contentPane);
    
    frame.setBounds(400, 400, 400, 400);
    frame.setVisible(true);
  }
  
  
  
}
