package ru.yandex.practicum.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.MotionPictureAA;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpasRowMapper implements RowMapper<MotionPictureAA> {
    @Override
    public MotionPictureAA mapRow(ResultSet rs, int rowNum) throws SQLException {
        MotionPictureAA motionPictureAA = new MotionPictureAA();

        motionPictureAA.setId(rs.getInt("id"));
        motionPictureAA.setName(rs.getString("name"));
        return motionPictureAA;
    }
}
