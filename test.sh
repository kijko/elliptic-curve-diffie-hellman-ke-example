#! /bin/sh

set -e

javac pl/edu/prz/kijko/ECDHExample.java
javac pl/edu/prz/kijko/Point.java
javac pl/edu/prz/kijko/Main.java
java pl/edu/prz/kijko/Main test

