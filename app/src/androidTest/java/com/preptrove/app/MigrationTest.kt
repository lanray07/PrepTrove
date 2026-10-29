package com.preptrove.app
import androidx.room.Room
import androidx.sqlite.db.*
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
class MigrationTest {
 @Test fun upgradePreservesVersionOneSupplies()=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext;val name="migration-"+System.nanoTime()+".db"
  val helper=FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(object:SupportSQLiteOpenHelper.Callback(1) {
   override fun onCreate(db:SupportSQLiteDatabase) {db.execSQL("CREATE TABLE records (id TEXT NOT NULL PRIMARY KEY, kind TEXT NOT NULL, title TEXT NOT NULL, detail TEXT NOT NULL, category TEXT NOT NULL, kitId TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT NOT NULL, location TEXT NOT NULL, done INTEGER NOT NULL, checked TEXT NOT NULL)");db.execSQL("INSERT INTO records VALUES ('water','supply','Water','','Water','',2.0,'bottles','','Shelf',0,'')")}
   override fun onUpgrade(db:SupportSQLiteDatabase,old:Int,new:Int) {}
  }).build())
  helper.writableDatabase;helper.close()
  val db=Room.databaseBuilder(context,PrepDatabase::class.java,name).addMigrations(PrepDatabase.MIGRATION_1_2).build()
  try {val supply=db.entries().all().single();assertEquals("Water",supply.title);assertEquals(2.0,supply.quantity,0.0);assertEquals("{}",supply.extraData);assertFalse(supply.isTemplate)} finally {db.close();context.deleteDatabase(name)}
 }
}
