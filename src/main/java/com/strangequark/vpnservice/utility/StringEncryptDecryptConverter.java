package com.strangequark.vpnservice.utility;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringEncryptDecryptConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if(attribute == null)
            return null;
        return EncryptionUtility.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String databaseData) {
        if(databaseData == null)
            return null;
        return EncryptionUtility.decrypt(databaseData);
    }
}
