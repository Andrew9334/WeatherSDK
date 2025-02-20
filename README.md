# WeatherSDK

## Описание
WeatherSDK — это Java-библиотека для работы с OpenWeather API. Позволяет легко получать данные о погоде по названию города.

Поддержка кэширования (до 10 городов, данные актуальны 10 минут).  
Два режима работы: **on-demand** (по запросу) и **polling** (автообновление).  
Обработка ошибок: неверный API-ключ, город не найден, ошибки сети.  
Простая интеграция с Maven.  
Юнит-тесты (`JUnit + Mockito`).

---

## Установка
### 1. Подключение через Maven
Добавьте в `pom.xml`:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Установка вручную
Соберите проект:
```bash
mvn package
```
Добавьте скомпилированный JAR-файл (`target/weather-sdk-1.0.0.jar`) в свой проект.

---

## Использование
### 1. Создание экземпляра SDK
```java
import com.example.WeatherSDK;
import com.example.WeatherData;

public class Main {
    public static void main(String[] args) {
        try {
            // Создание экземпляра SDK с API-ключом и режимом работы
            WeatherSDK sdk = WeatherSDK.getInstance("YOUR_API_KEY", false);

            // Получение погоды по названию города
            WeatherData data = sdk.getWeather("London");
            System.out.println(data);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
```

### 2. Использование режима `polling` (автообновление)
```java
WeatherSDK sdk = WeatherSDK.getInstance("YOUR_API_KEY", true);
```
В этом режиме SDK автоматически обновляет данные о погоде каждые **10 минут**.

---

## Формат JSON-ответа
SDK возвращает объект `WeatherData`, который форматируется следующим образом:
```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "London"
}
```

---

## Обработка ошибок
SDK выбрасывает исключения, если что-то пошло не так:
| Ошибка                    | Исключение                      | Причина |
|---------------------------|--------------------------------|---------|
| Неверный API-ключ        | `InvalidApiKeyException`       | Введен неверный API-ключ |
| Город не найден          | `CityNotFoundException`        | Указанный город отсутствует в базе OpenWeather |
| Ошибка сети              | `IOException`                  | Проблема с подключением к API |

**Пример обработки ошибок:**
```java
try {
    WeatherData data = sdk.getWeather("UnknownCity");
} catch (CityNotFoundException e) {
    System.err.println("Город не найден!");
} catch (InvalidApiKeyException e) {
    System.err.println("Неверный API-ключ!");
} catch (IOException e) {
    System.err.println("Ошибка сети!");
}
```

---

## Разработка и тестирование
### Запуск тестов
```bash
mvn test
```
SDK покрыт тестами (`JUnit` + `Mockito`).  
Файлы тестов находятся в `src/test/java/com/example/`.

---

## Публикация в Maven Central
Если вы хотите загрузить SDK в **Maven Central**, выполните:
```bash
mvn deploy
```
Для этого вам потребуется учетная запись Sonatype.

---

## Полезные ссылки
- **Официальный сайт OpenWeather API**: [https://openweathermap.org/api](https://openweathermap.org/api)
