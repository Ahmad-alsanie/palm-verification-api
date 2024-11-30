package com.palm.service;

import com.palm.model.PalmData;
import com.palm.repository.PalmDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sun.jna.Pointer;
import org.tg.vein.SDPVD310API;
import java.util.Optional;
import java.util.UUID;

@Service
public class PalmService {
    private boolean sdkInitialized = false;

    @Autowired
    private PalmDataRepository palmDataRepository;

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

    public void openDevice() {
        if (!sdkInitialized) {
            throw new RuntimeException("SDK is not initialized");
        }
        byte[] fw = new byte[20];
        byte[] sn = new byte[17];
        int enRet = SDPVD310API.instanceDll.SD_API_OpenDev(fw, sn);
        if (enRet != 0) {
            throw new RuntimeException("Failed to open device");
        }
        System.out.println("Device opened successfully.");
    }

    public void closeDevice() {
        int enRet = SDPVD310API.instanceDll.SD_API_CloseDev();
        if (enRet != 0) {
            throw new RuntimeException("Failed to close device");
        }
        System.out.println("Device closed successfully.");
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
        openDevice();
        try {
            byte[] pucTmpl = new byte[getTemplateSize()];
            byte[] pucImages = new byte[getImageSize() * getRegistrationTimes()];
            int enRet = SDPVD310API.instanceDll.SD_API_Register(pucTmpl, pucImages, 0, new RegisterCallbackImpl(), 10);
            if (enRet == 0) {
                String palmId = UUID.randomUUID().toString();
                PalmData palmData = new PalmData(palmId, schoolId, pucTmpl);
                palmDataRepository.save(palmData);
                System.out.println("Palm data stored successfully with palmId: " + palmId);
                return palmId;
            } else {
                throw new RuntimeException("Failed to store palm data");
            }
        } finally {
            closeDevice();
        }
    }

    public String validatePalmData(String schoolId, byte[] palmBinary) {
        openDevice();
        try {
            byte[] pucFeature = new byte[getFeatureSize()];
            int enRet = SDPVD310API.instanceDll.SD_API_ExtractFeature(pucFeature, palmBinary, 0, new ExtractFeatureCallbackImpl(), 10);
            if (enRet != 0) {
                throw new RuntimeException("Failed to extract features from palm binary");
            }
            Optional<PalmData> matchingPalm = palmDataRepository.findAll().stream()
                    .filter(palmData -> palmData.getSchoolId().equals(schoolId) && palmData.matchesPalmBinary(pucFeature))
                    .findFirst();
            return matchingPalm.map(PalmData::getPalmId).orElse(null);
        } finally {
            closeDevice();
        }
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

    private int getFeatureSize() {
        int[] featureSize = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(featureSize, new int[1], new int[1], new int[1]);
        return featureSize[0];
    }

    private int getTemplateSize() {
        int[] tmplSize = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(new int[1], tmplSize, new int[1], new int[1]);
        return tmplSize[0];
    }

    private int getImageSize() {
        int[] imageSize = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(new int[1], new int[1], imageSize, new int[1]);
        return imageSize[0];
    }

    private int getRegistrationTimes() {
        int[] regTimes = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(new int[1], new int[1], new int[1], regTimes);
        return regTimes[0];
    }

    private static class CommCallBackImpl implements SDPVD310API.CommCallBack {
        public void methodWithCallback(final String mes) {
            System.out.printf("%s\n", mes);
        }
    }

    private static class ExtractFeatureCallbackImpl implements SDPVD310API.ExtractFeatureCallback {
        public void methodWithCallback(int error, final Pointer pImage, int imageSize, final Pointer pImage_roi_rect) {
            if (error != 0) {
                System.out.println(SDPVD310API.ErrMsg[(error & 0x7FFFFFF)]);
            }
        }
    }

    private static class RegisterCallbackImpl implements SDPVD310API.RegisterCallback {
        public void methodWithCallback(int error, int stage, final Pointer pImage, int imageSize, final Pointer pImage_roi_rect) {
            if (error == 0) {
                System.out.printf("Registration progress: %d/%d\n", stage, 4);
            } else {
                System.out.println(SDPVD310API.ErrMsg[(error & 0x7FFFFFF)]);
            }
        }
    }
}