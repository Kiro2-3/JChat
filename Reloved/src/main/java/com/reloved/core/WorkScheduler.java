package com.reloved.core;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class WorkScheduler {

    public static void scheduleSync(Context context) {
        try {
            // Define constraints: Wi-Fi and Charging
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresCharging(true)
                    .build();

            // Periodic sync every 6 hours
            PeriodicWorkRequest syncRequest =
                    new PeriodicWorkRequest.Builder(SyncWorker.class, 6, TimeUnit.HOURS)
                            .setConstraints(constraints)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "RelovedSync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
            );
            System.out.println("RelovedSync scheduled successfully.");
        } catch (Exception e) {
            System.err.println("Failed to schedule work (likely not running on Android): " + e.getMessage());
        }
    }
}
