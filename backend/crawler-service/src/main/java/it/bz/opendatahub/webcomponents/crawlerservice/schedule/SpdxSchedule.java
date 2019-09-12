package it.bz.opendatahub.webcomponents.crawlerservice.schedule;

import it.bz.opendatahub.webcomponents.crawlerservice.service.SpdxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpdxSchedule {
    private SpdxService spdxService;

    @Autowired
    public SpdxSchedule(SpdxService spdxService) {
        this.spdxService = spdxService;
    }

    @Scheduled(fixedDelayString = "${application.schedule.spdx}")
    public void updateSpdx() {
        spdxService.update();
    }
}