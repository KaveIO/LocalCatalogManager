import unittest
import urllib.request

if __name__ == '__main__':
    from os import sys, path

    sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase


class TestAuhtorizationIntegration(IntegrationTestCase):

    @classmethod
    def setUpClass(cls):
        super(TestAuhtorizationIntegration, cls).setUpClass()
        cls.create_authorized_lcm()

    @classmethod
    def create_authorized_lcm(cls):
        try:
            result = cls.request(cls, "client/v0/authorizedlcm",
                                  {"name": "integration-test-lcm", "uniqueId": cls.unique_lcm_id,
                                   "applicationId": cls.application_id, "applicationKey": cls.application_key},
                                  "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"
                                  )
            cls.assertTrue(cls, result.code == 200)

            result = cls.request(cls, "client/v0/users",
                                  {"name": "userA", "origin": cls.unique_lcm_id,
                                   "role": "remoteUser", "newPassword": "userA"},
                                  "application/nl.kpmg.lcm.server.data.User+json"
                                  )
            cls.assertTrue(cls, result.code == 200)

            result = cls.request(cls, "client/v0/users")
            cls.assertTrue(cls, result.code == 200)
            items = cls.get_response_items(cls, result)
            user_a_id = items[0]['item']['id']

            result = cls.request(cls, "client/v0/users",
                                 {"name": "userB", "origin": cls.unique_lcm_id,
                                  "role": "remoteUser", "newPassword": "userB"},
                                 "application/nl.kpmg.lcm.server.data.User+json"
                                 )
            cls.assertTrue(cls, result.code == 200)

            result = cls.request(cls, "client/v0/users",
                                  {"name": "userC", "origin": 'local',
                                   "role": "apiUser", "newPassword": "userC"},
                                  "application/nl.kpmg.lcm.server.data.User+json"
                                  )
            cls.assertTrue(cls, result.code == 200)

            result = cls.request(cls, "client/v0/userGroups",
                                 {"name": "userGroupA", "users": [user_a_id]},
                                 'application/nl.kpmg.lcm.server.data.UserGroup+json'
                                 )
            cls.assertTrue(cls, result.code == 200)

        except urllib.error.HTTPError as error:
            cls.print_docker_output('lcm-integration-server')

    @classmethod
    def tearDownClass(cls):
        try:
            result = cls.request(cls, 'client/v0/authorizedlcm')
            cls.assertTrue(cls, result.code == 200)
            items = cls.get_response_items(cls, result)
            lcm_id = items[0]['item']['id']

            # delete
            result = cls.request(cls, 'client/v0/authorizedlcm/' + lcm_id, method='DELETE')
            cls.assertTrue(cls, result.code == 200)

            result = cls.request(cls, 'client/v0/users')
            cls.assertTrue(cls, result.code == 200)
            items = cls.get_response_items(cls, result)
            for user in items:
                user_id = user['item']['id']
                result = cls.request(cls, 'client/v0/users/' + user_id, method='DELETE')
                cls.assertTrue(cls, result.code == 200)

        except urllib.error.HTTPError as error:
            cls.print_docker_output('lcm-integration-server')

        finally:
            super(TestAuhtorizationIntegration, cls).tearDownClass()


    def test_case1_administrator_role(self):
        pass
        result = self.request('remote/v0/metadata')
        self.assertTrue(result.code == 200)
        result = self.request('client/v0/local')
        self.assertTrue(result.code == 200)

    def test_case2_remote_user_role(self):
        authorization = self.get_authorization(self.application_id, self.hashed_key)
        result = self.request('remote/v0/metadata',  data=None, content_type="application/json",
                              authorization=authorization , origin=self.unique_lcm_id, method='GET', remote_user='userA' )
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request('client/v0/local', None, "application/json",
                         authorization, self.unique_lcm_id, 'GET', "UserA")

        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case3_api_user_role(self):
        authorization = self.get_authorization('userC', 'userC')
        result = self.request('client/v0/local', None,
                              "application/json", authorization)
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request('remote/v0/metadata', None ,
                         "application/json", authorization)

        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)


    def test_case4_remote_user_directly_athorized_by_id(self):
        pass

    def test_case5_remote_user_directly_athorized_by_path(self):
        pass

    def test_case6_remote_user_group_athorized_by_id(self):
        pass

    def test_case7_remote_user_group_athorized_by_path(self):
        pass


if __name__ == '__main__':
    import integration

    integration.main(warnings='ignore')
