#!/bin/sh
java -Xms1024m -Xmx7680m -verbose:gc -jar ./target/neddybenchmark-1.0.0-jar-with-dependencies.jar $1
