package ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.models.Location

import scala.util.Try

/**
 * Created by Alexandre Masselot on 15/09/15.
 */
object GeonamesLocationResolver extends LocationResolver{
  override def resolve(affiliationHook: String): Try[Location] = ???
}
