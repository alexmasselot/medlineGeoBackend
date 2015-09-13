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

}


object LocalizedAffiliationPubmedIds {
  def apply(affiliationHook: String,
            pubmedIds: List[Long]):LocalizedAffiliationPubmedIds =
    LocalizedAffiliationPubmedIds(
      affiliationHook,
      pubmedIds,
      pubmedIds.size,
      None,
      None,
      Nil
    )
}