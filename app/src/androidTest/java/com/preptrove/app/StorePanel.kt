package com.preptrove.app

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.os.LocaleList
import java.util.Locale

/** Original store composition around a real, unchanged app capture. Native shaping supports RTL/CJK. */
internal fun storePanel(source:Bitmap,content:Rect,headline:String,locale:Locale,index:Int):Bitmap {
 val output=Bitmap.createBitmap(1920,1200,Bitmap.Config.ARGB_8888)
 val canvas=Canvas(output);canvas.drawColor(Color.rgb(38,59,77))
 val brand=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=Color.rgb(215,166,92);textSize=30f;typeface=Typeface.create("sans-serif",Typeface.BOLD)}
 canvas.drawText("PrepTrove",170f,58f,brand)
 val rtl=locale.language=="ar"
 var size=64f
 val text=TextPaint(Paint.ANTI_ALIAS_FLAG).apply {color=Color.WHITE;typeface=Typeface.create("sans-serif",Typeface.BOLD);textLocales=LocaleList(locale)}
 var layout:StaticLayout
 while(true) {
  text.textSize=size
  layout=StaticLayout.Builder.obtain(headline,0,headline.length,text,1580).setAlignment(Layout.Alignment.ALIGN_NORMAL)
   .setTextDirection(if(rtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.FIRSTSTRONG_LTR).setIncludePad(false).build()
  if(layout.height<=155 && layout.lineCount<=2)break
  size-=2f;require(size>=40f) {"Headline does not fit: "+locale.toLanguageTag()}
 }
 canvas.save();canvas.translate(170f,88f);layout.draw(canvas);canvas.restore()
 val frame=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=Color.rgb(215,166,92)}
 val scale=minOf(1560f/content.width(),900f/content.height())
 val width=content.width()*scale;val height=content.height()*scale;val left=(1920f-width)/2f
 canvas.drawRoundRect(RectF(left-10f,270f,left+width+10f,290f+height),18f,18f,frame)
 canvas.drawBitmap(source,content,RectF(left,280f,left+width,280f+height),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
 val number=java.text.NumberFormat.getIntegerInstance(locale).apply {minimumIntegerDigits=2}.format(index)
 brand.textAlign=Paint.Align.RIGHT;canvas.drawText(number,1750f,58f,brand)
 return output
}
