# HELM

* создаем локальный docker registry:

```shell
docker run -d -p 5000:5000 --name local-registry registry:3
```
![helm_1.png](helm_1.png)

* добавляем insecure-registries в конфиг
  "insecure-registries": ["localhost:5000"]

* проверяем registry
```shell
  curl http://localhost:5000/v2/_catalog
```
![helm_2.png](helm_2.png)

* Проверяем доступность локального кластера:

```shell
kubectl get nodes
```
![helm_3.png](helm_3.png)

* делаем деплой сервиса auth-service (для остальных сервисов такой же скрипт лежит в соответсвующей папке)
* переходим сначало в git bash консоль
```shell
cd helm
./auth-service/deploy.sh
```
![helm_4.png](helm_4.png)
![helm_5.png](helm_5.png)
![helm_6.png](helm_6.png)

* смотрим есть ли под
```shell
kubectl get pods -n microservices
```
![helm_7.png](helm_7.png)

* Смотрим внешний ip
```shell
kubectl get svc -n microservices
```
![helm_8.png](helm_8.png)

* для удобства делаем port forward
```shell
kubectl port-forward svc/auth-service-chart 8090:8090 -n microservices
```
![helm_9.png](helm_9.png)

* Выполняем запросы из requests.http, под доступен и отвечает на запросы

![helm_10.png](helm_10.png)
![helm_11.png](helm_11.png)

* удаление сервиса
```shell
helm uninstall auth-service -n microservices
```