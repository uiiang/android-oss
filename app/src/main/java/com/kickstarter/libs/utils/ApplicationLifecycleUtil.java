package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

import javax.inject.Inject;

public final class ApplicationLifecycleUtil implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    protected
    @Inject
    Koala koala;
    protected
    @Inject
    ApiClientType client;
    protected
    @Inject
    CurrentConfigType config;
    protected
    @Inject
    Logout logout;

    private final KSApplication application;
    private boolean isInBackground = true;

    public ApplicationLifecycleUtil(final @NonNull KSApplication application) {
        this.application = application;
        application.component().inject(this);
    }

    @Override
    public void onActivityCreated(final @NonNull Activity activity, final @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(final @NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(final @NonNull Activity activity) {
        if (isInBackground) {
            koala.trackAppOpen();

            // Facebook: logs 'install' and 'app activate' App Events.
            AppEventsLogger.activateApp(activity);

            // Refresh the config file
            this.client.config()
                    .compose(Transformers.pipeApiErrorsTo(this::handleConfigApiError))
                    .compose(Transformers.neverError())
                    .subscribe(this.config::config);

            isInBackground = false;
        }
    }

    /**
     * Handles a config API error by logging the user out in the case of a 401. We will interpret
     * 401's on the config request as meaning the user's current access token is no longer valid,
     * as that endpoint should never 401 othewise.
     */
    private void handleConfigApiError(final @NonNull ErrorEnvelope error) {
        if (error.httpCode() == 401) {
            logout.execute();
            ApplicationUtils.startNewDiscoveryActivity(this.application);
        }
    }

    @Override
    public void onActivityPaused(final @NonNull Activity activity) {
        // Facebook: logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(activity);
    }

    @Override
    public void onActivityStopped(final @NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(final @NonNull Activity activity, final @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(final @NonNull Activity activity) {
    }

    @Override
    public void onConfigurationChanged(final @NonNull Configuration configuration) {
    }

    /**
     * OnLowMemory是Android提供的API，在系统内存不足，所有后台程序（优先级为background的进程，不是指后台运行的进程）都被杀死时，系统会调用OnLowMemory。系统提供的回调有：
     * Application.onLowMemory()
     * Activity.OnLowMemory()
     * Fragement.OnLowMemory()
     * Service.OnLowMemory()
     * ContentProvider.OnLowMemory()
     */
    @Override
    public void onLowMemory() {
        koala.trackMemoryWarning();
    }

    /**
     * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
     * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
     * http://developer.android.com/training/articles/memory.html
     */
    /**
     * OnTrimMemory是Android 4.0之后提供的API，系统会根据不同的内存状态来回调。系统提供的回调有：
     * Application.onTrimMemory()
     * Activity.onTrimMemory()
     * Fragement.OnTrimMemory()
     * Service.onTrimMemory()
     * ContentProvider.OnTrimMemory()
     * OnTrimMemory的参数是一个int数值，代表不同的内存状态：
     * TRIM_MEMORY_COMPLETE：内存不足，并且该进程在后台进程列表最后一个，马上就要被清理
     * TRIM_MEMORY_MODERATE：内存不足，并且该进程在后台进程列表的中部。
     * TRIM_MEMORY_BACKGROUND：内存不足，并且该进程是后台进程。
     * TRIM_MEMORY_UI_HIDDEN：内存不足，并且该进程的UI已经不可见了。
     * 以上4个是4.0增加
     * TRIM_MEMORY_RUNNING_CRITICAL：内存不足(后台进程不足3个)，并且该进程优先级比较高，需要清理内存
     * TRIM_MEMORY_RUNNING_LOW：内存不足(后台进程不足5个)，并且该进程优先级比较高，需要清理内存
     * TRIM_MEMORY_RUNNING_MODERATE：内存不足(后台进程超过5个)，并且该进程优先级比较高，需要清理内存
     * 以上3个是4.1增加
     * 系统也提供了一个ComponentCallbacks2，通过Context.registerComponentCallbacks()注册后，就会被系统回调到。
     */
    @Override
    public void onTrimMemory(final int i) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            koala.trackAppClose();
            isInBackground = true;
        }
    }
    /**
     1，OnLowMemory被回调时，已经没有后台进程；而onTrimMemory被回调时，还有后台进程。
     2，OnLowMemory是在最后一个后台进程被杀时调用，一般情况是low memory killer 杀进程后触发；而OnTrimMemory的触发更频繁，每次计算进程优先级时，只要满足条件，都会触发。
     3，通过一键清理后，OnLowMemory不会被触发，而OnTrimMemory会被触发一次。
     */
}
