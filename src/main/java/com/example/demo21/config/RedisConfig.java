package com.example.demo21.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }

            ClientOptions options = ClientOptions.builder()
                    .autoReconnect(true)  // Automatically reconnect on failure
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .build();

            // Improved connection pooling for better performance
            GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(5);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setMaxWait(Duration.ofMillis(1000)); // Faster timeout for connection acquisition
            poolConfig.setTestWhileIdle(true);
            poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));

            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .poolConfig(poolConfig)
                    .clientOptions(options)
                    .clientResources(ClientResources.create())
                    .commandTimeout(Duration.ofMillis(1000)) // Increase command timeout to reduce timeout errors
                    .build();

            return new LettuceConnectionFactory(config, clientConfig);
        } catch (Exception e) {
            log.error("Error creating Redis connection factory. Application will function without Redis caching.", e);
            // Return a minimal connection factory to allow application startup
            // This will be non-functional but won't block application startup
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
            return new LettuceConnectionFactory(config);
        }
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cacheManager")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY
            );
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Optimized serializers
            RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
            RedisSerializer<String> stringSerializer = new StringRedisSerializer();

            // Default configuration
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(48))  // Default TTL for all caches
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                    .disableCachingNullValues(); // Don't cache null values to save space

            // Configure different TTLs for different caches
            Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
            
            // Frequently used data with short TTL
            configMap.put("products", defaultConfig.entryTtl(Duration.ofHours(24)));
            configMap.put("categories", defaultConfig.entryTtl(Duration.ofHours(48)));
            configMap.put("subcategories", defaultConfig.entryTtl(Duration.ofHours(48)));
            configMap.put("categoryData", defaultConfig.entryTtl(Duration.ofHours(48)));
            configMap.put("subcategoryData", defaultConfig.entryTtl(Duration.ofHours(48)));
            configMap.put("shopByCategory", defaultConfig.entryTtl(Duration.ofHours(48)));
            
            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(configMap)
                    .transactionAware()
                    .build();
        } catch (Exception e) {
            log.error("Error creating Redis cache manager. Falling back to no caching.", e);
            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(Duration.ofSeconds(1))) // Very short TTL
                    .build();
        }
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            
            // Use optimized serializers
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            
            GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);
            
            template.afterPropertiesSet();
            return template;
        } catch (Exception e) {
            log.error("Error creating Redis template. Functionality using Redis will be limited.", e);
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.afterPropertiesSet();
            return template;
        }
    }
}