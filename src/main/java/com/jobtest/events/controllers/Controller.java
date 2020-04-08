package com.jobtest.events.controllers;

import com.jobtest.events.db.EventsDao;
import com.jobtest.events.entities.Event;
import com.jobtest.events.entities.GeoObject;
import io.micrometer.core.annotation.Timed;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RestController
public class Controller {

    private static final String GEOOBJECTS_SERVICE_ADDRESS = "http://geoobjects-service/";

    private final EventsDao eventsDao;
    private final RestTemplate restTemplate;



    public Controller(EventsDao eventsDao, RestTemplate restTemplate) {
        this.eventsDao = eventsDao;
        this.restTemplate = restTemplate;
    }

    @PostMapping("gen_events")
    @ResponseStatus(HttpStatus.CREATED)
    public void generateEvents(@RequestBody final int count) {
        final Random random = new Random();
        for (int i = 0; i < count; i++) {
            final Float lat = (random.nextFloat() * 180f) - 90f;
            final Float lng = (random.nextFloat() * 360f) - 180f;
            eventsDao.addEvent(lat, lng);
        }
    }

    @GetMapping("get_events_sql/{id}/{radius}")
    public double getAllEventsInRadiusSql(@PathVariable final Long id, @PathVariable final Long radius) {
        final GeoObject geoObject = restTemplate.getForObject(GEOOBJECTS_SERVICE_ADDRESS + id, GeoObject.class);
        if (geoObject != null) return eventsDao.findAllEventsInRangeSQL(geoObject.getLat(), geoObject.getLng(), radius);
        else return 0;
    }

    @GetMapping("get_events/{id}/{radius}")
    @Timed(value = "events_in_radius")
    public List<Event> getAllEventsInRadius(@PathVariable final Long id, @PathVariable final Long radius) {
        final GeoObject geoObject = restTemplate.getForObject(GEOOBJECTS_SERVICE_ADDRESS + id, GeoObject.class);
        if (geoObject != null) return eventsDao.findAllEventsInRange(geoObject.getLat(), geoObject.getLng(), radius);
        else return new ArrayList<>();
    }

}
