# Optional reminder notifications

Notifications are off until the user enables them in PrepTrove Settings. Saved reminder records remain available when notifications are off. Earlier preview versions did not store an opt-in flag, so their users must explicitly enable notifications after upgrading. The choice stays on the device and is not enabled by restoring a backup.

On Android 13 and newer, the app requests notification permission only after that enable action. A declined request leaves notifications disabled in the app. On older Fire versions, the in-app choice is saved without a runtime permission prompt; system notification settings can still block delivery. Users can turn the feature off again in Settings.

The app checks due reminders periodically, normally daily. Record saves, completion, deletion, template use and restore request an additional best-effort refresh. WorkManager and OS power/network/device policies control actual execution; these are planning reminders, not exact alarms or emergency alerts. The refresh queue is serialized. Disabling and notification publication share a process-local mutex so an older worker cannot publish after the disable operation completes. There is no multi-process notification publisher.

Notices contain only a localized generic title and due count. They do not include record titles, phone numbers, locations, photos or plan notes. A refresh clears the existing notice when no reminders are due, when opt-in is off, or when notifications are unavailable. Disabling or deleting all records also clears it. Tapping a notice opens the planner.

## Validation boundaries

Instrumentation covers an actual Android NotificationManager notice being posted, replaced and removed, using a unique QA tag. It checks native notification text against localized plural resources and cleans up only its test notice. That test is conditional on OS notification permission/availability; a skip is not a delivery pass. A separate UI test enables notifications, recreates the activity and disables them, restoring the original opt-in value afterward. API 33+ permission-dialog acceptance remains a manual check.

Long-running periodic scheduling, idle/battery restrictions, reboot delivery and additional Fire versions remain acceptance work. Native publisher tests do not prove that a daily scheduled job will run on every supported device.
