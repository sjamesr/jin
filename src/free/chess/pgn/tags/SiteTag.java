/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess.pgn.tags;


/**
 * A representation of the Site PGN tag. The Site tag specifies the location of
 * the event. The Site tag value should specify city and region names along
 * with a standard name for the country.
 */

public final class SiteTag extends SimpleTag{


  /**
   * The name of the Site tag - the String "Site".
   */

  public static final String TAG_NAME = "Site";



  /**
   * A SiteTag instance representing a Site tag with an unknown value.
   */

  public static final SiteTag SITE_UNKNOWN = new SiteTag("?");



  /**
   * Creates a new SiteTag with the specified site name. The site name may not
   * be an empty string. The event name may be <code>null</code> to specify that
   * the site is unknown, but it's preferable to use the
   * <code>SITE_UNKNOWN</code> SiteTag constant for that.
   */

  public SiteTag(String siteName){
    super(TAG_NAME, siteName == null ? "?" : siteName);

    if ("".equals(siteName))
      throw new IllegalArgumentException("The site name may not be an empty string");
  }




  /**
   * Creates a new SiteTag with the specified city name, region name and country
   * name/code. The use of the IOC (International Olympic Committee) three
   * letter names is suggested for those countries where such codes are
   * available. Any of the three values may be <code>null</code>, specifying
   * that the location is either unknown or not applicable, but at least one
   * value must be non <code>null</code>. All three strings may not be empty.
   */

  public SiteTag(String cityName, String regionName, String countryName){
    this(createSiteName(cityName, regionName, countryName));
  }




  /**
   * Creates a site name from the specified city, region and country names.
   * At least one of the three values must be non <code>null</code> and .
   */

  private static String createSiteName(String cityName, String regionName, String countryName){
    if ((cityName == null) && (regionName == null) && (countryName == null))
      throw new IllegalArgumentException("At least one of the city name, region name and country name values must be non null");
    if ("".equals(cityName))
      throw new IllegalArgumentException("The city name may not be an empty string");
    if ("".equals(regionName))
      throw new IllegalArgumentException("The region name may not be an empty string");
    if ("".equals(countryName))
      throw new IllegalArgumentException("The country name may not be an empty string");

    StringBuffer siteName = new StringBuffer();
    if (cityName != null)
      siteName.append(cityName);
    if ((regionName != null) || (countryName != null))
      siteName.append(",");
    if (regionName != null)
      siteName.append(" "+regionName);
    if (countryName != null)
      siteName.append(" "+countryName);

    return siteName.toString();
  }



  /**
   * Returns the name of the site, or <code>null</code> if the site is unknown.
   */

  public String getSiteName(){
    String tagValue = getValue();
    return "?".equals(tagValue) ? null : tagValue;
  }


}

 