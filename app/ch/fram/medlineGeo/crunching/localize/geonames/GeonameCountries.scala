package ch.fram.medlineGeo.crunching.localize.geonames

import java.io.File

import com.typesafe.config.ConfigFactory
import com.github.tototoshi.csv._


/**
 * Created by Alexandre Masselot on 13/09/15.
 */
class GeonameCountries(list: Iterator[GeonameCountry], itAlt: Iterator[(String, Long)]) {
  /**
   * dictionary id -> GeonameCountry
   */
  val idRef = list.map(gc => (gc.id, gc)).toMap
  /**
   * dictionary name -> List of GeonameCountry
   */
  val nameRef: Map[String, List[GeonameCountry]] = idRef.values.toList.groupBy(_.name)
  /**
   * dictionary name -> List of GeonameCountry, based on the passed alternate list name/id
   */
  val alternateNameRef: Map[String, List[GeonameCountry]] =
    itAlt.filter({ case (name, id) => idRef.get(id).isDefined })
      .toList
      .groupBy(_._1)
      .map({ case (name, xs) => (name, xs.map(x => idRef(x._2))) })

  def size = idRef.size

  def findById(geonameId: Long): Option[GeonameCountry] = idRef.get(geonameId)

  def findByName(countryName: String): List[GeonameCountry] = nameRef.get(countryName) match {
    case Some(xs) => xs
    case None => Nil
  }

  def findByAlternateName(countryName: String): List[GeonameCountry] = alternateNameRef.get(countryName) match {
    case Some(xs) => xs
    case None => Nil
  }
}

object GeonameCountries {
  def srcFile = {
    ConfigFactory.defaultApplication().getString("thirdParties.geonames.txt.countryFile")
  }

  def alternameNamesFile = {
    ConfigFactory.defaultApplication().getString("thirdParties.geonames.txt.alternateNamesFile")
  }

  /**
   * from the default source file (config thirdParties.geonames.txt.countryFile), build a dictionary
   * of GeonameCountries
   * @return
   */
  def load: GeonameCountries = {
    implicit object MyFormat extends DefaultCSVFormat {
      override val delimiter = '\t'
    }
    val reader = CSVReader.open(new File(srcFile))

    val itcountries = reader.iteratorWithHeaders.map(x => GeonameCountry(
      x("geonameid").toLong,
      x("#ISO"),
      x("Country")
    ))

    val itAlternates = CSVReader.open(new File(alternameNamesFile))
      .iterator
      .map(_ match {
      case id :: idRef :: ln :: name :: xs => (id.toLong, idRef.toLong, ln, name)
      case x => throw new IllegalArgumentException(x.toString)
    })
      .filter(p => p._3 == "en" || p._3 == "abbr")
      .map(p => (p._4, p._2))

    new GeonameCountries(itcountries, itAlternates)
  }
}
