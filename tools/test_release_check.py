import unittest
from release_check import gate_failures


class DeferredBillingTest(unittest.TestCase):
    def test_only_verified_disabled_purchases_can_defer_paid_release_checks(self):
        gates = {'amazonCatalogueAndVerifiedReceipts': {
            'status': 'deferred_while_disabled', 'evidence': 'Real store tests pending.'}}
        self.assertEqual([], gate_failures(gates, purchases_disabled=True))
        self.assertEqual(1, len(gate_failures(gates, purchases_disabled=False)))

    def test_other_gates_cannot_be_deferred_by_disabling_purchases(self):
        gates = {'localisedScreenshotsAndMetadata': {
            'status': 'deferred_while_disabled', 'evidence': 'Translation pending.'}}
        self.assertEqual(1, len(gate_failures(gates, purchases_disabled=True)))
