package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import org.junit.Test
import org.junit.Assert.*
class PdfReportsTest {
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
