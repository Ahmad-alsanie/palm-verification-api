package com.palm.service;

import com.palm.model.PalmData;
import com.palm.repository.PalmDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PalmServiceTest {

    @Mock
    private PalmDataRepository palmDataRepository;

    @InjectMocks
    private PalmService palmService;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        palmService.initSDK();
    }

    @Test
    void testStorePalmData_success() {
        String schoolId = "school-123";
        byte[] palmBinary = new byte[]{1, 2, 3};

        when(palmDataRepository.save(any(PalmData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String palmId = palmService.storePalmData(schoolId, palmBinary);

        assertNotNull(palmId);
        verify(palmDataRepository, times(1)).save(any(PalmData.class));
    }

    @Test
    void testStorePalmData_sdkNotInitialized() {
        palmService.uninitSDK();
        String schoolId = "school-123";
        byte[] palmBinary = new byte[]{1, 2, 3};

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            palmService.storePalmData(schoolId, palmBinary);
        });

        assertEquals("SDK is not initialized", thrown.getMessage());
        verify(palmDataRepository, never()).save(any(PalmData.class));
    }


    @Test
    void testValidatePalmData_notFound() {
        String schoolId = "school-123";
        byte[] palmBinary = new byte[]{1, 2, 3};

        when(palmDataRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        String resultPalmId = palmService.validatePalmData(schoolId, palmBinary);

        assertNull(resultPalmId);
        verify(palmDataRepository, times(1)).findAll();
    }

    @Test
    void testDeletePalmData_success() {
        String palmId = "palm-123";
        String schoolId = "school-123";
        PalmData existingPalmData = new PalmData(palmId, schoolId, new byte[]{1, 2, 3});

        when(palmDataRepository.findById(palmId)).thenReturn(Optional.of(existingPalmData));

        boolean result = palmService.deletePalmData(palmId, schoolId);

        assertTrue(result);
        verify(palmDataRepository, times(1)).findById(palmId);
        verify(palmDataRepository, times(1)).deleteById(palmId);
    }

    @Test
    void testDeletePalmData_notFound() {
        String palmId = "palm-123";
        String schoolId = "school-123";

        when(palmDataRepository.findById(palmId)).thenReturn(Optional.empty());

        boolean result = palmService.deletePalmData(palmId, schoolId);

        assertFalse(result);
        verify(palmDataRepository, times(1)).findById(palmId);
        verify(palmDataRepository, never()).deleteById(palmId);
    }
}
