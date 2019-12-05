package com.gilt.gfc.id

import org.scalatestplus.scalacheck.Checkers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GuidSetTest extends AnyFunSuite with Matchers with Checkers {
  test("Basics") {
    val guid = Guid.randomGuid()
    val guidSet = GuidSet(guid)
    guidSet.contains(guid) shouldBe true
    guidSet.contains(Guid.randomGuid()) shouldBe false
    guidSet.size shouldBe 1
    guidSet.empty.size shouldBe 0
  }

  test("equality") {
    val guids = (1 to 5) map (_ => Guid.randomGuid())
    guids.size shouldBe 5
    guids.isEmpty shouldBe false
    val set1 = GuidSet(guids)
    val set2 = GuidSet(guids)
    set1 should equal (set2)
    set1.## should equal (set2.##)
    set1.toString should equal (set2.toString())

    val moreGuids = (1 to 5) map (_ => Guid.randomGuid())
    val set3 = GuidSet(moreGuids)
    set1 should not equal (set3)
    set1.## should not equal (set3.##)
    set1.toString should not equal (set3.toString())
  }

  test("all can be found") {
    val guids = (1 to 100) map (_ => Guid.randomGuid())
    val set = GuidSet(guids)
    guids foreach { set.contains(_) shouldBe true }
    (1 to 100) map (_ => Guid.randomGuid()) foreach { set.contains(_) shouldBe false }
  }

  test("works for duplicate high indexes") {
    val guids = (1L to 100L) map (l => Guid(100L, l))
    val set = GuidSet(guids)
    guids foreach { set.contains(_) shouldBe true }
    (1L to 100L) map (l => Guid(101L, l)) foreach { set.contains(_) shouldBe false }
    (1L to 100L) map (l => Guid(100L, l + 100L)) foreach { set.contains(_) shouldBe false }
  }

  test("can be turned into a list") {
    val guids = (1 to 100) map (_ => Guid.randomGuid()) toList
    val set = GuidSet(guids)
    val list = set.toList
    list.size shouldBe (guids.size)
    list.toSet shouldBe (guids.toSet)
  }

  test("Set.add whould throw") {
    an [RuntimeException] shouldBe thrownBy {
      GuidSet(Guid.randomGuid()) + Guid.randomGuid()
    }
  }

  test("Set.remove whould throw") {
    an [RuntimeException] shouldBe thrownBy {
      val guid = Guid.randomGuid()
      GuidSet(guid) - guid
    }
  }
}
