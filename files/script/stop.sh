#!/bin/bash

stop-master.sh
stop-slaves.sh

stop-yarn.sh

hdfs dfs -rm -r -f /task1 /task2 /task3   

stop-dfs.sh

# change to "master" in order to run init.sh again
sed -i s/$HOSTNAME/master/ $HADOOP_CONF_DIR/core-site.xml
sed -i s/$HOSTNAME/master/ $HADOOP_CONF_DIR/yarn-site.xml