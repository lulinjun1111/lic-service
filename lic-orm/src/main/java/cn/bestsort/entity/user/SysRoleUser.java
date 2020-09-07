package cn.bestsort.entity.user;

import cn.bestsort.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;


/**
 * @author GoodTime0313
 * @version 1.0
 * @date 2020/9/4 23:39
 */

@Data
@Entity
@ToString
@EqualsAndHashCode
public class SysRoleUser extends BaseEntity{

    @Column
    private Long userId;

    @Column
    private Long roleId;
}