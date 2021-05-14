package it.bz.opendatahub.webcomponents.dataservice.application.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import it.bz.opendatahub.webcomponents.dataservice.application.domain.GoogleLighthouseMetrics;
import it.bz.opendatahub.webcomponents.dataservice.application.domain.WebcomponentVersion;
import it.bz.opendatahub.webcomponents.dataservice.application.port.in.UpdateWebcomponentVersionMetricsUseCase;
import it.bz.opendatahub.webcomponents.dataservice.application.port.out.GoogleLighthousePort;
import it.bz.opendatahub.webcomponents.dataservice.application.port.out.WriteWebcomponentVersionPort;
import it.bz.opendatahub.webcomponents.dataservice.exception.impl.MetricsErrorException;
import it.bz.opendatahub.webcomponents.dataservice.exception.impl.MetricsInvalidRequestException;
import it.bz.opendatahub.webcomponents.dataservice.exception.impl.MetricsUnavailableException;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Slf4j
@Service
public class WebcomponentVersionMetricsService implements UpdateWebcomponentVersionMetricsUseCase {
	@Value("${application.previewBaseUrl}")
	private String previewBaseUrl;

	private final GoogleLighthousePort googleLighthousePort;
	private final WriteWebcomponentVersionPort writeWebcomponentVersionPort;

	public WebcomponentVersionMetricsService(GoogleLighthousePort googleLighthousePort, WriteWebcomponentVersionPort writeWebcomponentVersionPort) {
		this.googleLighthousePort = googleLighthousePort;
		this.writeWebcomponentVersionPort = writeWebcomponentVersionPort;
	}

	@Override
	public WebcomponentVersion updateMetricsForWebcomponentVersion(@NonNull WebcomponentVersion webcomponentVersion) {
		log.debug(
			"updating metrics for: {}/{}",
			webcomponentVersion.getWebcomponentUuid(),
			webcomponentVersion.getVersionTag()
		);

		val previewUrl = getPreviewUrlForWebcomponentVersion(webcomponentVersion);

		try {
			loadAndUpdateMetricsData(webcomponentVersion, previewUrl);

			log.debug("metrics gathered");

			return writeWebcomponentVersionPort.saveWebcomponentVersion(
				webcomponentVersion
			);
		}
		catch (MetricsUnavailableException e) {
			log.error(
				"metrics failed for: {}/{}",
				webcomponentVersion.getWebcomponentUuid(),
				webcomponentVersion.getVersionTag()
			);

			return webcomponentVersion;
		}
	}

	private void loadAndUpdateMetricsData(WebcomponentVersion webcomponentVersion, String previewUrl) throws MetricsUnavailableException {
		try {
			val metrics = fetchMetrics(previewUrl);

			webcomponentVersion.setLighthouseMetricsMobileData(metrics.getResultMobile());
			webcomponentVersion.setLighthouseMetricsDesktopData(metrics.getResultMobile());

			updateRatings(webcomponentVersion, metrics);

			webcomponentVersion.setLighthouseMetricsDesktopDatetime(LocalDateTime.now());
			webcomponentVersion.setLighthouseMetricsMobileDatetime(LocalDateTime.now());

			webcomponentVersion.setLighthouseUpdateRequired(false);
		}
		catch (MetricsErrorException | MetricsInvalidRequestException e) {
			webcomponentVersion.setLighthouseMetricsMobileData(null);
			webcomponentVersion.setLighthouseMetricsDesktopData(null);

			webcomponentVersion.setLighthouseDesktopPerformanceRating(0);
			webcomponentVersion.setLighthouseMobilePerformanceRating(0);

			webcomponentVersion.setLighthouseMetricsDesktopDatetime(LocalDateTime.now());
			webcomponentVersion.setLighthouseMetricsMobileDatetime(LocalDateTime.now());

			webcomponentVersion.setLighthouseUpdateRequired(false);
		}
	}

	private void updateRatings(WebcomponentVersion webcomponentVersion, GoogleLighthouseMetrics metrics) {
		try {
			webcomponentVersion.setLighthouseMobilePerformanceRating(
				extractPerformanceRating(
					metrics.getResultMobile()
				)
			);
		} catch (MetricsParseException e) {
			webcomponentVersion.setLighthouseMobilePerformanceRating(0);
		}

		try {
			webcomponentVersion.setLighthouseDesktopPerformanceRating(
				extractPerformanceRating(
					metrics.getResultDesktop()
				)
			);
		} catch (MetricsParseException e) {
			webcomponentVersion.setLighthouseDesktopPerformanceRating(0);
		}
	}

	private GoogleLighthouseMetrics fetchMetrics(String previewUrl) throws MetricsUnavailableException, MetricsInvalidRequestException, MetricsErrorException {
		return googleLighthousePort.getMetricsForUrl(previewUrl);
	}

	private String getPreviewUrlForWebcomponentVersion(WebcomponentVersion webcomponentVersion) {
		return previewBaseUrl + "/" + webcomponentVersion.getWebcomponentUuid() + "/" + webcomponentVersion.getVersionTag() + getUrlAttributesFromConfig(webcomponentVersion);
	}

	private int extractPerformanceRating(String json) throws MetricsParseException {
		Number rating;
		try {
			rating = JsonPath.read(json, "$.lighthouseResult.categories.performance.score");
		}
		catch (PathNotFoundException e) {
			throw new MetricsParseException();
		}

		if(rating == null) {
			throw new MetricsParseException();
		}

		return (int) (rating.doubleValue() * 100);
	}

	private String getUrlAttributesFromConfig(WebcomponentVersion webcomponentVersion) {
		val options = webcomponentVersion.getConfiguration().getOptions();

		val attribs = new ArrayList<String>();

		for(val option : options) {
			if(!(option.getOptions() instanceof LinkedHashMap)) {
				continue;
			}

			@SuppressWarnings("unchecked")
			val innerOptions = (LinkedHashMap<String, Object>)option.getOptions();

			if(optionIsRequiredOrHasDefaultValue(option, innerOptions)) {
				attribs.add(
					option.getKey()+"=\""+ urlencode(String.valueOf(innerOptions.get("default"))) +"\""
				);
			}
		}

		if(attribs.isEmpty()) {
			return "";
		}

		return "?attribs="+String.join(";", attribs);
	}

	@SneakyThrows(UnsupportedEncodingException.class)
	private String urlencode(String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8.toString());
	}

	private boolean optionIsRequiredOrHasDefaultValue(it.bz.opendatahub.webcomponents.common.data.struct.Configuration.Options option, LinkedHashMap<String, Object> innerOptions) {
		return (option.getRequired() != null && option.getRequired()) || innerOptions.containsKey("default");
	}

	private static class MetricsParseException extends Exception {}
}
