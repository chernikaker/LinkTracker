
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
- Docker и Docker Compose

## Установка

1. Клонируйте репозиторий:

   ```bash
   git clone https://github.com/central-university-dev/java-chernikaker.git
   cd <имя папки с репозиторием>
   ```
   
2. Запустите сервисы через Docker Compose (в корне проекта):

```bash
docker-compose up -d
```
Это запустит PostgreSQL. Миграции применятся автоматически

3. Создайте переменные окружения для конфигурации запуска модуля bot
   `` TELEGRAM_TOKEN`` - токен для телеграм бота
4. Создайте переменные окружения для конфигурации запуска модуля scrapper
   `` GITHUB_TOKEN`` - токен для клиента GitHub REST API (для количества запросов)

   `` SO_TOKEN_KEY`` - токен для клиента StackOverflow REST API  (для количества запросов)

   `` SO_ACCESS_TOKEN`` - токен доступа для клиента StackOverflow REST API

5. Соберите проект:

   ```bash
   mvn clean install
   ```
6. Запустите модули в IDE через соответствующие классы ``@SpringBootApplication``

## Тестирование
Для запуска тестов (с использованием Testcontainers):

```bash
mvn test
```
Тесты автоматически поднимают PostgreSQL в контейнере

Для дополнительной справки: [HELP.md](./HELP.md)
