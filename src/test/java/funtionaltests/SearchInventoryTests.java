package funtionaltests;

import com.petstore.PetEntity;
import com.petstore.PetStoreReader;
import com.petstoreservices.exceptions.PetDataStoreException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.stream.Collectors;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchInventoryTests {
    private List<PetEntity> expectedResults;
    private static Headers headers;

    @BeforeEach
    public void retrieveDataStore() throws PetDataStoreException {
        PetStoreReader psReader = new PetStoreReader();
        expectedResults = psReader.readJsonFromFile();

        RestAssured.baseURI = "http://localhost:8080/";

        Header contentType = new Header("Content-Type", ContentType.JSON.toString());
        Header accept = new Header("Accept", ContentType.JSON.toString());
        headers = new Headers(contentType, accept);
    }

    @Test
    @DisplayName("Search Pet Entity by Name Test")
    public void searchInventoryByBreedTest() {
        String animalTypeToSearch = "GERMAN_SHEPERD";

        List<PetEntity> matchingPets =
                expectedResults.stream()
                        .filter(p -> p.getAnimalType().equals(animalTypeToSearch))
                        .collect(Collectors.toList());

        List<PetEntity> actualPets = given()
                .headers(headers)
                .when()
                .get("inventory/searchByName/" + animalTypeToSearch)
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", PetEntity.class);

        assertEquals(matchingPets.size(), actualPets.size());
        // Add more assertions for matching pets
    }
}
