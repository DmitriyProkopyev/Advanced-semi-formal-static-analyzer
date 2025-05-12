package org.SNA.core;

import java.util.List;

public class ToolResult {
    private int errorCount = 0;
    private int warningCount = 0;
    private List<String> messages;

    

    public void setErrorCount(int value) {
        this.errorCount = value;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public void setWarningCount(int newWarningsCount) {
        this.warningCount = newWarningsCount;
    }

    public int getWarningCount() {
        return this.warningCount;
    }

    public List<String> getMessages() {
        return messages;
    }
    
}