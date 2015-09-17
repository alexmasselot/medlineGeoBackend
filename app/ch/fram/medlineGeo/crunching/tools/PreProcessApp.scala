package ch.fram.medlineGeo.crunching.tools

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



}
