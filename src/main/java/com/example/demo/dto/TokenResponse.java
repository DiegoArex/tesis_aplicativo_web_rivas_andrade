package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
  @JsonProperty("access_token")
  private String access_token;

  @JsonProperty("refresh_token")
    private String refresh_token;

  @JsonProperty("token_type")
    private String token_type;

  @JsonProperty("expires_in")
    private long expires_in;
}
