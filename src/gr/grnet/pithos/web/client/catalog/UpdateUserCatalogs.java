package gr.grnet.pithos.web.client.catalog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;

import java.util.List;

/**
 * This is a wrapper around GetUserCatalogs that takes care of updating
 * the application-wide user catalogs.
 *
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public class UpdateUserCatalogs implements Scheduler.ScheduledCommand {
    private final GetUserCatalogs getUserCatalogs;
    private final Pithos app;

    public UpdateUserCatalogs(Pithos app) {
        this(app, null, null);
    }

    public UpdateUserCatalogs(Pithos app, String userID) {
        this(app, Helpers.toList(userID), null);
    }

    public UpdateUserCatalogs(Pithos app, List<String> ids) {
        this(app, ids, null);
    }

    public void onSuccess(UserCatalogs requestedUserCatalogs, UserCatalogs updatedUserCatalogs) {
    }

    public void onError(Request request, Throwable t) {
        getUserCatalogs.onError(request, t);
    }

    public UpdateUserCatalogs(Pithos app, List<String> ids, List<String> names) {
        this.app = app;
        this.getUserCatalogs = new GetUserCatalogs(app, ids, names) {
            @Override
            public void onSuccess(Request request, Response response, JSONObject result, UserCatalogs userCatalogs) {
                UpdateUserCatalogs.this.app.getUserCatalogs().updateFrom(userCatalogs);
                UpdateUserCatalogs.this.onSuccess(userCatalogs, UpdateUserCatalogs.this.app.getUserCatalogs());
            }

            @Override
            public void onError(Request request, Throwable t) {
                UpdateUserCatalogs.this.onError(request, t);
            }
        };
    }

    @Override
    public void execute() {
        this.getUserCatalogs.execute();
    }

    public void scheduleDeferred() {
        Scheduler.get().scheduleDeferred(this);
    }
}
