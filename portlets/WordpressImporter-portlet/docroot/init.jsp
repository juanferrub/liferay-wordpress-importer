<%--
/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/> 
 */
--%>

<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>
<%@ page import="com.liferay.portlet.PortletPreferencesFactoryUtil" %>
<%@ page import="com.liferay.portal.kernel.util.*" %>

<%@ page import="javax.portlet.PortletPreferences" %>

<portlet:defineObjects/>

<liferay-theme:defineObjects />

<%
PortletPreferences preferences = renderRequest.getPreferences();

String portletResource = ParamUtil.getString(request, "portletResource");

if (Validator.isNotNull(portletResource)) {
	preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
}

boolean importBlogEntries = GetterUtil.getBoolean(preferences.getValue("importBlogEntries", null), true);
boolean importCategories = GetterUtil.getBoolean(preferences.getValue("importCategories", null), true);
boolean importPages = GetterUtil.getBoolean(preferences.getValue("importPages", null), true);
boolean importTags = GetterUtil.getBoolean(preferences.getValue("importTags", null), true);
String wordpressVocabularyName = GetterUtil.getString(preferences.getValue("wordpressVocabularyName", null), StringPool.BLANK);
String blogTags = GetterUtil.getString(preferences.getValue("blogTags", null), StringPool.BLANK);
String userMappings = GetterUtil.getString(preferences.getValue("userMappings", null), StringPool.BLANK);

themeDisplay.setIncludeServiceJs(true);
%>