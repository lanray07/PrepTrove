package com.preptrove.app

import android.app.Application
import android.content.Context
import androidx.room.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@Entity(tableName="records")
data class Entry(@PrimaryKey val id:String=UUID.randomUUID().toString(), val kind:String, val title:String, val detail:String="", val category:String="Other", val kitId:String="", val quantity:Double=1.0, val unit:String="items", val expiry:String="", val location:String="", val done:Boolean=false, val checked:String="", @ColumnInfo(defaultValue="'{}'") val extraData:String="{}", @ColumnInfo(defaultValue="''") val purchasedDate:String="", @ColumnInfo(defaultValue="0") val intervalDays:Int=0, @ColumnInfo(defaultValue="0") val isTemplate:Boolean=false)
@Entity(tableName="review_events")
data class ReviewEvent(@PrimaryKey val id:String=UUID.randomUUID().toString(),val entryId:String,val title:String,val completedOn:String)
@Dao interface EntryDao {
 @Query("SELECT * FROM review_events ORDER BY completedOn DESC") fun reviews():Flow<List<ReviewEvent>>
 @Query("SELECT * FROM review_events ORDER BY completedOn DESC") suspend fun allReviews():List<ReviewEvent>
 @Transaction suspend fun backupSnapshot():BackupSnapshot = BackupSnapshot(all(),allReviews())
 @Query("DELETE FROM review_events") suspend fun clearReviews()
 @Query("DELETE FROM review_events WHERE entryId=:id") suspend fun deleteReviews(id:String)
 @Insert suspend fun insertReview(event:ReviewEvent)
 @Transaction suspend fun complete(entry:Entry,today:String) {
  val date=LocalDate.parse(today)
  insertReview(ReviewEvent(entryId=entry.id,title=entry.title,completedOn=today))
  save(if(entry.kind=="reminder") entry.copy(checked=today,done=entry.intervalDays==0,expiry=if(entry.intervalDays>0) date.plusDays(entry.intervalDays.toLong()).toString() else entry.expiry) else entry.copy(checked=today,done=if(entry.kind=="check") true else entry.done))
 }
 @Transaction suspend fun makeTemplate(entry:Entry) {
  val template=entry.copy(id=UUID.randomUUID().toString(),isTemplate=true,kitId="")
  save(template)
  if(entry.kind=="kit") all().filter {it.kitId==entry.id && !it.isTemplate}.forEach {save(it.copy(id=UUID.randomUUID().toString(),kitId=template.id,isTemplate=true))}
 }
 @Transaction suspend fun useTemplate(template:Entry) {
  val copy=template.copy(id=UUID.randomUUID().toString(),isTemplate=false,done=false,checked="",kitId="")
  save(copy)
  if(template.kind=="kit") all().filter {it.kitId==template.id && it.isTemplate}.forEach {save(it.copy(id=UUID.randomUUID().toString(),kitId=copy.id,isTemplate=false,done=false,checked=""))}
 }

 @Query("SELECT * FROM records ORDER BY title COLLATE NOCASE") fun observe():Flow<List<Entry>>
 @Query("SELECT * FROM records") suspend fun all():List<Entry>
 @Upsert suspend fun save(entry:Entry)
 @Query("DELETE FROM records WHERE id=:id") suspend fun delete(id:String)
 @Query("DELETE FROM records") suspend fun clear()
 @Transaction suspend fun importCopies(entries:List<Entry>) { entries.forEach { save(it) } }
 @Transaction suspend fun importSnapshot(snapshot:BackupSnapshot) {
  importCopies(snapshot.entries)
  snapshot.reviews.forEach {insertReview(it)}
 }
 @Transaction suspend fun remove(entry:Entry) { if(entry.kind=="kit") all().filter { it.kitId==entry.id }.forEach { if(entry.isTemplate && it.isTemplate) {deleteReviews(it.id);delete(it.id)} else save(it.copy(kitId="")) }; deleteReviews(entry.id); delete(entry.id) }
}
@Database(entities=[Entry::class,ReviewEvent::class],version=2,exportSchema=true)
abstract class PrepDatabase:RoomDatabase() {
 abstract fun entries():EntryDao
 companion object {
  val MIGRATION_1_2=object:androidx.room.migration.Migration(1,2) {
   override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE records ADD COLUMN extraData TEXT NOT NULL DEFAULT '{}'")
    db.execSQL("ALTER TABLE records ADD COLUMN purchasedDate TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE records ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 0")
    db.execSQL("ALTER TABLE records ADD COLUMN isTemplate INTEGER NOT NULL DEFAULT 0")
    db.execSQL("CREATE TABLE IF NOT EXISTS review_events (id TEXT NOT NULL PRIMARY KEY, entryId TEXT NOT NULL, title TEXT NOT NULL, completedOn TEXT NOT NULL)")
   }
  }
 }
}
val Context.preferences by preferencesDataStore("preferences")
class PrepTrove:Application() {
 val billing by lazy {AmazonBilling(this)}
 val db by lazy { Room.databaseBuilder(this,PrepDatabase::class.java,"preptrove.db").addMigrations(PrepDatabase.MIGRATION_1_2).build() }
 override fun onCreate() { super.onCreate(); ReminderWorker.schedule(this) }
}
class PlannerModel(app:Application):AndroidViewModel(app) {
 private val dao=(app as PrepTrove).db.entries()
 val backupPassword=androidx.compose.runtime.mutableStateOf("")
 suspend fun snapshot():List<Entry> = dao.all()
 suspend fun backupSnapshot():BackupSnapshot = dao.backupSnapshot()
 val records=dao.observe().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val reviews=dao.reviews().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 fun complete(e:Entry)=viewModelScope.launch {dao.complete(e,LocalDate.now().toString())}
 fun makeTemplate(e:Entry)=viewModelScope.launch {dao.makeTemplate(e)}
 fun useTemplate(e:Entry)=viewModelScope.launch {dao.useTemplate(e)}
 val darkTheme=app.preferences.data.map { it[booleanPreferencesKey("darkTheme")] ?: false }.stateIn(viewModelScope,SharingStarted.Eagerly,false)
 fun setDarkTheme(value:Boolean)=viewModelScope.launch { getApplication<Application>().preferences.edit { it[booleanPreferencesKey("darkTheme")]=value } }
 val onboarded=app.preferences.data.map { it[booleanPreferencesKey("onboarded")] ?: false }.stateIn(viewModelScope,SharingStarted.Eagerly,false)
 fun save(e:Entry)=viewModelScope.launch { dao.save(e) }
 fun remove(e:Entry)=viewModelScope.launch { dao.remove(e) }
 suspend fun importCopies(entries:List<Entry>) { dao.importCopies(entries) }
 suspend fun importSnapshot(snapshot:BackupSnapshot) {dao.importSnapshot(snapshot)}
 fun clear()=viewModelScope.launch { getApplication<Application>().dbClear() ;getApplication<Application>().preferences.edit {it.remove(stringPreferencesKey("household"));it.remove(stringPreferencesKey("region"))} }
 fun onboard(size:String,region:String)=viewModelScope.launch { getApplication<Application>().preferences.edit { it[booleanPreferencesKey("onboarded")]=true;it[stringPreferencesKey("household")]=size;it[stringPreferencesKey("region")]=region } }
}
object Inventory {
 fun attention(entries:List<Entry>,today:LocalDate=LocalDate.now())=entries.filter { it.kind=="supply" && !it.isTemplate && it.expiry.isNotEmpty() && runCatching { !LocalDate.parse(it.expiry).isAfter(today.plusDays(30)) }.getOrDefault(false) }
 fun csv(entries:List<Entry>):String {
  fun quote(s:String):String { val safe=if(s.firstOrNull() in listOf('=','+','-','@','\t','\r')) "'"+s else s;return "\""+safe.replace("\"","\"\"")+"\"" }
  return "Name,Quantity,Unit,Expiry,Location\r\n"+entries.filter { it.kind=="supply" && !it.isTemplate }.joinToString("\r\n") { e->listOf(e.title,e.quantity.toString(),e.unit,e.expiry,e.location).joinToString(",") { quote(it) } }
 }
}

private suspend fun Application.dbClear() { val db=(this as PrepTrove).db; db.withTransaction {db.entries().clear();db.entries().clearReviews()}; kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {PhotoStore.clear(this@dbClear)} }
