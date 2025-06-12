#!/bin/bash

IN=$JAVA_HOME/lib/security/cacerts
OUT=/opt/java/openjdk/lib/security/cacerts
OUT=src/main/jib/$OUT
OUT_DIR=$(dirname $OUT)

mkdir -p $OUT_DIR
cp $IN $OUT
