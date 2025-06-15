# Архитектура проекта

![design.png](screenshots/design.png)
[design.mmd](design.mmd)

# Описание саги
![saga.png](screenshots/saga.png)
[saga.mmd](saga.mmd)

# Сборка и настройка проекта
* для сборки проекта необходимо собрать сначала все модули
используя команду ```mvn clean verify```
Настроена проверка на покрытие кода тестами в 70% c помощью jacoco плагина.
Если покрытие модуля меньше, то происходит ошибка:
![build_verify_0.png](screenshots/build_verify_0.png)
Если у всех модулей покрытие тестами не менее 70%, то сборка успешна:
![build_verify_1.png](screenshots/build_verify_1.png)
* далее необходимо запустить docker compose из корня проекта ```docker compose up```
* после старта Axon контейнера нужно зайти по адресу: http://localhost:8024/
выбрать "Start standalone node" и нажать "Сomplete"
![axon_1.png](screenshots/axon_1.png)
* после этого остальные микросервисы должны будут присоединиться к Axon серверу
![axon_2.png](screenshots/axon_2.png)
* после этого микросервисы готовы принимать запросы
* можно использовать ```requests.http``` из корня проекта для тестовых запросов:
![requests_1.png](screenshots/requests_1.png)
![requests_2.png](screenshots/requests_2.png)
![requests_3.png](screenshots/requests_3.png)
![requests_4.png](screenshots/requests_4.png)
![requests_5.png](screenshots/requests_5.png)
* можно зайти в jaeger ui по адресу http://localhost:16686/ 
 и указать в поле Tags фильтр по bookingId.
Т.к. bookingId является уникальным идентификатором агрегата, по которому Axon маршрутизирует команды. ```axon_aggregate_identifier=def6a67e-6ef2-4f10-9972-5065195e18cd```
* после фильтрации можно увидеть 2 трейса, это две разные саги. одна для бронирования билета, вторая для отмены бронирования
![jaeger_1.png](screenshots/jaeger_1.png)
* можно также посмотреть более подробную информацию 
![jaeger_2.png](screenshots/jaeger_2.png)
* в трейсах видна также обработка Kafka событий
![jaeger_3.png](screenshots/jaeger_3.png)
* содержимое топиков Kafka можно посмотреть в Kafdrop: http://localhost:9000/
![kafdrop_1.png](screenshots/kafdrop_1.png)
![kafdrop_2.png](screenshots/kafdrop_2.png)
![kafdrop_3.png](screenshots/kafdrop_3.png)
![kafdrop_4.png](screenshots/kafdrop_4.png)

* мониторинг grafana: http://localhost:3000/login
* дашборды для тестовых запросов
![grafana_1.png](screenshots/grafana_1.png)
![grafana_2.png](screenshots/grafana_2.png)
![grafana_3.png](screenshots/grafana_3.png)

* дашборды после запуска запросов с помощью jmeter:
TODO
# Описание компонентов проекта

> **agents**: \
> OpenTelemetry java agent для трейсов. Встраивается в каждый микросервис на этапе запуска. Динамически внедряет байт-код для сбора телеметрии.

> **auth-service**: \
> Есть два интерфейса взаимодействия: 1) REST для пользователей и 2) gRPC для внутренних сервисов.
> Для авторизации используются JWT токены.
> off-heap используется для загрузки в память файла скомпрометированных паролей, при регистрации
проверятся если пароль скомпрометирован, регистрация не проходит. Для этого используется BloomFilter из библиотеки guava.
> JMH бенчмарки выделены в отдельный модуль.
* gRPC сервис: auth-service/src/main/java/ru/otus/auth/grpc/AuthGrpcService.java
* Off-heap: auth-service/src/main/java/ru/otus/auth/offheap/MappedByteBufferStorageImpl.java
* BloomFilterManager: auth-service/src/main/java/ru/otus/auth/service/BloomFilterManager.java
* JwtService: auth-service/src/main/java/ru/otus/auth/service/JwtService.java
* JMH бенчмарки: auth-service-jmh-benchmark/src/main/java/ru/otus/benchmark/jmh/PasswordHashBenchmark.java
* Результаты бенчмарков: auth-service-jmh-benchmark/README.md

> **booking-orchestrator-service**: \
> Оркестратор сервис содержит основную логику для саги бронирования авиабилета и саги отмены авиабилета пользователем.
> Используется Axon фреймворк для реализации саги. 
> Также сервис содержит кастомные метрики: ```totalBookings, confirmedBookings, cancelledBookings``` которые отображаются на дашборде.
* Метрики: booking-orchestrator-service/src/main/java/ru/otus/orchestrator/metrics/BookingMetrics.java
* Сага бронирования авиабилета: booking-orchestrator-service/src/main/java/ru/otus/orchestrator/saga/BookingSaga.java
* Сага отмены авиабилета: booking-orchestrator-service/src/main/java/ru/otus/orchestrator/saga/BookingCancellationSaga.java

> **common**: \
> Модуль с общими dto для всех сервисов
* Saga команды: common/src/main/java/ru/otus/common/command
* Общие enums: common/src/main/java/ru/otus/common/enums
* Kafka события: common/src/main/java/ru/otus/common/kafka
* Requests: common/src/main/java/ru/otus/common/request
* Responses: common/src/main/java/ru/otus/common/response
* Saga события: common/src/main/java/ru/otus/common/saga

> **common-entity**: \
> Модуль с общими entity для flight-service и flight-query-service.
* common-entity/src/main/java/ru/otus/common/entity/Airport.java
* common-entity/src/main/java/ru/otus/common/entity/Flight.java

> **flight-query-service**: \
> Этот сервис был создан для реализации CQRS паттерна. Он предназначен только для чтения информации о рейсах.
> Он реализует принцип eventual consistency и синхронизирует информацию о рейсах через Kafka события.
> Для исключения повторной обработки событий реализован EventDeduplicationCache на основе ConcurrentHashMap и ConcurrentLinkedQueue.
> В качестве consumer используется Kafka Streams. Обрабатываются события: ```FlightCreatedEvent, FlightUpdatedEvent```.
> Также есть возможность поиска билетов.
> Есть кеширование данных о рейсах с помощью Caffeine и кастомные метрики
> ```cache_size, cache_requests_hit, cache_requests_miss``` которые отображаются на дашборде.
> Используется liquibase для создания таблиц и вставки dummy записей.
* Кеш для дедупликации событий: flight-query-service/src/main/java/ru/otus/flightquery/cache/EventDeduplicationCache.java
* Настройки Caffeine кеша и метрик: flight-query-service/src/main/java/ru/otus/flightquery/config/CacheConfig.java
* Контроллер со swagger аннотациями: flight-query-service/src/main/java/ru/otus/flightquery/controller/FlightQueryController.java
* Kafka Streams обработчик: flight-query-service/src/main/java/ru/otus/flightquery/processor/KafkaFlightStreamProcessor.java
* Liquibase миграционные скрипты: flight-query-service/src/main/resources/db/changelog/db.changelog-master.yaml

> **flight-service**: \
> Сервис для резервирования и освобождения мест на рейсах, добавления новых рейсов.
> Используется пессимистическая блокировка ```@Lock(LockModeType.PESSIMISTIC_WRITE)``` чтобы избежать проблемы race condition при большом количестве параллельных запросов к одному рейсу.
> Реализована возможность overbooking по формуле: ```freeSeats = (totalSeats * (1 + overbookingPercentage / 100)) - bookedSeats```.
> Участвует в саге. Публикует Kafka события ```FlightCreatedEvent, FlightUpdatedEvent```.
> Используется liquibase для создания таблиц и вставки dummy записей.
> Также добавлен constraint на уровне БД, чтобы не зарезервировать одно и тоже место дважды:
> ```@UniqueConstraint(name = "uc_flight_seat", columnNames = {"flight_number", "seat_number"})```
* Repository с PESSIMISTIC_WRITE для информации о рассадке мест: flight-service/src/main/java/ru/otus/flight/repository/BookingSeatMappingRepository.java
* Repository с PESSIMISTIC_WRITE для информации о рейсе: flight-service/src/main/java/ru/otus/flight/repository/FlightRepository.java
* Контроллер со swagger аннотациями: flight-service/src/main/java/ru/otus/flight/controller/FlightWriteController.java 
* Kafka producer: flight-service/src/main/java/ru/otus/flight/publisher/FlightPublisher.java
* Liquibase миграционные скрипты: flight-service/src/main/resources/db/changelog/db.changelog-master.yaml

> **helm**: \
> Для деплоя каждого сервиса используется deploy.sh скрипт и соответствующий values.yaml.
* helm/auth-service/deploy.sh
* helm/booking-orchestrator-service/deploy.sh
* helm/flight-query-service/deploy.sh
* helm/flight-service/deploy.sh
* helm/payment-service/deploy.sh
* helm/reservation-service/deploy.sh
* пример деплоя сервиса авторизации: helm/README.md

> **monitoring**: \
> Папка для хранения глобальных настроек prometheus и grafana. А также дашбордов, для provisioning.
* Настройки prometheus: monitoring/prometheus/prometheus.yml
* Настройки grafana: monitoring/grafana/provisioning/dashboards/dashboard.yaml
* Дашборд для метрик бронирования: monitoring/grafana/dashboards/booking_metrics.json
* Дашборд для метрик кеша поиска авиабилетов: monitoring/grafana/dashboards/cache-monitoring.json
* Дашборд для golden signals: monitoring/grafana/dashboards/golden-signals-dashboard.json

> **payment-service**: \
> Cервис для обработки платежей. Эмулирует вызов платежного провайдера по адресу "http://localhost:8080/mock-payments"
> через RestClient. Используется resilience4j библиотека.
> Участвует в саге. Публикует Kafka событиe ```PaymentEvent```.
> Используется liquibase для создания таблиц.
* Адаптер для проведения оплаты с ```@Retry, @CircuitBreaker, @RateLimiter```: payment-service/src/main/java/ru/otus/payment/client/PaymentClientAdapter.java
* RestClient:  payment-service/src/main/java/ru/otus/payment/client/PaymentClient.java
* Kafka producer: payment-service/src/main/java/ru/otus/payment/publisher/PaymentPublisher.java
* Liquibase миграционные скрипты: payment-service/src/main/resources/db/changelog/db.changelog-master.yaml

> **reservation-service**: \
> Cервис который запускает саги для резервации билета
> или отмену брони. Также обрабатывает сообщения от саги для синхронизации данных.
> Хранит и управляет информацией о бронированиях.
> Чтобы обеспечить высокую пропускную способность и гарантию доставки сообщений в Kafka,
> используется паттерн ```transactional outbox```. Сначала сервис сохраняет запрос от пользователя
> в таблицу ```outbox_events```. Далее Scheduler периодически вычитывает из таблицы 
> самые старые неотправленные события и публикует их в Kafka топик (```ReservationCreatedEvent, ReservationCancelledEvent```).
> Для исключения повторной обработки событий реализован EventDeduplicationCache на основе ConcurrentHashMap и ConcurrentLinkedQueue.
> Далее Kafka Streams consumer (processor) обрабатывает их и отправляет команды
> в сагу: ```BookFlightCommand, CancelFlightCommand``` .
> Используется liquibase для создания таблиц.
* Кеш для дедупликации событий: reservation-service/src/main/java/ru/otus/reservation/cache/EventDeduplicationCache.java
* Контроллер для резервирования/отмены билетов со swagger аннотациями: reservation-service/src/main/java/ru/otus/reservation/controller/ReservationController.java
* Outbox таблица: reservation-service/src/main/java/ru/otus/reservation/entity/BookingOutboxEvent.java
* Kafka Streams обработчик: reservation-service/src/main/java/ru/otus/reservation/processor/KafkaTicketStreamProcessor.java
* Scheduler: reservation-service/src/main/java/ru/otus/reservation/publisher/ReservationEventPublisher.java 
* Обработчик сага событий: reservation-service/src/main/java/ru/otus/reservation/saga/ReservationEventsHandler.java
* Сервис отправки команд в сагу: reservation-service/src/main/java/ru/otus/reservation/service/BookingProcessor.java
* Liquibase миграционные скрипты: reservation-service/src/main/resources/db/changelog/db.changelog-master.yaml


----------------------------------------------------

!скриншоты
!проверить работу на jmeter
!выключить circuitbreaker?


