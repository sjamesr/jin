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

package free.jin;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;



/**
 * A preferences panel for selecting the current Swing Look and Feel.
 */
 
public class LookAndFeelPrefPanel extends PreferencesPanel{
  
  
  
  /**
   * The list of tree roots to update when the look and feel changes.
   */
   
  private final Component [] treeRoots;
  
  
  
  /**
   * The list of available Look and Feels. This actually holds instances of
   * <code>LnF</code>.
   */
   
  private final JList lookAndFeels;
  
  
  
  /**
   * Creates a new <code>LookAndFeelPrefPanel</code> with the specified list
   * tree roots to update when the look and feel changes.
   */
  
  public LookAndFeelPrefPanel(Component [] treeRoots){
    this.treeRoots = treeRoots;
    
    UIManager.LookAndFeelInfo [] installedLnfs = UIManager.getInstalledLookAndFeels(); 
    LnF [] lnfs = new LnF[installedLnfs.length];
    for (int i = 0; i < lnfs.length; i++)
      lnfs[i] = new LnF(installedLnfs[i]);
    
    this.lookAndFeels = new JList(lnfs);
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this panel.
   */
   
  private void createUI(){
    lookAndFeels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lookAndFeels.setVisibleRowCount(Math.max(5, Math.min(lookAndFeels.getModel().getSize(), 10)));

    JScrollPane scrollPane = new JScrollPane(lookAndFeels);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    JLabel chooseLabel = new JLabel("Choose a Look and Feel");
    chooseLabel.setDisplayedMnemonic('C');
    chooseLabel.setLabelFor(lookAndFeels);
    
    JLabel warningLabel1 = new JLabel("It is recommended to restart Jin after");
    JLabel warningLabel2 = new JLabel("changing the Look and Feel");
    JPanel warningPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    warningPanel.add(warningLabel1);
    warningPanel.add(warningLabel2);
    
    setLayout(new BorderLayout(5, 5));
    add(chooseLabel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(warningPanel, BorderLayout.SOUTH);
    
    // Select the right current look and feel
    String currentLnfClassName = UIManager.getLookAndFeel().getClass().getName();
    for (int i = 0; i < lookAndFeels.getModel().getSize(); i++){
      String classname = ((LnF)lookAndFeels.getModel().getElementAt(i)).classname;
      if (currentLnfClassName.equals(classname)){
        lookAndFeels.setSelectedIndex(i);
        lookAndFeels.ensureIndexIsVisible(i);
        break;
      }
    }
    
    lookAndFeels.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        fireStateChanged();
      }
    });
  }
  
  
  
  /**
   * Applies the selected look and feel selection to the list of tree roots
   * specified in the constructor.
   */
   
  public void applyChanges() throws BadChangesException{
    try{
      LnF lnf = (LnF)lookAndFeels.getSelectedValue();
      if (lnf == null)
        throw new BadChangesException("No Look and Feel selected", this);
      
      UIManager.setLookAndFeel(lnf.classname);
      
      for (int i = 0 ; i < treeRoots.length; i++)
        SwingUtilities.updateComponentTreeUI(treeRoots[i]);
      
      SwingUtilities.updateComponentTreeUI(SwingUtilities.getAncestorOfClass(Window.class, this));
      
      // JOptionPane.showMessageDialog(null, "It is advisable to restart the application\n"+
      //   "for the look and feel to take full effect.", "Look and Feel change", JOptionPane.INFORMATION_MESSAGE);
    } catch (UnsupportedLookAndFeelException e){
        throw new BadChangesException("This Look and Feel is not supported on your platform", this);
      }
      catch (ClassNotFoundException e){
        e.printStackTrace();
        throw new BadChangesException("Unable to load the Look and Feel", this);
      }
      catch (InstantiationException e){
        e.printStackTrace();
        throw new BadChangesException("Unable to instantiate the look and feel (InstantiationException)", this);
      }
      catch (IllegalAccessException e){
        e.printStackTrace();
        throw new BadChangesException("Unable to instantiate the look and feel (IllegalAccessException)", this);
      }
  }
  
  
  
  /**
   * A small class which holds the name and classname of a look and feel, and
   * also returns the name in its <code>toString</code> method. We use instances
   * of this class as the elements in the lookAndFeels list.
   */
   
  private static class LnF{
    
    
    /**
     * The Look and Feel name.
     */
     
    public final String name;
    
    
    
    /**
     * The Look and Feel classname.
     */
     
    public final String classname;
    
    
    
    /**
     * Creates a new LnF from the specified
     * <code>UIManager.LookAndFeelInfo</code>.
     */
     
    public LnF(UIManager.LookAndFeelInfo info){
      this.name = info.getName();
      this.classname = info.getClassName();;
    }
    
    
    
    /**
     * Returns the name of the Look and Feel.
     */
     
    public String toString(){
      return name;
    }
    
  }
  

  
  
}
 