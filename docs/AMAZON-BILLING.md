# Amazon billing integration

## Implemented client

Pinned Amazon Appstore SDK 3.0.9. Main-thread listener registration, account/catalogue refresh, entitlement-only catalogue validation, store-supplied formatted price, purchase callbacks, paginated full restore, cancellations, account binding, durable local entitlement cache, and fulfillment after local persistence are implemented. The protected Amazon broadcast receiver and package queries are declared. There is no Google Play Billing dependency.

Purchases are OFF by default. No catalogue item, authentication key, receipt, successful purchase or price has been invented. All development features remain available for evaluation; commercial free/premium creation limits are not yet enforced. Contacts and existing plans remain accessible. Do not enable production checkout until the premium value/limits, verification policy and store tests are approved.

## Owner configuration and validation

1. Create the app and the final one-time ENTITLED SKU in Amazon Developer Console; select territories and final store prices.
2. Download that app's real AppstoreAuthenticationKey.pem to app/src/main/assets/. It is the app public key, not an Amazon developer secret. Do not place developer/private keys in the APK.
3. Build with `-PamazonPremiumSku=OWNER_SKU`. With no public key or SKU the SDK is not started and purchases cannot be offered.
4. For authorised sandbox checkout testing only, also set `-PamazonPurchasesEnabled=true`. The default is false. Configure the official Amazon App Tester and confirm its test environment before attempting test purchases.
5. Run the matrix below and record actual results. Production checkout must remain disabled until release gates are closed.

## Verification boundary

EntitlementPolicy validates user, SKU, entitlement product type and receipt identity from SDK callbacks. It rejects mismatched users/SKUs and applies cancellation before restore grants. These are metadata checks; they are not independent server-side RVS verification. SDK public-key authentication, App Tester purchase/restore, and production RVS policy have not been validated because the owner has not configured the app or catalogue.

Cached entitlements support offline display after a previous successful store update. A successful user-data response binds the current state to that Amazon account; an account switch does not inherit another user's cached grant. An unidentified offline session can retain the last cached entitlement, pending a store refresh. The app's private plans and contacts are never sent to Amazon by this adapter. After enabling the SDK, Amazon may process store account and transaction information under its terms.

## Required real-store matrix (pending)

- Correct app public key; catalogue present/absent; unsupported store/device; signed-out store.
- Store-localised price and correct ENTITLED product type.
- Successful one-time test purchase, cancellation, failure, duplicate delivery, interrupted fulfillment and restart.
- Paginated restore, no matching receipt, cancellation/revocation, different Amazon account and offline restart.
- Sandbox/production separation and independent RVS verification decision; developer secrets remain server-side if RVS is used.
- Free-tier limits and paid feature value; no loss of access to existing plans or contacts.

Local tests exercise receipt-selection policy and disabled configuration only. They do not establish that a real purchase succeeds.

References checked 29 October 2026:
- https://developer.amazon.com/docs/appstore-sdk/integrate-appstore-sdk.html
- https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html
- https://repo.maven.apache.org/maven2/com/amazon/device/amazon-appstore-sdk/maven-metadata.xml
