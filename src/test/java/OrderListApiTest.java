import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderListApiTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1/orders";

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    // Тест: Получение всех заказов без указания courierId
    @Test
    public void testGetOrdersWithoutCourierId() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get();

        response.then()
                .statusCode(200)
                .body("orders", notNullValue()) // Проверка, что поле orders не null
                .body("orders", isA(java.util.List.class)) // Проверка, что orders является списком
                .body("orders.size()", greaterThan(0)) // Проверка, что в orders хотя бы один заказ
                .body("pageInfo", notNullValue()) // Проверка наличия объекта pageInfo
                .body("availableStations", notNullValue()); // Проверка наличия доступных станций
    }

    // Тест: Получение заказов с несуществующим courierId
    @Test
    public void testGetOrdersWithNonExistentCourierId() {
        int nonExistentCourierId = 9999; // Не существует

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", nonExistentCourierId)
                .when()
                .get();

        response.then()
                .statusCode(404)
                .body("message", equalTo("Курьер с идентификатором " + nonExistentCourierId + " не найден"));
    }

    // Тест: Получение заказов с существующим courierId
    @Test
    public void testGetOrdersWithExistingCourierId() {
        int existingCourierId = 1; // Укажите здесь корректный id курьера

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", existingCourierId)
                .when()
                .get();

        response.then()
                .statusCode(200)
                .body("orders", notNullValue()) // Заказы не null
                .body("orders", isA(java.util.List.class)) // Проверка, что это список
                .body("orders.size()", greaterThan(0)); // Проверка, что хотя бы один заказ присутствует
    }
}