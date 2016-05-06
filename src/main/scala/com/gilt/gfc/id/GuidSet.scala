package com.gilt.gfc.id

import java.util.Arrays
import scala.annotation.tailrec

/**
  * A Set implementation for Guids, which stores the data in a memory efficient way that is optimized for fast look-ups
  *
  * @param highBits - an array of most significant bits of the Guids to be stored, sorted in ascending order.
  * @param lowBits - an array of the least significant bits of the Guids to be stored. The bits in this array must be
  * in sync with the bits in the highBits array.
  */
case class GuidSet[A] private (highBits: Array[Long], lowBits: Array[Long]) extends Set[Guid[A]] {
  override def empty: GuidSet[A] = GuidSet.empty

  override def size = highBits.length

  override def contains(elem: Guid[A]) = {
    val high = elem.mostSignificantBits
    val low = elem.leastSignificantBits

    @tailrec def sequentialSearch(index: Int, next: Int => Int): Boolean = {
      if (index < 0 || index >= highBits.length || highBits(index) != high) {
        false
      } else if (lowBits(index) == low) {
        true
      } else {
        sequentialSearch(next(index), next)
      }
    }

    val start = Arrays.binarySearch(highBits, high)
    sequentialSearch(start, _ + 1) || sequentialSearch(start - 1, _ - 1)
  }

  override def +(elem: Guid[A]) = sys.error("Too inefficient to support")

  override def -(elem: Guid[A]) = sys.error("Too inefficient to support")

  override def iterator = new Iterator[Guid[A]] {
    @volatile private var index = 0

    def hasNext = index < highBits.length

    def next(): Guid[A] = {
      val result = Guid[A](highBits(index), lowBits(index))
      index += 1
      result
    }
  }
}

object GuidSet {
  def apply[A](guid: Guid[A]): GuidSet[A] = apply(Seq(guid))

  def apply[A](guids: Seq[Guid[A]]): GuidSet[A] = {
    val (highBits: Array[Long], lowBits: Array[Long]) = {
      val sortedGuids = guids.map(guid =>
        (guid.mostSignificantBits, guid.leastSignificantBits)).sortWith(_._1 < _._1)
      (sortedGuids.map(_._1).toArray, sortedGuids.map(_._2).toArray)
    }
    new GuidSet(highBits, lowBits)
  }

  private val emptyInstance: GuidSet[Nothing] = GuidSet(Array(), Array())

  def empty[A]: GuidSet[A] = emptyInstance.asInstanceOf[GuidSet[A]]
}
