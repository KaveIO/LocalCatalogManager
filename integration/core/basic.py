import unittest
import json
import urllib.request

if __name__ == '__main__':
  from os import sys, path
  sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase

class TestCoreIntegration(IntegrationTestCase):

  # Flush the fixture so we know the DB is empty   
  fixture = {}

  def test_add_storage(self):
    result = self.request('client/v0/storage', 
      {"name":"csv-storage","type": "csv", "options":{"storagePath":"/data"}}, 
      'application/nl.kpmg.lcm.server.data.Storage+json')
    self.assertEquals(200, result.status)

    result = self.request_to_json('client/v0/storage')
    self.assertEquals(1, len(result['items']))
    self.assertEquals('csv-storage', result['items'][0]['item']['name'])
    self.assertEquals('csv', result['items'][0]['item']['type'])
    self.assertEquals('/data', result['items'][0]['item']['options']['storagePath'])

  def test_add_metadata(self): 
    result = self.request('client/v0/local',
      {"name":"sample", "data": {"uri": "csv://csv-storage/temp.csv"}},
      'application/nl.kpmg.lcm.server.data.MetaData+json')
    self.assertEquals(200, result.status)

    result = self.request_to_json('client/v0/local')
    self.assertEquals(1, len(result['items']))
    self.assertEquals('sample', result['items'][0]['item']['name'])
    self.assertEquals('csv://csv-storage/temp.csv', result['items'][0]['item']['data']['uri'])

if __name__ == '__main__':
  import integration
  integration.main(warnings='ignore')
