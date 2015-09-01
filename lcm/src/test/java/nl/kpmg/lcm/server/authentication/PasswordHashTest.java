package nl.kpmg.lcm.server.authentication;

import nl.kpmg.lcm.server.rest.authentication.PasswordHash;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

/**
 * @author venkateswarlub
 *
 */
public class PasswordHashTest {

    @Test
    public void testHashesDifferInIterations() throws NoSuchAlgorithmException, InvalidKeySpecException {
        for(int i = 0; i < 100; i++) {
            String password = ""+i;
            String hash = PasswordHash.createHash(password);
            String secondHash = PasswordHash.createHash(password);
            assertNotSame(hash, secondHash);
        }
    }

    @Test
    public void testWrongPasswordDoesNotValidate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        for(int i = 0; i < 100; i++) {
            String password = ""+i;
            String wrongPassword = ""+i+1;
            String hash = PasswordHash.createHash(password);

            assertFalse(PasswordHash.validatePassword(wrongPassword, hash));
        }
    }

    @Test
    public void testGoodPasswordDoesValidate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        for(int i = 0; i < 100; i++) {
            String password = ""+i;
            String hash = PasswordHash.createHash(password);
            assertTrue(PasswordHash.validatePassword(password, hash));
        }
    }
}
