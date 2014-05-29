package com.gilt.gfc.id

import org.scalatest.{FunSuite, Matchers}
import org.scalatest.prop.Checkers

class IdTest extends FunSuite with Matchers with Checkers {

  test("toString") {
    val id1 = Id(1L)
    id1.toString should be("1")

    val id2 = Id("foo")
    id2.toString should be("foo")

    val id3 = Id(List(1, 2, 3))
    id3.toString should be("List(1, 2, 3)")
  }

  test("typeCast") {
    trait Marker1
    trait Marker2

    val intId1: Id[Marker1, Int] = Id(123)
    val intId2: Id[Marker2, Int] = intId1.typeCast
    intId2 should be(intId1)

    val strId1: Id[Marker1, String] = Id("bar")
    val strId2 = strId1.typeCast[Marker2]
    strId2 should be(strId1)
  }

  test("hashCode") {
    Id(123).hashCode should be(123)
    Id(666L).hashCode should be(666)
    Id("baz").hashCode should be("baz".hashCode)
    Id(List(1, 2, 3)).hashCode should be(List(1, 2, 3).hashCode)
  }

  test("equals") {
    Id(123) should be(Id(123))
    Id(666) should be(Id(666L))
    Id("baz") should be(Id("baz"))
    Id(List(1, 2, 3)) should be(Id(List(1, 2, 3)))
    Id(666L) should not be(Id("bam"))

    Id(123) should be(123)
    Id(666L) should be(666L)
    Id("baz") should be("baz")
    Id(List(1, 2, 3)) should be(List(1, 2, 3))
    Id(666L) should not be("eggs")
  }
}
