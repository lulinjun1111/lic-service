package cn.bestsort.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;

import cn.bestsort.entity.MetaInfo;
import cn.bestsort.constant.MetaEnum;
import cn.bestsort.repository.MetaInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bestsort
 * @version 1.0
 * @date 2020-09-09 08:38
 */
@Service
public class MetaInfoService extends AbstractBaseService<MetaInfo, Long> {

    final MetaInfoRepository metaInfoRepo;
    private final ConcurrentHashMap<String, String> metaMap = new ConcurrentHashMap<>();

    public String getMeta(String metaKey) {
        return metaMap.get(metaKey);
    }
    public String getMeta(String metaKey, String defaultVal) {
        return metaMap.getOrDefault(metaKey, defaultVal);
    }
    public <T> T getMetaObj(Class<T> clazz, MetaEnum metaEnum) {
        String res;
        if ((res = getMeta(metaEnum)) != null) {
            return JSON.parseObject(res, clazz);
        }
        return MetaEnum.get(clazz, metaEnum);
    }

    public String getMeta(MetaEnum metaKey) {
        return getMeta(metaKey.getVal());
    }
    public String getMeta(MetaEnum metaKey, String defaultVal) {
        return getMeta(metaKey.getVal(), defaultVal);
    }
    public String getMetaOrDefault(MetaEnum metaKey) {
        return getMeta(metaKey.getVal(), metaKey.getDefaultVal().toString());
    }

    public void refresh() {
        List<MetaInfo> metaInfos = listAll();
        metaMap.clear();
        for (MetaInfo metaInfo : metaInfos) {
            metaMap.put(metaInfo.getMetaKey(), metaInfo.getMetaVal());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMeta(String metaKey, String metaVal) {
        Optional<MetaInfo> metaInfoOpt = metaInfoRepo.findByMetaKey(metaKey);
        MetaInfo metaInfo;
        if (metaInfoOpt.isEmpty()) {
            metaInfo = new MetaInfo();
            metaInfo.setMetaKey(metaKey);
            metaInfo.setMetaVal(metaVal);
        } else {
            metaInfo = metaInfoOpt.get();
        }
        save(metaInfo);
        metaMap.put(metaKey, metaVal);
    }

    protected MetaInfoService(
        MetaInfoRepository repository) {
        super(repository);
        this.metaInfoRepo = repository;
    }
}
