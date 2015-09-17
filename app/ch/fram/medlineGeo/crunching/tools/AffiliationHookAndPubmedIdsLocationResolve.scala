package ch.fram.medlineGeo.crunching.tools

import java.io.File
import scala.collection.JavaConversions._

import org.apache.spark.SparkContext
import play.api.Logger

/**
 *
 * gett all the citation and extracti unique affiliation hooks (the part needed for localization) together with  least of pubmed ids
 *
 * @author Alexandre Masselot.
 */
object AffiliationHookAndPubmedIdsLocationResolve extends PreProcessApp {
  val sc = new SparkContext(sparkConf)

  val reFilename = s"""${new File(objectsAffiliationPubmedIds).getName}_([0-9]{3})""".r

  /**
   * process files are like the objectsAffiliationPubmedIds file with _\d{3} suffix
   * this function return the last processed file (or the original on if none exist)
   * @return
   */
  def latestResolvedFilename:String = {
    val dir = new File(objectsAffiliationPubmedIds).getParentFile
    dir.listFiles
      .filter(f => reFilename.findFirstIn(f.getName).isDefined)
      .toList
      .sortBy(_.getName)
      .reverse
      .headOption match {
      case Some(f) => f.getAbsolutePath
      case None => objectsAffiliationPubmedIds
    }
  }

  /**
   * get the next filename, incremental the counter with 3 digit
   * @return
   */
  def nextResolvedFilename:String = {
    latestResolvedFilename match {
      case reFilename(n) => s"${objectsAffiliationPubmedIds}_${n+1}"
      case _ => s"${objectsAffiliationPubmedIds}_000"
    }
  }



  val currentFile = latestResolvedFilename
  val nextFile = nextResolvedFilename

  Logger.info (s"$currentFile -> $nextFile")

  val rdd =  sc.objectFile(currentFile)
  println(rdd.take(5))
  Logger.info("DONE")


}
