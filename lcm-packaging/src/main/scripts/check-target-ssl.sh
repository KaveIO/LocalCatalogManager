#usage
#./check-target-ssl.sh  10.191.30.200 4444
# Print certificate details if ssl is started.
DOMAIN="$1"
PORT="$2"

openssl s_client -connect $DOMAIN:$PORT | openssl x509 -text -noout
