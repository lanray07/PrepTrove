import csv
import io
import unittest
from review_packets import import_translations, validate_store_queue


class ReviewImportTest(unittest.TestCase):
    def test_store_validation_rejects_missing_fields_stale_source_and_overflow(self):
        source = {'displayTitle': 'PrepTrove', 'shortDescription': 'Planner', 'longDescription': 'Household planner'}
        queue = {'locale': 'fr-FR', 'source': source, 'translation': {'displayTitle': 'PrepTrove', 'shortDescription': 'Planificateur', 'longDescription': 'Planificateur du foyer'}}
        self.assertTrue(validate_store_queue(queue, source))
        for changed in [dict(queue['translation'], extra='x'), dict(queue['translation'], displayTitle='PrepTrove' + 'x' * 200)]:
            with self.assertRaises(ValueError):
                validate_store_queue(dict(queue, translation=changed), source)
        with self.assertRaises(ValueError):
            validate_store_queue(dict(queue, source={}), source)
    def queue(self):
        return {'locale': 'fr-FR', 'reviewer': 'Prior reviewer', 'qualifiedSafetyReview': True,
                'strings': {'count': {'source': '%d records', 'translation': '%d fiches', 'reviewed': True}}}

    def csv(self, *rows):
        text = io.StringIO()
        writer = csv.writer(text)
        writer.writerow(['locale', 'key', 'source', 'translation', 'safetyReviewRequired'])
        writer.writerows(rows)
        return text.getvalue()

    def test_changed_translation_clears_approval_without_mutating_original(self):
        queue = self.queue()
        updated = import_translations(queue, self.csv(['fr-FR', 'count', '%d records', '%d éléments', False]))
        self.assertFalse(updated['strings']['count']['reviewed'])
        self.assertFalse(updated['qualifiedSafetyReview'])
        self.assertEqual('', updated['reviewer'])
        self.assertTrue(queue['strings']['count']['reviewed'])

    def test_stale_wrong_locale_duplicate_and_invalid_placeholders_are_rejected(self):
        valid = ['fr-FR', 'count', '%d records', '%d fiches', False]
        for row in [['de-DE', *valid[1:]], ['fr-FR', 'count', 'old source', '%d fiches', False],
                    ['fr-FR', 'count', '%d records', 'fiches', False]]:
            with self.subTest(row=row), self.assertRaises(ValueError):
                import_translations(self.queue(), self.csv(row))
        with self.assertRaises(ValueError):
            import_translations(self.queue(), self.csv(valid, valid))

    def test_unchanged_text_preserves_approval_and_blank_clears_it(self):
        queue = self.queue()
        self.assertEqual(queue, import_translations(queue, self.csv(['fr-FR', 'count', '%d records', '%d fiches', False])))
        cleared = import_translations(queue, self.csv(['fr-FR', 'count', '%d records', '', False]))
        self.assertIsNone(cleared['strings']['count']['translation'])
        self.assertFalse(cleared['strings']['count']['reviewed'])
