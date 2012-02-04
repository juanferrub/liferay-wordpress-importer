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

<c:choose>
	<c:when test="<%= themeDisplay.isSignedIn() %>">
		<label><liferay-ui:icon image="search" /><liferay-ui:message key="upload-your-wordpress-file" />:</label>
		
		<form action="<portlet:actionURL name="importWordpressSite" />" class="uni-form" enctype="multipart/form-data" method="post" name="<portlet:namespace />fm">	
			<input type="hidden" name="<portlet:namespace />redirect" value="<portlet:renderURL />" />
			
			<input id="<portlet:namespace />file" name="<portlet:namespace />file" type="file" />
			
			<br></br>
			
			<div class="button-holder">
				<input type="submit" value="Upload" />
			</div>
		</form>
	</c:when>
	<c:otherwise>
		<liferay-ui:message key="you-need-to-be-logged-in-to-use-the-wordpress-importer-portlet" /> (<a href="<%= themeDisplay.getURLSignIn() %>" target="_top"><liferay-ui:message key="sign-in" /></a>)
	</c:otherwise>
</c:choose>
		