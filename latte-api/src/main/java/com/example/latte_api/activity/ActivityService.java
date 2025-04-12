package com.example.latte_api.activity;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.mapper.ActivityMapper;
import com.example.latte_api.shared.PagedEntity;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {
  private final ActivityRepository activityRepository;
  private final ActivityMapper activityMapper;

  @Transactional
  public ActivityDto createActivity(Activity activity) {
    Activity result = activityRepository.save(activity);
    return activityMapper.mapToActivityDto(result);
  }

  @Transactional
  public List<ActivityDto> saveActivities(List<Activity> activites) {
    List<Activity> result = activityRepository.saveAll(activites);
    return result.stream().map(a -> activityMapper.mapToActivityDto(a)).toList();
  }

  public PagedEntity<ActivityDto> getActivitiesForTicket(Long ticketId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Activity> activities = activityRepository.findActivitesForTicket(ticketId, pageable);

    PagedEntity<ActivityDto> response = new PagedEntity<>();
    response.setNext(activities.hasNext());
    response.setPrev(activities.hasPrevious());
    response.setContent(activities.getContent().stream().map(a -> activityMapper.mapToActivityDto(a)).toList());
    return response;
  }

  public Activity getActivity(Long id) {
    Activity activity = activityRepository.findById(id).orElseThrow(
      () -> new EntityNotFoundException("Activity not found")
    );

    return activity;
  }
  
  public ActivityDto updateActivity(Activity activity) {
    activityRepository.save(activity);
    return activityMapper.mapToActivityDto(activity);
  }

  public void deleteActivity (Activity activity) {
    activityRepository.delete(activity);
  }  
}
