package com.example.latte_api.activity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.shared.PagedEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/latte-api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
  private final ActivityService activityService;

  @GetMapping("/ticket/{id}")
  public ResponseEntity<PagedEntity<ActivityDto>> getActivitesForTicket(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,  @RequestParam(defaultValue = "0") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(activityService.getActivitiesForTicket(id, page, size));
  }
}
