### Overview

You need to generate(obtain) two  certificates sets as two servers are run. 
In most cases you will run LCM with self signed certificates. Bellow are the commands that you need 
to generate all security files that you need for SSL

Execute this commands in LocalCatalogManager/cetificates directory

1. openssl req -newkey rsa:2048 -nodes -keyform PEM -keyout ca.key -x509 -days 3650 -outform PEM -out ca.cer 
2. openssl genrsa -out server.key 2048
3. openssl req -new -key server.key -out server.req  
4. openssl x509 -req -in server.req -CA ca.cer -CAkey ca.key -set_serial 100 -extensions server -days 365 -outform PEM -out server.cer
5. keytool -import -alias cert-ui- -file server.cer -keystore ui.keystore - must enter meaningful password
6. openssl pkcs12 -export -inkey server.key -in server.cer -out server.p12 - must enter meaningful password
7. keytool -import -alias cert-lcm -file server.cer -keystore lcm.keystore - must enter meaningful password
8. openssl genrsa -out client.key 2048
9. openssl req -new -key client.key -out client.req  
10. openssl x509 -req -in client.req -CA ca.cer -CAkey ca.key -set_serial 100 -extensions server -days 365 -outform PEM -out client.cer
11. openssl pkcs12 -export -inkey client.key -in client.cer -out client.p12 - must enter meaningful password


Edit LocalCatalogManager/config/security.properties and set keystore/truststore passwords entered in steps  #5, 6, 7  and 11

Now LCM should use ssl correctly, but not all of generated files in previous steps are used and you can delete all files except:
1. server.p12
2. lcm.keystore
3. ui.keystore
4. client.p12
5. server.cer
