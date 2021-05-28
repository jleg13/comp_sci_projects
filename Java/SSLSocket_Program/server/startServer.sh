#/bin/sh

# remove the db.txt file each run of the server for the application demo
rm db.txt

javac Server.java 

java -Djavax.net.ssl.trustStore=samplecacerts \
     -Djavax.net.ssl.trustStorePassword=changeit \
     Server $1