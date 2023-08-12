package com.fauzi.jobservice.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DansJobListResponse {
    private String id;
    private String type;
    private String url;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("company")
    private String company;
    @JsonProperty("company_url")
    private String companyUrl;
    private String location;
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("how_to_apply")
    private String howToApply;
    @JsonProperty("company_logo")
    private String companyLogo;
}
