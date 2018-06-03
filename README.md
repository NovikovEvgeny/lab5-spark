1. Создал 2 виртуальных докер-машины (docker-machine create vmName)
2. Докер-файл
3. Создал образы на обеих машинах ("docker build -f spark.df -t spark-lab5-last ."), переключение между машинами: $ eval "$(docker-machine env VMNAME)"
3.1. чтобы не использовать докер-хаб и локальный репозиторий, но в то же время не использовать docker build на обеих машинах, можно сохранить образ как файл на одной машине "docer save -o PATH_TO_FILE IMAGE_NAME" и загрузить на другой "docker load -i PATH_TO_IMAGE" 
4. init swarm: "docker swarm init" на master VM
5. подключить воркер-машину: docker swarm join --token SWMTKN-1-1da5t7bxt9a36avgprpaernb3fn2ncnsjd6lppcugeja37ta5a-90beq8otup0cdvecax8k9iwfm 192.168.99.100:2377   (токен, очевидно, будет другой)
6. Создал docker-compose 
7. запустить docker stack: docker stack deploy -c docker-compose.yml spark-lab5-last
8. Необходимо понять, на какой докер-машине запущен контейнер с "мастером": "docker container ls"
9. поключиться к этой машине: "docker exec -it MASTER_CONTAINER_ID bash"

10. Standalone режим: /usr/local/scripts/standalone.sh
11. Yarn режим: /usr/local/scripts/yarn.sh
12. вывод результатов: /usr/local/scripts/print_results.sh
13. очистка и остановка: stop.sh


В stanalone режиме, чтобы запустить воркеров на другой (не мастере) докер-машине, необходимо:

* удалить строку "start-saves.sh" из скрипта "standalone.sh" (sed -i s/start-slaves.sh/''/ standalone.sh)
* запустить спарк-воркеров на воркер-машине: "$SPARK_HOME/sbin/start-slave.sh spark://spark-master:7077" (проверить, что воркер поднялся: http://192.168.99.101:8081/ )
* запустить standalone.sh на мастер-машине 
* enjoy!

* проверить состояние Spark: http://192.168.99.101:8080/

P.S. IP контейнеров может, конечно, отличаться