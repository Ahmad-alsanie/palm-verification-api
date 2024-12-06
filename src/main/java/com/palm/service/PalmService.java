package com.palm.service;

import com.palm.model.PalmData;
import com.palm.repository.PalmDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.tg.vein.SDPVD310API;
import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

@Service
public class PalmService {
    private boolean sdkInitialized = false;

    @Autowired
    private PalmDataRepository palmDataRepository;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void initSDK() {
        final String licensePath = "license.dat";
        int enRet = SDPVD310API.instanceDll.SD_API_Init(new CommCallBackImpl(), licensePath, 1, 1);
        if (enRet == 0) {
            sdkInitialized = true;
            System.out.println("SDK initialized successfully.");
        } else {
            throw new RuntimeException("Failed to initialize SDK");
        }
    }

    @PreDestroy
    public void uninitSDK() {
        if (sdkInitialized) {
            SDPVD310API.instanceDll.SD_API_Uninit();
            sdkInitialized = false;
            System.out.println("SDK uninitialized successfully.");
        }
    }

    @Transactional
    public String storePalmData(String schoolId, byte[] palmBinary) {
        if (!sdkInitialized) {
            throw new RuntimeException("SDK is not initialized");
        }
        // Assuming palmBinary contains the data required for storing
        String palmId = UUID.randomUUID().toString();
        PalmData palmData = new PalmData(palmId, schoolId, palmBinary);
        palmDataRepository.save(palmData);
        System.out.println("Palm data stored successfully with palmId: " + palmId);
        return palmId;
    }

    public String validatePalmData(String schoolId, byte[] palmBinary) {
        if (!sdkInitialized) {
            throw new RuntimeException("SDK is not initialized");
        }
        Optional<PalmData> matchingPalm = palmDataRepository.findAll().stream()
                .filter(palmData -> palmData.getSchoolId().equals(schoolId) && palmData.matchesPalmBinary(palmBinary))
                .findFirst();
        return matchingPalm.map(PalmData::getPalmId).orElse(null);
    }

    @Transactional
    public boolean deletePalmData(String palmId, String schoolId) {
        Optional<PalmData> palmDataOpt = palmDataRepository.findById(palmId);
        if (palmDataOpt.isPresent() && palmDataOpt.get().getSchoolId().equals(schoolId)) {
            palmDataRepository.deleteById(palmId);
            System.out.println("Palm data deleted successfully for palmId: " + palmId);
            return true;
        }
        System.out.println("Palm data not found for palmId: " + palmId);
        return false;
    }

    private static class CommCallBackImpl implements SDPVD310API.CommCallBack {
        public void methodWithCallback(final String mes) {
            System.out.printf("%s\n", mes);
        }
    }
}
