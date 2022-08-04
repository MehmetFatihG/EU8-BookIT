package com.bookit.utilities;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class BookItApiUtil {

    //it generates token for the authorization
    public static String generateToken(String email, String password){

       String accessToken = RestAssured.given().accept(ContentType.JSON).queryParam("email", email)
                .queryParam("password", password)
                .when().get(ConfigurationReader.get("qa2apiUrl") + "/sign")
                .then().statusCode(200).extract().jsonPath().getString("accessToken");

       return "Bearer " + accessToken;
    }

    //it deletes one student
    public static void deleteStudent(String studentEmail,String studentPassword){

        //1.send a get request to get token with student information
        String studentToken = BookItApiUtil.generateToken(studentEmail,studentPassword);

        //2.send a get request to /api/users/me endpoint and get the id number
        int studentIdToDelete = RestAssured.given().accept(ContentType.JSON)
                .header("Authorization", studentToken)
                .when().get(ConfigurationReader.get("qa2apiUrl") + "/api/users/me")
                .then().statusCode(200).extract().jsonPath().getInt("id");

        //3.send a delete request as a teacher to /api/students/{id} endpoint to delete the student
        String teacherToken = BookItApiUtil.generateToken(ConfigurationReader.get("teacher_email"),
                ConfigurationReader.get("teacher_password"));
        RestAssured.given()
                .pathParam("id",studentIdToDelete)
                .header("Authorization", teacherToken)
                .when().delete(ConfigurationReader.get("qa2apiUrl") + "/api/students/{id}")
                .then().statusCode(204);


    }

}
