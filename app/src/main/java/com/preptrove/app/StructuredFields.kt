package com.preptrove.app
import org.json.JSONObject
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
object StructuredFields {
 val plans=linkedMapOf("members" to R.string.plan_members,"meeting" to R.string.plan_meeting,"communication" to R.string.plan_communication,"evacuation" to R.string.plan_evacuation,"shelter" to R.string.plan_shelter,"pets" to R.string.plan_pets,"accessibility" to R.string.plan_accessibility,"documents" to R.string.plan_documents,"checkIn" to R.string.plan_checkin)
 val contacts=linkedMapOf("phone" to R.string.contact_phone,"relationship" to R.string.contact_relationship,"organisation" to R.string.contact_organisation)
 fun read(json:String)=runCatching {JSONObject(json)}.getOrElse {JSONObject()}
 fun summary(json:String):String {val data=read(json);return data.keys().asSequence().map {data.optString(it)}.filter {it.isNotBlank()}.joinToString("\n")}
}
@Composable fun StructuredEditor(kind:String,json:String,onChange:(String)->Unit) {
 val data=StructuredFields.read(json)
 Column {(if(kind=="plan") StructuredFields.plans else StructuredFields.contacts).forEach {(key,label)->
  OutlinedTextField(value=data.optString(key),onValueChange={onChange(StructuredFields.read(json).put(key,it).toString())},label={Text(stringResource(label))})
 }}
}
