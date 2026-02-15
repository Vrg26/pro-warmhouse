# Project_template

Это шаблон для решения проектной работы. Структура этого файла повторяет структуру заданий. Заполняйте его по мере работы над решением.

# Задание 1. Анализ и планирование

<aside>

Чтобы составить документ с описанием текущей архитектуры приложения, можно часть информации взять из описания компании и условия задания. Это нормально.

</aside

### 1. Описание функциональности монолитного приложения

**Управление отоплением:**

- Пользователи могут удалённо включать/выключать отопление в своих домах.

**Мониторинг температуры:**

- Система получает данные о температуре с датчиков, установленных в домах. Пользователи могут просматривать текущую температуру в своих домах через веб-интерфейс.

### 2. Анализ архитектуры монолитного приложения

- Язык программирования: Go
- База данных: PostgreSQL
- Архитектура: Монолитная, все компоненты системы (обработка запросов, бизнес-логика, работа с данными) находятся в рамках одного приложения.
- Взаимодействие: Синхронное, запросы обрабатываются последовательно.
- Масштабируемость: Ограничена, так как монолит сложно масштабировать по частям.
- Развертывание: Требует остановки всего приложения.


### 3. Определение доменов и границы контекстов

#### Выделенные домены:
#### **1. Device Registry Domain (Реестр устройств)**

**Тип:** Core Domain  
**Bounded Context:** Device Catalog & Lifecycle

**Границы контекста:**
- Не управляет устройствами (это Device Control)
- Не хранит телеметрию (это Telemetry)
- Не обрабатывает команды к устройствам

**Основные сущности:**
- Device (ID, type, manufacturer, model, protocol, status, owner)
- DeviceCapability (heating, lighting, lock, camera)
- DeviceLocation (house, room)

**Ответственность:**
- Регистрация и каталогизация всех IoT-устройств в системе
- Хранение метаданных устройств (производитель, модель, возможности, протокол связи)
- Управление жизненным циклом устройства (регистрация → активация → деактивация → удаление)
- Привязка устройств к пользователям и помещениям
- Отслеживание статуса подключения устройств (online/offline)

#### **2. Device Control Domain (Управление устройствами)**

**Тип:** Core Domain  
**Bounded Context:** Command & Control

**Ответсвенность:**
- Отправка команд на устройства (включать/выключать, изменить параметры)
- Валидация команд перед отправкой
- Очередь команд и управление их выполнением
- получение подтверждений выполнения
- Обработка ошибок и повторные попытки

**Границы контекста:**
- Работает только с зарегистрированнымми устройствами (Device Registry)
- Не хранит исторические данные команд долгосрочно
- Не создает автоматические сценарии (Это Scenarios)

**Основные сущности:**
- Command (ID, deviceId, type, parameters, status, timestamp)
- CommandQueue
- CommandResult

**Взаимодействие:**
- Вызывает Device Registry для проверки существования устройства
- Публикует события CommandExecuted, CommandFailed

#### **3. Telemetry & Monitoring Domain (Телеметрия и мониторинг)**

**Тип:** Core Domain  
**Bounded Context:** Data Collection & Analytics

**Ответственность:**
- Сбор телеметрии с датчиков (температура, влажность, движение, состояние)
- Хранение временных рядов данных
- Агрегация данных (почасовая, посуточная)
- Предоставление исторических данных
- Базовая аналитика (средние значения, тренды)

**Границы контекста:**
- Только сбор и хранение данных, без бизнес-логики
- Не принимает решений на основе данных (это Scenarios)
- Не управляет устройствами

**Основные сущности:**
- TelemetryEvent (deviceId, metric, value, timestamp)
- AggregatedMetrics (hourly, daily summaries)

#### **4. Scenarios Domain (Cценарии)**

**Тип:** Core Domain  
**Bounded Context:** Rule Engine & Orchestration

**Ответственность:**
- Создание и управление правилами автоматизации
- Программирование сценариев пользователями
- Обработка триггеров и условий
- Выполнение действий на основе правил
- Расписания (например, включать отопление в 6 утра)

**Границы контекста:**
- Не отправляет команды напрямую на устройства (использует Device Control)
- Не хранит телеметрию (читает из Telemetry)
- Создаёт команды, но делегирует выполнение

**Основные сущности:**
- Scenario (ID, name, owner, triggers, conditions, actions)
- Trigger (type: time, sensor, manual)
- Condition (if temperature < 18)
- Action (turn on heating, send notification)
- Schedule (cron expression)

**Взаимодействие:**
- Подписывается на события от Telemetry
- Вызывает Device Control для выполнения команд
- Использует Notification для алертов

#### **5. User & Access Management Domain (Управление пользователями и доступом)**

**Тип:** Supporting Domain  
**Bounded Context:** Identity & Authorization

**Ответственность:**
- Регистрация и аутентификация пользователей
- Управление профилями
- Управление доступом к устройствам (RBAC)
- Семейные аккаунты (несколько пользователей на один дом)
- Временный доступ (например, для гостей)

**Границы контекста:**
- Не управляет устройствами
- Не содержит бизнес-логику IoT
- Только identity и permissions

**Основные сущности:**
- User (ID, email, password_hash, profile)
- Role (owner, family_member, guest)
- Permission (can_control, can_view)
- AccessGrant (userId, deviceId, role, expires_at)

#### **6. Notification Service Domain (Сервис уведомлений)**

**Тип:** Supporting Domain  
**Bounded Context:** User Communications

**Ответственность:**
- Отправка push-уведомлений
- Email-рассылка
- SMS-алерты
- Управление настройками уведомлений пользователя
- История уведомлений

**Границы контекста:**
- Не принимает решений о том, когда отправлять (получает запросы)
- Не знает о бизнес-логике IoT
- Только доставка сообщений

**Основные сущности:**
- Notification (ID, userId, type, channel, message, status)
- NotificationPreferences (email_enabled, push_enabled)

#### **7. Video Surveillance Domain (Видеонаблюдение)**

**Тип:** Core Domain  
**Bounded Context:** Video & Security

**Ответственность:**
- Стриминг видео с камер
- Запись и хранение видео
- Детекция движения и событий (опционально ML)
- Предоставление доступа к архиву

**Границы контекста:**
- Специфичный домен для видео
- Не управляет другими типами устройств
- Интегрируется с Notification при детекции событий

**Основные сущности:**
- Camera (extends Device)
- VideoStream (URL, quality)
- Recording (ID, cameraId, start_time, duration, storage_path)
- Event (motion_detected, person_detected)

#### **8. Protocol Gateway Domain (Шлюз протоколов)**

**Тип:** Generic Domain (Infrastructure)  
**Bounded Context:** Device Communication Layer

**Ответственность:**
- Поддержка различных протоколов (MQTT, Zigbee, Z-Wave, HTTP, CoAP)
- Нормализация данных от разных производителей
- Преобразование команд в протокол-специфичный формат
- Plug-and-play для новых типов устройств
- Обнаружение устройств в сети

**Границы контекста:**
- Только транспортный уровень и адаптация протоколов
- Не содержит бизнес-логики
- Прозрачный для верхних уровней

**Основные компоненты:**
- MQTT Broker (для устройств на MQTT)
- Protocol Adapters (Zigbee → MQTT, HTTP → MQTT)
- Device Discovery Service
- Message Translator

**Взаимодействие:**
- Получает команды от Device Control, преобразует в протокол устройства
- Получает данные от устройств, публикует в Telemetry

#### **9. API Gateway**

**Тип:** Generic Domain (Infrastructure)  
**Bounded Context:** External Interfac

**Ответственность:**
- Единая точка входа для всех клиентских приложений
- Маршрутизация запросов к микросервисам
- Аутентификация и авторизация на уровне gateway
- Rate limiting и throttling
- Кеширование
- Агрегация данных из нескольких сервисов (BFF pattern)

**Границы контекста:**
- Не содержит бизнес-логики
- Только routing, security, cross-cutting concerns

### **4. Проблемы монолитного решения**

**Архитектурные проблемы:**
- **Сильная связанность компонентов.** Ошибка в одной части приложения может привести к падению всей системы.
- **Синхронная модель взаимодействия.** При недоступности датчика запрос блокируется до timeout, что приводит к зависаниям и плохому пользовательскому опыту.
- **Единая точка отказа.** Падение монолита означает недоступность всех функций для всех N пользователей одновременно.

**Проблемы масштабируемости:**
- **Невозможность горизонтального масштабирования.** При высокой нагрузке на одну функцию (например, сбор телеметрии) требуется масштабировать всё приложение целиком.
- **Ограничения базы данных.** Единая PostgreSQL для всех типов данных (пользователи, команды, телеметрия) не оптимальна для работы с временными рядами IoT-данных.

**Проблемы расширяемости:**
- **Невозможность самостоятельного подключения устройств.** Требуется выезд специалиста для каждой установки, что противоречит модели SaaS с самообслуживанием.
- **Сложность добавления новых типов устройств.** Интеграция света, ворот, камер требует изменения всего монолита с полным регрессионным тестированием.
- **Отсутствие поддержки сторонних протоколов.** Нет механизма plug-and-play для устройств партнёров (Zigbee, Z-Wave, проприетарные протоколы).

**Проблемы разработки и эксплуатации:**
- **Длительные циклы разработки.** Любое изменение требует сборки, тестирования и деплоя всего приложения с downtime.
- **Необходимость согласования изменений.** Команды разработчиков не могут работать независимо, требуется координация и разрешение конфликтов в коде.
- **Очереди на релизы.** Невозможность независимых деплоев приводит к задержкам в выпуске новой функциональности.
- **Технологическая привязка.** Вся система на Go, невозможно использовать специализированные технологии (Python для ML, Node.js для streaming).

### 5. Визуализация контекста системы — диаграмма С4
![Context as is](/schemas/as_is/img/as-is-context-c4.png)

# Задание 2. Проектирование микросервисной архитектуры

В этом задании вам нужно предоставить только диаграммы в модели C4. Мы не просим вас отдельно описывать получившиеся микросервисы и то, как вы определили взаимодействия между компонентами To-Be системы. Если вы правильно подготовите диаграммы C4, они и так это покажут.

**Диаграмма контейнеров (Containers)**

![Containers to be](/schemas/to_be/img/container.png)

**Диаграмма компонентов (Components)**

**Device Control Service**

![Components Device Control Service](/schemas/to_be/img/device-control-service-components.png)

**Device Registry Service**

![Components Device Registry Service](/schemas/to_be/img/device-register-service-components.png)

**Telemetry Service**

![Components Telemetry Service](/schemas/to_be/img/telemetry-service-components.png)

**Scenarios Service**

![Components Scenarios Service](/schemas/to_be/img/scenarios-service-components.png)

**User Service**

![Components User Service](/schemas/to_be/img/user-service-components.png)

**Notification Service**

![Components Notification Service](/schemas/to_be/img/notification-service-components.png)

**Video Service**

![Components Video Service](/schemas/to_be/img/video-service-components.png)

**Диаграмма кода (Code)**

**Device Control Service**

![Code Device Control Service](/schemas/to_be/img/device-control-service-code.png)

# Задание 3. Разработка ER-диаграммы

![Er-diagrams](/schemas/to_be/img/common-er.png)

# Задание 4. Создание и документирование API

### 1. Тип API

В микросервисной архитектуре системы "Умный дом" используется комбинированный подход с разными типами API для разных задач. REST API выбран как основной протокол для синхронного взаимодействия между клиентами (Web/Mobile) и микросервисами, а также для межсервисного взаимодействия, благодаря своей простоте, универсальности. Дополнительно используется MQTT для легковесной коммуникации с IoT-устройствами (отправка команд, получение телеметрии), Kafka для асинхронной event-driven коммуникации между микросервисами (события CommandExecuted, TelemetryReceived, MotionDetected).

### 2. Документация API

Для MVP-микросервисов подготовлена документация в формате OpenAPI 3.0:

- **Telemetry Service** (Python/FastAPI) — [`apps/telemetry-service/docs/openapi.yaml`](apps/telemetry-service/docs/openapi.yaml)
  - Также доступна интерактивная Swagger UI при запуске сервиса: http://localhost:8082/docs
- **Device Control Service** (Java/Spring Boot) — [`apps/device-control-service/docs/openapi.yaml`](apps/device-control-service/docs/openapi.yaml)

#### Telemetry Service API (порт 8082)

| Метод | Эндпоинт | Описание |
|-------|---------|----------|
| GET | `/health` | Health check |
| POST | `/api/v1/telemetry` | Записать показание телеметрии |
| GET | `/api/v1/devices/{device_id}/telemetry` | История показаний устройства |
| GET | `/api/v1/devices/{device_id}/telemetry/current` | Последнее показание |
| GET | `/api/v1/devices/{device_id}/telemetry/aggregate` | Агрегаты (avg/min/max) |

#### Device Control Service API (порт 8083)

| Метод | Эндпоинт | Описание |
|-------|---------|----------|
| GET | `/health` | Health check |
| POST | `/api/v1/devices/{deviceId}/commands` | Отправить команду устройству |
| GET | `/api/v1/commands/{commandId}` | Детали команды с результатом |
| GET | `/api/v1/commands/{commandId}/status` | Статус выполнения команды |

Типы команд: `SET_TEMPERATURE`, `TOGGLE_POWER`, `SET_BRIGHTNESS`

Жизненный цикл команды: `PENDING → VALIDATING → QUEUED → EXECUTING → SUCCESS / FAILED / TIMEOUT`

# Задание 5. Работа с docker и docker-compose

Перейдите в apps.

Там находится приложение-монолит для работы с датчиками температуры. В README.md описано как запустить решение.

Вам нужно:

1) сделать простое приложение temperature-api на любом удобном для вас языке программирования, которое при запросе /temperature?location= будет отдавать рандомное значение температуры.

Locations - название комнаты, sensorId - идентификатор названия комнаты

```
	// If no location is provided, use a default based on sensor ID
	if location == "" {
		switch sensorID {
		case "1":
			location = "Living Room"
		case "2":
			location = "Bedroom"
		case "3":
			location = "Kitchen"
		default:
			location = "Unknown"
		}
	}

	// If no sensor ID is provided, generate one based on location
	if sensorID == "" {
		switch location {
		case "Living Room":
			sensorID = "1"
		case "Bedroom":
			sensorID = "2"
		case "Kitchen":
			sensorID = "3"
		default:
			sensorID = "0"
		}
	}
```

2) Приложение следует упаковать в Docker и добавить в docker-compose. Порт по умолчанию должен быть 8081

3) Кроме того для smart_home приложения требуется база данных - добавьте в docker-compose файл настройки для запуска postgres с указанием скрипта инициализации ./smart_home/init.sql

Для проверки можно использовать Postman коллекцию smarthome-api.postman_collection.json и вызвать:

- Create Sensor
- Get All Sensors

Должно при каждом вызове отображаться разное значение температуры

Ревьюер будет проверять точно так же.


# **Задание 6. Разработка MVP**

Необходимо создать новые микросервисы и обеспечить их интеграции с существующим монолитом для плавного перехода к микросервисной архитектуре. 

### **Что нужно сделать**

1. Создайте новые микросервисы для управления телеметрией и устройствами (с простейшей логикой), которые будут интегрированы с существующим монолитным приложением. Каждый микросервис на своем ООП языке.
2. Обеспечьте взаимодействие между микросервисами и монолитом (при желании с помощью брокера сообщений), чтобы постепенно перенести функциональность из монолита в микросервисы. 

В результате у вас должны быть созданы Dockerfiles и docker-compose для запуска микросервисов. 
