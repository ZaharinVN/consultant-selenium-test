# Consultant Plus Selenium Test (Java + TestNG + Allure)

## Требования
- JDK 17+
- Maven
- Chrome (последняя версия)

## Запуск тестов
1. Импортировать проект как Maven‑проект в IntelliJ IDEA.
2. В терминале выполнить:
   ```bash
   mvn clean test
   ```
3. После прогона тестов сгенерировать Allure‑отчёт:
   ```bash
   mvn allure:serve
   ```
   или:
   ```bash
   mvn allure:report
   ```
   Затем открыть в браузере: `target/site/allure-maven-plugin/index.html`.

## Примечания
- Если сайт запросит капчу — пройти её вручную, затем повторить запуск теста.
- При необходимости увеличить таймаут в `utils/Config.java` (TIMEOUT_SECONDS).
- Локаторы в Page Object могут требовать уточнения по реальной вёрстке consultant.ru.

## Структура проекта
- `src/test/java/tests` — тестовые классы.
- `src/test/java/pages` — Page Object.
- `src/test/java/utils` — утилиты (драйвер, Allure, конфиг).
- `src/test/resources` — testng.xml, allure.properties.