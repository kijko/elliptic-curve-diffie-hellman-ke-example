#! /bin/sh

set -e

javac -g pl/edu/prz/kijko/ECDHExample.java
javac -g pl/edu/prz/kijko/Point.java
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 pl/edu/prz/kijko/ECDHExample

