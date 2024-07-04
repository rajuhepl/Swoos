package com.example.swoos.service.serviceimpl;

import com.example.swoos.dto.MasterRoleDTO;
import com.example.swoos.dto.PasswordUpdateDTO;
import com.example.swoos.dto.UserDTO;
import com.example.swoos.dto.UserResponseDTO;
import com.example.swoos.model.MasterRole;
import com.example.swoos.model.User;
import com.example.swoos.repository.MasterRoleRepository;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.response.UserSignUpRequest;
import com.example.swoos.service.UserService;
import com.example.swoos.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MasterRoleRepository masterRoleRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public SuccessResponse<Object> userSignup(UserSignUpRequest userSignUpRequest) throws RuntimeException {
        SuccessResponse<Object> successResponse = null;
            successResponse = new SuccessResponse();
            if (Objects.nonNull(userSignUpRequest)) {
                User users = null;
                Optional<User> savedUser = this.userRepository.findByEmail(userSignUpRequest.getEmail());
                if (savedUser.isPresent()) {
                    throw new RuntimeException("Mail Already exists");
                }

                List<User> usersPresented = this.userRepository.findAll();
                MasterRole role;
                if (usersPresented.isEmpty()) {
                    role = this.masterRoleRepository.findByRoleName("Admin");
                } else {
                    role = this.masterRoleRepository.findByRoleName("Employee");
                }

                if (Objects.isNull(userSignUpRequest.getId())) {
                    User user = new User();
                    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                    user.setUsername(userSignUpRequest.getUsername());
                    user.setApplicationRole(role);
                    user.setEmail(userSignUpRequest.getEmail());
                    user.setDob(userSignUpRequest.getDob());
                    user.setMobileNumber(userSignUpRequest.getMobileNumber());
                    user.setFirstName(userSignUpRequest.getFirstName());
                    user.setLastName(userSignUpRequest.getLastName());
                    user.setIsActive(userSignUpRequest.isActive());
                    user.setDeleteFlag(!userSignUpRequest.isActive());
                    user.setPassword(bCryptPasswordEncoder.encode(userSignUpRequest.getPassword()));
                    user.setOgPassword(userSignUpRequest.getPassword());
                    this.userRepository.save(user);
                    successResponse.setData(Constant.USER_CREATED_SUCCESSFULLY);
                    log.info("user created");
                } else {
                    users = userRepository.findById(userSignUpRequest.getId()).orElseThrow(()->new RuntimeException("User not found"));
                    users.setUsername(userSignUpRequest.getUsername());
                    if (userSignUpRequest.getApplicationRole().getId() > 0L) {
                       masterRoleRepository.findById(userSignUpRequest.getApplicationRole().getId())
                               .ifPresent(users::setApplicationRole);

                    }
                    users.setEmail(userSignUpRequest.getEmail());
                    users.setDob(userSignUpRequest.getDob());
                    users.setMobileNumber(userSignUpRequest.getMobileNumber());
                    users.setFirstName(userSignUpRequest.getFirstName());
                    users.setLastName(userSignUpRequest.getLastName());
                    users.setIsActive(userSignUpRequest.isActive());
                    users.setDeleteFlag(!userSignUpRequest.isActive());
                    this.userRepository.save(users);
                    successResponse.setData(Constant.USER_CREATED_SUCCESSFULLY);
                }
            }

            return successResponse;

    }

    private void setRole(UserSignUpRequest userSignUpRequest, User user) {
        if (userSignUpRequest.getApplicationRole().getId()>0) {
            Optional<MasterRole> masterRole = masterRoleRepository.findById
                    (userSignUpRequest.getApplicationRole().getId());
            if (masterRole.isPresent()) {
                user.setApplicationRole(masterRole.get());

            } else {
                MasterRole masterRole1 = new MasterRole();
                masterRole1.setId(userSignUpRequest.getApplicationRole().getId());
                masterRole1.setRoleName(userSignUpRequest.getApplicationRole().getRoleName());
                masterRole1.setIsActive(true);
                masterRole1.setDeletedFlag(false);
                masterRoleRepository.save(masterRole1);
                user.setApplicationRole(masterRole1);
            }
        }
    }

    private static void setActive(UserSignUpRequest userSignUpRequest, User user) {
        user.setIsActive(userSignUpRequest.isActive());
        user.setDeleteFlag(!userSignUpRequest.isActive());
    }

    @Override
    public SuccessResponse<Object> getUserById(String id) {

        SuccessResponse<Object> response = new SuccessResponse<>();
        try {
            if (Objects.nonNull(id)) {
                Optional<User> user = userRepository.findById(Long.valueOf(id));
                if (user.isPresent()) {
                    UserDTO userDTO = modelMapper.map(user, UserDTO.class);

                    Optional<MasterRole> masterRole = masterRoleRepository.findById(user.get().getApplicationRole().getId());
                    if (masterRole.isPresent()){
                        MasterRoleDTO masterRoleDTO = new MasterRoleDTO();
                        masterRoleDTO.setId(masterRole.get().getId());
                        masterRoleDTO.setRoleName(masterRole.get().getRoleName());
                        masterRoleDTO.setIsActive(masterRole.get().getIsActive());
                        userDTO.setApplicationRole(masterRoleDTO);
                    }

                    response.setStatusCode(200);
                    response.setStatusMessage("success");
                    response.setData(userDTO);
                }
            } else {
                response.setStatusCode(500);
                response.setStatusMessage("Data Not Found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }


    @Override
    public PageResponse<Object> getAllUser(Integer pageNo) {

        PageResponse<Object> pageResponse = new PageResponse<>();
        Pageable pageable = PageRequest.of(pageNo - 1, 15);
        List<UserResponseDTO> userDTOS = new LinkedList<>();

        try {
            Page<User> userList = userRepository.findAll(pageable);

            userList.forEach(user -> {
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setId((user.getId()));
                userResponseDTO.setUsername(user.getUsername());
                userResponseDTO.setEmail(user.getEmail());
                userResponseDTO.setApplicationRole(user.getApplicationRole().getRoleName());
                userResponseDTO.setCreatedAt(String.valueOf(user.getCreatedAt()));
                userDTOS.add(userResponseDTO);
            });

            Page<UserResponseDTO> userResponseDTOPage = new PageImpl<>(userDTOS,
                    pageable, userDTOS.size());
            pageResponse.setData(userResponseDTOPage.getContent());
            pageResponse.setHasNext(userResponseDTOPage.hasNext());
            pageResponse.setHasPrevious(userResponseDTOPage.hasPrevious());
            pageResponse.setTotalRecordCount(userResponseDTOPage.getTotalElements());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pageResponse;
    }

    @Override
    public String updatePassword(PasswordUpdateDTO pass) throws Exception {

        Optional<User> users = userRepository.findByEmail(pass.getEmail());
        if (users.isPresent()){
            User user = users.get();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if(bCryptPasswordEncoder.matches(pass.getOldPassword(), user.getPassword())){
                user.setPassword(bCryptPasswordEncoder.encode(pass.getNewPassword()));
                userRepository.save(user);
                return "The Password Updated";
            }else {
                throw new Exception(Constant.INVALID_CREDENTIALS);
            }
        }
        throw new Exception(Constant.INVALID_CREDENTIALS);
    }

}