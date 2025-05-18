package com.domain.nfr_taxonomy;

import com.application.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class NFRTaxonomyMap {
    private final List<String> topLayer;
    private final List<NFRTaxonomyItem> bottomLayer;

    public NFRTaxonomyMap() {
        var path = Config.NFRTaxonomy;
        topLayer = new ArrayList<>();
        bottomLayer = new ArrayList<>();
        // read both layers from the config
    }

    public Collection<String> readNFRs() {
        return topLayer;
    }

    public Collection<String> unpackNFRSequence(Iterable<Integer> priorities) {
        // reorder topLayer using priorities
        // use NFRTaxonomyItem.evaluate() to map top layer to bottom layer items
        // return the names of selected bottom layer items
        return null;
    }
}
