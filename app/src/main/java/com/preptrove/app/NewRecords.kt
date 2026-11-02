package com.preptrove.app

import android.content.Context

/** Localize only defaults for a new draft; never rewrite existing private records. */
internal fun Context.newPlannerRecord(kind:String,title:String="")=Entry(kind=kind,title=title,
 unit=getString(R.string.default_unit_items),category=getString(R.string.default_category_other))
