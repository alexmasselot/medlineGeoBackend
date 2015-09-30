package ch.fram.medlineGeo.view

import com.typesafe.config.ConfigFactory
import play.api.cache.CacheApi

/**
 * Created by alex on 28/09/15.
 */
object CitationLocatedService  {
  val config = ConfigFactory.defaultApplication()
  val sparkDataDir = config.getConfig("spark").getString("dataDir")
  val parquetCitationsLocated = s"$sparkDataDir/citations-located.parquet"

  import SparkCommons.sqlContext.implicits._
  import org.apache.spark.sql.functions._

  lazy val rdd = SparkCommons.sqlContext.read.parquet(parquetCitationsLocated)

  def count:Long = {
    rdd.count()
  }
}
