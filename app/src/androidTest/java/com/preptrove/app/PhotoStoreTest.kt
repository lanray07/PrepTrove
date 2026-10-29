package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import org.junit.Test
import org.junit.Assert.*
class PhotoStoreTest {
 @Test fun importedImageIsStoredPrivatelyAndInvalidNamesAreRejected() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val bitmap=Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888);val out=ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.PNG,100,out);bitmap.recycle()
  val name=PhotoStore.importBytes(context,out.toByteArray());val file=PhotoStore.file(context,name)!!
  try {assertTrue(file.canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath));assertEquals(8,BitmapFactory.decodeFile(file.path).width);assertNull(PhotoStore.file(context,"../../private.db"));assertTrue(runCatching {PhotoStore.importBytes(context,byteArrayOf(1,2,3))}.isFailure)} finally {file.delete()}
 }
}
