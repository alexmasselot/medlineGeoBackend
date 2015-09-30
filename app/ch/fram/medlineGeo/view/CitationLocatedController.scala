package ch.fram.medlineGeo.view

import play.api.mvc.{Action, Controller}

/**
 * Created by alex on 28/09/15.
 */
object CitationLocatedController extends Controller{
  def index = Action {
    Ok("hello workd")
  }

  def count = Action {
    Ok(CitationLocatedService.count.toString)
  }
}
