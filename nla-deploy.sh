#!/bin/sh
dest="$1"
mvn package
mv target/lib $dest/lib
mv target/*.jar $dest
