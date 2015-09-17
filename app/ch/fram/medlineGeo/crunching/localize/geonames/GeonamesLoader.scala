package ch.fram.medlineGeo.crunching.localize.geonames

import ch.fram.medlineGeo.models.{GeoCoordinates, Location}
import com.typesafe.config.ConfigFactory

import scala.io.Source

/**
 * loads data like GeonamesCity or GeonamesCountry
 *
 * Created by Alexandre Masselot on 14/09/15.
 */
trait GeonamesLoader[T <: GeonamesEntity] {
  /**
   * the end key for where to dfind the original data in the geonames directory
   */
  val srcConfigKey: String

  /**
   * convert a Map[String, String] into one bean. This mean giving actual sense to the column
   * it can return None is the map is not providing enough data
   * @param map
   * @return
   */
  def fromMap(map: Map[String, String]): Option[T]

  /**
   * the tabular column names. If let to None, we assume there are hear in the tsv file
   */
  val columnNames: Option[List[String]] = None

  /**
   * where the file is located (derived from config "thirdParties.geonames.txt."+srcConfigKey)
   * @return
   */
  def srcFile = {
    ConfigFactory.defaultApplication().getString("thirdParties.geonames.txt." + srcConfigKey)
  }

  /**
   * where the laternate names file is located (from config "thirdParties.geonames.txt.alternateNamesFile")
   * @return
   */
  def alternameNamesFile = {
    ConfigFactory.defaultApplication().getString("thirdParties.geonames.txt.alternateNamesFile")
  }

  /**
   * from the default source file (config thirdParties.geonames.txt.countryFile), build a dictionary
   * of GeonameEntity
   * @return
   */
  def load: GeonameDirectory[T] = {

    val itEntities: Iterator[T] = {
      val names = columnNames match {
        case None =>
          val headerLine = Source.fromFile(srcFile)
            .getLines()
            .takeWhile(_.startsWith("#"))
            .toList
            .last
            .substring(1)

          headerLine.split("\t").toList

        case Some(sx) => sx
      }

      Source.fromFile(srcFile)
        .getLines()
        .filterNot(_.startsWith("#"))
        .filter(_.trim() != "")
        .map(line => fromMap(names.zip(line.split("\t").toList).toMap))
        .filter(_.isDefined)
        .map(_.get)
    }

    val itAlternates = Source.fromFile(alternameNamesFile)
      .getLines()
      .filter(_.trim() != "")
      .map(_.split("\t").toList)
      .map(_ match {
      case id :: idRef :: ln :: name :: xs => (id.toLong, idRef.toLong, ln, name)
      case x => throw new IllegalArgumentException(x.toString)
    })
      .filter(p => p._3 == "en" || p._3 == "abbr")
      .map(p => (p._4, p._2))

    new GeonameDirectory[T](itEntities, itAlternates)
  }
}

object GeonamesCountryLoader extends GeonamesLoader[GeonamesCountry] {
  val srcConfigKey = "countryFile"

  override def fromMap(map: Map[String, String]): Option[GeonamesCountry] = {
    if (map("geonameid").trim() == "")
      None
    else
      Some(GeonamesCountry(
        map("geonameid").toLong,
        map("ISO"),
        map("Country")
      ))
  }
}

object GeonamesCityLoader extends GeonamesLoader[GeonamesCity] {
  val srcConfigKey = "cityFile"

  override val columnNames = Some(List(
    "geonameid",
    "name",
    "asciiname",
    "alternatenames",
    "latitude",
    "longitude",
    "feature class",
    "feature code",
    "country code",
    "cc2",
    "admin1 code",
    "admin2 code",
    "admin3 code",
    "admin4 code",
    "population",
    "elevation",
    "dem",
    "timezone",
    "modification date"))


  override def fromMap(map: Map[String, String]): Option[GeonamesCity] = {
    Some(GeonamesCity(
      id = map("geonameid").toLong,
      name = map("name"),
      location = Location(GeoCoordinates(map("latitude").toDouble, map("longitude").toDouble), map("country code")),
      population = map("population").toInt
    ))
  }
}

