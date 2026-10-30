package com.preptrove.app
import org.junit.Test
import org.junit.Assert.*
class ExportPolicyTest {
 @Test fun defaultExportDoesNotIncludeContactsOrPlans() {
  val rows=listOf(Entry(kind="contact",title="Private contact"),Entry(kind="plan",title="Private plan"),Entry(kind="supply",title="Water"))
  assertEquals(listOf("Water"),ExportPolicy.select(rows,ExportPolicy.Options()).map {it.title})
 }
 @Test fun namedFieldRedactionPreservesOriginalRecords() {
  val plan=Entry(kind="plan",title="Plan",detail="Personal note",location="Home",extraData="{\"members\":\"Member A\",\"meeting\":\"Library\",\"communication\":\"Check in\"}")
  val contact=Entry(kind="contact",title="Contact",extraData="{\"phone\":\"Private phone\"}")
  val rows=ExportPolicy.select(listOf(plan,contact),ExportPolicy.Options(sensitive=true,notes=false))
  assertEquals("",rows[0].detail);assertEquals("",rows[0].location)
  assertFalse(StructuredFields.read(rows[0].extraData).has("members"));assertFalse(StructuredFields.read(rows[0].extraData).has("meeting"));assertFalse(StructuredFields.read(rows[1].extraData).has("phone"))
  assertEquals("Check in",StructuredFields.read(rows[0].extraData).getString("communication"));assertEquals("Personal note",plan.detail)
 }
}
