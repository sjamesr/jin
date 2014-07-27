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

package free.jin.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.ImmutableList;

import free.jin.I18n;
import free.jin.Jin;
import free.jin.Server;
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
   * Creates a new <code>ServerChoicePanel</code>.
   */

  public ServerChoicePanel(){
    serverListModel = new DefaultListModel();
    List<Server> servers = ImmutableList.copyOf(Jin.getInstance().getServers());
    for (Server server : servers)
      serverListModel.addElement(server);

    createUI();
  }



  /**
   * Displays this panel and returns the selected server.
   */

  public Server askServer(){
    return (Server)super.askResult();
  }



  /**
   * Returns the title for this panel.
   */

  @Override
  protected String getTitle(){
    return I18n.get(ServerChoicePanel.class).getString("title");
  }



  /**
   * Creates the UI for this dialog.
   */

  private void createUI(){
    I18n i18n = I18n.get(ServerChoicePanel.class);
    
    final JList list = new JList(serverListModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setVisibleRowCount(Math.max(5, Math.min(serverListModel.getSize(), 10)));

    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JLabel chooseLabel = i18n.createLabel("chooseServerLabel");
    chooseLabel.setLabelFor(list);

    final JButton okButton = i18n.createButton("okButton");
    JButton cancelButton = i18n.createButton("cancelButton");
    final JButton showWebsiteButton = i18n.createButton("serverWebsiteButton");

    okButton.setEnabled(false);
    showWebsiteButton.setEnabled(false);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(showWebsiteButton);

    list.addListSelectionListener(new ListSelectionListener(){
      @Override
      public void valueChanged(ListSelectionEvent evt){
        int selectedIndex = list.getSelectedIndex();
        boolean enabled = (selectedIndex != -1);
        okButton.setEnabled(enabled);
        showWebsiteButton.setEnabled(enabled);
      }
    });

    okButton.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent evt){
        close(list.getSelectedValue());
      }
    });

    list.addMouseListener(new MouseAdapter(){
      @Override
      public void mouseClicked(MouseEvent evt){
        if ((evt.getClickCount() == 2) && (evt.getModifiers() == InputEvent.BUTTON1_MASK)){
          Object result = list.getSelectedValue();
          if (result != null)
            close(result);
        }
      }
    });

    cancelButton.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent evt){
        close(null);
      }
    });

    showWebsiteButton.addActionListener(new ActionListener(){
      @Override
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