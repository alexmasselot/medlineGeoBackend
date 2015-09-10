package ch.fram.medlineGeo.crunching


/**
 * Created by Alexandre Masselot on 08/09/15.
 * from an medline affiliation string and extract the relevant info in order to locate it.
 * * take the first one in case of multiple affiliations
 * * remove header tag serving as reference handler (typically a number)
 * * remove email
 * * remove trailing dot
 */
object AffiliationHook {

  def extractFirstAffiliation(s: String): String = s.split("; ")(0)

  def removeHeadRef(s: String): String = """^\d+""".r.replaceFirstIn(s, "")

  val reRemoveEmail = """(.*)\.[^\.]*\s*[\w]+(?:\.\w+)*@(?:\w+\.)+\w+""".r

  def removeEmail(s: String): String = s match {
    case reRemoveEmail(x) => x
    case x => x
  }

  def removeTrailDot(s: String): String = """\s*\.\s*$""".r.replaceFirstIn(s, "").trim

  def composeAll =
    extractFirstAffiliation _ andThen
      removeHeadRef _ andThen
      removeTrailDot _ andThen
      removeEmail _

  /**
   * extract the affiliation hook, by applying all th previous rules
   * @param affiliation
   * @return
   */
  def apply(affiliation: String): String = composeAll(affiliation)
}
