# wheretolive-feed
It is a project to deploy:

1. feed-api: http actor to interact with the feed extractor
2. feed-cluster: clustered actors to deal with feed extraction
3. feed-kafka: kafka interface


## Start in Production

**1. Select the Network Interface**

```bash
$ docker run --rm -it data2knowledge/feed-cluster:0.2.1 network
```

**2. Start the Master**

```bash

$ docker run -d -it --name feed-master data2knowledge/feed-cluster:0.2.1 master ethwe \
  -Dkafka.zk-address="zoo-1:2181,zoo-2:2181,zoo-3:2181" \
  -Dkafka.brokers="kafka-1:9092,kafka-2:9092,kafka-3:9092"

```

**3. Start a Worker**
We can run more worker on different machines.

```bash

docker run -d -it --name <name-worker> data2knowledge/feed-cluster:0.2.1 worker ethwe <master_ip> <master_port> \
  -Dkafka.zk-address="zoo-1:2181,zoo-2:2181,zoo-3:2181" \
  -Dkafka.brokers="kafka-1:9092,kafka-2:9092,kafka-3:9092"
```

For example
```bash

  docker run -d -it --name feed-worker-7 data2knowledge/feed-cluster:0.2.1 worker ethwe 192.160.0.3 5000 \
    -Dkafka.zk-address="zoo-1:2181,zoo-2:2181,zoo-3:2181" \
    -Dkafka.brokers="kafka-1:9092,kafka-2:9092,kafka-3:9092"
```
please consider to give the right name of the zookeeper and kafka.

we can specify a config file at runtime using `-Dconfig.file="config/myapp.conf"`

**3. Start an instance of the Api**

```bash

$ docker run -d -it --name=feed-api-1 \
    data2knowledge/feed-api:0.2 <ip_master> <port>
```

Example

```bash

$ docker run -d -it --name=feed-api \
    data2knowledge/feed-api:0.2 192.160.0.3 5000
```



## Local Development

**1. Start a local zookeeper and kafka with docker**
```bash

setup a 
```

**2. Run with Sbt ~reStart**

For some subfolder in the main folder it is possible to start a microservice.

##### Start the Master

```bash

$ sbt
$ project cluster
$ ~reStart master eth0

```

##### Start a Worker

```bash

$ sbt 
$ project api
$ ~restart worker eth0 <ip_master> <port>

```

##### Start the api

```bash

$ sbt
$ project cluster
$ ~reStart 192.168.1.4 5000
```


## Docker Test

**1. Select the Network Interface**

```bash
$ docker run --rm -it data2knowledge/feed-cluster:0.2 network
```

**2. Start the Master**

```bash
$ docker run --rm -it dtk/feed-cluster:0.2 master <network_name>

```

**3. Start a Worker**
We can run more worker on different machines.

```bash
$ docker run --rm -it --name worker1 \
    --link local_kafka_1:kafkahost --link local_zookeeper_1:zkhost \
    data2knowledge/feed-cluster:0.2 worker <network_name> <master_ip> <master_port>
```
please consider to give the right name of the zookeeper and kafka.

Example:

```bash
$ docker run --rm -it --name worker1 \
    --link local_kafka_1:kafkahost --link local_zookeeper_1:zkhost \
    data2knowledge/feed-cluster:0.2 worker eth0 172.17.0.20 5000
```

we can specify a config file at runtime using `-Dconfig.file="config/myapp.conf"`

**3. Start an instance of the Api**

```bash

$ docker run --rm -it -name api1 \
    data2knowledge/feed-api:0.2 <ip_master> <port>
```

## Deploy to docker hub

1. login with your credential command line
2. in sbt type the command `docker:publish`

## Tool to monitor kafka

docker run -it -d -p 8888:8888 -e ZOOKEEPERS="zkhost:2181" chatu/trifecta