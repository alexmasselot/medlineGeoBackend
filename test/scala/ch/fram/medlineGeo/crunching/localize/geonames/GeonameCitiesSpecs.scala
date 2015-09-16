package scala.ch.fram.medlineGeo.crunching.localize.geonames

import ch.fram.medlineGeo.crunching.localize.geonames.{ GeonamesCity, GeonamesCityLoader }
import ch.fram.medlineGeo.models.{ GeoCoordinates, Location }
import org.specs2.mutable.Specification

/**
 * Created by alex on 13/09/15.
 */
class GeonameCitiesSpecs extends Specification {
  "GeonameCityLoader" should {
    def cx = GeonamesCityLoader.load

    "get config" in {
      GeonamesCityLoader.srcFile must beEqualTo("test/resources/cities-samples.txt")
    }

    "count" in {
      cx.size must beEqualTo(125)
    }

    "findById, present" in {
      val ogc = cx.findById(266826)
      ogc must beSome

      ogc.get must beEqualTo(GeonamesCity(266826, "Tripoli", Location(GeoCoordinates(34.43667, 35.84972), "LB"), 229398))
    }

    "findById, none" in {
      val ogc = cx.findById(42)
      ogc must beNone
    }

    "findByName, present" in {
      cx.findByName("Tripoli").map(_.location.countryIso).sorted must beEqualTo(List("GR", "LB", "LY"))
    }

    "findByName, miss case, present" in {
      cx.findByName("tripoli").map(_.location.countryIso).sorted must beEqualTo(List("GR", "LB", "LY"))
    }
    "findByName, forget accent, present" in {
      cx.findByName("trípoli").map(_.location.countryIso).sorted must beEqualTo(List("GR", "LB", "LY"))
    }
    "findByName, not present" in {
      cx.findByName("Atlantis") must beEqualTo(Nil)
    }

    "findByAlternateName" in {
      cx.findByAlternateName("New York").map(_.name) must beEqualTo(List("New York City"))
    }

  }
}
