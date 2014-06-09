package com.gilt.gfc.id

import java.lang.{StringBuilder => JStringBuilder}
import java.util.UUID
import scala.math.Ordered
import scala.beans.BeanProperty

/**
 * Tries to be a space/cpu efficient and type safe replacement for
 * java's UUID.
 *
 * Took Stephane's efficient String parsing code from pagegen to
 * replace UUID's lame built-in implementation.
 *
 * This class takes a single (although unused) type parameter.
 * This is to implemnet compile-time checks while still keeping the
 * json integration simple (only need to define (de)serialization
 * in one place)
 *
 * E.g.
 * {{{
 *      val orderGuid: Guid[Order] = ...
 *      val userGuid: Guid[User] = ...
 * }}}
 */
// intention is to always construct via apply(...) methods in the companion object
sealed class Guid[+A] protected(@BeanProperty val mostSignificantBits: Long, // preserve old GUID api
                                @BeanProperty val leastSignificantBits: Long) extends Ordered[Guid[_]] {

  import Guid.{appendHexDigits, UUID_BYTE_LENGTH, UUID_STRING_LENGTH}

  private[this] def this(ml: (Long, Long)) = this(ml._1, ml._2)
  def this(uuid: String) = this(Guid.parse(uuid))
  def this(uuid: UUID) = this(uuid.getMostSignificantBits, uuid.getLeastSignificantBits)

  final def toUUID: UUID = new UUID(mostSignificantBits, leastSignificantBits)

  /**
   * Safe way to cast an Guid of marker type A to Guid of marker type B.
   *
   * This should only be used to convert between Guids with equivalent
   * marker types (i.e. Guid[com.foo.User] -> Guid[org.bar.User]).
   *
   * It should _not_ be used to convert between Guids with unrelated marker
   * types, like User -> Order.
   *
   * @tparam B The target marker type
   * @return a new Guid
   */
  final def typeCast[B]: Guid[B] = new Guid(mostSignificantBits, leastSignificantBits)

  final def toBytes: Array[Byte] = {
    val buf = Array.ofDim[Byte](UUID_BYTE_LENGTH)
    longToBytes(buf, 0, mostSignificantBits)
    longToBytes(buf, 8, leastSignificantBits)
    buf
  }

  @inline
  private[this] final def longToBytes(buf: Array[Byte], pos: Int, num: Long) {
    buf(pos + 0) = (num >> 56).asInstanceOf[Byte]
    buf(pos + 1) = (num >> 48).asInstanceOf[Byte]
    buf(pos + 2) = (num >> 40).asInstanceOf[Byte]
    buf(pos + 3) = (num >> 32).asInstanceOf[Byte]
    buf(pos + 4) = (num >> 24).asInstanceOf[Byte]
    buf(pos + 5) = (num >> 16).asInstanceOf[Byte]
    buf(pos + 6) = (num >> 8).asInstanceOf[Byte]
    buf(pos + 7) = (num).asInstanceOf[Byte]
  }

  /** Optimized version (should generate much less garbage than UUID).
    * @return should produce same result as UUID.
    */
  final override def toString: String = {
    val strBuf = new JStringBuilder(UUID_STRING_LENGTH)
    val charBuf = Array.ofDim[Char](64)

    appendHexDigits(strBuf, charBuf, mostSignificantBits >> 32, 8)
    strBuf.append('-')
    appendHexDigits(strBuf, charBuf, mostSignificantBits >> 16, 4)
    strBuf.append('-')
    appendHexDigits(strBuf, charBuf, mostSignificantBits, 4)
    strBuf.append('-')
    appendHexDigits(strBuf, charBuf, leastSignificantBits >> 48, 4)
    strBuf.append('-')
    appendHexDigits(strBuf, charBuf, leastSignificantBits, 12)

    strBuf.toString
  }

  // same as UUID, not that it matters
  // NB: this is a val, so it *is* an additional field in the generated object,
  // however, we use hash maps a lot, so this is probably justified,
  // we can flip it to 'def' if not
  final override val hashCode: Int = {
    ((mostSignificantBits >> 32) ^ mostSignificantBits ^ (leastSignificantBits >> 32) ^ leastSignificantBits).asInstanceOf[Int]
  }

  // for simplicity's sake, do assume that they are really 'globally unique'
  // and don't check the type here, only the bits
  final override def equals(obj: Any): Boolean = obj match {
    case guid: Guid[_] => (guid.mostSignificantBits == this.mostSignificantBits) && (guid.leastSignificantBits == this.leastSignificantBits)
    case _ => false
  }

  // Ordered is also Comparable, so this should plug into existing java apis as well
  final override def compare(other: Guid[_]): Int = {
    if (this.mostSignificantBits < other.mostSignificantBits) {
      -1
    } else if (this.mostSignificantBits > other.mostSignificantBits) {
      1
    } else {
      if (this.leastSignificantBits < other.leastSignificantBits) {
        -1
      } else if (this.leastSignificantBits > other.leastSignificantBits) {
        1
      } else {
        0
      }
    }
  }
}

object Guid {
  /** length of a uuid as a byte array (16 bytes) */
  private val UUID_BYTE_LENGTH = 16

  /** length as a uuid as a string (36 characters) */
  private val UUID_STRING_LENGTH = 36

  private val DIGITS: Array[Char] = Array(
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f'
  )


  def apply[A](uuid: UUID): Guid[A] = {
    require(uuid != null, "uuid must not be null")
    new Guid[A](uuid.getMostSignificantBits,
      uuid.getLeastSignificantBits)
  }

  def apply[A](msb: Long, lsb: Long): Guid[A] = {
    new Guid[A](msb, lsb)
  }

  def apply[A](data: String): Guid[A] = {
    val bits = parse(data)
    Guid(bits._1, bits._2)
  }

  def parse(data: String): (Long, Long) = {
    require(data != null, "uuid must not be null")
    require(data.length == UUID_STRING_LENGTH, "uuid data must be 36 characters")

    var msb = parseHexLong(data, 0, 8)
    assertEquals(data, 8, '-')
    msb <<= 16
    msb |= parseHexLong(data, 9, 4)
    assertEquals(data, 13, '-')
    msb <<= 16
    msb |= parseHexLong(data, 14, 4)

    assertEquals(data, 18, '-')

    var lsb: Long = parseHexLong(data, 19, 4)
    lsb <<= 48
    assertEquals(data, 23, '-')
    lsb |= parseHexLong(data, 24, 12)

    (msb, lsb)
  }

  def apply[A](data: Array[Byte]): Guid[A] = {
    require(data != null, "uuid must not be null")
    require(data.length == UUID_BYTE_LENGTH, "uuid data must be 16 bytes")

    val msb = bytesToLong(data, 0)
    val lsb = bytesToLong(data, 8)
    Guid[A](msb, lsb)
  }

  def randomGuid[T](): Guid[T] = {
    Guid[T](UUID.randomUUID())
  }

  /** Decodes a Long from a given byte array starting at a given offset. */
  @inline
  private[this] final def bytesToLong(buf: Array[Byte], offset: Int): Long = {
    (buf(offset + 7) & 0xFF).asInstanceOf[Long] |
      ((buf(offset + 6) & 0xFF).asInstanceOf[Long] << 8) |
      ((buf(offset + 5) & 0xFF).asInstanceOf[Long] << 16) |
      ((buf(offset + 4) & 0xFF).asInstanceOf[Long] << 24) |
      ((buf(offset + 3) & 0xFF).asInstanceOf[Long] << 32) |
      ((buf(offset + 2) & 0xFF).asInstanceOf[Long] << 40) |
      ((buf(offset + 1) & 0xFF).asInstanceOf[Long] << 48) |
      ((buf(offset) & 0xFF).asInstanceOf[Long] << 56)
  }

  @inline
  private def assertEquals(data: String, offset: Int, expected: Char) {
    if (data.charAt(offset) != expected) {
      throw new IllegalArgumentException("Invalid character at offset " + offset + ": " + data)
    }
  }

  /**
   * Fast way to parse a long represented as a (positive) hexadecimal value. It does *NO* check of the data and only
   * handles positive values.
   *
   * @param data       the string representing the uuid (hexadecimal encoded)
   * @param startIndex start index of the block to decode
   * @param len        length of the block to decode
   * @return the representation in base 10 of the decoded block.
   */
  private def parseHexLong(data: CharSequence, startIndex: Int, len: Int): Long = {
    val radix: Int = 16
    val endIndex: Int = startIndex + len

    var digit: Int = hexToDigit(data.charAt(startIndex))
    var i: Int = startIndex + 1
    var result: Long = -digit

    while (i < endIndex) {
      digit = hexToDigit(data.charAt(i))
      result = result * radix - digit
      i += 1
    }
    -result
  }

  /**
   * Convert hex char to Int.
   *
   * @param c the character in hexadecimal
   * @return the value of the hexadecimal code
   */
  private def hexToDigit(c: Char): Int = {
    if ((c >= '0') && (c <= '9')) {
      c - 48
    } else if ((c >= 'A') && (c <= 'F')) {
      c - 55
    } else if ((c >= 'a') && (c <= 'f')) {
      c - 87
    } else {
      throw new IllegalArgumentException("Invalid hexadecimal code: " + c)
    }
  }

  /** Used by toString method,
    * implementation is adapted from that of UUID class to generate
    * less garbage (re-uses buffers, no intermediary strings).
    *
    * @param strBuf string buffer to append hex digits to
    * @param charBuf temporary buffer used to convert Int to hex digits (not strictly necessary,
    *                with some more work this can be optimized)
    * @param num number to encode
    * @param digits how many hex digits of a given number to encode
    *
    */
  private final def appendHexDigits(strBuf: JStringBuilder, charBuf: Array[Char], num: Long, digits: Int) {
    val hi = 1L << (digits * 4)
    val bitmasked = num & (hi - 1)
    val charPos = toHexString(charBuf, hi | bitmasked)
    strBuf.append(charBuf, charPos + 1, (63 - charPos))
  }

  /** Used by appendHexDigits,
    * an adaptation of the implementation from java.lang.Long,
    * re-uses char buffer, no intermediary strings.
    *
    * @param charBuf temporary buffer to write hex chars of the converted number to
    * @param num number to encode
    * @return last postition of the encoded data in the charBuf
    */
  @inline
  private final def toHexString(charBuf: Array[Char], num: Long): Int = {
    var i = num
    var charPos = 64
    do {
      charPos -= 1
      charBuf(charPos) = DIGITS((i & 0xF).asInstanceOf[Int])
      i >>>= 4
    } while (i != 0)

    charPos
  }
}
