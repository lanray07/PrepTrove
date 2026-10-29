package com.preptrove.app
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class StorageTest {
 @Test fun deletingKitPreservesAndUnassignsSupplies()= runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val db=Room.inMemoryDatabaseBuilder(context,PrepDatabase::class.java).build()
  try {val dao=db.entries();val kit=Entry(kind="kit",title="Home");dao.save(kit);dao.save(Entry(kind="supply",title="Water",kitId=kit.id));dao.remove(kit);assertEquals(1,dao.all().size);assertEquals("",dao.all().single().kitId)} finally {db.close()}
 }
}
