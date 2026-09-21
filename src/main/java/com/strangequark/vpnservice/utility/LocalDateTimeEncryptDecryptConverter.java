package com.strangequark.vpnservice.utility;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;

@Converter
public class LocalDateTimeEncryptDecryptConverter implements AttributeConverter<LocalDateTime, String> {
    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if(attribute == null)
            return null;
        return EncryptionUtility.encrypt(attribute.toString());
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String databaseData) {
        if(databaseData == null)
            return null;
        return LocalDateTime.parse(EncryptionUtility.decrypt(databaseData));
    }
}
