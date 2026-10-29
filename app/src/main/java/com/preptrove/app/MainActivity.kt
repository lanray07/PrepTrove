package com.preptrove.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.Locale

val Navy=Color(0xFF263B4D)
val Teal=Color(0xFF2C7A7B)
val Ivory=Color(0xFFF8F6F0)
val Amber=Color(0xFFD7A65C)
val sections=listOf("Overview","Kits","Supplies","Family plans","Contacts","Checklists","Reminders","Reference","Voice","Settings")
val kinds=mapOf("Kits" to "kit","Supplies" to "supply","Family plans" to "plan","Contacts" to "contact","Checklists" to "check","Reminders" to "reminder")

class MainActivity:ComponentActivity() {
 private var recognizer:SpeechRecognizer?=null
 private var tts:TextToSpeech?=null
 private var speechReady=false
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  tts=TextToSpeech(this) { status-> speechReady=status==TextToSpeech.SUCCESS && (tts?.setLanguage(Locale.getDefault()) ?: -1)>=0 }
  setContent { Planner() }
 }
 override fun onDestroy() { recognizer?.destroy();tts?.stop();tts?.shutdown();super.onDestroy() }
 private fun speak(text:String) { if(speechReady) tts?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"response") }
 private fun listen(update:(String)->Unit,active:(Boolean)->Unit) {
  if(!SpeechRecognizer.isRecognitionAvailable(this)) { update("No recognition service is installed. Type your command below.");return }
  recognizer?.destroy();recognizer=SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object:RecognitionListener {
   override fun onReadyForSpeech(p:Bundle?) { active(true) }
   override fun onBeginningOfSpeech() {}
   override fun onRmsChanged(r:Float) {}
   override fun onBufferReceived(b:ByteArray?) {}
   override fun onEndOfSpeech() { active(false) }
   override fun onError(e:Int) {active(false);update("Voice recognition unavailable or interrupted ($e). Try again or type a command.")}
   override fun onResults(b:Bundle?) {active(false);update(b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())}
   override fun onPartialResults(b:Bundle?) {}
   override fun onEvent(t:Int,b:Bundle?) {}
  })
  try { recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag())) } catch(e:Exception) { active(false);update("Recognition could not start. Use text instead.") }
 }
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable fun Planner(model:PlannerModel=viewModel()) {
  val records by model.records.collectAsStateWithLifecycle()
  val onboarded by model.onboarded.collectAsStateWithLifecycle()
  var section by remember { mutableStateOf("Overview") }
  var editing by remember { mutableStateOf<Entry?>(null) }
  var deleting by remember { mutableStateOf<Entry?>(null) }
  var wipe by remember { mutableStateOf(false) }
  var search by remember { mutableStateOf("") }
  val dark by model.darkTheme.collectAsStateWithLifecycle()
  var command by remember { mutableStateOf("") }
  var response by remember { mutableStateOf("") }
  var listening by remember { mutableStateOf(false) }
  var exportType by remember { mutableStateOf("") }
  var exportConfirm by remember { mutableStateOf(false) }
  var sensitive by remember { mutableStateOf(false) }
  var status by remember { mutableStateOf("") }
  val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted->if(granted) listen({command=it},{listening=it}) else {response="Microphone permission was declined. Text commands are available."} }
  val notifications=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { status=if(it) "Notifications enabled. Delivery timing depends on your device." else "Notifications declined. Saved reminders remain available here." }
  val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri->
   if(uri!=null) try {
    val chosen=records.filter { sensitive || (it.kind!="contact" && it.kind!="plan") }
    contentResolver.openOutputStream(uri)?.use { stream->
     when(exportType) {
      "csv"->stream.write(Inventory.csv(chosen).toByteArray())
      "json"->{val array=JSONArray();chosen.forEach { e->array.put(JSONObject().put("id",e.id).put("kind",e.kind).put("title",e.title).put("detail",e.detail).put("category",e.category).put("kitId",e.kitId).put("quantity",e.quantity).put("unit",e.unit).put("expiry",e.expiry).put("location",e.location).put("done",e.done).put("checked",e.checked)) };stream.write(JSONObject().put("schemaVersion",1).put("records",array).toString(2).toByteArray())}
      "pdf"->{ val pdf=PdfDocument();val paint=Paint().apply {textSize=12f};var pageNumber=0;var page:PdfDocument.Page?=null;var y=0f
       fun newPage() {page?.let {pdf.finishPage(it)};pageNumber++;page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,pageNumber).create());y=45f}
       fun line(s:String) {if(page==null || y>790) newPage();page!!.canvas.drawText(s,36f,y,paint);y+=20f}
       line("PrepTrove | Household preparedness");line("Prepared ${LocalDate.now()} | Saved planning records")
       chosen.forEach { e->line("${e.kind.uppercase(Locale.ROOT)}: ${e.title}"); (e.detail+" "+e.location+" "+e.expiry+if(e.kind=="supply") " ${e.quantity} ${e.unit}" else "").chunked(75).forEach {line(it)} }
       page?.let {pdf.finishPage(it)};pdf.writeTo(stream);pdf.close()
      }
     }
    } ?: error("Destination unavailable")
    status="Export saved. Keep the file somewhere private."
   } catch(e:Exception) { status="Export could not be saved. Check the destination and try again." }
  }
  MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=Color(0xFF83CFCA),background=Color(0xFF152632),surface=Color(0xFF203746)) else lightColorScheme(primary=Teal,background=Ivory,surface=Color.White,onSurface=Navy)) {
   Surface(Modifier.fillMaxSize()) {
    BoxWithConstraints {
     val wide=maxWidth>=840.dp
     Row {
      if(wide) Column(Modifier.width(220.dp).fillMaxHeight().background(Navy).padding(20.dp)) {
       Text(androidx.compose.ui.res.stringResource(R.string.preptrove_c7187b),color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.your_household_organised_96de4e),color=Amber,fontSize=10.sp)
       Spacer(Modifier.height(32.dp))
       sections.forEach { s->TextButton(onClick={section=s;search=""},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),colors=ButtonDefaults.textButtonColors(contentColor=if(section==s) Amber else Color.White)) {Text(s,modifier=Modifier.fillMaxWidth())} }
      }
      Scaffold(modifier=Modifier.weight(1f),topBar={TopAppBar(title={Column {Text(if(wide) section else "PrepTrove",fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.prepared_today_ready_for_tomorrow_6786a9),fontSize=12.sp)}},actions={IconButton(onClick={section="Voice"}) {Icon(Icons.Outlined.Mic,"Voice assistant")}})},floatingActionButton={if(section in kinds) FloatingActionButton(onClick={editing=Entry(kind=kinds.getValue(section),title="")}) {Icon(Icons.Outlined.Add,"Add record")}},bottomBar={if(!wide) ScrollableTabRow(selectedTabIndex=sections.indexOf(section),edgePadding=0.dp) {sections.forEach {s->Tab(selected=section==s,onClick={section=s;search=""},text={Text(s)})}}}) { padding->
       LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal=if(wide) 32.dp else 20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(top=16.dp,bottom=100.dp)) {
        item { Text(androidx.compose.ui.res.stringResource(R.string.saved_records_work_offline_eceb81),color=MaterialTheme.colorScheme.primary,fontSize=12.sp) }
        if(status.isNotEmpty()) item { Text(status) }
        when(section) {
         "Overview"->{
          item {Card(colors=CardDefaults.cardColors(containerColor=Navy),shape=RoundedCornerShape(24.dp)) {Column(Modifier.padding(28.dp)) {Text(androidx.compose.ui.res.stringResource(R.string.a_little_preparation_more_peace_of_mind_48526d),color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));Text(androidx.compose.ui.res.stringResource(R.string.your_essentials_and_household_plans_together_in__354383),color=Color(0xFFCDDCD9));Spacer(Modifier.height(16.dp));Button(onClick={section="Kits"},colors=ButtonDefaults.buttonColors(containerColor=Amber,contentColor=Navy)) {Text(androidx.compose.ui.res.stringResource(R.string.organise_your_kit_e4829c))}}} }
          item {Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {Metric("Kits",records.count{it.kind=="kit"},Modifier.weight(1f));Metric("Supplies",records.count{it.kind=="supply"},Modifier.weight(1f));Metric("Due soon",Inventory.attention(records).size,Modifier.weight(1f))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.your_next_small_step_b68082),fontSize=22.sp,fontWeight=FontWeight.SemiBold)}
          item {InfoCard("Build a household plan","Record a meeting point and how you’ll check in.","Open family plans") {section="Family plans"} }
          item {InfoCard("Supplies to review","${Inventory.attention(records).size} supplies expire within 30 days or are past their recorded date.","Review supplies") {section="Supplies"} }
          item {InfoCard("Keep useful contacts close","Save phone numbers manually, with no access to your device contacts.","Open contacts") {section="Contacts"} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.planning_progress_is_not_a_guarantee_of_safety_f_26ae34),fontSize=12.sp)}
         }
         "Reference"->{
          item {Text(androidx.compose.ui.res.stringResource(R.string.saved_guidance_united_kingdom_8a9326),fontSize=24.sp,fontWeight=FontWeight.Bold)}
          item {Text(androidx.compose.ui.res.stringResource(R.string.general_planning_information_not_live_alerts_for_127afe))}
          item {InfoCard("Start with what you have","Consider a torch, phone power bank, drinking water and food suited to your household. Keep supplies easy to find and review their condition.","Source: UK Government Prepare") {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://prepare.campaign.gov.uk/get-prepared-for-emergencies/")))} }
          item {InfoCard("Talk through your plan","Discuss a meeting point, communication arrangements and any support your household may need.","Review your household plan") {section="Family plans"} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.source_uk_government_prepare_https_prepare_campa_17bb7e),fontSize=12.sp)}
         }
         "Voice"->{
          item {Text(androidx.compose.ui.res.stringResource(R.string.your_voice_your_planner_4e42c9),fontSize=26.sp,fontWeight=FontWeight.Bold)}
          item {Text(androidx.compose.ui.res.stringResource(R.string.push_to_talk_then_review_the_transcript_your_dev_afdb27))}
          item {Button(onClick={if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) listen({command=it},{listening=it}) else permission.launch(Manifest.permission.RECORD_AUDIO)}) {Icon(Icons.Outlined.Mic,null);Text(if(listening) "Listening…" else "Start listening")} }
          item {OutlinedTextField(value=command,onValueChange={command=it},label={Text(androidx.compose.ui.res.stringResource(R.string.transcript_or_typed_command_48008f))},modifier=Modifier.fillMaxWidth())}
          item {Button(onClick={val c=command.lowercase(Locale.ROOT);when {
           "expiring" in c->{response="${Inventory.attention(records).size} supplies need an expiry review."}
           "contact" in c->{section="Contacts";response="Your contacts are ready."}
           "checklist" in c->{section="Checklists";response="Your checklists are ready."}
           "read" in c && "plan" in c->{response=records.filter {it.kind=="plan"}.joinToString(". ") {it.title+". "+it.detail}.ifEmpty {"No family plans have been saved."}}
           c.startsWith("add ")->{editing=Entry(kind="supply",title=command.drop(4).substringBefore(" to my "));response="Review the supply details and save to confirm."}
           "how many " in c->{val term=c.substringAfter("how many ").substringBefore(" do i").trim();response="${records.filter {it.kind=="supply" && it.title.contains(term,true)}.sumOf {it.quantity}} $term recorded."}
           else->response="Try: add bottled water; what supplies are expiring soon; show my contacts; read my family plan; show my checklist."
          }}) {Text(androidx.compose.ui.res.stringResource(R.string.review_command_399b62))} }
          item {Text(response);TextButton(onClick={speak(response)}) {Text(androidx.compose.ui.res.stringResource(R.string.read_response_aloud_ac4ff0))} }
         }
         "Settings"->{
          item {Text(androidx.compose.ui.res.stringResource(R.string.private_by_default_f52e06),fontSize=26.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.no_account_ads_analytics_location_tracking_or_au_75a32c))}
          item {Row(verticalAlignment=Alignment.CenterVertically) {Switch(checked=dark,onCheckedChange={model.setDarkTheme(it)});Text(androidx.compose.ui.res.stringResource(R.string.dark_theme_3f11a5))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.interface_english_preview_other_languages_are_pe_364f22))}
          item {Button(onClick={if(android.os.Build.VERSION.SDK_INT>=33) notifications.launch(Manifest.permission.POST_NOTIFICATIONS) else status="Notifications follow device settings."}) {Text(androidx.compose.ui.res.stringResource(R.string.enable_optional_reminders_4af429))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.reminders_are_checked_periodically_usually_daily_226139))}
          item {Text(androidx.compose.ui.res.stringResource(R.string.exports_2f4dcf),fontSize=22.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.exports_are_unencrypted_review_the_included_info_14b601))}
          item {Row {listOf("pdf","csv","json").forEach {type->TextButton(onClick={exportType=type;exportConfirm=true}) {Text(type.uppercase())}}} }
          item {InfoCard("PrepTrove Premium","Purchases are unavailable in this development preview. Amazon catalogue setup, receipt verification and store testing are required before any purchase can be enabled.","View release notes") {status="See docs/RELEASE.md in the source package for release gates."} }
          item {OutlinedButton(onClick={wipe=true}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_all_saved_records_4e66b6))} }
         }
         else->{
          val kind=kinds[section]
          if(kind=="check") item {OutlinedButton(onClick={
           listOf("Power Outages","Severe Weather","Flood Preparation","Winter Readiness","Travel Preparation","Household Evacuation","Pet Preparedness","Emergency Document Organisation","Supply Inspection").forEach { group->
            listOf("Review official local guidance", "Discuss household needs and arrangements", "Review relevant supplies and contacts").forEach { task->if(records.none {it.kind=="check" && it.category==group && it.title==task}) model.save(Entry(kind="check",title=task,category=group)) }
           }
          }) {Text("Add starter checklist tasks")} }
          val visible=records.filter {it.kind==kind && (it.title.contains(search,true)||it.detail.contains(search,true))}
          item {Text(section,fontSize=28.sp,fontWeight=FontWeight.Bold);Text(when(kind) {"plan"->"Use notes for meeting points, communication, pets and accessibility arrangements. Sensitive details are optional.";"check"->"Each record is an editable checklist task. Group tasks with the category field.";"contact"->"Enter a phone number in notes. No device contacts permission is used.";else->"Keep the details that matter to your household."})}
          item {OutlinedTextField(search,{search=it},label={Text("Search $section")},modifier=Modifier.fillMaxWidth(),leadingIcon={Icon(Icons.Outlined.Search,null)})}
          if(visible.isEmpty()) item {InfoCard("Make room for peace of mind","Nothing saved here yet. Start with one small step.","Add your first record") {editing=Entry(kind=kind ?: "supply",title="")} }
          items(visible,key={it.id}) {e->Card(shape=RoundedCornerShape(18.dp)) {Column(Modifier.padding(20.dp)) {
           Row(verticalAlignment=Alignment.CenterVertically) {if(kind in listOf("check","reminder")) Checkbox(e.done,{model.save(e.copy(done=it,checked=LocalDate.now().toString()))});Text(e.title,fontSize=20.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));IconButton(onClick={editing=e}) {Icon(Icons.Outlined.Edit,"Edit ${e.title}")}}
           Text(e.detail);if(kind=="supply") Text("${e.quantity} ${e.unit} · ${e.category}")
           if(e.location.isNotEmpty()) Text("Stored: ${e.location}")
           if(e.kitId.isNotEmpty()) Text("Kit: ${records.find {it.id==e.kitId}?.title ?: "Unassigned"}")
           if(e.expiry.isNotEmpty()) Text("Date: ${e.expiry}",color=MaterialTheme.colorScheme.primary)
           if(e.checked.isNotEmpty()) Text("Last checked: ${e.checked}")
           Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick={model.save(e.copy(checked=LocalDate.now().toString()))}) {Text(androidx.compose.ui.res.stringResource(R.string.checked_today_d85745))}
            TextButton(onClick={model.save(e.copy(id=java.util.UUID.randomUUID().toString(),title=e.title+" (copy)"))}) {Text(androidx.compose.ui.res.stringResource(R.string.duplicate_02cdaa))}
            TextButton(onClick={deleting=e}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_e2d0a5))}
           }
          }} }
         }
        }
       }
      }
     }
    }
   }
   editing?.let {entry->EntryEditor(entry,records.filter{it.kind=="kit"},onClose={editing=null},onSave={model.save(it);editing=null})}
   deleting?.let {e->AlertDialog(onDismissRequest={deleting=null},title={Text("Delete ${e.title}?")},text={Text(androidx.compose.ui.res.stringResource(R.string.this_cannot_be_undone_supplies_in_a_deleted_kit__54a684))},confirmButton={TextButton(onClick={model.remove(e);deleting=null}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_e2d0a5))}},dismissButton={TextButton(onClick={deleting=null}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})}
   if(wipe) AlertDialog(onDismissRequest={wipe=false},title={Text(androidx.compose.ui.res.stringResource(R.string.delete_all_records_1dcbbc))},text={Text(androidx.compose.ui.res.stringResource(R.string.all_kits_supplies_contacts_plans_reminders_and_c_bbd353))},confirmButton={TextButton(onClick={model.clear();wipe=false}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_everything_71eb8a))}},dismissButton={TextButton(onClick={wipe=false}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
   if(exportConfirm) AlertDialog(onDismissRequest={exportConfirm=false},title={Text(androidx.compose.ui.res.stringResource(R.string.review_export_contents_521854))},text={Column {Text("$exportType export includes saved inventory, kit notes, checklists and reminders. Those notes may contain personal information. JSON is a data export; restore is not yet implemented.");Row(verticalAlignment=Alignment.CenterVertically) {Checkbox(sensitive,{sensitive=it});Text(androidx.compose.ui.res.stringResource(R.string.also_include_contacts_and_family_plans_331c8a))}}},confirmButton={TextButton(onClick={exportConfirm=false;export.launch("PrepTrove-${LocalDate.now()}.$exportType")}) {Text(androidx.compose.ui.res.stringResource(R.string.choose_destination_5853e2))}},dismissButton={TextButton(onClick={exportConfirm=false}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
   if(!onboarded) {var size by remember {mutableStateOf("1")};var region by remember {mutableStateOf("United Kingdom")};AlertDialog(onDismissRequest={model.onboard("","")},title={Text(androidx.compose.ui.res.stringResource(R.string.welcome_to_preptrove_2fb668))},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text(androidx.compose.ui.res.stringResource(R.string.prepared_today_ready_for_tomorrow_your_records_s_5cbed1));OutlinedTextField(size,{size=it.filter{c->c.isDigit()}.take(2)},label={Text(androidx.compose.ui.res.stringResource(R.string.household_size_optional_73a7cf))});OutlinedTextField(region,{region=it},label={Text(androidx.compose.ui.res.stringResource(R.string.country_or_region_8d2360))});Text(androidx.compose.ui.res.stringResource(R.string.reminders_and_microphone_access_are_optional_and_121a8c))}},confirmButton={TextButton(onClick={model.onboard(size,region)}) {Text(androidx.compose.ui.res.stringResource(R.string.start_planning_655059))}},dismissButton={TextButton(onClick={model.onboard("","")}) {Text(androidx.compose.ui.res.stringResource(R.string.skip_28d035))}})}
  }
 }
}
@Composable fun Metric(label:String,count:Int,modifier:Modifier) {Card(modifier) {Column(Modifier.padding(16.dp)) {Text(count.toString(),fontSize=30.sp,fontWeight=FontWeight.Bold);Text(label,fontSize=12.sp)}}}
@Composable fun InfoCard(title:String,body:String,action:String,onClick:()->Unit) {Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)) {Column(Modifier.padding(22.dp)) {Text(title,fontSize=20.sp,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Text(body);TextButton(onClick=onClick) {Text(action)}}}}

@Composable fun EntryEditor(initial:Entry,kits:List<Entry>,onClose:()->Unit,onSave:(Entry)->Unit) {
 var title by remember {mutableStateOf(initial.title)};var detail by remember {mutableStateOf(initial.detail)};var quantity by remember {mutableStateOf(initial.quantity.toString())};var unit by remember {mutableStateOf(initial.unit)};var date by remember {mutableStateOf(initial.expiry)};var location by remember {mutableStateOf(initial.location)};var category by remember {mutableStateOf(initial.category)};var kit by remember {mutableStateOf(initial.kitId)};var error by remember {mutableStateOf("")}
 AlertDialog(onDismissRequest=onClose,title={Text(if(initial.title.isEmpty()) "Add ${initial.kind}" else "Edit ${initial.kind}")},text={Column(Modifier.heightIn(max=460.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  OutlinedTextField(title,{title=it},label={Text(androidx.compose.ui.res.stringResource(R.string.name_dcd1d5))},singleLine=true)
  OutlinedTextField(detail,{detail=it},label={Text(if(initial.kind=="contact") "Phone number and notes" else "Notes / plan details")},minLines=3)
  OutlinedTextField(category,{category=it},label={Text(androidx.compose.ui.res.stringResource(R.string.category_checklist_group_0cf902))})
  if(initial.kind=="supply") {OutlinedTextField(quantity,{quantity=it},label={Text(androidx.compose.ui.res.stringResource(R.string.quantity_822bab))});OutlinedTextField(unit,{unit=it},label={Text(androidx.compose.ui.res.stringResource(R.string.unit_e_g_litres_bottles_3092cb))});Text(androidx.compose.ui.res.stringResource(R.string.assign_to_a_kit_ba9a85));Row(Modifier.horizontalScroll(rememberScrollState())) {FilterChip(selected=kit.isEmpty(),onClick={kit=""},label={Text(androidx.compose.ui.res.stringResource(R.string.unassigned_14d33b))});kits.forEach {k->FilterChip(selected=kit==k.id,onClick={kit=k.id},label={Text(k.title)})}}}
  if(initial.kind in listOf("supply","reminder")) OutlinedTextField(date,{date=it},label={Text("${if(initial.kind=="reminder") "Due" else "Expiry"} date: YYYY-MM-DD (optional)")})
  if(initial.kind in listOf("supply","kit")) OutlinedTextField(location,{location=it},label={Text(androidx.compose.ui.res.stringResource(R.string.storage_location_a59e28))})
  if(error.isNotEmpty()) Text(error,color=MaterialTheme.colorScheme.error)
 }},confirmButton={TextButton(onClick={val q=quantity.toDoubleOrNull();error=when {title.isBlank()->"Enter a name.";q==null||!q.isFinite()||q<0->"Enter a valid non-negative quantity.";date.isNotEmpty()&&runCatching {LocalDate.parse(date)}.isFailure->"Use a valid date in YYYY-MM-DD format.";else->""};if(error.isEmpty()) onSave(initial.copy(title=title.trim(),detail=detail,quantity=q!!,unit=unit,expiry=date,location=location,category=category,kitId=kit))}) {Text(androidx.compose.ui.res.stringResource(R.string.save_1509f5))}},dismissButton={TextButton(onClick=onClose) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
}
