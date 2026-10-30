package com.preptrove.app
object ExportPolicy {
 data class Options(val sensitive:Boolean=false,val notes:Boolean=true,val locations:Boolean=false,val phones:Boolean=false,val members:Boolean=false)
 fun select(records:List<Entry>,options:Options):List<Entry> = records.filter {options.sensitive || it.kind !in listOf("plan","contact")}.map {entry->
  val extra=StructuredFields.read(entry.extraData)
  if(!options.phones && entry.kind=="contact")extra.remove("phone")
  if(!options.members && entry.kind=="plan")extra.remove("members")
  if(!options.locations && entry.kind=="plan")extra.remove("meeting")
  entry.copy(detail=if(options.notes) entry.detail else "",location=if(options.locations) entry.location else "",extraData=extra.toString())
 }
}
