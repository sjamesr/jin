package free.jin.ui;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;



/**
 * A <code>CompositePreferencesPanel</code> which puts its children preferences
 * panels into a tabbed pane.  
 * 
 * @author Maryanovsky Alexander
 */

public class TabbedPreferencesPanel extends CompositePreferencesPanel{
  
  
  
  /**
   * The tabbed pane.
   */
  
  protected final JTabbedPane tabs = new JTabbedPane();
  
  
  
  /**
   * Whether the UI has already been created (.
   */
  
  private boolean uiCreated = false;
  
  
  
  /**
   * If {@link #createLayout()} hasn't been called yet, calls it.
   */
  
  public void addNotify(){
    super.addNotify();
    
    if (!uiCreated){
      uiCreated = true;
      createLayout();
    }
  }
  
  
  
  /**
   * Creates the layout of this panel, adding the tabbed pane.
   */
  
  protected void createLayout(){
    setLayout(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }
  
  
  
  /**
   * Adds the panel to the tabbed pane. 
   */
  
  protected void addPanelToUi(PreferencesPanel panel, String panelTitle, String panelToolTip){
    tabs.addTab(panelTitle, null, panel, panelToolTip);
  }
  
  
  
}
