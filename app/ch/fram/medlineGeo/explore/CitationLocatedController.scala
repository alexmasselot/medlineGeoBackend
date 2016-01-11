package ch.fram.medlineGeo.explore

import javax.inject.Inject

import ch.fram.medlineGeo.explore.utils.DiskCache
import play.api.Play
import play.api.cache.Cached
import play.api.mvc.{Action, Controller}

/**
  * Created by alex on 28/09/15.
  */
class CitationLocatedController @Inject()(cached: DiskCache) extends Controller {

  def countByHexagon(radius: Double, year: Int) =
    Action { req =>
      val ret = cached.getOrElse(req.uri)("[\n"+ CitationLocatedService.countByHexagon(radius, year).toJSON.collect.mkString(",\n")+ "\n]")
      Ok( ret).as("application/json")
    }

}
