package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsFactory;
import com.kickstarter.factories.ProjectsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class CreatorDashboardHeaderHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardHeaderHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> projectBackersCountText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> timeRemainingText = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);


  }

  @Test
  public void testProjectBackersCountText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(10).build();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.projectBackersCountText.assertValues("10");
  }

  @Test
  public void testProjectNameTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.projectNameTextViewText.assertValues("somebody once told me");
  }

  @Test
  public void testTimeRemainingText() {
    final Project project = ProjectFactory.project().toBuilder().deadline(new DateTime().plusDays(10)).build();
    final int deadlineVal = ProjectUtils.deadlineCountdownValue(project);
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.timeRemainingText().subscribe(this.timeRemainingText);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.timeRemainingText.assertValues(NumberUtils.format(deadlineVal));
  }
}
