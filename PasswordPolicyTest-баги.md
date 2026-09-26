# Баг-репорты PasswordPolicy

## REG-001. Пароль длиной 64 символа ошибочно считается слишком длинным

**Окружение:** проект `demo-junit` 1.0.0, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.

**Шаги воспроизведения:**

1. Создать валидный пароль длиной ровно 64 символа.
2. Вызвать `new PasswordPolicy().check(password)`.
3. Проверить `result.violations()`.

**Фактический результат:** список содержит `TOO_LONG`.

**Ожидаемый результат:** `TOO_LONG` отсутствует. Цитата из спецификации: «длина больше 64 — TOO_LONG. Длина ровно 64 — ещё допустима».

**Severity:** Major — валидный пароль отклоняется.  
**Priority:** High — нарушена явно заданная граница.

**Регрессионный тест:** `LengthRules.checksLengthBoundaries`, набор данных с длиной 64.

## REG-002. Серия из трёх одинаковых символов не определяется

**Окружение:** проект `demo-junit` 1.0.0, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.

**Шаги воспроизведения:**

1. Вызвать `new PasswordPolicy().check("Ab1!cdeee")`.
2. Проверить `result.violations()`.

**Фактический результат:** `REPEATED_RUN` отсутствует.

**Ожидаемый результат:** список содержит `REPEATED_RUN`. Цитата: «три и более одинаковых символа подряд — REPEATED_RUN. “aaa” — нарушение, “aa” — нет».

**Severity:** Major — невалидный пароль может быть принят.  
**Priority:** High — нарушена граница правила безопасности.

**Регрессионный тест:** `RepeatedRunRule.checksRepeatedRunBoundary`, набор данных с тремя символами.

## REG-003. Чёрный список проверяется с учётом регистра

**Окружение:** проект `demo-junit` 1.0.0, класс `ru.mts.itmo.aqa.junit5.PasswordPolicy`, JDK 21.

**Шаги воспроизведения:**

1. Вызвать `new PasswordPolicy().check("PaSsWoRd")`.
2. Проверить `result.violations()`.

**Фактический результат:** `BLACKLISTED` отсутствует.

**Ожидаемый результат:** список содержит `BLACKLISTED`. Цитата: «пароль совпадает с одним из простых: password, qwerty, 123456, admin, welcome, без учёта регистра — BLACKLISTED».

**Severity:** Major — запрещённый пароль может быть принят при изменении регистра.  
**Priority:** High — простой обход правила безопасности.

**Регрессионный тест:** `BlacklistRule.reportsBlacklistedPasswordIgnoringCase`, набор `PaSsWoRd`.
