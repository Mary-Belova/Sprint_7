import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;

public class OrderListApiTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1/orders";
    private static final String COURIER_URL = "https://qa-scooter.praktikum-services.ru/api/v1/courier";

    private String idCourier;

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void getOrdersListNoCourierId() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get();

        response.then().log().all().assertThat().statusCode(200)
                .and()
                .assertThat()
                .body("orders", notNullValue());
    }

    @Test
    public void getOrdersListWithExistCourierId() {
        // Создаем нового курьера
        String courierData = "{ \"login\": \"testCourier" + System.currentTimeMillis() + "\", \"password\": \"testPassword\", \"firstname\": \"Test\" }";
        Response createCourierResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(courierData)
                .when()
                .post(COURIER_URL);

        createCourierResponse.then().statusCode(201);

        // Логин курьера
        Response loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(courierData)
                .when()
                .post(COURIER_URL + "/login");

        idCourier = loginResponse.jsonPath().getString("id");

        // Получение списка заказов с существующим идентификатором курьера
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", idCourier)
                .when()
                .get();

        response.then().log().all().assertThat().statusCode(200)
                .and()
                .assertThat()
                .body("orders", notNullValue());
    }

    @Test
    public void getOrdersListWithNotExistCourierId() {
        int nonExistentCourierId = 9999; // Не существует
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", nonExistentCourierId)
                .when()
                .get();

        response.then().log().all().assertThat().statusCode(404)
                .and()
                .assertThat()
                .body("message", equalTo("Курьер с идентификатором " + nonExistentCourierId + " не найден"));
    }

    @After
    public void deleteCourier() {
        if (idCourier != null) {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete(COURIER_URL + "/" + idCourier)
                    .then()
                    .statusCode(200); // Ожидаем успешное удаление курьера
        }
    }
}