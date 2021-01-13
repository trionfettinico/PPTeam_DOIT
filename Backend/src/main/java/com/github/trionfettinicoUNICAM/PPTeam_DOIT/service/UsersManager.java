package com.github.trionfettinicoUNICAM.PPTeam_DOIT.service;

import com.github.trionfettinicoUNICAM.PPTeam_DOIT.model.User;


public interface UsersManager {
    User getUserInstance(String userMail);
    User createUser(String mail, String name);
    boolean deleteUser(String mail);
    boolean updateUser(User user);
    boolean exists(String userMail);
}