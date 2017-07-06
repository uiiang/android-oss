package com.kickstarter.viewmodels.outputs;


import com.kickstarter.models.Project;

import rx.Observable;

public interface CreatorDashboardHeaderHolderViewModelOutputs {
  /* localized count of number of backers */
  Observable<String> projectBackersCountText();

  /* current project's name */
  Observable<String> projectNameTextViewText();

  /* percentage funded text */
  Observable<String> percentageFundedTextViewText();

  /* project that is currently being viewed */
  Observable<Project> currentProject();

  /* time remaining for latest project (no units) */
  Observable<String> timeRemainingText();
}
