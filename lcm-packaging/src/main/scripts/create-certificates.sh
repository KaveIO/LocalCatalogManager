#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PATH_PREFIX=${1:-$DIR/../../main/resources}

KEYSTORE_PATH_SUFFIX=keystore.jks
TRUSTSTORE_PATH_SUFFIX=truststore.jks
CERTIFICATE_PATH_SUFFIX=certificate.cer

KEYSTORE_PATH_UI=$PATH_PREFIX/ui-$KEYSTORE_PATH_SUFFIX
TRUSTSTORE_PATH_UI=$PATH_PREFIX/ui-$TRUSTSTORE_PATH_SUFFIX
CERTIFICATE_PATH_UI=$PATH_PREFIX/ui-$CERTIFICATE_PATH_SUFFIX
KEYSTORE_PATH_LCM=$PATH_PREFIX/lcm-$KEYSTORE_PATH_SUFFIX
TRUSTSTORE_PATH_LCM=$PATH_PREFIX/lcm-$TRUSTSTORE_PATH_SUFFIX
CERTIFICATE_PATH_LCM=$PATH_PREFIX/lcm-$CERTIFICATE_PATH_SUFFIX
ALIAS_UI=${2:-cert-ui}
ALIAS_LCM=${3:-cert-lcm}
STOREPASS=${4:-storepass}
KEYPASS=${5:-keypass}
DNAME_CN_UI=${6:-localhost}
DNAME_CN_LCM=${7:-localhost}
DNAME_OU=${8:-}
DNAME_O=${9:-}
DNAME_L=${10:-}
DNAME_S=${11:-}
DNAME_C=${12:-}
KEYALG=${13:-RSA}


function import_certificate {
	local alias="$1"
	local truststore="$2"
	local storepass="$3"
	local keypass="$4"
        local certificate="$5"
	keytool -import -noprompt -keystore "$truststore" -file "$certificate" -storepass "$storepass"
        #echo keytool -import -noprompt -keystore "$truststore" -file "$certificate" -storepass "$storepass"
}

#Inversion: the truststore of A contains the public certificate of B
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_LCM" "$TRUSTSTORE_PATH_UI" "$ALIAS_LCM" "$STOREPASS" "$KEYPASS" "$DNAME_CN_LCM" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG" "$CERTIFICATE_PATH_LCM"
import_certificate "$ALIAS_LCM" "$TRUSTSTORE_PATH_LCM" "$STOREPASS" "$KEYPASS" "$CERTIFICATE_PATH_LCM"
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_UI" "$TRUSTSTORE_PATH_LCM" "$ALIAS_UI" "$STOREPASS" "$KEYPASS" "$DNAME_CN_UI" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG" "$CERTIFICATE_PATH_UI"
import_certificate "$ALIAS_UI" "$TRUSTSTORE_PATH_UI" "$STOREPASS" "$KEYPASS" "$CERTIFICATE_PATH_LCM"


# Quick fix to remove not used the raw certificate.
rm "$CERTIFICATE_PATH_UI"
echo Unused files removed.
echo Setup finished.