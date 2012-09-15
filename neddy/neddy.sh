#!/bin/sh
java -server -Xms1024m -Xmx4096m -verbose:gc -jar ./target/neddy-1.0.0-jar-with-dependencies.jar
