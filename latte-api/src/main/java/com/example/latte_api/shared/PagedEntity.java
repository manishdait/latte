package com.example.latte_api.shared;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagedEntity<T> {
  private List<T> content;
  private Boolean previous;
  private Boolean next;
  private long totalElement;
}
