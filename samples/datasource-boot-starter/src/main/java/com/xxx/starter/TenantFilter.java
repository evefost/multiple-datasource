package com.xxx.starter;


import com.eve.multiple.RouteContextManager;
import com.xxx.starter.adapter.CompositeTenantMappingAdapter;
import com.xxx.starter.adapter.TenantMappingAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author xieyang
 * @date 19/7/27
 */

@Order(-1000)
public class TenantFilter implements Filter, ApplicationContextAware {


    private TenantMappingAdapter tenantMappingAdapter;

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String tenantId = tenantMappingAdapter.getTenantId(servletRequest);
        RouteContextManager.setCurrentTenant(tenantId);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            RouteContextManager.setCurrentTenant(null);
        }
    }

    @Override
    public void destroy() {

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, TenantMappingAdapter> beansOfType = applicationContext.getBeansOfType(TenantMappingAdapter.class);
        Collection<TenantMappingAdapter> values = beansOfType.values();
        tenantMappingAdapter = new CompositeTenantMappingAdapter(values);
    }
}
