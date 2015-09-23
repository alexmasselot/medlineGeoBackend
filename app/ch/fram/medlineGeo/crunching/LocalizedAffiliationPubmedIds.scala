package ch.fram.medlineGeo.crunching

import ch.fram.medlineGeo.models._

/**
 * Created by Alexandre Massselot on 11/09/15.
 */
case class LocalizedAffiliationPubmedIds(affiliationHook: String,
                                         pubmedIds: List[Long],
                                         citationCount: Int,
                                         location: Option[Location],
                                         locResolverSolution: Option[String],
                                         locResolverTried: List[String]) {

  /**
   * builds a new LocalizedAffiliationPubmedIds, setting a given location + resolver name
   * @param newLocation
   * @param resolver
   * @return
   */
  def resolveSuccess(newLocation: Location, resolver: String): LocalizedAffiliationPubmedIds =
    new LocalizedAffiliationPubmedIds(
      affiliationHook,
      pubmedIds,
      citationCount,
      Some(newLocation),
      Some(resolver),
      locResolverTried
    )

  /**
   * builds a new LocalizedAffiliationPubmedIds, just add the resolver name to the set of tried resolvers
   * @param resolver
   * @return
   */
  def resolveFailure(resolver: String): LocalizedAffiliationPubmedIds =
    new LocalizedAffiliationPubmedIds(
      affiliationHook,
      pubmedIds,
      citationCount,
      location,
      None,
      (locResolverTried :+ resolver).distinct
    )
}


object LocalizedAffiliationPubmedIds {
  /**
   * create a default LocalizedAffiliationPubmedIds.
   * @param affiliationHook
   * @param pubmedIds
   * @return
   */
  def create(affiliationHook: String,
             pubmedIds: List[Long]): LocalizedAffiliationPubmedIds =
    LocalizedAffiliationPubmedIds(
      affiliationHook,
      pubmedIds,
      pubmedIds.size,
      None,
      None,
      Nil
    )
}