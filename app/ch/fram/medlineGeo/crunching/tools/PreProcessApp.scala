package ch.fram.medlineGeo.crunching.tools

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

  val conf = new SparkConf(false)
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("just a worksheet") // name in Spark web UI
    .set("spark.logConf", "true")



}
