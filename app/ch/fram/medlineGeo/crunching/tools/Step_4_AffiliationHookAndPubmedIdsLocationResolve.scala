package ch.fram.medlineGeo.crunching.tools

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.crunching.localize.{GeonamesLocationResolver, LocationResolver}
import org.apache.spark.SparkContext
import play.api.Logger

import scala.util.{Failure, Try}

/**
 *
 * Step 4 : open the last serilized object files, apply a resolver and save into the next
 *
 * @author Alexandre Masselot.
 */
object Step_4_AffiliationHookAndPubmedIdsLocationResolve extends PreProcessApp {
  val sc = new SparkContext(sparkConf)


  class SerializeReader(filename: String) extends Iterator[LocalizedAffiliationPubmedIds] {
    val ois = new ObjectInputStream(new FileInputStream(filename))

    var nextOne: Try[LocalizedAffiliationPubmedIds] = Failure(new UnsupportedOperationException())
    tryNext

    override def hasNext: Boolean = nextOne.isSuccess

    override def next(): LocalizedAffiliationPubmedIds = {
      val n = nextOne.get
      tryNext
      n
    }

    def tryNext: Unit = {
      nextOne = Try {
        ois.readObject().asInstanceOf[LocalizedAffiliationPubmedIds]
      }
    }
  }

  class SerializeWriter(filename: String) {
    val oos = new ObjectOutputStream(new FileOutputStream(filename))

    def append(lapi: LocalizedAffiliationPubmedIds) = oos.writeObject(lapi)

    def close = oos.close()
  }

  val currentFile = latestResolvedAffiliationPubmedIdsObjectsFilename.get
  val nextFile = nextResolvedAffiliationPubmedIdsObjectsFilename

  Logger.info(s"$currentFile -> $nextFile")

  val resolverName: String = "geonames"
  val resolver: LocationResolver = GeonamesLocationResolver
  val resolverMaxCounter: Int = Int.MaxValue


  val reader = new SerializeReader(currentFile)
  for {x <- reader} {
    println(x)
  }

}
