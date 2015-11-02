package ch.fram.medlineGeo.explore

import com.typesafe.config.ConfigFactory

/**
 * Created by alex on 02/11/15.
 */
trait SparkService {
  val config = ConfigFactory.defaultApplication()
  val sparkDataDir = config.getConfig("spark").getString("dataDir")
  val parquetCitationsLocated = s"$sparkDataDir/citations-located.parquet"

}
