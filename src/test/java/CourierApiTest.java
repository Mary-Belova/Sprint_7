import io.restassured.RestAssured;
import io.qameta.allure.Step;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CourierApiTest {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1/courier";
    private String courierId; // Для хранения ID курьера

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @Step("Создаем нового курьера")
    public void testCreateCourier() {
        String requestBody = "{\"login\": \"ninja\", \"password\": \"1234\", \"firstName\": \"Тест\"}";

        // Создание курьера и сохранение ID в courierId
        courierId = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/")
                .then()
                .statusCode(201) // Проверка успешного создания
                .extract()
                .path("id"); // Предполагаем, что API возвращает ID курьера

        // Удаление созданного курьера сразу после успешного создания
        deleteCourier(courierId);
    }

    private void deleteCourier(String id) {
        if (id != null) {
            given()
                    .pathParam("id", id)
                    .when()
                    .delete("/{id}")
                    .then()
                    .statusCode(200); // Успешное удаление курьера
        }
    }

    @Test
    @Step("Тестируем создание курьера без обязательных полей")
    public void testCreateCourierWithoutRequiredFields() {
        String requestBody = "{\"login\": \"\", \"password\": \"\", \"firstName\": \"Тест\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/")
                .then()
                .statusCode(400) // Проверка ожидаемого статуса
                .body("message", equalTo("Недостаточно данных для создания учетной записи")); // Проверка сообщения ошибки
    }

    @Test
    @Step("Тестируем создание двух одинаковых курьеров")
    public void testCreateTwoIdenticalCouriers() {
        // Данные для курьера
        String requestBody = "{\"login\": \"ninja\", \"password\": \"1234\", \"firstName\": \"Тест\"}";

        // Создаем первого курьера
        courierId = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/")
                .then()
                .statusCode(201) // Проверка успешного создания
                .extract()
                .path("id"); // Сохраняем ID для удаления

        // Попытка создания второго курьера с теми же данными
        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/")
                .then()
                .statusCode(409) // Проверка, что возвращается код 409 (конфликт)
                .body("message", equalTo("Этот логин уже используется")); // Проверка сообщения ошибки

        // Удаление курьера после теста
        deleteCourier(courierId);
    }
}