package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import org.junit.Test
import org.junit.Assert.*
class PdfReportsTest {
 @Test fun previewRendererClampsPageAndRejectsCorruptFiles() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val file=java.io.File.createTempFile("qa-preview-",".pdf",context.cacheDir)
  try {
   file.outputStream().use {PdfReports.write(context,listOf(Entry(kind="kit",title="Preview test")),it)}
   val page=PdfPreviewRenderer.render(file,Int.MAX_VALUE)
   try {assertEquals(page.count-1,page.index);assertTrue(page.image.width>0)} finally {page.image.recycle()}
   file.writeText("This is not a PDF")
   assertTrue(runCatching {PdfPreviewRenderer.render(file,0)}.isFailure)
  } finally {file.delete()}
 }
 @Test fun longStructuredPlansProduceReadableMultipagePdf() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext;val file=java.io.File(context.cacheDir,"qa-report.pdf")
  val plan=Entry(kind="plan",title="Family plan",detail=(1..250).joinToString("\n") {"Review item $it for the household."},extraData="{\"meeting\":\"Library\"}")
  try {
   file.outputStream().use {PdfReports.write(context,listOf(plan),it)}
   assertTrue(PdfReports.structuredLines(context,plan).any {it.contains("Library")})
   ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use {fd->PdfRenderer(fd).use {renderer->assertTrue(renderer.pageCount>1);renderer.openPage(0).use {assertEquals(595,it.width)}}}
  } finally {file.delete()}
 }
}
