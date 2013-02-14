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

import gr.grnet.pithos.web.client.Helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds one-to-one mappings from user IDs to respective user display names.
 *
 * The iterator returns map entries with id as the key and name as the value.
 */
public class UserCatalogs implements Iterable<Map.Entry<String, String>>{
    private HashMap<String, String> id2name = new HashMap<String, String>();
    private HashMap<String, String> name2id = new HashMap<String, String>();

    public UserCatalogs() {
        this(new HashMap<String, String>(), new HashMap<String, String>());
    }

    private UserCatalogs(HashMap<String, String> id2name, HashMap<String, String> name2id) {
        this.id2name = id2name;
        this.name2id = name2id;
    }

    public UserCatalogs updateWithIDAndName(String id, String name) {
        this.id2name.put(id, name);
        this.name2id.put(name, id);

        return this;
    }

    public UserCatalogs updateFrom(UserCatalogs other) {
        for(Map.Entry<String, String> otherIDAndName : other) {
            this.updateWithIDAndName(otherIDAndName.getKey(), otherIDAndName.getValue());
        }
        return this;
    }

    public UserCatalogs copy() {
        return new UserCatalogs(
            Helpers.copyHashMap(id2name),
            Helpers.copyHashMap(name2id)
        );
    }

    public boolean hasID(String userID) {
        return id2name.containsKey(userID);
    }

    public boolean hasDisplayName(String displayName) {
        return name2id.containsKey(displayName);
    }

    public String getDisplayName(String userID) {
        return id2name.get(userID);
    }

    public String getID(String displayName) {
        return name2id.get(displayName);
    }

    /**
     * Returns an iterator of <code>(UUID, DisplayName)</code> pairs.
     */
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return id2name.entrySet().iterator();
    }
}
