package funtionaltests;


import com.petstore.PetEntity;
import com.petstore.PetStoreReader;
import com.petstore.animals.attributes.PetType;
import com.petstoreservices.exceptions.PetDataStoreException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Get Pet Entity Tests by PetType only.  The class has some functional and error message tests
 * The test class is using rest assured to help with functional testing
 */
public class GetInventoryByPetTypeTests
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
    @DisplayName("Get Inventory Test")
    public Stream<DynamicNode> getInventoryTest()
    {
        List<PetEntity> pets =
                expectedResults.stream()
                        .sorted(Comparator.comparingInt(PetEntity::getPetId))
                        .collect(Collectors.toList());
        Response response = RestAssured.get("inventory");
        List<PetEntity> actualResults = response.body().jsonPath().getList(".", PetEntity.class);
        List<DynamicNode> testNodes = new ArrayList<DynamicNode>();

        List<DynamicTest> responseTests = Arrays.asList(
                DynamicTest.dynamicTest("Status Code 200 Test",
                        ()-> assertEquals(200, response.getStatusCode())),
                DynamicTest.dynamicTest("Content Type text/plain;charset=UTF-8 Test ",
                        ()-> assertTrue("text/plain;charset=UTF-8"
                                .equalsIgnoreCase( response.getContentType()))),
                DynamicTest.dynamicTest("Size of results test[" + pets.size() + "]",
                        ()-> assertEquals(pets.size(), actualResults.size())));

        DynamicContainer responseContainer = DynamicContainer.dynamicContainer("Response Tests", responseTests);
        testNodes.add(responseContainer);

        return testNodes.stream();

    }

}