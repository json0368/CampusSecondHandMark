package com.cshm.campussecondhandmark.common.service;

import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthAuthorizeVO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthCallbackVO;

public interface OutlookOAuthTokenService {

    OutlookOAuthAuthorizeVO buildAuthorizeRequest();

    OutlookOAuthCallbackVO handleAuthorizationCallback(String code, String state, String error, String errorDescription);

    String getAccessToken();
}
