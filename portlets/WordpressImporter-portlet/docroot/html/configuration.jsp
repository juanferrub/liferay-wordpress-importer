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

<%@ include file="/init.jsp" %>

<liferay-portlet:actionURL portletConfiguration="true" var="configurationURL" />
<br />

<aui:form action="<%= configurationURL %>" method="post" name="fm">
	<aui:input label="import-blog-entries" name="preferences--importBlogEntries--" type="checkbox" value="<%= importBlogEntries %>" />
	<aui:input label="import-tags" name="preferences--importTags--" type="checkbox" value="<%= importTags %>" />
	<aui:input label="import-categories" name="preferences--importCategories--" type="checkbox" value="<%= importCategories %>" />
	<aui:input label="import-pages" name="preferences--importPages--" type="checkbox" value="<%= importPages %>" />

	<!--  Tag Filter -->
	
    <aui:field-wrapper label="tag-filter" helpMessage="tag-filter-help">
        <div class="lfr-tags-selector-content" id="<portlet:namespace />assetTagsSelector">
            <input id="<portlet:namespace />hiddenInput" name="<portlet:namespace />preferences--blogTags--" type="hidden" />
            <input id="<portlet:namespace />assetTagNames" class="lfr-tag-selector-input" size="15" type="text" />
        </div>

        <aui:script use="liferay-asset-tags-selector">
            new Liferay.AssetTagsSelector(
                {
                    allowSuggestions: false,
                    contentBox: '#<portlet:namespace />assetTagsSelector',
                    curEntries: '<%= HtmlUtil.escapeJS(blogTags) %>',
                    hiddenInput: '#<portlet:namespace />hiddenInput',
                    input: '#<portlet:namespace />assetTagNames',
                    instanceVar: '<portlet:namespace />instance'
                }
            ).render();
        </aui:script>
    </aui:field-wrapper>
    
    <aui:input cssClass="lfr-textarea-container" cols="80" helpMessage="user-mappings-help" label="user-mappings" name="preferences--userMappings--" rows="6" type="textarea" value="<%= userMappings %>"  />

	<br />
	
	<aui:input helpMessage="wordpress-vocabulary-name-help" label="wordpress-vocabulary-name" name="preferences--wordpressVocabularyName--" type="text" value="<%= wordpressVocabularyName %>"  />
	
	<br />
		
	<aui:button-row>
		<aui:button type="submit" />
	</aui:button-row>		
</aui:form>