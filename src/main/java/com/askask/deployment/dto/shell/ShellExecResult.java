package com.askask.deployment.dto.shell;

import lombok.Data;

@Data
public class ShellExecResult {
    private Integer exitStatus; // 0:正常執行 1:異常
    private String resultData;
}
