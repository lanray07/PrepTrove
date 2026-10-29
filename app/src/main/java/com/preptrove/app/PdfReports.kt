package com.preptrove.app
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import java.io.OutputStream
import java.time.LocalDate
object PdfReports {
 fun write(context:Context,entries:List<Entry>,output:OutputStream) {
  val document=PdfDocument();val paint=TextPaint().apply {textSize=12f;isAntiAlias=true}
  var page:PdfDocument.Page?=null;var number=0;var y=50f
  fun newPage() {
   page?.let {document.finishPage(it)};number++
   page=document.startPage(PdfDocument.PageInfo.Builder(595,842,number).create());y=50f
   page!!.canvas.drawText("PrepTrove · $number",36f,28f,paint)
  }
  fun paragraph(text:String) {
   if(text.isBlank())return
   val layout=StaticLayout.Builder.obtain(text,0,text.length,paint,523).setAlignment(Layout.Alignment.ALIGN_NORMAL).setTextDirection(TextDirectionHeuristics.LOCALE).setIncludePad(false).build()
   var first=0
   while(first<layout.lineCount) {
    if(page==null || y+18>795) newPage()
    val top=layout.getLineTop(first);var end=first
    while(end<layout.lineCount && layout.getLineBottom(end)-top<=795-y) end++
    if(end==first) {newPage();continue}
    val bottom=layout.getLineBottom(end-1);val canvas=page!!.canvas
    canvas.save();canvas.translate(36f,y-top);canvas.clipRect(0,top,523,bottom);layout.draw(canvas);canvas.restore()
    y+=bottom-top;first=end
   };y+=10
  }
  try {
   paragraph(context.getString(R.string.msg_88a395e76126));paragraph(LocalDate.now().toString())
   entries.filter {!it.isTemplate}.forEach {e->
    paragraph(kindLabel(context,e.kind)+": "+e.title)
    paragraph(e.detail)
    structuredLines(context,e).forEach {paragraph(it)}
    if(e.kind=="supply") paragraph(java.text.NumberFormat.getNumberInstance(context.resources.configuration.locales[0]).format(e.quantity)+" "+e.unit)
    if(e.location.isNotBlank())paragraph(context.getString(R.string.msg_b7b25d4790e6,e.location))
    if(e.expiry.isNotBlank())paragraph(context.getString(R.string.msg_f55bccec7713,e.expiry))
    if(e.purchasedDate.isNotBlank())paragraph(context.getString(R.string.purchase_date_value,e.purchasedDate))
   }
   page?.let {document.finishPage(it)};document.writeTo(output)
  } finally {document.close()}
 }
 fun structuredLines(context:Context,entry:Entry):List<String> {
  val fields=when(entry.kind) {"plan"->StructuredFields.plans;"contact"->StructuredFields.contacts;else->return emptyList()}
  val data=StructuredFields.read(entry.extraData)
  return fields.mapNotNull {(key,label)->data.optString(key).takeIf {it.isNotBlank()}?.let {context.getString(label)+": "+it}}
 }
}
