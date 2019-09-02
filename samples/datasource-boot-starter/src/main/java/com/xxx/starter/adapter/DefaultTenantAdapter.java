package com.xxx.starter.adapter;


import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import static com.xxx.starter.Constants.TENANT_ID_HEADER_KEY;

/**
 * @author xieyang
 * @date 19/8/3
 */
public class DefaultTenantAdapter implements TenantMappingAdapter {


    @Override
    public String getTenantId(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String tenantId = request.getHeader(TENANT_ID_HEADER_KEY);
            return tenantId;
        }
        return null;
    }
}
