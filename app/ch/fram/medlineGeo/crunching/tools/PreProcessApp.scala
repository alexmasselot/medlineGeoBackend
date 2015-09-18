package ch.fram.medlineGeo.crunching.tools

import java.io.File

import ch.fram.medlineGeo.crunching.SparkUtils
import com.typesafe.config.ConfigFactory

/**
 * Created by alex on 11/09/15.
 */
trait PreProcessApp extends App {
  import org.apache.spark.{SparkConf, SparkContext}

  val config = ConfigFactory.defaultApplication()
  val sparkDataDir = config.getConfig("spark").getString("dataDir")

  val parquetCitations = s"$sparkDataDir/citations.parquet"
  val parquetAffiliationPubmedIds = s"$sparkDataDir/affiliation-pubmedids.parquet"
  val objectsAffiliationPubmedIds = s"$sparkDataDir/affiliation-pubmedids.objects"

  
  
  
  val sparkConf = SparkUtils.defaultConf

  val reFilename = s"""${new File(objectsAffiliationPubmedIds).getName}_([0-9]{3})""".r

  /**
   * process files are like the objectsAffiliationPubmedIds file with _\d{3} suffix
   * this function return the last processed file (or the original on if none exist)
   * @return
   */
  def latestResolvedAffiliationPubmedIdsObjectsFilename: String = {
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
  def nextResolvedAffiliationPubmedIdsObjectsFilename: String = {
    latestResolvedAffiliationPubmedIdsObjectsFilename match {
      case reFilename(n) => s"${objectsAffiliationPubmedIds}_${n + 1}"
      case _ => s"${objectsAffiliationPubmedIds}_000"
    }
  }

}
