package com.preptrove.app
import androidx.compose.runtime.saveable.Saver
import org.json.JSONObject
val EntryStateSaver=Saver<Entry?,String>(save={entry->entry?.let {
 JSONObject().put("id",it.id).put("kitId",it.kitId).put("title",it.title).put("record",BackupCodec.encode(listOf(it.copy(title=it.title.ifBlank {"Draft"}))).toString(Charsets.UTF_8)).toString()
} ?: ""},restore={saved->if(saved.isEmpty()) null else runCatching {
 val objectValue=JSONObject(saved);BackupCodec.decodeCopies(objectValue.getString("record").toByteArray()).single().copy(id=objectValue.getString("id"),kitId=objectValue.getString("kitId"),title=objectValue.getString("title"))
}.getOrNull()})
