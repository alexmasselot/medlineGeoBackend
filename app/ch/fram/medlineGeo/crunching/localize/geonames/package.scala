package ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.models.Location

/**
 * Created by alex on 14/09/15.
 */
package object geonames {

  trait GeonamesEntity {
    val id: Long
    val name: String
  }

  case class GeonamesCountry(id: Long, iso: String, name: String) extends GeonamesEntity

  case class GeonamesCity(id: Long, name: String, location: Location, population: Int) extends GeonamesEntity
}
