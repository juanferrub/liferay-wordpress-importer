package com.liferay.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.liferay.portal.kernel.util.StringPool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jelmer Kuperus
 */
public class TagFilter implements ItemFilter {

    public TagFilter(List<String> tags) {
        this._tags = tags;
    }

    public boolean ignoreElement(Element itemElement) {
        if (itemElement == null || _tags.isEmpty()) {
            return false;
        }

        String postType = getPostType(itemElement);

        if (!TYPE_POST.equals(postType)) {
            return false;
        }

        List<String> postTags = getTags(itemElement);

        for (String tag : postTags) {
            if (_tags.contains(tag)) {
                return false;
            }
        }
        
        return true;
    }

    private List<String> getTags(Element itemElement) {
        NodeList categoryList = itemElement.getElementsByTagName("category");

        List<String> result = new ArrayList<String>();

        for (int i = 0; i < categoryList.getLength(); i++) {
            Element element = (Element) categoryList.item(i);

            String elementValue = element.getTextContent().trim();

            String domain = element.getAttribute("domain");
                       
            if ("tag".equals(domain) || "post_tag".equals(domain)) {
                result.add(elementValue);
            }
        }
        return result;
    }

    private String getPostType(Element itemElement) {        
    	String type = StringPool.BLANK;
    	
    	NodeList postTypeList =	itemElement.getElementsByTagName(
			"wp:post_type");
        Element postTypeElement = (Element) postTypeList.item(0);
        NodeList textpostTypeList = postTypeElement.getChildNodes();
        
        type = textpostTypeList.item(0).getNodeValue().trim();
        
        return type;
    }

    private final List<String> _tags;
    private static final String TYPE_POST = "post";
    
}
