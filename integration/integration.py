import unittest
import os 
import glob
import shutil
import subprocess
import docker 
import base64
import urllib.request, urllib.error, urllib.parse
import ssl
import json
import argparse
import sys

from time import sleep
from os import path

class IntegrationTestCase(unittest.TestCase):
  client = docker.from_env()
  dockers = {
    'lcm-integration-mongo': {
      'image': 'mongo:3',
      'hostname': 'mongo',
      'detach': True,
      'ports': {'27017/tcp': 27017}
    }, 
    'lcm-integration-server': {
      'image': 'kave/lcm:latest',
      'command': 'server',
      'hostname': 'server',
      'links': {'lcm-integration-mongo': 'mongo'},
      'volumes': {'data': {'bind':'/data', 'mode': 'rw'}},
      'detach': True,
      'ports': {'8081/tcp': 8081},
      'stdin_open':True
   # UI module is currently not used for tests
   # },
   # 'lcm-integration-ui': {
   #   'image': 'kave/lcm:latest',
   #   'command': 'ui',
   #   'hostname': 'ui',
   #   'links': {'lcm-integration-server': 'server'},
   #   'detach': True,
   #   'ports': {'8080/tcp': 8080},
   #   'stdin_open': True
    }
  }
  fixture = {
    'metadata': [
      {"name":"example", "data": { "uri": ["csv://local/mock.csv"], "path": "kpmg"}}
    ], 
    'storage': [
      {"name": "local", "type": "csv", "options":{"path":"/data"}}
    ]
  }

  retain = False

  server_url = 'http://localhost:8081'

  username = 'admin'
  password = 'admin'

  @classmethod
  def setUpClass(cls):
    if not cls.retain:
      cls.tearDownDockers()

    cls.setUpDockers()

  @classmethod
  def tearDownClass(cls):
    if not cls.retain: 
      cls.tearDownDockers()

  @classmethod
  def setUpDockers(cls):
    print('')
    print('Setting up containers')
    container_names = [container.name for container in cls.client.containers.list(all=True)]

    for name in cls.dockers:
      if name not in container_names:
        print('  booting %s' % name)
        docker_config = cls.dockers[name]
        if 'volumes' in docker_config:
          original_volume_names = list(docker_config['volumes'].keys())
          for volume in original_volume_names:
            if volume[0] != '/':
              docker_config['volumes'][path.dirname(path.abspath(__file__)) + '/' + volume] = docker_config['volumes'][volume]
              docker_config['volumes'].pop(volume, None)

        cls.client.containers.run(**docker_config, name=name)
      else:
        print('  skipping %s' %  name)
   
    # Some arbitrary back-off time to allow the dockers to become alive
    sleep(30)

    cls.load_fixture('lcm-integration-mongo', cls.fixture)

  @classmethod
  def tearDownDockers(cls):
    containers = cls.client.containers.list(all=True)
    docker_names = list(cls.dockers.keys())

    for container in containers:
      if container.name in docker_names:
        container.remove(force=True)

    # Some arbitrary backoff time to allow the dockers to become alive
    sleep(5)


  @classmethod
  def load_fixture(cls, docker_name, fixture): 
    mongo = cls.client.containers.get(docker_name)
    for collection in fixture:
      for document in fixture[collection]:
        mongo.exec_run('mongo -eval \'db.%s.insert(%s)\' localhost/lcm' % (collection, json.dumps(document)))

  @classmethod
  def print_docker_output(cls, container_id): 
    print('Printing for %s ' % container_id)
    container = cls.client.containers.get(container_id)
    err = container.logs(stderr=True, tail=40)
    print('--------------  Container %s error stream  --------------' % container.name)
    print(err.decode('utf-8').replace('\\n', '\n'))
    print('')
 
  @classmethod
  def print_lcm_log(cls, container_id):
    print('Printing for %s ' % container_id)
    container = cls.client.containers.get(container_id)
    result = container.exec_run('tail -n 100 lcm-complete/logs/lcm-server.log')
    print('--------------  Container %s lcm-server.log  --------------' % container.name)
    print(result.decode('utf-8').replace('\\n', '\n'))
    print('')
 
  def get_context(self):
    # Example code from a different project
    # We need to do this, somehow we need the right ssl certificate configured here for the two way 
    # SSL to work. The requires us to fetch the certificate from the dockers/LCM/default folder. (or similar)
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    return ctx 

  def get_response_items(self, response):
    string = response.read().decode('utf-8')
    json_obj = json.loads(string)
    if json_obj is None or len(json_obj) == 0:
      return None

    if json_obj['items'] is None or len(json_obj['items']) == 0:
      return None

    return json_obj['items']


  def get_authorization(self, username, password):
    identity = ('%s:%s' % (username, password)).encode('utf-8') 
    return "Basic %s" % base64.b64encode(identity).decode('utf-8')

  def request(self, url, data=None, content_type="application/json", authorization=None, origin="local",  method='GET'):
    if url[:7] != 'http://':
      url = '%s/%s' % (self.server_url, url)

    if authorization is None:
      authorization = self.get_authorization(self.username, self.password)

    if type(data) is dict: 
      data = json.dumps(data).encode('utf-8')
      method = 'POST'

    req = urllib.request.Request(url, data, {
      'Content-Type': content_type,
      'Authorization': authorization,
      'LCM-Authentication-origin': origin}, method=method)

    return urllib.request.urlopen(req)

  def request_to_json(self, url, **kwargs):
    response = self.request(url, **kwargs)
    body = response.read()
    return json.loads(body)

  def request_remote(self, url, origin):
    response = self.request(url, None, "application/json", None, origin)
    return response.read()

def main(**kwargs): 
  parser = argparse.ArgumentParser(description='Runs integration tests')
  parser.add_argument('--retain', 
    dest='retain',
    action='store_true',
    help='Retain the dockers booted after test execution')
  parser.add_argument('unittest_args', nargs='*')
  args = parser.parse_args()
  
  # Put the parsed args on our test class
  IntegrationTestCase.retain = args.retain

  # 'Repair' the args for the unittest module
  unit_argv = [sys.argv[0]] + args.unittest_args
  
  unittest.main(argv=unit_argv, **kwargs)


if __name__ == "__main__":
  from core import *

  test_suite = unittest.TestSuite()
  test_suite.addTest(unittest.makeSuite(TestCoreIntegration))
  test_suite.addTest(unittest.makeSuite(TestCoreFetchUrlIntegration))

  runner=unittest.TextTestRunner()
  runner.run(test_suite)
