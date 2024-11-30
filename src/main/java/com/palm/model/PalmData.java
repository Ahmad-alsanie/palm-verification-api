package com.palm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.tg.vein.SDPVD310API;

@Entity
public class PalmData {
    @Id
    private String palmId;
    private String schoolId;
    @Lob
    private byte[] palmTemplate;

    public PalmData() {
    }

    public PalmData(String palmId, String schoolId, byte[] palmTemplate) {
        this.palmId = palmId;
        this.schoolId = schoolId;
        this.palmTemplate = palmTemplate;
    }

    public String getPalmId() {
        return palmId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public boolean matchesPalmBinary(byte[] otherPalmBinary) {
        // Use the SDK method to perform a proper comparison between the palm templates
        int[] matchScore = new int[1];
        int result = SDPVD310API.instanceDll.SD_API_Match1VN(this.palmTemplate, otherPalmBinary, 1, matchScore, new byte[getTemplateSize()]);
        return result == 0 && matchScore[0] > 80; // Assuming a match score threshold of 80 for successful matching
    }

    private int getTemplateSize() {
        int[] tmplSize = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(new int[1], tmplSize, new int[1], new int[1]);
        return tmplSize[0];
    }
}