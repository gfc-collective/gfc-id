package com.gilt.gfc.id

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

import org.scalatestplus.scalacheck.Checkers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class IdTest extends AnyFunSuite with Matchers with Checkers {

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


  test("de/serialize") {
    def serDeSer[A, B](id: Id[A, B]): Id[A, B] = {
      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(id)
      oos.close()
      baos.close()

      val bytes = baos.toByteArray

      val bais = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(bais)

      ois.readObject.asInstanceOf[Id[A, B]]
    }

    serDeSer(Id(123L)) should be(Id(123L))
    val id: Id[String, String] = Id("foo")
    serDeSer(id) should be(id)
    serDeSer(id) should be(Id("foo"))
    serDeSer(id) should be(Id[Int, String]("foo"))
    serDeSer(id) should not be(Id("bar"))
  }
}
