package com.jobtest.events.db;

import com.jobtest.events.entities.Event;
import com.jobtest.events.utils.GeoDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class EventsDao {


    private final JdbcTemplate jdbcTemplate;

    public EventsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final String SQL_INSERT_EVENT = "insert into events(lat, lng, timestamp) values(?, ?, ?)";

    public long addEvent(final Float lat, final Float lng) {
        final PreparedStatementCreator psc = connection -> {
//          final PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);
            final PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT, new String[]{"id"});
            ps.setFloat(1, lat);
            ps.setFloat(2, lng);
            ps.setLong(3, System.currentTimeMillis());
            return ps;
        };
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : 0;
    }


    public double findAllEventsInRangeSQL(final float geoObjectLat, final float geoObjectLng, final long range) {
//        SimpleJdbcCall jdbcCall = new
//                SimpleJdbcCall(jdbcTemplate)
//                .withCatalogName("events")
//                .withSchemaName("public")
//                .withFunctionName("deg2rad");
//        double deg = 90.0d;
//        SqlParameterSource in = new MapSqlParameterSource().addValue("deg", deg);
//        Map map =jdbcCall.execute(in);
//        System.out.println("JdbsCall result = " + map.toString());
//        return 0;
        final String sql_querry = "CREATE FUNCTION public.degtest()\n" +
                "    RETURNS double precision\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    \n" +
                "    \n" +
                "AS $BODY$begin\n" +
                "\treturn 0.0;\n" +
                "end;$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.degtest()\n" +
                "    OWNER TO postgres;";

//        NamedParameterJdbcTemplate  namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
//        SqlParameterSource namedParameters = new MapSqlParameterSource("deg", 0.0d);
//        namedParameterJdbcTemplate.query(sql_querry, namedParameters, (rs, rowNum) -> null);
        Double result = jdbcTemplate.queryForObject(sql_querry, Double.class);
        return result == null ? -1.0 : result;
    }

    public List<Event> findAllEventsInRange(final float geoObjectLat, final float geoObjectLng, final long range) {

        final String SQL_QUERY_EVENTS = "select * from events";
        return jdbcTemplate.query(
                SQL_QUERY_EVENTS,
                (rs, rowNum) -> {
                    Event event = new Event();
                    event.setId(rs.getLong("id"));
                    event.setLat(rs.getFloat("lat"));
                    event.setLng(rs.getFloat("lng"));
                    event.setTimestamp(rs.getLong("timestamp"));
                    return event;
                }
        ).stream()
                .filter(event ->
                        GeoDistance.inRange(
                                event.getLat(),
                                event.getLng(),
                                geoObjectLat,
                                geoObjectLng,
                                range)
                )
                .collect(Collectors.toList());
    }

}
