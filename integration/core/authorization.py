import unittest
import urllib.request

if __name__ == '__main__':
    from os import sys, path

    sys.path.append(path.dirname(path.dirname(path.abspath(__file__))))

from integration import IntegrationTestCase


class TestAuhtorizationIntegration(IntegrationTestCase):
    user_a_id = None
    usergroup_a_id = None
    metadata_id = None
    metadata_path = None
    authorization = None

    @classmethod
    def setUpClass(cls):
        super(TestAuhtorizationIntegration, cls).setUpClass()
        cls.create_authorized_lcm()
        cls.create_metadata()
        cls.create_users()
        cls.create_user_group()

    @classmethod
    def create_authorized_lcm(cls):
        result = cls.get_request('client/v0/authorizedlcm')
        cls.assertTrue(cls, result.code == 200)

        items = cls.get_response_items(cls, result)
        cls.assertIsNone(cls, items, 'No items are expected!')

        result = cls.post_request("client/v0/authorizedlcm",
                                  {"name": "integration-test-lcm", "uniqueId": cls.unique_lcm_id,
                                   "applicationId": cls.application_id, "applicationKey": cls.application_key},
                                  "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"
                                  )
        cls.assertTrue(cls, result.code == 200)

        cls.authorization = cls.get_authorization(cls, cls.application_id, cls.hashed_key)

        result = cls.get_request('remote/v0/test', None,
                                 "application/json",
                                 cls.authorization,
                                 cls.unique_lcm_id
                                 )
        cls.assertTrue(cls, result.code == 200)

    @classmethod
    def create_metadata(cls):
        # create metadata A
        result = cls.post_request('client/v0/local',
                                  {"name": "metadataA",
                                   "data": {"uri": ["file://localFile/f.txt"],
                                            "path": "kpmg"}},
                                  "application/nl.kpmg.lcm.server.data.MetaData+json")
        cls.assertTrue(cls, result.code == 200)

        # get metadata A`s id and path
        result = cls.get_request('client/v0/local')
        cls.assertTrue(cls, result.code == 200)
        items = cls.get_response_items(cls, result)
        cls.metadata_id = items[0]['item']['id']
        cls.metadata_path = items[0]['item']['data']['path']

    @classmethod
    def create_users(cls):
        # create user A with remote user role
        result = cls.post_request("client/v0/users",
                                  {"name": "userA", "origin": cls.unique_lcm_id,
                                   "role": "remoteUser", "newPassword": "userA"},
                                  "application/nl.kpmg.lcm.server.data.User+json"
                                  )
        cls.assertTrue(cls, result.code == 200)

        # get userA`s id
        result = cls.get_request("client/v0/users")
        cls.assertTrue(cls, result.code == 200)
        items = cls.get_response_items(cls, result)
        cls.user_a_id = items[0]['item']['id']

        # create user B with remote user role
        result = cls.post_request("client/v0/users",
                                  {"name": "userB", "origin": cls.unique_lcm_id,
                                   "role": "remoteUser", "newPassword": "userB"},
                                  "application/nl.kpmg.lcm.server.data.User+json"
                                  )
        cls.assertTrue(cls, result.code == 200)

        # create user B with api user role
        result = cls.post_request("client/v0/users",
                                  {"name": "userC", "origin": 'local',
                                   "role": "apiUser", "newPassword": "userC"},
                                  "application/nl.kpmg.lcm.server.data.User+json"
                                  )
        cls.assertTrue(cls, result.code == 200)

    @classmethod
    def create_user_group(cls):
        # create user group A
        result = cls.post_request("client/v0/userGroups",
                                  {"name": "userGroupA", "users": [cls.user_a_id]},
                                  'application/nl.kpmg.lcm.server.data.UserGroup+json'
                                  )
        cls.assertTrue(cls, result.code == 200)

        # get user group A`s id
        result = cls.get_request("client/v0/userGroups")
        cls.assertTrue(cls, result.code == 200)
        items = cls.get_response_items(cls, result)
        cls.usergroup_a_id = items[0]['item']['id']

    @classmethod
    def tearDownClass(cls):
        result = cls.get_request('client/v0/authorizedlcm')
        cls.assertTrue(cls, result.code == 200)
        items = cls.get_response_items(cls, result)
        lcm_id = items[0]['item']['id']

        # delete
        result = cls.delete_request('client/v0/authorizedlcm/' + lcm_id)
        cls.assertTrue(cls, result.code == 200)

        # delete the metadata
        result = cls.delete_request('client/v0/local/' + cls.metadata_id)
        cls.assertTrue(cls, result.code == 200)

        result = cls.get_request('client/v0/users')
        cls.assertTrue(cls, result.code == 200)
        items = cls.get_response_items(cls, result)
        for user in items:
            user_id = user['item']['id']
            result = cls.delete_request('client/v0/users/' + user_id)
            cls.assertTrue(cls, result.code == 200)
        super(TestAuhtorizationIntegration, cls).tearDownClass()

    def request_access_to_remote_metadata_using_remote_user(self, user):
        return self.get_request('remote/v0/metadata/' + self.metadata_id, data=None, content_type="application/json",
                                authorization=self.authorization, origin=self.unique_lcm_id, remote_user=user)

    def update_user_removing_allowed_metadata_or_path(self):
        return self.put_request('client/v0/users', {"id": self.user_a_id, "name": "userA",
                                                    "origin": self.unique_lcm_id,
                                                    "role": "remoteUser", "newPassword": "userA"},
                                "application/nl.kpmg.lcm.server.data.User+json")

    def update_user_group_removing_allowed_metadata_or_path(self):
        return self.put_request('client/v0/userGroups', {"id": self.usergroup_a_id,
                                                         "name": "userGroupA",
                                                         "users": [self.user_a_id]},
                                "application/nl.kpmg.lcm.server.data.UserGroup+json")

    def test_case1_administrator_role(self):
        result = self.get_request('remote/v0/metadata')
        self.assertTrue(result.code == 200)
        result = self.get_request('client/v0/local')
        self.assertTrue(result.code == 200)

    def test_case2_remote_user_role(self):
        result = self.get_request('remote/v0/metadata', None, "application/json",
                                  self.authorization, self.unique_lcm_id, 'userA')
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.get_request('client/v0/local', None, "application/json",
                             self.authorization, self.unique_lcm_id, "UserA")
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case3_api_user_role(self):
        authorization = self.get_authorization('userC', 'userC')
        result = self.get_request('client/v0/local', None,
                                  "application/json", authorization)
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.get_request('remote/v0/metadata', None,
                             "application/json", authorization)
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case4_remote_user_directly_athorized_by_id(self):
        result = self.get_request('client/v0/users/' + self.user_a_id)
        self.assertTrue(result.code == 200)

        result = self.put_request('client/v0/users', {"id": self.user_a_id, "name": "userA",
                                                      "origin": self.unique_lcm_id,
                                                      "role": "remoteUser", "newPassword": "userA",
                                                      "allowedMetadataList": [self.metadata_id]},
                                  "application/nl.kpmg.lcm.server.data.User+json")
        self.assertTrue(result.code == 200)

        result = self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue(result.code == 200)

        result = self.get_request('client/v0/users/' + self.user_a_id)
        self.assertTrue(result.code == 200)

        result = self.update_user_removing_allowed_metadata_or_path()
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case5_remote_user_directly_athorized_by_path(self):
        result = self.get_request('client/v0/users/' + self.user_a_id)
        self.assertTrue(result.code == 200)

        result = self.put_request('client/v0/users', {"id": self.user_a_id, "name": "userA",
                                                      "origin": self.unique_lcm_id,
                                                      "role": "remoteUser", "newPassword": "userA",
                                                      "allowedPathList": [self.metadata_path]},
                                  "application/nl.kpmg.lcm.server.data.User+json")
        self.assertTrue(result.code == 200)

        result = self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue(result.code == 200)

        result = self.get_request('client/v0/users/' + self.user_a_id)
        self.assertTrue(result.code == 200)

        result = self.update_user_removing_allowed_metadata_or_path()
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case6_remote_user_group_athorized_by_id(self):
        result = self.get_request('client/v0/userGroups/' + self.usergroup_a_id)
        self.assertTrue(result.code == 200)

        result = self.put_request('client/v0/userGroups', {"id": self.usergroup_a_id,
                                                           "name": "userGroupA",
                                                           "users": [self.user_a_id],
                                                           "allowedMetadataList": [self.metadata_id]},
                                  "application/nl.kpmg.lcm.server.data.UserGroup+json")
        self.assertTrue(result.code == 200)

        result = self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userB')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

        result = self.get_request('client/v0/userGroups/' + self.usergroup_a_id)
        self.assertTrue(result.code == 200)

        result = self.update_user_group_removing_allowed_metadata_or_path()
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

    def test_case7_remote_user_group_athorized_by_path(self):
        result = self.get_request('client/v0/userGroups/' + self.usergroup_a_id)
        self.assertTrue(result.code == 200)

        result = self.put_request('client/v0/userGroups', {"id": self.usergroup_a_id,
                                                           "name": "userGroupA",
                                                           "users": [self.user_a_id],
                                                           "allowedPathList": [self.metadata_path]},
                                  "application/nl.kpmg.lcm.server.data.UserGroup+json")
        self.assertTrue(result.code == 200)

        result = self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userB')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)

        result = self.get_request('client/v0/userGroups/' + self.usergroup_a_id)
        self.assertTrue(result.code == 200)

        result = self.update_user_group_removing_allowed_metadata_or_path()
        self.assertTrue(result.code == 200)

        with self.assertRaises(Exception) as context:
            self.request_access_to_remote_metadata_using_remote_user('userA')
        self.assertTrue('Forbidden' in context.exception.reason)
        self.assertTrue(context.exception.code == 403)


if __name__ == '__main__':
    import integration

    integration.main(warnings='ignore')
