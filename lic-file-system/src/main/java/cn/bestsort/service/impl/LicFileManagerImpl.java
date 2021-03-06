package cn.bestsort.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import cn.bestsort.constant.ExceptionConstant;
import cn.bestsort.model.dto.FileDTO;
import cn.bestsort.model.entity.FileInfo;
import cn.bestsort.model.entity.FileMapping;
import cn.bestsort.model.entity.FileShare;
import cn.bestsort.model.entity.User;
import cn.bestsort.model.enums.FileNamespace;
import cn.bestsort.model.enums.Status;
import cn.bestsort.model.param.ShareParam;
import cn.bestsort.model.param.UploadSuccessCallbackParam;
import cn.bestsort.model.vo.UploadTokenVO;
import cn.bestsort.service.FileInfoService;
import cn.bestsort.service.FileManager;
import cn.bestsort.service.FileManagerHandler;
import cn.bestsort.service.FileMappingService;
import cn.bestsort.service.FileShareService;
import cn.bestsort.service.LicFileManager;
import cn.bestsort.util.TimeUtil;
import cn.bestsort.util.UrlUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

/**
 * @author bestsort
 * @version 1.0
 * @date 2020-09-07 17:36
 */
@Service
public class LicFileManagerImpl implements LicFileManager {

    final FileManagerHandler    manager;
    final FileInfoService    fileInfoImp;
    final FileMappingService mappingService;
    final FileShareService   fileShareImpl;

    @Override
    public boolean canSuperUpload(String md5, FileNamespace fileNamespace) {
        return fileInfoImp.getByMd5(md5, fileNamespace) != null;
    }

    @Override
    public List<FileMapping> listFilesByShare(String url) {
        FileShare fileShare = fileShareImpl.getByUrl(url)
            .orElseThrow(() -> ExceptionConstant.NOT_FOUND_ITEM);
        if (fileShare.getExpire().before(TimeUtil.now())) {
            throw ExceptionConstant.EXPIRED;
        }
        return null;
    }

    @Override
    public String createDownloadLink(Long mappingId, User user, Long expire) {
        FileMapping mapping = mappingService.getById(mappingId);
        if (mapping.getIsDir()) {
            throw ExceptionConstant.MUST_BE_NOT_DIR;
        }

        FileInfo info = fileInfoImp.getById(mapping.getInfoId());
        return UrlUtil.appendParam(manager.handle(info).downloadLink(info.getPath(), expire),
                                   "fileName", mapping.getFileName());
    }

    @Override
    public UploadTokenVO createUploadToken(FileNamespace namespace, Map<String, String> config) {
        return manager.handle(namespace).generatorUploadVO(config);
    }

    @Override
    public String createShareLink(ShareParam param, User user) {
        String url;
        do {
            // 防止生成的url产生碰撞
            url = RandomStringUtils.randomAlphanumeric(16);
        } while (fileShareImpl.existsByUrl(url));

        fileShareImpl.save(new FileShare(param.getFileId(), user.getUsername(),
            user.getId(), param.getPassword(), url, param.getExpire()));
        return url;
    }

    @Override
    public void uploadSuccess(User user, UploadSuccessCallbackParam param) {
        FileInfo info = fileInfoImp.getByMd5(param.getMd5(), param.getNamespace());
        if (info != null) {
            info.setReference(info.getReference() + 1);
        } else {
            info = new FileInfo(mappingService.fullPath(param.getPid()), user.getUsername(),
                                param.getName(), 1, param.getMd5(), param.getSize(),
                                param.getNamespace(), null);
            info = fileInfoImp.save(info);
        }
        FileMapping fileMapping = new FileMapping(param.getName(), info.getId(), user.getId(), param.getSize(),
                                                  param.getPid(), false, false, Status.VALID);
        mappingService.save(fileMapping);
    }

    @Override
    public void deleteFile(Long fileId, User user, boolean remove) {
        Optional<FileMapping>             fileMapping = mappingService.fetchById(fileId);
        Map<FileNamespace, List<FileDTO>> map         = new TreeMap<>();
        fileMapping.ifPresent(mapping -> deleteSoftLink(mapping, user, remove, map));
        // 部分OSS支持删除文件列表, 防止多次创建连接造成的时间消耗
        for (Map.Entry<FileNamespace, List<FileDTO>> pir : map.entrySet()) {
            handle(pir.getKey()).del(
                pir.getValue().stream().map(FileDTO::getFileInfo).collect(Collectors.toList())
            );
        }
    }

    /**
     * 1. 若为文件夹， 递归删除
     * 2. 若无映射指向文件实体，删除文件实体
     * 3. 文件实体 Reference-1
     */
    private void deleteSoftLink(FileMapping fileMapping, User user, boolean remove,
        Map<FileNamespace, List<FileDTO>> needRemove) {
        if (fileMapping == null) {
            return;
        }
        if (fileMapping.getIsDir()) {
            List<FileMapping> fileMappings = null;
            //TODO listFiles(fileMapping.getId(), user.getId(), Status.VALID);
            for (FileMapping mapping : fileMappings) {
                deleteSoftLink(mapping, user, remove, needRemove);
            }
        }
        if (remove) {
            Optional<FileInfo> fileInfoOpt = fileInfoImp.fetchById(fileMapping.getInfoId());
            if (fileInfoOpt.isPresent()) {
                FileInfo fileInfo = fileInfoOpt.get();
                // 删除文件映射后再无映射指向该文件实体, 物理删除
                if (fileInfo.getReference() <= 1) {
                    fileInfoImp.removeById(fileInfo.getId());
                    FileDTO fileDTO = new FileDTO(user.getId(), fileInfo.getNamespace(), fileInfo, null);
                    if (!needRemove.containsKey(fileDTO.getNamespace())) {
                        needRemove.put(fileDTO.getNamespace(), new LinkedList<>());
                    }
                    needRemove.get(fileDTO.getNamespace()).add(fileDTO);
                } else {
                    // 否则文件引用-1
                    fileInfo.setReference(fileInfo.getReference() - 1);
                    fileInfoImp.save(fileInfo);
                }
            }
        }
        fileMapping.setStatus(Status.INVALID);
        mappingService.save(fileMapping);
    }

    private FileManager handle(FileNamespace nameSpace) {
        return manager.handle(nameSpace);
    }


    public LicFileManagerImpl(FileManagerHandler manager, FileInfoService fileInfoImp,
                              FileMappingService mappingService, FileShareService fileShareImpl) {
        this.manager        = manager;
        this.fileInfoImp    = fileInfoImp;
        this.mappingService = mappingService;
        this.fileShareImpl  = fileShareImpl;
    }
}
