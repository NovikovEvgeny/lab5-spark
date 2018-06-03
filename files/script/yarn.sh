#!/bin/bash

sh init.sh

start-yarn.sh

spark-submit --class com.novikov.Application \
             --master yarn \
             --deploy-mode client \
             ../lab5-1.0-SNAPSHOT.jar spark-master /logs
