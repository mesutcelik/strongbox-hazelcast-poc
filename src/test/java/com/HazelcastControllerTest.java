package com;

import javax.inject.Inject;

import java.util.Objects;

import com.config.HazelcastConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

@SpringBootTest
@ActiveProfiles({ "HazelcastControllerTestConfig" })
public class HazelcastControllerTest {

    public static final String CACHE_NAME = "remoteRepositoryAliveness";
    public static final String CACHE_KEY = "canPersist";

    @Inject
    private CacheManager cacheManager;

    @Test
    public void cacheCanPersist()
    {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        Objects.requireNonNull(cache, "Cache was unavailable");

        cache.put(CACHE_KEY, true);

        Boolean cacheValue = cache.get("canPersist", Boolean.class);

        Assert.notNull(cacheValue, "Cache was unable to persist");
        Assert.isTrue(cacheValue, "Cache stored the wrong value");
    }

    @Profile("HazelcastControllerTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class RemoteRepositoryAlivenessCacheManagerTestConfig
    {

        @Bean
        public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
        {
            return new HazelcastCacheManager(hazelcastInstance);
        }

    }

}