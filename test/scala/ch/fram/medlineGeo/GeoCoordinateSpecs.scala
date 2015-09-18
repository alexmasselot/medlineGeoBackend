package scala.ch.fram.medlineGeo

import ch.fram.medlineGeo.models.GeoCoordinates
import org.specs2.mutable.Specification

/**
 * Created by alex on 18/09/15.
 */
class GeoCoordinateSpecs extends Specification {

  def check(lat1: Double, long1: Double, lat2: Double, long2: Double, expectedDistance: Double) = {
    s"|($lat1, $long1), ($lat2, $long2)| ~ $expectedDistance" in {
      GeoCoordinates(lat1, long1).distance(GeoCoordinates(lat2, long2)) must beCloseTo(expectedDistance, 100)
    }
  }

  check(0, 0, 0, 0, 0)
  check(180, 0, -180, 0, 0)
  check(38.898556, -77.037852, 38.897147, -77.043934, 549)
  check(38.898556, -77.037852, 38.897147, -35.043934, 3601072)
  check(-38.898556, -77.037852, 38.897147, -77.043934, 8650487)
  check(38.897147, -77.043934, -38.898556, -77.037852, 8650487)

}
