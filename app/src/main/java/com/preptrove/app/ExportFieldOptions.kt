package com.preptrove.app
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
@Composable fun ExportFieldOptions(notes:Boolean,onNotes:(Boolean)->Unit,locations:Boolean,onLocations:(Boolean)->Unit,phones:Boolean,onPhones:(Boolean)->Unit,members:Boolean,onMembers:(Boolean)->Unit) {
 Column {
  Text(stringResource(R.string.export_field_warning))
  ExportFieldToggle(notes,onNotes,R.string.export_notes)
  ExportFieldToggle(locations,onLocations,R.string.export_locations)
  ExportFieldToggle(phones,onPhones,R.string.export_phones)
  ExportFieldToggle(members,onMembers,R.string.export_members)
 }
}
@Composable private fun ExportFieldToggle(value:Boolean,onChange:(Boolean)->Unit,label:Int) {Row(Modifier.fillMaxWidth().toggleable(value=value,role=Role.Checkbox,onValueChange=onChange),verticalAlignment=Alignment.CenterVertically) {Checkbox(value,null);Text(stringResource(label))}}
