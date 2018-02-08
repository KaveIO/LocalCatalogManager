#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PATH_PREFIX=$DIR/../../main/resources
FORCE_OVERWRITE=FALSE

for i in "$@"
do
case $i in
    --force-overwrite)
    FORCE_OVERWRITE=TRUE
    shift;
    shift;
    ;;
    -dir=*)
    PATH_PREFIX="${i#*=}"
    shift;
    ;;
*)
;;
esac
done

KEYSTORE_PATH_SUFFIX=keystore.jks
TRUSTSTORE_PATH_SUFFIX=truststore.jks
CERTIFICATE_PATH_SUFFIX=certificate.cer

KEYSTORE_PATH_UI=$PATH_PREFIX/ui-$KEYSTORE_PATH_SUFFIX
TRUSTSTORE_PATH_UI=$PATH_PREFIX/ui-$TRUSTSTORE_PATH_SUFFIX
CERTIFICATE_PATH_UI=$PATH_PREFIX/ui-$CERTIFICATE_PATH_SUFFIX
KEYSTORE_PATH_LCM=$PATH_PREFIX/lcm-$KEYSTORE_PATH_SUFFIX
TRUSTSTORE_PATH_LCM=$PATH_PREFIX/lcm-$TRUSTSTORE_PATH_SUFFIX
CERTIFICATE_PATH_LCM=$PATH_PREFIX/lcm-$CERTIFICATE_PATH_SUFFIX
ALIAS_UI=cert-ui
ALIAS_LCM=cert-lcm
STOREPASS=storepass
KEYPASS=keypass
DNAME_CN_UI=localhost
DNAME_CN_LCM=localhost
DNAME_OU=
DNAME_O=
DNAME_L=
DNAME_S=
DNAME_C=
KEYALG=RSA


function import_certificate {
	local alias="$1"
	local truststore="$2"
	local storepass="$3"
	local keypass="$4"
        local certificate="$5"
	keytool -import -noprompt -keystore "$truststore" -file "$certificate" -storepass "$storepass"
        #echo keytool -import -noprompt -keystore "$truststore" -file "$certificate" -storepass "$storepass"
}

function clean {
    rm -f $KEYSTORE_PATH_UI
    rm -f $TRUSTSTORE_PATH_UI
    rm -f $CERTIFICATE_PATH_UI
    rm -f $KEYSTORE_PATH_LCM
    rm -f $TRUSTSTORE_PATH_LCM
    rm -f $CERTIFICATE_PATH_LCM
    echo old files deleted sucessfully!.
}

if [[ -f $KEYSTORE_PATH_LCM && $FORCE_OVERWRITE == "FALSE" ]];
then
    echo "One or more certificate files already exists! Start the script with \"--force-overwrite\" to force certificate creation."
    exit 1
fi

if [[ $FORCE_OVERWRITE == "TRUE" ]];
then
    clean
fi

#Inversion: the truststore of A contains the public certificate of B
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_LCM" "$TRUSTSTORE_PATH_UI" "$ALIAS_LCM" "$STOREPASS" "$KEYPASS" "$DNAME_CN_LCM" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG" "$CERTIFICATE_PATH_LCM"
import_certificate "$ALIAS_LCM" "$TRUSTSTORE_PATH_LCM" "$STOREPASS" "$KEYPASS" "$CERTIFICATE_PATH_LCM"
"$DIR/create-certificate.sh" "$KEYSTORE_PATH_UI" "$TRUSTSTORE_PATH_LCM" "$ALIAS_UI" "$STOREPASS" "$KEYPASS" "$DNAME_CN_UI" "$DNAME_OU" "$DNAME_O" "$DNAME_L" "$DNAME_S" "$DNAME_C" "$KEYALG" "$CERTIFICATE_PATH_UI"
import_certificate "$ALIAS_UI" "$TRUSTSTORE_PATH_UI" "$STOREPASS" "$KEYPASS" "$CERTIFICATE_PATH_LCM"

# Quick fix to remove not used the raw certificate.
rm "$CERTIFICATE_PATH_UI"
echo Unused files removed.
echo Setup finished.
