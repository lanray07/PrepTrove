import unittest
from localization_qa import preview_pack


class PreviewIsolationTest(unittest.TestCase):
    def queue(self):
        return {'locale': 'ar', 'qualifiedSafetyReview': False, 'strings': {
            'label': {'source': 'Name', 'translation': 'الاسم', 'reviewed': True, 'safetyReviewRequired': False},
            'guidance': {'source': 'Official guidance', 'translation': 'UNAPPROVED', 'reviewed': True, 'safetyReviewRequired': True}}}

    def test_unapproved_safety_text_never_enters_preview_translation(self):
        strings, _, safety = preview_pack(self.queue(), {'label': 'Name', 'guidance': 'Official guidance'}, {})
        self.assertEqual('الاسم', strings['label'])
        self.assertEqual('Official guidance', strings['guidance'])
        self.assertEqual(['guidance'], safety)

    def test_stale_or_unreviewed_non_safety_copy_blocks_preview(self):
        for change in [{'reviewed': False}, {'source': 'Old name'}, {'translation': None}]:
            queue = self.queue()
            queue['strings']['label'].update(change)
            with self.subTest(change=change), self.assertRaises(ValueError):
                preview_pack(queue, {'label': 'Name', 'guidance': 'Official guidance'}, {})
    def test_authoritative_safety_flags_cannot_be_overridden_by_a_queue(self):
        queue=self.queue()
        queue['strings']['guidance']['safetyReviewRequired']=False
        with self.assertRaises(ValueError):
            preview_pack(queue, {'label':'Name','guidance':'Official guidance'}, {}, {'guidance'})
