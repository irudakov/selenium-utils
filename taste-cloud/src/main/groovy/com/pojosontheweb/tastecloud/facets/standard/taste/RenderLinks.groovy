package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksImpl
import woko.facets.builtin.RenderLinks
import woko.util.LinkUtil

@FacetKey(name="renderLinks", profileId="standard", targetObjectType=Taste.class)
class RenderLinks extends RenderLinksImpl implements woko.facets.builtin.RenderLinks {

    @Override
    java.util.List<Link> getLinks() {
        def all = new ArrayList(super.getLinks())
        Taste t = (Taste)facetContext.targetObject
        all.add(
            0,
            new Link('#', 'Run')
                .addAttribute('data-toggle', 'modal')
                .addAttribute('data-target', '#runModal')
                .addAttribute('data-taste-id', woko.objectStore.getKey(t))
                .addAttribute('data-taste-name', t.name)
        )
        return all
    }

}