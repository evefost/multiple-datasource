package com.xxx.starter.adapter;


import javax.servlet.ServletRequest;
import java.util.Collection;

/**
 * @author xieyang
 * @date 19/8/3
 */
public final class CompositeTenantMappingAdapter implements TenantMappingAdapter {

    private Collection<TenantMappingAdapter> adapters;

    public CompositeTenantMappingAdapter(Collection<TenantMappingAdapter> adapters) {
        this.adapters = adapters;
    }

    @Override
    public String getTenantId(ServletRequest servletRequest) {
        for (TenantMappingAdapter adapter : adapters) {
            String tenantId = adapter.getTenantId(servletRequest);
            if (tenantId != null) {
                return tenantId;
            }
        }
        return null;
    }
}
