package ch.fram.medlineGeo.crunching.parsers

import java.io.{BufferedInputStream, FileInputStream}
import java.util.zip.GZIPInputStream

import ch.fram.medlineGeo.models._

import scala.xml.factory.XMLLoader
import scala.xml.{Node, Elem, SAXParser, XML}

/**
 * Created by alexandre masselot on 07/09/15.
 */
object MedlineParser {

  case class MedlineXMLParsingException(message: String) extends Exception(message)


  def toSingleLine(text: String) = {
    text.replaceAll( """[ \t]*\n[ \t]*""", " ").trim()
  }

  /**
   * get inputstream out of .gz or not file
   * @param filename
   * @return
   */
  def getInputStream(filename: String) = {
    if (filename.endsWith(".gz")) {
      new GZIPInputStream(new BufferedInputStream(new FileInputStream(filename)))
    } else {
      new FileInputStream(filename)
    }
  }

  /**
   * build an XML loader dtd independant (work when out of network)
   */
  class DTDLessXMLLoader extends XMLLoader[Elem] {
    override def parser: SAXParser = {
      val f = javax.xml.parsers.SAXParserFactory.newInstance()
      //f.setNamespaceAware(false)
      //f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      f.setValidating(false);
      f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      f.newSAXParser()
    }
  }

  /**
   * load the root element
   * @param filename
   * @return
   */
  def getDoc(filename: String) = {
    xml.XML.withSAXParser(new DTDLessXMLLoader().parser).load(getInputStream(filename))
  }

  def parseAuthor(node: Node): Author =
    Author(lastName = node \ "LastName" text,
      forename = node \ "ForeName" text,
      initials = node \ "Initials" text,
      affiliations = (node \ "AffiliationInfo" \\ "Affiliation" flatMap (x => (x text).split("; ").toList) ).toList
    )

  def parse(filename: String): Seq[Citation] = {
    val doc = getDoc(filename)
    for {
      node <- (doc \ "PubmedArticle" \ "MedlineCitation") ++ (doc \ "MedlineCitation")
    } yield {
      val pmid = node \ "PMID" text
      val nodeArticle = node \ "Article"
      val abstractText = toSingleLine((nodeArticle \ "Abstract" \\ "AbstractText" map(_.text)).mkString(" "))
      val title = toSingleLine(nodeArticle \ "ArticleTitle" text)

      val reMedlineDate = """.*\b(\d\d\d\d).*""".r
      val year =
        List(
          (nodeArticle \ "Journal" \ "JournalIssue" \ "PubDate" \ "Year" text),
          (nodeArticle \ "Journal" \ "JournalIssue" \ "PubDate" \ "MedlineDate" text),
          (nodeArticle \ "ArticleDate" \ "Year" text)
        ).find(_.trim != "") match {
          case Some(reMedlineDate(x)) => x.toInt
          case Some(x) => x.toInt
          case None => throw MedlineXMLParsingException(s"no year for pmid=${pmid}")
        }

      val authors = nodeArticle \ "AuthorList" \\ "Author"  map (parseAuthor)

      Citation(
        pmid.toLong,
        pubDate = PubDate(year = year),
        title = title,
        abstractText = abstractText,
        authors = authors.toList
      )
    }
  }
}
