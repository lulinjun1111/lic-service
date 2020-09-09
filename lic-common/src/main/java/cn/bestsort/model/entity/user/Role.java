package cn.bestsort.model.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;

import cn.bestsort.model.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

/**
 * @author GoodTime0313
 * @version 1.0
 * @date 2020/9/4 23:12
 */

@Data
@Entity
@ToString
public class Role extends BaseEntity  {

    @Column(nullable = false,length = 50)
    private String roleName;

    @Column(nullable = false,length = 100)
    private String roleDescription;

}