mvn clean verify - проверить 70% покрытие тестами
Rule violated for bundle flight-query-service: instructions covered ratio is 0.04, but expected minimum is 0.70
----------------------------------------------------

TODO:

!тесты для flight-query-service (FlightSyncService)
!все сервисы в docker
!добавить swagger
!добавить java agent для трейсов
!добавить grafana + prometheus

!проверить интеграцию flight сервисов, что события синхронизируются

!добавить avro и schema registry (поместить avro в common?)
!нарисовать диаграмму

!helm, k8s
!notification-service?
!discovery-service?
!api-gateway?



---

### ✅ **1. `common` модуль**
**Тип:** Library  
**Назначение:**
- Общие команды, события, DTO, value-объекты.
- Используется всеми микросервисами.

---

### ✅ **2. `ticket-service`**
**Тип:** Command-side  
**Роль:**
- Начальная точка пользовательского запроса на бронирование билета.
- Обрабатывает `BookFlightCommand`.
- Хранит `BookingAggregate` (или делегирует).

---

### ✅ **3. `booking-saga-service`**
**Тип:** Orchestrator  
**Роль:**
- Запускает и управляет сагой (`@Saga`).
- Отправляет команды в другие микросервисы.
- Реагирует на события (`@SagaEventHandler`) и принимает решения (например, завершить/отменить бронирование).

---

### ✅ **4. `seat-service`**
**Тип:** Command-side  
**Роль:**
- Отвечает за проверку и резервирование мест.
- Хранит агрегат `SeatInventory`.
- Обрабатывает `ReserveSeatCommand`, публикует `SeatReservedEvent` или `SeatReservationFailedEvent`.

---

### ✅ **5. `ticket-read-service`** (или `booking-query-service`)
**Тип:** Query-side  
**Роль:**
- Обрабатывает `@QueryHandler` и предоставляет клиенту данные по бронированию, рейсам и т.д.
- Отдельная БД, оптимизированная под чтение.
- Слушает события, например, `FlightBookedEvent`, `BookingConfirmedEvent`, `BookingCancelledEvent` и обновляет `projection` модели.

---

### ✅ **6. `flight-service`** (минимально)
**Тип:** Командно-ориентированный  
**Роль:**
- Отвечает за бизнес-логику рейсов (создание, отмена, изменение).
- Может быть полезен отдельно от seat-service для изоляции обязанностей.

---

### ✅ **7. `user-service`** (опционально)
**Тип:** Авторизация / управление пользователями  
**Роль:**
- Хранение информации о пользователях, роли, предпочтения.

---

### ✅ **8. `axon-server`**
**Тип:** Infrastructure  
**Роль:**
- Шина сообщений (команды/события/запросы).
- Поддержка Saga и EventStore (если используется как event store).

---

### ✅ **9. `kafka` **
Если хочешь использовать `Kafka` как event bus или outbox pattern, особенно для сторонней интеграции.

---

### ✨ Опционально 
| Сервис                      | Назначение |
|----------------------------|------------|
| **notification-service**   | Рассылка e-mail/SMS о бронировании |
| **payment-service**        | Обработка платежей |
| **api-gateway**            | Единая точка входа |
| **config-service**         | Централизованная конфигурация |
| **discovery-service**      | Service registry (например, Eureka) |
| **monitoring/metrics**     | Prometheus + Grafana или аналог |
| **logging/tracing**        | ELK stack, Zipkin, OpenTelemetry |




REST POST /api/tickets
|
v
TicketController
|
+--> Kafka -> topic: ticket-service.inbound
|
v
Kafka Streams (Processor)
|
+--> produce to ticket-service.outbound
|
v
TicketEventConsumer
|
+--> commandGateway.send(new BookFlightCommand(...))


booking-query-service

flight-query-service
добавить инициализацию 10 рейсов
поиск по конкретным датам
по месяцу
по городу
все фильтры как на скайсканнере

доп функции к остальным сервисам?

kafka streams

Где	Transactional нужно? везде где сохранение в БД
SeatService	✅ Да
BookingProjection	✅ Да

добавить все сервисы в docker и docker compose

переключени на Postgresql

Вот верная, чёткая и подробная диаграмма **Booking Saga**, полностью соответствующая вашему коду:

```
FlightBookedEvent (Старт Саги)
          │
          ▼
Команда ReserveSeatCommand ────▶ Seat Service (резервирование мест)
          │                                      │
          │                                      ├── SeatReservedEvent ────▶ ProcessPaymentCommand ────▶ Payment Service
          │                                      │                                      │
          │                                      │                                      ├── PaymentProcessedEvent ────▶ ConfirmBookingCommand ────▶ Booking Service (Конец Саги)
          │                                      │                                      │
          │                                      │                                      └── PaymentFailedEvent ────▶ CancelBookingCommand ────▶ Booking Service
          │                                      │                                                              │
          │                                      │                                                              └── ReleaseSeatCommand ────▶ Seat Service (освобождение места) (Конец Саги)
          │                                      │
          │                                      └── SeatReservationFailedEvent ────▶ CancelBookingCommand ────▶ Booking Service (Конец Саги)
```

### Пояснения:
- **Happy Path** (успешный сценарий):
    - FlightBookedEvent → ReserveSeatCommand → SeatReservedEvent → ProcessPaymentCommand → PaymentProcessedEvent → ConfirmBookingCommand.

- **Ошибка платежа**:
    - FlightBookedEvent → ReserveSeatCommand → SeatReservedEvent → ProcessPaymentCommand → PaymentFailedEvent → CancelBookingCommand и ReleaseSeatCommand.

- **Ошибка резервирования места**:
    - FlightBookedEvent → ReserveSeatCommand → SeatReservationFailedEvent → CancelBookingCommand.

Это полностью отражает реализацию вашего кода саги и корректные сценарии, обработку успешных и неуспешных случаев.