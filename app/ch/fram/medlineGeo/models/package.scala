package ch.fram.medlineGeo

/**
 * Created by Alexandre Masselot on 07/09/15.
 */
package object models {


  case class Author(lastName: String, forename: String, initials: String, affiliations: List[String])

  case class PubDate(year: Int)

  case class Citation(pubmedId: Long, pubDate: PubDate, title: String, abstractText: String, authors: List[Author])

  case class GeoCoordinates(lat: Double, lng: Double)

  case class LocalizedAffiliationPubmedIds(affiliationHook: String, pubmedIds: List[Long], coordinates: Option[GeoCoordinates]=None, locResolverSolution: Option[String]=None, locResolverTried: List[String]=Nil)

}
