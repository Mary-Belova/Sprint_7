import io.restassured.RestAssured;
import io.qameta.allure.Step;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

public class CourierLoginApiTest {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1/courier";
    private String validLogin; // Логин для успешного теста
    private String validPassword; // Пароль для успешного теста
    private String courierId;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Генерируем уникальные логин и пароль перед каждым тестом
        validLogin = "courier_" + UUID.randomUUID().toString();
        validPassword = UUID.randomUUID().toString();

        // Создаем курьера
        courierId = createCourier(validLogin, validPassword);
    }

    @After
    public void tearDown() {
        // Удаляем курьера после завершения тестов
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }

    private String createCourier(String login, String password) {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\", \"firstName\": \"Test\"}", login, password);
        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/")
                .then()
                .statusCode(201) // Ожидаем успешное создание
                .extract()
                .path("id"); // Возвращаем ID созданного курьера
    }

    private void deleteCourier(String id) {
        if (id != null) {
            given()
                    .pathParam("id", id)
                    .when()
                    .delete("/{id}")
                    .then()
                    .statusCode(200); // Ожидаем успешное удаление
        }
    }

    @Test
    @Step("Тестируем успешный логин с корректными учетными данными")
    public void testSuccessfulLogin() {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", validLogin, validPassword);

        // Логинимся и проверяем, что ID больше 0
        int loginResponseId = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        assert loginResponseId != 0 : "ID курьера не должен быть равен 0";
    }

    @Test
    @Step("Проверяем логин без обязательных полей")
    public void testLoginWithoutRequiredFields() {
        // Проверяем без логина
        String requestBodyWithoutLogin = String.format("{\"login\": \"\", \"password\": \"%s\"}", validPassword);
        checkLoginRequest(requestBodyWithoutLogin, 400, "Недостаточно данных для входа");

        // Проверяем без пароля
        String requestBodyWithoutPassword = String.format("{\"login\": \"%s\", \"password\": \"\"}", validLogin);
        checkLoginRequest(requestBodyWithoutPassword, 400, "Недостаточно данных для входа");
    }

    @Step("Отправляем запрос на авторизацию")
    private void checkLoginRequest(String requestBody, int expectedStatusCode, String expectedMessage) {
        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/login") // Убедитесь, что вы запрашиваете правильный путь
                .then()
                .statusCode(expectedStatusCode)
                .body("message", equalTo(expectedMessage)); // Проверяем сообщение об ошибке
    }

    @Test
    @Step("Тестируем авторизацию с неверными учетными данными")
    public void testLoginWithInvalidCredentials() {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", "invalid", "wrongpassword");
        checkLoginRequest(requestBody, 404, "Учетная запись не найдена");
    }
}