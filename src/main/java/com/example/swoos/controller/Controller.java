package com.example.swoos.controller;

import com.example.swoos.dto.MasterRoleDTO;
import com.example.swoos.dto.UserDTO;
import com.example.swoos.model.MasterRole;
import com.example.swoos.model.User;
import com.example.swoos.repository.MasterRoleRepository;
import com.example.swoos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
public class Controller {

    @Autowired
    UserRepository userRepo;

    @Autowired
    MasterRoleRepository roleRepo;

    @PostMapping("/save/user")
    public String saveUser(@RequestBody UserDTO user, @RequestParam("id") Long id){
        if (user.getId() > 0){
            User users = new User();
            users.setUsername(user.getUsername());
           // users.setPassword(user.getPassword());
            users.setEmail(user.getEmail());
            userRepo.save(users);
            return "saved";
        } else if (user.getId() > 0 && id == null) {
            Optional<User> users = userRepo.findById(Long.valueOf(user.getId()));
            if (users.isPresent()){
                User user1 = users.get();
                user1.setEmail(user.getEmail());
                user1.setUsername(user.getUsername());
               // user1.setPassword(user.getPassword());
                userRepo.save(user1);
                return "updated";
            }
            return "null";
        } else if (user.getId() > 0 && id != null) {
            Optional<User> users = userRepo.findById(user.getId());
            Optional<MasterRole> roll = roleRepo.findById(id);
            if (users.isPresent() && roll.isPresent()){
                User user1 = users.get();
                MasterRole roll1 = roll.get();
                user1.setEmail(user.getEmail());
                user1.setUsername(user.getUsername());
                // user1.setApplicationRole(roll1);
               // user1.setPassword(user.getPassword());
                userRepo.save(user1);
                return "updated with roll";
            }
            return "null";
        } else {
            return "failed";
        }
    }

    @PostMapping("/save/roll")
    public String saveRoll(@RequestBody MasterRoleDTO rollDTO){
        if(rollDTO.getId()== null || rollDTO.getId() <=0) {
            MasterRole roll = new MasterRole();
            roll.setRoleName(rollDTO.getRoleName());
            roleRepo.save(roll);
            return "saved";
        }else if (rollDTO.getId() > 0){
            Optional<MasterRole> roll = roleRepo.findById(rollDTO.getId());
            if (roll.isPresent()){
                MasterRole roll1 = roll.get();
                roll1.setRoleName(rollDTO.getRoleName());
                roleRepo.save(roll1);
                return "updated";
            }
            return "null";
        }else {
            return "failed";
        }
    }
//@PostMapping("/save/roll")
//public String saveRoll(@RequestBody MasterRoleDTO rollDTO) {
//    if (rollDTO.getId() == null) {
//        return "failed";
//    }
//
//    Long id = rollDTO.getId();
//
//    if (id > 0) {
//        Optional<MasterRole> rollOpt = roleRepo.findById(id);
//        if (rollOpt.isPresent()) {
//            MasterRole roll = rollOpt.get();
//            roll.setRoleName(rollDTO.getRoleName());
//            roleRepo.save(roll);
//            return "updated";
//        } else {
//            return "not found";
//        }
//    } else {
//        MasterRole roll = new MasterRole();
//        roll.setRoleName(rollDTO.getRoleName());
//        roleRepo.save(roll);
//        return "saved";
//    }
//}

}

