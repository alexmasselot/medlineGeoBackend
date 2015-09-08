package ch.fram.medlineGeo.crunching.parsers

import java.io.{BufferedInputStream, FileInputStream}
import java.util.zip.GZIPInputStream

import ch.fram.medlineGeo.models.{PubmedId, PubDate, Citation}

import scala.xml.factory.XMLLoader
import scala.xml.{Elem, SAXParser, XML}

/**
 * Created by alexandre masselot on 07/09/15.
 */
object MedlineParser {
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

  def parse(filename: String): Seq[Citation] = {
    val doc = getDoc(filename)
    for {
      node <- (doc \ "PubmedArticle" \ "MedlineCitation") ++ (doc \ "MedlineCitation")
    } yield {
      val pmid = node \ "PMID" text
      val nodeArticle = node \ "Article"
      val abstractText = toSingleLine(nodeArticle \ "Abstract" \ "AbstractText" text)
      val title = toSingleLine(nodeArticle \ "ArticleTitle" text)
      println(pmid)
      println(nodeArticle \ "Journal" \ "JournalIssue" \ "PubDate" \ "Year" text)
      val year =
        List(
          (nodeArticle \ "ArticleDate" \ "JournalIssue" \ "PubDate" \ "Year"),
          (nodeArticle \ "ArticleDate" \ "Year")
        ).find()

      Citation(
        PubmedId(pmid.toLong),
        pubDate = PubDate(year = year.toInt),
        title = title,
        abstractText = abstractText,
        authors = Nil
      )
    }
  }
}
