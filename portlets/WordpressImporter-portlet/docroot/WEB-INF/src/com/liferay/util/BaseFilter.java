package com.liferay.util;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jelmer Kuperus
 */
public class BaseFilter implements ItemFilter {

    private List<ItemFilter> _filterList = new ArrayList<ItemFilter>();

    public boolean ignoreElement(Element itemElement) {
        for (ItemFilter filter : _filterList) {
            if (filter.ignoreElement(itemElement)) {
                return true;
            }
        }
        
        return false;
    }

    public void addFilter(ItemFilter filter) {
        _filterList.add(filter);
    }

}
