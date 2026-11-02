package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import org.junit.Test
import org.junit.Assert.*
import android.content.res.Configuration
import android.text.Layout
import android.text.TextPaint
import java.util.Locale
class PdfReportsTest {
 @Test fun reportLocaleControlsDirectionTypographyAndDatesIndependentlyOfDeviceLanguage() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  fun localized(tag:String)=context.createConfigurationContext(Configuration(context.resources.configuration).apply {setLocale(Locale.forLanguageTag(tag));setLayoutDirection(Locale.forLanguageTag(tag))})
  val arabic=localized("ar");val french=localized("fr-FR");val paint=TextPaint().apply {textSize=12f}
  assertEquals(Layout.DIR_RIGHT_TO_LEFT,PdfReports.paragraphLayout(arabic,"12345",paint).getParagraphDirection(0))
  assertEquals("ar",paint.textLocales[0].language)
  assertEquals(Layout.DIR_RIGHT_TO_LEFT,PdfReports.paragraphLayout(arabic,"ماء للشرب",paint).getParagraphDirection(0))
  assertEquals(Layout.DIR_LEFT_TO_RIGHT,PdfReports.paragraphLayout(french,"12345",paint).getParagraphDirection(0))
  assertEquals("1 nov. 2026",PdfReports.displayDate(french,"2026-11-01"))
  assertEquals("invalid-date",PdfReports.displayDate(french,"invalid-date"))
  val file=java.io.File.createTempFile("qa-rtl-",".pdf",context.cacheDir)
  try {
   file.outputStream().use {PdfReports.write(arabic,listOf(Entry(kind="supply",title="ماء للشرب",detail="مستلزمات المنزل",quantity=2.5,unit="لتر",expiry="2026-11-15")),it)}
   val page=PdfPreviewRenderer.render(file,0)
   try {assertTrue(page.image.width>0);assertEquals(1,page.count)} finally {page.image.recycle()}
  } finally {file.delete()}
 }
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
