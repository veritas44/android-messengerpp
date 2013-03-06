package org.solovyev.android.messenger.sync;

import android.content.Context;
import android.widget.Toast;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
* User: serso
* Date: 3/6/13
* Time: 5:42 PM
*/
public class MessengerSyncAllAsyncTask extends MessengerAsyncTask<Void, Void, Void> {

    @Nullable
    private final Realm realm;

    @Nonnull
    private final SyncService syncService;

    private MessengerSyncAllAsyncTask(@Nonnull Context context, @Nonnull SyncService syncService, @Nullable Realm realm) {
        super(context);
        this.realm = realm;
        this.syncService = syncService;
    }

    @Nonnull
    public static MessengerSyncAllAsyncTask newForAllRealms(@Nonnull Context context, @Nonnull SyncService syncService) {
        return new MessengerSyncAllAsyncTask(context, syncService, null);
    }

    @Nonnull
    public static MessengerSyncAllAsyncTask newForRealm(@Nonnull Context context, @Nonnull SyncService syncService, @Nonnull Realm realm) {
        return new MessengerSyncAllAsyncTask(context, syncService, realm);
    }

    @Override
    protected Void doWork(@Nonnull List<Void> voids) {
        Context context = getContext();
        if (context != null) {
            try {
                if (realm == null) {
                    syncService.syncAll(true);
                } else {
                    syncService.syncAllInRealm(realm, true);
                }
            } catch (SyncAllTaskIsAlreadyRunning e) {
                throwException(e);
            }
        }
        return null;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable Void result) {
    }

    @Override
    protected void onFailurePostExecute(@Nonnull Exception e) {
        if (e instanceof SyncAllTaskIsAlreadyRunning) {
            final Context context = getContext();
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.sync_task_is_running), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onFailurePostExecute(e);
        }
    }
}