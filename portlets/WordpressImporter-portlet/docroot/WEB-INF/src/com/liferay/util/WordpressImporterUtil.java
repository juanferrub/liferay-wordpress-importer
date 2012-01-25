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

package com.liferay.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.NoSuchWorkflowDefinitionLinkException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.User;
import com.liferay.portal.model.WorkflowDefinitionLink;
import com.liferay.portal.service.LayoutServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.WorkflowDefinitionLinkLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.asset.DuplicateCategoryException;
import com.liferay.portlet.asset.DuplicateTagException;
import com.liferay.portlet.asset.DuplicateVocabularyException;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetCategoryServiceUtil;
import com.liferay.portlet.asset.service.AssetTagServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyServiceUtil;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleServiceUtil;

/**
 * @author Juan Fernández
 * @author Jelmer Kuperus
 */

public class WordpressImporterUtil {

	public static Map<String, Integer> processFile(File file,
			ActionRequest request) {

		Map<String, Integer> results = new HashMap<String, Integer>();

		_categoriesCount = 0;
		_entriesCount = 0;
		_pagesCount = 0;
		_tagsCount = 0;

		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setAddCommunityPermissions(true);

		serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());

		List<String> importedTagNames = new ArrayList<String>();
		List<String> importedCategoryNames = new ArrayList<String>();
		Map<String, Layout> parentLayouts = new HashMap<String, Layout>();

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file), "UTF-8");
			InvalidXmlFilterReader ixfr = new InvalidXmlFilterReader(isr);
			Document doc = docBuilder.parse(new InputSource(ixfr));

			doc.getDocumentElement().normalize();

			NodeList listOfItems = doc.getElementsByTagName("item");

			for (int s = 0; s < listOfItems.getLength(); s++) {

				Node item = listOfItems.item(s);

				if (item.getNodeType() == Node.ELEMENT_NODE) {

					Element itemElement = (Element) item;

					// Title of the item

					NodeList titleList = 
						itemElement.getElementsByTagName("title");
					Element titleElement = (Element) titleList.item(0);
					String titleValue = titleElement.getTextContent().trim();

					// Item type

					NodeList postTypeList = 
						itemElement.getElementsByTagName("wp:post_type");
					Element postTypeElement = (Element) postTypeList.item(0);
					NodeList textpostTypeList = postTypeElement.getChildNodes();
					String postTypeValue = ((Node) textpostTypeList.item(0))
							.getNodeValue().trim();

					// Post id

					NodeList postIdList = 
						itemElement.getElementsByTagName("wp:post_id");
					Element postIdElement = (Element) postIdList.item(0);
					NodeList textpostIdList = postIdElement.getChildNodes();
					String postIdValue = 
						((Node)textpostIdList.item(0)).getNodeValue().trim();

					// Post parent

					NodeList postParentList = 
						itemElement.getElementsByTagName("wp:post_parent");
					Element postParentElement = 
						(Element) postParentList.item(0);
					NodeList textpostParentList = 
						postParentElement.getChildNodes();
					String postParentValue = StringPool.BLANK;

					if (textpostParentList.getLength() > 0) {
						Node postParentNode = 
							((Node) textpostParentList.item(0));

						postParentValue = postParentNode.getNodeValue().trim();
					}

					// Content

					NodeList contentList = 
						itemElement.getElementsByTagName("content:encoded");
					Element contentElement = (Element) contentList.item(0);
					NodeList textContentList = contentElement.getChildNodes();
					String contentValue = StringPool.BLANK;

					if (textContentList.getLength() > 0) {
						Node contentNode = ((Node) textContentList.item(0));
						contentValue = contentNode.getNodeValue().trim();
					}
					
					contentValue = formatContent(contentValue);

					// Description

					NodeList descriptionList = 
						itemElement.getElementsByTagName("description");
					Element descriptionElement = 
						(Element) descriptionList.item(0);
					NodeList textDescriptionList = 
						descriptionElement.getChildNodes();
					String descriptionValue = StringPool.BLANK;

					if (textDescriptionList.getLength() > 0) {
						Node descriptionNode = 
							(Node)textDescriptionList.item(0);
						descriptionValue = 
							descriptionNode.getNodeValue().trim();
					}

					// Item Link

					NodeList linkList = 
						itemElement.getElementsByTagName("link");
					Element linkElement = (Element) linkList.item(0);
					NodeList textLinkList = linkElement.getChildNodes();
					String linkValue = StringPool.BLANK;

					if (textLinkList.getLength() > 0) {
						linkValue = 
							((Node)textLinkList.item(0)).getNodeValue().trim();
					}

					// Configuration preferences

					PortletPreferences preferences = request.getPreferences();

					boolean importBlogEntries = 
						GetterUtil.getBoolean(preferences.getValue(
							"importBlogEntries", StringPool.TRUE));

					boolean importTags = GetterUtil.getBoolean(
						preferences.getValue("importTags", StringPool.TRUE));

					boolean importCategories = 
						GetterUtil.getBoolean(preferences.getValue("importTags",
							StringPool.TRUE));

					boolean importPages = 
						GetterUtil.getBoolean(preferences.getValue(
							"importPages", StringPool.TRUE));

					BaseFilter filter = new BaseFilter();

                    String[] assetTagNames = 
                    	StringUtil.split(GetterUtil.getString(
                			preferences.getValue("blogTags", 
            					StringPool.BLANK)));

                    filter.addFilter(
                    		new TagFilter(Arrays.asList(assetTagNames)));
                    
                    if (filter.ignoreElement(itemElement)) {
                        continue;
                    }
                    		
                    Map<String, String> userMapping = 
                    	new HashMap<String, String>();

                    String userMappings = 
                    	GetterUtil.getString(preferences.getValue(
                			"userMappings", StringPool.BLANK));

                    BufferedReader reader = new BufferedReader(
                        new StringReader(userMappings));

                    String line = StringPool.BLANK;
                    
                    while((line = reader.readLine()) != null) {
                        String[] pair = line.split("=");
                        
                        if (pair.length == 2) {
                            userMapping.put(pair[0], pair[1]);
                        }
                        else {
                            System.err.println("Line \"" + line + "\" " +
                            	"is not a valid user mapping");
                        }
                    }
                    
					String wordpressVocabularyName = 
						GetterUtil.getString(preferences.getValue(
							"wordpressVocabularyName", 
							DEFAULT_VOCABULARY_NAME));

                    if (Validator.isNotNull(wordpressVocabularyName)) {
						DEFAULT_VOCABULARY_NAME = wordpressVocabularyName;
					}
                  
					if (WordpressImporterUtil.TYPE_PAGE.equals(postTypeValue)
							&& importPages) {

						// Manage pages

						addPageAndContent(themeDisplay, serviceContext,
								parentLayouts, titleValue, postIdValue,
								postParentValue, descriptionValue, 
								contentValue);

						_pagesCount++;

					} else if (WordpressImporterUtil.TYPE_POST
							.equals(postTypeValue)) {

						if (Validator.isNotNull(contentValue)) {

							// Manage blog entries

							addBlogEntry(themeDisplay, serviceContext,
									importedTagNames, importedCategoryNames,
									itemElement, titleValue, contentValue,
									importBlogEntries, importTags,
									importCategories, userMapping);
						}
					}
				}
			}
		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());
		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		finally {

			results.put("categoriesCount", _categoriesCount);
			results.put("entriesCount", _entriesCount);
			results.put("tagsCount", _tagsCount);
			results.put("pagesCount", _pagesCount);

			System.out.println("Import ended");
		}

		return results;
	}

	private static void addBlogEntry(ThemeDisplay themeDisplay,
			ServiceContext serviceContext, List<String> importedTagNames,
			List<String> importedCategoryNames, Element itemElement,
			String title, String content, boolean importBlogEntries,
			boolean importTags, boolean importCategories,
            Map<String, String> userMapping) {

		try {

			// Create vocabulary, tags and categories & add them to the
			// serviceContext

			if (WORDPRESS_VOCABULARY == null) {
				try {
					Map<Locale, String> vocabularyTitleMap = 
						new HashMap<Locale, String>();
					Map<Locale, String> vocabularydescriptionMap = 
						new HashMap<Locale, String>();

					vocabularyTitleMap.put(LocaleUtil.getDefault(),
							DEFAULT_VOCABULARY_NAME);

					WORDPRESS_VOCABULARY = 
						AssetVocabularyServiceUtil.addVocabulary(
							vocabularyTitleMap, vocabularydescriptionMap, null,
							serviceContext);
					
				} catch (DuplicateVocabularyException dve) {
					List<AssetVocabulary> vocabularies = 
						AssetVocabularyServiceUtil.getGroupVocabularies(
							themeDisplay.getScopeGroupId());

					for (AssetVocabulary vocabulary : vocabularies) {
						String vocabularyTitle = 
							vocabulary.getTitle(LocaleUtil.getDefault());
						
						if (vocabularyTitle.equals(DEFAULT_VOCABULARY_NAME)) {
							WORDPRESS_VOCABULARY = vocabulary;
						}
					}
				}
			}

			NodeList categoryList = 
				itemElement.getElementsByTagName("category");

			List<String> assetTagNames = new ArrayList<String>();
			List<Long> assetCategoryIds = new ArrayList<Long>();

			Element element = null;

			for (int i = 0; i < categoryList.getLength(); i++) {

				element = (Element) categoryList.item(i);
				NodeList elementList = element.getChildNodes();
				String elementValue = 
					((Node) elementList.item(0)).getNodeValue().trim();

				String domain = element.getAttribute("domain");

				try {
					if (("tag".equals(domain) || "post_tag".equals(domain)) && 
						importTags) {

						assetTagNames.add(elementValue);

						if (!importedTagNames.contains(elementValue)) {
							AssetTagServiceUtil.addTag(elementValue, null,
								serviceContext);
							importedTagNames.add(elementValue);
						}

						_tagsCount++;
					} 
					else if ("category".equals(domain) && importCategories) {

						if (!importedCategoryNames.contains(elementValue)) {
							Map<Locale, String> titleMap = 
								new HashMap<Locale, String>();
							
							Map<Locale, String> descriptionMap = 
									new HashMap<Locale, String>();
							
							titleMap.put(LocaleUtil.getDefault(), elementValue);
							
							AssetCategory category = 
								AssetCategoryServiceUtil.addCategory(0, 
									titleMap, descriptionMap,
									WORDPRESS_VOCABULARY.getVocabularyId(), 
									null, serviceContext);

							importedCategoryNames.add(elementValue);
							assetCategoryIds.add(category.getCategoryId());

							_categoriesCount++;
						}
					}
				} 
				catch (DuplicateTagException dte) {
				} 
				catch (DuplicateCategoryException dce) {

					// Search for the category and add it to the service Context

					List<AssetCategory> vocCategories = 
						AssetCategoryServiceUtil.getVocabularyRootCategories(
							WORDPRESS_VOCABULARY.getVocabularyId(), -1, -1, 
							null);

					for (AssetCategory cat : vocCategories) {
						if (cat.getTitle(LocaleUtil.getDefault()).equals(
								elementValue)) {
							assetCategoryIds.add(cat.getCategoryId());
						}
					}

				} catch (Exception e) {
					System.err
							.println("Woops! There's been an error creating"
									+ " a tag/category called \""
									+ elementValue + "\"");
					e.printStackTrace();
				}
			}

			try {
				String[] tagNamesArray = new String[assetTagNames.size()];
				assetTagNames.toArray(tagNamesArray);

				serviceContext.setAssetTagNames(tagNamesArray);

				long[] assetCategoryIdsArray = 
					new long[assetCategoryIds.size()];

				for (int i = 0; i < assetCategoryIds.size(); i++) {
					Long assetCategoryId = assetCategoryIds.get(i);
					assetCategoryIdsArray[i] = assetCategoryId.longValue();
				}

				serviceContext.setAssetCategoryIds(assetCategoryIdsArray);

			} catch (Exception e) {
				System.err.println("Couldn't create tags and categories for "
						+ "entry called \"" + title + "\"");
			}

			// Create blog entry

			if (importBlogEntries) {
				System.out.println("Creating blog entry \"" + title + "\"");

				boolean allowPingbacks = true;
				boolean allowTrackbacks = true;
				String[] trackbacks = new String[]{};

				NodeList pubDateList = 
					itemElement.getElementsByTagName("pubDate");

				Date displayDate;

				if (pubDateList.getLength() > 0) {
					Element pubDateElement = (Element) pubDateList.item(0);

					SimpleDateFormat format = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
					displayDate = format.parse(pubDateElement.getTextContent());
				} else {
					displayDate = new Date();
				}

				Calendar calendar = CalendarFactoryUtil.getCalendar(
						TimeZoneUtil.getDefault(), LocaleUtil.getDefault());
				calendar.setTime(displayDate);

				int displayDateMonth = calendar.get(Calendar.MONTH);
				int displayDateDay = calendar.get(Calendar.DAY_OF_MONTH);
				int displayDateYear = calendar.get(Calendar.YEAR);
				int displayDateHour = calendar.get(Calendar.HOUR_OF_DAY);
				int displayDateMinute = calendar.get(Calendar.MINUTE);

				NodeList creatorList = itemElement.getElementsByTagName(
					"dc:creator");

                long userId = themeDisplay.getUserId();

                if (creatorList.getLength() > 0) {
                    String creatorName = creatorList.item(0).getTextContent();
                    String mappedScreenName = userMapping.get(creatorName);

                    if (Validator.isNotNull(mappedScreenName)) {
                    	try {      
	                    	User user = 
	                    		UserLocalServiceUtil.getUserByScreenName(
                    				themeDisplay.getCompanyId(), 
                    				mappedScreenName);
	
	                        userId = user.getUserId();
	                    } catch (NoSuchUserException e) {
	                        System.err.println("User \"" + creatorName +
	                            "\" was mapped to liferay user \"" + 
	                            mappedScreenName + 
	                            "\" but this user does not exist");
	                    }
                    }
                }

                // Entry extra attributes
                
                String description = StringPool.BLANK;
                boolean smallImage = false;
				String smallImageURL = null;
				String smallImageFileName = null;
				InputStream smallImageInputStream = null;
				
				BlogsEntry entry = 
                	BlogsEntryLocalServiceUtil.addEntry(userId, title, description,
                        content, displayDateMonth, displayDateDay,
                        displayDateYear, displayDateHour, displayDateMinute,
                        allowPingbacks, allowTrackbacks, trackbacks,
                        smallImage, smallImageURL, smallImageFileName, smallImageInputStream,
                        serviceContext);

				_entriesCount++;
			}

			// Import comments (TBD)

		} catch (Exception e) {
			System.err.println("Woops! There's been an error importing the "
					+ "post \"" + title + "\"");
			e.printStackTrace();
		}
	}

	private static void addPageAndContent(ThemeDisplay themeDisplay,
			ServiceContext serviceContext, Map<String, Layout> parentLayouts,
			String title, String postId, String postParent, String description,
			String content) 
		throws PortalException, SystemException {

		System.out.println("Creating page  \"" + title + "\"");

		String type = LayoutConstants.TYPE_PORTLET;
		boolean hidden = false;
		String friendlyURL = StringPool.BLANK;

		Layout layout = null;

		try {
			if ("0".equals(postParent)) {
				layout = LayoutServiceUtil.addLayout(
					themeDisplay.getScopeGroupId(), false, 0, title, title,
					description, type, hidden, friendlyURL, serviceContext);

				parentLayouts.put(postId, layout);
			} else if (parentLayouts.containsKey(postParent)) {
				Layout parentLayout = parentLayouts.get(postParent);

				layout = LayoutServiceUtil.addLayout(
					themeDisplay.getScopeGroupId(), false,
					parentLayout.getLayoutId(), title, title, description,
					type, hidden, friendlyURL, serviceContext);

				parentLayouts.put(postId, layout);
			}

			// Create a web content for this page

			JournalArticle article = null;

			try {

				article = addSimpleJournalArticle(
					themeDisplay.getScopeGroupId(), title, description,
					content, serviceContext);
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Publish the web content in an asset publisher

			if ((layout != null) && (article != null)) {
				LayoutTypePortlet layoutTypePortlet = 
					(LayoutTypePortlet) layout.getLayoutType();
				String assetPublisherPortletId = 
					layoutTypePortlet.addPortletId(
							themeDisplay.getUserId(), 
							PortletKeys.JOURNAL_CONTENT, "column-2", -1);

				long companyId = themeDisplay.getCompanyId();
				long ownerId = PortletKeys.PREFS_OWNER_ID_DEFAULT;
				int ownerType = PortletKeys.PREFS_OWNER_TYPE_LAYOUT;

				PortletPreferences prefs = 
					PortletPreferencesLocalServiceUtil.getPreferences(
						companyId, ownerId, ownerType, layout.getPlid(), 
						assetPublisherPortletId);

				prefs.setValue("article-id", article.getArticleId());
				prefs.setValue("group-id",
					String.valueOf(themeDisplay.getScopeGroupId()));

				PortletPreferencesLocalServiceUtil.updatePreferences(ownerId,
					ownerType, layout.getPlid(), assetPublisherPortletId,
					prefs);

				LayoutServiceUtil.updateLayout(layout.getGroupId(),
					layout.isPrivateLayout(), layout.getLayoutId(),
					layout.getTypeSettings());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static JournalArticle addSimpleJournalArticle(long groupId,
			String title, String description, String content,
			ServiceContext serviceContext) throws PortalException,
			SystemException {

		String articleId = StringPool.BLANK;
		boolean autoArticleId = true;
		String contentType = "general";
		boolean neverExpire = true;
		boolean neverReview = true;
		boolean indexable = true;
		boolean smallImage = false;

		Date displayDate = new Date();
		Calendar calendar = CalendarFactoryUtil.getCalendar(
				TimeZoneUtil.getDefault(), LocaleUtil.getDefault());

		calendar.setTime(displayDate);

		int displayDateMonth = calendar.get(Calendar.MONTH);
		int displayDateDay = calendar.get(Calendar.DAY_OF_MONTH);
		int displayDateYear = calendar.get(Calendar.YEAR);
		int displayDateHour = calendar.get(Calendar.HOUR_OF_DAY);
		int displayDateMinute = calendar.get(Calendar.MINUTE);

		String structureId = StringPool.BLANK;
		String templateId = StringPool.BLANK;
		String link = StringPool.BLANK;

		StringBundler sb = new StringBundler(3);

		sb.append(DEFAULT_XML_PREFIX);
		sb.append(content);
		sb.append(DEFAULT_XML_SUFIX);
		
		// Extra article attributes	
		
		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
		
		long classNameId = 0;
		long classPK = 0;
		String layoutUuid = null;
		
		Locale locale = LanguageUtil.getLocale(serviceContext.getLanguageId());
		
		titleMap.put(locale, title);
		descriptionMap.put(locale, description);
		
		return JournalArticleServiceUtil.addArticle(groupId, classNameId, classPK, 
				articleId, autoArticleId, titleMap, descriptionMap, sb.toString(), 
				contentType, structureId, templateId, layoutUuid, displayDateMonth, 
				displayDateDay, displayDateYear, displayDateHour, displayDateMinute, 
				0, 0, 0, 0, 0, neverExpire, 0, 0, 0, 0, 0, neverReview, indexable,
				smallImage, null, null, null, link, serviceContext);
	}
	
	private static String formatContent(String contentValue) {
		
		String result = "<p>" + contentValue + "</p>";
		
		result = result.replace("\n", "</p><p>");
		
		// TODO: format the images correctly
		
		// result = result.replace("<img ", "<img style=\"float:left;\"");
						
		return result;
	}

	private static String DEFAULT_VOCABULARY_NAME = "Wordpress Vocabulary";
	private static final String DEFAULT_XML_PREFIX = 
		"<?xml version='1.0' encoding='UTF-8'?><root available-locales=\""
			+ "en_US\" default-locale=\"en_US\"><static-content "
			+ "language-id=\"en_US\"><![CDATA[<p>";
	private static final String DEFAULT_XML_SUFIX = "</p>]]></static-content>"
			+ "</root>";
	private static final String TYPE_PAGE = "page";
	private static final String TYPE_POST = "post";
	private static AssetVocabulary WORDPRESS_VOCABULARY = null;
	

	private static int _categoriesCount;
	private static int _entriesCount;
	private static int _pagesCount;
	private static int _tagsCount;
}