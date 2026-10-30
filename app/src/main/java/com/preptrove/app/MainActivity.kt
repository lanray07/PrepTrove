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
import androidx.compose.ui.platform.testTag
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder

val Navy=Color(0xFF263B4D)
val Teal=Color(0xFF2C7A7B)
val Ivory=Color(0xFFF8F6F0)
val Amber=Color(0xFFD7A65C)
val sections=listOf("Overview","Kits","Supplies","Family plans","Contacts","Checklists","Reminders","Reference","Voice","Settings")
val kinds=mapOf("Kits" to "kit","Supplies" to "supply","Family plans" to "plan","Contacts" to "contact","Checklists" to "check","Reminders" to "reminder")

class MainActivity:ComponentActivity() {
 private fun screenLabel(value:String):String=when(value) {
  "Overview"->getString(R.string.nav_overview)
  "Kits"->getString(R.string.nav_kits)
  "Supplies"->getString(R.string.nav_supplies)
  "Family plans"->getString(R.string.nav_family_plans)
  "Contacts"->getString(R.string.nav_contacts)
  "Checklists"->getString(R.string.nav_checklists)
  "Reminders"->getString(R.string.nav_reminders)
  "Reference"->getString(R.string.nav_reference)
  "Voice"->getString(R.string.nav_voice)
  "Settings"->getString(R.string.nav_settings)
  else->value
 }
 private var lockEnabled by mutableStateOf(false)
 private var locked by mutableStateOf(false)
 private var enablingLock=false
 private val unlock=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
  if(result.resultCode==RESULT_OK) {
   if(enablingLock) {getSharedPreferences("privacy",MODE_PRIVATE).edit().putBoolean("lock",true).commit();lockEnabled=true}
   locked=false
  }
  enablingLock=false
 }
 private fun authenticate(enable:Boolean=false) {
  val manager=getSystemService(android.app.KeyguardManager::class.java)
  if(!manager.isDeviceSecure)return
  enablingLock=enable
  manager.createConfirmDeviceCredentialIntent(getString(R.string.lock_title),getString(R.string.lock_prompt))?.let {unlock.launch(it)}
 }
 override fun onStop() {super.onStop();tts?.stop();recognizer?.cancel();if(lockEnabled) locked=true}
 private var recognizer:SpeechRecognizer?=null
 private var tts:TextToSpeech?=null
 private var speechReady=false
 override fun attachBaseContext(base:android.content.Context) { super.attachBaseContext(AppLanguages.wrap(base)) }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  lockEnabled=getSharedPreferences("privacy",MODE_PRIVATE).getBoolean("lock",false);locked=lockEnabled
  tts=TextToSpeech(this) { status-> speechReady=status==TextToSpeech.SUCCESS && (tts?.setLanguage(resources.configuration.locales[0]) ?: -1)>=0 }
  (application as PrepTrove).billing.start()
  setContent {val plannerState=rememberSaveableStateHolder();LaunchedEffect(lockEnabled) {if(lockEnabled) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)};if(locked) MaterialTheme {Surface(Modifier.fillMaxSize()) {Column(Modifier.padding(32.dp),verticalArrangement=Arrangement.Center) {Text(getString(R.string.lock_title),fontSize=28.sp);Text(getString(R.string.lock_prompt));Button(onClick={authenticate()}) {Text(getString(R.string.lock_unlock))}}}} else plannerState.SaveableStateProvider("planner") {Planner()} }
 }
 override fun onResume() {super.onResume();(application as PrepTrove).billing.refresh()}
 override fun onDestroy() { recognizer?.destroy();tts?.stop();tts?.shutdown();super.onDestroy() }
 private fun speak(text:String) { if(speechReady) tts?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"response") }
 private fun listen(update:(String)->Unit,active:(Boolean)->Unit) {
  if(locked)return
  if(!SpeechRecognizer.isRecognitionAvailable(this)) { update(getString(R.string.msg_79f24e0d59e2));return }
  recognizer?.destroy();recognizer=SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object:RecognitionListener {
   override fun onReadyForSpeech(p:Bundle?) { active(true) }
   override fun onBeginningOfSpeech() {}
   override fun onRmsChanged(r:Float) {}
   override fun onBufferReceived(b:ByteArray?) {}
   override fun onEndOfSpeech() { active(false) }
   override fun onError(e:Int) {active(false);update(getString(R.string.msg_27a56819312e ,e))}
   override fun onResults(b:Bundle?) {active(false);update(b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())}
   override fun onPartialResults(b:Bundle?) {}
   override fun onEvent(t:Int,b:Bundle?) {}
  })
  try { recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,resources.configuration.locales[0].toLanguageTag())) } catch(e:Exception) { active(false);update(getString(R.string.msg_3449cda89b1a)) }
 }
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable fun Planner(model:PlannerModel=viewModel()) {
  val records by model.records.collectAsStateWithLifecycle()
  val reviews by model.reviews.collectAsStateWithLifecycle()
  val billing=(application as PrepTrove).billing
  val purchase by billing.state.collectAsStateWithLifecycle()
  val onboarded by model.onboarded.collectAsStateWithLifecycle()
  var chooseLanguage by remember { mutableStateOf(false) }
  var section by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("Overview") }
  var editing by rememberSaveable(stateSaver=EntryStateSaver) { mutableStateOf<Entry?>(null) }
  var deleting by remember { mutableStateOf<Entry?>(null) }
  var wipe by remember { mutableStateOf(false) }
  var search by rememberSaveable { mutableStateOf("") }
  val dark by model.darkTheme.collectAsStateWithLifecycle()
  var command by rememberSaveable { mutableStateOf("") }
  var response by remember { mutableStateOf("") }
  var listening by remember { mutableStateOf(false) }
  var exportType by rememberSaveable { mutableStateOf("") }
  var previewReport by rememberSaveable {mutableStateOf(false)}
  var exportConfirm by remember { mutableStateOf(false) }
  var backupPassword by model.backupPassword
  var importing by remember { mutableStateOf(false) }
  var importEncrypted by rememberSaveable { mutableStateOf(false) }
  var pendingImport by remember { mutableStateOf<List<Entry>?>(null) }
  val scope=rememberCoroutineScope()
  var includeNotes by rememberSaveable {mutableStateOf(true)}
  var includeLocations by rememberSaveable {mutableStateOf(false)}
  var includePhones by rememberSaveable {mutableStateOf(false)}
  var includeMembers by rememberSaveable {mutableStateOf(false)}
  var sensitive by rememberSaveable { mutableStateOf(false) }
  var status by rememberSaveable { mutableStateOf("") }
  val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted->if(granted) listen({command=it},{listening=it}) else {response=getString(R.string.msg_16cfda4cd82d)} }
  val notifications=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { status=if(it) getString(R.string.msg_4bd4c7b77408) else getString(R.string.msg_52df1e1e5c92) }
  val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri->
   if(uri!=null) scope.launch {
    status=getString(R.string.msg_e72fbb36b74a)
    val password=backupPassword.toCharArray();backupPassword=""
    try {
     val restored=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      val bytes=contentResolver.openInputStream(uri)?.use {it.readBytesLimited(BackupCrypto.MAX_BYTES)} ?: error(getString(R.string.msg_ca8f3e592a42))
      BackupCodec.decodeCopies(if(importEncrypted) BackupCrypto.decrypt(bytes,password) else bytes)
     }
     pendingImport=restored;status=getString(R.string.msg_8c5bd585f189)
    } catch(e:Exception) {status=getString(R.string.msg_6cf8046d34d7)} finally {password.fill('\u0000')}
   }
  }
  val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri->
   if(uri==null) backupPassword="" else scope.launch {
    val format=exportType
    val options=ExportPolicy.Options(sensitive,includeNotes,includeLocations,includePhones,includeMembers)
    val password=backupPassword.toCharArray();backupPassword=""
    try {
     kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      require(format in listOf("csv","json","ptb","pdf"))
      val chosen=ExportPolicy.select(model.snapshot(),options)
      contentResolver.openOutputStream(uri)?.use {stream->when(format) {
       "csv"->stream.write(Inventory.csv(chosen).toByteArray())
       "json"->stream.write(BackupCodec.encode(chosen))
       "ptb"->stream.write(BackupCrypto.encrypt(BackupCodec.encode(chosen),password))
       "pdf"->PdfReports.write(this@MainActivity,chosen,stream)
      }} ?: error("Destination unavailable")
     }
     status=getString(R.string.msg_3b92ecba1a57)
    } catch(e:Exception) {status=getString(R.string.msg_e82ead9f0716)} finally {password.fill('\u0000')}
   }
  }
  MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=Color(0xFF83CFCA),background=Color(0xFF152632),surface=Color(0xFF203746)) else lightColorScheme(primary=Teal,background=Ivory,surface=Color.White,onSurface=Navy)) {
   Surface(Modifier.fillMaxSize()) {
    BoxWithConstraints {
     val wide=maxWidth>=840.dp
     Row {
      if(wide) Column(Modifier.width(220.dp).fillMaxHeight().background(Navy).padding(20.dp)) {
       Text(androidx.compose.ui.res.stringResource(R.string.preptrove_c7187b),color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.your_household_organised_96de4e),color=Amber,fontSize=10.sp)
       Spacer(Modifier.height(32.dp))
       sections.forEach { s->TextButton(onClick={section=s;search=""},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),colors=ButtonDefaults.textButtonColors(contentColor=if(section==s) Amber else Color.White)) {Text(screenLabel(s),modifier=Modifier.fillMaxWidth())} }
      }
      Scaffold(modifier=Modifier.weight(1f),topBar={TopAppBar(title={Column {Text(if(wide) screenLabel(section) else "PrepTrove",fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.prepared_today_ready_for_tomorrow_6786a9),fontSize=12.sp)}},actions={IconButton(onClick={section="Voice"}) {Icon(Icons.Outlined.Mic,getString(R.string.msg_290b79b6acea))}})},floatingActionButton={if(section in kinds) FloatingActionButton(onClick={editing=Entry(kind=kinds.getValue(section),title="")}) {Icon(Icons.Outlined.Add,getString(R.string.msg_950eea0391cc))}},bottomBar={if(!wide) ScrollableTabRow(selectedTabIndex=sections.indexOf(section),edgePadding=0.dp) {sections.forEach {s->Tab(selected=section==s,onClick={section=s;search=""},text={Text(screenLabel(s))})}}}) { padding->
       LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal=if(wide) 32.dp else 20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(top=16.dp,bottom=100.dp)) {
        item { Text(androidx.compose.ui.res.stringResource(R.string.saved_records_work_offline_eceb81),color=MaterialTheme.colorScheme.primary,fontSize=12.sp) }
        if(status.isNotEmpty()) item { Text(status) }
        when(section) {
         "Overview"->{
          item {Card(colors=CardDefaults.cardColors(containerColor=Navy),shape=RoundedCornerShape(24.dp)) {Column(Modifier.padding(28.dp)) {Text(androidx.compose.ui.res.stringResource(R.string.a_little_preparation_more_peace_of_mind_48526d),color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));Text(androidx.compose.ui.res.stringResource(R.string.your_essentials_and_household_plans_together_in__354383),color=Color(0xFFCDDCD9));Spacer(Modifier.height(16.dp));Button(onClick={section="Kits"},colors=ButtonDefaults.buttonColors(containerColor=Amber,contentColor=Navy)) {Text(androidx.compose.ui.res.stringResource(R.string.organise_your_kit_e4829c))}}} }
          item {Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {Metric(screenLabel("Kits"),records.count{it.kind=="kit" && !it.isTemplate},Modifier.weight(1f));Metric(screenLabel("Supplies"),records.count{it.kind=="supply" && !it.isTemplate},Modifier.weight(1f));Metric(getString(R.string.msg_9a9143036dec),Inventory.attention(records).size,Modifier.weight(1f))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.your_next_small_step_b68082),fontSize=22.sp,fontWeight=FontWeight.SemiBold)}
          item {InfoCard(getString(R.string.msg_d2dbe2559ba6),getString(R.string.msg_6079021c3755),getString(R.string.msg_ed39c4413dda)) {section="Family plans"} }
          item {InfoCard(getString(R.string.msg_46e7e7653a2f),resources.getQuantityString(R.plurals.supplies_attention,Inventory.attention(records).size,Inventory.attention(records).size),getString(R.string.msg_20881999e49c)) {section="Supplies"} }
          item {InfoCard(getString(R.string.msg_29bad2d89383),getString(R.string.msg_e0d1c3905d20),getString(R.string.msg_e2f452853253)) {section="Contacts"} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.planning_progress_is_not_a_guarantee_of_safety_f_26ae34),fontSize=12.sp)}
         }
         "Reference"->{
          item {Text(androidx.compose.ui.res.stringResource(R.string.saved_guidance_united_kingdom_8a9326),fontSize=24.sp,fontWeight=FontWeight.Bold)}
          item {Text(androidx.compose.ui.res.stringResource(R.string.general_planning_information_not_live_alerts_for_127afe))}
          item {InfoCard(getString(R.string.msg_763918ef854e),getString(R.string.msg_64807d95473b),getString(R.string.msg_54c27d3dfa89)) {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://prepare.campaign.gov.uk/get-prepared-for-emergencies/")))} }
          item {InfoCard(getString(R.string.msg_1a856e66963c),getString(R.string.msg_78d63721f142),getString(R.string.msg_d879aface73a)) {section="Family plans"} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.source_uk_government_prepare_https_prepare_campa_17bb7e),fontSize=12.sp)}
         }
         "Voice"->{
          item {Text(androidx.compose.ui.res.stringResource(R.string.your_voice_your_planner_4e42c9),fontSize=26.sp,fontWeight=FontWeight.Bold)}
          item {Text(androidx.compose.ui.res.stringResource(R.string.push_to_talk_then_review_the_transcript_your_dev_afdb27))}
          item {Button(onClick={if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) listen({command=it},{listening=it}) else permission.launch(Manifest.permission.RECORD_AUDIO)}) {Icon(Icons.Outlined.Mic,null);Text(if(listening) getString(R.string.msg_bbb4106e8144) else getString(R.string.msg_4c15103c8011))} }
          item {OutlinedTextField(value=command,onValueChange={command=it},label={Text(androidx.compose.ui.res.stringResource(R.string.transcript_or_typed_command_48008f))},modifier=Modifier.fillMaxWidth())}
          item {Button(onClick={val c=command.lowercase(Locale.ROOT);val reminderDraft=VoiceDrafts.reminder(command);val inspection=VoiceDrafts.checkedSupply(command,records);when {
           reminderDraft!=null->{editing=reminderDraft;response=getString(R.string.voice_reminder_review)}
           inspection!=null->{if(inspection.size==1) {model.complete(inspection.single());response=getString(R.string.voice_checked)} else response=getString(R.string.voice_exact_supply)}
           "expiring" in c->{response=getString(R.string.msg_99d34c2615d3 ,Inventory.attention(records).size)}
           "contact" in c->{section="Contacts";response=getString(R.string.msg_ed7e19e138cd)}
           "checklist" in c->{section="Checklists";response=getString(R.string.msg_a66277f71e95)}
           "read" in c && "plan" in c->{response=records.filter {it.kind=="plan" && !it.isTemplate}.joinToString(". ") {it.title+". "+it.detail+". "+PdfReports.structuredLines(this@MainActivity,it).joinToString(". ")}.ifEmpty {getString(R.string.msg_7ac9410ea796)}}
           c.startsWith("add ")->{editing=Entry(kind="supply",title=command.drop(4).substringBefore(" to my "));response=getString(R.string.msg_186c6ec83f48)}
           VoiceDrafts.quantity(command,records)!=null->{val quantity=VoiceDrafts.quantity(command,records)!!;response=if(quantity.combinable) getString(R.string.voice_quantity,java.text.NumberFormat.getNumberInstance(resources.configuration.locales[0]).format(quantity.total),quantity.unit,quantity.phrase) else getString(R.string.voice_quantity_units)}
           else->response=getString(R.string.msg_475a159ff59c)
          }}) {Text(androidx.compose.ui.res.stringResource(R.string.review_command_399b62))} }
          item {Text(response);TextButton(onClick={speak(response)}) {Text(androidx.compose.ui.res.stringResource(R.string.read_response_aloud_ac4ff0))} }
         }
         "Settings"->{
          item {OutlinedButton(onClick={if(lockEnabled) {getSharedPreferences("privacy",MODE_PRIVATE).edit().putBoolean("lock",false).commit();lockEnabled=false} else if(getSystemService(android.app.KeyguardManager::class.java).isDeviceSecure) authenticate(true) else status=getString(R.string.lock_unavailable)}) {Text(getString(if(lockEnabled) R.string.lock_disable else R.string.lock_enable))} }
          item {TextButton(onClick={runCatching {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/lanray07/PrepTrove/blob/main/docs/PRIVACY.md")))}.onFailure {status=getString(R.string.support_browser_missing)}}) {Text(getString(R.string.support_privacy))};TextButton(onClick={runCatching {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/lanray07/PrepTrove/blob/main/docs/DATA-DELETION.md")))}.onFailure {status=getString(R.string.support_browser_missing)}}) {Text(getString(R.string.support_deletion))} }
          item {OutlinedButton(onClick={chooseLanguage=true}) {Text(getString(R.string.language_label))} }

          item {Text(androidx.compose.ui.res.stringResource(R.string.private_by_default_f52e06),fontSize=26.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.no_account_ads_analytics_location_tracking_or_au_75a32c))}
          item {Row(verticalAlignment=Alignment.CenterVertically) {Switch(checked=dark,onCheckedChange={model.setDarkTheme(it)});Text(androidx.compose.ui.res.stringResource(R.string.dark_theme_3f11a5))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.interface_english_preview_other_languages_are_pe_364f22))}
          item {Button(onClick={if(android.os.Build.VERSION.SDK_INT>=33) notifications.launch(Manifest.permission.POST_NOTIFICATIONS) else status=getString(R.string.msg_182831393f70)}) {Text(androidx.compose.ui.res.stringResource(R.string.enable_optional_reminders_4af429))} }
          item {Text(androidx.compose.ui.res.stringResource(R.string.reminders_are_checked_periodically_usually_daily_226139))}
          item {Text(androidx.compose.ui.res.stringResource(R.string.exports_2f4dcf),fontSize=22.sp,fontWeight=FontWeight.Bold);Text(androidx.compose.ui.res.stringResource(R.string.exports_are_unencrypted_review_the_included_info_14b601))}
          item {Row {listOf("pdf","csv","json","ptb").forEach {type->TextButton(onClick={exportType=type;exportConfirm=true;backupPassword=""}) {Text(type.uppercase())}}} }
          item {OutlinedButton(onClick={importing=true;backupPassword=""}) {Text(getString(R.string.msg_96276c8b3a4b))} }
          item {Card(Modifier.fillMaxWidth()) {Column(Modifier.padding(20.dp)) {
           Text(getString(R.string.billing_title),fontSize=22.sp)
           Text(getString(purchase.message))
           if(purchase.premium) Text(getString(R.string.billing_premium_active))
           if(purchase.price.isNotEmpty()) Text(getString(R.string.billing_price,purchase.price))
           Row {Button(enabled=purchase.canPurchase && !purchase.busy && !purchase.premium,onClick={billing.purchase()}) {Text(getString(R.string.billing_buy))};TextButton(enabled=!purchase.busy,onClick={billing.restore()}) {Text(getString(R.string.billing_restore))}}
          }} }
          item {OutlinedButton(onClick={wipe=true}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_all_saved_records_4e66b6))} }
         }
         else->{
          val kind=kinds[section]
          if(kind=="check") item {OutlinedButton(onClick={
           listOf(getString(R.string.msg_dab9ab319759),getString(R.string.msg_bd48c42d9275),getString(R.string.msg_e53d1915f2aa),getString(R.string.msg_0c32b624fed5),getString(R.string.msg_c1f91d2d0d07),getString(R.string.msg_445cbef5c132),getString(R.string.msg_45474e80fd18),getString(R.string.msg_1503c09ae775),getString(R.string.msg_c22e78a7a25a)).forEach { group->
            listOf(getString(R.string.msg_9894105732a6), getString(R.string.msg_ceda0ef368ce), getString(R.string.msg_5d8d8b9627ca)).forEach { task->if(records.none {it.kind=="check" && it.category==group && it.title==task}) model.save(Entry(kind="check",title=task,category=group)) }
           }
          }) {Text(getString(R.string.msg_b836676c3c6d))} }
          val visible=records.filter {!it.isTemplate && it.kind==kind && (it.title.contains(search,true)||it.detail.contains(search,true))}
          item {Text(screenLabel(section),fontSize=28.sp,fontWeight=FontWeight.Bold);Text(when(kind) {"plan"->getString(R.string.msg_4db74eab28d5);"check"->getString(R.string.msg_0d4f4e9b3444);"contact"->getString(R.string.msg_a36a7fd80438);else->getString(R.string.msg_ffacb0664095)})}
          item {OutlinedTextField(search,{search=it},label={Text(getString(R.string.msg_7d76ac75b53e ,screenLabel(section)))},modifier=Modifier.fillMaxWidth(),leadingIcon={Icon(Icons.Outlined.Search,null)})}
          items(records.filter {it.kind==kind && it.isTemplate && it.kitId.isEmpty()},key={"template-"+it.id}) {template->Card {Column(Modifier.padding(16.dp)) {Text(getString(R.string.template_label,template.title));Row {TextButton(onClick={model.useTemplate(template)}) {Text(getString(R.string.template_use))};TextButton(onClick={deleting=template}) {Text(getString(R.string.delete_e2d0a5))}}}}}
          if(section=="Reminders") item {Text(getString(R.string.review_history));reviews.take(20).forEach {Text(it.title+" · "+displayDate(it.completedOn))}}
          if(visible.isEmpty()) item {InfoCard(getString(R.string.msg_357221fd3aac),getString(R.string.msg_ac8bbdb90299),getString(R.string.msg_f841d822b35e)) {editing=Entry(kind=kind ?: "supply",title="")} }
          items(visible,key={it.id}) {e->Card(shape=RoundedCornerShape(18.dp)) {Column(Modifier.padding(20.dp)) {
           Row(verticalAlignment=Alignment.CenterVertically) {if(kind in listOf("check","reminder")) Checkbox(e.done,{if(it) model.complete(e) else model.save(e.copy(done=false))});Text(e.title,fontSize=20.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));IconButton(onClick={editing=e}) {Icon(Icons.Outlined.Edit,getString(R.string.msg_261e9c17ca74 ,e.title))}}
           Text(e.detail);if(kind=="supply") Text("${java.text.NumberFormat.getNumberInstance(resources.configuration.locales[0]).format(e.quantity)} ${e.unit} · ${e.category}")
           if(e.kind=="plan" || e.kind=="contact") Text(StructuredFields.summary(e.extraData))
           if(e.purchasedDate.isNotEmpty()) Text(getString(R.string.purchase_date_value,displayDate(e.purchasedDate)))
           if(e.location.isNotEmpty()) Text(getString(R.string.msg_b7b25d4790e6 ,e.location))
           if(e.kitId.isNotEmpty()) Text(getString(R.string.msg_b23a0abbda63 ,records.find {it.id==e.kitId}?.title ?: getString(R.string.unassigned_14d33b)))
           if(e.expiry.isNotEmpty()) Text(getString(R.string.msg_f55bccec7713 ,displayDate(e.expiry)),color=MaterialTheme.colorScheme.primary)
           if(e.checked.isNotEmpty()) Text(getString(R.string.msg_7273969a69f6 ,displayDate(e.checked)))
           Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick={model.complete(e)}) {Text(androidx.compose.ui.res.stringResource(R.string.checked_today_d85745))}
            TextButton(onClick={model.save(e.copy(id=java.util.UUID.randomUUID().toString(),title=(e.title+getString(R.string.msg_d75612450689)).take(500)))}) {Text(androidx.compose.ui.res.stringResource(R.string.duplicate_02cdaa))}
            TextButton(onClick={model.makeTemplate(e)}) {Text(getString(R.string.template_save))}
            if(e.kind=="reminder") TextButton(onClick={model.save(e.copy(done=!e.done))}) {Text(getString(if(e.done) R.string.reminder_resume else R.string.reminder_pause))}
            if(e.kind=="reminder") TextButton(onClick={model.save(e.copy(expiry=LocalDate.now().plusDays(7).toString(),done=false))}) {Text(getString(R.string.reminder_snooze))}
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
   if(chooseLanguage) AlertDialog(onDismissRequest={chooseLanguage=false},title={Text(getString(R.string.language_label))},text={Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState())) {
    Text(getString(R.string.language_review_notice))
    TextButton(onClick={AppLanguages.select(this@MainActivity,"");recreate()}) {Text(getString(R.string.language_system))}
    AppLanguages.targets.forEach {tag->val available=tag in AppLanguages.available;TextButton(enabled=available,onClick={AppLanguages.select(this@MainActivity,tag);recreate()}) {Text(java.util.Locale.forLanguageTag(tag).getDisplayName(java.util.Locale.forLanguageTag(tag)) + if(available) "" else " · "+getString(R.string.language_pending))}}
   }},confirmButton={TextButton(onClick={chooseLanguage=false}) {Text(getString(R.string.language_close))}})
   editing?.let {entry->EntryEditor(entry,records.filter{it.kind=="kit" && !it.isTemplate},onClose={editing=null},onSave={model.save(it);editing=null})}
   deleting?.let {e->AlertDialog(onDismissRequest={deleting=null},title={Text(getString(R.string.msg_6474596f1237 ,e.title))},text={Text(androidx.compose.ui.res.stringResource(R.string.this_cannot_be_undone_supplies_in_a_deleted_kit__54a684))},confirmButton={TextButton(onClick={model.remove(e);deleting=null}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_e2d0a5))}},dismissButton={TextButton(onClick={deleting=null}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})}
   if(wipe) AlertDialog(onDismissRequest={wipe=false},title={Text(androidx.compose.ui.res.stringResource(R.string.delete_all_records_1dcbbc))},text={Text(androidx.compose.ui.res.stringResource(R.string.all_kits_supplies_contacts_plans_reminders_and_c_bbd353))},confirmButton={TextButton(onClick={model.clear();wipe=false}) {Text(androidx.compose.ui.res.stringResource(R.string.delete_everything_71eb8a))}},dismissButton={TextButton(onClick={wipe=false}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
   if(importing) AlertDialog(onDismissRequest={importing=false;backupPassword=""},title={Text(getString(R.string.msg_96276c8b3a4b))},text={Column {Text(getString(R.string.msg_591249e28bc1));Row(verticalAlignment=Alignment.CenterVertically) {Checkbox(importEncrypted,{importEncrypted=it});Text(getString(R.string.msg_f6a927e954b2))};if(importEncrypted) OutlinedTextField(backupPassword,{backupPassword=it},label={Text(getString(R.string.msg_3e39703b6e87))},visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation())}},confirmButton={TextButton(onClick={importing=false;restore.launch(arrayOf("*/*"))}) {Text(getString(R.string.msg_b09c73d9d3f8))}},dismissButton={TextButton(onClick={importing=false;backupPassword=""}) {Text(getString(R.string.msg_19766ed6ccb2))}})
   pendingImport?.let {entries->AlertDialog(onDismissRequest={pendingImport=null},title={Text(getString(R.string.msg_3e69551e06f4 ,entries.size))},text={Text(getString(R.string.msg_4f1b416367bf))},confirmButton={TextButton(onClick={scope.launch {try {model.importCopies(entries);status=getString(R.string.msg_b749a0e44f52 ,entries.size)} catch(e:Exception) {status=getString(R.string.msg_a6ed8cd2316c)}};pendingImport=null}) {Text(getString(R.string.msg_f539a84db776))}},dismissButton={TextButton(onClick={pendingImport=null}) {Text(getString(R.string.msg_19766ed6ccb2))}})}
   if(previewReport) ReportPreview(ExportPolicy.select(records,ExportPolicy.Options(sensitive,includeNotes,includeLocations,includePhones,includeMembers)),{previewReport=false})
   if(exportConfirm) AlertDialog(onDismissRequest={exportConfirm=false;backupPassword=""},title={Text(androidx.compose.ui.res.stringResource(R.string.review_export_contents_521854))},text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState())) {Text(getString(R.string.msg_6793524957eb ,exportType));if(exportType=="ptb") OutlinedTextField(backupPassword,{backupPassword=it},label={Text(getString(R.string.msg_03cf154b18e2))},visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation());ExportFieldOptions(includeNotes,{includeNotes=it},includeLocations,{includeLocations=it},includePhones,{includePhones=it},includeMembers,{includeMembers=it});Row(verticalAlignment=Alignment.CenterVertically) {Checkbox(sensitive,{sensitive=it},modifier=Modifier.testTag("export-sensitive"));Text(androidx.compose.ui.res.stringResource(R.string.also_include_contacts_and_family_plans_331c8a))};if(exportType=="pdf") TextButton(onClick={previewReport=true}) {Text(getString(R.string.report_preview))}}},confirmButton={TextButton(enabled=exportType!="ptb" || backupPassword.length>=12,onClick={exportConfirm=false;export.launch(getString(R.string.msg_dd73784cf848 ,LocalDate.now(),exportType))}) {Text(androidx.compose.ui.res.stringResource(R.string.choose_destination_5853e2))}},dismissButton={TextButton(onClick={exportConfirm=false;backupPassword=""}) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
   if(!onboarded) {var size by remember {mutableStateOf("1")};var region by remember {mutableStateOf(resources.configuration.locales[0].displayCountry)};AlertDialog(onDismissRequest={model.onboard("","")},title={Text(androidx.compose.ui.res.stringResource(R.string.welcome_to_preptrove_2fb668))},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Text(androidx.compose.ui.res.stringResource(R.string.prepared_today_ready_for_tomorrow_your_records_s_5cbed1));OutlinedTextField(size,{size=it.filter{c->c.isDigit()}.take(2)},label={Text(androidx.compose.ui.res.stringResource(R.string.household_size_optional_73a7cf))});OutlinedTextField(region,{region=it},label={Text(androidx.compose.ui.res.stringResource(R.string.country_or_region_8d2360))});Text(androidx.compose.ui.res.stringResource(R.string.reminders_and_microphone_access_are_optional_and_121a8c))}},confirmButton={TextButton(onClick={model.onboard(size,region)}) {Text(androidx.compose.ui.res.stringResource(R.string.start_planning_655059))}},dismissButton={TextButton(onClick={model.onboard("","")}) {Text(androidx.compose.ui.res.stringResource(R.string.skip_28d035))}})}
  }
 }
}
@Composable fun Metric(label:String,count:Int,modifier:Modifier) {Card(modifier) {Column(Modifier.padding(16.dp)) {Text(java.text.NumberFormat.getIntegerInstance(androidx.compose.ui.platform.LocalConfiguration.current.locales[0]).format(count),fontSize=30.sp,fontWeight=FontWeight.Bold);Text(label,fontSize=12.sp)}}}
@Composable fun InfoCard(title:String,body:String,action:String,onClick:()->Unit) {Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)) {Column(Modifier.padding(22.dp)) {Text(title,fontSize=20.sp,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Text(body);TextButton(onClick=onClick) {Text(action)}}}}

@Composable fun EntryEditor(initial:Entry,kits:List<Entry>,onClose:()->Unit,onSave:(Entry)->Unit) {
 val editorContext=androidx.compose.ui.platform.LocalContext.current
 fun getString(id:Int,vararg args:Any):String=editorContext.getString(id,*args)
 var title by rememberSaveable(initial.id) {mutableStateOf(initial.title)};var detail by rememberSaveable(initial.id) {mutableStateOf(initial.detail)};var quantity by rememberSaveable(initial.id) {mutableStateOf(LocalNumbers.input(initial.quantity,editorContext.resources.configuration.locales[0]))};var unit by rememberSaveable(initial.id) {mutableStateOf(initial.unit)};var date by rememberSaveable(initial.id) {mutableStateOf(initial.expiry)};var location by rememberSaveable(initial.id) {mutableStateOf(initial.location)};var category by rememberSaveable(initial.id) {mutableStateOf(initial.category)};var kit by rememberSaveable(initial.id) {mutableStateOf(initial.kitId)};var error by rememberSaveable(initial.id) {mutableStateOf("")};var extraData by rememberSaveable(initial.id) {mutableStateOf(initial.extraData)};var purchased by rememberSaveable(initial.id) {mutableStateOf(initial.purchasedDate)};var interval by rememberSaveable(initial.id) {mutableStateOf(initial.intervalDays.toString())}
 AlertDialog(onDismissRequest=onClose,title={Text(if(initial.title.isEmpty()) getString(R.string.msg_df4fe91a7edb ,kindLabel(editorContext,initial.kind)) else getString(R.string.msg_261e9c17ca74 ,kindLabel(editorContext,initial.kind)))},text={Column(Modifier.heightIn(max=460.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  OutlinedTextField(title,{title=it},label={Text(androidx.compose.ui.res.stringResource(R.string.name_dcd1d5))},singleLine=true)
  OutlinedTextField(detail,{detail=it},label={Text(if(initial.kind=="contact") getString(R.string.msg_fe367046458e) else getString(R.string.msg_774de0ec6b19))},minLines=3)
  if(initial.kind=="supply") PhotoEditor(extraData,{extraData=it})
  if(initial.kind=="plan" || initial.kind=="contact") StructuredEditor(initial.kind,extraData,{extraData=it})
  if(initial.kind=="supply") OutlinedTextField(purchased,{purchased=it},label={Text(getString(R.string.purchase_date_label))})
  if(initial.kind=="reminder") OutlinedTextField(interval,{interval=it},label={Text(getString(R.string.reminder_interval))})
  OutlinedTextField(category,{category=it},label={Text(androidx.compose.ui.res.stringResource(R.string.category_checklist_group_0cf902))})
  if(initial.kind=="supply") {OutlinedTextField(quantity,{quantity=it},label={Text(androidx.compose.ui.res.stringResource(R.string.quantity_822bab))});OutlinedTextField(unit,{unit=it},label={Text(androidx.compose.ui.res.stringResource(R.string.unit_e_g_litres_bottles_3092cb))});Text(androidx.compose.ui.res.stringResource(R.string.assign_to_a_kit_ba9a85));Row(Modifier.horizontalScroll(rememberScrollState())) {FilterChip(selected=kit.isEmpty(),onClick={kit=""},label={Text(androidx.compose.ui.res.stringResource(R.string.unassigned_14d33b))});kits.forEach {k->FilterChip(selected=kit==k.id,onClick={kit=k.id},label={Text(k.title)})}}}
  if(initial.kind in listOf("supply","reminder")) OutlinedTextField(date,{date=it},label={Text(getString(R.string.msg_5c542b734931 ,if(initial.kind=="reminder") getString(R.string.date_due) else getString(R.string.date_expiry)))})
  if(initial.kind in listOf("supply","kit")) OutlinedTextField(location,{location=it},label={Text(androidx.compose.ui.res.stringResource(R.string.storage_location_a59e28))})
  if(error.isNotEmpty()) Text(error,color=MaterialTheme.colorScheme.error)
 }},confirmButton={TextButton(onClick={val q=LocalNumbers.parse(quantity,editorContext.resources.configuration.locales[0]);error=when {title.isBlank()->getString(R.string.msg_099f9079ba79);title.length>500 || detail.length>20000 || extraData.length>20000 || category.length>20000 || location.length>20000 || unit.length>20000->getString(R.string.record_too_long);q==null||!q.isFinite()||q<0->getString(R.string.msg_d81b36d718f3);purchased.isNotEmpty()&&runCatching {LocalDate.parse(purchased)}.isFailure->getString(R.string.msg_032613d09381);interval.toIntOrNull()==null || interval.toInt() !in 0..3650->getString(R.string.reminder_interval_error);date.isNotEmpty()&&runCatching {LocalDate.parse(date)}.isFailure->getString(R.string.msg_032613d09381);else->""};if(error.isEmpty()) onSave(initial.copy(title=title.trim(),detail=detail,quantity=q!!,unit=unit,expiry=date,location=location,category=category,kitId=kit,extraData=extraData,purchasedDate=purchased,intervalDays=interval.toInt()))}) {Text(androidx.compose.ui.res.stringResource(R.string.save_1509f5))}},dismissButton={TextButton(onClick=onClose) {Text(androidx.compose.ui.res.stringResource(R.string.cancel_19766e))}})
}

private fun java.io.InputStream.readBytesLimited(limit:Int):ByteArray {
 val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
 while(true) {val count=read(buffer);if(count<0) break;require(out.size()+count<=limit);out.write(buffer,0,count)}
 return out.toByteArray()
}

private fun android.content.Context.displayDate(value:String):String=runCatching {
 java.time.LocalDate.parse(value).format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0]))
}.getOrDefault(value)
