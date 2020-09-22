package com.idemia.tec.jkt.cardiotest.model;

import javafx.collections.ObservableList;

public class FileManagement {

    public String[] data_ghost = new String[200];
    public String[] data_master = new String[200];
    public int row=0;
    private boolean includeLinkFilesTest;
    private boolean testLinkFilesOk;
    private String testLinkFilesMessage;
    public Boolean LinkFilesSimbiosCtd_bool;

    public int rowRuwi=0;
    public String[] data_ruwi = new String[200];
    private boolean includeRuwiTest;
    private boolean testRuwiOk;
    private String testRuwiMessage;
    public Boolean RuwiSimbiosCtd_bool;

    private boolean includeSfiTest;
    private boolean testSfiOk;
    private String testSfiMessage;

    public Boolean SFI_Iccid_2FE2_02_bool , SFI_ARR_2F06_06_bool ;
    public Boolean SFI_PL_2F05_05_bool , SFI_Dir_2F00_1E_bool;
    public Boolean SFI_ECC_6F7B_01_bool , SFI_StartHFN_6F5B_0F_bool;
    public Boolean SFI_LI_6F05_02_bool , SFI_TRESHOLD_6F5C_10_bool;
    public Boolean SFI_AD_6FAD_03_bool , SFI_OPLMNwACT_6F61_11_bool;
    public Boolean SFI_UST_6F38_04_bool , SFI_HPPLMN_6F31_12_bool;
    public Boolean SFI_EST_6F56_05_bool , SFI_HPLMNwACT_6F62_13_bool;
    public Boolean SFI_ACC_6F78_06_bool , SFI_ICI_6F80_14_bool;
    public Boolean SFI_IMSI_6F07_07_bool , SFI_OCI_6F81_15_bool;
    public Boolean SFI_KEYS_6F08_08_bool , SFI_CCP2_6F4F_16_bool;
    public Boolean SFI_KEYSPS_6F09_09_bool , SFI_ARR_6F06_17_bool;
    public Boolean SFI_PLMNwACT_6F60_0A_bool ,SFI_ePDGIdEm_6F65_18_bool;
    public Boolean SFI_LOCI_6F7E_0B_bool , SFI_PNN_6FC5_19_bool;
    public Boolean SFI_PSLOCI_6F73_0C_bool , SFI_OPL_6FC6_1A_bool;
    public Boolean SFI_FPLMN_6F7B_0D_bool , SFI_SPDI_6FCD_1B_bool;
    public Boolean SFI_CBMID_6F48_0E_bool , SFI_ACM_6F39_1C_bool;
    public Boolean SFI_Kc_4F20_01_bool , SFI_KcGPRS_4F52_02_bool;

    public FileManagement() {}

    public FileManagement(boolean includeLinkFilesTest, boolean testLinkFilesOk, String testLinkFilesMessage, boolean includeRuwiTest, boolean testRuwiOk, String testRuwiMessage, boolean includeSfiTest, boolean testSfiOk, String testSfiMessage, Boolean SFI_Iccid_2FE2_02_bool, Boolean SFI_ARR_2F06_06_bool, Boolean SFI_PL_2F05_05_bool, Boolean SFI_Dir_2F00_1E_bool, Boolean SFI_ECC_6F7B_01_bool, Boolean SFI_StartHFN_6F5B_0F_bool, Boolean SFI_LI_6F05_02_bool, Boolean SFI_TRESHOLD_6F5C_10_bool, Boolean SFI_AD_6FAD_03_bool, Boolean SFI_OPLMNwACT_6F61_11_bool, Boolean SFI_UST_6F38_04_bool, Boolean SFI_HPPLMN_6F31_12_bool, Boolean SFI_EST_6F56_05_bool, Boolean SFI_HPLMNwACT_6F62_13_bool, Boolean SFI_ACC_6F78_06_bool, Boolean SFI_ICI_6F80_14_bool, Boolean SFI_IMSI_6F07_07_bool, Boolean SFI_OCI_6F81_15_bool, Boolean SFI_KEYS_6F08_08_bool, Boolean SFI_CCP2_6F4F_16_bool, Boolean SFI_KEYSPS_6F09_09_bool, Boolean SFI_ARR_6F06_17_bool, Boolean SFI_PLMNwACT_6F60_0A_bool, Boolean SFI_ePDGIdEm_6F65_18_bool, Boolean SFI_LOCI_6F7E_0B_bool, Boolean SFI_PNN_6FC5_19_bool, Boolean SFI_PSLOCI_6F73_0C_bool, Boolean SFI_OPL_6FC6_1A_bool, Boolean SFI_FPLMN_6F7B_0D_bool, Boolean SFI_SPDI_6FCD_1B_bool, Boolean SFI_CBMID_6F48_0E_bool, Boolean SFI_ACM_6F39_1C_bool, Boolean SFI_Kc_4F20_01_bool, Boolean SFI_KcGPRS_4F52_02_bool, int row, int rowRuwi, Boolean RuwiSimbiosCtd_bool, Boolean LinkFilesSimbiosCtd_bool) {
        this.includeLinkFilesTest = includeLinkFilesTest;
        this.testLinkFilesOk = testLinkFilesOk;
        this.testLinkFilesMessage = testLinkFilesMessage;
        this.includeRuwiTest = includeRuwiTest;
        this.testRuwiOk = testRuwiOk;
        this.testRuwiMessage = testRuwiMessage;
        this.includeSfiTest = includeSfiTest;
        this.testSfiOk = testSfiOk;
        this.testSfiMessage = testSfiMessage;
        this.SFI_Iccid_2FE2_02_bool = SFI_Iccid_2FE2_02_bool;
        this.SFI_ARR_2F06_06_bool = SFI_ARR_2F06_06_bool;
        this.SFI_PL_2F05_05_bool = SFI_PL_2F05_05_bool;
        this.SFI_Dir_2F00_1E_bool = SFI_Dir_2F00_1E_bool;
        this.SFI_ECC_6F7B_01_bool = SFI_ECC_6F7B_01_bool;
        this.SFI_StartHFN_6F5B_0F_bool = SFI_StartHFN_6F5B_0F_bool;
        this.SFI_LI_6F05_02_bool = SFI_LI_6F05_02_bool;
        this.SFI_TRESHOLD_6F5C_10_bool = SFI_TRESHOLD_6F5C_10_bool;
        this.SFI_AD_6FAD_03_bool = SFI_AD_6FAD_03_bool;
        this.SFI_OPLMNwACT_6F61_11_bool = SFI_OPLMNwACT_6F61_11_bool;
        this.SFI_UST_6F38_04_bool = SFI_UST_6F38_04_bool;
        this.SFI_HPPLMN_6F31_12_bool = SFI_HPPLMN_6F31_12_bool;
        this.SFI_EST_6F56_05_bool = SFI_EST_6F56_05_bool;
        this.SFI_HPLMNwACT_6F62_13_bool = SFI_HPLMNwACT_6F62_13_bool;
        this.SFI_ACC_6F78_06_bool = SFI_ACC_6F78_06_bool;
        this.SFI_ICI_6F80_14_bool = SFI_ICI_6F80_14_bool;
        this.SFI_IMSI_6F07_07_bool = SFI_IMSI_6F07_07_bool;
        this.SFI_OCI_6F81_15_bool = SFI_OCI_6F81_15_bool;
        this.SFI_KEYS_6F08_08_bool = SFI_KEYS_6F08_08_bool;
        this.SFI_CCP2_6F4F_16_bool = SFI_CCP2_6F4F_16_bool;
        this.SFI_KEYSPS_6F09_09_bool = SFI_KEYSPS_6F09_09_bool;
        this.SFI_ARR_6F06_17_bool = SFI_ARR_6F06_17_bool;
        this.SFI_PLMNwACT_6F60_0A_bool = SFI_PLMNwACT_6F60_0A_bool;
        this.SFI_ePDGIdEm_6F65_18_bool = SFI_ePDGIdEm_6F65_18_bool;
        this.SFI_LOCI_6F7E_0B_bool = SFI_LOCI_6F7E_0B_bool;
        this.SFI_PNN_6FC5_19_bool = SFI_PNN_6FC5_19_bool;
        this.SFI_PSLOCI_6F73_0C_bool = SFI_PSLOCI_6F73_0C_bool;
        this.SFI_OPL_6FC6_1A_bool = SFI_OPL_6FC6_1A_bool;
        this.SFI_FPLMN_6F7B_0D_bool = SFI_FPLMN_6F7B_0D_bool;
        this.SFI_SPDI_6FCD_1B_bool = SFI_SPDI_6FCD_1B_bool;
        this.SFI_CBMID_6F48_0E_bool = SFI_CBMID_6F48_0E_bool;
        this.SFI_ACM_6F39_1C_bool = SFI_ACM_6F39_1C_bool;
        this.SFI_Kc_4F20_01_bool = SFI_Kc_4F20_01_bool;
        this.SFI_KcGPRS_4F52_02_bool = SFI_KcGPRS_4F52_02_bool;
        this.row = row;
        this.rowRuwi = rowRuwi;
        this.RuwiSimbiosCtd_bool = RuwiSimbiosCtd_bool;
        this.LinkFilesSimbiosCtd_bool = LinkFilesSimbiosCtd_bool;
    }

    public boolean isIncludeLinkFilesTest() {
        return includeLinkFilesTest;
    }

    public void setIncludeLinkFilesTest(boolean includeLinkFilesTest) {
        this.includeLinkFilesTest = includeLinkFilesTest;
    }

    public boolean isTestLinkFilesOk() {
        return testLinkFilesOk;
    }

    public void setTestLinkFilesOk(boolean testLinkFilesOk) {
        this.testLinkFilesOk = testLinkFilesOk;
    }

    public String getTestLinkFilesMessage() {
        return testLinkFilesMessage;
    }

    public void setTestLinkFilesMessage(String testLinkFilesMessage) {
        this.testLinkFilesMessage = testLinkFilesMessage;
    }

    public boolean isIncludeRuwiTest() {
        return includeRuwiTest;
    }

    public void setIncludeRuwiTest(boolean includeRuwiTest) {
        this.includeRuwiTest = includeRuwiTest;
    }

    public boolean isTestRuwiOk() {
        return testRuwiOk;
    }

    public void setTestRuwiOk(boolean testRuwiOk) {
        this.testRuwiOk = testRuwiOk;
    }

    public String getTestRuwiMessage() {
        return testRuwiMessage;
    }

    public void setTestRuwiMessage(String testRuwiMessage) {
        this.testRuwiMessage = testRuwiMessage;
    }

    public boolean isIncludeSfiTest() {
        return includeSfiTest;
    }

    public void setIncludeSfiTest(boolean includeSfiTest) {
        this.includeSfiTest = includeSfiTest;
    }

    public boolean isTestSfiOk() {
        return testSfiOk;
    }

    public void setTestSfiOk(boolean testSfiOk) {
        this.testSfiOk = testSfiOk;
    }

    public String getTestSfiMessage() {
        return testSfiMessage;
    }

    public void setTestSfiMessage(String testSfiMessage) {
        this.testSfiMessage = testSfiMessage;
    }

    // =========================================

    public void setSFI_Iccid_2FE2_02_bool(Boolean SFI_Iccid_2FE2_02_bool) {
        this.SFI_Iccid_2FE2_02_bool = SFI_Iccid_2FE2_02_bool;
    }

    public void setSFI_ARR_2F06_06_bool(Boolean SFI_ARR_2F06_06_bool) {
        this.SFI_ARR_2F06_06_bool = SFI_ARR_2F06_06_bool;
    }

    public void setSFI_PL_2F05_05_bool(Boolean SFI_PL_2F05_05_bool) {
        this.SFI_PL_2F05_05_bool = SFI_PL_2F05_05_bool;
    }

    public void setSFI_Dir_2F00_1E_bool(Boolean SFI_Dir_2F00_1E_bool) {
        this.SFI_Dir_2F00_1E_bool = SFI_Dir_2F00_1E_bool;
    }

    public void setSFI_ECC_6F7B_01_bool(Boolean SFI_ECC_6F7B_01_bool) {
        this.SFI_ECC_6F7B_01_bool = SFI_ECC_6F7B_01_bool;
    }

    public void setSFI_StartHFN_6F5B_0F_bool(Boolean SFI_StartHFN_6F5B_0F_bool) {
        this.SFI_StartHFN_6F5B_0F_bool = SFI_StartHFN_6F5B_0F_bool;
    }

    public void setSFI_LI_6F05_02_bool(Boolean SFI_LI_6F05_02_bool) {
        this.SFI_LI_6F05_02_bool = SFI_LI_6F05_02_bool;
    }

    public void setSFI_TRESHOLD_6F5C_10_bool(Boolean SFI_TRESHOLD_6F5C_10_bool) {
        this.SFI_TRESHOLD_6F5C_10_bool = SFI_TRESHOLD_6F5C_10_bool;
    }

    public void setSFI_AD_6FAD_03_bool(Boolean SFI_AD_6FAD_03_bool) {
        this.SFI_AD_6FAD_03_bool = SFI_AD_6FAD_03_bool;
    }

    public void setSFI_OPLMNwACT_6F61_11_bool(Boolean SFI_OPLMNwACT_6F61_11_bool) {
        this.SFI_OPLMNwACT_6F61_11_bool = SFI_OPLMNwACT_6F61_11_bool;
    }

    public void setSFI_UST_6F38_04_bool(Boolean SFI_UST_6F38_04_bool) {
        this.SFI_UST_6F38_04_bool = SFI_UST_6F38_04_bool;
    }

    public void setSFI_HPPLMN_6F31_12_bool(Boolean SFI_HPPLMN_6F31_12_bool) {
        this.SFI_HPPLMN_6F31_12_bool = SFI_HPPLMN_6F31_12_bool;
    }

    public void setSFI_EST_6F56_05_bool(Boolean SFI_EST_6F56_05_bool) {
        this.SFI_EST_6F56_05_bool = SFI_EST_6F56_05_bool;
    }

    public void setSFI_HPLMNwACT_6F62_13_bool(Boolean SFI_HPLMNwACT_6F62_13_bool) {
        this.SFI_HPLMNwACT_6F62_13_bool = SFI_HPLMNwACT_6F62_13_bool;
    }

    public void setSFI_ACC_6F78_06_bool(Boolean SFI_ACC_6F78_06_bool) {
        this.SFI_ACC_6F78_06_bool = SFI_ACC_6F78_06_bool;
    }

    public void setSFI_ICI_6F80_14_bool(Boolean SFI_ICI_6F80_14_bool) {
        this.SFI_ICI_6F80_14_bool = SFI_ICI_6F80_14_bool;
    }

    public void setSFI_IMSI_6F07_07_bool(Boolean SFI_IMSI_6F07_07_bool) {
        this.SFI_IMSI_6F07_07_bool = SFI_IMSI_6F07_07_bool;
    }

    public void setSFI_OCI_6F81_15_bool(Boolean SFI_OCI_6F81_15_bool) {
        this.SFI_OCI_6F81_15_bool = SFI_OCI_6F81_15_bool;
    }

    public void setSFI_KEYS_6F08_08_bool(Boolean SFI_KEYS_6F08_08_bool) {
        this.SFI_KEYS_6F08_08_bool = SFI_KEYS_6F08_08_bool;
    }

    public void setSFI_CCP2_6F4F_16_bool(Boolean SFI_CCP2_6F4F_16_bool) {
        this.SFI_CCP2_6F4F_16_bool = SFI_CCP2_6F4F_16_bool;
    }

    public void setSFI_KEYSPS_6F09_09_bool(Boolean SFI_KEYSPS_6F09_09_bool) {
        this.SFI_KEYSPS_6F09_09_bool = SFI_KEYSPS_6F09_09_bool;
    }

    public void setSFI_ARR_6F06_17_bool(Boolean SFI_ARR_6F06_17_bool) {
        this.SFI_ARR_6F06_17_bool = SFI_ARR_6F06_17_bool;
    }

    public void setSFI_PLMNwACT_6F60_0A_bool(Boolean SFI_PLMNwACT_6F60_0A_bool) {
        this.SFI_PLMNwACT_6F60_0A_bool = SFI_PLMNwACT_6F60_0A_bool;
    }

    public void setSFI_ePDGIdEm_6F65_18_bool(Boolean SFI_ePDGIdEm_6F65_18_bool) {
        this.SFI_ePDGIdEm_6F65_18_bool = SFI_ePDGIdEm_6F65_18_bool;
    }

    public void setSFI_LOCI_6F7E_0B_bool(Boolean SFI_LOCI_6F7E_0B_bool) {
        this.SFI_LOCI_6F7E_0B_bool = SFI_LOCI_6F7E_0B_bool;
    }

    public void setSFI_PNN_6FC5_19_bool(Boolean SFI_PNN_6FC5_19_bool) {
        this.SFI_PNN_6FC5_19_bool = SFI_PNN_6FC5_19_bool;
    }

    public void setSFI_PSLOCI_6F73_0C_bool(Boolean SFI_PSLOCI_6F73_0C_bool) {
        this.SFI_PSLOCI_6F73_0C_bool = SFI_PSLOCI_6F73_0C_bool;
    }

    public void setSFI_OPL_6FC6_1A_bool(Boolean SFI_OPL_6FC6_1A_bool) {
        this.SFI_OPL_6FC6_1A_bool = SFI_OPL_6FC6_1A_bool;
    }

    public void setSFI_FPLMN_6F7B_0D_bool(Boolean SFI_FPLMN_6F7B_0D_bool) {
        this.SFI_FPLMN_6F7B_0D_bool = SFI_FPLMN_6F7B_0D_bool;
    }

    public void setSFI_SPDI_6FCD_1B_bool(Boolean SFI_SPDI_6FCD_1B_bool) {
        this.SFI_SPDI_6FCD_1B_bool = SFI_SPDI_6FCD_1B_bool;
    }

    public void setSFI_CBMID_6F48_0E_bool(Boolean SFI_CBMID_6F48_0E_bool) {
        this.SFI_CBMID_6F48_0E_bool = SFI_CBMID_6F48_0E_bool;
    }

    public void setSFI_ACM_6F39_1C_bool(Boolean SFI_ACM_6F39_1C_bool) {
        this.SFI_ACM_6F39_1C_bool = SFI_ACM_6F39_1C_bool;
    }

    public void setSFI_Kc_4F20_01_bool(Boolean SFI_Kc_4F20_01_bool) {
        this.SFI_Kc_4F20_01_bool = SFI_Kc_4F20_01_bool;
    }

    public void setSFI_KcGPRS_4F52_02_bool(Boolean SFI_KcGPRS_4F52_02_bool) {
        this.SFI_KcGPRS_4F52_02_bool = SFI_KcGPRS_4F52_02_bool;
    }


    public boolean isSFI_Iccid_2FE2_02_bool() {return SFI_Iccid_2FE2_02_bool;}
    public boolean isSFI_PL_2F05_05_bool () {return SFI_PL_2F05_05_bool;}
    //public boolean isSFI_ECC_6F7B_01_bool () {return SFI_ECC_6F7B_01_bool;}
    public boolean isSFI_LI_6F05_02_bool () {return SFI_LI_6F05_02_bool;}
    public boolean isSFI_AD_6FAD_03_bool () {return SFI_AD_6FAD_03_bool;}
    public boolean isSFI_UST_6F38_04_bool () {return SFI_UST_6F38_04_bool;}
    public boolean isSFI_EST_6F56_05_bool () {return SFI_EST_6F56_05_bool;}
    public boolean isSFI_ACC_6F78_06_bool () {return SFI_ACC_6F78_06_bool;}
    public boolean isSFI_IMSI_6F07_07_bool () {return SFI_IMSI_6F07_07_bool;}
    public boolean isSFI_KEYS_6F08_08_bool () {return SFI_KEYS_6F08_08_bool;}
    public boolean isSFI_KEYSPS_6F09_09_bool () {return SFI_KEYSPS_6F09_09_bool;}
    public boolean isSFI_PLMNwACT_6F60_0A_bool () {return SFI_PLMNwACT_6F60_0A_bool;}
    public boolean isSFI_LOCI_6F7E_0B_bool () {return SFI_LOCI_6F7E_0B_bool;}
    public boolean isSFI_PSLOCI_6F73_0C_bool () {return SFI_PSLOCI_6F73_0C_bool;}
    public boolean isSFI_FPLMN_6F7B_0D_bool () {return SFI_FPLMN_6F7B_0D_bool;}
    public boolean isSFI_CBMID_6F48_0E_bool () {return SFI_CBMID_6F48_0E_bool;}
    public boolean isSFI_ARR_2F06_06_bool(){return SFI_ARR_2F06_06_bool ;}
    public boolean isSFI_Dir_2F00_1E_bool(){return SFI_Dir_2F00_1E_bool;}
    public boolean isSFI_StartHFN_6F5B_0F_bool (){return SFI_StartHFN_6F5B_0F_bool ;}
    public boolean isSFI_TRESHOLD_6F5C_10_bool (){return SFI_TRESHOLD_6F5C_10_bool ;}
    public boolean isSFI_OPLMNwACT_6F61_11_bool (){return SFI_OPLMNwACT_6F61_11_bool ;}
    public boolean isSFI_HPPLMN_6F31_12_bool(){return SFI_HPPLMN_6F31_12_bool;}
    public boolean isSFI_HPLMNwACT_6F62_13_bool (){return SFI_HPLMNwACT_6F62_13_bool ;}
    public boolean isSFI_ICI_6F80_14_bool(){return SFI_ICI_6F80_14_bool;}
    public boolean isSFI_OCI_6F81_15_bool(){return SFI_OCI_6F81_15_bool;}
    public boolean isSFI_CCP2_6F4F_16_bool(){return SFI_CCP2_6F4F_16_bool;}
    public boolean isSFI_ARR_6F06_17_bool(){return SFI_ARR_6F06_17_bool;}
    //public boolean isSFI_ePDGIdEm_6F65_18_bool (){return  SFI_ePDGIdEm_6F65_18_bool;}
    public boolean isSFI_PNN_6FC5_19_bool(){return SFI_PNN_6FC5_19_bool;}
    public boolean isSFI_OPL_6FC6_1A_bool(){return SFI_OPL_6FC6_1A_bool;}
    public boolean isSFI_SPDI_6FCD_1B_bool(){return SFI_SPDI_6FCD_1B_bool;}
    public boolean isSFI_ACM_6F39_1C_bool(){return SFI_ACM_6F39_1C_bool;}
    public boolean isSFI_Kc_4F20_01_bool(){return SFI_Kc_4F20_01_bool;}
    public boolean isSFI_KcGPRS_4F52_02_bool(){return SFI_KcGPRS_4F52_02_bool;}

    public String getData_ghost(int i) {
        return data_ghost[i];
    }
    public void setData_ghost(int i, String data_ghost) {

        this.data_ghost[i] = data_ghost;
    }
    public String getData_master(int i) {
        return data_master[i];
    }
    public void setData_master(int i, String data_master) {
        this.data_master[i] = data_master;
    }

    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }

    public int getRowRuwi() {
        return rowRuwi;
    }
    public void setRowRuwi(int rowRuwi) {
        this.rowRuwi = rowRuwi;
    }

    public String getData_ruwi(int i) {
        return data_ruwi[i];
    }
    public void setData_ruwi(int i, String data_ruwi) {
        this.data_ruwi[i] = data_ruwi;
    }

    public Boolean isRuwiSimbiosCtd_bool() {
        return RuwiSimbiosCtd_bool;
    }

    public void setRuwiSimbiosCtd_bool(Boolean ruwiSimbiosCtd_bool) {
        RuwiSimbiosCtd_bool = ruwiSimbiosCtd_bool;
    }

    public Boolean isLinkFilesSimbiosCtd_bool() {
        return LinkFilesSimbiosCtd_bool;
    }

    public void setLinkFilesSimbiosCtd_bool(Boolean linkFilesSimbiosCtd_bool) {
        LinkFilesSimbiosCtd_bool = linkFilesSimbiosCtd_bool;
    }
}
