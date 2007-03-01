/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.Session;
import free.jin.SessionEvent;
import free.jin.SessionListener;
import free.jin.plugin.Plugin;
import free.util.AWTUtilities;
import free.util.PlatformUtils;
import free.util.WindowDisposingListener;
import free.util.models.BooleanModel;
import free.util.models.Model;
import free.util.models.ModelUtils;
import free.util.swing.SwingUtils;


/**
 * The preferences menu.
 */

public class PrefsMenu extends JMenu implements SessionListener{
  
  
  
  /**
   * The "Look and Feel" menu item.
   */
   
  private final JMenuItem lnfMenu;



  /**
   * Are we currently in the "connected" state?
   */

  private boolean isConnected = false;



  /**
   * The index of the separator between global preferences and plugin
   * preferences. <code>-1</code> when none.
   */

  private int separatorIndex = -1;



  /**
   * The plugins in the current session, <code>null</code> when none.
   */

  private Plugin [] plugins = null;



  /**
   * Creates a new <code>PreferencesMenu</code>.
   */

  public PrefsMenu(){
    I18n i18n = I18n.get(PrefsMenu.class);
    
    i18n.initAbstractButton(this, "this");

    add(lnfMenu = i18n.createMenuItem("userInterfaceMenuItem"));
    lnfMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Frame parentFrame = AWTUtilities.frameForComponent(PrefsMenu.this); 
        JDialog dialog = new PrefsDialog(parentFrame, 
          I18n.get(PrefsMenu.class).getString("userInterfacePrefsDialog.title"), new UiPrefsPanel());
        AWTUtilities.centerWindow(dialog, parentFrame);
        dialog.setVisible(true);
      }
    });
  }
  
  
  
  /**
   * Registers us as session listener.
   */
  
  public void addNotify(){
    super.addNotify();
    
    Jin.getInstance().getConnManager().addSessionListener(this);
    Session session = Jin.getInstance().getConnManager().getSession();
    setConnected(session != null, session);
  }
  
  
  
  /**
   * Unregisters us as a session listener.
   */
  
  public void removeNotify(){
    super.removeNotify();
    
    Jin.getInstance().getConnManager().removeSessionListener(this);
  }
  
  
  
  /**
   * SessionListener implementation. Simply delegates to
   * <code>setConnected</code>.
   */
  
  public void sessionEstablished(SessionEvent evt){
    setConnected(true, evt.getSession());
  }
  
  
  
  /**
   * SessionListener implementation. Simply delegates to
   * <code>setConnected</code>.
   */
  
  public void sessionClosed(SessionEvent evt){
    setConnected(false, evt.getSession());
  }
  



  /**
   * Modifies the state of the menu to match the specified state.
   */

  public void setConnected(boolean isConnected, Session session){
    if (this.isConnected == isConnected)
      return;

    this.isConnected = isConnected;

    this.plugins = session.getPlugins();

    if (isConnected)
      addPluginPreferenceMenuItems();
    else
      removePluginPreferenceMenuItems();
  }



  /**
   * Adds the menu items for opening preference dialogs for the specified list
   * of plugins.
   */

  private void addPluginPreferenceMenuItems(){
    separatorIndex = getItemCount();
    addSeparator();

    for (int i = 0; i < plugins.length; i++){
      Plugin plugin = plugins[i];
      if (!plugin.hasPreferencesUI())
        continue;

      JMenuItem menuItem = new JMenuItem(plugins[i].getName() + "...");
      menuItem.setActionCommand(String.valueOf(i));
      menuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          int pluginIndex = Integer.parseInt(evt.getActionCommand());
          Plugin plugin = plugins[pluginIndex];
          
          I18n i18n = I18n.get(PrefsMenu.class);
          String pluginPrefsDialogTitle = 
            i18n.getFormattedString("pluginPrefsDialog.title", new Object[]{plugin.getName()});
          
          Frame parentFrame = AWTUtilities.frameForComponent(PrefsMenu.this);
          JDialog dialog = new PrefsDialog(parentFrame, pluginPrefsDialogTitle, plugin.getPreferencesUI());
          AWTUtilities.centerWindow(dialog, parentFrame);
          dialog.setVisible(true);
        }
      });
      add(menuItem);
    }
    
    addSeparator();
    
    for (int i = 0; i < plugins.length; i++){
      Plugin plugin = plugins[i];
      Model [] models = plugin.getHotPrefs();
      if (models == null)
        continue;
      
      for (int j = 0; j < models.length; j++){
        Model model = models[j];
        if (model instanceof BooleanModel){
          addBooleanPref((BooleanModel)model);
        }
        else
          throw new IllegalArgumentException("Unsupported model: " + model);
      }
    }
  }
  
  
  
  /**
   * Adds a checkbox menu item to toggle the state of the specified boolean
   * model.
   */
  
  private void addBooleanPref(BooleanModel model){
    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(model.getName(), model.get());
    ModelUtils.link(model, menuItem.getModel());
    add(menuItem);
  }



  /**
   * Removes the menu items associated with displaying the preference panels
   * of the various plugins.
   */

  private void removePluginPreferenceMenuItems(){
    while (separatorIndex < getItemCount())
      remove(separatorIndex);

    separatorIndex = -1;
  }


  
  
  /**
   * The dialog displaying the preferences panel for a specified plugin.
   */

  private class PrefsDialog extends JDialog implements ChangeListener, ActionListener{



    /**
     * The preferences panel.
     */

    private final PreferencesPanel prefsPanel;



    /**
     * The ok button.
     */

    private final JButton okButton;



    /**
     * The apply button.
     */

    private final JButton applyButton;



    /**
     * The cancel button.
     */

    private final JButton cancelButton;



    /**
     * Creates a new <code>PrefsDialog</code> with the specified parent frame,
     * title and preferences panel.
     */

    public PrefsDialog(Frame parent, String title, PreferencesPanel prefsPanel){
      super(parent, title, true);
      
      I18n i18n = I18n.get(PrefsMenu.class);

      this.prefsPanel = prefsPanel;
      this.applyButton = i18n.createButton("prefsDialog.applyButton");
      this.okButton = i18n.createButton("prefsDialog.okButton");
      this.cancelButton = i18n.createButton("prefsDialog.cancelButton");

      createUI();

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      SwingUtils.registerEscapeCloser(this);

      okButton.addActionListener(this);
      applyButton.addActionListener(this);
      cancelButton.addActionListener(new WindowDisposingListener(this));
      prefsPanel.addChangeListener(this);
    }



    /**
     * Creates the UI.
     */

    private void createUI(){
      Container content = getContentPane();
      content.setLayout(new BorderLayout());

      JPanel prefWrapperPanel = new JPanel(new BorderLayout());
      prefWrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      prefWrapperPanel.add(prefsPanel, BorderLayout.CENTER);
      content.add(prefWrapperPanel, BorderLayout.CENTER);

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      applyButton.setEnabled(false);

      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      buttonPanel.add(applyButton);
      bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
      
      // To prevent overlapping the window resize handle
      if (PlatformUtils.isMacOSX())
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

      content.add(bottomPanel, BorderLayout.SOUTH);
      this.getRootPane().setDefaultButton(okButton);
    }



    /**
     * ChangeListener implementation. Registered with the preferences panel,
     * enables the apply button when invoked.
     */

    public void stateChanged(ChangeEvent evt){
      applyButton.setEnabled(true);
    }



    /**
     * ActionListener implementation. Registered with the ok and apply buttons.
     */

    public void actionPerformed(ActionEvent evt){
      try{
        if (applyButton.isEnabled())
          prefsPanel.applyChanges();
        applyButton.setEnabled(false);

        if (evt.getSource() == okButton)
          dispose();
      } catch (BadChangesException e){
          OptionPanel.error(I18n.get(PrefsMenu.class).getString("badPrefsChangeDialog.title"), e.getMessage(), this);
          if (e.getErrorComponent() != null)
            e.getErrorComponent().requestFocus();
        }
    }

  }

}
