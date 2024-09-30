import io.restassured.RestAssured;
import io.qameta.allure.Step; // Импортируем аннотацию @Step
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CourierLoginApiTest {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1/courier/login";
    private final String validLogin = "ninja"; // Логин для успешного теста
    private final String validPassword = "1234"; // Пароль для успешного теста
    private final String invalidLogin = "invalid"; // Логин для теста с несуществующим пользователем
    private final String invalidPassword = "wrongpassword"; // Пароль для теста с несуществующим пользователем

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    // Тест успешной авторизации
    @Test
    @Step("Тестируем успешный логин с корректными учетными данными")
    public void testSuccessfulLogin() {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", validLogin, validPassword);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("id", equalTo(12345)); // Убедитесь, что это правильный ID
    }

    // Тест, проверяющий обязательные поля
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

    // Общий метод для проверки запросов
    @Step("Отправляем запрос на авторизацию")
    private void checkLoginRequest(String requestBody, int expectedStatusCode, String expectedMessage) {
        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(expectedStatusCode)
                .body("message", equalTo(expectedMessage));
    }

    // Тест авторизации с неправильными логин и паролем
    @Test
    @Step("Тестируем авторизацию с неверными учетными данными")
    public void testLoginWithInvalidCredentials() {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", invalidLogin, invalidPassword);
        checkLoginRequest(requestBody, 404, "Учетная запись не найдена");
    }

    // Тест авторизации под несуществующим пользователем
    @Test
    @Step("Тестируем авторизацию под несуществующим пользователем")
    public void testLoginWithNonExistingUser() {
        String requestBody = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", invalidLogin, invalidPassword);
        checkLoginRequest(requestBody, 404, "Учетная запись не найдена");
    }
}