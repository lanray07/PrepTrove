package com.preptrove.app

import android.content.Context
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Real store adapter. No test purchase success or developer secret is embedded. */
class AmazonBilling(private val context:Context):PurchasingListener {
 private val prefs=context.getSharedPreferences("amazon-entitlement",Context.MODE_PRIVATE)
 private val mutable=MutableStateFlow(BillingState(premium=prefs.getBoolean("premium",false)))
 val state=mutable.asStateFlow()
 private var user:String?=null
 private var started=false
 private var restoring=false
 private val restoreReceipts=mutableListOf<StoreReceipt>()
 private val sku get()=BuildConfig.AMAZON_SKU
 fun start() {
  if(started) return
  if(sku.isBlank() || !runCatching {context.assets.open("AppstoreAuthenticationKey.pem").close()}.isSuccess) {mutable.value=BillingState(message=R.string.billing_configuration);return}
  try {PurchasingService.registerListener(context,this);started=true;refresh()} catch(e:Exception) {fail()}
 }
 fun refresh() {if(!started)return;runCatching {PurchasingService.getUserData()}.onFailure {fail()}}
 fun purchase() {if(!mutable.value.canPurchase || mutable.value.busy)return;mutable.value=mutable.value.copy(busy=true);runCatching {PurchasingService.purchase(sku)}.onFailure {fail()}}
 fun restore() {if(!started || user==null || restoring)return;restoring=true;restoreReceipts.clear();mutable.value=mutable.value.copy(busy=true);runCatching {PurchasingService.getPurchaseUpdates(true)}.onFailure {restoring=false;fail()}}
 override fun onUserDataResponse(response:UserDataResponse) {
  if(response.requestStatus!=UserDataResponse.RequestStatus.SUCCESSFUL) {user=null;mutable.value=mutable.value.copy(canPurchase=false,busy=false,message=R.string.billing_unavailable);return}
  val id=response.userData.userId
  val changed=user!=id;user=id
  mutable.value=mutable.value.copy(premium=prefs.getString("user",null)==id && prefs.getBoolean("premium",false))
  PurchasingService.getProductData(setOf(sku))
  if(changed) restore()
 }
 override fun onProductDataResponse(response:ProductDataResponse) {
  val product=if(response.requestStatus==ProductDataResponse.RequestStatus.SUCCESSFUL) response.productData[sku] else null
  val valid=product?.productType==ProductType.ENTITLED
  mutable.value=mutable.value.copy(price=if(valid) product!!.price else "",canPurchase=valid && user!=null && BuildConfig.AMAZON_PURCHASES_ENABLED,busy=restoring,message=if(valid) R.string.billing_ready else R.string.billing_unavailable)
 }
 override fun onPurchaseResponse(response:PurchaseResponse) {
  mutable.value=mutable.value.copy(busy=false)
  if(response.requestStatus!=PurchaseResponse.RequestStatus.SUCCESSFUL) {mutable.value=mutable.value.copy(message=R.string.billing_not_completed);return}
  if(response.userData.userId!=user) {refresh();return}
  val receipt=response.receipt
  val result=EntitlementPolicy.accept(user,response.userData.userId,sku,StoreReceipt(receipt.sku,receipt.receiptId,receipt.isCanceled,receipt.productType==ProductType.ENTITLED))
  if(result==null) {fail();return}
  if(persist(result)) {if(result) PurchasingService.notifyFulfillment(receipt.receiptId,FulfillmentResult.FULFILLED)}
 }
 override fun onPurchaseUpdatesResponse(response:PurchaseUpdatesResponse) {
  if(response.requestStatus!=PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL || response.userData.userId!=user) {restoring=false;fail();return}
  restoreReceipts.addAll(response.receipts.map {StoreReceipt(it.sku,it.receiptId,it.isCanceled,it.productType==ProductType.ENTITLED)})
  if(response.hasMore()) {PurchasingService.getPurchaseUpdates(false);return}
  val entitled=EntitlementPolicy.restore(sku,restoreReceipts)
  if(persist(entitled)) restoreReceipts.filter {it.sku==sku && it.entitled && !it.cancelled}.forEach {PurchasingService.notifyFulfillment(it.id,FulfillmentResult.FULFILLED)}
  restoring=false;restoreReceipts.clear()
 }
 private fun persist(premium:Boolean):Boolean {
  val id=user ?: return false
  val saved=prefs.edit().putString("user",id).putBoolean("premium",premium).commit()
  mutable.value=mutable.value.copy(premium=if(saved) premium else mutable.value.premium,busy=false,message=if(saved) R.string.billing_updated else R.string.billing_unavailable)
  return saved
 }
 private fun fail() {mutable.value=mutable.value.copy(canPurchase=false,busy=false,message=R.string.billing_unavailable)}
}
data class BillingState(val premium:Boolean=false,val price:String="",val canPurchase:Boolean=false,val busy:Boolean=false,val message:Int=R.string.billing_configuration)
data class StoreReceipt(val sku:String,val id:String,val cancelled:Boolean,val entitled:Boolean)
object EntitlementPolicy {
 fun accept(currentUser:String?,receiptUser:String,sku:String,receipt:StoreReceipt):Boolean? {
  if(currentUser==null || currentUser!=receiptUser || sku.isBlank() || receipt.sku!=sku || receipt.id.isBlank() || !receipt.entitled)return null
  return !receipt.cancelled
 }
 fun restore(sku:String,receipts:List<StoreReceipt>):Boolean {
  if(sku.isBlank())return false
  val valid=receipts.filter {it.sku==sku && it.id.isNotBlank() && it.entitled}
  val revoked=valid.filter {it.cancelled}.map {it.id}.toSet()
  return valid.any {!it.cancelled && it.id !in revoked}
 }
}
