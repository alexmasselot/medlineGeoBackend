package ch.fram.medlineGeo.explore

import ch.fram.medlineGeo.explore.geo.HexagonTiling
import ch.fram.medlineGeo.models.GeoCoordinates
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{UserDefinedFunction, Row}
import play.api.Logger
import play.api.cache.CacheApi

import scala.collection.mutable.ArrayBuffer

/**
 * Created by alex on 28/09/15.
 */
object CitationLocatedService {
  val config = ConfigFactory.defaultApplication()
  val sparkDataDir = config.getConfig("spark").getString("dataDir")
  val parquetCitationsLocated = s"$sparkDataDir/citations-located.parquet"

  import SparkCommons.sqlContext.implicits._
  import org.apache.spark.sql.functions._

  lazy val df = SparkCommons.sqlContext.read.parquet(parquetCitationsLocated)

  def count: Long = {
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
      val f: (ArrayBuffer[Any]) => List[GeoCoordinates] = { (coords: ArrayBuffer[Any]) =>
        coords.toList.map({
          case Row(lat: Double, lng: Double) => HexagonTiling.project(radius, GeoCoordinates(lat, lng))
        })
      }
      udfHexagonProjectionRegistered.put(udfName, SparkCommons.sqlContext.udf.register(udfName, f))
    }
    udfHexagonProjectionRegistered(udfName)
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
    def project(coords: (Double, Double)): (Double, Double) = {
      val l = HexagonTiling.project(radius, GeoCoordinates(coords._1, coords._2))
      (l.lat, l.lng)
    }

    Logger.info(s"countByHexagon($radius, $fYear)")
    val dfReduced = df.select("pubmedId", "locations.coordinates", "pubDate.year").limit(1000)

    dfReduced.filter(s"year = $fYear")
      .withColumn("hexaCoordsDup", hexagonProjection(radius)(dfReduced("coordinates")))
      .explode[List[GeoCoordinates], GeoCoordinates]("hexaCoordsDup", "hexaCoordinates")({
      case l: List[GeoCoordinates] =>
        l.toList
          .distinct
    })
      .drop("coordinates")
      .drop("hexaCoordsDup")
      .groupBy("year", "hexaCoordinates")
      .count
      .withColumnRenamed("COUNT(pubmedId)", "countPubmedId")
      .show()
  }
}
