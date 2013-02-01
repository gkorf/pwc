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

    public boolean hasID(String id) {
        return id2name.containsKey(id);
    }

    public boolean hasName(String email) {
        return name2id.containsKey(email);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return id2name.entrySet().iterator();
    }
}
