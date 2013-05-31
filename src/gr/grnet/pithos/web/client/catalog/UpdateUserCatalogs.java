/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 *   1. Redistributions of source code must retain the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and
 * documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed
 * or implied, of GRNET S.A.
 */

package gr.grnet.pithos.web.client.catalog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;

import java.util.List;
import java.util.Map;

/**
 * This is a wrapper around GetUserCatalogs that takes care of updating
 * the application-wide user catalogs.
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
        for(Map.Entry<String, String> uc : requestedUserCatalogs) {
//            app.LOG("New displayName ", uc.getValue());
        }
    }

    public void onError(Request request, Throwable t) {
        getUserCatalogs.onError(request, t);
    }

    public UpdateUserCatalogs(Pithos app, List<String> ids, List<String> names) {
        this.app = app;
        this.getUserCatalogs = new GetUserCatalogs(app.getUserToken(), ids, names) {
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

    public void scheduleEntry() {
        Scheduler.get().scheduleEntry(this);
    }
}
