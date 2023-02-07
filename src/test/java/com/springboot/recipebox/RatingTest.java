package com.springboot.recipebox;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@RunWith(SpringRunner.class)
public class RatingTest
{
    @LocalServerPort
    int port;

    @Before
    public void setUp()
    {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void createThenGetThenDeleteRecipeRating() throws JSONException
    {
        // used for testing data already in DB with no data related to rating
        String username = "John9287";
        int recipeID = 67; // watermelon shake

        Response response = RestAssured.given().
                header("Content-Type", "application/json").
                queryParams("username", username).
                queryParams("score", 4).
                when().
                post("/rating-management/recipes/" + recipeID + "/rate");

        // Check status code
        int statusCode = response.getStatusCode();
        assertEquals(201, statusCode); // created

        // GET request to check if added successfully
        response = RestAssured.given().
                header("Content-Type", "application/json").
                when().
                get("/rating-management/recipes/" + recipeID + "/ratings");

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // Check response body for correct response
        String returnString = response.getBody().asString();

        JSONArray returnArr = new JSONArray(returnString);

        // check if returned rating is only one
        assertEquals(1, returnArr.length());

        // check if rating is correct
        JSONObject returnObj = returnArr.getJSONObject(0);

        assertEquals(4, returnObj.getInt("score"));
        assertEquals(username, returnObj.getString("username"));
        assertEquals(recipeID, returnObj.getInt("recipeID"));

        int ratingID = returnObj.getInt("ratingID");

        // GET request to check
        response = RestAssured.given().
                header("Content-Type", "application/json").
                when().
                get("/rating-management/recipes/" + recipeID + "/ratings/" + ratingID);

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // --------------------------------------------
        // Update rating test
        response = RestAssured.given().
                header("Content-Type", "application/json").
                queryParams("score", 3).
                when().
                put("/rating-management/recipes/" + recipeID + "/ratings/" + ratingID);

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // GET request to check if updates successfully
        response = RestAssured.given().
                header("Content-Type", "application/json").
                when().
                get("/rating-management/recipes/" + recipeID + "/ratings");

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // Check response body for correct response
        returnString = response.getBody().asString();

        returnArr = new JSONArray(returnString);

        // check if returned rating is only one
        assertEquals(1, returnArr.length());

        // check if rating is correct
        returnObj = returnArr.getJSONObject(0);

        assertEquals(3, returnObj.getInt("score"));

        // --------------------------------------------------------
        // From this point, we know that add rating works
        // Now, we will test delete rating

        response = RestAssured.given().
                header("Content-Type", "application/json").
                when().
                delete("/rating-management/recipes/" + recipeID + "/ratings/" + ratingID);

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // make a GET request of rating on recipe and make sure it's now empty
        response = RestAssured.given().
                header("Content-Type", "application/json").
                when().
                get("/rating-management/recipes/" + recipeID + "/ratings");

        // Check status code
        statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        // compare response. should just be empty
        returnArr = new JSONArray(response.getBody().asString());

        // check if returned ingredients is only one
        assertEquals(0, returnArr.length());

    }

    @Test(expected = Exception.class)
    public void pantryWrongUsernameTest() throws Exception
    {
        // used for testing data already in DB with no data related to rating
        String username = "kjdh";
        int recipeID = 67; // watermelon shake

        Response response = RestAssured.given().
                header("Content-Type", "application/json").
                queryParams("username", username).
                queryParams("score", 4).
                when().
                post("/rating-management/recipes/" + recipeID + "/rate");

        // Check status code
        int statusCode = response.getStatusCode();
        if (statusCode == 500)
            throw new Exception("username doesn't exist");
    }
}
