package ch.fram.medlineGeo.explore.geo

import ch.fram.medlineGeo.models.{GeoCoordinates, Location}

/**
 * Created by Alexandre Masselot on 02/10/15.
 */
object HexagonTiling {
  val alpha = Math.sqrt(3) / 2

  /**
   * project a location into the hexagon center, where the radius is in degree.
   * For sure the hexagon won't have the same area across the globe, but let's take that as a first approximation
   * @param radius the hexagon radius in degree
   * @param loc origin coordinates
   * @return
   */
  def project(radius: Double, loc: GeoCoordinates): GeoCoordinates = {
    val radiusp = radius * alpha

    val latCenter = 3.0 * Math.round(loc.lat / (3 * radius))
    val lngCenter = 2.0 * Math.round(loc.lng / (2 * radiusp))

    val latp = loc.lat / radius - latCenter
    val lngp = loc.lng / radiusp - lngCenter

    val (latPShift: Double, lngPShift: Double) = if (Math.abs(latp) <= 1 - Math.abs(lngp) / 2) {
      (0.0, 0.0)
    } else {
      if (lngp >= 0)
        if (latp >= 0) (1.5, 1.0) else (-1.5, 1.0)
      else
      if (latp >= 0) (1.5, -1.0) else (-1.5, -1.0)
    }

    GeoCoordinates(
      radius *(latCenter +  latPShift),
      radiusp * (lngCenter + lngPShift)
    )
  }
}
