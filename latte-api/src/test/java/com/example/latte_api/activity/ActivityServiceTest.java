package com.example.latte_api.activity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.latte_api.activity.dto.ActivityDto;
import com.example.latte_api.activity.enums.ActivityType;
import com.example.latte_api.activity.mapper.ActivityMapper;
import com.example.latte_api.shared.PagedEntity;
import com.example.latte_api.ticket.Ticket;
import com.example.latte_api.user.User;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {
  private ActivityService activityService;

  @Mock
  private ActivityRepository activityRepository;

  @Mock
  private ActivityMapper activityMapper;

  @BeforeEach
  void setup() {
    activityService = new ActivityService(activityRepository, activityMapper);
  }

  @AfterEach
  void purge() {
    activityService = null;
  }

  @Test
  void shouldReturn_activityDto_whenActivityCreated() {
    // mock
    final User user = Mockito.mock(User.class);
    final Ticket ticket = Mockito.mock(Ticket.class);
    final ActivityDto activityDto = Mockito.mock(ActivityDto.class);

    // given
    final Activity activity = Activity.builder()
      .type(ActivityType.EDIT)
      .author(user)
      .ticket(ticket)
      .message("Created ticket")
      .build();

    // when
    when(activityRepository.save(activity)).thenReturn(activity);
    when(activityMapper.mapToActivityDto(activity)).thenReturn(activityDto);
    
    final ActivityDto result = activityService.createActivity(activity);

    // then
    verify(activityRepository, times(1)).save(activity);
    verify(activityMapper, times(1)).mapToActivityDto(activity);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_listOfActivityDto_whenActivitySaved() {
    // mock
    final ActivityDto activityDto = Mockito.mock(ActivityDto.class);

    // given
    final List<Activity> activites = List.of(Mockito.mock(Activity.class));

    // when
    when(activityRepository.saveAll(activites)).thenReturn(activites);
    when(activityMapper.mapToActivityDto(any(Activity.class))).thenReturn(activityDto);
    
    final List<ActivityDto> result = activityService.saveActivities(activites);

    // then
    verify(activityRepository, times(1)).saveAll(activites);
    verify(activityMapper, times(1)).mapToActivityDto(any(Activity.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturn_pagedEntity_ofActivityDto_forTicketId() {
    // mock
    final Page<Activity> page = Mockito.mock(Page.class);
    // given
    final Long id = 101L;
    final int number = 0;
    final int size = 10;

    // when
    when(activityRepository.findActivitesForTicket(eq(id), any(Pageable.class))).thenReturn(page);
    when(page.getContent()).thenReturn(List.of(Mockito.mock(Activity.class)));

    final PagedEntity<ActivityDto> result = activityService.getActivitiesForTicket(id, number, size);

    // then
    verify(activityRepository, times(1)).findActivitesForTicket(eq(id), any(Pageable.class));
    verify(page, times(1)).hasNext();
    verify(page, times(1)).hasPrevious();
    verify(page, times(1)).getContent();
    verify(activityMapper, times(1)).mapToActivityDto(any(Activity.class));

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldReturn_activity_forId() {
    // mock
    final Activity activity = Mockito.mock(Activity.class);
    // given
    final Long id = 101L;

    // when
    when(activityRepository.findById(id)).thenReturn(Optional.of(activity));
    final Activity result = activityService.getActivity(id);

    // then
    verify(activityRepository, times(1)).findById(id);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldThrowException_whenGetActivity_forInvalidId() {
    // given
    final Long id = 300L;

    // when
    when(activityRepository.findById(id)).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> activityService.getActivity(id))
            .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldReturn_activityDto_whenUpdateActivity() {
    // mock
    final Activity activity = Mockito.mock(Activity.class);
    final ActivityDto activityDto = Mockito.mock(ActivityDto.class);


    // when
    when(activityMapper.mapToActivityDto(activity)).thenReturn(activityDto);
    final ActivityDto result = activityService.updateActivity(activity);

    // then
    verify(activityRepository, times(1)).save(activity);
    verify(activityMapper, times(1)).mapToActivityDto(activity);

    Assertions.assertThat(result).isNotNull();
  }

  @Test
  void shouldDelete_activity() {
    // given
    final Activity activity = Mockito.mock(Activity.class);

    // when
    activityService.deleteActivity(activity);

    // then
    verify(activityRepository, times(1)).delete(activity);
  }
}
