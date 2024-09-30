import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreationWithColorApiTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1/orders";

    @Parameterized.Parameter()
    public String firstName;

    @Parameterized.Parameter(1)
    public String lastName;

    @Parameterized.Parameter(2)
    public String address;

    @Parameterized.Parameter(3)
    public int metroStation;

    @Parameterized.Parameter(4)
    public String phone;

    @Parameterized.Parameter(5)
    public int rentTime;

    @Parameterized.Parameter(6)
    public String deliveryDate;

    @Parameterized.Parameter(7)
    public String comment;

    @Parameterized.Parameter(8)
    public String[] color;

    @Parameterized.Parameter(9)
    public int expectedStatusCode;

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Parameterized.Parameters(name = "{index}: createOrder(color={8})")
    public static Object[][] data() {
        return new Object[][]{
                // Указание одного цвета
                {"Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 5, "2020-06-06", "Saske, come back to Konoha", new String[]{"BLACK"}, 201}, // Один цвет
                {"Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 5, "2020-06-06", "Saske, come back to Konoha", new String[]{"GREY"}, 201},  // Один цвет

                // Указание обоих цветов
                {"Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 5, "2020-06-06", "Saske, come back to Konoha", new String[]{"BLACK", "GREY"}, 201}, // Оба цвета

                // Без указания цвета
                {"Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 5, "2020-06-06", "Saske, come back to Konoha", null, 201} // Без цвета
        };
    }

    @Test
    public void testCreateOrderWithColor() {
        String requestBody = createRequestBody();
        Response response = sendPostRequest(requestBody);
        validateResponse(response);
    }

    private String createRequestBody() {
        String colorJson = (color == null || color.length == 0) ? "[]" : "[\"" + String.join("\",\"", color) + "\"]";
        return "{\n" +
                "    \"firstName\": \"" + firstName + "\",\n" +
                "    \"lastName\": \"" + lastName + "\",\n" +
                "    \"address\": \"" + address + "\",\n" +
                "    \"metroStation\": " + metroStation + ",\n" +
                "    \"phone\": \"" + phone + "\",\n" +
                "    \"rentTime\": " + rentTime + ",\n" +
                "    \"deliveryDate\": \"" + deliveryDate + "\",\n" +
                "    \"comment\": \"" + comment + "\",\n" +
                "    \"color\": " + colorJson + "\n" +
                "}";
    }

    private Response sendPostRequest(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post();
    }

    private void validateResponse(Response response) {
        response.then()
                .statusCode(expectedStatusCode) // Проверяем, что статус код равен 201
                .body("track", notNullValue()); // Проверяем, что поле "track" присутствует в ответе
    }
}
