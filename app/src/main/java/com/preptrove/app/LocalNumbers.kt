package com.preptrove.app
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale
object LocalNumbers {
 fun input(value:Double,locale:Locale):String=NumberFormat.getNumberInstance(locale).apply {isGroupingUsed=false;maximumFractionDigits=12}.format(value)
 fun parse(value:String,locale:Locale):Double? {
  val text=value.trim();val position=ParsePosition(0)
  val number=NumberFormat.getNumberInstance(locale).apply {isGroupingUsed=false}.parse(text,position)?.toDouble()
  return number?.takeIf {position.index==text.length && it.isFinite() && it>=0}
 }
}
