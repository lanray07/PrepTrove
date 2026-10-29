package com.preptrove.app
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
class PlannerFeaturesTest {
 private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun kitTemplateCreatesIndependentSupplies()=runBlocking {
  val db=Room.inMemoryDatabaseBuilder(context,PrepDatabase::class.java).build()
  try {
   val dao=db.entries();val kit=Entry(kind="kit",title="Home");dao.save(kit);dao.save(Entry(kind="supply",title="Water",kitId=kit.id,quantity=4.0))
   dao.makeTemplate(kit);val template=dao.all().single {it.kind=="kit" && it.isTemplate};dao.useTemplate(template)
   val copy=dao.all().single {it.kind=="kit" && !it.isTemplate && it.id!=kit.id}
   assertEquals(4.0,dao.all().single {it.kitId==copy.id}.quantity,0.0)
   dao.remove(template);assertEquals(4,dao.all().size);assertTrue(dao.all().none {it.isTemplate})
  } finally {db.close()}
 }
 @Test fun recurringReviewRecordsHistoryAndSchedulesNextDate()=runBlocking {
  val db=Room.inMemoryDatabaseBuilder(context,PrepDatabase::class.java).build()
  try {
   val dao=db.entries();val reminder=Entry(kind="reminder",title="Check batteries",expiry="2026-10-01",intervalDays=30);dao.save(reminder);dao.complete(reminder,"2026-10-29")
   assertEquals("2026-11-28",dao.all().single().expiry);assertFalse(dao.all().single().done);assertEquals(1,dao.reviews().first().size)
   val task=Entry(kind="check",title="Discuss plans");dao.save(task);dao.complete(task,"2026-10-29");assertTrue(dao.all().single {it.id==task.id}.done)
  } finally {db.close()}
 }
 @Test fun missingStoreConfigurationCannotOfferPurchases() {
  val billing=AmazonBilling(context);billing.start();assertFalse(billing.state.value.canPurchase);assertEquals("",billing.state.value.price)
 }
 @Test fun backupKeepsStructuredPlansAndPurchaseMetadata() {
  val entry=Entry(kind="plan",title="Family",extraData="{\"meeting\":\"Library\"}",purchasedDate="2026-10-29",intervalDays=30,isTemplate=true)
  val restored=BackupCodec.decodeCopies(BackupCodec.encode(listOf(entry))).single()
  assertEquals(entry.extraData,restored.extraData);assertEquals(entry.purchasedDate,restored.purchasedDate);assertEquals(30,restored.intervalDays);assertTrue(restored.isTemplate)
 }
}
