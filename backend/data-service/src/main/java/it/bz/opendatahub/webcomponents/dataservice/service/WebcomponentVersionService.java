package it.bz.opendatahub.webcomponents.dataservice.service;

import it.bz.opendatahub.webcomponents.dataservice.data.dto.WebcomponentVersionDto;

import java.util.List;

public interface WebcomponentVersionService {
    List<WebcomponentVersionDto> listAllVersionsOfWebcomponent(String webcomponentId);

    WebcomponentVersionDto getLatestVersionOfWebcomponent(String webcomponentId);

    WebcomponentVersionDto getSpecificVersionOfWebcomponent(String webcomponentId, String versionTag);
}
