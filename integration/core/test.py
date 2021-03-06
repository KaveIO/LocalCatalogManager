import unittest
import json
import urllib.request

if __name__ == '__main__':
  from os import sys, path
  sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase 

class TestCoreIntegration(IntegrationTestCase):

  def test_basic_web_request(self):
    result = self.request('client/v0')
    self.assertTrue(result)

  def test_create_metadata(self):
    try:  
      result = self.request('client/v0/local',
                            {"name":"example2", "data": { "uri": ["csv://local/mock.csv"], "path" : "kpmg"}},
                            'application/nl.kpmg.lcm.server.data.MetaData+json')
      self.assertTrue(result)
    except urllib.error.HTTPError as error:
      integration.TestCase.print_docker_output('lcm-integration-server')

if __name__ == '__main__':
  import integration
  integration.main(warnings='ignore')
