#!/bin/bash

KEYSTORE_PATH=${1:-keystore.jks}
TRUSTSTORE_PATH=${2:-cacerts.jks}
ALIAS=${3:-cert}
STOREPASS=${4:-storepass}
KEYPASS=${5:-keypass}
DNAME_CN=${6:-localhost}
DNAME_OU=${7:-D&A}
DNAME_O=${8:-KPMG Advisory N.V.}
DNAME_L=${9:-Amstelveen}
DNAME_S=${10:-NH}
DNAME_C=${11:-NL}
KEYALG=${12:-RSA}
CERTIFICATE_PATH=${13:-certificate.cer}

function create_certificate {
	local alias="$1"
	local dname_cn="$2"
	local dname_ou="$3"
	local dname_o="$4"
	local dname_l="$5"
	local dname_s="$6"
	local dname_c="$7"
	local keystore="$8"
	local storepass="$9"
	local keypass="${10}"
	local keyalg="${11}"
        local certificate="${12}"
	keytool -genkey -noprompt -alias "$alias" -dname "CN=$dname_cn, OU=$dname_ou, O=$dname_o, L=$dname_l, S=$dname_s, C=$dname_c" -keystore "$keystore" -storepass "$storepass" -keypass "$keypass" -keyalg "$keyalg" -validity 360 -keysize 2048
#echo keytool -genkey -noprompt -alias "$alias" -dname "CN=$dname_cn, OU=$dname_ou, O=$dname_o, L=$dname_l, S=$dname_s, C=$dname_c" -keystore "$keystore" -storepass "$storepass" -keypass "$keypass" -keyalg "$keyalg" -validity 360 -keysize 2048
echo key store generate successfully. Keystore: "$keystore".
        keytool -export -alias "$alias" -storepass "$storepass" -rfc -file "$certificate" -keystore "$keystore"
#echo    keytool -export -alias "$alias" -storepass "$storepass" -rfc -file "$certificate" -keystore "$keystore"
}

create_certificate "$ALIAS" "$DNAME_CN" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYSTORE_PATH" "$STOREPASS" "$KEYPASS" "$KEYALG" "$CERTIFICATE_PATH"
