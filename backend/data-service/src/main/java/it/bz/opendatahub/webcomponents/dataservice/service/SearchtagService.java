package it.bz.opendatahub.webcomponents.dataservice.service;

import java.util.List;

public interface SearchtagService {
    List<String> listAllUsedSearchtags();

    List<String> listAllSearchtags();

    String findOneByName(String name);
}
