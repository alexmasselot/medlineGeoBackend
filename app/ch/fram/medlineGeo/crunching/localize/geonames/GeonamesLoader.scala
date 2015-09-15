package ch.fram.medlineGeo.crunching.localize.geonames

import java.io.File

import ch.fram.medlineGeo.crunching.localize.geonames.GeonameCountryLoader._
import ch.fram.medlineGeo.models.{GeoCoordinates, Location}
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
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
   * @param map
   * @return
   */
  def fromMap(map: Map[String, String]): T

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
    implicit object MyFormat extends DefaultCSVFormat {
      override val delimiter = '\t'
    }

    val itEntities: Iterator[T] = columnNames match {
      case None =>
        val reader = CSVReader.open(new File(srcFile))
        reader.iteratorWithHeaders.map(fromMap)
      case Some(names) =>
        Source.fromFile(srcFile)
          .getLines()
          .filter(_.trim() != "")
          .map(line => fromMap(names.zip(line.split("\t").toList).toMap))
    }

    val itAlternates = CSVReader.open(new File(alternameNamesFile))
      .iterator
      .map(_ match {
      case id :: idRef :: ln :: name :: xs => (id.toLong, idRef.toLong, ln, name)
      case x => throw new IllegalArgumentException(x.toString)
    })
      .filter(p => p._3 == "en" || p._3 == "abbr")
      .map(p => (p._4, p._2))

    new GeonameDirectory[T](itEntities, itAlternates)
  }
}


object GeonameCountryLoader extends GeonamesLoader[GeonamesCountry] {
  val srcConfigKey = "countryFile"

  override def fromMap(map: Map[String, String]): GeonamesCountry = {
    GeonamesCountry(
      map("geonameid").toLong,
      map("#ISO"),
      map("Country")
    )
  }
}

object GeonameCityLoader extends GeonamesLoader[GeonamesCity] {
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


  override def fromMap(map: Map[String, String]): GeonamesCity = {
    GeonamesCity(
      id = map("geonameid").toLong,
      name = map("name"),
      location = Location(GeoCoordinates(map("latitude").toDouble, map("longitude").toDouble), map("country code")),
      population = map("population").toInt
    )
  }
}

