FROM java:openjdk-8

#Update all
#Install ssh && rsync
#Generate ssh key
#Add the created key to the authorized list
RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get -y install ssh
RUN apt-get -y install rsync
RUN apt-get -y install software-properties-common

RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P '' && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
COPY files/conf/ssh_conf /usr/local/ssh_conf
RUN mv /usr/local/ssh_conf $HOME/.ssh/config

WORKDIR /usr/local/

# HADOOP 3.0.2
RUN wget http://apache-mirror.rbc.ru/pub/apache/hadoop/common/hadoop-3.0.2/hadoop-3.0.2.tar.gz && \
    tar xzf hadoop-3.0.2.tar.gz && \
    mv hadoop-3.0.2 hadoop && \
    rm -rf hadoop-3.0.2.tar.gz
ENV HADOOP_HOME=/usr/local/hadoop
ENV HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

# SPARK 2.3.0
RUN wget http://apache-mirror.rbc.ru/pub/apache/spark/spark-2.3.0/spark-2.3.0-bin-hadoop2.7.tgz && \
    tar xzf spark-2.3.0-bin-hadoop2.7.tgz && \
    mv spark-2.3.0-bin-hadoop2.7 spark && \
    rm -rf spark-2.3.0-bin-hadoop2.7.tgz
ENV SPARK_HOME=/usr/local/spark
ENV PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

# NASA LOGS
RUN wget ftp://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz && \
    gzip -d NASA_access_log_Jul95.gz

# CONF FILES & permissions
COPY files/conf/hadoop/* $HADOOP_CONF_DIR/
COPY files/conf/spark/* $SPARK_HOME/conf/

RUN chmod +x $HADOOP_CONF_DIR/hadoop-env.sh && \
    chmod +x $HADOOP_HOME/sbin/start-dfs.sh && \
    chmod +x $HADOOP_HOME/sbin/start-yarn.sh

COPY files/script/* /usr/local/scripts/
RUN chmod +x /usr/local/scripts/init.sh
RUN chmod +x /usr/local/scripts/print_results.sh
RUN chmod +x /usr/local/scripts/standalone.sh
RUN chmod +x /usr/local/scripts/yarn.sh
RUN chmod +x /usr/local/scripts/stop.sh

RUN mkdir -p /app/hadoop/tmp && \
    mkdir -p /usr/local/hadoop_store/hdfs/namenode && \
    mkdir -p /usr/local/hadoop_store/hdfs/datanode
RUN hdfs namenode -format

COPY files/application/lab5-1.0-SNAPSHOT.jar .
RUN chmod +x /usr/local/lab5-1.0-SNAPSHOT.jar

ENV SPARK_WORKER_INSTANCES=3

EXPOSE 9000 9001 4040 8088 7077
CMD [ "/bin/bash", "-c", "service ssh start; tail -f /dev/null"]