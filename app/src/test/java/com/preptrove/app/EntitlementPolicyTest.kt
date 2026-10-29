package com.preptrove.app
import org.junit.Test
import org.junit.Assert.*
class EntitlementPolicyTest {
 private val receipt=StoreReceipt("premium","receipt1",false,true)
 @Test fun accountSkuAndReceiptMustMatch() {
  assertEquals(true,EntitlementPolicy.accept("a","a","premium",receipt))
  assertNull(EntitlementPolicy.accept(null,"a","premium",receipt))
  assertNull(EntitlementPolicy.accept("b","a","premium",receipt))
  assertNull(EntitlementPolicy.accept("a","a","other",receipt))
  assertNull(EntitlementPolicy.accept("a","a","premium",receipt.copy(entitled=false)))
  assertNull(EntitlementPolicy.accept("a","a","premium",receipt.copy(id="")))
 }
 @Test fun cancellationWinsRegardlessOfRestorePageOrder() {
  val cancelled=receipt.copy(cancelled=true)
  assertFalse(EntitlementPolicy.restore("premium",listOf(receipt,cancelled)))
  assertFalse(EntitlementPolicy.restore("premium",listOf(cancelled,receipt)))
  assertTrue(EntitlementPolicy.restore("premium",listOf(cancelled,receipt.copy(id="new"))))
  assertFalse(EntitlementPolicy.restore("premium",emptyList()))
 }
}
