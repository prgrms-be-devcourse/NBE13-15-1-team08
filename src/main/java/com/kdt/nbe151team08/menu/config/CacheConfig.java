package com.kdt.nbe151team08.menu.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache 활성화.
 * 저장소: application.yaml spring.cache.type=redis (menus, TTL 60s).
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
