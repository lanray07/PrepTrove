package com.preptrove.app
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
object PhotoStore {
 const val MAX_BYTES=8*1024*1024
 private val safeName=Regex("[0-9a-f-]{36}\\.jpg")
 private fun folder(context:Context)=File(context.noBackupFilesDir,"photos").apply {mkdirs()}
 fun file(context:Context,name:String):File?=if(safeName.matches(name)) File(folder(context),name) else null
 fun importBytes(context:Context,bytes:ByteArray):String {
  require(bytes.size<=MAX_BYTES)
  val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
  require(bounds.outWidth>0 && bounds.outHeight>0 && bounds.outWidth.toLong()*bounds.outHeight<=16000000)
  val bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.size,BitmapFactory.Options().apply {inPreferredConfig=Bitmap.Config.RGB_565}) ?: error("Invalid image")
  val name=UUID.randomUUID().toString()+".jpg"
  try {require((folder(context).listFiles()?.size ?: 0)<200);file(context,name)!!.outputStream().use {require(bitmap.compress(Bitmap.CompressFormat.JPEG,85,it))}} finally {bitmap.recycle()}
  return name
 }
 fun clear(context:Context) {val directory=folder(context).canonicalFile;directory.listFiles()?.filter {safeName.matches(it.name) && it.canonicalFile.parentFile==directory}?.forEach {it.delete()}}
}
@Composable fun PhotoEditor(json:String,onChange:(String)->Unit) {
 val context=LocalContext.current;val scope=rememberCoroutineScope();var error by remember {mutableStateOf(false)}
 val name=StructuredFields.read(json).optString("photo")
 val bitmap by produceState<Bitmap?>(null,name) {value=withContext(Dispatchers.IO) {PhotoStore.file(context,name)?.let {BitmapFactory.decodeFile(it.path,BitmapFactory.Options().apply {inSampleSize=4})}}}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {uri->if(uri!=null) scope.launch {
  try {val photo=withContext(Dispatchers.IO) {
   val bytes=context.contentResolver.openInputStream(uri)?.use {input->val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192);while(true) {val count=input.read(buffer);if(count<0) break;require(out.size()+count<=PhotoStore.MAX_BYTES);out.write(buffer,0,count)};out.toByteArray()} ?: error("Unavailable image")
   PhotoStore.importBytes(context,bytes)
  };onChange(StructuredFields.read(json).put("photo",photo).toString());error=false} catch(e:Exception) {error=true}
 }}
 Column {
  if(bitmap!=null) Image(bitmap!!.asImageBitmap(),contentDescription=stringResource(R.string.photo_description))
  if(name.isNotEmpty() && bitmap==null) Text(stringResource(R.string.photo_missing))
  TextButton(onClick={picker.launch("image/*")}) {Text(stringResource(R.string.photo_add))}
  if(name.isNotEmpty()) TextButton(onClick={onChange(StructuredFields.read(json).apply {remove("photo")}.toString())}) {Text(stringResource(R.string.photo_remove))}
  if(error) Text(stringResource(R.string.photo_error))
 }
}
