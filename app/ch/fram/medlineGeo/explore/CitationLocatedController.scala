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

  def countByHexagon(radius: Double, year: Option[Int]) =
    Action { req =>
      val ret = cached.getOrElse(req.uri)(CitationLocatedService.countByHexagon(radius, year.getOrElse(2014)).toJSON.collect.mkString(",\n"))
      Ok("[\n" + ret + "\n]").as("application/json")
    }

}
