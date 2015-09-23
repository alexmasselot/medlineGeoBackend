package ch.fram.medlineGeo.crunching.tools

import java.io.File

import ch.fram.medlineGeo.crunching.SparkUtils
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.{FilenameUtils, FileUtils}

import scala.util.matching.Regex

/**
 * Created by alex on 11/09/15.
 */
trait PreProcessApp extends App {

  import org.apache.spark.{SparkConf, SparkContext}

  val config = ConfigFactory.defaultApplication()
  val privateConfig = ConfigFactory.parseFile(new File("conf/private.conf"))

  val sparkDataDir = config.getConfig("spark").getString("dataDir")

  val parquetCitations = s"$sparkDataDir/citations.parquet"
  val objectsAffiliationPubmedIdsInit = s"$sparkDataDir/affiliation-pubmedids.objects"
  val parquetAffiliationPubmedIdsInit = s"$sparkDataDir/affiliation-pubmedids.parquet"
  val jsonAffiliationPubmedIdsLocated = s"$sparkDataDir/affiliation-pubmedids-located.json"
  val parquetAffiliationPubmedIdsLocated = s"$sparkDataDir/affiliation-pubmedids-located.parquet"
  val parquetCitationsLocated = s"$sparkDataDir/citations-located.parquet"


  val sparkConf = SparkUtils.defaultConf

  val objectCptBaseName = s"""${FilenameUtils.getName(objectsAffiliationPubmedIdsInit)}_"""

  /**
   * process files are like the objectsAffiliationPubmedIds file with _\d{3} suffix
   * this function return the last processed file (or none)
   * @return
   */
  def latestResolvedAffiliationPubmedIdsObjectsFilename: Option[String] = {
    val dir = new File(objectsAffiliationPubmedIdsInit).getParentFile
    dir.listFiles
      .filter(f => f.getName.startsWith(objectCptBaseName))
      .toList
      .sortBy(_.getName)
      .reverse
      .headOption
      .map(_.getAbsolutePath)
  }

  /**
   * get the next filename, incremental the counter with 3 digit
   * @return
   */
  def nextResolvedAffiliationPubmedIdsObjectsFilename: String = {
    latestResolvedAffiliationPubmedIdsObjectsFilename match {
      case None => objectsAffiliationPubmedIdsInit + "_000"
      case Some(bn) =>
        val i = FilenameUtils.getName(bn).replace(objectCptBaseName, "").toInt + 1
        objectsAffiliationPubmedIdsInit + "_" + f"$i%03d"
    }
  }

}
