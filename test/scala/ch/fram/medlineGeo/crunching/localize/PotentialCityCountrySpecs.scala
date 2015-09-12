package ch.fram.medlineGeo.crunching.localize

import org.specs2.mutable.Specification

/**
 * Created by Alexandre Masselot on 11/09/15.
 */
class PotentialCityCountrySpecs extends Specification {

  val data: List[(String, List[(String, String)])] = List(
    ("Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia",
      List(("Melbourne", "Australia"), ("University of Melbourne", "Australia"), ("University of Melbourne", "Melbourne"))),
    ("Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India",
      List(("Coimbatore", "India"), ("Peelamedu", "India"), ("Peelamedu", "Coimbatore"))),
    ("Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan",
      List(("Tokyo", "Japan"), ("Itabashi-ku", "Japan"), ("Itabashi-ku", "Tokyo"))),
    ("Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy",
      List(("Roma", "Italy"), ("Via Gramsci", "Italy"), ("Via Gramsci", "Roma"))),
    ("CH-1211 Geneva, Switzerland",
      List(("Geneva", "Switzerland"))),
    ("Department of Neonatology, Manipal Hospital, Bangalore- 560017, India",
      List(("Bangalore", "India"), ("Manipal Hospital", "India"), ("Manipal Hospital", "Bangalore"))),
    ("Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India",
      List(("Haryana", "India"), ("Rohtak", "India"), ("Rohtak", "Haryana"))),
    ("University of Washington, Seattle 98195", List(("University of Washington", "Seattle")))
  //(, List(("", ""), ("", ""), ("", ""))),
  //(, List(("", ""), ("", ""), ("", ""))),
  )

  for {
    (aff, expected) <- data
  } yield {
    "PotentialCityCountry.apply" should {
      aff in {
        PotentialCityCountry(aff) must beEqualTo(expected.map(p => PotentialCityCountry(p._1, p._2)))
      }
    }
  }
}
