package iu.sna.domain.nfr_taxonomy;

import iu.sna.application.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class NFRTaxonomyMap {
    private final List<String> topLayer;
    private final List<NFRTaxonomyItem> bottomLayer;

    public NFRTaxonomyMap() {
        this.topLayer = Config.NFRs;
        this.bottomLayer = new ArrayList<>();

        for (String name : Config.NFRMapping.keySet()) {
            var item = new NFRTaxonomyItem(name, Config.NFRMapping.getJSONObject(name));
            this.bottomLayer.add(item);
        }
    }

    public Collection<String> readNFRs() {
        return topLayer;
    }

    public Collection<String> unpackNFRSequence(Iterable<Integer> priorities) {
        var prioritizedNFRs = new HashMap<String, Double>();
        int priority = this.topLayer.size() - 1;

        for (int index : priorities) {
            String nfr = this.topLayer.get(index);
            double value = Math.pow(Config.priorityFactor, priority--);
            prioritizedNFRs.put(nfr, value);
        }

        var practices = this.bottomLayer.stream().
                filter((item) -> item.evaluate(prioritizedNFRs)).
                map((item) -> item.name).
                toList();
        return practices;
    }
}
