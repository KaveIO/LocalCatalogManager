import unittest
import urllib.request

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
                                  {"name": "integration-test-lcm", "uniqueId": "test_dev-1509029918655-HYO38o80zAs",
                                   "applicationId": "integration", "applicationKey": "ij8spjd5e4kb"},
                                  "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"
                                  )
            self.assertTrue(result.code == 200)
            string = result.read().decode('utf-8')
            self.assertTrue(len(string) == 0)
        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')

    def test_case1_authentication_of_authorized_LCM(self):
        try:
            self.create_authorized_lcm()

            result = self.request('remote/v0/test', None,
                                  "application/json",
                                  self.get_authorization("integration",
                                                         "1000:4332779a128eaf94d3d920c726f360ec10ded9d3f146cc47:a66aa86e154e5ded59b0510fbeeb25e94057c83e563e875c"),
                                  "test_dev-1509029918655-HYO38o80zAs"
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
                             self.get_authorization("integration",
                                                    "1000:4332779a128eaf94d3d920c726f360ec10ded9d3f146cc47:a66aa86e154e5ded59b0510fbeeb25e94057c83e563e875c"),
                             "test_dev-1509029918655-HYO38o80zAs"
                             )
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')

    def test_case2_authentication_of_authorized_LCM_wrong_application_key(self):
        try:
            self.create_authorized_lcm()

            with self.assertRaises(Exception) as context:
                self.request('remote/v0/metadata', None, "application/json",
                             self.get_authorization("integration", "wrong_application_key"),
                             "test_dev-1509029918655-HYO38o80zAs")
            self.assertTrue('Unauthorized' in context.exception.reason)
            self.assertTrue(context.exception.code == 401)

        except urllib.error.HTTPError as error:
            self.print_docker_output('lcm-integration-server')


if __name__ == '__main__':
    import integration

    integration.main(warnings='ignore')
