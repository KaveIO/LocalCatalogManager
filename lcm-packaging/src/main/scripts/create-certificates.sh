#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PATH_PREFIX=${1:-$DIR/../../main/resources}

KEYSTORE_PATH_SUFFIX=keystore.jks
TRUSTSTORE_PATH_SUFFIX=truststore.jks

KEYSTORE_PATH_UI=${1:-$PATH_PREFIX/ui-$KEYSTORE_PATH_SUFFIX}
TRUSTSTORE_PATH_UI=${2:-$PATH_PREFIX/ui-$TRUSTSTORE_PATH_SUFFIX}
KEYSTORE_PATH_LCM=${3:-$PATH_PREFIX/lcm-$KEYSTORE_PATH_SUFFIX}
TRUSTSTORE_PATH_LCM=${4:-$PATH_PREFIX/lcm-$TRUSTSTORE_PATH_SUFFIX}
ALIAS_UI=${5:-cert-ui}
ALIAS_LCM=${6:-cert-lcm}
STOREPASS=${7:-storepass}
KEYPASS=${8:-keypass}
DNAME_CN_UI=${9:-localhost}
DNAME_CN_LCM=${10:-localhost}
DNAME_OU=${11:-}
DNAME_O=${12:-}
DNAME_L=${13:-}
DNAME_S=${14:-}
DNAME_C=${15:-}
KEYALG=${16:-RSA}

#Inversion: the truststore of A contains the public certificate of B
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_UI" "$TRUSTSTORE_PATH_LCM" "$ALIAS_UI" "$STOREPASS" "$KEYPASS" "$DNAME_CN_UI" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG"
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_LCM" "$TRUSTSTORE_PATH_UI" "$ALIAS_LCM" "$STOREPASS" "$KEYPASS" "$DNAME_CN_LCM" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG"
 