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

from time import sleep

class TestCase(unittest.TestCase):
  client = docker.from_env()
  server_url = 'http://localhost:8081'
  username = 'admin'
  password = 'admin'

  @classmethod
  def setUpClass(cls):
    cls.tearDownClass()
    mongo = cls.client.containers.run("mongo:3",
      hostname="mongo",
      name="lcm-integration-mongo",
      detach=True,
      ports={'27017/tcp': 27017})

    server = cls.client.containers.run("kave/lcm", "server",
      hostname="server",
      name="lcm-integration-server",
      links={"lcm-integration-mongo": "mongo"},
      detach=True,
      ports={'8081/tcp': 8081},
      stdin_open=True)

    ui = cls.client.containers.run("kave/lcm", "ui",
      hostname="ui",
      name="lcm-integration-ui",
      links={"lcm-integration-server": "server"},
      detach=True,
      ports={'8080/tcp': 8080},
      stdin_open=True)

    # Some random backoff time to allow mongo to become alive
    sleep(5)

    cls.load_fixture()


  @classmethod
  def tearDownClass(cls):
    containers = cls.client.containers.list(all=True)
    for container in containers: 
      if container.name in ["lcm-integration-mongo", "lcm-integration-server", "lcm-integration-ui"]: 
        container.remove(force=True)

  @classmethod
  def load_fixture(cls): 
    mongo = cls.client.containers.get('lcm-integration-mongo')
    fixture = cls.get_fixture()
    for collection in fixture:
      for document in fixture[collection]:
        mongo.exec_run('mongo -eval \'db.%s.insert(%s)\' localhost/lcm' % (collection, json.dumps(document)))

  @classmethod 
  def get_fixture(cls):
    return {
      'metadata': [
        {"name":"example", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 58084631, "records": 3455461}, "data": { "uri": "csv://local/mock.csv"}}
      ], 'storage': [
        {"name": "local", "type": "csv", "options":[{"storagePath":"/tmp"}]}
      ]
    }

  @classmethod
  def print_docker_output(cls, container_id): 
    print('Printing for %s ' % container_id)
    container = cls.client.containers.get(container_id)
    err = container.logs(stderr=True, tail=40)
    print('--------------  Container %s error stream  --------------' % container.name)
    print(err.decode('utf-8').replace('\\n', '\n'))
    print('')
  
  def get_context(self):
    # Example code from a different project
    # We need to do this, somehow we need the right ssl certificate configured here for the two way 
    # SSL to work. The requires us to fetch the certificate from the dockers/LCM/default folder. (or similar)
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    return ctx 

  def get_authorization(self, username, password):
    identity = ('%s:%s' % (username, password)).encode('utf-8') 
    return "Basic %s" % base64.b64encode(identity).decode('utf-8')

  def request(self, url, data=None, content_type="application/json", authorization=None):
    if url[:7] != 'http://':
      url = '%s/%s' % (self.server_url, url)

    if authorization is None:
      authorization = self.get_authorization(self.username, self.password)

    req = urllib.request.Request(url, data, {
      'Content-Type': content_type,
      'Authorization': authorization})

    return urllib.request.urlopen(req)
      

  

