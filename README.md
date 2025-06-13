# Архитектура проекта



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
> TODO: add dashboards

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


TODO: add screens of diagrams, dashboards, traces, axon, kafkadrop, add instruction how to start project(run axon in standalone mode)

mvn clean verify - проверить 70% покрытие тестами
Rule violated for bundle flight-query-service: instructions covered ratio is 0.04, but expected minimum is 0.70
http://localhost:8024/ - axon server

http://localhost:16686/ - jaeger ui
axon_aggregate_identifier=854ed379-11ec-4044-a869-4e0f2f2aa013

http://localhost:3000/login - grafana

----------------------------------------------------

!скриншоты
!проверить работу системы в целом отдельными запросами из requests
!проверить работу на jmeter
!выключить circuitbreaker?


!!!5. В отдельной папке проекта размещаются helm скрипты для деплоя каждого приложения (в подпапках)

+7. Для всех эндпоинтов должны быть запросы с применением JMeter

+8. В приложениях должны быть кеши, для хранения справочных данных из БД. Работа с кешами подразумевает использование пакета java.util.concurrent

+9. В приложениях должны быть метрики, чтобы можно было в мониторинге посчитать:

а) Для сервиса: rps, число обращений, latency, успешные/неуспешные завершения (+ свои метрики)

б) Для приложения: размер кешей, число обработанных ошибок (+ свои метрики)

+10. Построить в графане дашборды для отображения метрик

!!!11. Применять шаблоны отказоустойчивых сервисов:

а) При обращении к upstream сервисам делать повторы

б) При обработке запросов обеспечить защиту от перегрузки


