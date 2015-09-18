package ch.fram.medlineGeo.crunching

/**
 * Created by alex on 18/09/15.
 */
package object localize {

  /**
   * this execption is rather fatal.
   * It means that we encountered a situation where, for example, a city/country combination can give ambivalent places
   * @param message
   */
  case class LocationResolutionConflictException(message: String) extends Exception(message)

  /**
   * For some reason, the resolver skips the reoslution.
   * For example google map resolution have reached the daily limit
   * @param message
   */
  case class LocationResolutionSkipException(message: String) extends Exception(message)

  /**
   * simply that no location was found. Well, that happens rather often
   */
  object LocationResolutionNotfoundException extends Exception()

}
