#!/bin/bash

sh init.sh

start-master.sh
start-slaves.sh

spark-submit --class com.novikov.Application ../lab5-1.0-SNAPSHOT.jar spark-master /logs
