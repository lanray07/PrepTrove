package com.preptrove.app
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
internal data class PreviewPage(val image:Bitmap,val index:Int,val count:Int)
internal object PdfPreviewRenderer {
 fun render(file:File,page:Int):PreviewPage =
  ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use {descriptor->PdfRenderer(descriptor).use {renderer->
   require(renderer.pageCount>0)
   val index=page.coerceIn(0,renderer.pageCount-1)
   renderer.openPage(index).use {pdfPage->
    val bitmap=Bitmap.createBitmap(pdfPage.width*2,pdfPage.height*2,Bitmap.Config.ARGB_8888)
    try {
     bitmap.eraseColor(AndroidColor.WHITE)
     pdfPage.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
     PreviewPage(bitmap,index,renderer.pageCount)
    } catch(error:Exception) {bitmap.recycle();throw error}
   }
  }}
}
private data class PreviewResult(val page:PreviewPage?=null,val failed:Boolean=false)
@Composable fun ReportPreview(entries:List<Entry>,onClose:()->Unit) {
 val context=LocalContext.current
 var page by rememberSaveable {mutableStateOf(0)}
 val file=remember(entries) {File.createTempFile("preptrove-preview-",".pdf",context.cacheDir)}
 DisposableEffect(file) {onDispose {file.delete()}}
 val preview by produceState(PreviewResult(),file,page) {
  value=PreviewResult()
  value=withContext(Dispatchers.IO) {try {
   if(file.length()==0L)file.outputStream().use {PdfReports.write(context,entries,it)}
   PreviewResult(page=PdfPreviewRenderer.render(file,page))
  } catch(error:kotlinx.coroutines.CancellationException) {throw error}
    catch(error:Exception) {PreviewResult(failed=true)}}
 }
 Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false)) {
  Surface(Modifier.fillMaxSize()) {Column(Modifier.padding(20.dp)) {
   Row {Text(stringResource(R.string.report_preview),modifier=Modifier.weight(1f));TextButton(onClick=onClose,modifier=Modifier.testTag("report-close")) {Text(stringResource(R.string.language_close))}}
   preview.page?.let {value->
    Row {TextButton(enabled=value.index>0,onClick={page=value.index-1}) {Text(stringResource(R.string.report_previous))};Text(stringResource(R.string.report_page,value.index+1,value.count));TextButton(enabled=value.index+1<value.count,onClick={page=value.index+1}) {Text(stringResource(R.string.report_next))}}
    Image(value.image.asImageBitmap(),stringResource(R.string.report_preview),modifier=Modifier.fillMaxSize())
   } ?: Text(stringResource(if(preview.failed) R.string.report_error else R.string.report_loading))
  }}
 }
}
