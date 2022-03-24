package com.shareworks.api.encryption.domain;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author martin.peng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserApplication implements Serializable {

    private Long id;

    private String applicationName;

    private Date createTime;

    private Date modifyTime;

    private String state;

    private List<UserApplicationKeyInfo> userApplicationKeyInfoList;

    public void addKeyInfo(UserApplicationKeyInfo userApplicationKeyInfo) {
        if (CollectionUtil.isEmpty(userApplicationKeyInfoList)) {
            userApplicationKeyInfoList = new ArrayList<>();
        }
        userApplicationKeyInfoList.add(userApplicationKeyInfo);
    }
}
