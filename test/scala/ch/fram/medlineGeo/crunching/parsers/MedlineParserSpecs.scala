package ch.fram.medlineGeo.crunching.parsers

import ch.fram.medlineGeo.models._
import org.specs2.mutable.Specification

/**
 * Created by alex on 07/09/15.
 */
class MedlineParserSpecs extends Specification {
  val txtIn = List(
    ("\"don't change a simple line" -> """paf le chien"""),
    ("multi line" -> """paf
          le
          chien"""),
    ("multi line without trailng space" -> """paf
          le
          chien"""),
    ("end with enter" -> "paf le chien\n"),
    ("space head/tail" -> """   paf le chien  """)
  )
  for {
    (title, in) <- txtIn
  } yield {
    "MedlineParser.toSingleLine" should {
      title in {
        MedlineParser.toSingleLine(in) must beEqualTo("paf le chien")
      }
    }
  }

  "pubmed parsing" should {
    val fname = "test/resources/pubmed_result_fred.xml"
    "count them right" in {
      MedlineParser.parse(fname).size must beEqualTo(176)
    }
    "check one" should {
      def cit = MedlineParser.parse(fname)(2)
      "PubmedId" in {
        cit.pubmeId must beEqualTo(PubmedId(25785427))
      }
      s"${cit.pubmeId} date" in {
        cit.pubDate must beEqualTo(PubDate(year = 2015))
      }
      s"${cit.pubmeId} title" in {
        cit.title must beEqualTo("Regulation of the oncoprotein Smoothened by small molecules.")
      }
      s"${cit.pubmeId} abstractTet" in {
        cit.abstractText must startWith("The Hedgehog pathway is critical")
      }
    }
    "22487467 year from ArticleDate" in {
      val oCit = MedlineParser.parse(fname).find(_.pubmeId == PubmedId(22487467))
      oCit must beSome
      val cit = oCit.get
      cit.pubDate must beEqualTo(PubDate(year=2012))
    }

    "11766691 year parsed from MedlineDate" in {
      val oCit = MedlineParser.parse(fname).find(_.pubmeId == PubmedId(11766691))
      oCit must beSome
      val cit = oCit.get
      cit.pubDate must beEqualTo(PubDate(year=2000))
    }
  }

  "medline parsing" should {
    "count them right" in {
      val fname = "test/resources/medline_few.xml"
      MedlineParser.parse(fname).size must beEqualTo(9)
    }
  }
}
