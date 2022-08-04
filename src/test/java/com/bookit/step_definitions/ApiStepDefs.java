package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DBUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.Map;

public class ApiStepDefs {

    String accessToken;
    Response response;
    String emailGlobal;
    String studentEmail;
    String studentPassword;

    @Given("I logged Bookit api using {string} and {string}")
    public void ı_logged_Bookit_api_using_and(String email, String password) {
        accessToken = BookItApiUtil.generateToken(email,password);
        emailGlobal = email;

    }

    @When("I get the current user information from api")
    public void ı_get_the_current_user_information_from_api() {
        response = RestAssured.given().accept(ContentType.JSON)
                .and()
                .header("Authorization", accessToken)
                .when()
                .get(ConfigurationReader.get("qa2apiUrl") + "/api/users/me");

    }
    @Then("status code should be {int}")
    public void status_code_should_be(int statusCode) {
        Assert.assertEquals(statusCode,response.statusCode());

    }

    @Then("the information about current user from api and database should match")
    public void theInformationAboutCurrentUserFromApiAndDatabaseShouldMatch() {
        //we will compare databese and api in this step
        //1. get information from database
        //connections is from hooks, and it is ready
        String query = "select firstname, lastname,role from users\n" +
                "where email = '"+ emailGlobal +"'";

        Map<String,Object> dbMap = DBUtils.getRowMap(query);
        System.out.println("dbMap = " + dbMap);

        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");


        //2. get information from api
        String actualFirstname = response.jsonPath().getString("firstName");
        String actualLastname = response.jsonPath().getString("lastName");
        String actualRole = response.jsonPath().getString("role");

        //3. compare database and api
        Assert.assertEquals(expectedFirstName ,actualFirstname);
        Assert.assertEquals(expectedLastName, actualLastname);
        Assert.assertEquals(expectedRole, actualRole);



    }

    @Then("UI,API and Database user information must be match")
    public void uıAPIAndDatabaseUserInformationMustBeMatch() {

        //1. get information from database
        //connections is from hooks, and it is ready
        String query = "select firstname, lastname,role from users\n" +
                "where email = '"+ emailGlobal +"'";

        Map<String,Object> dbMap = DBUtils.getRowMap(query);
        System.out.println("dbMap = " + dbMap);

        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");


        //2. get information from api
        String actualFirstname = response.jsonPath().getString("firstName");
        String actualLastname = response.jsonPath().getString("lastName");
        String actualRole = response.jsonPath().getString("role");

        //3. get information from UI
        SelfPage selfPage = new SelfPage();

        String actualUIName = selfPage.name.getText();
        String actualUIRole = selfPage.role.getText();

        System.out.println(actualUIName);
        System.out.println(actualUIRole);

        //compare UI and database
        String expectedFullName = expectedFirstName + " " + expectedLastName;
        //verify UI and database
        Assert.assertEquals(expectedFullName,actualUIName);
        Assert.assertEquals(expectedRole,actualUIRole);

        //compare UI and API
        String APIFullName = actualFirstname + " " + actualLastname;
        Assert.assertEquals(APIFullName,actualUIName);
        Assert.assertEquals(actualRole,actualUIRole);
    }

    @When("I send POST request to {string} endpoint with following information")
    public void ıSendPOSTRequestToEndpointWithFollowingInformation(String path,Map<String,String> studentInfo) {

        studentEmail = studentInfo.get("email");
        studentPassword = studentInfo.get("password");
         response = RestAssured.given().accept(ContentType.JSON)
                .queryParams(studentInfo).header("Authorization", accessToken)
                .when().post(ConfigurationReader.get("qa2apiUrl") + path);

    }

    @And("I delete previously added student")
    public void ıDeletePreviouslyAddedStudent() {

        //we create a method to delete one student
        BookItApiUtil.deleteStudent(studentEmail,studentPassword);

    }
}
