package ch.fram.medlineGeo.models


/**
 * Created by alex on 18/09/15.
 */
case class GeoCoordinates(lat: Double, lng: Double) {
  val earthRadius = 6371000

  def distance(other: GeoCoordinates): Double = {
    val dLat = Math.toRadians(other.lat - lat)
    val dLng = Math.toRadians(other.lng - lng)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadius * c
  }
}
