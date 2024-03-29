package com.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class HazelcastConfiguration
{

    @Value("${cacheManagerConfiguration.caches.authentications.timeToLiveSeconds:10}")
    public int authenticationsCacheInvalidateInterval;

    @Value("${cacheManagerConfiguration.caches.authentications.cacheLocalEntries:true}")
    public boolean authenticationsCacheCacheLocalEntries;

    @Value("${cacheManagerConfiguration.caches.authentications.evictionConfigSize:1000}")
    public int authenticationsCacheEvictionConfigSize;

    @Value("${cacheManagerConfiguration.caches.authentications.evictionConfigMaxSizePolicy:ENTRY_COUNT}")
    public EvictionConfig.MaxSizePolicy authenticationsCacheEvictionConfigMaxSizePolicy;

    @Value("${cacheManagerConfiguration.caches.authentications.invalidateOnChange:true}")
    public boolean authenticationsCacheInvalidateOnChange;

    public MapConfig authenticationCacheConfig(String name)
    {
        return new MapConfig().setName(name).setNearCacheConfig(new NearCacheConfig().setCacheLocalEntries(authenticationsCacheCacheLocalEntries)
                                                                                     .setEvictionConfig(new EvictionConfig().setMaximumSizePolicy(authenticationsCacheEvictionConfigMaxSizePolicy)
                                                                                                                            .setSize(authenticationsCacheEvictionConfigSize))
                                                                                     .setInvalidateOnChange(authenticationsCacheInvalidateOnChange)
                                                                                     .setTimeToLiveSeconds(authenticationsCacheInvalidateInterval));
    }

    @Value("${cacheManagerConfiguration.caches.remoteRepositoryAliveness.maxSizeLimit:1000}")
    public int remoteRepositoryAlivenessMaxSizeLimit;

    @Value("${cacheManagerConfiguration.caches.remoteRepositoryAliveness.maxSizePolicy:FREE_HEAP_SIZE}")
    public MaxSizeConfig.MaxSizePolicy remoteRepositoryAlivenessMaxSizePolicy;

    @Value("${cacheManagerConfiguration.caches.remoteRepositoryAliveness.evictionPolicy:LFU}")
    public EvictionPolicy remoteRepositoryAlivenessEvictionPolicy;

    @Value("${cacheManagerConfiguration.caches.tags.maxSizeLimit:1000}")
    public int tagsMaxSizeLimit;

    @Value("${cacheManagerConfiguration.caches.tags.maxSizePolicy:FREE_HEAP_SIZE}")
    public MaxSizeConfig.MaxSizePolicy tagsMaxSizePolicy;

    @Value("${cacheManagerConfiguration.caches.tags.evictionPolicy:LFU}")
    public EvictionPolicy tagsEvictionPolicy;

    public static MapConfig newDefaultMapConfig(String name,
                                                int maxSize,
                                                MaxSizeConfig.MaxSizePolicy maxSizePolicy,
                                                EvictionPolicy evictionPolicy)
    {
        return new MapConfig().setName(name)
                              .setMaxSizeConfig(new MaxSizeConfig(maxSize, maxSizePolicy))
                              .setEvictionPolicy(evictionPolicy);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config)
    {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Value("${cacheManagerConfiguration.instanceId:strongbox}")
    public String hazelcastInstanceId;

    @Value("${cacheManagerConfiguration.groupConfig.name:strongbox}")
    public String groupConfigName;

    @Value("${cacheManagerConfiguration.groupConfig.password:password}")
    public String groupConfigPassword;

    @Value("${cacheManagerConfiguration.enableMulticastConfig:true}")
    public boolean enableMulticastConfig;

    @Value("${cacheManagerConfiguration.multicast.multicastGroup:224.2.2.3}")
    public String multicastGroup;

    @Value("${cacheManagerConfiguration.multicast.multicastPort:54327}")
    public int multicastPort;

    @Value("${cacheManagerConfiguration.multicast.multicastTimeoutSeconds:2}")
    public int multicastTimeoutSeconds;

    @Value("${cacheManagerConfiguration.multicast.multicastTimeToLive:32}")
    public int multicastTimeToLive;

    @Value("#{'${cacheManagerConfiguration.multicast.trustedInterfaces:}'.split(',')}")
    public String[] multicastTrustedInterfaces;

    @Value("${cacheManagerConfiguration.multicast.loopbackModeEnabled:false}")
    public boolean multicastLoopbackModeEnabled;

    @Bean
    public Config hazelcastConfig()
    {
        final Config config = new Config().setInstanceName(hazelcastInstanceId)
                                          .addMapConfig(newDefaultMapConfig("remoteRepositoryAliveness",
                                                                            remoteRepositoryAlivenessMaxSizeLimit,
                                                                            remoteRepositoryAlivenessMaxSizePolicy,
                                                                            remoteRepositoryAlivenessEvictionPolicy))
                                          .addMapConfig(newDefaultMapConfig("tags",
                                                                            tagsMaxSizeLimit,
                                                                            tagsMaxSizePolicy,
                                                                            tagsEvictionPolicy))
                                          .addMapConfig(authenticationCacheConfig("authentications"));
        config.setGroupConfig(new GroupConfig(groupConfigName, groupConfigPassword));
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(enableMulticastConfig);

        ManagementCenterConfig manCenterCfg = new ManagementCenterConfig();
        manCenterCfg.setEnabled(true).setUrl("http://localhost:8080/hazelcast-mancenter");

        config.setManagementCenterConfig(manCenterCfg);

        if (enableMulticastConfig)
        {

            Set<String> trustedInterfaces = new HashSet<>();
            Arrays.stream(multicastTrustedInterfaces).forEach(trustedInterfaces::add);
            MulticastConfig mcConfig = config.getNetworkConfig().getJoin().getMulticastConfig();
            mcConfig.setMulticastGroup(multicastGroup)
                    .setMulticastPort(multicastPort)
                    .setMulticastTimeoutSeconds(multicastTimeoutSeconds)
                    .setMulticastTimeToLive(multicastTimeToLive)
                    .setTrustedInterfaces(trustedInterfaces)
                    .setLoopbackModeEnabled(multicastLoopbackModeEnabled);
        }

        return config;
    }

}