package com.preptrove.app

import android.content.Context
import org.json.JSONObject
import java.net.URI
import java.util.Locale

internal data class GuidanceSource(val country:String,val publisher:String,val url:String) {
 fun countryName(locale:Locale)=Locale("",country).getDisplayCountry(locale)
}
internal object GuidanceDirectory {
 fun entries(context:Context):List<GuidanceSource> {
  val root=JSONObject(context.assets.open("official-sources.json").bufferedReader().use {it.readText()})
  require(root.getString("scope")=="external_links_only" && !root.getBoolean("adviceCached"))
  val values=root.getJSONArray("sources")
  return (0 until values.length()).map {index->val item=values.getJSONObject(index)
   val url=item.getString("url");val parsed=URI(url)
   require(parsed.scheme=="https" && !parsed.host.isNullOrBlank() && parsed.userInfo==null)
   GuidanceSource(item.getString("country"),item.getString("publisher"),url)
  }
 }
 fun selected(context:Context)=context.getSharedPreferences("guidance-location",0).getString("country","").orEmpty().takeIf {value->entries(context).any {it.country==value}}.orEmpty()
 fun select(context:Context,country:String) {
  require(entries(context).any {it.country==country})
  context.getSharedPreferences("guidance-location",0).edit().putString("country",country).apply()
 }
}
