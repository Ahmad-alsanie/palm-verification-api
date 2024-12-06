package com.palm.service;

import com.palm.model.PalmData;
import com.palm.repository.PalmDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tg.vein.SDPVD310API;

import java.util.Optional;
import java.util.UUID;

@Service
public class PalmService {
    private boolean sdkInitialized = false;

    @Autowired
    private PalmDataRepository palmDataRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(PalmService.class);

    @PostConstruct
    public void initSDK() {
        final String licensePath = "license.dat";
        int enRet = SDPVD310API.instanceDll.SD_API_Init(new CommCallBackImpl(), licensePath, 1, 1);
        if (enRet == 0) {
            sdkInitialized = true;
            LOGGER.info("SDK initialized successfully.");
        } else {
            throw new RuntimeException("Failed to initialize SDK");
        }
    }

    @PreDestroy
    public void uninitSDK() {
        if (sdkInitialized) {
            SDPVD310API.instanceDll.SD_API_Uninit();
            sdkInitialized = false;
            LOGGER.error("SDK uninitialized successfully.");
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
        LOGGER.info("Palm data stored successfully with palmId: {}", palmId);
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
            LOGGER.info("Palm data deleted successfully for palmId: {}", palmId);
            return true;
        }
        LOGGER.error("Palm data not found for palmId: {}", palmId);
        return false;
    }

    private static class CommCallBackImpl implements SDPVD310API.CommCallBack {
        public void methodWithCallback(final String mes) {
            System.out.printf("%s\n", mes);
        }
    }
}
