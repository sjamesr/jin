/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin;

import free.jin.ui.UpgradeDialog;
import free.util.IOUtilities;
import free.util.Utilities;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/** Manages the process of upgrading Jin when a new version becomes available. */
public class UpgradeManager {

  /** Invoked when Jin starts. */
  public static void start() {
    validateLocalData();

    Preferences prefs = Jin.getInstance().getPrefs();
    String appVersion = Jin.getAppVersion();
    boolean newerVersionAvailable =
        prefs.getString("latestVersion", appVersion).compareTo(appVersion) > 0;
    boolean remindUpgrade =
        prefs.getLong("upgradeRemindTime", System.currentTimeMillis())
            <= System.currentTimeMillis();

    if (newerVersionAvailable && remindUpgrade) new UpgradeDialog().show();
    else {
      Thread t =
          new Thread("NewVersionChecker") {
            @Override
            public void run() {
              // checkNewVersion();
            }
          };
      t.setDaemon(true);
      t.start();
    }
  }

  /**
   * Checks if the latest version data URL is different from the one we remembered and if so, clears
   * the cached latest data version.
   */
  private static void validateLocalData() {
    Preferences prefs = Jin.getInstance().getPrefs();
    String oldLatestVersionDataUrl = prefs.getString("latestVersionDataUrl", null);
    String latestVersionDataUrl = Jin.getAppProperty("app.latestVersionDataURL", null);

    if (!Utilities.areEqual(oldLatestVersionDataUrl, latestVersionDataUrl)) {
      // The url changed, so clear our remembered values
      prefs.remove("latestVersion");
      prefs.remove("upgradeUrl");
      prefs.setString("latestVersionDataUrl", latestVersionDataUrl);
    }
  }

  /** Checks whether a new version of Jin is available. */
  private static void checkNewVersion() {
    try {
      String urlString = Jin.getAppProperty("app.latestVersionDataURL", null);
      if (urlString == null) return;

      Properties props = IOUtilities.loadProperties(new URL(urlString), false);

      Preferences prefs = Jin.getInstance().getPrefs();
      prefs.setString("latestVersion", props.getProperty("version"));
      prefs.setString("upgradeURL", props.getProperty("url"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
