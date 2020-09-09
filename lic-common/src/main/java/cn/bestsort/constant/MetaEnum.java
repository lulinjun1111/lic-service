package cn.bestsort.constant;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import cn.bestsort.cache.CacheStoreType;

/**
 * @author bestsort
 * @version 1.0
 * @date 2020-09-09 08:54
 */
@SuppressWarnings("checkstyle:Indentation")
public enum  MetaEnum {
    /**
     * 版本号
     */
    VERSION("V1.0"),

    /**
     * 是否已经进行系统的初始化
     */
    INSTALLED("false"),

    /**
     * 选择的缓存类型
     */
    CACHE_TYPE(CacheStoreType.DEFAULT.toString()),

    /**
     * 应用网址
     */
    HOST("http://localhost:8080"),


    TIME_ZERO(Timestamp.valueOf("1900-1-1 00:00:00")),

    /**
     * 默认Cache_KEY失效时间
     */
    CACHE_EXPIRE(30),
    /**
     * val 为 null 时的缓存失效时间
     */
    CACHE_NULL_EXPIRE(5),

    CACHE_UNIT(TimeUnit.MINUTES);

    private final String val;
    private final Object defaultVal;
    MetaEnum(Object defaultVal) {
        this.val = this.name().toLowerCase();
        this.defaultVal = defaultVal;
    }

    public String getVal() {
        return this.val;
    }

    public static  <T> T get(Class<T> clz,MetaEnum metaEnum) {
        return clz.cast(metaEnum.defaultVal);
    }

    public Object getDefaultVal() {
        return this.defaultVal;
    }

}
