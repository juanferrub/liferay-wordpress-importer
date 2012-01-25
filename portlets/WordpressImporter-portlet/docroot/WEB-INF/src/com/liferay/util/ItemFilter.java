package com.liferay.util;

import org.w3c.dom.Element;

/**
 * @author Jelmer Kuperus
 */
public interface ItemFilter {

    boolean ignoreElement(Element itemElement);
}
