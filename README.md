# Домашнее задание №2 — тестирование REST и GraphQL API

Проект содержит API-автотесты на Java с использованием JUnit 5
и RestAssured. Сборка и запуск выполняются через Maven.

## Требования

- JDK 21.
- Maven 3.9 или новее.
- Доступ к интернету для загрузки зависимостей и обращения к учебным API.

## Используемые API

- REST: https://playground.nileslabs.com/api/v1
- GraphQL: https://graphql.postman-echo.com/graphql

## Запуск

Из корня проекта:

```bash
mvn test
```

Запуск отдельного класса:

```bash
mvn test -Dtest=PostsTest
mvn test -Dtest=AuthTest
mvn test -Dtest=GraphQlTest
```

## Реализованные сценарии

1. Создание поста через POST, извлечение id, изменение title через PATCH
   и получение результата через GET. Проверяются идентификатор,
   новый заголовок и сохранение неизменённых полей.
2. Получение access_token через login и передача его в /auth/me.
   Идентификатор, username и email сравниваются с ответом login.
3. Отказ в доступе к /auth/me без токена.
4. GraphQL query Hello: проверяются HTTP 200, данные приветствия
   и отсутствие errors.
5. GraphQL mutation CreatePerson: проверяются HTTP 200, наличие id,
   значения name и age, отсутствие errors.
6. Параметризованная проверка отказа во входе с неверным паролем:
   пароль с лишним символом и пароль с изменённым регистром.

Идентификаторы созданных ресурсов и токены извлекаются из ответов
во время выполнения тестов. Для REST-запросов cookies сохраняются
в пределах одного теста. Общие настройки вынесены в ApiTestBase.

При падении проверки выводятся запрос и ответ.

## Дополнительный негативный сценарий

Тест `shouldRejectLoginWithIncorrectPassword` отправляет запрос
`POST /auth/login` для пользователя Bret с двумя неверными паролями:
`Password@1234` и `password@123`.

Для каждого варианта проверяются HTTP 401, поле status со значением 401,
сообщение `Invalid username or password.` в поле error и отсутствие
полей access_token и refresh_token в ответе.

Сценарий проверяет отказ в выдаче токенов при неверных учётных данных.
Обязательный негативный тест отдельно проверяет отказ в доступе к
`/auth/me` без токена.

## Учётные данные

Используются username Bret и пароль Password@123.

При проверке пароль password123 из условия возвращал HTTP 401
Invalid username or password. С Password@123 вход выполнен успешно.

## GraphQL subscription

Запрос сохранён в коллекции:

`postman/api-testing-homework.postman_collection.json`

Получение SSE-событий проверено вручную в Bruno: получены приветствия
Hi, Bonjour и Hola. Коллекция экспортирована в формат
Postman Collection v2.1.

Для повторения запроса в Postman:

1. Импортировать коллекцию.
2. Открыть GraphQL Subscription — Greetings (SSE).
3. Проверить заголовок Accept: text/event-stream.
4. Отправить запрос и убедиться в получении событий greetings.

Тело запроса:

```json
{
  "query": "subscription Greetings { greetings }"
}
```

Subscription выполняется вручную и не входит в запуск `mvn test`.
