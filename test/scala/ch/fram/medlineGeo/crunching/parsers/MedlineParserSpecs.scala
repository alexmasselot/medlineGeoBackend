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
        cit.pubmedId must beEqualTo(25785427L)
      }
      s"${cit.pubmedId} date" in {
        cit.pubDate must beEqualTo(PubDate(year = 2015))
      }
      s"${cit.pubmedId} title" in {
        cit.title must beEqualTo("Regulation of the oncoprotein Smoothened by small molecules.")
      }
      s"${cit.pubmedId} abstractText" in {
        cit.abstractText must startWith("The Hedgehog pathway is critical")
      }
      s"${cit.pubmedId} authors size" in {
        cit.authors.size must beEqualTo(4)
      }
      s"${cit.pubmedId} one author" in {
        cit.authors.last must beEqualTo(Author(
          "de Sauvage",
          "Frederic J",
          "FJ",
          List("Department of Molecular Oncology, Genentech Inc., San Francisco, California, USA.")
        ))
      }
    }
    "22487467 year from ArticleDate" in {
      val oCit = MedlineParser.parse(fname).find(_.pubmedId == 22487467L)
      oCit must beSome
      val cit = oCit.get
      cit.pubDate must beEqualTo(PubDate(year = 2012))
    }

    "11766691 year parsed from MedlineDate" in {
      val oCit = MedlineParser.parse(fname).find(_.pubmedId == 11766691L)
      oCit must beSome
      val cit = oCit.get
      cit.pubDate must beEqualTo(PubDate(year = 2000))
    }
  }

  "medline parsing" should {
    "count them right" in {
      val fname = "test/resources/medline_few.xml"
      MedlineParser.parse(fname).size must beEqualTo(9)
    }
  }

  "pubmed_tricky parsing" should {
    val fname = "test/resources/pubmed_tricky.xml"
    "count them right" in {
      MedlineParser.parse(fname).size must beEqualTo(1)
    }
    "check one" should {
      def cit(pmid: Long) = MedlineParser.parse(fname).find(_.pubmedId == pmid).get
      "PubmedId" in {
        cit(24101054L).pubmedId must beEqualTo(24101054L)
      }
      "multi element abstract" in {
        cit(24101054L).abstractText must contain("""lung cancer (NSCLC). One thousand one hundred""")
      }

      "multiple affiliation on the first author" in {
        val affiliations = cit(24101054L).authors.head.affiliations
        affiliations must haveLength(14)
        affiliations(2) must beEqualTo("Louis Fehrenbacher, Kaiser Permanente Northern California, Vallejo")
      }
    }
  }
}
