package ch.fram.medlineGeo.explore

import ch.fram.medlineGeo.explore.geo.HexagonTiling
import ch.fram.medlineGeo.models.GeoCoordinates
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, UserDefinedFunction, Row}
import play.api.Logger
import play.api.cache.CacheApi

import scala.collection.mutable

/**
 * Created by alex on 28/09/15.
 */
object CitationLocatedService extends SparkService{

  import SparkCommons.sqlContext.implicits._
  import org.apache.spark.sql.functions._

  lazy val df = SparkCommons.sqlContext.read.parquet(parquetCitationsLocated)

  def size: Long = {
    df.count()
  }

  val udfHexagonProjectionRegistered = scala.collection.mutable.Map[String, UserDefinedFunction]()

  /**
   * register only once the user defined function for hexagonal projection
   * @param radius
   * @return
   */
  def hexagonProjection(radius: Double): UserDefinedFunction = {
    val udfName = s"hexagonProjection_$radius".replaceAll(".", "_")

    if (!udfHexagonProjectionRegistered.contains(udfName)) {
      val f: (mutable.WrappedArray[Any]) => List[GeoCoordinates] = { (coords: mutable.WrappedArray[Any]) =>
        coords.array.toList.map({
          case Row(lat: Double, lng: Double) => HexagonTiling.project(radius, GeoCoordinates(lat, lng))
        })
      }
      udfHexagonProjectionRegistered.put(udfName, SparkCommons.sqlContext.udf.register(udfName, f))
    }
    udfHexagonProjectionRegistered(udfName)
  }


  //we cache the hexagon aggregated dataframe (which is pretty small), as we would need to filter it by year subsequentyl)
  var cachedCountByHexagon = scala.collection.mutable.Map[Double, DataFrame]()

  def dfCountByHexagon(radius: Double): DataFrame = {
    if (!cachedCountByHexagon.contains(radius)) {
      def project(coords: (Double, Double)): (Double, Double) = {
        val l = HexagonTiling.project(radius, GeoCoordinates(coords._1, coords._2))
        (l.lat, l.lng)
      }

      Logger.info(s"countByHexagon($radius)")
      val dfReduced = df.select("pubmedId", "locations.coordinates", "pubDate.year")

      cachedCountByHexagon.put(radius,
        dfReduced
      .withColumn("hexaCoordsDup", hexagonProjection(radius)(dfReduced("coordinates")))
      .explode[mutable.WrappedArray[GeoCoordinates], GeoCoordinates]("hexaCoordsDup", "hexaCoordinates")({
      case l: mutable.WrappedArray[GeoCoordinates] =>
        l.array.toList
          .distinct
    })
          .drop("coordinates")
          .drop("hexaCoordsDup")
          .groupBy("year", "hexaCoordinates")
          .agg($"year", $"hexaCoordinates", count($"pubmedId"))
          .withColumnRenamed("count(pubmedId)", "countPubmedId")
          .cache
      )
    }
    cachedCountByHexagon(radius)
  }

  /**
   *
   * project publication hexagon (with the given radius) and count them
   * If one ublication lands in multiple hexagons (distant enough affiliations), then each of them receive a token.
   * It multiple affiliation lands in the same hexagon, they are only counted once)
   * @param radius
   * @param fYear
   * @return
   */
  def countByHexagon(radius: Double, fYear: Int) = {

    dfCountByHexagon(radius).filter(s"year = $fYear")

  }
}
