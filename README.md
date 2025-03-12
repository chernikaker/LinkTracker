![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

# Установка и запуск приложения

## Требования

- Java 23
- Maven

## Установка

1. Клонируйте репозиторий:

   ```bash
   git clone https://github.com/central-university-dev/java-chernikaker.git
   cd <имя папки с репозиторием>

   ```
2. Создайте переменные окружения для конфигурации запуска модуля bot
   `` TELEGRAM_TOKEN`` - токен для телеграм бота
3. Создайте переменные окружения для конфигурации запуска модуля scrapper
   `` GITHUB_TOKEN`` - токен для клиента GitHub REST API (для количества запросов)

   `` SO_TOKEN_KEY`` - токен для клиента StackOverflow REST API  (для количества запросов)

   `` SO_ACCESS_TOKEN`` - токен доступа для клиента StackOverflow REST API

4. Соберите проект:

   ```bash
   mvn clean install

   ```
5. Запустите модули в IDE через соответствующие классы ``@SpringBootApplication``

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

Для дополнительной справки: [HELP.md](./HELP.md)
