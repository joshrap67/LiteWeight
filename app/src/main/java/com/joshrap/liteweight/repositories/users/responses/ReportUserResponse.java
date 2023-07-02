package com.joshrap.liteweight.repositories.users.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportUserResponse {

   private String id;
   private String claimantUserId;
   private String reportedUserId;
   private String reportedUsername;
   private String reportedUtc;
}
