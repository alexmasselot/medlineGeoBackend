package ch.fram.medlineGeo.crunching.localize.geonames

import com.typesafe.config.ConfigFactory


/**
 * Created by alex on 13/09/15.
 */
class GeonameCountries {

}

object GeonameCountries{
  def srcFile = {
    ConfigFactory.defaultApplication().getString("thirdParties.geonames.countryFile")
  }
  def load() = {
    val filename = ""

  }
}
