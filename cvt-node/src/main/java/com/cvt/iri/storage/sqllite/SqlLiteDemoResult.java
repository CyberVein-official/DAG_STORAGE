package com.cvt.iri.storage.sqllite;

/**
 * Created by WANG on
 */
public class SqlLiteDemoResult {

    private String bizNo;
    private String content;
    private String createTime;

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
