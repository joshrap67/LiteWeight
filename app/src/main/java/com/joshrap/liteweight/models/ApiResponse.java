package com.joshrap.liteweight.models;

import java.util.Map;

import lombok.Data;

@Data
public class ApiResponse {

    public static final String SUCCESS = "success";
    public static final String RESULT_MESSAGE = "resultMessage";

    private boolean success;
    private String jsonString;

    public ApiResponse(Map<String, Object> json){
        System.out.println("gjkhhhhhhhhhhhhkggggggggggggggggggggggggggggggggggggggg");

        this.success = Boolean.parseBoolean((String) json.get(SUCCESS));
        this.jsonString = (String) json.get(RESULT_MESSAGE);
    }
}
