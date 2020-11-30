package com.github.paganini2008.springdessert.webcrawler;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.pool2.PooledObject;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.paganini2008.devtools.RandomUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * HtmlUnitPageExtractor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class HtmlUnitPageExtractor extends PageExtractorSupport<WebClient> implements PageExtractor {

	@Override
	public WebClient createObject() throws Exception {
		WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
		webClient.addRequestHeader("User-Agent", RandomUtils.randomChoice(userAgents));
		Map<String, String> defaultHeaders = getDefaultHeaders();
		if (MapUtils.isNotEmpty(defaultHeaders)) {
			for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
				webClient.addRequestHeader(entry.getKey(), entry.getValue());
			}
		}
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setActiveXNative(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setRedirectEnabled(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setTimeout(60 * 1000);
		webClient.setCookieManager(new CookieManager());
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.setJavaScriptTimeout(60 * 1000);
		return webClient;
	}

	@Override
	public void destroyObject(PooledObject<WebClient> object) throws Exception {
		object.getObject().close();
	}

	@Override
	public String extractHtml(String url) throws Exception {
		WebClient webClient = objectPool.borrowObject();
		try {
			Page page = webClient.getPage(url);
			if (page.getWebResponse().getStatusCode() == 200) {
				if (page instanceof HtmlPage) {
					return ((HtmlPage) page).asXml();
				} else if (page instanceof TextPage) {
					return ((TextPage) page).getContent();
				}
			}
			throw new PageExtractorException(url);
		} finally {
			if (webClient != null) {
				objectPool.returnObject(webClient);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> getDefaultHeaders() {
		return Collections.EMPTY_MAP;
	}

	public static void main(String[] args) throws Exception {
		HtmlUnitPageExtractor pageSource = new HtmlUnitPageExtractor();
		pageSource.configure();
		// System.out.println(pageSource.getHtml("https://blog.csdn.net/u010814849/article/details/52526705"));
		System.out.println(pageSource.extractHtml("https://gny.ly.com/line/t3j1p1137406c321.html?dk=EE9B7E7003DCE18A36C92D38C527C10AA9E1D53C8764A23A"));
		System.in.read();
		pageSource.destroy();
	}

}
