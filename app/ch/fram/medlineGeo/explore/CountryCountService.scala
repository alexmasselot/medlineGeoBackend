package ch.fram.medlineGeo.explore

import ch.fram.medlineGeo.explore.geo.HexagonTiling
import ch.fram.medlineGeo.models.GeoCoordinates
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, Row, UserDefinedFunction}
import play.api.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by alex on 28/09/15.
 */
object CountryCountService extends SparkService {

  import SparkCommons.sqlContext.implicits._
  import org.apache.spark.sql.functions._

  lazy val df = SparkCommons.sqlContext.read.parquet(parquetCitationsLocated)

  def size: Long = {
    df.count()
  }


  lazy val dfCachedCountPerCountry: DataFrame = {
    df.select("pubmedId", "locations.countryIso", "pubDate.year")
      .explode[mutable.WrappedArray[String], String]("countryIso", "countryIso2")({
      case l: mutable.WrappedArray[String] => l.array.toList.distinct
    }).drop("countryIso")
      .withColumnRenamed("countryIso2", "countryIso")
      .groupBy("year", "countryIso")
      .agg(count("pubmedId"))
      .withColumnRenamed("count(pubmedId)", "countPubmedId")
      .cache
  }


  /**
   *
   * count by country, for a given year
   * @param fYear
   * @return
   */
  def countByYear(fYear: Int) = {
    dfCachedCountPerCountry.filter(s"year = $fYear")
  }
}
