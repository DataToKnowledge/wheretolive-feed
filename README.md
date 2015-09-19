# wheretolive-feed
It is a project to deploy:

1. feed-api: http actor to interact with the feed extractor
2. feed-cluster: clustered actors to deal with feed extraction
3. feed-kafka: kafka interface


## Local Development

**1. Start the local zookeeper and kafka with docker**
```bash



```

**2. **


## Docker Test

**1. Select the Network Interface**

```bash
$ docker run --rm -it dtk/feed-cluster:0.1 network
``

**1. Start the Master**

```bash
$ docker run --rm -it dtk/feed-cluster:0.1 master <network_name>

```

**2. Start a Worker**
We can run more worker on differnt machines.

```bash
$ docker run --rm -it --name worker1 \
    --link local_kafka_1:kafkahost --link local_zookeeper_1:zkhost \
    dtk/feed-cluster:0.1 worker <network_name> <master_ip> <master_port>
```
please consider to give the right name of the zookeeper and kafka.

Example:

```bash
$ docker run --rm -it --name worker1 \
    --link local_kafka_1:kafkahost --link local_zookeeper_1:zkhost \
    dtk/feed-cluster:0.1 worker eth0 172.17.0.20 5000
```