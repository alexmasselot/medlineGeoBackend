package ch.fram.medlineGeo.explore.geo

import ch.fram.medlineGeo.models.{ GeoCoordinates, Location }
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

/**
 * Created by alex on 02/10/15.
 */
class HexagonTilingSpecs extends Specification {
  /**
   * check for the projection.
   * warning: get a (x,y) input when GeoCoordinates are (lat, lng)  - coords are swapped
   * moreover, multiply the x by sqrt(3)/2
   */
  val alpha = Math.sqrt(3.0) / 2
  def check(radius: Double, l: (Double, Double), lExp: (Double, Double)) = {

    val mappedData = for {
      lats <- List(1, -1)
      lngs <- List(1, -1)
    } yield {
      val lt = GeoCoordinates(lats * l._2, lngs * l._1 * alpha)
      val ltExp = GeoCoordinates(lats * lExp._2, lngs * lExp._1 * alpha)
      (lt, ltExp)
    }

    s"Hexagone.tiling $l%$radius -> $lExp" should {
      Fragment.foreach(mappedData) { p =>
        s"${p._1}%$radius -> ${p._2}" in {
          val ml = HexagonTiling.project(radius, p._1)
          ml.lat must beCloseTo(p._2.lat, 0.0001)
          ml.lng must beCloseTo(p._2.lng, 0.0001)
        }
      }
    }
  }

  check(1, (0, 0), (0, 0))
  check(1, (0.999, 0), (0, 0))
  check(1, (0, 0.999), (0, 0))
  check(1, (0.5, 0.74), (0, 0))
  check(1, (0.5, 0.76), (1, 1.5))
  check(1, (0.9999, 0.4999), (0, 0))
  check(1, (1, 0.50001), (1, 1.5))
  check(1, (1, 1.5), (1, 1.5))
  check(1, (1.1, 0), (2.0, 0))
  check(1, (2.0, 0), (2.0, 0))
  check(1, (7.3, 0), (8, 0))
  check(1, (0.999, 0.4), (0, 0))
  check(1, (0.999, 0.6), (1, 1.5))
  check(1, (2, 0.999), (2, 0))
  check(1, (1.9999, 1.01), (1, 1.5))
  check(1, (2.00001, 1.01), (3, 1.5))
  check(1, (2, 0.9), (2, 0))

  check(.1, (0, 0), (0, 0))
  check(10.0, (0, 0), (0, 0))
  check(.1, (0, 0.08), (0, 0))
  check(10.0, (0, 8), (0, 0))
  check(.1, (0.08, 0), (0, 0))
  check(10.0, (8, 0), (0, 0))
  check(.1, (0.21, 0), (0.2, 0))
  check(.1, (0.0001, 0.15), (0.1, 0.15))

  check(0.1, (00.41, 0.26), (0.4, 0.3))

}