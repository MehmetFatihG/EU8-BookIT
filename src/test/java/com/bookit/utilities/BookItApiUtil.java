package com.bookit.utilities;

import io.cucumber.java.af.En;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class BookItApiUtil {

    //it generates token for the authorization
    public static String generateToken(String email, String password){

       String accessToken = RestAssured.given().accept(ContentType.JSON).queryParam("email", email)
                .queryParam("password", password)
                .when().get(Environment.BASE_URL + "/sign")
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
                .when().get(Environment.BASE_URL + "/api/users/me")
                .then().statusCode(200).extract().jsonPath().getInt("id");

        //3.send a delete request as a teacher to /api/students/{id} endpoint to delete the student
        String teacherToken = BookItApiUtil.generateToken(Environment.TEACHER_EMAIL,
                Environment.TEACHER_PASSWORD);
        RestAssured.given()
                .pathParam("id",studentIdToDelete)
                .header("Authorization", teacherToken)
                .when().delete(Environment.BASE_URL + "/api/students/{id}")
                .then().statusCode(204);


    }

    public static String getTokenByRole(String role){

        if(role.equalsIgnoreCase("teacher")){
            String teacherToken = given().accept(ContentType.JSON)
                    .queryParam("email", Environment.TEACHER_EMAIL)
                    .queryParam("password", Environment.TEACHER_PASSWORD)
                    .when().get(Environment.BASE_URL + "/sign")
                    .then().statusCode(200).extract().jsonPath().getString("accessToken");
            System.out.println(role + " : " + teacherToken);
            return "Bearer " + teacherToken;
        }else if(role.equalsIgnoreCase("student member")){
            String studentMemberToken = given().accept(ContentType.JSON)
                    .queryParam("email", Environment.MEMBER_EMAIL)
                    .queryParam("password", Environment.MEMBER_PASSWORD)
                    .when().get(Environment.BASE_URL + "/sign")
                    .then().statusCode(200).extract().jsonPath().getString("accessToken");
            System.out.println(role + " : " + studentMemberToken);
            return "Bearer " + studentMemberToken;
        }else if(role.equalsIgnoreCase("student leader")){
            String studentLeaderToken = given().accept(ContentType.JSON)
                    .queryParam("email", Environment.LEADER_EMAIL)
                    .queryParam("password", Environment.LEADER_PASSWORD)
                    .when().get(Environment.BASE_URL + "/sign")
                    .then().statusCode(200).extract().jsonPath().getString("accessToken");
            System.out.println(role + " : " + studentLeaderToken);
            return "Bearer " + studentLeaderToken;
        }else{
            throw new IllegalStateException();
        }

    }
}
