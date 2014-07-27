/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.jin.I18n;
import free.jin.Jin;
import free.util.AWTUtilities;
import free.util.BrowserControl;
import free.util.swing.SwingUtils;



/**
 * A dialog notifying the user of the availability of a new version of Jin. 
 */

public class UpgradeDialog{
  
  
  
  /**
   * The dialog.
   */
  
  private JDialog dialog;
  
  
  
  /**
   * Creates the dialog.
   */
  
  private JDialog createDialog(){
    I18n i18n = I18n.get(UpgradeDialog.class);
    
    JLabel icon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
    JLabel [] labels = SwingUtils.labelsForLines(i18n.getFormattedString("text", 
        new Object[]{Jin.getAppName()})); 
    JButton visitWebsiteButton = i18n.createButton("visitWebsiteButton");
    JButton remindLaterButton = i18n.createButton("remindLaterButton");
    
    visitWebsiteButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        String website = Jin.getInstance().getPrefs().getString("upgradeURL");
        BrowserControl.displayURL(website);
        dialog.dispose();
      }
    });
    
    remindLaterButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        Jin.getInstance().getPrefs().setLong("upgradeRemindTime",
            System.currentTimeMillis() + 1000L*60/* *60*24*3 */); // Remind in 3 days
        dialog.dispose();
      }
    });
    
    JPanel panel = new JPanel();
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    
    layout.setAutocreateContainerGaps(true);
    
    GroupLayout.ParallelGroup hLabelsGroup = layout.createParallelGroup(GroupLayout.LEADING);
    GroupLayout.SequentialGroup vLabelsGroup = layout.createSequentialGroup();
    
    for (int i = 0; i < labels.length; i++){
      hLabelsGroup.add(labels[i]);
      vLabelsGroup.add(labels[i]);
    }
    
    GroupLayout.SequentialGroup hButtonsGroup = layout.createSequentialGroup();
    hButtonsGroup.addPreferredGap(LayoutStyle.RELATED, 1, Integer.MAX_VALUE);
    if (SwingUtils.isMacLnF() || SwingUtils.isGtkLnF())
      hButtonsGroup
        .add(remindLaterButton)
        .addPreferredGap(LayoutStyle.RELATED)
        .add(visitWebsiteButton);
    else
      hButtonsGroup
        .add(visitWebsiteButton)
        .addPreferredGap(LayoutStyle.RELATED)
        .add(remindLaterButton);
    
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.TRAILING)
      .add(layout.createSequentialGroup()
        .add(icon)
        .addPreferredGap(LayoutStyle.RELATED)
        .add(hLabelsGroup)
      )
      .add(hButtonsGroup)
    );
    
    layout.setVerticalGroup(layout.createSequentialGroup()
      .add(layout.createParallelGroup(GroupLayout.CENTER)
        .add(icon)
        .add(vLabelsGroup)
      )
      .addPreferredGap(LayoutStyle.UNRELATED)
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(remindLaterButton)
        .add(visitWebsiteButton)
      )
    );
    
    JDialog dialog = new JDialog((Frame)null, i18n.getString("title"));
    dialog.setContentPane(panel);
    
    dialog.getRootPane().setDefaultButton(visitWebsiteButton);
    
    SwingUtils.registerEscapeCloser(dialog);
    
    return dialog;
  }
  
  
  
  /**
   * Displays the dialog.
   */
  
  public void show(){
    dialog = createDialog();
    dialog.setModal(true);
    
    AWTUtilities.centerWindow(dialog, null);
    dialog.setVisible(true);
  }
  
  
  
}
