package ch.fram.medlineGeo.crunching.tools

import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.models._
import play.api.libs.json._

/**
 * Created by alex on 19/09/15.
 */
object JsonSerializer {
  implicit val formatAuthor = Json.format[Author]
  implicit val formatPubDate = Json.format[PubDate]
  implicit val formatCitation = Json.format[Citation]
  implicit val formatGeoCoordinates = Json.format[GeoCoordinates]
  implicit val formatLocation = Json.format[Location]
  implicit val formatLocalizedAffiliationPubmedIds = Json.format[LocalizedAffiliationPubmedIds]

}
