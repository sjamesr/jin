/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import free.util.BrowserControl;


/**
 * A panel allowing the user to choose a server to connect to.
 */

public class ServerChoicePanel extends DialogPanel{



  /**
   * The ListModel holding the server list.
   */

  private final DefaultListModel serverListModel;



  /**
   * Creates a new <code>ServerChoicePanel</code> with the specified list of
   * servers.
   */

  public ServerChoicePanel(Server [] serverList){
    serverListModel = new DefaultListModel();
    for (int i = 0; i < serverList.length; i++)
      serverListModel.addElement(serverList[i]);

    createUI();
  }



  /**
   * Displays this panel using the specified <code>UIProvider</code> and returns
   * the selected server.
   */

  public Server askServer(UIProvider uiProvider){
    return (Server)super.askResult(uiProvider);
  }



  /**
   * Returns the title for this panel.
   */

  protected String getTitle(){
    return "Choose Server";
  }



  /**
   * Creates the UI for this dialog.
   */

  private void createUI(){
    final JList list = new JList(serverListModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(300, 80));

    JLabel chooseLabel = new JLabel("Choose a server to connect to");
    chooseLabel.setDisplayedMnemonic('C');
    chooseLabel.setLabelFor(list);

    final JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    final JButton showWebsiteButton = new JButton("Server's Website");
    showWebsiteButton.setMnemonic('s');

    okButton.setEnabled(false);
    showWebsiteButton.setEnabled(false);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(showWebsiteButton);

    list.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        int selectedIndex = list.getSelectedIndex();
        boolean enabled = (selectedIndex != -1);
        okButton.setEnabled(enabled);
        showWebsiteButton.setEnabled(enabled);
      }
    });

    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        close(list.getSelectedValue());
      }
    });

    list.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent evt){
        if ((evt.getClickCount() == 2) && (evt.getModifiers() == KeyEvent.BUTTON1_MASK)){
          Object result = list.getSelectedValue();
          if (result != null)
            close(result);
        }
      }
    });

    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        close(null);
      }
    });

    showWebsiteButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Server server = (Server)list.getSelectedValue();
        String url = server.getWebsite();
        if (!BrowserControl.displayURL(url))
          BrowserControl.showDisplayBrowserFailedDialog(url, ServerChoicePanel.this, true);
      }
    });

    setLayout(new BorderLayout(5, 5));
    add(chooseLabel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    setDefaultButton(okButton);
  }



}