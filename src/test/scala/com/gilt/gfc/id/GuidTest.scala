package com.gilt.gfc.id

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.util.UUID
import org.scalacheck.Prop._
import org.scalatestplus.scalacheck.Checkers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GuidTest extends AnyFunSuite with Matchers with Checkers {

  val ValidGuidChars = {
    ('0' to '9') ++
    ('a' to 'f') ++
    ('A' to 'F') :+
    ('-')
  }.toSet

  test("toUUID") {
    check { (msb: Long, lsb: Long) =>
      val uuid = new UUID(msb, lsb)
      val guidFromMsbLsb = Guid[String](msb,lsb)
      val guidFromUUID = Guid[String](uuid)
      (uuid == guidFromMsbLsb.toUUID) &&
      (uuid == guidFromUUID.toUUID) &&
      (guidFromMsbLsb == guidFromUUID) &&
      (guidFromMsbLsb.mostSignificantBits == guidFromUUID.mostSignificantBits) &&
      (guidFromMsbLsb.leastSignificantBits == guidFromUUID.leastSignificantBits)
    }
  }

  test("toString") {
    check { (msb: Long, lsb: Long) =>
      val uuid = new UUID(msb, lsb)
      val guid = Guid[Int](uuid)
      uuid.toString == guid.toString
    }
  }

  test("typeCast") {
    val intGuid: Guid[Int] = Guid.randomGuid()
    val strGuid: Guid[String] = intGuid.typeCast
    strGuid should be(intGuid)
  }

  test("toString randomized") {
    (0 to 100000).foreach { _ =>
      val uuid = UUID.randomUUID
      val guid = Guid[Int](uuid)
      guid.toString() should be (uuid.toString)
    }
  }

  test("fromString") {
    check { (msb: Long, lsb: Long) =>
      val uuid = new UUID(msb, lsb)
      val guid = Guid[Int](uuid.toString)
      uuid.toString == guid.toString
    }
  }

  test("fromString randomized") {
    (0 to 100000).foreach { _ =>
      val uuid = UUID.randomUUID
      val guid = Guid[Int](uuid.toString)
      guid.toString() should be (uuid.toString)
    }
  }

  test("from capitalized String") {
    (0 to 100000).foreach { _ =>
      val uuid = UUID.randomUUID
      val guid = Guid[Int](uuid.toString.toUpperCase)
      guid.toString() should be (uuid.toString)
    }
  }

  test("from String invalid input") {
    check { noise: Char => ( !ValidGuidChars.contains(noise) ) ==> {
      val uuidChars = UUID.randomUUID.toString.toCharArray
      val randomIdx = util.Random.nextInt(uuidChars.length)
      uuidChars(randomIdx) = noise
      val invalidInput = new String(uuidChars)

      try {
        Guid[Int](invalidInput) // should explode
        false
      } catch {
        case e: IllegalArgumentException => true
      }
    }}
  }

  test("to and from byte[]") {
    check { (msb: Long, lsb: Long) =>
      val uuid = new UUID(msb, lsb)
      val guid1 = Guid[Int](uuid.toString)
      val guid2 = Guid[Int](guid1.toBytes)
      (uuid.toString == guid1.toString) && (uuid.toString == guid2.toString)
    }
  }

  test("to and from byte[] randomized") {
    (0 to 100000).foreach { _ =>
      val uuid = UUID.randomUUID
      val guid1 = Guid[Int](uuid.toString)
      val guid2 = Guid[Int](guid1.toBytes)
      guid1.toString should be(uuid.toString)
      guid2.toString should be(uuid.toString)
    }
  }

  test("to byte[] format") {
    (0 to 100000).foreach { _ =>
      val uuid = UUID.randomUUID
      val hexUuid = uuid.toString.replaceAll("-", "")
      val guid = Guid[Int](uuid.toString)
      val hexGuid = toHexString(guid.toBytes)
      hexGuid should be(hexUuid)
    }
  }

  test("hashCode") {
    check { (msb: Long, lsb: Long) =>
      val uuid = new UUID(msb, lsb)
      val guid = Guid[Int](uuid)
      uuid.hashCode == guid.hashCode
    }
  }

  test("equals") {
    check { (msb: Long, lsb: Long, noiseByte: Byte) => // noise is chosen to be small, so that we see 0 some times
      val noise = noiseByte.asInstanceOf[Long]
      val guid1 = Guid[Int](new UUID(msb, lsb))
      val guid2 = Guid[Int](new UUID(msb + noise, lsb))
      val guid3 = Guid[Int](new UUID(msb, lsb + noise))

      if (noise == 0) {
        (guid1 == guid2) && (guid2 == guid3) && (guid3 == guid1)
      } else {
        (guid1 != guid2) && (guid2 != guid3) && (guid3 != guid1)
      }
    }
  }

  test("compare") {
    check { (msb1: Long, lsb1: Long,
             msb2: Long, lsb2: Long) =>
      val guid1 = Guid[Int](msb1, lsb1)
      val guid2 = Guid[Int](msb2, lsb2)

      guid1.compare(guid2) match {
        case 0  => guid1 == guid2
        case -1 => (msb1 < msb2) || ((msb1 == msb2) && (lsb1 < lsb2))
        case 1  => (msb1 > msb2) || ((msb1 == msb2) && (lsb1 > lsb2))
        case _ => println("Unexpected result from %s %s".format(guid1, guid2)) ; false
      }
    }
  }

  test("fromString performance") {
    val input = UUID.randomUUID.toString

    def uuidFromSting() { UUID.fromString(input) }
    def guidFromSting() { Guid[Int](input) }

    // warmup
    for(_ <- 0 to 100000) {
      uuidFromSting()
      guidFromSting()
    }

    val uuidTime = time("UUID from string"){ for(_ <- 0 to 1000000) { uuidFromSting()} }
    val guidTime = time("Guid from string"){ for(_ <- 0 to 1000000) { guidFromSting()} }

    // Diabled check due to intermitted failures in jenkins. Instead printing an exception stack trace.
    // guidTime should be < uuidTime    // lots of times more like 8x, but jenkins, parallel, etc.

    if (guidTime >= uuidTime) {
      new java.lang.Exception("FAILED GuidTest.testFromStringPerformance: guidTime=%f, uuidTime=%f".format(guidTime, uuidTime)).printStackTrace
    }
  }

  test("toString performance") {
    val input = UUID.randomUUID

    def uuidToSting() { input.toString }
    def guidToSting() { Guid[Int](input).toString }

    // warmup
    for(_ <- 0 to 100000) {
      uuidToSting()
      guidToSting()
    }

    val uuidTime = time("UUID toString()"){ for(_ <- 0 to 1000000) { uuidToSting()} }
    val guidTime = time("Guid toString()"){ for(_ <- 0 to 1000000) { guidToSting()} }

    // Diabled check due to intermitted failures in jenkins. Instead printing an exception stack trace.
    // guidTime should be < uuidTime    // lots of times more like 8x, but jenkins, parallel, etc.

    if (guidTime >= uuidTime) {
      new java.lang.Exception("FAILED GuidTest.testToStringPerformance: guidTime=%f, uuidTime=%f".format(guidTime, uuidTime)).printStackTrace
    }
  }

  test("de/serialize") {
    def serDeSer[T](guid: Guid[T]): Guid[T] = {
      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(guid)
      oos.close()
      baos.close()

      val bytes = baos.toByteArray

      val bais = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(bais)

      ois.readObject.asInstanceOf[Guid[T]]
    }

    val guid = Guid.randomGuid[Int]
    serDeSer(guid) should be(guid)
    serDeSer(Guid.randomGuid) should not be(guid)
  }

  private def toHexString(data: Array[Byte]): String = {
    data.map{ b => "%02x".format(b) }.mkString("")
  }

  // run a block of code, print and return time in millis
  private def time(msg: String)
                  (block: => Unit): Double = {
    val t0 = System.nanoTime
    block
    val elapsedMs: Double = (System.nanoTime - t0)/1000000.0
    println("%s elapsed ms: %s".format(msg, elapsedMs))
    elapsedMs
  }
}
