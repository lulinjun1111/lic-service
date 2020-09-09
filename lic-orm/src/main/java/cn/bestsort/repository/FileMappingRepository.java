package cn.bestsort.repository;

import java.util.List;

import cn.bestsort.model.FileMapping;
import cn.bestsort.enums.Status;

/**
 * @author bestsort
 * @version 1.0
 * @date 2020-09-07 17:39
 */
public interface FileMappingRepository extends BaseRepository<FileMapping, Long> {
    /**
     * 获取当前目录下用户的文件列表
     **/
    List<FileMapping> findAllByPidAndOwnerIdAndStatus(Long pid, Long userId, Status status);
}
