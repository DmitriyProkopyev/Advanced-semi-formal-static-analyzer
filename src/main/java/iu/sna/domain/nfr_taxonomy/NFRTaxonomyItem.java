package iu.sna.domain.nfr_taxonomy;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NFRTaxonomyItem {
    private final Map<String, Integer> effects;

    public NFRTaxonomyItem(JSONObject config) {
        effects = new HashMap<>();
        // read the json object, initialize the map
    }

    public boolean evaluate(Collection<String> topLayerItems) {
        // should this item be selected when unpacking the top layer?
        return true;
    }
}
