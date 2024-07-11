package com.example.swoos.service.serviceimpl;

import com.example.swoos.configure.UserContextHolder;
import com.example.swoos.dto.*;
import com.example.swoos.model.MasterRole;
import com.example.swoos.model.User;
import com.example.swoos.model.UserProfile;
import com.example.swoos.repository.ColumnRepository;
import com.example.swoos.repository.MasterRoleRepository;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.response.UserSignUpRequest;
import com.example.swoos.service.UserService;
import com.example.swoos.util.Constant;
import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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
             User user = new User();
            if (Objects.nonNull(userSignUpRequest)) {
                User users = null;
                Optional<User> savedUser = userRepository.findByEmail(userSignUpRequest.getEmail());
                if (savedUser.isPresent()) {
                    throw new RuntimeException("Mail Already exists");
                }

                List<User> usersPresented = userRepository.findAll();
                MasterRole role;
                if (usersPresented.isEmpty()) {
                    role = masterRoleRepository.findByRoleName("Admin");
                } else {
                    role = masterRoleRepository.findByRoleName("Employee");
                }

                if (Objects.isNull(userSignUpRequest.getId())) {
                    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                    user.setUsername(userSignUpRequest.getUsername());
                    user.setApplicationRole(role);
                    user.setEmail(userSignUpRequest.getEmail());
                    user.setDob(userSignUpRequest.getDob());
                    user.setMobileNumber(userSignUpRequest.getMobileNumber());
                    user.setFirstName(userSignUpRequest.getFirstName());
                    user.setLastName(userSignUpRequest.getLastName());
                    user.setActive(userSignUpRequest.isActive());
                    user.setDeleteFlag(!userSignUpRequest.isActive());
                    user.setPassword(bCryptPasswordEncoder.encode(userSignUpRequest.getPassword()));
                    user.setOgPassword(userSignUpRequest.getPassword());
                    userRepository.save(user);
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
                    users.setActive(userSignUpRequest.isActive());
                    users.setDeleteFlag(!userSignUpRequest.isActive());
                    this.userRepository.save(users);
                    successResponse.setData(Constant.USER_CREATED_SUCCESSFULLY);
                }
            }
        UserProfile userProfile = new UserProfile();
        userProfile.setAsin(true);
        userProfile.setBrand(true);
        userProfile.setCategory(true);
        userProfile.setBangalore(true);
        userProfile.setDate(true);
        userProfile.setChannel(true);
        userProfile.setPname(true);
        userProfile.setRevenue(true);
        userProfile.setDaySales(true);
        userProfile.setDivision(true);
        userProfile.setSubCategory(true);
        userProfile.setIndore(true);
        userProfile.setDelhi(true);
        userProfile.setMumbai(true);
        userProfile.setNagpur(true);
        userProfile.setPatna(true);
        userProfile.setPune(true);
        userProfile.setAhmedabad(true);
        userProfile.setChennai(true);
        userProfile.setHyderabad(true);
        userProfile.setCalcutta(true);
        userProfile.setReason(true);
        userProfile.setRemarks(true);
        userProfile.setDownload(true);
        userProfile.setValueLoss(true);
        userProfile.setOther(true);
        userProfile.setSwooscontribution(true);
        userProfile.setSwoosPercentage(true);
        userProfile.setUser(user);
        columnRepository.save(userProfile);
        return successResponse;

    }
    @Autowired
    private ColumnRepository columnRepository;

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
                masterRole1.setActive(true);
                masterRole1.setDeletedFlag(false);
                masterRoleRepository.save(masterRole1);
                user.setApplicationRole(masterRole1);
            }
        }
    }

    private static void setActive(UserSignUpRequest userSignUpRequest, User user) {
        user.setActive(userSignUpRequest.isActive());
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
                        masterRoleDTO.setIsActive(masterRole.get().isActive());
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
                throw new RuntimeException(Constant.INVALID_CREDENTIALS);
            }
        }
        throw new RuntimeException(Constant.INVALID_CREDENTIALS);
    }

    @Override
    public String addColumn(ColumnDto columnDto) {
        UserDTO userDTO =  UserContextHolder.getUserDto();
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(()->new NoSuchElementException("User not found"));
        UserProfile userProfile = columnRepository.findByUser(user)
                .orElseThrow(()->new NoSuchElementException("User not found"));
        userProfile.setUser(user);
        userProfile.setAsin(columnDto.isAsin());
        userProfile.setBrand(columnDto.isBrand());
        userProfile.setCategory(columnDto.isCategory());
        userProfile.setBangalore(columnDto.isBangalore());
        userProfile.setDate(false);
        userProfile.setChannel(columnDto.isPlatform());
        userProfile.setPname(columnDto.isPname());
        userProfile.setRevenue(columnDto.isRevenue());
        userProfile.setDaySales(columnDto.isDaySales());
        userProfile.setDivision(columnDto.isInternalDivision());
        userProfile.setSubCategory(columnDto.isSubCategory());
        userProfile.setIndore(columnDto.isIndore());
        userProfile.setDelhi(columnDto.isDelhi());
        userProfile.setMumbai(columnDto.isMumbai());
        userProfile.setNagpur(columnDto.isNagpur());
        userProfile.setPatna(columnDto.isPatna());
        userProfile.setPune(columnDto.isPune());
        userProfile.setAhmedabad(columnDto.isAhmedabad());
        userProfile.setChennai(columnDto.isChennai());
        userProfile.setHyderabad(columnDto.isHyderabad());
        userProfile.setCalcutta(columnDto.isCalcutta());
        userProfile.setReason(columnDto.isReason());
        userProfile.setRemarks(columnDto.isRemarks());
        userProfile.setValueLoss(columnDto.isValueLoss());
        userProfile.setOther(columnDto.isOther());
        userProfile.setSNo(columnDto.isSNo());
        userProfile.setSwooscontribution(columnDto.isSwooscontribution());
        userProfile.setSwoosPercentage(columnDto.isSwoosPercentage());
        columnRepository.save(userProfile);

        return "Column added successfully";
    }

    @Override

    public ColumnDto getAllColumns() {
        User userEntity = userRepository.findById(UserContextHolder.getUserDto().getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserProfile userProfile = columnRepository.findByUser(userEntity)
                .orElseThrow(() -> new NoSuchElementException("UserProfile not found for the given user"));

        ColumnDto columnDto = new ColumnDto();
        columnDto.setSNo(userProfile.isSNo());
        columnDto.setPlatform(userProfile.isChannel());
        columnDto.setPname(userProfile.isPname());
        columnDto.setAsin(userProfile.isAsin());
        columnDto.setRevenue(userProfile.isRevenue());
        columnDto.setDaySales(userProfile.isDaySales());
        columnDto.setInternalDivision(userProfile.isDivision());
        columnDto.setBrand(userProfile.isBrand());
        columnDto.setCategory(userProfile.isCategory());
        columnDto.setSubCategory(userProfile.isSubCategory());
        columnDto.setIndore(userProfile.isIndore());
        columnDto.setDelhi(userProfile.isDelhi());
        columnDto.setMumbai(userProfile.isMumbai());
        columnDto.setNagpur(userProfile.isNagpur());
        columnDto.setPatna(userProfile.isPatna());
        columnDto.setPune(userProfile.isPune());
        columnDto.setAhmedabad(userProfile.isAhmedabad());
        columnDto.setBangalore(userProfile.isBangalore());
        columnDto.setChennai(userProfile.isChennai());
        columnDto.setHyderabad(userProfile.isHyderabad());
        columnDto.setCalcutta(userProfile.isCalcutta());
        columnDto.setReason(userProfile.isReason());
        columnDto.setRemarks(userProfile.isRemarks());
        columnDto.setValueLoss(userProfile.isValueLoss());
        columnDto.setOther(userProfile.isOther());
        columnDto.setSwooscontribution(userProfile.isSwooscontribution());
        columnDto.setSwoosPercentage(userProfile.isSwoosPercentage());
        return columnDto;
    }

}