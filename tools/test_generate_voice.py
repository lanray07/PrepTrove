import unittest
from generate_voice import validated_templates,ACTIONS


class VoiceTemplateValidation(unittest.TestCase):
 def queue(self):
  return {'locale':'fr-FR','reviewer':'Codex AI','intents':{k:['ajouter {supply}'] for k in ACTIONS},'translations':{k:['ajouter {supply}'] for k in ACTIONS}}
 def test_slots_cannot_be_removed_or_renamed(self):
  queue=self.queue()
  self.assertEqual(8,len(validated_templates(queue)))
  for text in ['ajouter','ajouter {task}','ajouter {supply} {supply}']:
   queue['translations']['addSupply']=[text]
   with self.assertRaises(ValueError):validated_templates(queue)
 def test_missing_intent_and_multiline_templates_are_rejected(self):
  queue=self.queue();del queue['translations']['quantity']
  with self.assertRaises(ValueError):validated_templates(queue)
  queue=self.queue();queue['translations']['addSupply']=['ajouter\n{supply}']
  with self.assertRaises(ValueError):validated_templates(queue)
