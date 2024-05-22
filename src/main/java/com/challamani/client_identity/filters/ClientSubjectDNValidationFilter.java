package com.challamani.client_identity.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@ConditionalOnProperty(name = "service.san-identity", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ClientSubjectDNValidationFilter implements Filter {

    @Value("#{'${service.client.allowed-list}'.split(',')}")
    private List<String> clientCnOrSanAllowedList;

    private final Pattern commonNamePattern = Pattern.compile("CN=([^,]+)");
    private final Pattern sanPattern = Pattern.compile("DNS:([^,]+)");

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) {

        HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
        if(log.isDebugEnabled()){
            logAttributes(httpServletRequest);
        }

        X509Certificate[] x509Certificates = (X509Certificate[]) httpServletRequest.getAttribute("jakarta.servlet.request.X509Certificate");
        boolean isClientSanOrCnMatched = false;
        if (Objects.nonNull(x509Certificates) && x509Certificates.length > 0) {
            X509Certificate x509Certificate = x509Certificates[0];
            String subjectDN = x509Certificate.getSubjectDN().getName();

            Matcher commonNameMatcher = commonNamePattern.matcher(subjectDN);
            if(commonNameMatcher.find()){
                String clientCommonName = commonNameMatcher.group(1);
                log.debug(">>> Client Subject DN: {}", subjectDN);
                log.debug(">>> Client CN: {}", clientCommonName);
                isClientSanOrCnMatched = clientCnOrSanAllowedList.stream().anyMatch(allowedName -> clientCommonName.equals(allowedName));
            }

            if(!isClientSanOrCnMatched && Objects.nonNull(x509Certificate.getSubjectAlternativeNames())) {
                log.debug("Client certs SANs: {}", x509Certificate.getSubjectAlternativeNames());
                isClientSanOrCnMatched = x509Certificate.getSubjectAlternativeNames().stream().anyMatch(sanList ->
                        sanList.stream().anyMatch(sanName -> clientCnOrSanAllowedList.contains(sanName)));
            }
        }

        if (isClientSanOrCnMatched) {
            filterChain.doFilter(servletRequest, servletResponse);
        }else{
            throw new RuntimeException("Client CN/SAN is not in allowed list.");
        }
    }

    private void logAttributes(HttpServletRequest httpServletRequest){

        Enumeration<String> attributeNames = httpServletRequest.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = httpServletRequest.getAttribute(attributeName);
            log.debug("name: {} \n value: {}", attributeName, attributeValue);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (Objects.nonNull(headerNames)) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headersMap.put(headerName, headerValue);
            }
        }
        return Collections.unmodifiableMap(headersMap);
    }

}
