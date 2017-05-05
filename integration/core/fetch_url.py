import unittest
import json
import urllib.request

if __name__ == '__main__':
  from os import sys, path
  sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase

class TestCoreFetchUrlIntegration(IntegrationTestCase):

  def test_get_fetch_url(self): 
    metadatas = self.request_to_json('client/v0/local')
    self.assertEquals(1, len(metadatas['items']))
    metadata_id = metadatas['items'][0]['item']['id'] 

    fetch_url = self.request_to_json('remote/v0/metadata/%s/fetchUrl' % metadata_id)
    fetch_url_id = fetch_url['item']['id']


    try: 
      fetch_content = self.request_to_json('remote/v0/fetch/%s' % fetch_url_id)
      self.assertEquals([{'column_b': '2', 'column_a': '1'}, {'column_b': '4', 'column_a': '3'}], 
        fetch_content)
    except urllib.error.HTTPError as error:
      self.print_docker_output('lcm-integration-server')
      self.print_lcm_log('lcm-integration-server')
      self.fail() 

if __name__ == '__main__':
  import integration 
  integration.main(warnings='ignore')
