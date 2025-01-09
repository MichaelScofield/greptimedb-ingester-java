#!/bin/bash

PROTO_ROOT=.
JAVA_OUTPUT=../../java/

protoc --plugin=protoc-gen-grpc-java=/Users/luofucong/bin/protoc-gen-grpc-java-1.69.0-osx-aarch_64.exe \
  -I=${PROTO_ROOT} --java_out=${JAVA_OUTPUT} --grpc-java_out=${JAVA_OUTPUT} export.proto
