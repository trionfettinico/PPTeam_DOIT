package com.github.trionfettinicoUNICAM.PPTeam_DOIT.controller;

import com.github.trionfettinicoUNICAM.PPTeam_DOIT.exception.EntityNotFoundException;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.model.Organization;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.model.Skill;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.model.UserEntity;

import java.util.List;

public interface OrganizationsController extends EntityController<Organization, String>{

    List<Organization> getByUser(String userMail);

    List<UserEntity> getUsers(String organizationId) throws EntityNotFoundException;

    void addCollaborator(String organizationId, String userMail, Skill skill) throws EntityNotFoundException;

    void addExpert(String organizationId, String userMail, Skill skill) throws EntityNotFoundException;

    boolean addMember(String organizationId, String userMail) throws EntityNotFoundException;

    boolean removeMember(String organizationId, String userMail) throws EntityNotFoundException;

    List<Organization> findByCreator(String userMail);

}
