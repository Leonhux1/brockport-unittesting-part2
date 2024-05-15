package funtionaltests;


import com.petstore.AnimalType;
import com.petstore.PetEntity;
import com.petstore.PetStoreReader;
import com.petstore.animals.CatEntity;
import com.petstore.animals.attributes.Breed;
import com.petstore.animals.attributes.Gender;
import com.petstore.animals.attributes.PetType;
import com.petstore.animals.attributes.Skin;
import com.petstoreservices.exceptions.PetDataStoreException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.petstore.animals.attributes.Skin.FUR;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Get Pet Entity Tests by PetType only.  The class has some functional and error message tests
 * The test class is using rest assured to help with functional testing
 */
public class UpdateInventoryByPetTypeTests
{
    private List<PetEntity> expectedResults;
    private static Headers headers;
    @BeforeEach
    public void retrieveDataStore() throws PetDataStoreException {
        PetStoreReader psReader = new PetStoreReader();
        expectedResults = psReader.readJsonFromFile();

        RestAssured.baseURI  = "http://localhost:8080/";

        Header contentType = new Header("Content-Type", ContentType.JSON.toString());
        Header accept = new Header("Accept", ContentType.JSON.toString());
        headers = new Headers(contentType, accept);
    }

    @TestFactory
    @DisplayName("Update Pet Entity Cat Test")
    public Stream<DynamicNode> updateInventoryCatTest() throws PetDataStoreException {
        PetEntity oldCat = new CatEntity(AnimalType.DOMESTIC, Skin.HAIR, Gender.MALE, Breed.BURMESE,
                new BigDecimal("65.00"), 1);
        PetEntity itemUpdates =
                given()
                        .headers(headers)
                        .and()
                        .body(new CatEntity(AnimalType.DOMESTIC, FUR, Gender.FEMALE, Breed.SPHYNX,
                                new BigDecimal("225.00"), 1))
                        .when()
                        .put("inventory/update?petType=CAT&petId=1")
                        .then()
                        .log().all()
                        .assertThat().statusCode(200)
                        .assertThat().contentType("application/json")
                        .extract()
                        .jsonPath()
                        .getObject(".", PetEntity.class);

        PetStoreReader psReader = new PetStoreReader();
        List<PetEntity> expectedResults = psReader.readJsonFromFile();
        List<PetEntity> actualCats =
                expectedResults.stream()
                        .filter(p -> p.getPetType().equals(PetType.CAT) && p.getPetId()==itemUpdates.getPetId())
                        .sorted(Comparator.comparingInt(PetEntity::getPetId))
                        .collect(Collectors.toList());

        List<DynamicNode> testNodes = new ArrayList<DynamicNode>();
        testNodes.add(DynamicTest.dynamicTest("Pet item not match",
                () -> assertNotEquals(oldCat.toString(), itemUpdates.toString())));
        testNodes.add(DynamicTest.dynamicTest("Pet item with PetId[" + oldCat.getPetId() + "]",
                () -> assertTrue(itemUpdates.toString().contains(String.valueOf(oldCat.getPetId())))));
        testNodes.add(DynamicTest.dynamicTest("Pet item with PetType<Cat>",
                () -> assertTrue(itemUpdates.toString().contains(oldCat.getPetType().name()))));
        //testNodes.add(PetEntityValidator.addPetEntityBodyTests(Arrays.asList(itemCreated), actualDogs));
        return testNodes.stream();

    }

    @TestFactory
    @DisplayName("Update Inventory Missing petType and petId test")
    public Stream<DynamicTest> updateInventoryMissingPetTypePetIdTest()
    {
        RestAssured.registerParser("application/json", Parser.JSON);
        BadRequestResponseBody body =
                given()
                        .headers(headers)
                        .and()
                        .body(new CatEntity(AnimalType.DOMESTIC, FUR, Gender.MALE, Breed.GERMAN_SHEPHERD,
                                new BigDecimal("225.00"), 1))
                        .when()
                        .put("inventory/update")
                        .then()
                        .log().all()
                        .assertThat().statusCode(400)
                        .assertThat().contentType("application/json")
                        .extract()
                        .jsonPath()
                        .getObject(".", BadRequestResponseBody.class);

        return body.executeTests("Bad Request", "Invalid request parameters.",
                "/inventory/update", 400).stream();
    }


}
