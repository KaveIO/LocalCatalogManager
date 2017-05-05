from os import sys, path
sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

import integration
import unittest
import json
import urllib.request

from time import sleep

class TestLcm2LcmIntegration(integration.TestCase):

  # Extension on core configuration
  dockers = integration.TestCase.dockers
  dockers['lcm-integration-mongo2'] = {
    'image': 'mongo:3',
    'hostname': 'mongo',
    'detach': True
  }
  dockers['lcm-integration-server2'] = {
    'image': 'kave/lcm:latest',
    'command': 'server',
    'hostname': 'server',
    'links': {
      'lcm-integration-mongo2': 'mongo',
      'lcm-integration-server': 'remote'
    },
    'detach': True,
    'ports': {'8081/tcp': 8082},
    'stdin_open': True
  }

  @classmethod
  def setUpClass(cls):
    super(TestLcm2LcmIntegration, cls).setUpClass()
    
    cls.load_fixture('lcm-integration-mongo2', {
      'remote_lcm': [
        {'name': 'remote', 'domain': 'remote', 'protocol': 'http', 'port': '8081'}
      ]
    })


  def test_basic_web_request(self):
    """ Basic sanity check to see if the tests will work """
    result = self.request('client/v0')
    self.assertTrue(result)

  def test_list_remote_metadata(self):
    remote_lcms = self.request_to_json('http://localhost:8082/client/v0/remoteLcm')
    remote_lcm_id = remote_lcms['items'][0]['item']['id']

    result = self.request_to_json('http://localhost:8082/client/v0/remote/%s/search' % remote_lcm_id)
    self.assertEquals(1, len(result['items']))

    # Further tests... currently we don't pass this stage



if __name__ == '__main__':
  integration.main(warnings='ignore')
