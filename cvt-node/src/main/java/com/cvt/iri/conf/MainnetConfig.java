package com.cvt.iri.conf;

public class MainnetConfig extends BaseIotaConfig {

    public MainnetConfig() {
        //All the configs are defined in the super class
        super();
    }

    @Override
    public boolean isTestnet() {
        return false;
    }
    
    
    public String getCoinbase() {
    	return "";
    }
}
