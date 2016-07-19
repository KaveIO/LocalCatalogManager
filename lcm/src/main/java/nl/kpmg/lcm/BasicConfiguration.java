package nl.kpmg.lcm;

public abstract class BasicConfiguration {

	protected String serviceName;
    protected Integer servicePort;
    protected Integer secureServicePort;

    protected String keystore;
    protected String keystoreType;
    protected String keystorePassword;
    protected String keystoreAlias;
    protected String keystoreKeypass;
    protected String truststore;
    protected String truststoreType;
    protected String truststorePassword;
    
    protected boolean fallback;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String ServiceName) {
		this.serviceName = ServiceName;
	}
	public Integer getServicePort() {
		return servicePort;
	}
	public void setServicePort(Integer ServicePort) {
		this.servicePort = ServicePort;
	}
	public Integer getSecureServicePort() {
		return secureServicePort;
	}
	public void setSecureServicePort(Integer secureServicePort) {
		this.secureServicePort = secureServicePort;
	}
	public String getKeystore() {
		return keystore;
	}
	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}
	public String getKeystoreType() {
		return keystoreType;
	}
	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getKeystoreAlias() {
		return keystoreAlias;
	}
	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}
	public String getKeystoreKeypass() {
		return keystoreKeypass;
	}
	public void setKeystoreKeypass(String keystoreKeypass) {
		this.keystoreKeypass = keystoreKeypass;
	}
	public String getTruststore() {
		return truststore;
	}
	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}
	public String getTruststoreType() {
		return truststoreType;
	}
	public void setTruststoreType(String truststoreType) {
		this.truststoreType = truststoreType;
	}
	public String getTruststorePassword() {
		return truststorePassword;
	}
	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}
	
	public boolean isFallback() {
		return fallback;
	}
	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}

}
