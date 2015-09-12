package ch.fram.medlineGeo.crunching.localize

/**
 * Created by Alexandre Masselot on 11/09/15.
 */
case class PotentialCityCountry(city: String, country: String) {

}

object PotentialCityCountry {
  val reSplitBlocks = """\s*,\s*""".r
  val reNumberOnly = """.""".r
  val reHeadPostalCode = """[\-\s\d]+(\-[A-Z]{1,2})?$""".r
  val reTailPostalCode = """^([A-Z]{1,2})?[\-\s\d]+""".r

  /**
   * out of an affiliationHook, build 3 pair of potential city/country, removing postal code and so
   * @param affiliationHook
   * @return
   */
  def apply(affiliationHook: String): List[PotentialCityCountry] = {
    reSplitBlocks.split(affiliationHook)
      .toList
      .reverse
      .map(s => {
        reHeadPostalCode.replaceFirstIn(reTailPostalCode.replaceFirstIn(s, ""), "").trim
      })
      .filter(_ != "")
      .take(3)
      .combinations(2)
      .toList
      .map({
      l => PotentialCityCountry(l(1), l.head)
    })

  }
}
