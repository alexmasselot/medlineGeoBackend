package ch.fram.medlineGeo.crunching

import org.specs2.mutable.Specification

/**
 * Created by alex on 08/09/15.
 */
class AffiliationHookSpecs extends Specification {
  def data = List(
    (
      "just trail dot",
      "John B. Pierce Laboratory of Hygiene, New Haven.",
      "John B. Pierce Laboratory of Hygiene, New Haven"
    ),
    (
      """keep "dept."""",
      "Dept. of Clinical Radiology, University of Muenster, Germany.",
      "Dept. of Clinical Radiology, University of Muenster, Germany"
    ),
    (
      "simple email",
      "St. Georges Healthcare NHS Trust, London, United Kingdom (N.F.). snick@doctors.org.uk.",
      "St. Georges Healthcare NHS Trust, London, United Kingdom (N.F.)"
    ),
    (
      "wtf",
      "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia.",
      "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia"
    ),
    (
      "remove ref",
      "1University of Sussex, UK",
      "University of Sussex, UK"
    ),
    (
      "multiple affiliations",
      "Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China.",
      "Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China"
    ),
    (
      "US state",
      "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA. Electronic address: AKThittai@iitm.ac.in.",
      "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA"
    ),
    ("trim",
      "UPMC, Université Paris 6 , Paris , France ; AP-HP, Hôpital Pitié-Salpêtrière, Centre de référence national pour le Lupus Systémique et le syndrome des Antiphospholipides, Service de Médecine Interne 2, 47-83 Boulevard de l'Hôpital , Paris, Cedex , France.",
      "UPMC, Université Paris 6 , Paris , France"
    ),
    (
      "with email",
      "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States. Electronic address: klpsung@eng.ucsd.edu.",
      "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States"
    )
  )

  for {
    dt <- data
  } yield {
    s"AffiliationHook" should {
      dt._1 in {
        AffiliationHook(dt._2) must beEqualTo(dt._3)
      }
    }
  }

}
