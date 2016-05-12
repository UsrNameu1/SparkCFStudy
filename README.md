# SparkCFStudy

Study for Collaborative filtering in spark

## launch zeppelin


```
$ git clone https://github.com/apache/incubator-zeppelin.git
$ cd incubator-zeppelin
$ mvn clean package -Pspark-1.6 -Phadoop-2.4 -Pyarn -Ppyspark -Dscala-2.10 -DskipTests
$ ./bin/zeppelin-daemon.sh start
```

then import CFSampleNote.json notebook file
