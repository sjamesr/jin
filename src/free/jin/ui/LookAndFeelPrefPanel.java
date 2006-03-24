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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Jin;
import free.util.PlatformUtils;



/**
 * A preferences panel for selecting the current Swing Look and Feel.
 */
 
public class LookAndFeelPrefPanel extends PreferencesPanel{
  
  
  
  /**
   * Have we installed the extra look and feels already?
   */
  
  private static boolean extraLnFsInstalled = false;
  
  
  
  /**
   * The list of available Look and Feels. This actually holds instances of
   * <code>LnF</code>.
   */
   
  private final JList lookAndFeels;
  
  
  
  /**
   * Creates a new <code>LookAndFeelPrefPanel</code>.
   */
  
  public LookAndFeelPrefPanel(){
    installExtraLookAndFeels();
    
    UIManager.LookAndFeelInfo [] installedLnfs = UIManager.getInstalledLookAndFeels(); 
    LnF [] lnfs = new LnF[installedLnfs.length];
    for (int i = 0; i < lnfs.length; i++){
      lnfs[i] = new LnF(installedLnfs[i]);
    }
    
    // WORKAROUND: GTK Look and Feel is broken for now in 1.5.0 with an applet
    // Remove this when Sun fixes it.
    if ((System.getSecurityManager() != null) && PlatformUtils.isJavaBetterThan("1.5")){
      int gtkIndex = -1;
      for (int i = 0; i < lnfs.length; i++){
        if (lnfs[i].classname.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")){
          gtkIndex = i;
          break;
        }
      }
      if (gtkIndex != -1){
        LnF [] lnfs2 = new LnF[lnfs.length - 1];
        for (int i = 0; i < gtkIndex; i++)
          lnfs2[i] = lnfs[i];
        for (int i = gtkIndex; i < lnfs2.length; i++)
          lnfs2[i] = lnfs[i + 1];
        lnfs = lnfs2;
      }
    }    
    this.lookAndFeels = new JList(lnfs);
    
    // Select the right current look and feel
    String lnfClassName = Jin.getInstance().getPrefs().getString("lookAndFeel.classname");
    for (int i = 0; i < lookAndFeels.getModel().getSize(); i++){
      String classname = ((LnF)lookAndFeels.getModel().getElementAt(i)).classname;
      if (lnfClassName.equals(classname)){
        lookAndFeels.setSelectedIndex(i);
        lookAndFeels.ensureIndexIsVisible(i);
        break;
      }
    }
    
    
    createUI();
  }
  
  

  
  /**
   * Installs any extra look and feels Jin is using.
   */
  
  private static synchronized void installExtraLookAndFeels(){
    if (extraLnFsInstalled)
      return;
    extraLnFsInstalled = true;
    
    int extraLooksCount = Integer.parseInt(Jin.getInstance().getAppProperty("lf.extra.count", "0"));
    for (int i = 0; i < extraLooksCount; i++){
      String name = Jin.getInstance().getAppProperty("lf.extra." + i + ".name", null);
      String className = Jin.getInstance().getAppProperty("lf.extra." + i + ".class", null);
      String minRequiredJavaVer = Jin.getInstance().getAppProperty("lf.extra." + i + ".minRequiredJava", "0");
      if (PlatformUtils.isJavaBetterThan(minRequiredJavaVer)){
        try{
          Class.forName(className);
          UIManager.installLookAndFeel(name, className);
        } catch (ClassNotFoundException e){
            // We used to print a message here, but too many people panic and
            // think something is terribly wrong.
          } 
      }
    }
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

    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    add(Box.createHorizontalStrut(150), BorderLayout.SOUTH);
    
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
    LnF lnf = (LnF)lookAndFeels.getSelectedValue();
    if (lnf == null){
      I18n i18n = I18n.get(LookAndFeelPrefPanel.class);
      throw new BadChangesException(i18n.getString("noLookNFeelSelectedErrorMessage"), this);
    }
    
    Jin.getInstance().getPrefs().setString("lookAndFeel.classname", lnf.classname);
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
 