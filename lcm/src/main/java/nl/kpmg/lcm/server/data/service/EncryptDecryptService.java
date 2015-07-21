package nl.kpmg.lcm.server.data.service;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Standard Encrypt and Decrypt service. It is used to encrypt
 * or decrypt password string using default PBEWithMD5AndDES.
 * @author venkateswarlub
 *
 */
public class EncryptDecryptService {

	@Autowired
	private StandardPBEStringEncryptor encryption;
	
	public StandardPBEStringEncryptor getEncryptDecryptService() {
		return this.encryption;
	}
	
	public String encrypt(String password) {
		return encryption.encrypt(password);
	}
	
	public String decrypt(String encpassword) {
		return encryption.decrypt(encpassword);
	}
}
