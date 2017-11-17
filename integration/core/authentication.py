import unittest
import urllib.request
import integration

if __name__ == '__main__':
    from os import sys, path

    sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase


class TestCoreIntegration(IntegrationTestCase):

    def create_authorized_lcm(self):
        try:
            result = self.request('client/v0/authorizedlcm')
            self.assertTrue(result.code == 200)

            result = self.request("client/v0/authorizedlcm",
                                  {"name": "integration-test-lcm", "uniqueId": self.unique_lcm_id,
                                   "applicationId": self.application_id, "applicationKey": self.application_key},
                                  "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"
                                  )
            self.assertTrue(result.code == 200)
            string = result.read().decode('utf-8')
            self.assertTrue(len(string) == 0)
        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def setUp(self):
        self.create_authorized_lcm()


    def tearDown(self):
        result = self.request('client/v0/authorizedlcm')
        items = self.get_response_items(result)
        if items != None and len(items) == 1:
            lcm_id = items[0]['item']['id']
            result = self.request('client/v0/authorizedlcm/' + lcm_id, method='DELETE')


    def test_case1_authentication_of_authorized_LCM(self):
        try:
            result = self.request('remote/v0/test', None,
                                  "application/json",
                                  self.get_authorization(self.application_id,
                                                         self.hashed_key),
                                  self.unique_lcm_id
                                  )
            self.assertTrue(result.code == 200)

            result = self.request('client/v0/authorizedlcm')
            self.assertTrue(result.code == 200)
            items = self.get_response_items(result)
            self.assertIsNotNone(items)
            self.assertEqual(len(items), 1, 'Only one item is expected!')
            lcm_id = items[0]['item']['id']

            # delete
            result = self.request('client/v0/authorizedlcm/' + lcm_id, method='DELETE')
            self.assertTrue(result.code == 200)

            # try to authorize now
            with self.assertRaises(Exception) as context:
                self.request('remote/v0/test', None,
                             "application/json",
                             self.get_authorization(self.application_id,
                                                    self.hashed_key),
                             self.unique_lcm_id
                             )
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')



    def test_case2_authentication_of_authorized_LCM_wrong_application_key(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request('remote/v0/metadata', None, "application/json",
                             self.get_authorization(self.application_id, "wrong_application_key"),
                             self.unique_lcm_id)
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')



    def test_case3_authentication_of_authorized_LCM_wrong_application_key(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request('remote/v0/metadata', None, "application/json",
                             self.get_authorization("wrong_application_id",
                                                    self.hashed_key),
                             self.unique_lcm_id)
            self.assertTrue("Unauthorized" in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def test_case4_authentication_of_authorized_LCM_invalid_origin(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request('remote/v0/metadata', None, "application/json",
                             self.get_authorization(self.application_id,
                                                    self.hashed_key),
                             "invalid_origin_header")
            self.assertTrue("Unauthorized", context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def test_case5_authentication_of_authorized_LCM_sensitive_endpoints_wrong_key(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request('remote/v0/fetch/59b9337b90c30d384b3db4c5', None, "application/json",
                             self.get_authorization(self.application_id, "wrong_application_key"),
                             self.unique_lcm_id)
            self.assertTrue("Unauthorized", context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def test_case6_authentication_of_administrator_user(self):
        try:
            result = self.request('remote/v0/test')
            self.assertTrue(result.code == 200)

            result = self.request("client/v0/users",
                                  {"name": "testAdmin", "role": "administrator",
                                   "origin": "local", "newPassword": "password"},
                                  "application/nl.kpmg.lcm.server.data.User+json")
            self.assertTrue(result.code == 200)

            with self.assertRaises(Exception) as context:
                self.request('remote/v0/test')
            self.assertTrue("Unauthorized", context.exception.reason)
            self.assertTrue(context.exception.code == 401)

            result = self.request('remote/v0/test', None,
                                  "application/json",
                                  self.get_authorization("testAdmin", "password"))
            self.assertTrue(result.code == 200)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')

        finally:
            result = self.request("client/v0/users", None,
                                  "application/nl.kpmg.lcm.server.data.User+json",
                                  self.get_authorization("testAdmin", "password"))
            self.assertTrue(result.code == 200)
            items = self.get_response_items(result)
            for user in items:
                user_id = user['item']['id']
                result = self.request('client/v0/users/' + user_id, None,
                                      "application/nl.kpmg.lcm.server.data.User+json",
                                      self.get_authorization("testAdmin", "password"),
                                      method='DELETE')
                self.assertTrue(result.code == 200)


    def test_case7_authentication_of_local_api_user(self):
        try:
            result = self.request('client/v0/users', {"name":"testApiUser", "role" : "apiUser",
                                             "origin" : "local", "newPassword" : "password"},
                         'application/nl.kpmg.lcm.server.data.User+json')
            self.assertTrue(result.code == 200)

            result = self.request('remote/v0/test', None,
                                  "application/json",
                                  self.get_authorization("testApiUser", "password"))
            self.assertTrue(result.code == 200)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')

        finally:
            result = self.request("client/v0/users", None,
                                  "application/nl.kpmg.lcm.server.data.User+json")
            self.assertTrue(result.code == 200)
            items = self.get_response_items(result)
            for user in items:
                user_id = user['item']['id']
                result = self.request('client/v0/users/' + user_id, None,
                                      "application/nl.kpmg.lcm.server.data.User+json",
                                      None,
                                      method='DELETE')
                self.assertTrue(result.code == 200)


    def test_case8_authentication_of_local_administrator_wrong_password(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request('client/v0/users', None, "application/json",
                             self.get_authorization("admin", "wrong_password"))
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def test_case9_authentication_with_session_key(self):
        try:
            result = self.request('client/login', {"username" : "admin", "password" : "admin"},
                                  "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json")
            self.assertTrue(result.code == 200)
            authentication_token = str(result.read(), 'utf-8')

            result = self.request_session_authentication('remote/v0/test', "admin", authentication_token)
            self.assertTrue(result.code == 200)

            with self.assertRaises(Exception) as context:
                self.request_session_authentication('remote/v0/test', "admin", "random_string")
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')



    def test_case10_authentication_with_sesstion_key_authorized_lcm(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request_session_authentication('remote/v0/test', self.application_id, self.application_key)
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

            with self.assertRaises(Exception) as context:
                self.request_session_authentication('remote/v0/test', self.application_id, self.hashed_key)
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


    def test_case11_authentication_without_credentials(self):
        try:
            with self.assertRaises(Exception) as context:
                self.request_session_authentication('remote/v0/test')
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


if __name__ == '__main__':
    import integration

    integration.main(warnings='ignore')
