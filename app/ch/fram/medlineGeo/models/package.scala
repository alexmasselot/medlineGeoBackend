package ch.fram.medlineGeo

/**
 * Created by Alexandre Masselot on 07/09/15.
 */
package object models {

  case class PubmedId(value: Long) extends AnyVal

  case class Affiliation(value: String) extends AnyVal

  case class Author(lastName: String, forename: String, initials: String, affiliations: List[Affiliation])

  case class PubDate(year: Int)

  case class Citation(pubmeId: PubmedId, pubDate: PubDate, title: String, abstractText: String, authors: List[Author])

}
