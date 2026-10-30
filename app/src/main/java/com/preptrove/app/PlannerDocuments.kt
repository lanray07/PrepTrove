package com.preptrove.app

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream

/** File-provider I/O only. Parsing never mutates the database; the UI confirms restore separately. */
object PlannerDocuments {
 private fun validateUri(uri:Uri) {
  require(uri.scheme==android.content.ContentResolver.SCHEME_CONTENT && !uri.authority.isNullOrBlank()) {"A document-provider URI is required."}
 }
 fun read(context:Context,uri:Uri,encrypted:Boolean,password:CharArray):BackupSnapshot {
  validateUri(uri)
  val bytes=context.contentResolver.openInputStream(uri)?.use {input->
   val output=ByteArrayOutputStream();val buffer=ByteArray(8192)
   while(true) {val count=input.read(buffer);if(count<0)break
    require(output.size()+count<=BackupCrypto.MAX_BYTES) {"Backup exceeds the size limit."}
    output.write(buffer,0,count)
   }
   output.toByteArray()
  } ?: error("Source unavailable")
  return BackupCodec.decodeSnapshotCopies(if(encrypted) BackupCrypto.decrypt(bytes,password) else bytes)
 }

 fun write(context:Context,uri:Uri,snapshot:BackupSnapshot,format:String,options:ExportPolicy.Options,includeHistory:Boolean,password:CharArray) {
  validateUri(uri)
  require(format in listOf("csv","json","ptb","pdf"))
  val chosen=ExportPolicy.select(snapshot.entries,options)
  val history=if(includeHistory) snapshot.reviews else emptyList()
  // Validate/encode before opening a provider destination, which may truncate an existing file.
  val bytes=when(format) {
   "json"->BackupCodec.encode(chosen,history)
   "ptb"->BackupCrypto.encrypt(BackupCodec.encode(chosen,history),password)
   "csv"->Inventory.csv(chosen).toByteArray(Charsets.UTF_8)
   else->null
  }
  context.contentResolver.openOutputStream(uri,"wt")?.use {output->
   if(format=="pdf") PdfReports.write(context,chosen,output) else output.write(bytes!!)
  } ?: error("Destination unavailable")
 }
}
