package ch.fram.medlineGeo.crunching.localize.geonames

import java.io.File

import ch.fram.medlineGeo.models.{GeoCoordinates, Location}
import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils


/**
 * Created by Alexandre Masselot on 13/09/15.
 */
class GeonameDirectory[T <: GeonamesEntity](list: Iterator[T], itAlt: Iterator[(String, Long)]) {
  /**
   * project the string removing accent and case
   * @param name
   * @return
   */
  def normalizeName(name:String):String = StringUtils.stripAccents(name).toLowerCase

  /**
   * dictionary id -> GeonameCountry
   */
  val idRef = list.map(gc => (gc.id, gc)).toMap
  /**
   * dictionary name -> List of GeonameCountry
   */
  val nameRef: Map[String, List[T]] = idRef.values.toList.groupBy(e => normalizeName(e.name))
  /**
   * dictionary name -> List of GeonameCountry, based on the passed alternate list name/id
   */
  val alternateNameRef: Map[String, List[T]] =
    itAlt.filter({ case (name, id) => idRef.get(id).isDefined })
      .toList
      .groupBy(p => normalizeName(p._1))
      .map({ case (name, xs) => (name, xs.map(x => idRef(x._2))) })

  def size = idRef.size

  def findById(geonameId: Long): Option[T] = idRef.get(geonameId)

  def findByName(name: String): List[T] = nameRef.get(normalizeName(name)) match {
    case Some(xs) => xs
    case None => Nil
  }

  def findByAlternateName(name: String): List[T] = alternateNameRef.get(normalizeName(name)) match {
    case Some(xs) => xs
    case None => Nil
  }
}
