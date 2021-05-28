#/bin/sh

javac Client.java 

java -Djavax.net.ssl.trustStore=samplecacerts \
     -Djavax.net.ssl.trustStorePassword=changeit \
     Client $1 $2 $3