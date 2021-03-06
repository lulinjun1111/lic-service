package cn.bestsort.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bestsort.model.dto.UserDTO;
import cn.bestsort.model.entity.RoleUser;
import cn.bestsort.model.entity.User;
import cn.bestsort.model.entity.User.Status;
import cn.bestsort.repository.impl.RepositoryEntity;
import cn.bestsort.repository.RoleUserRepository;
import cn.bestsort.repository.UserRepository;
import cn.bestsort.service.AbstractBaseService;
import cn.bestsort.service.RoleUserService;
import cn.bestsort.service.UserService;
import cn.bestsort.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author GoodTime0313
 * @version 1.0
 * @date 2020/9/15 8:59
 */
@Slf4j
@Service
public class UserServiceImpl extends AbstractBaseService<User, Long> implements UserService {

    @Autowired
    private RoleUserService roleUserService;
    @Autowired
    private RoleUserRepository roleUserRepository;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User saveUser(UserDTO userDto) {
        User user = new User();
        SpringUtil.cloneWithoutNullVal(userDto, user);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setStatus(Status.VALID);
        user.setTotalCapacity(userDto.getTotalCapacity());
        save(user);
        saveUserRoles(user.getId(), userDto.getRoleIds());
        log.debug("新增用户{}", user.getUsername());
        return user;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null) {
            roleUserRepository.deleteAllByUserId(userId);
            if (!CollectionUtils.isEmpty(roleIds)) {
                List<RoleUser> list = new LinkedList<>();
                roleIds.forEach(i -> list.add(RoleUser.of(userId, i)));
                roleUserService.saveAll(list);
            }
        }
    }

    @Override
    public User getUser(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User u = userRepo.findByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, u.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        update(u, u.getId());
        log.debug("修改{}的密码", username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(UserDTO userDto) {
        User user = new User();
        SpringUtil.cloneWithoutNullVal(userDto, user);
        update(user, user.getId());
        saveUserRoles(userDto.getId(), userDto.getRoleIds());
        return userDto;
    }

    @Override
    public int count(Map<String, Object> params) {
        String username = (String) params.get("username");
        String nickname = (String) params.get("nickname");
        String status = (String) params.get("status");
        if ("".equals(status)) {
            return userRepo.count(username, nickname, null);
        }
        return userRepo.count(username, nickname, Integer.valueOf(status));
    }

    @Autowired
    private RepositoryEntity ure;

    @Override
    public List<User> list(Map<String, Object> params, int offset, int limit) {
        String username = (String) params.get("username");
        String nickname = (String) params.get("nickname");
        String status = (String) params.get("status");
        String orderBy = (String) params.get("orderBy");
        if ("".equals(status)) {
            return ure.listUser(username, nickname, null, orderBy, offset, limit);
        }
        return ure.listUser(username, nickname, Integer.valueOf(status), orderBy, offset, limit);
    }

    protected UserServiceImpl(UserRepository repository) {
        super(repository);
        userRepo = repository;
    }


}
