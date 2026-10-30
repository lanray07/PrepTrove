package com.preptrove.app
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HistoryRestoreTest {
 @Test fun recordsAndReviewHistoryImportAtomically()=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val db=Room.inMemoryDatabaseBuilder(context,PrepDatabase::class.java).build()
  try {
   val dao=db.entries();val existing=Entry(kind="kit",title="Existing")
   dao.save(existing)
   val occupied=ReviewEvent(entryId=existing.id,title=existing.title,completedOn="2026-10-30")
   dao.insertReview(occupied)
   val imported=Entry(kind="reminder",title="Imported")
   val conflicting=occupied.copy(entryId=imported.id)
   assertTrue(runCatching {dao.importSnapshot(BackupSnapshot(listOf(imported),listOf(conflicting)))}.isFailure)
   assertEquals(listOf(existing),dao.all())
   val bytes=BackupCodec.encode(listOf(imported),listOf(conflicting))
   val restored=BackupCodec.decodeSnapshotCopies(bytes)
   dao.importSnapshot(restored)
   assertEquals(2,dao.all().size);assertEquals(2,dao.allReviews().size)
   assertTrue(dao.allReviews().any {it.entryId==restored.entries.single().id})
  } finally {db.close()}
 }
}
