package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EmailRequest {
    @SerializedName("sender")
    public Person sender;
    
    @SerializedName("to")
    public List<Person> to;
    
    @SerializedName("subject")
    public String subject;
    
    @SerializedName("htmlContent")
    public String htmlContent;

    public static class Person {
        @SerializedName("name")
        public String name;
        @SerializedName("email")
        public String email;

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
