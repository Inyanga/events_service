package com.jobtest.events.entities;

import lombok.Data;

@Data
public class Event {
    private Long id;
    private Float lat;
    private Float lng;
    private Long timestamp;
}
