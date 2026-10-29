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
data class Entry(@PrimaryKey val id:String=UUID.randomUUID().toString(), val kind:String, val title:String, val detail:String="", val category:String="Other", val kitId:String="", val quantity:Double=1.0, val unit:String="items", val expiry:String="", val location:String="", val done:Boolean=false, val checked:String="")
@Dao interface EntryDao {
 @Query("SELECT * FROM records ORDER BY title COLLATE NOCASE") fun observe():Flow<List<Entry>>
 @Query("SELECT * FROM records") suspend fun all():List<Entry>
 @Upsert suspend fun save(entry:Entry)
 @Query("DELETE FROM records WHERE id=:id") suspend fun delete(id:String)
 @Query("DELETE FROM records") suspend fun clear()
 @Transaction suspend fun remove(entry:Entry) { if(entry.kind=="kit") all().filter { it.kitId==entry.id }.forEach { save(it.copy(kitId="")) }; delete(entry.id) }
}
@Database(entities=[Entry::class],version=1,exportSchema=false)
abstract class PrepDatabase:RoomDatabase() { abstract fun entries():EntryDao }
val Context.preferences by preferencesDataStore("preferences")
class PrepTrove:Application() {
 val db by lazy { Room.databaseBuilder(this,PrepDatabase::class.java,"preptrove.db").build() }
 override fun onCreate() { super.onCreate(); ReminderWorker.schedule(this) }
}
class PlannerModel(app:Application):AndroidViewModel(app) {
 private val dao=(app as PrepTrove).db.entries()
 val records=dao.observe().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val darkTheme=app.preferences.data.map { it[booleanPreferencesKey("darkTheme")] ?: false }.stateIn(viewModelScope,SharingStarted.Eagerly,false)
 fun setDarkTheme(value:Boolean)=viewModelScope.launch { getApplication<Application>().preferences.edit { it[booleanPreferencesKey("darkTheme")]=value } }
 val onboarded=app.preferences.data.map { it[booleanPreferencesKey("onboarded")] ?: false }.stateIn(viewModelScope,SharingStarted.Eagerly,false)
 fun save(e:Entry)=viewModelScope.launch { dao.save(e) }
 fun remove(e:Entry)=viewModelScope.launch { dao.remove(e) }
 fun clear()=viewModelScope.launch { dao.clear() }
 fun onboard(size:String,region:String)=viewModelScope.launch { getApplication<Application>().preferences.edit { it[booleanPreferencesKey("onboarded")]=true;it[stringPreferencesKey("household")]=size;it[stringPreferencesKey("region")]=region } }
}
object Inventory {
 fun attention(entries:List<Entry>,today:LocalDate=LocalDate.now())=entries.filter { it.kind=="supply" && it.expiry.isNotEmpty() && runCatching { !LocalDate.parse(it.expiry).isAfter(today.plusDays(30)) }.getOrDefault(false) }
 fun csv(entries:List<Entry>):String {
  fun quote(s:String):String { val safe=if(s.firstOrNull() in listOf('=','+','-','@','\t','\r')) "'"+s else s;return "\""+safe.replace("\"","\"\"")+"\"" }
  return "Name,Quantity,Unit,Expiry,Location\r\n"+entries.filter { it.kind=="supply" }.joinToString("\r\n") { e->listOf(e.title,e.quantity.toString(),e.unit,e.expiry,e.location).joinToString(",") { quote(it) } }
 }
}
