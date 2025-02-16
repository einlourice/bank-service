package com.rabobank.bankservice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value != null) {
            gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
