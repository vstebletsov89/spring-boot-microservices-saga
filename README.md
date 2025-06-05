# Описание системы:

* Booking Orchestrator Service выступает как управляющий сервис саги.
* Reservation Service хранит и управляет информацией о бронированиях.
* Flight Service отвечает за резервирование и освобождение мест на рейсах.
* Payment Service проводит оплату.
* Flight Query Service не участвует в оркестровке (только для чтения).
* Auth Service - сервис авторизации

TODO: add saga screen

mvn clean verify - проверить 70% покрытие тестами
Rule violated for bundle flight-query-service: instructions covered ratio is 0.04, but expected minimum is 0.70
http://localhost:8024/ - axon server

http://localhost:16686/ - jaeger ui
axon_aggregate_identifier=854ed379-11ec-4044-a869-4e0f2f2aa013

http://localhost:3000/login - grafana

----------------------------------------------------

TODO:

run integration tests

!!!GRPC to auth
!!!добавить helm скрипты
!!!offheap storage?


!проверить работу системы в целом отдельными запросами из requests
!проверить работу на jmeter
!!!выключить circuitbreaker?

добавить в readme какие темы покрыты с ссылками на код (сервис:к
ласс:метод или docker compose?)

!!!5. В отдельной папке проекта размещаются helm скрипты для деплоя каждого приложения (в подпапках)

https://github.com/moryakovdv/otus-project - check helm scripts

+7. Для всех эндпоинтов должны быть запросы с применением JMeter

+8. В приложениях должны быть кеши, для хранения справочных данных из БД. Работа с кешами подразумевает использование пакета java.util.concurrent

+9. В приложениях должны быть метрики, чтобы можно было в мониторинге посчитать:

а) Для сервиса: rps, число обращений, latency, успешные/неуспешные завершения (+ свои метрики)

б) Для приложения: размер кешей, число обработанных ошибок (+ свои метрики)

+10. Построить в графане дашборды для отображения метрик

!!!11. Применять шаблоны отказоустойчивых сервисов:

а) При обращении к upstream сервисам делать повторы

б) При обработке запросов обеспечить защиту от перегрузки


