/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.swing.tabbedpane;

import free.util.UnsupportedOperationException;
import free.util.swing.WrapLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** A better implementation of a tabbed pane. */
public class TabbedPane extends JComponent {

  /** The color of various lines in the tabbed pane. */
  private static final Color LINE_COLOR = new Color(0, 0, 0, 96);
  //  static{
  //    Color bg = UIManager.getColor("Panel.background");
  //    float [] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
  //    hsb[2] *= 0.4f;
  //    LINE_COLOR = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
  //  }

  /** The panel where our tab handles reside. */
  private final JPanel handlePanel = new TabHandlesPanel();

  /** The panel holding the components. */
  private final JPanel componentPanel = new JPanel(WrapLayout.getInstance());

  /** Our tab handles. */
  private final List tabHandles = new ArrayList();

  /** Our proxy listener with the model. */
  private final TabbedPaneListener proxyModelListener =
      new TabbedPaneListener() {

        @Override
        public void tabAdded(TabbedPaneEvent evt) {
          TabbedPane.this.tabAdded(evt);
        }

        @Override
        public void tabRemoved(TabbedPaneEvent evt) {
          TabbedPane.this.tabRemoved(evt);
        }

        @Override
        public void tabSelected(TabbedPaneEvent evt) {
          TabbedPane.this.tabSelected(evt);
        }

        @Override
        public void tabDeselected(TabbedPaneEvent evt) {
          TabbedPane.this.tabDeselected(evt);
        }
      };

  /** Our tab handle factory. */
  private TabHandleFactory tabHandleFactory;

  /** Our model. */
  private TabbedPaneModel model;

  /** The placement of our tabs. */
  private int tabPlacement;

  /** Whether we show tabs even when there's only a single tab. */
  private boolean isAlwaysShowTabs = true;

  /** Our approver of tab close actions. */
  private TabCloseApprover tabCloseApprover = null;

  /**
   * Creates a new <code>TabbedPane</code> with the specified tab placement - one of:
   *
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  public TabbedPane(int tabPlacement) {
    switch (tabPlacement) {
      case SwingConstants.TOP:
      case SwingConstants.BOTTOM:
      case SwingConstants.LEFT:
      case SwingConstants.RIGHT:
        break;
      default:
        throw new IllegalArgumentException("Unknown tab placement: " + tabPlacement);
    }

    this.tabHandleFactory = new DefaultTabHandleFactory();
    this.model = new DefaultTabbedPaneModel();
    this.tabPlacement = tabPlacement;

    registerModelListeners(model);

    for (int i = 0; i < model.getTabCount(); i++)
      tabHandles.add(getTabHandleFactory().createTabHandle(this, model.getTab(i)));

    setBorder(BorderFactory.createLineBorder(LINE_COLOR, 1));

    recreateUI();
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isFocusCycleRoot() {
    return true;
  }

  /** Returns the model of this tabbed pane. */
  public TabbedPaneModel getModel() {
    return model;
  }

  /** Sets the model of this tabbed pane. */
  public void setModel(TabbedPaneModel model) {
    if (model == null) throw new IllegalArgumentException("model may not be null");

    unregisterModelListeners(this.model);
    this.model = model;
    registerModelListeners(this.model);

    recreateUI();
  }

  /** Registers any listeners we need with the model. */
  private void registerModelListeners(TabbedPaneModel model) {
    model.addTabbedPaneListener(proxyModelListener);
  }

  /** Unregisters any listeners we registered with the model. */
  private void unregisterModelListeners(TabbedPaneModel model) {
    model.removeTabbedPaneListener(proxyModelListener);
  }

  /** Returns the tab handle factory of this tabbed pane. */
  public TabHandleFactory getTabHandleFactory() {
    return tabHandleFactory;
  }

  /** Sets the tab handle factory of this tabbed pane. */
  public void setTabHandleFactory(TabHandleFactory tabHandleFactory) {
    if (tabHandleFactory == null)
      throw new IllegalArgumentException("tabHandleFactory may not be null");

    this.tabHandleFactory = tabHandleFactory;

    recreateUI();
  }

  /**
   * Returns the tab placement of this tabbed pane - one of:
   *
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  public int getTabPlacement() {
    return tabPlacement;
  }

  /**
   * Sets the tab placement of this tabbed pane to one of:
   *
   * <ul>
   *   <li><code>SwingConstants.TOP</code>
   *   <li><code>SwingConstants.BOTTOM</code>
   *   <li><code>SwingConstants.LEFT</code>
   *   <li><code>SwingConstants.RIGHT</code>
   * </ul>
   */
  public void setTabPlacement(int tabPlacement) {
    this.tabPlacement = tabPlacement;

    recreateUI();
    repaint();
  }

  /** Returns whether we always show tabs, even when the model only has a single tab. */
  public boolean isAlwaysShowTabs() {
    return isAlwaysShowTabs;
  }

  /** Sets whether we always show tabs, even when the model only has a single tab. */
  public void setAlwaysShowTabs(boolean isAlwaysShowTabs) {
    this.isAlwaysShowTabs = isAlwaysShowTabs;

    if (getModel().getTabCount() == 1) {
      recreateUI();
      repaint();
    }
  }

  /** Returns the current approver of tab close actions; <code>null</code> if none. */
  public TabCloseApprover getTabCloseApprover() {
    return tabCloseApprover;
  }

  /**
   * Sets the approver of tab close actions. The approver is consulted when the user attempts to
   * close a closeable tab. If the approver disapproves, the action is canceled and the tab isn't
   * closed. A <code>null</code> approver is not consulted and thus the close action always goes
   * forward. The default value is <code>null</code>. In addition to this approver, the approver of
   * the tab being closed is also consulted.
   */
  public void setTabCloseApprover(TabCloseApprover tabCloseApprover) {
    this.tabCloseApprover = tabCloseApprover;
  }

  /** Invoked when a new tab is added to the model. */
  private void tabAdded(TabbedPaneEvent evt) {
    int index = evt.getTabIndex();
    TabbedPaneModel model = evt.getTabbedPaneModel();
    Tab tab = model.getTab(index);
    Component component = tab.getComponent();
    boolean isSelected = index == model.getSelectedIndex();

    TabHandle tabHandle = getTabHandleFactory().createTabHandle(this, tab);
    tabHandles.add(index, tabHandle);

    if (isAlwaysShowTabs() || (model.getTabCount() > 2)) {
      handlePanel.add(new TabCell(tabHandle, isSelected));
      componentPanel.add(tab.getComponent());

      validate();
    } else recreateUI();

    component.setVisible(isSelected);

    repaint();
  }

  /** Invoked when a tab is removed from the model. */
  private void tabRemoved(TabbedPaneEvent evt) {
    int index = evt.getTabIndex();

    tabHandles.remove(index);

    if (isAlwaysShowTabs() || (model.getTabCount() > 1)) {
      handlePanel.remove(index);
      componentPanel.remove(index);

      validate();
    } else recreateUI();

    repaint();
  }

  /** Invoked when a tab is selected in the model. */
  private void tabSelected(TabbedPaneEvent evt) {
    int index = evt.getTabIndex();
    TabbedPaneModel model = getModel();

    model.getTab(index).getComponent().setVisible(true);
    ((TabHandle) tabHandles.get(index)).setSelected(true);

    if (isAlwaysShowTabs() || (model.getTabCount() > 1))
      ((TabCell) handlePanel.getComponent(index)).setSelected(true);

    repaint();
  }

  /** Invoked when a tab is deselected in the model. */
  private void tabDeselected(TabbedPaneEvent evt) {
    int index = evt.getTabIndex();
    TabbedPaneModel model = getModel();

    model.getTab(index).getComponent().setVisible(false);
    ((TabHandle) tabHandles.get(index)).setSelected(false);

    if (isAlwaysShowTabs() || (model.getTabCount() > 1))
      ((TabCell) handlePanel.getComponent(index)).setSelected(false);

    repaint();
  }

  /** Creates (or re-creates, if already exists) the UI of this tabbed pane. */
  protected void recreateUI() {
    TabbedPaneModel model = getModel();

    // Remove existing UI
    removeAll();
    handlePanel.removeAll();

    // Create new UI
    int selectedIndex = model.getSelectedIndex();
    int tabCount = model.getTabCount();
    boolean noTabs = (!isAlwaysShowTabs()) && (model.getTabCount() < 2);

    for (int i = 0; i < tabCount; i++) {
      Tab tab = model.getTab(i);
      Component component = tab.getComponent();
      boolean isSelected = i == selectedIndex;

      component.setVisible(isSelected);

      if (!noTabs) handlePanel.add(new TabCell((TabHandle) tabHandles.get(i), isSelected));
      componentPanel.add(component);
    }

    // Add new UI
    if (noTabs) {
      componentPanel.setBorder(null);
      setLayout(WrapLayout.getInstance());
      add(componentPanel);
      return;
    }

    setLayout(new BorderLayout());

    Object tabRowPosition;
    switch (getTabPlacement()) {
      case SwingConstants.TOP:
        tabRowPosition = BorderLayout.NORTH;
        break;
      case SwingConstants.BOTTOM:
        tabRowPosition = BorderLayout.SOUTH;
        break;
      case SwingConstants.LEFT:
        tabRowPosition = BorderLayout.WEST;
        break;
      case SwingConstants.RIGHT:
        tabRowPosition = BorderLayout.EAST;
        break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
    }

    add(handlePanel, tabRowPosition);
    add(componentPanel, BorderLayout.CENTER);

    int tabPlacement = getTabPlacement();

    // Set the handle panel border
    int top, left, bottom, right;
    final int startOffset = 5; // The offset of the tabs at the start of the tab panel
    final int endOffset = 5; // The offset of the tabs at the end of the tab panel
    final int nearOffset = 0; // The offset of the tab panel from the component
    final int farOffset = 2; // The offset of the tab panel from the side opposite to the component
    switch (tabPlacement) {
      case SwingConstants.TOP:
        top = farOffset;
        bottom = nearOffset;
        left = startOffset;
        right = endOffset;
        break;
      case SwingConstants.BOTTOM:
        top = nearOffset;
        bottom = farOffset;
        left = startOffset;
        right = endOffset;
        break;
      case SwingConstants.LEFT:
        top = startOffset;
        bottom = endOffset;
        left = farOffset;
        right = nearOffset;
        break;
      case SwingConstants.RIGHT:
        top = startOffset;
        bottom = endOffset;
        left = nearOffset;
        right = farOffset;
        break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + tabPlacement);
    }
    handlePanel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));

    // Set the component panel border
    final int tabOffset = 3; // The offset of the component to the tabs
    top = left = bottom = right = 0;
    switch (tabPlacement) {
      case SwingConstants.TOP:
        top = tabOffset;
        break;
      case SwingConstants.BOTTOM:
        bottom = tabOffset;
        break;
      case SwingConstants.LEFT:
        left = tabOffset;
        break;
      case SwingConstants.RIGHT:
        right = tabOffset;
        break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + tabPlacement);
    }
    componentPanel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
  }

  /** Paints the tabbed pane. */
  @Override
  public void paint(Graphics graphics) {
    super.paint(graphics);

    if ((getModel().getTabCount() <= 1) && !isAlwaysShowTabs()) return;

    Graphics2D g = (Graphics2D) graphics;

    TabbedPaneModel model = getModel();

    g.setColor(LINE_COLOR);

    // Draw the lines around the tabs and the lines to the side of the component
    Rectangle tabPanelBounds = handlePanel.getBounds();
    Rectangle firstTabBounds =
        handlePanel.getComponentCount() == 0
            ? new Rectangle()
            : handlePanel.getComponent(0).getBounds();

    // The coordinates of the "anchor" point of the current tab.
    // For TOP, it's the top-left, for RIGHT, it's the top-right etc.
    int x, y;

    // The direction of the component from the anchor point
    int wc, hc;

    // The direction in which we advance from tab to tab
    int cx, cy;

    // The offset of the selected tab, in the direction perpendicular to the
    // direction of the tab row, from the border.
    int selectedOffset;

    switch (tabPlacement) {
      case SwingConstants.TOP:
        x = tabPanelBounds.x + firstTabBounds.x - 1;
        y = tabPanelBounds.y;
        selectedOffset = firstTabBounds.y;
        wc = 0;
        hc = 1;
        cx = 1;
        cy = 0;
        break;
      case SwingConstants.BOTTOM:
        x = tabPanelBounds.x + firstTabBounds.x - 1;
        y = tabPanelBounds.y + tabPanelBounds.height - 1;
        selectedOffset = tabPanelBounds.height - (firstTabBounds.y + firstTabBounds.height);
        wc = 0;
        hc = -1;
        cx = 1;
        cy = 0;
        break;
      case SwingConstants.LEFT:
        x = tabPanelBounds.x;
        y = tabPanelBounds.y + firstTabBounds.y - 1;
        selectedOffset = firstTabBounds.x;
        wc = 1;
        hc = 0;
        cx = 0;
        cy = 1;
        break;
      case SwingConstants.RIGHT:
        x = tabPanelBounds.x + tabPanelBounds.width - 1;
        y = tabPanelBounds.y + firstTabBounds.y - 1;
        selectedOffset = tabPanelBounds.width - (firstTabBounds.x + firstTabBounds.width);
        wc = -1;
        hc = 0;
        cx = 0;
        cy = 1;
        break;
      default:
        throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
    }

    int w = wc * (tabPanelBounds.width - 1); // The x-axis offset from the anchor to the component
    int h = hc * (tabPanelBounds.height - 1); // The y-axis offset from the anchor to the component

    int dx, dy; // The offset to the end of the current tab

    // The lines drawn below must not overlap because they are/may be drawn with
    // a translucent color.

    // The small line from the side of the tab row to the first tab
    dx = -cx * (firstTabBounds.x - 1);
    dy = -cy * (firstTabBounds.y - 1);
    g.drawLine(x + w, y + h, x + w + dx, y + h + dy);

    int selectedIndex = model.getSelectedIndex();
    int tabCount = handlePanel.getComponentCount();

    Dimension tabSize = null;
    for (int i = 0; i < tabCount; i++) {
      tabSize = handlePanel.getComponent(i).getSize(tabSize);

      dx = cx * (tabSize.width + 1);
      dy = cy * (tabSize.height + 1);

      if (selectedIndex == i) { // The selected tab
        final int cutSize =
            2; // If you adjust this, you also need to change TabCell to match the shape of the tab

        int selectedX = x + wc * selectedOffset;
        int selectedY = y + hc * selectedOffset;
        // The line before the selected tab
        g.drawLine(selectedX + wc * cutSize, selectedY + hc * cutSize, x + w - wc, y + h - hc);

        // The line after the selected tab
        g.drawLine(
            selectedX + wc * cutSize + dx, selectedY + hc * cutSize + dy, x + w + dx, y + h + dy);

        // The line opposite to the component
        g.drawLine(
            selectedX + cx * cutSize,
            selectedY + cy * cutSize,
            selectedX - cx * cutSize + dx,
            selectedY - cy * cutSize + dy);

        // The corners
        if (cutSize > 1) {
          g.drawLine(
              selectedX + wc * cutSize + (cx - wc),
              selectedY + hc * cutSize + (cy - hc),
              selectedX + cx * cutSize - (cx - wc),
              selectedY + cy * cutSize - (cy - hc));
          g.drawLine(
              selectedX + wc * cutSize + dx - (cx + wc),
              selectedY + hc * cutSize + dy - (cy + hc),
              selectedX - cx * cutSize + dx + (cx + wc),
              selectedY - cy * cutSize + dy + (cy + hc));
        }
      } else {
        if (selectedIndex - 1 != i)
          g.drawLine(x + dx, y + dy, x + dx + w - wc, y + dy + h - hc); // The line between the tabs
        g.drawLine(
            x + w + cx, y + h + cy, x + w + dx, y + h + dy); // The line on the component's side
      }

      x += dx;
      y += dy;
    }

    // The line from the end of the tabs to the other side of the tab row
    dx = cx * (tabPanelBounds.width - x);
    dy = cy * (tabPanelBounds.height - y);
    g.drawLine(x + w, y + h, x + w + dx, y + h + dy);
  }

  /** The panel hosting the tab handles. */
  private class TabHandlesPanel extends JPanel {

    /** Creates a new <code>TabHandlesPanel</code>. */
    public TabHandlesPanel() {
      super(new TabHandlesLayout());

      setOpaque(false);
      setBackground(
          new Color(
              0, 0, 0, 30)); // Make it slightly darker than normal panels (presumably behind it)
    }

    /**
     * Paints the panel. We do this because we want the panel to be slightly darker than normal
     * panels. Why don't we just make it opaque and set the background to a solid, darker, color?
     * Because some systems (Mac OS X, for example) have non-uniform panel background, which we
     * can't replicate (in a darker variant). Why don't we make it opaque set the color to a
     * translucent one? Because Swing would then not repaint the panel behind this panel (as we've
     * told it that our panel is opaque), making our panel darker with each repaint. So we are
     * forced to make the panel non-opaque (in the constructor) and paint the translucent background
     * ourselves.
     */
    @Override
    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());

      super.paint(g);
    }
  }

  /** The layout manager we use for the main tab row. */
  private class TabHandlesLayout implements LayoutManager2 {

    /** The size of the gap between the tabs. */
    private static final int TAB_GAP = 1;

    /** The minimum sizes of the children. */
    private Dimension[] minSizes = null;

    /** The preferred sizes of the children. */
    private Dimension[] prefSizes = null;

    /** Returns the minimum sizes of the child tabs. */
    private Dimension[] getMinTabSizes() {
      if (minSizes != null) return minSizes;

      int tabCount = handlePanel.getComponentCount();
      minSizes = new Dimension[tabCount];

      for (int i = 0; i < tabCount; i++) minSizes[i] = handlePanel.getComponent(i).getMinimumSize();

      return minSizes;
    }

    /** Returns the preferred sizes of the child tabs. */
    private Dimension[] getPrefTabSizes() {
      if (prefSizes != null) return prefSizes;

      int tabCount = handlePanel.getComponentCount();
      prefSizes = new Dimension[tabCount];

      for (int i = 0; i < tabCount; i++)
        prefSizes[i] = handlePanel.getComponent(i).getPreferredSize();

      return prefSizes;
    }

    /** Lays out the tab row. */
    @Override
    public void layoutContainer(Container parent) {
      if (parent != handlePanel)
        throw new IllegalStateException("TabHandlesLayout may only be used to layout handlePanel");

      int tabPlacement = getTabPlacement();
      boolean isVertical =
          (tabPlacement == SwingConstants.LEFT) || (tabPlacement == SwingConstants.RIGHT);

      Dimension parentSize = parent.getSize();
      Insets insets = parent.getInsets();
      int d = isVertical ? insets.top : insets.left;
      int availableWidth = parentSize.width - insets.left - insets.right;
      int availableHeight = parentSize.height - insets.top - insets.bottom;

      int tabCount = parent.getComponentCount();

      // Get the preferred sizes of the tabs
      Dimension[] prefSizes = getPrefTabSizes();

      // Calculate the sizes of the tabs along the main axis
      int[] sizes = new int[tabCount];
      int totalSize = 0;
      for (int i = 0; i < tabCount; i++) {
        sizes[i] = isVertical ? prefSizes[i].height : prefSizes[i].width;
        totalSize += sizes[i];
      }

      int availableTabSize =
          (isVertical ? availableHeight : availableWidth) - TAB_GAP * (tabCount - 1);
      if (totalSize > availableTabSize) {
        Dimension[] minDims = getMinTabSizes();
        int[] minSizes = new int[minDims.length];
        for (int i = 0; i < tabCount; i++)
          minSizes[i] = isVertical ? minDims[i].height : minDims[i].width;

        fitTabs(sizes, minSizes, totalSize, availableTabSize);
      }

      // Layout
      for (int i = 0; i < tabCount; i++) {
        Component tab = parent.getComponent(i);

        int x = isVertical ? insets.left : d;
        int y = isVertical ? d : insets.top;

        int width = isVertical ? availableWidth : sizes[i];
        int height = isVertical ? sizes[i] : availableHeight;

        tab.setBounds(x, y, width, height);

        d += isVertical ? height : width;
        d += TAB_GAP;
      }

      // Seems we are sometimes painted before being properly laid out
      TabbedPane.this.repaint();
    }

    /**
     * Attempts to fit the tabs into the specified available size (isn't successful if the sum of
     * the minimum sizes is larger than the available size).
     *
     * <p>The algorithm is roughly as follows: each round, find the tabs that have the maximum
     * reduction capability (difference between current size and minimum size) and find the second
     * largest value. Reduce these tabs to the second largest value and repeat. The algorithm ends
     * when either it is impossible to reduce further (all tabs at their minimum sizes) or when the
     * possible reduction in the current round places the total size below the available size.
     */
    private void fitTabs(int[] sizes, int[] minSizes, int totalSize, int availableSize) {
      int count = sizes.length;
      int largestIndices[] =
          new int[count]; // The indices of the tabs which can be maximally reduced
      int largestCount; // The number of such tabs

      while (totalSize > availableSize) {
        // Find the indices with the largest difference between size and minimum size
        int largestDiff = 0;
        int secondLargest = 0;
        largestCount = 0;
        for (int i = 0; i < sizes.length; i++) {
          int diff = sizes[i] - minSizes[i];
          if (diff > largestDiff) {
            largestCount = 1;
            largestIndices[0] = i;
            secondLargest = largestDiff;
            largestDiff = diff;
          } else if (diff == largestDiff) largestIndices[largestCount++] = i;
          else if (diff > secondLargest) secondLargest = diff;
        }

        if (largestDiff == 0) return; // Nothing else we can do

        // Reduce them
        if (totalSize - availableSize < largestCount) {
          // Less pixels needed than there are reducible tabs.
          // Reduce only the first totalSize - availableSize tabs, each by 1
          for (int i = 0; i < totalSize - availableSize; i++) sizes[largestIndices[i]]--;

          totalSize = availableSize;
        } else if (totalSize - availableSize < largestCount * (largestDiff - secondLargest)) {
          // Less pixels needed than we reduce in this round
          // Reduce each tab by almost the same amount - some of the first tabs
          // will be reduced by 1 pixel more.
          int baseReduction = (int) Math.floor((totalSize - availableSize) / (double) largestCount);
          int extraReductionCount = totalSize - availableSize - baseReduction * largestCount;

          for (int i = 0; i < extraReductionCount; i++)
            sizes[largestIndices[i]] -= (baseReduction + 1);

          for (int i = extraReductionCount; i < largestCount; i++)
            sizes[largestIndices[i]] -= baseReduction;

          totalSize = availableSize;
        } else {
          // More pixels needed than we can reduce in this round
          // Reduce each tab by largestDiff - secondLargest
          int reduction = largestDiff - secondLargest;
          for (int i = 0; i < largestCount; i++) sizes[largestIndices[i]] -= reduction;

          totalSize -= reduction * largestCount;
        }
      }
    }

    /** Returns the layout size for the specified tab sizes. */
    private Dimension layoutSize(Dimension[] tabSizes) {
      int tabPlacement = getTabPlacement();
      boolean isVertical =
          (tabPlacement == SwingConstants.LEFT) || (tabPlacement == SwingConstants.RIGHT);

      int mainSize = 0; // The size along the main axis
      int secondarySize = 0; // The size along the secondary axis
      for (int i = 0; i < tabSizes.length; i++) {
        Dimension size = tabSizes[i];

        mainSize += isVertical ? size.height : size.width;
        if (i != tabSizes.length - 1) mainSize += TAB_GAP;
        secondarySize = Math.max(secondarySize, isVertical ? size.width : size.height);
      }

      Insets insets = handlePanel.getInsets();
      return new Dimension(
          (isVertical ? secondarySize : mainSize) + insets.left + insets.right,
          (isVertical ? mainSize : secondarySize) + insets.top + insets.bottom);
    }

    /** Returns the minimum size for the tab row. */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
      if (parent != handlePanel)
        throw new IllegalStateException("TabHandlesLayout may only be used to layout handlePanel");

      return layoutSize(getMinTabSizes());
    }

    /** Returns the preferred size for the tab row. */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
      if (parent != handlePanel)
        throw new IllegalStateException("TabHandlesLayout may only be used to layout handlePanel");

      return layoutSize(getPrefTabSizes());
    }

    /** Returns the maximum size for the tab row. */
    @Override
    public Dimension maximumLayoutSize(Container target) {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** Deprecated. Throws an exception. */
    @Override
    public void addLayoutComponent(String name, Component comp) {
      throw new UnsupportedOperationException("deprecated addLayoutComponent(String, Component)");
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    /** Returns our layout alignment along the X axis. */
    @Override
    public float getLayoutAlignmentX(Container target) {
      return Component.LEFT_ALIGNMENT;
    }

    /** Returns our layout alignment along the Y axis. */
    @Override
    public float getLayoutAlignmentY(Container target) {
      return Component.CENTER_ALIGNMENT;
    }

    /** Flushes cached size values. */
    @Override
    public void invalidateLayout(Container target) {
      minSizes = null;
      prefSizes = null;
    }
  }

  /** A component which holds the tab handle component. */
  private class TabCell extends JPanel {

    /** The handle sits inside this panel. */
    private final JPanel mainPanel = new JPanel();

    /**
     * This is a 1-pixel tall (or wide, in vertical mode) panel which goes between the main panel
     * and the tab component. Its whole point is that it is 1 pixel shorter on each side (of the
     * main axis), allowing the round corners of the selected tab to be drawn properly.
     */
    private final JPanel smallPanel = new JPanel();

    /** Creates a new <code>TabCell</code> for the specified tab handle. */
    public TabCell(TabHandle tabHandle, boolean initiallySelected) {
      int tabPlacement = getTabPlacement();
      boolean isVertical =
          (tabPlacement == SwingConstants.LEFT) || (tabPlacement == SwingConstants.RIGHT);

      mainPanel.setLayout(WrapLayout.getInstance());
      mainPanel.add(tabHandle.getComponent());

      JPanel secondaryPanel = new JPanel(WrapLayout.getInstance());
      secondaryPanel.setOpaque(false);
      secondaryPanel.add(smallPanel);
      if (isVertical) secondaryPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
      else secondaryPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
      secondaryPanel.setMinimumSize(new Dimension(1, 1));
      secondaryPanel.setPreferredSize(new Dimension(1, 1));

      setLayout(new BorderLayout(0, 0));
      add(mainPanel, BorderLayout.CENTER);

      Object secondaryPosition;
      switch (tabPlacement) {
        case SwingConstants.TOP:
          secondaryPosition = BorderLayout.NORTH;
          break;
        case SwingConstants.BOTTOM:
          secondaryPosition = BorderLayout.SOUTH;
          break;
        case SwingConstants.LEFT:
          secondaryPosition = BorderLayout.WEST;
          break;
        case SwingConstants.RIGHT:
          secondaryPosition = BorderLayout.EAST;
          break;
        default:
          throw new IllegalStateException("Unknown tab placement: " + getTabPlacement());
      }
      add(secondaryPanel, secondaryPosition);

      setOpaque(false);
      setSelected(initiallySelected);
    }

    /** Notifies us of the selection status of the tab whose handle we're holding. */
    public void setSelected(boolean isSelected) {
      mainPanel.setOpaque(isSelected);
      smallPanel.setOpaque(isSelected);
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    TabbedPane tabbedPane = new TabbedPane(SwingConstants.BOTTOM);
    tabbedPane.setBorder(null);

    Tab[] tabs =
        new Tab[] {
          new Tab(new JLabel(""), "Console", null, false),
          new Tab(new JLabel(""), "Channel 100", null, true),
          new Tab(new JLabel(""), "Channel 0", null, true),
          new Tab(new JLabel(""), "AlexTheGreat", null, true),
          new Tab(new JLabel(""), "Kasparov vs. Karpov", null, true),
        };

    for (int i = 0; i < tabs.length; i++) tabbedPane.getModel().addTab(tabs[i], i);

    tabbedPane.getModel().setSelectedIndex(1);

    JPanel contentPane = new JPanel(new BorderLayout());
    //    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPane.add(tabbedPane);
    frame.setContentPane(contentPane);

    frame.setBounds(300, 300, 650, 400);
    frame.setVisible(true);
  }
}
