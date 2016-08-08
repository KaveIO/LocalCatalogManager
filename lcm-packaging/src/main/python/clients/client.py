#!/usr/bin/python

import urllib2
import pprint
import json

class LCMConnector(object):

  _url = None
  _user = None
  _token = None

  def __init__(self, url):
    self._url = url

  def authenticate(self, username, password):
    url = '%s/client/login' % (self._url)

    req = urllib2.Request(url, "{\"username\": \"%s\", \"password\": \"%s\"}" % (username, password), headers={
      "Content-Type": "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json"})
    f = urllib2.urlopen(req)

    if f.code != 200:
      raise Exception("Could not authenticate")

    self._user = username
    self._token = f.read()
    
    print "API token is: %s" % self._token

  def request(self, path, parse_output=True, data=None, headers={}, **kwargs):
    if self._user is None:
      raise Exception("No authentication user found")
    if self._token is None:
      raise Exception("No authentication token found")

    url = '%s/%s' % (self._url, path)
    headers['LCM-Authentication-User'] = self._user
    headers['LCM-Authentication-Token'] = self._token

    req = urllib2.Request(url, data, headers, **kwargs)
    f = urllib2.urlopen(req)

    if parse_output:
      return json.loads(f.read())
    else:
      return f

connector = LCMConnector("http://localhost:8080")
connector.authenticate("admin", "admin") 

print ""
print "List all the metadata"
pprint.pprint(connector.request("client/v0/local/"))


print ""
print "Show a single piece of metadata"
pprint.pprint(connector.request("client/v0/local/example"))

print ""
print "Insert a piece of metadata"
connector.request(
  "client/v0/local/", 
  False,  
  json.dumps({
    "name": "wifiMeasurement",
    "data": {"uri": "hdfs://la/my/file/path"},
    "general": {"tags": ["la"]},
    "schema": {
      "type": "avro",
      "content": {
        "namespace": "tst_la",
        "type": "record",
        "name": "wifiMeasurement_v2",
        "fields": [
          {"name": "measurementTimestamp", "type": "long"},
          {"name": "processingTimestamp", "type": "long"},
          {"name": "application", "type": "string"},
          {"name": "value", "type": "record", "fields": [
            {"name": "typenr", "type": "int"},
            {"name": "seqnr", "type": "int"},
            {"name": "droneid", "type": "string"},
            {"name": "sourcemac", "type": "string"},
            {"name": "signal", "type": "int"},
            {"name": "subtypenr", "type": "int"},
            {"name": "retryflag", "type": "int"}
          ]},
          {"name": "history", "type": "array", "items": "string"}
        ]
      }
    }
  }), {"Content-Type": "application/nl.kpmg.lcm.server.data.MetaData+json"})

print ""
print "Retrieve it"
pprint.pprint(connector.request("client/v0/local/wifiMeasurement"))
