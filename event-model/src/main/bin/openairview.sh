#!/bin/sh
#
# OpenAir ModelHtmlOutput execution script for Linux
#
if [ $# != 2 ]; then
   echo "USAGE: openairview.sh <event-file> <browser-program>"
   exit 1
fi
TMP_DIR="/tmp/openair-event-htmloutput-$$"
rm -rf $TMP_DIR
echo "OpenAir program viewer"
echo "Generating HTML to $TMP_DIR ..."
CLASSPATH="../lib/joda-time-1.6.2.jar"
CLASSPATH="$CLASSPATH:../lib/openair-event-model.jar"
CLASSPATH="$CLASSPATH:../lib/xmlParserAPIs-2.6.2.jar"
CLASSPATH="$CLASSPATH:../lib/xpp3-1.1.4c.jar"
java -cp $CLASSPATH sk.linhard.openair.eventmodel.util.ModelHtmlOutput $1 $TMP_DIR
$2 $TMP_DIR/event.html
