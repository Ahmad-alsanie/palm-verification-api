package com.palm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.tg.vein.SDPVD310API;

import java.util.Arrays;

@Entity
@Table(name = "PALM_DATA")
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
        byte[] bufferSize = new byte[getTemplateSize()];
        int result = SDPVD310API.instanceDll.SD_API_Match1VN( otherPalmBinary,this.palmTemplate,1, matchScore, bufferSize);
        System.out.println("Stored PalmBinaryTemplate: "+ Arrays.toString(this.palmTemplate));
        System.out.println();
        System.out.println("Received PalmBinary: "+ Arrays.toString(otherPalmBinary));
        System.out.println();
        System.out.println("enRet: "+ result);
        System.out.println();
        System.out.println("BufferSize: " + Arrays.toString(bufferSize));
        return result == 0;
    }

    private int getTemplateSize() {
        int[] tmplSize = new int[1];
        SDPVD310API.instanceDll.SD_API_GetBufferSize(new int[1], tmplSize, new int[1], new int[1]);
        return tmplSize[0];
    }
}