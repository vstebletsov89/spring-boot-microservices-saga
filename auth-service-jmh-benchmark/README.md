## Реализация benchmark для алгоритма хеширования

```console 
mvn clean package
```

```console 
java -jar target/benchmarks.jar
```
![run_hash_benchmarks.png](src/main/resources/run_hash_benchmarks.png)

Результаты:

![results_hash_benchmarks.png](src/main/resources/results_hash_benchmarks.png)
![results_hash_benchmarks_2.png](src/main/resources/results_hash_benchmarks_2.png)
Throughput (операций в секунду):

- MD5: примерно 4,99×10^6 ops/s
- SHA-256: примерно 5,02×10^6 ops/s
- SHA-512: примерно 2,15×10^6 ops/s (самый медленный)

Average Time (среднее время на операцию):

- MD5 и SHA-256 – примерно 10^-7 секунд на операцию
- SHA-512 – примерно 10^-6 секунд на операцию
Это подтверждает, что на каждый вызов хеширования SHA-512 тратит около 10 раз больше времени, чем MD5 и SHA-256.

Single Shot Time:
В режиме single shot (ss) для всех алгоритмов время исполнения оценивается примерно как 10^-4 секунд на операцию.

Вывод: оптимальнее всего использовать "SHA-256" алгоритм для хеширования и криптографической подписи. 