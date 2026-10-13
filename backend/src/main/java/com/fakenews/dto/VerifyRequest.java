package com.fakenews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyRequest {

    @Size(max = 500, message = "Headline cannot exceed 500 characters")
    private String headline;

    @NotBlank(message = "News text is required")
    @Size(min = 10, max = 10000, message = "Text must be between 10 and 10,000 characters")
    private String text;

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
